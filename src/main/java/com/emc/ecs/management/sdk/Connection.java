package com.emc.ecs.management.sdk;

import com.emc.ecs.management.sdk.model.EcsManagementClientError;
import com.emc.ecs.servicebroker.exception.EcsManagementClientException;
import com.emc.ecs.servicebroker.exception.EcsManagementClientUnauthorizedException;
import com.emc.ecs.servicebroker.exception.EcsManagementResourceNotFoundException;
import org.apache.juli.JdkLoggerFormatter;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.jersey.logging.LoggingFeature;
import org.slf4j.LoggerFactory;
import sun.net.www.protocol.http.HttpURLConnection;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;

import static com.emc.ecs.management.sdk.Constants.*;

public class Connection {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(Connection.class);

    private static final int AUTH_RETRIES_MAX = 3;

    private final String endpoint;
    private final String username;
    private final String password;
    private String authToken;
    private String certificate;
    private int maxLoginSessionLength;

    private int authRetries = 0;
    private Instant authExpiration = null;

    private static final LoggingFeature loggingFeature = new LoggingFeature(
            setupHttpLogger(),
            Level.FINE, LoggingFeature.Verbosity.HEADERS_ONLY, 2048
    );

    public Connection(String endpoint, String username, String password) {
        super();
        this.endpoint = endpoint;
        this.username = username;
        this.password = password;
    }

    public Connection(String endpoint, String username, String password,
                      String certificate) {
        super();
        this.endpoint = endpoint;
        this.username = username;
        this.password = password;
        this.certificate = certificate;
    }

    private static HostnameVerifier getHostnameVerifier() {
        return (hostname, session) -> true;
    }

    public String getAuthToken() {
        return authToken;
    }

    private Client buildJerseyClient() throws EcsManagementClientException {
        ClientBuilder builder;
        if (certificate != null) {
            // Disable host name verification. Should be able to configure the
            // ECS certificate with the correct host name to avoid this.
            HostnameVerifier hostnameVerifier = getHostnameVerifier();
            HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);
            builder = ClientBuilder.newBuilder()
                    .register(hostnameVerifier);
            builder.sslContext(getSSLContext());
        } else {
            builder = ClientBuilder.newBuilder();
        }

        builder = builder.register(loggingFeature);

        return builder.build();
    }

    private SSLContext getSSLContext() throws EcsManagementClientException {
        try {
            CertificateFactory certFactory;

            //InputStream targetStream = new ByteArrayInputStream(initialString.getBytes());
            //InputStream certInputStream = certificate.openStream();

            InputStream certInputStream = new ByteArrayInputStream(certificate.getBytes());

            SSLContext sslContext = SSLContext.getInstance("TLS");
            certFactory = CertificateFactory.getInstance("X.509");
            Certificate caCert = certFactory
                    .generateCertificate(certInputStream);
            TrustManagerFactory trustMgrFactory = TrustManagerFactory
                    .getInstance(TrustManagerFactory.getDefaultAlgorithm());
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null);
            keyStore.setCertificateEntry("caCert", caCert);
            trustMgrFactory.init(keyStore);
            sslContext.init(null, trustMgrFactory.getTrustManagers(), null);
            return sslContext;
        } catch (Exception e) {
            throw new EcsManagementClientException(e);
        }
    }

    public boolean isLoggedIn() {
        return authToken != null;
    }

    public boolean sessionExpired() {
        return isLoggedIn() && authExpiration != null && authExpiration.isBefore(Instant.now());
    }

    public void login() throws EcsManagementClientException {
        UriBuilder uriBuilder = UriBuilder.fromPath(endpoint).segment(LOGIN);

        logger.info("Logging into {} as {}", endpoint, username);

        HttpAuthenticationFeature authFeature = HttpAuthenticationFeature
                .basicBuilder().credentials(username, password).build();
        Client jerseyClient = buildJerseyClient().register(authFeature);

        Builder request = jerseyClient.target(uriBuilder).request();

        Response response = null;

        try {
            response = request.get();
            handleResponse(response);

            this.authToken = response.getHeaderString(X_SDS_AUTH_TOKEN);
            this.authRetries = 0;

            if (maxLoginSessionLength > 0) {
                this.authExpiration = Instant.now().plus(maxLoginSessionLength, ChronoUnit.MINUTES);
            } else {
                this.authExpiration = null;
            }
        } catch (EcsManagementResourceNotFoundException e) {
            logger.warn("Login failed to handle response: {}", e.getMessage());
            logger.warn("Response: {}", response);

            throw new EcsManagementClientException(e);
        } catch (EcsManagementClientUnauthorizedException e) {
            logger.error("Failed to login to ECS management endpoint {}: {}", endpoint, e.getMessage());
            throw new EcsManagementClientUnauthorizedException("Login attempt failed: " + e.toString(), e);
        } catch (Exception e) {
            logger.error("Failed to login to ECS management endpoint {}: {}", endpoint, e.getMessage());
            throw new EcsManagementClientException(e.getMessage(), e);
        }
    }

    public void logout() throws EcsManagementClientException {
        this.authToken = null;
        this.authExpiration = null;
        // UriBuilder uri = UriBuilder.fromPath(endpoint).segment(LOGOUT)
        //         .queryParam("force", true);
        // handleRemoteCall(GET, uri, null);
    }

    protected Response handleRemoteCall(String method, UriBuilder uri, Object arg) throws EcsManagementClientException {
        return handleRemoteCall(method, uri, arg, XML);
    }

    protected Response handleRemoteCall(String method, UriBuilder uri,
                                        Object arg, String contentType) throws EcsManagementClientException {
        Response response = makeRemoteCall(method, uri, arg, contentType);
        try {
            handleResponse(response);
        } catch (EcsManagementResourceNotFoundException e) {
            throw new EcsManagementClientException(e);
        }
        return response;
    }

    protected UriBuilder getUriBuilder() {
        return UriBuilder.fromPath(endpoint);
    }

    protected Response makeRemoteCall(String method, UriBuilder uri, Object arg) throws EcsManagementClientException {
        return makeRemoteCall(method, uri, arg, XML);
    }

    protected Response makeRemoteCall(String method, UriBuilder uri, Object arg, String contentType)
            throws EcsManagementClientException {
        if (sessionExpired()) {
            logger.info("Session token expired after {} minutes", maxLoginSessionLength);
            logout();
        }

        if (!isLoggedIn()) {
            login();
        }

        try {
            logger.info("{} {}", method, uri);

            Client jerseyClient = buildJerseyClient();
            Builder request = jerseyClient.target(uri)
                    .request()
                    .header("X-EMC-Override", "true")            // enables access to ECS Flex API (pre-GA limitation)
                    .header(X_SDS_AUTH_TOKEN, authToken)
                    .header("Accept", APPLICATION_XML);

            Response response = null;
            if (GET.equals(method)) {
                response = request.get();
            } else if (POST.equals(method) || PUT.equals(method)) {
                Entity<Object> objectEntity;
                if (XML.equals(contentType)) {
                    objectEntity = Entity.xml(arg);
                } else if (JSON.equals(contentType)) {
                    objectEntity = Entity.json(arg);
                } else {
                    throw new EcsManagementClientException("Content type must be \"XML\" or \"JSON\"");
                }

                if (POST.equals(method)) {
                    response = request.post(objectEntity);
                } else if (PUT.equals(method)) {
                    response = request.put(objectEntity);
                }
            } else if (DELETE.equals(method)) {
                response = request.delete();
            } else {
                throw new EcsManagementClientException(
                        "Invalid request method: " + method);
            }

            if (response.getStatus() == 401 && authRetries < AUTH_RETRIES_MAX) {
                // attempt to re-authorize and retry up to _authMaxRetries_ times.
                authRetries += 1;
                this.authToken = null;
                this.authExpiration = null;
                response = makeRemoteCall(method, uri, arg, XML);
            }

            return response;
        } catch (Exception e) {
            logger.warn("Failed to make a call to {}: {}", uri, e.getMessage());
            throw e;
        }
    }

    protected boolean existenceQuery(UriBuilder uri, Object arg) throws EcsManagementClientException {
        Response response = makeRemoteCall(GET, uri, arg, XML);
        try {
            handleResponse(response);
        } catch (EcsManagementResourceNotFoundException e) {
            Logger.getAnonymousLogger().log(Level.FINE, "info", e);
            return false;
        }
        return true;
    }

    private void handleResponse(Response response) throws EcsManagementClientException, EcsManagementResourceNotFoundException {
        if (response.getStatus() > 399) {
            EcsManagementClientError error = response.readEntity(EcsManagementClientError.class);
            if (response.getStatus() == 404) {
                throw new EcsManagementResourceNotFoundException(response.getStatusInfo().toString());
            } else if (response.getStatus() == 401) {
                throw new EcsManagementClientUnauthorizedException(error.toString());
            } else if (error.getCode() == 1004) {
                throw new EcsManagementResourceNotFoundException(error.toString());
            } else {
                throw new EcsManagementClientException(error.toString());
            }
        }
    }

    private static Logger setupHttpLogger() {
        Level httpLoggerLevel;
        if (logger.isTraceEnabled()) {
            httpLoggerLevel = Level.ALL;
        } else if (logger.isDebugEnabled()) {
            httpLoggerLevel = Level.FINEST;
        } else if (logger.isInfoEnabled()) {
            httpLoggerLevel = Level.INFO;
        } else {
            httpLoggerLevel = Level.WARNING;
        }

        Logger httpLogger = Logger.getLogger(Connection.class.getCanonicalName());
        httpLogger.setUseParentHandlers(false);
        httpLogger.addHandler(new LegacyStreamHandler(logger));
        httpLogger.setLevel(httpLoggerLevel);

        logger.info("Http logger level set to {}", httpLogger.getLevel().getName());

        if (logger.isDebugEnabled()) {
            Logger.getLogger(HttpURLConnection.class.getName()).addHandler(
                    new LegacyStreamHandler(org.slf4j.LoggerFactory.getLogger(HttpURLConnection.class))
            );
            Logger.getLogger(HttpURLConnection.class.getName()).setLevel(httpLoggerLevel);
        }

        return httpLogger;
    }

    public String getCertificate() {
        return certificate;
    }

    public void setCertificate(String certificate) {
        this.certificate = certificate;
    }

    public int getMaxLoginSessionLength() {
        return maxLoginSessionLength;
    }

    public void setMaxLoginSessionLength(int maxLoginSessionLength) {
        this.maxLoginSessionLength = maxLoginSessionLength;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public int getAuthRetries() {
        return authRetries;
    }

    public void setAuthRetries(int authRetries) {
        this.authRetries = authRetries;
    }

    public Instant getAuthExpiration() {
        return authExpiration;
    }

    public void setAuthExpiration(Instant authExpiration) {
        this.authExpiration = authExpiration;
    }

    private static class LegacyStreamHandler extends StreamHandler {
        private org.slf4j.Logger log;

        public LegacyStreamHandler(org.slf4j.Logger logger) {
            super(System.err, new JdkLoggerFormatter());
            this.log = logger;
            this.setLevel(Level.ALL);
        }

        @Override
        public synchronized void publish(final LogRecord record) {
            String message = record.getMessage();
            message = message.replaceAll("X-SDS-AUTH-TOKEN: [\\w]+=", "X-SDS-AUTH-TOKEN: *****");
            message = message.replaceAll("Authorization: Basic [\\w]+=", "Authorization: Basic ******");
            if (record.getLevel().intValue() < Level.INFO.intValue() && log.isDebugEnabled()) {
                log.debug(message);
            } else if (record.getLevel().intValue() < Level.WARNING.intValue() && log.isInfoEnabled()) {
                log.info(message);
            } else {
                log.warn(message);
            }
        }
    }
}

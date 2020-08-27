package com.emc.ecs.management.sdk;

import com.emc.ecs.management.sdk.model.EcsManagementClientError;
import com.emc.ecs.servicebroker.EcsManagementClientException;
import com.emc.ecs.servicebroker.EcsManagementClientUnauthorizedException;
import com.emc.ecs.servicebroker.EcsManagementResourceNotFoundException;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.jersey.logging.LoggingFeature;
import org.slf4j.LoggerFactory;

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
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.emc.ecs.management.sdk.Constants.*;

public class Connection {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(Connection.class);

    private static final int AUTH_RETRIES_MAX = 3;
    private final String endpoint;
    private final String username;
    private final String password;
    private String authToken;
    private String certificate;
    private int authRetries = 0;

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

    public void login() throws EcsManagementClientException {
        UriBuilder uriBuilder = UriBuilder.fromPath(endpoint).segment("login");

        logger.info("Logging into {} as {}", endpoint, username);

        HttpAuthenticationFeature authFeature = HttpAuthenticationFeature
                .basicBuilder().credentials(username, password).build();
        Client jerseyClient = buildJerseyClient().register(authFeature);

        Builder request = jerseyClient.target(uriBuilder).request();

        Response response = null;

        try {
            response = request.get();
            handleResponse(response);

            this.authToken = response.getHeaderString("X-SDS-AUTH-TOKEN");
            this.authRetries = 0;
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
        UriBuilder uri = UriBuilder.fromPath(endpoint).segment("logout")
                .queryParam("force", true);
        handleRemoteCall(GET, uri, null);
        this.authToken = null;
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
        if (!isLoggedIn()) {
            login();
        }

        try {
            logger.info("{} {}", method, uri);

            Client jerseyClient = buildJerseyClient();
            Builder request = jerseyClient.target(uri)
                    .register(LoggingFeature.class).request()
                    .header("X-EMC-Override", "true")            // enables access to ECS Flex API (pre-GA limitation)
                    .header("X-SDS-AUTH-TOKEN", authToken)
                    .header("Accept", "application/xml");

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

    public String getCertificate() {
        return certificate;
    }

    public void setCertificate(String certificate) {
        this.certificate = certificate;
    }

}

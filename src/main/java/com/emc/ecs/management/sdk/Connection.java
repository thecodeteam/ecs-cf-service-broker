package com.emc.ecs.management.sdk;

import com.emc.ecs.cloudfoundry.broker.EcsManagementClientException;
import com.emc.ecs.cloudfoundry.broker.EcsManagementResourceNotFoundException;
import com.emc.ecs.management.sdk.model.EcsManagementClientError;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.jersey.logging.LoggingFeature;

import javax.net.ssl.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.io.InputStream;
import java.net.URL;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.emc.ecs.management.sdk.Constants.*;

public class Connection {
    private static final int AUTH_RETRIES_MAX = 3;
    private final String endpoint;
    private final String username;
    private final String password;
    private String authToken;
    private URL certificate;
    private int authRetries = 0;

    public Connection(String endpoint, String username, String password) {
        super();
        this.endpoint = endpoint;
        this.username = username;
        this.password = password;
    }

    public Connection(String endpoint, String username, String password,
                      URL certificate) {
        super();
        this.endpoint = endpoint;
        this.username = username;
        this.password = password;
        this.certificate = certificate;
    }

    private static HostnameVerifier getHostnameVerifier() {
        return new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };
    }

    public String getAuthToken() {
        return authToken;
    }

    private Client buildJerseyClient() throws EcsManagementClientException {
        /**
         * Disable host name verification. Should be able to configure the ECS
         * certificate with the correct host name to avoid this.
         **/
        HostnameVerifier hostnameVerifier = getHostnameVerifier();
        HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);
        ClientBuilder builder = JerseyClientBuilder.newBuilder()
                .register(hostnameVerifier);

        if (certificate != null) {
            builder.sslContext(getSSLContext());
        }
        return builder.build();
    }

    private SSLContext getSSLContext() throws EcsManagementClientException {
        try {
            CertificateFactory certFactory;
            InputStream certInputStream = certificate.openStream();
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
        HttpAuthenticationFeature authFeature = HttpAuthenticationFeature
                .basicBuilder().credentials(username, password).build();
        Client jerseyClient = buildJerseyClient().register(authFeature);

        Response response = jerseyClient.target(uriBuilder).request().get();
        try {
            handleResponse(response);
        } catch (EcsManagementResourceNotFoundException e) {
            throw new EcsManagementClientException(e);
        }
        this.authToken = response.getHeaderString("X-SDS-AUTH-TOKEN");
        this.authRetries = 0;
    }

    public void logout() throws EcsManagementClientException {
        UriBuilder uri = UriBuilder.fromPath(endpoint).segment("logout")
                .queryParam("force", true);
        handleRemoteCall(GET, uri, null);
        this.authToken = null;
    }

    protected Response handleRemoteCall(String method, UriBuilder uri,
            Object arg) throws EcsManagementClientException {
        Response response = makeRemoteCall(method, uri, arg);
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

    protected Response makeRemoteCall(String method, UriBuilder uri, Object arg)
            throws EcsManagementClientException {
        if (!isLoggedIn())
            login();
        Client jerseyClient = buildJerseyClient();
        Builder request = jerseyClient.target(uri)
                .register(LoggingFeature.class).request()
                .header("X-SDS-AUTH-TOKEN", authToken)
                .header("Accept", "application/xml");
        Response response;
        if (GET.equals(method)) {
            response = request.get();
        } else if (POST.equals(method)) {
            response = request.post(Entity.xml(arg));
        } else if (PUT.equals(method)) {
            response = request.put(Entity.xml(arg));
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
            response = makeRemoteCall(method, uri, arg);
        }
        return response;
    }

    protected boolean existenceQuery(UriBuilder uri, Object arg)
            throws EcsManagementClientException {
        Response response = makeRemoteCall(GET, uri, arg);
        try {
            handleResponse(response);
        } catch (EcsManagementResourceNotFoundException e) {
            Logger.getAnonymousLogger().log(Level.FINE, "info", e);
            return false;
        }
        return true;
    }

    private void handleResponse(Response response)
            throws EcsManagementClientException,
            EcsManagementResourceNotFoundException {
        if (response.getStatus() > 399) {
            EcsManagementClientError error = response
                    .readEntity(EcsManagementClientError.class);
            if (response.getStatus() == 404) {
                throw new EcsManagementResourceNotFoundException(
                        response.getStatusInfo().toString());
            } else if (error.getCode() == 1004) {
                throw new EcsManagementResourceNotFoundException(
                        error.toString());
            } else {
                throw new EcsManagementClientException(error.toString());
            }
        }
    }

    public URL getCertificate() {
        return certificate;
    }

    public void setCertificate(URL certificate) {
        this.certificate = certificate;
    }

}
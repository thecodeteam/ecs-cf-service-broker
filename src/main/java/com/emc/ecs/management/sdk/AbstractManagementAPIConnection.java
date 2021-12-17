package com.emc.ecs.management.sdk;

import com.emc.ecs.management.sdk.model.EcsManagementClientError;
import com.emc.ecs.servicebroker.exception.EcsManagementClientException;
import com.emc.ecs.servicebroker.exception.EcsManagementClientUnauthorizedException;
import com.emc.ecs.servicebroker.exception.EcsManagementResourceNotFoundException;
import org.slf4j.LoggerFactory;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.time.Instant;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.emc.ecs.management.sdk.ManagementAPIConstants.X_SDS_AUTH_TOKEN;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;

public abstract class AbstractManagementAPIConnection implements ManagementAPIConnection, LoginHandler {
    public static final int AUTH_RETRIES_MAX = 3;

    private final org.slf4j.Logger logger = LoggerFactory.getLogger(this.getClass());

    protected final String endpoint;
    protected final String username;
    protected final String password;
    protected String certificate;

    protected int maxLoginSessionLength;

    protected String authToken;

    protected int authRetries = 0;
    protected Instant authExpiration = null;

    protected final JerseyClientBuilder clientBuilder;

    public AbstractManagementAPIConnection(String endpoint, String username, String password, String certificate, boolean ignoreSslErrors) {
        super();
        this.endpoint = endpoint;
        this.username = username;
        this.password = password;
        this.clientBuilder = new JerseyClientBuilder(certificate, ignoreSslErrors, LoggingDebugFeature.prepareLoggingFeature(this.getClass()));
    }

    abstract public void login() throws EcsManagementClientException;
    abstract public void logout() throws EcsManagementClientException;

    public Response remoteCall(String method, UriBuilder uri, Object arg) throws EcsManagementClientException {
        return remoteCall(method, uri, arg, APPLICATION_XML, null);
    }

    public Response remoteCall(String method, UriBuilder uri, Object arg, Map<String, String> headers) throws EcsManagementClientException {
        String contentType = APPLICATION_XML;
        if (headers != null) {
            contentType = headers.getOrDefault(javax.ws.rs.core.HttpHeaders.CONTENT_TYPE, APPLICATION_XML);
        }
        return remoteCall(method, uri, arg, contentType, headers);
    }

    public Response remoteCall(String method, UriBuilder uri, Object arg, String contentType) throws EcsManagementClientException {
        return remoteCall(method, uri, arg, contentType, null);
    }

    public Response remoteCall(String method, UriBuilder uri, Object arg, String contentType, Map<String, String> headers) throws EcsManagementClientException {
        Response response = makeRemoteCall(method, uri, arg, contentType, headers);
        try {
            handleResponse(response);
        } catch (EcsManagementResourceNotFoundException e) {
            throw new EcsManagementClientException(e);
        }
        return response;
    }

    public boolean existenceQuery(UriBuilder uri, Object arg) throws EcsManagementClientException {
        return existenceQuery(uri, arg, null);
    }

    @Override
    public boolean existenceQuery(UriBuilder uri, Object arg, Map<String, String> headers) throws EcsManagementClientException {
        Response response = makeRemoteCall(HttpMethod.GET, uri, arg, APPLICATION_XML, headers);
        try {
            handleResponse(response);
        } catch (EcsManagementResourceNotFoundException e) {
            Logger.getAnonymousLogger().log(Level.FINE, "info", e);
            return false;
        }
        return true;
    }

    protected Response makeRemoteCall(String method, UriBuilder uri, Object arg, String contentType, Map<String, String> headers) throws EcsManagementClientException {
        if (sessionExpired()) {
            logger.info("Session token expired after {} minutes", maxLoginSessionLength);
            logout();
        }

        if (!isLoggedIn()) {
            login();
        }

        try {
            logger.debug("{} {}", method, uri);

            Builder request = clientBuilder.newClient()
                    .target(uri)
                    .request()
                    .header("X-EMC-Override", "true")            // enables access to ECS Flex API (pre-GA limitation)
                    .header(X_SDS_AUTH_TOKEN, authToken)
                    .header(HttpHeaders.ACCEPT, APPLICATION_XML);

            if (headers != null && headers.size() > 0) {
                for (Map.Entry<String, String> header : headers.entrySet()) {
                    request.header(header.getKey(), header.getValue());
                }
            }

            Response response = null;

            if (HttpMethod.GET.equals(method)) {
                response = request.get();
            } else if (HttpMethod.POST.equals(method)) {
                Entity<Object> objectEntity = getObjectEntity(arg, contentType);
                response = request.post(objectEntity);
            } else if (HttpMethod.PUT.equals(method)) {
                Entity<Object> objectEntity = getObjectEntity(arg, contentType);
                response = request.put(objectEntity);
            } else if (HttpMethod.DELETE.equals(method)) {
                response = request.delete();
            } else {
                throw new EcsManagementClientException("Invalid request method: " + method);
            }

            if (response.getStatus() == 401 && authRetries < AUTH_RETRIES_MAX) {
                // attempt to re-authorize and retry up to _authMaxRetries_ times.
                authRetries += 1;
                this.authToken = null;
                this.authExpiration = null;
                response = makeRemoteCall(method, uri, arg, APPLICATION_XML, headers);
            }

            return response;
        } catch (Exception e) {
            logger.warn("Failed to make a call to {}: {}", uri, e.getMessage());
            throw e;
        }
    }

    private static Entity<Object> getObjectEntity(Object arg, String contentType) {
        Entity<Object> objectEntity;
        if (APPLICATION_XML.equals(contentType)) {
            objectEntity = Entity.xml(arg);
        } else if (APPLICATION_JSON.equals(contentType)) {
            objectEntity = Entity.json(arg);
        } else {
            throw new EcsManagementClientException("Content type must be '" + APPLICATION_XML + "' or '" + APPLICATION_JSON + "'");
        }
        return objectEntity;
    }

    boolean isLoggedIn() {
        return authToken != null;
    }

    private boolean sessionExpired() {
        return isLoggedIn() && authExpiration != null && authExpiration.isBefore(Instant.now());
    }

    protected void handleResponse(Response response) throws EcsManagementClientException, EcsManagementResourceNotFoundException {
        if (response.getStatus() > 399) {
            if (response.getStatus() == 404) {
                throw new EcsManagementResourceNotFoundException(response.getStatusInfo().toString());
            }

            EcsManagementClientError error = response.readEntity(EcsManagementClientError.class);
            if (response.getStatus() == 404) {
                throw new EcsManagementResourceNotFoundException(response.getStatusInfo().toString());
            } else if (response.getStatus() == 401) {
                throw new EcsManagementClientUnauthorizedException(error.toString());
            } else if (error.getCode() == 1004) {
                // API_PARAMETER_NOT_FOUND
                throw new EcsManagementResourceNotFoundException(error.toString());
            } else if (error.getCode() == 1007) {
                // API_METHOD_NOT_SUPPORTED
                throw new EcsManagementClientException("API method not supported (" + error.toString() + ")");
            } else {
                throw new EcsManagementClientException(error.toString());
            }
        }
    }

    public UriBuilder uriBuilder() {
        return UriBuilder.fromPath(endpoint);
    }

    public String getCertificate() {
        return certificate;
    }

    public void setCertificate(String certificate) {
        this.certificate = certificate;
    }

    public String getAuthToken() {
        return authToken;
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

    public int getMaxLoginSessionLength() {
        return maxLoginSessionLength;
    }

    public void setMaxLoginSessionLength(int maxLoginSessionLength) {
        this.maxLoginSessionLength = maxLoginSessionLength;
    }
}

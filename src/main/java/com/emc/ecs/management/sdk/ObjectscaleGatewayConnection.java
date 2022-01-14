package com.emc.ecs.management.sdk;

import com.emc.ecs.servicebroker.exception.EcsManagementClientException;
import com.emc.ecs.servicebroker.exception.EcsManagementClientUnauthorizedException;
import com.emc.ecs.servicebroker.exception.EcsManagementResourceNotFoundException;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static com.emc.ecs.management.sdk.ManagementAPIConstants.X_SDS_AUTH_TOKEN;

public class ObjectscaleGatewayConnection extends AbstractManagementAPIConnection {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(ObjectscaleGatewayConnection.class);

    public ObjectscaleGatewayConnection(String endpoint, String username, String password, String certificate, boolean ignoreSslErrors) {
        super(endpoint, username, password, certificate, ignoreSslErrors);
    }

    public void login() throws EcsManagementClientException {
        logger.info("Logging into {} as {}", endpoint, username);

        UriBuilder loginURI = uriBuilder().segment("mgmt", "login");

        HttpAuthenticationFeature authFeature = HttpAuthenticationFeature
                .basicBuilder()
                .credentials(username, password)
                .build();

        Invocation.Builder request = clientBuilder.newClient()
                .register(authFeature)
                .target(loginURI)
                .request();

        Response response = null;

        try {
            response = request.get();

            handleErrorResponse(response);

            logger.info("Successful login!");

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
}

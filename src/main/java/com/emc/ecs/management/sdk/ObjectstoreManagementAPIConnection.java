package com.emc.ecs.management.sdk;

import com.emc.ecs.servicebroker.exception.EcsManagementClientException;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class ObjectstoreManagementAPIConnection extends AbstractManagementAPIConnection {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(ObjectstoreManagementAPIConnection.class);

    private LoginHandler objectscaleConnection;

    public ObjectstoreManagementAPIConnection(String endpoint, String certificate, boolean ignoreSslErrors, LoginHandler objectscaleConnection) {
        super(endpoint, null, null, certificate, ignoreSslErrors);
        this.objectscaleConnection = objectscaleConnection;
    }

    public void login() throws EcsManagementClientException {
        logger.info("Obtaining token from Objectscale");
        try {
            objectscaleConnection.login();

            logger.debug("Successful login!");

            this.authToken = objectscaleConnection.getAuthToken();
            this.authRetries = 0;

            if (maxLoginSessionLength > 0) {
                this.authExpiration = Instant.now().plus(maxLoginSessionLength, ChronoUnit.MINUTES);
            } else {
                this.authExpiration = null;
            }
        } catch (Exception e) {
            logger.error("Failed to login to objectscale endpoint: {}", e.getMessage());
            throw new EcsManagementClientException(e.getMessage(), e);
        }
    }

    public void logout() throws EcsManagementClientException {
        this.authToken = null;
        this.authExpiration = null;
    }
}

package com.emc.ecs.servicebroker.config;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/*
 * This class serves to restart the Application in the case of updating
 * the Application.yml file. This is necessary in Kubernetes if changes
 * are made as restarting the Pod does not update the application context.
 * Accessed via POST to /restart (requires auth)
 */

@RestController
public class RestartController {
    @PostMapping(value = "/restart")
    public void restart() {
        Thread restartThread = new Thread(() -> {
                Application.main();
        });
        restartThread.setDaemon(false);
        restartThread.start();
    }
}

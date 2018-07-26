package com.emc.ecs.management.simulator;

import com.github.tomakehurst.wiremock.WireMockServer;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

public class Server {

    public static void main(String[] args) {
        WireMockServer wireMockServerMgmt = new WireMockServer(
                wireMockConfig()
                .httpsPort(4443)
                .usingFilesUnderClasspath("wiremockMgmt"));
        wireMockServerMgmt.start();
	
        WireMockServer wireMockServerS3 = new WireMockServer(
                wireMockConfig()
                .port(9020)
                .httpsPort(9021)
                .usingFilesUnderClasspath("wiremockS3"));
        wireMockServerS3.start();
    }

}

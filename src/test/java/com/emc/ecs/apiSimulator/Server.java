package com.emc.ecs.apiSimulator;

import com.github.tomakehurst.wiremock.WireMockServer;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

public class Server {

	public static void main(String[] args) {
		WireMockServer wireMockServer = new WireMockServer(
				wireMockConfig()
					.port(9020) 	   // use HTTP port for s3
					.httpsPort(4443)); // use HTTPS port for ecs-management 
		wireMockServer.start();
	}

}

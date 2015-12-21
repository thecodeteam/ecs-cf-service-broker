package com.emc.ecs.apiSimulator;

import com.github.tomakehurst.wiremock.WireMockServer;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

public class Server {

	public static void main(String[] args) {
		WireMockServer wireMockServer = new WireMockServer(
				wireMockConfig().httpsPort(8443));
		wireMockServer.start();
	}

}

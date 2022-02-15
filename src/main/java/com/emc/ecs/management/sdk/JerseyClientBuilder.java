package com.emc.ecs.management.sdk;

import com.emc.ecs.servicebroker.exception.EcsManagementClientException;
import org.glassfish.jersey.logging.LoggingFeature;
import org.slf4j.LoggerFactory;

import javax.net.ssl.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import java.io.ByteArrayInputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public class JerseyClientBuilder {
    private String certificate;
    private boolean ignoreSslVerification;
    private LoggingFeature loggingFeature;

    public JerseyClientBuilder(String certificate, boolean ignoreSslVerification, LoggingFeature loggingFeature) {
        this.certificate = certificate;
        this.ignoreSslVerification = ignoreSslVerification;
        this.loggingFeature = loggingFeature;
    }

    public Client newClient() throws EcsManagementClientException {
        ClientBuilder builder = ClientBuilder.newBuilder();

        if (ignoreSslVerification) {
            LoggerFactory.getLogger(this.getClass()).info("Building JerseyClient with allCertsTrust enabled");
            SSLContext sslContext = trustAllCertsContext();
            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
            builder.sslContext(sslContext);
        } else if (this.certificate != null) {
            // Disable host name verification. Should be able to configure the
            // ECS certificate with the correct host name to avoid this.
            HostnameVerifier allHostsValid = (hostname, session) -> true;
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
            builder = builder.register(allHostsValid);

            SSLContext sslContext = getSSLContext(this.certificate);
            builder.sslContext(sslContext);
        }

        if (loggingFeature != null) {
            builder = builder.register(loggingFeature);
        }

        return builder.build();
    }

    private SSLContext getSSLContext(String certificate) throws EcsManagementClientException {
        try {
            Certificate caCert = CertificateFactory.getInstance("X.509")
                    .generateCertificate(
                            new ByteArrayInputStream(certificate.getBytes())
                    );

            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null);
            keyStore.setCertificateEntry("caCert", caCert);

            TrustManagerFactory trustMgrFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustMgrFactory.init(keyStore);

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustMgrFactory.getTrustManagers(), null);

            return sslContext;
        } catch (Exception e) {
            throw new EcsManagementClientException(e);
        }
    }

    private static SSLContext trustAllCertsContext() throws EcsManagementClientException {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
                public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                public void checkServerTrusted(X509Certificate[] certs, String authType) {}
            }};

            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());

            return sc;
        } catch (Exception e) {
            throw new EcsManagementClientException(e);
        }
    }
}

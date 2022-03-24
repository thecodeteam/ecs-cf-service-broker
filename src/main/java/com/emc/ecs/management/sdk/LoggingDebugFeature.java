package com.emc.ecs.management.sdk;

import org.apache.juli.JdkLoggerFormatter;
import org.glassfish.jersey.logging.LoggingFeature;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.HttpHeaders;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;

import static com.emc.ecs.management.sdk.ManagementAPIConstants.X_SDS_AUTH_TOKEN;

public class LoggingDebugFeature {
    public static LoggingFeature prepareLoggingFeature(Class connectionClass) {
        return new LoggingFeature(
                setupHttpLogger(connectionClass),
                Level.FINE, LoggingFeature.Verbosity.HEADERS_ONLY, 2048
        );
    }

    private static Logger setupHttpLogger(Class connectionClass) {
        org.slf4j.Logger logger = LoggerFactory.getLogger(connectionClass);

        Level slf4jLoggerLevel;
        if (logger.isTraceEnabled()) {
            slf4jLoggerLevel = Level.ALL;
        } else if (logger.isDebugEnabled()) {
            slf4jLoggerLevel = Level.FINEST;
        } else if (logger.isInfoEnabled()) {
            slf4jLoggerLevel = Level.INFO;
        } else {
            slf4jLoggerLevel = Level.WARNING;
        }

        Logger javaUtilLogger = Logger.getLogger(connectionClass.getCanonicalName());
        javaUtilLogger.setLevel(slf4jLoggerLevel);
        javaUtilLogger.setUseParentHandlers(false);

        if (logger.isDebugEnabled()) {
            // using FQDN class name as sun.net classes are not visible anymore
            Logger javaLogger = Logger.getLogger("sun.net.www.protocol.http.HttpURLConnection");
            if (javaLogger.getHandlers() == null || javaLogger.getHandlers().length == 0) {
                javaLogger.addHandler(
                        new LegacyStreamHandler(LoggerFactory.getLogger("sun.net.www.protocol.http.HttpURLConnection"))
                );
            }
            javaLogger.setLevel(slf4jLoggerLevel);
        }
        logger.info("Http logger level set to {}", javaUtilLogger.getLevel().getName());

        javaUtilLogger.addHandler(new LegacyStreamHandler(logger));

        return javaUtilLogger;
    }

    private static class LegacyStreamHandler extends StreamHandler {
        private org.slf4j.Logger log;

        public LegacyStreamHandler(org.slf4j.Logger logger) {
            super(System.err, new JdkLoggerFormatter());
            this.log = logger;
            this.setLevel(Level.ALL);
        }

        @Override
        public synchronized void publish(final LogRecord record) {
            String message = record.getMessage();
            message = message.replaceAll(X_SDS_AUTH_TOKEN + ": [\\w]+=", X_SDS_AUTH_TOKEN + ": *****");
            message = message.replaceAll(HttpHeaders.AUTHORIZATION + ": Basic [\\w]+=", HttpHeaders.AUTHORIZATION + ": Basic ******");
            if (record.getLevel().intValue() < Level.INFO.intValue() && log.isDebugEnabled()) {
                log.debug(message);
            } else if (record.getLevel().intValue() < Level.WARNING.intValue() && log.isInfoEnabled()) {
                log.info(message);
            } else {
                log.warn(message);
            }
        }
    }
}

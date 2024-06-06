package org.openmrs.module.insuranceclaims.util;

import org.openmrs.api.context.Context;
import javax.servlet.http.HttpServletRequest;

public class OpenmrsUtils {

    /**
     * Gets the base URL for the OpenMRS instance.
     *
     * @param request the HttpServletRequest object
     * @return the base URL as a String
     */
    public static String getOpenmrsBaseUrl(HttpServletRequest request) {
        String contextPath = request.getContextPath();
        String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + contextPath;
        return baseUrl;
    }

    /**
     * Gets the base URL for the OpenMRS instance without HttpServletRequest.
     *
     * @return the base URL as a String
     */
    public static String getOpenmrsBaseUrl() {
        String contextPath = Context.getAdministrationService().getGlobalProperty("webapp.name", "openmrs");
        String scheme = "http";
        int port = 8080;
        String serverName = "localhost";

        try {
            // Get scheme, server name and port from the runtime properties
            String runtimeScheme = Context.getRuntimeProperties().getProperty("webserver.scheme");
            String runtimeServerName = Context.getRuntimeProperties().getProperty("webserver.name");
            String runtimePort = Context.getRuntimeProperties().getProperty("webserver.port");

            if (runtimeScheme != null) {
                scheme = runtimeScheme;
            }
            if (runtimeServerName != null) {
                serverName = runtimeServerName;
            }
            if (runtimePort != null) {
                port = Integer.parseInt(runtimePort);
            }
        } catch (Exception e) {
            // Fallback to default values if any exception occurs
        }

        String baseUrl = scheme + "://" + serverName + ":" + port + "/" + contextPath;
        return baseUrl;
    }
}


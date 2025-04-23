package org.openmrs.module.insuranceclaims.api.client.impl;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.openmrs.api.context.Context;
import org.openmrs.module.insuranceclaims.api.client.FHIRClient;
import org.openmrs.module.insuranceclaims.api.client.FhirMessageConverter;
import org.openmrs.module.insuranceclaims.api.service.fhir.util.GeneralUtil;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.util.Base64Utils;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

public class FhirRequestClient implements FHIRClient {

    private RestTemplate restTemplate = new RestTemplate();

    private HttpHeaders headers = new HttpHeaders();

    private HttpMessageConverter<IBaseResource> converter = new FhirMessageConverter();

    public <T extends IBaseResource> T getObject(String url, Class<T> objectClass) throws URISyntaxException {
        prepareRestTemplate();
        setBasicRequestHeaders();
        ClientHttpEntity clientHttpEntity = createClientHttpEntity(url, HttpMethod.GET, null);
        ResponseEntity<T> response = sendRequest(clientHttpEntity, objectClass);
        return response.getBody();
    }

    private <L> ResponseEntity<L> sendRequest(ClientHttpEntity clientHttpEntity, Class<L> objectClass) {
        HttpEntity<Object> entity = new HttpEntity<>(clientHttpEntity.getBody(), headers);
        return restTemplate.exchange(clientHttpEntity.getUrl(), clientHttpEntity.getMethod(), entity, objectClass);
    }

    private void setBasicRequestHeaders() {
        headers = new HttpHeaders();
        String username = Context.getAdministrationService().getGlobalProperty("insuranceclaims.externalApiLogin");
        String password = Context.getAdministrationService().getGlobalProperty("insuranceclaims.externalApiPassword");
        // System.err.println("User: " + username + " Password: " + password);

        String auth = username + ":" + password;
        byte[] encodedAuth = Base64Utils.encode(auth.getBytes(StandardCharsets.UTF_8));
        String authHeader = "Basic " + new String(encodedAuth, StandardCharsets.UTF_8);
        headers.set(HttpHeaders.AUTHORIZATION, authHeader);

        headers.add(HttpHeaders.USER_AGENT, "ClientHelperUserAgent");
        headers.setContentType(MediaType.APPLICATION_JSON);
    }

    private void setJWTRequestHeaders() {
        headers = new HttpHeaders();
        String token = "";
        try {
            System.out.println("InsuranceClaims: Getting JWT token");
            token = GeneralUtil.getJWTAuthToken();
            System.out.println("InsuranceClaims: Got JWT token as: " + token);
        } catch(Exception ex) {
            System.err.println("InsuranceClaims: Failed to get JWT token: " + ex.getMessage());
            ex.printStackTrace();
        }

        String authHeader = "Bearer " + token;
        headers.set(HttpHeaders.AUTHORIZATION, authHeader);
        headers.set(HttpHeaders.REFERER, "");
        headers.set(HttpHeaders.USER_AGENT, "ClientHelperUserAgent");
        headers.setContentType(MediaType.APPLICATION_JSON);
    }

    private void setStagingRequestHeaders() {
        headers = new HttpHeaders();
        String token = "";
        try {
            System.out.println("InsuranceClaims: Getting Staging Bearer token");
           // token = GeneralUtil.getHIEStagingAuthToken();
            token = GeneralUtil.getJWTAuthToken();
            System.out.println("InsuranceClaims: Got Staging Bearer token as: " + token);
        } catch(Exception ex) {
            System.err.println("InsuranceClaims: Failed to get Staging Bearer token: " + ex.getMessage());
            ex.printStackTrace();
        }

        String authHeader = "Bearer " + token;
        headers.set(HttpHeaders.AUTHORIZATION, authHeader);
        headers.set(HttpHeaders.REFERER, "");
        headers.set(HttpHeaders.USER_AGENT, "ClientHelperUserAgent");
        headers.setContentType(MediaType.APPLICATION_JSON);
    }

    private void setApiKeyRequestHeaders() {
        headers = new HttpHeaders();
        String token = "";
        try {
            System.out.println("InsuranceClaims: Getting APIKEY token");
            token = GeneralUtil.getApiKeyAuthToken();
            System.out.println("InsuranceClaims: Got APIKEY token as: " + token);
        } catch(Exception ex) {
            System.err.println("InsuranceClaims: Failed to get APIKEY token: " + ex.getMessage());
            ex.printStackTrace();
        }

        headers.set("apiKey", token);
        headers.set("x-request-source", "HIE");
        headers.set(HttpHeaders.REFERER, "");
        headers.set(HttpHeaders.USER_AGENT, "ClientHelperUserAgent");
        headers.setContentType(MediaType.APPLICATION_JSON);
    }

    private <L> ClientHttpEntity createClientHttpEntity(String url, HttpMethod method, L object) throws URISyntaxException {
        ClientHttpEntity clientHttpEntity = new ClientHttpEntity();
        clientHttpEntity.setMethod(method);
        clientHttpEntity.setUrl(new URI(url));
        if (object != null) {
            clientHttpEntity.setBody(object);
        }
        return clientHttpEntity;
    }

    private void prepareRestTemplate() {
        List<HttpMessageConverter<?>> converters = new ArrayList<>();
        converters.add(converter);
        restTemplate.setMessageConverters(converters);
    }

    // Custom class for holding HTTP entity information
    private static class ClientHttpEntity {
        private URI url;
        private HttpMethod method;
        private Object body;

        public URI getUrl() {
            return url;
        }

        public void setUrl(URI url) {
            this.url = url;
        }

        public HttpMethod getMethod() {
            return method;
        }

        public void setMethod(HttpMethod method) {
            this.method = method;
        }

        public Object getBody() {
            return body;
        }

        public void setBody(Object body) {
            this.body = body;
        }
    }

    public <T,K extends IBaseResource> K postObject(String url, T object, Class<K> objectClass) throws URISyntaxException,
            HttpServerErrorException {
        prepareRestTemplate();
        // setJWTRequestHeaders(); // For JWT HIE Auth
        setStagingRequestHeaders(); // For HIE staging server
        url = GeneralUtil.removeTrailingSlash(url);
        System.out.println("InsuranceClaims: Sending claim bundle to: " + url);
        ClientHttpEntity clientHttpEntity = createClientHttpEntity(url, HttpMethod.POST, object);
        ResponseEntity<K> response = sendRequest(clientHttpEntity, objectClass);
        return response.getBody();
    }
}


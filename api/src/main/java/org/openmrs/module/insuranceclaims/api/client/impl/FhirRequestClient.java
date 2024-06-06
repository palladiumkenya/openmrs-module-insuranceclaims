package org.openmrs.module.insuranceclaims.api.client.impl;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Resource;
import org.openmrs.api.context.Context;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.util.Base64Utils;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import org.openmrs.module.insuranceclaims.api.client.FHIRClient;
import org.openmrs.module.insuranceclaims.api.client.FhirMessageConverter;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class FhirRequestClient implements FHIRClient {

    private RestTemplate restTemplate = new RestTemplate();

    private HttpHeaders headers = new HttpHeaders();

    private HttpMessageConverter<IBaseResource> converter = new FhirMessageConverter();

    public <T extends IBaseResource> T getObject(String url, Class<T> objectClass) throws URISyntaxException {
    // public <T extends Resource> T getObject(String url, Class<T> objectClass) throws URISyntaxException {
        prepareRestTemplate();
        setRequestHeaders();
        ClientHttpEntity clientHttpEntity = createClientHttpEntity(url, HttpMethod.GET, null);
        ResponseEntity<T> response = sendRequest(clientHttpEntity, objectClass);
        return response.getBody();
    }

    // public <T extends Resource, K extends Resource> K postObject(String url, T object, Class<K> objectClass) throws URISyntaxException, HttpServerErrorException {
    //     prepareRestTemplate();
    //     setRequestHeaders();
    //     ClientHttpEntity clientHttpEntity = createClientHttpEntity(url, HttpMethod.POST, object);
    //     ResponseEntity<K> response = sendRequest(clientHttpEntity, objectClass);
    //     return response.getBody();
    // }

    private <L> ResponseEntity<L> sendRequest(ClientHttpEntity clientHttpEntity, Class<L> objectClass) {
        HttpEntity<Object> entity = new HttpEntity<>(clientHttpEntity.getBody(), headers);
        return restTemplate.exchange(clientHttpEntity.getUrl(), clientHttpEntity.getMethod(), entity, objectClass);
    }

    private void setRequestHeaders() {
        headers = new HttpHeaders();
        String username = Context.getAdministrationService().getGlobalProperty("api.login.property");
        String password = Context.getAdministrationService().getGlobalProperty("api.password.property");

        String auth = username + ":" + password;
        byte[] encodedAuth = Base64Utils.encode(auth.getBytes(StandardCharsets.UTF_8));
        String authHeader = "Basic " + new String(encodedAuth, StandardCharsets.UTF_8);
        headers.set(HttpHeaders.AUTHORIZATION, authHeader);

        headers.add(HttpHeaders.USER_AGENT, "ClientHelperUserAgent");
        headers.setContentType(MediaType.APPLICATION_JSON);
    }

    // private void setRequestHeaders() {
    //     headers = new HttpHeaders();
    //     String username = Context.getAdministrationService().getGlobalProperty("api.login.property");
    //     String password = Context.getAdministrationService().getGlobalProperty("api.password.property");

    //     for (ClientHttpRequestInterceptor interceptor : getCustomInterceptors(username, password)) {
    //         interceptor.addToHeaders(headers);
    //     }

    //     headers.add(HttpHeaders.USER_AGENT, "ClientHelperUserAgent");
    //     headers.setContentType(MediaType.APPLICATION_JSON);
    // }

    // private List<ClientHttpRequestInterceptor> getCustomInterceptors(String username, String password) {
    //     List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();
    //     // Add your custom interceptors here
    //     // For example, basic authentication interceptor
    //     interceptors.add((request, body, execution) -> {
    //         HttpHeaders headers = request.getHeaders();
    //         String auth = username + ":" + password;
    //         byte[] encodedAuth = Base64Utils.encode(auth.getBytes(StandardCharsets.UTF_8));
    //         String authHeader = "Basic " + new String(encodedAuth, StandardCharsets.UTF_8);
    //         headers.set(HttpHeaders.AUTHORIZATION, authHeader);
    //         return execution.execute(request, body);
    //     });
    //     return interceptors;
    // }

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
        setRequestHeaders();
        ClientHttpEntity clientHttpEntity = createClientHttpEntity(url, HttpMethod.POST, object);
        ResponseEntity<K> response = sendRequest(clientHttpEntity, objectClass);
        return response.getBody();
    }
}





// import org.hl7.fhir.instance.model.api.IBaseResource;
// import org.openmrs.api.context.Context;
// import org.openmrs.module.fhir.api.client.ClientHttpEntity;
// import org.openmrs.module.fhir.api.client.ClientHttpRequestInterceptor;
// import org.openmrs.module.fhir.api.helper.ClientHelper;
// import org.openmrs.module.fhir.api.helper.FHIRClientHelper;
// import org.openmrs.module.insuranceclaims.api.client.FHIRClient;
// import org.openmrs.module.insuranceclaims.api.client.FhirMessageConventer;
// import org.springframework.http.HttpEntity;
// import org.springframework.http.HttpHeaders;
// import org.springframework.http.HttpMethod;
// import org.springframework.http.MediaType;
// import org.springframework.http.ResponseEntity;
// import org.springframework.http.converter.AbstractHttpMessageConverter;
// import org.springframework.http.converter.HttpMessageConverter;
// import org.springframework.web.client.HttpServerErrorException;
// import org.springframework.web.client.RestTemplate;

// import java.net.URI;
// import java.net.URISyntaxException;
// import java.util.ArrayList;
// import java.util.List;

// import static org.openmrs.module.insuranceclaims.api.client.ClientConstants.API_LOGIN_PROPERTY;
// import static org.openmrs.module.insuranceclaims.api.client.ClientConstants.API_PASSWORD_PROPERTY;
// import static org.openmrs.module.insuranceclaims.api.client.ClientConstants.CLIENT_HELPER_USER_AGENT;
// import static org.openmrs.module.insuranceclaims.api.client.ClientConstants.USER_AGENT;

// public class FhirRequestClient implements FHIRClient {

//     private RestTemplate restTemplate = new RestTemplate();

//     private ClientHelper fhirClientHelper = new FHIRClientHelper();

//     private HttpHeaders headers = new HttpHeaders();

//     private AbstractHttpMessageConverter<IBaseResource> conventer = new FhirMessageConventer();

//     public <T extends IBaseResource> T getObject(String url, Class<T> objectClass) throws URISyntaxException {
//         prepareRestTemplate();
//         setRequestHeaders();
//         ClientHttpEntity clientHttpEntity = fhirClientHelper.retrieveRequest(url);
//         ResponseEntity<T> response = sendRequest(clientHttpEntity, objectClass);
//         return response.getBody();
//     }

//     public <T,K extends IBaseResource> K postObject(String url, T object, Class<K> objectClass) throws URISyntaxException,
//             HttpServerErrorException {
//         prepareRestTemplate();
//         setRequestHeaders();
//         ClientHttpEntity clientHttpEntity = createPostClientHttpEntity(url, object);
//         ResponseEntity<K> response = sendRequest(clientHttpEntity, objectClass);
//         return response.getBody();
//     }

//     private <L> ResponseEntity<L> sendRequest(ClientHttpEntity clientHttpEntity, Class<L> objectClass)  {
//         HttpEntity entity = new HttpEntity(clientHttpEntity.getBody(), headers);
//         return restTemplate.exchange(clientHttpEntity.getUrl(), clientHttpEntity.getMethod(), entity, objectClass);
//     }

//     private void setRequestHeaders() {
//         headers = new HttpHeaders();
//         String username = Context.getAdministrationService().getGlobalProperty(API_LOGIN_PROPERTY);
//         String password = Context.getAdministrationService().getGlobalProperty(API_PASSWORD_PROPERTY);

//         for (ClientHttpRequestInterceptor interceptor : fhirClientHelper.getCustomInterceptors(username, password)) {
//             interceptor.addToHeaders(headers);
//         }

//         headers.add(USER_AGENT, CLIENT_HELPER_USER_AGENT);
//         headers.setContentType(MediaType.APPLICATION_JSON);
//     }

//     private <L> ClientHttpEntity createPostClientHttpEntity(String url, L object) throws URISyntaxException {
//         ClientHttpEntity clientHttpEntity = fhirClientHelper.createRequest(url, object);
//         clientHttpEntity.setMethod(HttpMethod.POST);
//         clientHttpEntity.setUrl(new URI(url));
//         return clientHttpEntity;
//     }

//     private void prepareRestTemplate() {
//         List<HttpMessageConverter<?>> converters = new ArrayList<>(fhirClientHelper.getCustomMessageConverter());
//         converters.add(conventer);
//         restTemplate.setMessageConverters(converters);
//     }
// }

package org.openmrs.module.insuranceclaims.web.controller;

import static org.openmrs.module.insuranceclaims.InsuranceClaimsOmodConstants.CLAIM_ALREADY_SENT_MESSAGE;
import static org.openmrs.module.insuranceclaims.InsuranceClaimsOmodConstants.CLAIM_NOT_SENT_MESSAGE;

import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.util.Base64;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.annotation.Authorized;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.idgen.service.IdentifierSourceService;
import org.openmrs.module.insuranceclaims.api.client.ClientConstants;
import org.openmrs.module.insuranceclaims.api.client.impl.ClaimRequestWrapper;
import org.openmrs.module.insuranceclaims.api.model.Bill;
import org.openmrs.module.insuranceclaims.api.model.InsuranceClaim;
import org.openmrs.module.insuranceclaims.api.model.InsurancePolicy;
import org.openmrs.module.insuranceclaims.api.service.InsuranceClaimService;
import org.openmrs.module.insuranceclaims.api.service.exceptions.ClaimRequestException;
import org.openmrs.module.insuranceclaims.api.service.exceptions.EligibilityRequestException;
import org.openmrs.module.insuranceclaims.api.service.fhir.FHIREligibilityService;
import org.openmrs.module.insuranceclaims.api.service.request.ExternalApiRequest;
import org.openmrs.module.insuranceclaims.forms.ClaimFormService;
import org.openmrs.module.insuranceclaims.forms.NewClaimForm;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.openmrs.module.metadatadeploy.MetadataUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import liquibase.pro.packaged.J;

// import javax.json.Json;
// import javax.json.JsonArray;
// import javax.json.JsonArrayBuilder;
// import javax.json.JsonObject;
// import javax.json.JsonObjectBuilder;

// import jakarta.json.Json;
// import jakarta.json.JsonArray;
// import jakarta.json.JsonArrayBuilder;
// import jakarta.json.JsonObject;
// import jakarta.json.JsonObjectBuilder;
// import jakarta.json.spi.JsonProvider;
import org.json.simple.JSONArray;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.glassfish.json.JsonProviderImpl;
import org.hl7.fhir.r4.model.CoverageEligibilityRequest;
import org.hl7.fhir.r4.model.CoverageEligibilityResponse;
import org.hl7.fhir.r4.model.CoverageEligibilityResponse.InsuranceComponent;
import org.hl7.fhir.r4.model.CoverageEligibilityResponse.ItemsComponent;

@RestController
@Authorized
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/insuranceclaims")
public class InsuranceClaimResourceController {

    @Autowired
    private ClaimFormService claimFormService;

    @Autowired
    private InsuranceClaimService insuranceClaimService;

    @Autowired
    private ExternalApiRequest externalApiRequest;

    @CrossOrigin(origins = "*", methods = {RequestMethod.POST, RequestMethod.OPTIONS})
    @RequestMapping(value = "/claims", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public ResponseEntity<String> createClaim(@RequestBody NewClaimForm form, HttpServletRequest request, HttpServletResponse response) throws ResponseException {
        System.out.println("Insurance Claims: REST - New Claim");

        try {
            InsuranceClaim claim = claimFormService.createClaim(form);

            // ResponseEntity<InsuranceClaim> requestResponse = new ResponseEntity<>(claim, HttpStatus.ACCEPTED);
            // return requestResponse;
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.set("Content-Type", "application/json");
            responseHeaders.setContentType(MediaType.APPLICATION_JSON);

            //Sending claim to external server
            // Even if immediate sending fails, we can send later
            try {
                externalApiRequest.sendClaimToExternalApi(claim);
            } catch (Exception ex) {
                System.err.println("Insurance Claims Error: " + ex.getMessage());
                ex.printStackTrace();
            }

            // return new ResponseEntity<String>(convertObjectToJson(claim), responseHeaders, HttpStatus.ACCEPTED);
            String responseBody = "{\n" + //
                            "    \"result\": \"SUCCESS\",\n" + //
                            "    \"message\": \"\"\n" + //
                            "}";
            return new ResponseEntity<String>(responseBody, responseHeaders, HttpStatus.ACCEPTED);
        } catch (Exception ex) {
            System.err.println("Insurance Claims Error: " + ex.getMessage());
            ex.printStackTrace();

            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.set("Content-Type", "application/json");
            responseHeaders.setContentType(MediaType.APPLICATION_JSON);
            // return new ResponseEntity<String>(convertObjectToJson(claim), responseHeaders, HttpStatus.ACCEPTED);
            String responseBody = "{\"result\": \"FAILURE\", \"message\": \"" + ex.getMessage() + "}";
            return new ResponseEntity<String>(responseBody, responseHeaders, HttpStatus.BAD_REQUEST);
        }

        // return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    /**
     * Convert an object to json
     * @param obj
     * @return
     */
    public static String convertObjectToJson(Object obj) {
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonResult = "";
        
        try {
            jsonResult = objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            System.err.println("Error serializing object: " + e.getMessage());
            e.printStackTrace();
        }
        
        return jsonResult;
    }

    /**
     * Create a new Bill
     * @param form
     * @param request
     * @param response
     * @return
     * @throws ResponseException
     */
    @CrossOrigin(origins = "*", methods = {RequestMethod.POST, RequestMethod.OPTIONS})
    @RequestMapping(value = "/bills", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public ResponseEntity<Bill> createBill(@RequestBody NewClaimForm form, HttpServletRequest request, HttpServletResponse response) throws ResponseException {
        System.out.println("Insurance Claims: REST - New Bill");
        Bill bill = claimFormService.createBill(form);

        ResponseEntity<Bill> requestResponse = new ResponseEntity<>(bill, HttpStatus.ACCEPTED);
        return requestResponse;
    }

    /**
     * Get Claim by its UUID
     * @param claimUuid
     * @param request
     * @param response
     * @return
     * @throws ResponseException
     */
    @CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.OPTIONS})
    @RequestMapping(value = "/claims", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public ResponseEntity get(@RequestParam(value = "claimUuid") String claimUuid, HttpServletRequest request, HttpServletResponse response) throws ResponseException {
        System.out.println("Insurance Claims: REST - Get Claim by UUID: " + claimUuid);
        InsuranceClaim claim = insuranceClaimService.getByUuid(claimUuid);
        ResponseEntity<InsuranceClaim> requestResponse = new ResponseEntity<>(claim, HttpStatus.ACCEPTED);
        return requestResponse;
    }

    /**
     * Checks if claim is present in external id, if external id does not have information about this
     * claim it will send it to external system, if claim was already submitted it will get update object based on external
     * information.
     * @param claimUuid uuid of claim that will be send to external server
     * @return InsuranceClaim with updated values or error message that occured during processing request
     */
    @CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.OPTIONS})
    @RequestMapping(value = "/claims/sendToExternal", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public ResponseEntity sendClaimToExternalId(@RequestParam(value = "claimUuid", required = true) String claimUuid, HttpServletRequest request, HttpServletResponse response) {
        System.out.println("Insurance Claims: REST - Send Claim to external by UUID: " + claimUuid);
        InsuranceClaim claim = insuranceClaimService.getByUuid(claimUuid);

        if (claim.getExternalId() != null) {
            return ResponseEntity.badRequest().body(CLAIM_ALREADY_SENT_MESSAGE);
        }

        try {
            externalApiRequest.sendClaimToExternalApi(claim);
            return new ResponseEntity<>(claim, HttpStatus.ACCEPTED);
        } catch (ClaimRequestException requestException) {
            return new ResponseEntity<>(requestException.getMessage(), HttpStatus.EXPECTATION_FAILED);
        }
    }

    /**
     * Get a claim using its external code
     * @param claimExternalCode
     * @param request
     * @param response
     * @return
     */
    @CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.OPTIONS})
    @RequestMapping(value = "/claims/getFromExternal", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public ResponseEntity getClaimFromExternalId(@RequestParam(value = "claimExternalCode") String claimExternalCode, HttpServletRequest request, HttpServletResponse response) {
        System.out.println("Insurance Claims: REST - Get Claim from external by external code: " + claimExternalCode);
        ResponseEntity requestResponse;
        try {
             ClaimRequestWrapper wrapper = externalApiRequest.getClaimFromExternalApi(claimExternalCode);
             requestResponse = new ResponseEntity<>(wrapper, HttpStatus.ACCEPTED);
        } catch (URISyntaxException wrongUrl) {
             requestResponse = new ResponseEntity<>(wrongUrl.getMessage(), HttpStatus.EXPECTATION_FAILED);
        }
        return requestResponse;
    }

    /**
     * Get a policy given its policy number
     * @param policyNumber
     * @param request
     * @param response
     * @return
     */
    @CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.OPTIONS})
    @RequestMapping(value = "/getPolicyFromExternal", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public ResponseEntity getPolicyFromExternal(@RequestParam(value = "policyNumber") String policyNumber, HttpServletRequest request, HttpServletResponse response) {
        System.out.println("Insurance Claims: REST - Get Policy from external by policy number: " + policyNumber);
        ResponseEntity requestResponse;
        try {
             InsurancePolicy policy = externalApiRequest.getPatientPolicy(policyNumber);
             requestResponse = new ResponseEntity<>(policy, HttpStatus.ACCEPTED);
        } catch (EligibilityRequestException wrongUrl) {
             requestResponse = new ResponseEntity<>(wrongUrl.getMessage(), HttpStatus.EXPECTATION_FAILED);
        }
        return requestResponse;
    }

    /**
     * Uses insurance claim external api to receive ClaimResponse information and then use it to
     * to update this proper values related to this insurance claim (I.e. check if was claim was valuated, check which claim
     * items were approved).
     * @param claimUuid uuid claim which have to be updated witch external server values
     * @return InsuranceClaim with updated values
     */
    @CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.OPTIONS})
    @RequestMapping(value = "/claims/updateClaim", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public ResponseEntity updateClaim(@RequestParam(value = "claimUuid") String claimUuid, HttpServletRequest request, HttpServletResponse response) {
        System.out.println("Insurance Claims: REST - Get claim update from external to check approval by UUID: " + claimUuid);
        InsuranceClaim claim = insuranceClaimService.getByUuid(claimUuid);

        if (claim.getExternalId() == null) {
            return ResponseEntity.badRequest().body(CLAIM_NOT_SENT_MESSAGE);
        }

        ResponseEntity requestResponse;
        try {
            InsuranceClaim wrapper = externalApiRequest.updateClaim(claim);
            requestResponse =  new ResponseEntity<>(wrapper, HttpStatus.ACCEPTED);
        } catch (ClaimRequestException e) {
            requestResponse =  new ResponseEntity<>(e.getMessage(), HttpStatus.EXPECTATION_FAILED);
        }

        return requestResponse;
    }

    /**
     * Get All Claims
     * @param claimUuid
     * @param request
     * @param response
     * @return
     * @throws ResponseException
     */
    @CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.OPTIONS})
    @RequestMapping(value = "/getallclaims", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public ResponseEntity<List<InsuranceClaim>> getAllClaims(HttpServletRequest request, HttpServletResponse response) throws ResponseException {
        System.out.println("Insurance Claims: REST - Get All Claims");
        List<InsuranceClaim> claims = insuranceClaimService.getAll(false);
        ResponseEntity<List<InsuranceClaim>> requestResponse = new ResponseEntity<>(claims, HttpStatus.ACCEPTED);
        return requestResponse;
    }

    /**
     * Get Claims by patient
     * @param claimUuid
     * @param request
     * @param response
     * @return
     * @throws ResponseException
     */
    @CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.OPTIONS})
    @RequestMapping(value = "/claimsbypatient", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public ResponseEntity<List<InsuranceClaim>> getClaimsByPatient(@RequestParam(value = "patientUuid") String patientUuid, HttpServletRequest request, HttpServletResponse response) throws ResponseException {
        System.out.println("Insurance Claims: REST - Get Claim by Patient: " + patientUuid);
        List<InsuranceClaim> claims = insuranceClaimService.getAllInsuranceClaimsByPatient(patientUuid);
        ResponseEntity<List<InsuranceClaim>> requestResponse = new ResponseEntity<>(claims, HttpStatus.ACCEPTED);
        return requestResponse;
    }

    /**
     * Get Claims by Cashier Bill
     * @param claimUuid
     * @param request
     * @param response
     * @return
     * @throws ResponseException
     */
    @CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.OPTIONS})
    @RequestMapping(value = "/claimsbycashierbill", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public ResponseEntity<List<InsuranceClaim>> getClaimsByCashierBill(@RequestParam(value = "billUuid") String billUuid, HttpServletRequest request, HttpServletResponse response) throws ResponseException {
        System.out.println("Insurance Claims: REST - Get Claim by Cashier Bill: " + billUuid);
        List<InsuranceClaim> claims = insuranceClaimService.getAllInsuranceClaimsByCashierBill(billUuid);
        ResponseEntity<List<InsuranceClaim>> requestResponse = new ResponseEntity<>(claims, HttpStatus.ACCEPTED);
        return requestResponse;
    }

    /**
     * CoverageEligibilityRequest
     * 
     * Returns the eligibility request response
     */
    @CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.OPTIONS})
    @RequestMapping(value = "/CoverageEligibilityRequest", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public ResponseEntity<JSONArray> getCoverageEligibilityRequest(@RequestBody String payload, HttpServletRequest request, HttpServletResponse response) throws ResponseException {

        System.out.println("Insurance Claims: the CoverageEligibilityRequest is: " + payload);
        // String NATIONAL_UNIQUE_PATIENT_IDENTIFIER = "f85081e2-b4be-4e48-b3a4-7994b69bb101";
        // FHIREligibilityService fhirEligibilityService = Context.getService(FHIREligibilityService.class);

        // try {

        //     // parse the payload
        //     JSONParser parser = new JSONParser();
        //     JSONObject responseObj = (JSONObject) parser.parse(payload);
        //     JSONObject patientUuid = (JSONObject) responseObj.get("patientUuid");
        //     JSONObject providerUuid = (JSONObject) responseObj.get("providerUuid");
        //     JSONObject facilityUuid = (JSONObject) responseObj.get("facilityUuid");
        //     JSONObject packageUuid = (JSONObject) responseObj.get("packageUuid");
        //     JSONObject isRefered = (JSONObject) responseObj.get("isRefered");
        //     JSONObject diagnosisUuids = (JSONObject) responseObj.get("diagnosisUuids");
        //     JSONObject intervensions = (JSONObject) responseObj.get("intervensions");

        //     // Search for patient NUPI
        //     PatientService patientService = Context.getPatientService();
        //     Patient patient = patientService.getPatientByUuid(patientUuid.toString());

        //     PatientIdentifierType nupiIdentifierType = MetadataUtils.existing(PatientIdentifierType.class, NATIONAL_UNIQUE_PATIENT_IDENTIFIER);
        //     PatientIdentifier nupiObject = patient.getPatientIdentifier(nupiIdentifierType);

        //     String nupiNumber = nupiObject.getIdentifier();
        //     System.out.println("Insurance Claims: Got Patient NUPI number as: " + nupiNumber);

        //     CoverageEligibilityRequest coverageEligibilityRequest = fhirEligibilityService.generateEligibilityRequest(nupiNumber);

        //     //Connect to remote server and send FHIR resource
        //     String baseUrl = Context.getAdministrationService().getGlobalProperty(ClientConstants.BASE_URL_PROPERTY);
        //     String Url = baseUrl + "/CoverageEligibilityRequest";
        //     String username = Context.getAdministrationService().getGlobalProperty(ClientConstants.API_LOGIN_PROPERTY);
        //     String password = Context.getAdministrationService().getGlobalProperty(ClientConstants.API_PASSWORD_PROPERTY);
        //     String auth = Base64.getEncoder().encodeToString((username + ":" + password).getBytes());

        //     SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
        //         SSLContexts.createDefault(),
        //         new String[]{"TLSv1.2"},
        //         null,
        //         SSLConnectionSocketFactory.getDefaultHostnameVerifier());
            
        //     CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(sslsf).build();
        //     HttpPost postRequest = new HttpPost(Url);
        //     postRequest.setHeader("Authorization", "Basic " + auth);
        //     String preparedFHIRPayload = coverageEligibilityRequest.toString();
        //     System.err.println("Insurance Claims: Sending to server: " + preparedFHIRPayload);
        //     StringEntity userEntity = new StringEntity(preparedFHIRPayload);
        //     postRequest.setEntity(userEntity);

        //     HttpResponse postResponse = httpClient.execute(postRequest);
        //     //verify the valid error code first
        //     int responseCode = postResponse.getStatusLine().getStatusCode();
        //     // int responseCode = connection.getResponseCode();
        //     if (responseCode == HttpURLConnection.HTTP_OK) { //success
        //         System.err.println("Insurance Claims: CoverageEligibilityRequest : Success: We connected");
        //         // We process the response
        //         String reply = EntityUtils.toString(postResponse.getEntity(), "UTF-8");
        //         ObjectMapper objectMapper = new ObjectMapper();
        //         CoverageEligibilityResponse fhirResponse = objectMapper.readValue(reply, CoverageEligibilityResponse.class);
        //         //Extract what we need from the response
        //         System.err.println("Insurance Claims: CoverageEligibilityRequest : Server replied: " + reply);
        //         List<InsuranceComponent> insurance = fhirResponse.getInsurance(); //getAuthorizationRequiredElement();
        //         for(InsuranceComponent insuranceComponent : insurance) {
        //             boolean inForce = insuranceComponent.getInforce();
        //             List<ItemsComponent> items = insuranceComponent.getItem();
        //             for(ItemsComponent itemsComponent : items) {
        //                 boolean preAuthRequired = itemsComponent.getAuthorizationRequired();

        //             }
        //         }
        //     } else {
        //         System.err.println("Insurance Claims: CoverageEligibilityRequest: Error: We failed to connect: " + responseCode);
        //         String ret = "{\"status\": \"CoverageEligibilityRequest Error: " + responseCode + "\"}";
        //         JSONArray jsonArray = new JSONArray();
        //         JSONObject jsonObject = new JSONObject();
        //         jsonObject.put("status", ret);
        //         jsonArray.add(jsonObject);
        //         ResponseEntity<JSONArray> responseEntity = ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON).body(jsonArray);
        //         return responseEntity;
        //     }
        // } catch (Exception e) {
        //     System.err.println("Insurance Claims: CoverageEligibilityRequest Error: " + e.getMessage());
        //     String ret = "{\"status\": \"CoverageEligibilityRequest General Error: \"}";
        //     JSONArray jsonArray = new JSONArray();
        //     JSONObject jsonObject = new JSONObject();
        //     jsonObject.put("status", ret);
        //     jsonArray.add(jsonObject);
        //     ResponseEntity<JSONArray> responseEntity = ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON).body(jsonArray);
        //     return responseEntity;
        // }

        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("shaPackageCode", "SHA-001");
        jsonObject.put("shaPackageName", "Eye Care");
        jsonObject.put("shaInterventionCode", "SHA-001-01");
        jsonObject.put("shaInterventionName", "");
        jsonObject.put("shaInterventionTariff", 50000);
        jsonObject.put("requirePreauth", true);
        jsonObject.put("status", "Pending");

        jsonArray.add(jsonObject);

        jsonObject = new JSONObject();
        jsonObject.put("shaPackageCode", "SHA-002");
        jsonObject.put("shaPackageName", "Stomach Ache");
        jsonObject.put("shaInterventionCode", "SHA-001-02");
        jsonObject.put("shaInterventionName", "");
        jsonObject.put("shaInterventionTariff", 70000);
        jsonObject.put("requirePreauth", false);
        jsonObject.put("status", "Pending");

        jsonArray.add(jsonObject);

        ResponseEntity<JSONArray> requestResponse = new ResponseEntity<>(jsonArray, HttpStatus.ACCEPTED);
        return requestResponse;
    }
}

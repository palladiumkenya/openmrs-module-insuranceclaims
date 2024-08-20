package org.openmrs.module.insuranceclaims.api.service.request.impl;

import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.Claim;
import org.hl7.fhir.r4.model.ClaimResponse;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.CoverageEligibilityRequest;
import org.hl7.fhir.r4.model.CoverageEligibilityResponse;
import org.hl7.fhir.r4.model.CoverageEligibilityResponse.BenefitComponent;
import org.hl7.fhir.r4.model.CoverageEligibilityResponse.InsuranceComponent;
import org.hl7.fhir.r4.model.CoverageEligibilityResponse.ItemsComponent;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.Element;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Money;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.Extension;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.hl7.fhir.exceptions.FHIRException;
import org.openmrs.BaseOpenmrsData;
import org.openmrs.BaseOpenmrsMetadata;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonAddress;
import org.openmrs.PersonName;
import org.openmrs.User;
import org.openmrs.api.LocationService;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.insuranceclaims.api.client.ClaimHttpRequest;
import org.openmrs.module.insuranceclaims.api.client.ClientConstants;
import org.openmrs.module.insuranceclaims.api.client.EligibilityHttpRequest;
import org.openmrs.module.insuranceclaims.api.client.PatientHttpRequest;
import org.openmrs.module.insuranceclaims.api.client.impl.ClaimRequestWrapper;
import org.openmrs.module.insuranceclaims.api.model.InsuranceClaim;
import org.openmrs.module.insuranceclaims.api.model.InsuranceClaimDiagnosis;
import org.openmrs.module.insuranceclaims.api.model.InsuranceClaimItem;
import org.openmrs.module.insuranceclaims.api.model.InsurancePolicy;
import org.openmrs.module.insuranceclaims.api.service.InsuranceClaimItemService;
import org.openmrs.module.insuranceclaims.api.service.InsuranceClaimService;
import org.openmrs.module.insuranceclaims.api.service.InsurancePolicyService;
import org.openmrs.module.insuranceclaims.api.service.db.ItemDbService;
import org.openmrs.module.insuranceclaims.api.service.exceptions.ClaimRequestException;
import org.openmrs.module.insuranceclaims.api.service.exceptions.EligibilityRequestException;
import org.openmrs.module.insuranceclaims.api.service.exceptions.ItemMatchingFailedException;
import org.openmrs.module.insuranceclaims.api.service.exceptions.PatientRequestException;
import org.openmrs.module.insuranceclaims.api.service.fhir.FHIRClaimDiagnosisService;
import org.openmrs.module.insuranceclaims.api.service.fhir.FHIRClaimItemService;
import org.openmrs.module.insuranceclaims.api.service.fhir.FHIRClaimResponseService;
import org.openmrs.module.insuranceclaims.api.service.fhir.FHIREligibilityService;
import org.openmrs.module.insuranceclaims.api.service.fhir.FHIRInsuranceClaimService;
import org.openmrs.module.insuranceclaims.api.service.fhir.util.IdentifierUtil;
import org.openmrs.module.insuranceclaims.api.service.fhir.util.InsuranceClaimUtil;
import org.openmrs.module.insuranceclaims.api.service.request.ExternalApiRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import com.fasterxml.jackson.databind.ObjectMapper;

import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException;

import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static org.openmrs.module.insuranceclaims.api.client.ClientConstants.BASE_URL_PROPERTY;
import static org.openmrs.module.insuranceclaims.api.client.ClientConstants.CLAIM_RESPONSE_SOURCE_URI;
import static org.openmrs.module.insuranceclaims.api.client.ClientConstants.CLAIM_SOURCE_URI;
import static org.openmrs.module.insuranceclaims.api.client.ClientConstants.ELIGIBILITY_SOURCE_URI;
import static org.openmrs.module.insuranceclaims.api.client.ClientConstants.PATIENT_SOURCE_URI;
import static org.openmrs.module.insuranceclaims.api.service.fhir.util.InsuranceClaimConstants.ACCESSION_ID;

import org.json.simple.JSONArray;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import org.openmrs.module.metadatadeploy.MetadataUtils;
import ca.uhn.fhir.context.FhirContext;

public class ExternalApiRequestImpl implements ExternalApiRequest {
    private String claimResponseUrl;
    private String claimUrl;
    private String eligibilityUrl;
    private String patientUrl;

    private ClaimHttpRequest claimHttpRequest;

    private EligibilityHttpRequest eligibilityHttpRequest;

    private PatientHttpRequest patientHttpRequest;

    private FHIRInsuranceClaimService fhirInsuranceClaimService;

    private FHIRClaimItemService fhirClaimItemService;

    private FHIRClaimResponseService fhirClaimResponseService;

    private FHIRClaimDiagnosisService fhirClaimDiagnosisService;

    private FHIREligibilityService fhirEligibilityService;

    private InsuranceClaimService insuranceClaimService;

    private InsuranceClaimItemService insuranceClaimItemService;

    private InsurancePolicyService insurancePolicyService;

    private ItemDbService itemDbService;

    public static final String REQUEST_ISSUE_LIST = "The request cannot be processed due to following issues \n";

    private static final String FAMILY_PREFIX = "FAMILY_PREFIX";

	private static final String PREFIX = "PREFIX";

    public static final String MALE = "M";

	public static final String FEMALE = "F";

    @Override
    public ClaimRequestWrapper getClaimFromExternalApi(String claimCode) throws URISyntaxException {
        setUrls();
        Claim claim = claimHttpRequest.getClaimRequest(this.claimUrl, claimCode);
        return wrapResponse(claim);
    }

    @Override
    public ClaimRequestWrapper getClaimResponseFromExternalApi(String claimCode) throws URISyntaxException, FHIRException {
        setUrls();
        ClaimResponse response = claimHttpRequest.getClaimResponse(this.claimResponseUrl, claimCode);
        return wrapResponse(response);
    }

    @Override
    public ClaimResponse sendClaimToExternalApi(InsuranceClaim claim) throws ClaimRequestException {
        try {
            setUrls();
            ClaimResponse claimResponse = claimHttpRequest.sendClaimRequest(claimUrl, claim);
            String externalCode = InsuranceClaimUtil.getClaimResponseId(claimResponse);
            claim.setExternalId(externalCode);
            insuranceClaimService.saveOrUpdate(claim);
            return claimResponse;
        } catch (URISyntaxException | FHIRException requestException) {
            String exceptionMessage = "Exception occured during processing request: "
                    + "Message:" + requestException.getMessage();

            throw new ClaimRequestException(exceptionMessage, requestException);
        } catch (HttpServerErrorException e) {
            String exceptionMessage = "Exception occured during processing request: "
                    + "Message:" + e.getMessage()
                    + "Reason: " + e.getResponseBodyAsString();

            throw new ClaimRequestException(exceptionMessage, e);
        }
    }

    @Override
    public InsuranceClaim updateClaim(InsuranceClaim claim) throws ClaimRequestException {
        try {
            setUrls();
            String claimExternalId = claim.getExternalId();
            //For now it is assumed that external server don't have information that allows to match items
            // in ClaimResponse with MRS concepts, but order of items in ClaimResponse is the same as in Claim
            ClaimRequestWrapper wrappedResponse = getClaimResponseWithAssignedItemCodes(claimExternalId);
            insuranceClaimService.updateClaim(claim, wrappedResponse.getInsuranceClaim());
            List<InsuranceClaimItem> omrsItems = itemDbService.findInsuranceClaimItems(claim.getId());
            insuranceClaimItemService.updateInsuranceClaimItems(omrsItems, wrappedResponse.getItems());

            return claim;
        } catch (URISyntaxException | FHIRException | ItemMatchingFailedException requestException) {
            String exceptionMessage = "Exception occured during processing request: "
                    + "Message:" + requestException.getMessage();
            throw new ClaimRequestException(exceptionMessage, requestException);
        } catch (HttpServerErrorException e) {
            String exceptionMessage = "Exception occured during processing request: "
                    + "Message:" + e.getMessage()
                    + "Reason: " + e.getResponseBodyAsString();

            throw new ClaimRequestException(exceptionMessage, e);
        }
    }

    @Override
    public InsurancePolicy getPatientPolicy(String policyNumber) throws EligibilityRequestException {
        try {
            setUrls();
            CoverageEligibilityRequest eligibilityRequest = fhirEligibilityService.generateEligibilityRequest(policyNumber);
            CoverageEligibilityResponse response =  eligibilityHttpRequest.sendEligibilityRequest(
                    this.eligibilityUrl, eligibilityRequest);

            if (response.getInsurance() == null) {
                throw new EligibilityRequestException("Insurance not found");
            }
            return insurancePolicyService.generateInsurancePolicy(response);
        } catch (URISyntaxException | FHIRException requestException) {
            String exceptionMessage = "Exception occured during processing request: "
                    + "Message:" + requestException.getMessage();
            throw new EligibilityRequestException(exceptionMessage);
        } catch (HttpServerErrorException e) {
            String exceptionMessage = "Exception occured during processing request: "
                    + "Message:" + e.getMessage()
                    + "Reason: " + e.getResponseBodyAsString();

            throw new EligibilityRequestException(exceptionMessage, e);
        } catch (ResourceAccessException e) {
            String exceptionMessage = "Exception occured during processing request: "
                    + "Message:" + e.getMessage()
                    + "Reason: " + e.getCause();
            throw new EligibilityRequestException(exceptionMessage, e);
        }
    }

    @Override
    public org.openmrs.Patient getPatient(String patientId) throws PatientRequestException {
        try {
            Patient fhirPatient = getFhirPatient(patientId);
            org.openmrs.Patient patient = generateOmrsPatient(fhirPatient, new ArrayList<>());
            patient.setIdentifiers(new TreeSet<>());

            String identifier = IdentifierUtil.getPatientIdentifierValueBySystemCode(fhirPatient, ACCESSION_ID);
            patient.addIdentifier(IdentifierUtil.createExternalIdIdentifier(identifier));
            patient.addIdentifier(IdentifierUtil.createBasicPatientIdentifier());

            return patient;
        } catch (URISyntaxException uriSyntaxException) {
            String exceptionMessage = "Exception occured during processing request: "
                    + "Message:" + uriSyntaxException.getMessage();
            throw new PatientRequestException(exceptionMessage);
        }
    }

    @Override
    public List<Patient> getPatientsByIdentifier(String patientIdentifier) throws PatientRequestException {
        setUrls();
        try {
            List<Patient> fhirPatient = patientHttpRequest.getPatientByIdentifier(patientUrl, patientIdentifier);

            return fhirPatient;
        } catch (URISyntaxException uriSyntaxException) {
            String exceptionMessage = "Exception occured during processing request: " + "Message:" + uriSyntaxException.getMessage();
            throw new PatientRequestException(exceptionMessage);
        }
    }

    @Override
    public org.openmrs.Patient importPatient(String patientId) throws PatientRequestException {
        org.openmrs.Patient patient = getPatient(patientId);
        Context.getPatientService().savePatient(patient);
        Context.getPatientService().unvoidPatient(patient);
        return patient;
    }

    @Override
    public JSONArray postCoverageEligibilityRequest(String payload) {
        JSONArray coreArray = new JSONArray();
        String SOCIAL_HEALTH_AUTHORITY_IDENTIFICATION_NUMBER = "52c3c0c3-05b8-4b26-930e-2a6a54e14c90";

        try {

            // parse the payload
            JSONParser parser = new JSONParser();
            JSONObject responseObj = (JSONObject) parser.parse(payload);
            String patientUuid = (String) responseObj.get("patientUuid");
            String providerUuid = (String) responseObj.get("providerUuid");
            String facilityUuid = (String) responseObj.get("facilityUuid");
            String packageUuid = (String) responseObj.get("packageUuid");
            Boolean isRefered = (Boolean) responseObj.get("isRefered");
            JSONArray diagnosisUuids = (JSONArray) responseObj.get("diagnosisUuids");
            JSONArray intervensions = (JSONArray) responseObj.get("intervensions");

            // Search for patient NUPI
            PatientService patientService = Context.getPatientService();
            org.openmrs.Patient patient = patientService.getPatientByUuid(patientUuid.toString());

            if(patient != null) {

                PatientIdentifierType shaIdentifierType = Context.getPatientService().getPatientIdentifierTypeByUuid(SOCIAL_HEALTH_AUTHORITY_IDENTIFICATION_NUMBER);
                if(shaIdentifierType != null) {
                    PatientIdentifier shaObject = patient.getPatientIdentifier(shaIdentifierType);

                    if(shaObject != null) {
                        String shaNumber = shaObject.getIdentifier();
                        System.out.println("Insurance Claims: Got Patient SHA ID number as: " + shaNumber);

                        CoverageEligibilityRequest coverageEligibilityRequest = fhirEligibilityService.generateEligibilityRequest(shaNumber);

                        //Connect to remote server and send FHIR resource
                        String baseUrl = Context.getAdministrationService().getGlobalProperty(ClientConstants.BASE_URL_PROPERTY);
                        String Url = baseUrl + "/CoverageEligibilityRequest";
                        String username = Context.getAdministrationService().getGlobalProperty(ClientConstants.API_LOGIN_PROPERTY);
                        String password = Context.getAdministrationService().getGlobalProperty(ClientConstants.API_PASSWORD_PROPERTY);
                        String auth = Base64.getEncoder().encodeToString((username + ":" + password).getBytes());

                        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                            SSLContexts.createDefault(),
                            new String[]{"TLSv1.2"},
                            null,
                            SSLConnectionSocketFactory.getDefaultHostnameVerifier());
                        
                        CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(sslsf).build();
                        HttpPost postRequest = new HttpPost(Url);
                        postRequest.setHeader("Authorization", "Basic " + auth);

                        // Create a FhirContext for R4
                        FhirContext fhirContext = FhirContext.forR4();

                        // Convert the CoverageEligibilityRequest object to a JSON string
                        String preparedFHIRPayload = fhirContext.newJsonParser().setPrettyPrint(true).encodeResourceToString(coverageEligibilityRequest);

                        System.err.println("Insurance Claims: CoverageEligibilityRequest : Sending to server: " + preparedFHIRPayload);
                        StringEntity userEntity = new StringEntity(preparedFHIRPayload);
                        postRequest.setEntity(userEntity);

                        HttpResponse postResponse = httpClient.execute(postRequest);
                        //verify the valid error code first
                        int responseCode = postResponse.getStatusLine().getStatusCode();
                        // int responseCode = connection.getResponseCode();
                        if (responseCode == HttpURLConnection.HTTP_OK) { //success
                            System.err.println("Insurance Claims: CoverageEligibilityRequest : Success: We connected");
                            // We process the response
                            String reply = EntityUtils.toString(postResponse.getEntity(), "UTF-8");
                            ObjectMapper objectMapper = new ObjectMapper();
                            CoverageEligibilityResponse fhirResponse = objectMapper.readValue(reply, CoverageEligibilityResponse.class);
                            //Extract what we need from the response
                            System.err.println("Insurance Claims: CoverageEligibilityRequest : Server replied: " + reply);
                            List<InsuranceComponent> insurance = fhirResponse.getInsurance(); 
                            for(InsuranceComponent insuranceComponent : insurance) {
                                boolean inForce = insuranceComponent.getInforce();
                                List<ItemsComponent> items = insuranceComponent.getItem();
                                for(ItemsComponent itemsComponent : items) {
                                    boolean preAuthRequired = itemsComponent.getAuthorizationRequired();
                                    CodeableConcept category = itemsComponent.getCategory();
                                    Coding categoryCoding = category.getCodingFirstRep();
                                    String packageCode = categoryCoding.getCode();
                                    String packageName = categoryCoding.getDisplay();
                                    CodeableConcept intervention = itemsComponent.getProductOrService();
                                    Coding interventionCoding = intervention.getCodingFirstRep();
                                    String interventionCode = interventionCoding.getCode();
                                    String interventionName = interventionCoding.getDisplay();
                                    BenefitComponent benefit = itemsComponent.getBenefitFirstRep(); // Yearly benefit
                                    Money allowedMoney = benefit.getAllowedMoney();
                                    Money usedMoney = benefit.getUsedMoney();
                                    BigDecimal remainingBalance = allowedMoney.getValue().subtract(usedMoney.getValue());

                                    System.err.println("Insurance Claims: CoverageEligibilityRequest: Response: " + preAuthRequired + " : " + packageCode + " : " + packageName + " : " + packageName + " : " + interventionCode + " : " + interventionName + " : " + remainingBalance);
                                }
                            }
                        } else {
                            System.err.println("Insurance Claims: CoverageEligibilityRequest: Error: We failed to connect: " + responseCode);
                            String ret = "{\"status\": \"CoverageEligibilityRequest Error: " + responseCode + "\"}";
                            JSONArray jsonArray = new JSONArray();
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("status", ret);
                            jsonArray.add(jsonObject);
                            ResponseEntity<JSONArray> responseEntity = ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON).body(jsonArray);
                            // return responseEntity;
                        }
                    } else {
                        System.err.println("Insurance Claims: CoverageEligibilityRequest: Error: Patient identifier not found");
                    }
                } else {
                    System.err.println("Insurance Claims: CoverageEligibilityRequest: Error: Patient identifier type not found");
                }
            } else {
                System.err.println("Insurance Claims: CoverageEligibilityRequest: Error: Patient not found");
            }
        } catch (Exception e) {
            System.err.println("Insurance Claims: CoverageEligibilityRequest Error: " + e.getMessage());
            String ret = "{\"status\": \"CoverageEligibilityRequest General Error: \"}";
            JSONArray jsonArray = new JSONArray();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("status", ret);
            jsonArray.add(jsonObject);
            ResponseEntity<JSONArray> responseEntity = ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON).body(jsonArray);
            e.printStackTrace();
            // return responseEntity;
        }

        return(coreArray);
    }

    private Patient getFhirPatient(String patientId) throws URISyntaxException {
        setUrls();
        Patient fhirPatient =  patientHttpRequest.getPatientByQuery(patientUrl, createPatientQuery(patientId));
        if (fhirPatient.getDeceased() == null) {
            fhirPatient.setDeceased(new BooleanType(false));
        }
        return fhirPatient;
    }

    public void setClaimHttpRequest(ClaimHttpRequest claimHttpRequest) {
        this.claimHttpRequest = claimHttpRequest;
    }

    public void setFhirInsuranceClaimService(FHIRInsuranceClaimService fhirInsuranceClaimService) {
        this.fhirInsuranceClaimService = fhirInsuranceClaimService;
    }

    public void setFhirClaimItemService(FHIRClaimItemService fhirClaimItemService) {
        this.fhirClaimItemService = fhirClaimItemService;
    }

    public void setFhirClaimResponseService(FHIRClaimResponseService fhirClaimResponseService) {
        this.fhirClaimResponseService = fhirClaimResponseService;
    }

    public void setFhirClaimDiagnosisService(FHIRClaimDiagnosisService fhirClaimDiagnosisService) {
        this.fhirClaimDiagnosisService = fhirClaimDiagnosisService;
    }

    public void setInsuranceClaimService(InsuranceClaimService insuranceClaimService) {
        this.insuranceClaimService = insuranceClaimService;
    }

    public void setInsuranceClaimItemService(InsuranceClaimItemService insuranceClaimItemService) {
        this.insuranceClaimItemService = insuranceClaimItemService;
    }

    public void setEligibilityHttpRequest(EligibilityHttpRequest eligibilityHttpRequest) {
        this.eligibilityHttpRequest = eligibilityHttpRequest;
    }

    public void setItemDbService(ItemDbService itemDbService) {
        this.itemDbService = itemDbService;
    }

    public void setFhirEligibilityService(FHIREligibilityService fhirEligibilityService) {
        this.fhirEligibilityService = fhirEligibilityService;
    }

    public void setInsurancePolicyService(InsurancePolicyService insurancePolicyService) {
        this.insurancePolicyService = insurancePolicyService;
    }

    public void setPatientHttpRequest(PatientHttpRequest patientHttpRequest) {
        this.patientHttpRequest = patientHttpRequest;
    }

    private void setUrls() {
        String baseUrl = Context.getAdministrationService().getGlobalProperty(BASE_URL_PROPERTY);
        String claimUri =  Context.getAdministrationService().getGlobalProperty(CLAIM_SOURCE_URI);
        this.claimUrl = baseUrl + "/" + claimUri;
        String claimResponseUri =  Context.getAdministrationService().getGlobalProperty(CLAIM_RESPONSE_SOURCE_URI);
        this.claimResponseUrl = baseUrl + "/" + claimResponseUri;
        String eligibilityUri = Context.getAdministrationService().getGlobalProperty(ELIGIBILITY_SOURCE_URI);
        this.eligibilityUrl = baseUrl + "/" + eligibilityUri;
        String patientUri = Context.getAdministrationService().getGlobalProperty(PATIENT_SOURCE_URI);
        this.patientUrl = baseUrl + "/" + patientUri;
    }

    private ClaimRequestWrapper wrapResponse(Claim claim) {
        List<String> errors = new ArrayList<>();
        InsuranceClaim receivedClaim = fhirInsuranceClaimService.generateOmrsClaim(claim, errors);
        List<InsuranceClaimDiagnosis> receivedDiagnosis = claim.getDiagnosis().stream()
                .map(diagnosis -> fhirClaimDiagnosisService.createOmrsClaimDiagnosis(diagnosis, errors))
                .collect(Collectors.toList());
        List<InsuranceClaimItem> items = fhirClaimItemService.generateOmrsClaimItems(claim, errors);

        return new ClaimRequestWrapper(receivedClaim, receivedDiagnosis, items, errors);
    }

    private ClaimRequestWrapper wrapResponse(ClaimResponse claim) throws FHIRException {
        List<String> errors = new ArrayList<>();
        InsuranceClaim receivedClaim = fhirClaimResponseService.generateOmrsClaim(claim, errors);
        List<InsuranceClaimItem> items = fhirClaimItemService.generateOmrsClaimResponseItems(claim, errors);

        return new ClaimRequestWrapper(receivedClaim, null, items, errors);
    }

    private ClaimRequestWrapper getClaimResponseWithAssignedItemCodes(String claimCode) throws URISyntaxException,
            FHIRException {
        Claim claim = claimHttpRequest.getClaimRequest(this.claimUrl, claimCode);
        ClaimResponse claimResponse = claimHttpRequest.getClaimResponse(this.claimResponseUrl, claimCode);

        ClaimRequestWrapper wrappedResponse = wrapResponse(claimResponse);

        List<InsuranceClaimItem> claimResponseItems = wrappedResponse.getItems();
        List<InsuranceClaimItem> claimItems = wrapResponse(claim).getItems();

        for (int itemIndex = 0; itemIndex < claimResponseItems.size(); itemIndex++) {
            InsuranceClaimItem nextItem = claimResponseItems.get(itemIndex);
            InsuranceClaimItem itemAdditionalData = claimItems.get(itemIndex);
            addDataFromClaimItemToClaimResponseItem(nextItem, itemAdditionalData);
        }
        return wrappedResponse;
    }

    private void addDataFromClaimItemToClaimResponseItem(InsuranceClaimItem claimResponseItem,
            InsuranceClaimItem claimItem) {
        claimResponseItem.setItem(claimItem.getItem());
        claimResponseItem.setQuantityProvided(claimItem.getQuantityProvided());
    }

    private String createPatientQuery(String patientId) {
        return "/" + patientId;
    }

    public String extractUuid(String uuid) {
		return uuid.contains("/") ? uuid.substring(uuid.indexOf("/") + 1) : uuid;
	}

    public org.openmrs.Patient generateOmrsPatient(Patient patient, List<String> errors) {
		org.openmrs.Patient omrsPatient = new org.openmrs.Patient(); // add eror handli
		readBaseExtensionFields(omrsPatient, patient);

		if (patient.getId() != null) {
			omrsPatient.setUuid(extractUuid(patient.getId()));
		}

		List<Identifier> fhirIdList = patient.getIdentifier();
		Set<PatientIdentifier> idList = new TreeSet<PatientIdentifier>();

		if (fhirIdList == null || fhirIdList.isEmpty()) {
			errors.add("Identifiers cannot be empty");
		}

		for (Identifier fhirIdentifier : fhirIdList) {
			PatientIdentifier identifier = generateOpenmrsIdentifier(fhirIdentifier, errors);
			checkGeneratorErrorList(errors);
			idList.add(identifier);
		}
		omrsPatient.setIdentifiers(idList);

		if (patient.getName().size() == 0) {
			errors.add("Name cannot be empty");
		}
		omrsPatient.setNames(buildOpenmrsNames(patient.getName()));
		if (!validateOpenmrsNames(omrsPatient.getNames())) {
			errors.add("Person should have at least one name with family name and given name");
		}

		omrsPatient.setAddresses(buildPersonAddresses(patient.getAddress()));

		String gender = determineOpenmrsGender(patient.getGender());
		if (StringUtils.isNotBlank(gender)) {
			omrsPatient.setGender(gender);
		} else {
			errors.add("Gender cannot be empty");
		}
		omrsPatient.setBirthdate(patient.getBirthDate());

		BooleanType Isdeceased = (BooleanType) patient.getDeceased();
		omrsPatient.setDead(Isdeceased.getValue());

		if (patient.getActive()) {
			omrsPatient.setPersonVoided(false);
		} else {
			omrsPatient.setPersonVoided(true);
			omrsPatient.setPersonVoidReason("Deleted from FHIR module"); // deleted reason is compulsory
		}
		return omrsPatient;
	}

    public static void readBaseExtensionFields(BaseOpenmrsData openmrsData, DomainResource fhirResource) {
        for (Extension extension : fhirResource.getExtension()) {
            setBaseOpenMRSData(openmrsData, extension);
        }
    }

    public static void readBaseExtensionFields(BaseOpenmrsData openmrsData, Element fhirResource) {
        for (Extension extension : fhirResource.getExtension()) {
            setBaseOpenMRSData(openmrsData, extension);
        }
    }

    public static void readBaseExtensionFields(BaseOpenmrsMetadata openmrsMetadata, DomainResource fhirResource) {
        for (Extension extension : fhirResource.getExtension()) {
            setBaseOpenMRSMetadata(openmrsMetadata, extension);
        }
    }

    public static void setBaseOpenMRSData(BaseOpenmrsData openMRSData, Extension extension) {
        final String DATE_CREATED_URL = "http://fhir-es.transcendinsights.com/r4/StructureDefinition/resource-date-created";
        final String CREATOR_URL = "https://purl.org/elab/fhir/StructureDefinition/Creator-crew-version1";
        final String CHANGED_BY_URL = "changedBy";
        final String DATE_CHANGED_URL = "dateChanged";
        final String VOIDED_URL = "voided";
        final String DATE_VOIDED_URL = "dateVoided";
        final String VOIDED_BY_URL = "voidedBy";
        final String VOID_REASON_URL = "voidReason";
        final String RETIRED_URL = "retired";
        final String DATE_RETIRED_URL = "dateRetired";
        final String RETIRED_BY_URL = "retiredBy";
        final String RETIRE_REASON_URL = "retireReason";
        final String DESCRIPTION_URL = "description";

		switch (extension.getUrl()) {
			case DATE_CREATED_URL:
				openMRSData.setDateCreated(getDateValueFromExtension(extension));
				break;
			case CREATOR_URL:
				openMRSData.setCreator(getUserFromExtension(extension));
				break;
			case CHANGED_BY_URL:
				openMRSData.setChangedBy(getUserFromExtension(extension));
				break;
			case DATE_CHANGED_URL:
				openMRSData.setDateChanged(getDateValueFromExtension(extension));
				break;
			case VOIDED_URL:
				openMRSData.setVoided(getBooleanFromExtension(extension));
				break;
			case DATE_VOIDED_URL:
				openMRSData.setDateVoided(getDateValueFromExtension(extension));
				break;
			case VOIDED_BY_URL:
				openMRSData.setVoidedBy(getUserFromExtension(extension));
				break;
			case VOID_REASON_URL:
				openMRSData.setVoidReason(getStringFromExtension(extension));
				break;
			default:
				break;
		}
	}

    public static void setBaseOpenMRSMetadata(BaseOpenmrsMetadata openmrsMetadata, Extension extension) {

        final String DATE_CREATED_URL = "http://fhir-es.transcendinsights.com/r4/StructureDefinition/resource-date-created";
        final String CREATOR_URL = "https://purl.org/elab/fhir/StructureDefinition/Creator-crew-version1";
        final String CHANGED_BY_URL = "changedBy";
        final String DATE_CHANGED_URL = "dateChanged";
        final String VOIDED_URL = "voided";
        final String DATE_VOIDED_URL = "dateVoided";
        final String VOIDED_BY_URL = "voidedBy";
        final String VOID_REASON_URL = "voidReason";
        final String RETIRED_URL = "retired";
        final String DATE_RETIRED_URL = "dateRetired";
        final String RETIRED_BY_URL = "retiredBy";
        final String RETIRE_REASON_URL = "retireReason";
        final String DESCRIPTION_URL = "description";

		switch (extension.getUrl()) {
			case DATE_CREATED_URL:
				openmrsMetadata.setDateCreated(getDateValueFromExtension(extension));
				break;
			case CREATOR_URL:
				openmrsMetadata.setCreator(getUserFromExtension(extension));
				break;
			case CHANGED_BY_URL:
				openmrsMetadata.setChangedBy(getUserFromExtension(extension));
				break;
			case DATE_CHANGED_URL:
				openmrsMetadata.setDateChanged(getDateValueFromExtension(extension));
				break;
			case RETIRED_URL:
				openmrsMetadata.setRetired(getBooleanFromExtension(extension));
				break;
			case DATE_RETIRED_URL:
				openmrsMetadata.setDateRetired(getDateValueFromExtension(extension));
				break;
			case RETIRED_BY_URL:
				openmrsMetadata.setRetiredBy(getUserFromExtension(extension));
				break;
			case RETIRE_REASON_URL:
				openmrsMetadata.setRetireReason(getStringFromExtension(extension));
				break;
			default:
				break;
		}
	}

    public static Date getDateValueFromExtension(Extension extension) {
		if (extension.getValue() instanceof DateTimeType) {
			DateTimeType dateTimeValue = (DateTimeType) extension.getValue();
			return dateTimeValue.getValue();
		}
		return null;
	}

    public static User getUserFromExtension(Extension extension) {
		String userName = getStringFromExtension(extension);
		if (StringUtils.isNotEmpty(userName)) {
			return Context.getUserService().getUserByUsername(userName);
		}
		return null;
	}

    public static String getStringFromExtension(Extension extension) {
		if (extension.getValue() instanceof StringType) {
			StringType string = (StringType) extension.getValue();
			return string.getValue();
		}
		return null;
	}

    public static boolean getBooleanFromExtension(Extension extension) {
		if (extension.getValue() instanceof BooleanType) {
			BooleanType booleanType = (BooleanType) extension.getValue();
			return booleanType.booleanValue();
		}
		return false;
	}

    public void checkGeneratorErrorList(List<String> errors) {
		if (!errors.isEmpty()) {
			String errorMessage = generateErrorMessage(errors, REQUEST_ISSUE_LIST);
			throw new UnprocessableEntityException(errorMessage);
		}
	}

    public String generateErrorMessage(List<String> errors, String baseMessage) {
		StringBuilder errorMessage = new StringBuilder(baseMessage);
		for (int i = 0; i < errors.size(); i++) {
			errorMessage.append(i + 1).append(" : ").append(errors.get(i)).append("\n");
		}
		return errorMessage.toString();
	}

    public PatientIdentifier generateOpenmrsIdentifier(Identifier fhirIdentifier, List<String> errors) {
		PatientIdentifier patientIdentifier = new PatientIdentifier();
		patientIdentifier.setIdentifier(fhirIdentifier.getValue());
		if (String.valueOf(Identifier.IdentifierUse.USUAL).equalsIgnoreCase(fhirIdentifier.getUse().getDefinition())) {
			patientIdentifier.setPreferred(true);
		} else {
			patientIdentifier.setPreferred(false);
		}
		PatientIdentifierType type = getPatientIdentifierType(fhirIdentifier);
		if (type == null) {
			errors.add(String.format("Missing the PatientIdentifierType with the name '%s' and the UUID '%s'",
					fhirIdentifier.getSystem(), fhirIdentifier.getId()));
		}
		patientIdentifier.setIdentifierType(type);

		if (type != null) {
			PatientIdentifierType.LocationBehavior lb = type.getLocationBehavior();
			if (lb == null || lb == PatientIdentifierType.LocationBehavior.REQUIRED) {
				LocationService locationService = Context.getLocationService();
				patientIdentifier.setLocation(locationService.getLocation(1));
			}
		}
		patientIdentifier.setUuid(fhirIdentifier.getId());
		return patientIdentifier;
	}

    private PatientIdentifierType getPatientIdentifierType(Identifier fhirIdentifier) {
		String identifierTypeName = fhirIdentifier.getSystem();
		PatientIdentifierType patientIdentifierType =  Context.getPatientService().getPatientIdentifierTypeByName(identifierTypeName);
		if (patientIdentifierType == null) {
			String identifierTypeUuid = fhirIdentifier.getId();
			patientIdentifierType =  Context.getPatientService().getPatientIdentifierTypeByUuid(identifierTypeUuid);
		}
		return patientIdentifierType;
	}

    public Set<PersonName> buildOpenmrsNames(List<HumanName> humanNames) {
		Set<PersonName> names = new TreeSet<PersonName>();
		for (HumanName humanNameDt : humanNames) {
			PersonName name = buildOpenmrsPersonName(humanNameDt);
			names.add(name);
		}
		return names;
	}

    public PersonName buildOpenmrsPersonName(HumanName humanNameDt) {
		PersonName personName = new PersonName();
		if(StringUtils.isNotBlank(humanNameDt.getId())) {
			personName.setUuid(humanNameDt.getId());
		}
		setOpenmrsNames(humanNameDt, personName);
		buildOpenmrsNamePrefixes(humanNameDt, personName);
		buildOpenmrsSuffix(humanNameDt, personName);
		personName.setPreferred(determineIfPreferredName(humanNameDt));

		return personName;
	}

    public void setOpenmrsNames(HumanName humanNameDt, PersonName personName) {
		String familyName = humanNameDt.getFamily();
		if (!StringUtils.isEmpty(familyName)) {
			personName.setFamilyName(familyName);
		}

		List<StringType> names = humanNameDt.getGiven();
		if (names.size() > 0) {
			personName.setGivenName(String.valueOf(names.get(0)));
		}
		if (names.size() > 1) {
			personName.setMiddleName(String.valueOf(names.get(1)));
		}
	}

    public void buildOpenmrsNamePrefixes(HumanName humanNameDt, PersonName personName) {
		if (humanNameDt.getPrefix() != null) {
			List<StringType> prefixes = humanNameDt.getPrefix();
			for(StringType prefix : prefixes) {
				if (prefix.getId().equalsIgnoreCase(PREFIX)) {
					personName.setPrefix(String.valueOf(prefix));
				} else if (prefix.getId().equalsIgnoreCase(FAMILY_PREFIX)) {
					personName.setFamilyNamePrefix(String.valueOf(prefix));
				}
			}
		}
	}

    public void buildOpenmrsSuffix(HumanName humanNameDt, PersonName personName) {
		if (humanNameDt.getSuffix() != null) {
			List<StringType> suffixes = humanNameDt.getSuffix();
			if (suffixes.size() > 0) {
				StringType suffix = suffixes.get(0);
				personName.setFamilyNameSuffix(String.valueOf(suffix));
			}
		}
	}

    public boolean determineIfPreferredName(HumanName humanNameDt) {
		boolean preferred = false;
		if (humanNameDt.getUse() != null) {
			String getUse = humanNameDt.getUse().toCode();
			if (String.valueOf(HumanName.NameUse.OFFICIAL).equalsIgnoreCase(getUse)
					|| String.valueOf(HumanName.NameUse.USUAL).equalsIgnoreCase(getUse)) {
				preferred = true;
			}
			if (String.valueOf(HumanName.NameUse.OLD).equalsIgnoreCase(getUse)) {
				preferred = false;
			}
		}
		return preferred;
	}

    public boolean validateOpenmrsNames(Set<PersonName> names) {
		boolean valid = false;
		for (PersonName name : names) {
			if (org.apache.commons.lang.StringUtils.isNotBlank(name.getGivenName())
					&& org.apache.commons.lang.StringUtils.isNotBlank(name.getFamilyName())) {
				valid = true;
				break;
			}
		}
		return valid;
	}

    public Set<PersonAddress> buildPersonAddresses(List<Address> fhirAddresses) {
		Set<PersonAddress> addresses = new TreeSet<PersonAddress>();
		for (Address fhirAddress : fhirAddresses) {
			PersonAddress address = buildPersonAddress(fhirAddress);
			addresses.add(address);
		}
		return addresses;
	}

    public PersonAddress buildPersonAddress(Address fhirAddress) {
		PersonAddress address = new PersonAddress();
		if(StringUtils.isNotBlank(fhirAddress.getId())) {
			address.setUuid(fhirAddress.getId());
		}
		address.setCityVillage(fhirAddress.getCity());
		address.setCountry(fhirAddress.getCountry());
		address.setStateProvince(fhirAddress.getState());
		address.setPostalCode(fhirAddress.getPostalCode());
		List<StringType> addressStrings = fhirAddress.getLine();

		if (addressStrings != null) {
			for (int i = 0; i < addressStrings.size(); i++) {
				if (i == 0) {
					address.setAddress1(String.valueOf(addressStrings.get(0)));
				} else if (i == 1) {
					address.setAddress2(String.valueOf(addressStrings.get(1)));
				} else if (i == 2) {
					address.setAddress3(String.valueOf(addressStrings.get(2)));
				} else if (i == 3) {
					address.setAddress4(String.valueOf(addressStrings.get(3)));
				} else if (i == 4) {
					address.setAddress5(String.valueOf(addressStrings.get(4)));
				}
			}
		}

		if (String.valueOf(Address.AddressUse.HOME.toCode()).equalsIgnoreCase(fhirAddress.getUse().toCode())) {
			address.setPreferred(true);
		}
		if (String.valueOf(Address.AddressUse.OLD.toCode()).equalsIgnoreCase(fhirAddress.getUse().toCode())) {
			address.setPreferred(false);
		}
		return address;
	}

    public static String determineOpenmrsGender(Enumerations.AdministrativeGender fhirGender) {
		String gender = null;
		if (fhirGender != null) {
			if (fhirGender.toCode().equalsIgnoreCase(Enumerations.AdministrativeGender.MALE.toCode())) {
				gender = MALE;
			} else if (fhirGender.toCode().equalsIgnoreCase(String.valueOf(Enumerations.AdministrativeGender.FEMALE))) {
				gender = FEMALE;
			}
		}
		return gender;
	}


}

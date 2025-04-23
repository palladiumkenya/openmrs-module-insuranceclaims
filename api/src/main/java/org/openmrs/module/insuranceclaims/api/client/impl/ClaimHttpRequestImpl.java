package org.openmrs.module.insuranceclaims.api.client.impl;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Claim;
import org.hl7.fhir.r4.model.ClaimResponse;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.exceptions.FHIRException;
import org.openmrs.Encounter;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.module.insuranceclaims.api.client.ClaimHttpRequest;
import org.openmrs.module.insuranceclaims.api.client.FHIRClient;
import org.openmrs.module.insuranceclaims.api.model.InsuranceClaim;
import org.openmrs.module.insuranceclaims.api.service.fhir.FHIRInsuranceClaimService;
import org.springframework.web.client.HttpServerErrorException;
import ca.uhn.fhir.context.FhirContext;

import java.net.URISyntaxException;

public class ClaimHttpRequestImpl implements ClaimHttpRequest {

    private FHIRClient fhirRequestClient;

    private FHIRInsuranceClaimService fhirInsuranceClaimService;

    private static final FhirContext fhirContext = FhirContext.forR4();

    @Override
    public Claim getClaimRequest(String resourceUrl, String claimCode) throws URISyntaxException {
        String url = resourceUrl + "/" + claimCode;
        return fhirRequestClient.getObject(url, Claim.class);
    }

    @Override
    public ClaimResponse sendClaimRequest(String resourceUrl, InsuranceClaim insuranceClaim)
            throws URISyntaxException, HttpServerErrorException, FHIRException {
        String url = resourceUrl + "/";
        Claim claimToSend = fhirInsuranceClaimService.generateClaim(insuranceClaim);

        return fhirRequestClient.postObject(url, claimToSend, ClaimResponse.class);
    }

    @Override
    public Bundle sendClaimBundleRequest(String resourceUrl, InsuranceClaim insuranceClaim)
            throws URISyntaxException, HttpServerErrorException, FHIRException {
        String url = resourceUrl + "/";
        System.out.println("Insurance Module Debug: Provider 2: " + insuranceClaim.getProvider());
        Claim claimToSend = fhirInsuranceClaimService.generateClaim(insuranceClaim);
        Patient patient = insuranceClaim.getPatient();
        Provider provider = insuranceClaim.getProvider();
        Encounter encounter = insuranceClaim.getEncounter();
        Bundle claimBundle = fhirInsuranceClaimService.generateClaimBundle(claimToSend, patient, provider, encounter);

        String bundleJSON = fhirContext.newJsonParser().setPrettyPrint(true).encodeResourceToString(claimBundle);
        System.err.println("Insurance Module: Sending claim bundle to server: " + bundleJSON);

        return fhirRequestClient.postObject(url, claimBundle, Bundle.class);
    }

    @Override
    public OperationOutcome sendClaimOperationOutcomeRequest(String resourceUrl, InsuranceClaim insuranceClaim)
            throws URISyntaxException, HttpServerErrorException, FHIRException {
        String url = resourceUrl + "/";
        System.out.println("Insurance Module Debug: Provider 2: " + insuranceClaim.getProvider());
        Claim claimToSend = fhirInsuranceClaimService.generateClaim(insuranceClaim);
        Patient patient = insuranceClaim.getPatient();
        Provider provider = insuranceClaim.getProvider();
        Encounter encounter = insuranceClaim.getEncounter();
        Bundle claimBundle = fhirInsuranceClaimService.generateClaimBundle(claimToSend, patient, provider, encounter);

        String bundleJSON = fhirContext.newJsonParser().setPrettyPrint(true).encodeResourceToString(claimBundle);
        System.err.println("Insurance Module: Sending claim bundle to server: " + bundleJSON);

        return fhirRequestClient.postObject(url, claimBundle, OperationOutcome.class);
    }

    @Override
    public ClaimResponse getClaimResponse(String baseUrl, String claimCode) throws URISyntaxException {
        String url = baseUrl + "/" + claimCode;
        return fhirRequestClient.getObject(url, ClaimResponse.class);
    }

    public void setFhirInsuranceClaimService(FHIRInsuranceClaimService fhirInsuranceClaimService) {
        this.fhirInsuranceClaimService = fhirInsuranceClaimService;
    }

    public void setFhirRequestClient(FHIRClient fhirRequestClient) {
        this.fhirRequestClient = fhirRequestClient;
    }
}

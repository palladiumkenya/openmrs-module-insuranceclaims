package org.openmrs.module.insuranceclaims.api.service.fhir.impl;

import org.hl7.fhir.r4.model.CoverageEligibilityRequest;
import org.hl7.fhir.r4.model.CoverageEligibilityResponse;
import org.hl7.fhir.r4.model.Reference;
import org.openmrs.module.insuranceclaims.api.service.fhir.FHIREligibilityService;

import static org.openmrs.module.insuranceclaims.api.service.fhir.util.IdentifierUtil.buildReference;

public class FHIREligibilityServiceImpl implements FHIREligibilityService {

    public static final String PATIENT = "Patient";

    @Override
    public CoverageEligibilityRequest generateEligibilityRequest(String policyId) {
        CoverageEligibilityRequest request = new CoverageEligibilityRequest();
        Reference patient = buildReference(PATIENT, policyId);
        request.setPatient(patient);

        return request;
    }
}
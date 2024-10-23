package org.openmrs.module.insuranceclaims.api.service.fhir;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Claim;

import org.hl7.fhir.exceptions.FHIRException;
import org.openmrs.Patient;
import org.openmrs.module.insuranceclaims.api.model.InsuranceClaim;

import java.util.List;

public interface FHIRInsuranceClaimService {
    Claim generateClaim(InsuranceClaim omrsClaim) throws FHIRException;
    Bundle generateClaimBundle(Claim fhirClaim, Patient patient) throws FHIRException;
    InsuranceClaim generateOmrsClaim(Claim claim, List<String> errors);

}

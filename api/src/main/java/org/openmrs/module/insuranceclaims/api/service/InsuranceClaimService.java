package org.openmrs.module.insuranceclaims.api.service;

import org.openmrs.api.APIException;
import org.openmrs.module.insuranceclaims.api.model.InsuranceClaim;

import java.util.List;

public interface InsuranceClaimService extends OpenmrsDataService<InsuranceClaim> {
    List<InsuranceClaim> getAllInsuranceClaimsByPatient(Integer patientId) throws APIException;
    List<InsuranceClaim> getAllInsuranceClaimsByPatient(String patientId) throws APIException;
    List<InsuranceClaim> getUnProcessedInsuranceClaims();
    List<InsuranceClaim> getAllInsuranceClaimsByCashierBill(String billUuid);

    InsuranceClaim updateClaim(InsuranceClaim claimToUpdate, InsuranceClaim updatedClaim);
}

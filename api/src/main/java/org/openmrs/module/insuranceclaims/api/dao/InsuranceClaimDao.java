package org.openmrs.module.insuranceclaims.api.dao;

import org.openmrs.module.insuranceclaims.api.model.InsuranceClaim;

import java.util.List;

public interface InsuranceClaimDao extends BaseOpenmrsCriteriaDao<InsuranceClaim> {
    List<InsuranceClaim> getAllInsuranceClaimsByPatient(Integer patientId);
    List<InsuranceClaim> getAllInsuranceClaimsByPatient(String patientId);
    List<InsuranceClaim> getAllInsuranceClaimsByCashierBill(String billUuid);
    List<InsuranceClaim> getUnProcessedInsuranceClaims();
}

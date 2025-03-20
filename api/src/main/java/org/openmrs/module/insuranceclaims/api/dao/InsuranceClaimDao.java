package org.openmrs.module.insuranceclaims.api.dao;

import org.openmrs.module.insuranceclaims.api.model.InsuranceClaim;

import java.util.Date;
import java.util.List;

public interface InsuranceClaimDao extends BaseOpenmrsCriteriaDao<InsuranceClaim> {
    List<InsuranceClaim> getAllInsuranceClaims();
    List<InsuranceClaim> getAllInsuranceClaimsByPatient(Integer patientId);
    List<InsuranceClaim> getAllInsuranceClaimsByPatient(String patientId);
    List<InsuranceClaim> getAllInsuranceClaimsByCashierBill(String billUuid);
    List<InsuranceClaim> getUnProcessedInsuranceClaims();
    List<InsuranceClaim> getInsuranceClaims(String uuid, String status, String usetype, String claimCode, Date createdOnOrAfterDate, Date createdOnOrBeforeDate);
}

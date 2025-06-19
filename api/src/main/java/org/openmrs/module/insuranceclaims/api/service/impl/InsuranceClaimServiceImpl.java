package org.openmrs.module.insuranceclaims.api.service.impl;

import org.openmrs.api.APIException;
import org.openmrs.module.insuranceclaims.api.dao.InsuranceClaimDao;
import org.openmrs.module.insuranceclaims.api.model.InsuranceClaim;
import org.openmrs.module.insuranceclaims.api.model.InsuranceClaimStatus;
import org.openmrs.module.insuranceclaims.api.service.InsuranceClaimService;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public class InsuranceClaimServiceImpl extends BaseOpenmrsDataService<InsuranceClaim> implements InsuranceClaimService {
    private InsuranceClaimDao insuranceClaimDao;

    public void setInsuranceClaimDao(InsuranceClaimDao insuranceClaimDao) {
        this.insuranceClaimDao = insuranceClaimDao;
    }

    @Transactional
    @Override
    public InsuranceClaim updateClaim(InsuranceClaim claimToUpdate, InsuranceClaim updatedClaim) {
        claimToUpdate.setAdjustment(updatedClaim.getAdjustment());
        updateQuantityApproved(claimToUpdate, updatedClaim);
        claimToUpdate.setDateProcessed(updatedClaim.getDateProcessed());
        claimToUpdate.setRejectionReason(updatedClaim.getRejectionReason());
        claimToUpdate.setStatus(updatedClaim.getStatus());

        saveOrUpdate(claimToUpdate);

        return claimToUpdate;
    }

    @Transactional
    @Override
    public List<InsuranceClaim> getAllInsuranceClaimsByPatient(Integer patientId) throws APIException {
        return this.insuranceClaimDao.getAllInsuranceClaimsByPatient(patientId);
    }

    @Transactional
    @Override
    public List<InsuranceClaim> getAllInsuranceClaims() throws APIException {
        return this.insuranceClaimDao.getAllInsuranceClaims();
    }

    @Transactional
    @Override
    public List<InsuranceClaim> getAllInsuranceClaimsByPatient(String patientId) throws APIException {
        return this.insuranceClaimDao.getAllInsuranceClaimsByPatient(patientId);
    }

    private void updateQuantityApproved(InsuranceClaim claimToUpdate, InsuranceClaim updatedClaim) {
        BigDecimal totalBenefit = updatedClaim.getApprovedTotal();
        InsuranceClaimStatus status = updatedClaim.getStatus();
        if (totalBenefit == null && status == InsuranceClaimStatus.CHECKED) {
            claimToUpdate.setApprovedTotal(claimToUpdate.getClaimedTotal());
        } else {
            claimToUpdate.setApprovedTotal(totalBenefit);
        }
    }

    @Override
    public List<InsuranceClaim> getUnProcessedInsuranceClaims() {
        return this.insuranceClaimDao.getUnProcessedInsuranceClaims();
    }

    @Override
    public List<InsuranceClaim> getAllInsuranceClaimsByCashierBill(String billUuid) {
        return this.insuranceClaimDao.getAllInsuranceClaimsByCashierBill(billUuid);
    }

    @Override
    public List<InsuranceClaim> getInsuranceClaims(String uuid, String status, String usetype, String claimCode,
            Date createdOnOrAfterDate, Date createdOnOrBeforeDate) {
        return this.insuranceClaimDao.getInsuranceClaims(uuid, status, usetype, claimCode, createdOnOrAfterDate,
                createdOnOrBeforeDate);
    }

    @Transactional
    @Override
    public InsuranceClaim getInsuranceClaimByExternalId(String externalId) {
        return this.insuranceClaimDao.getInsuranceClaimByExternalId(externalId);
    }

    /**
     * Maps claim-state extension values to local InsuranceClaimStatus enum values
     * 
     * @param status the status value from FHIR
     * @return corresponding local enum value
     */
    private InsuranceClaimStatus mapStatusToLocal(String status) {
        if (status == null || status.isEmpty()) {
            return InsuranceClaimStatus.ENTERED;
        }

        switch (status.toLowerCase()) {
            case "approved":
                return InsuranceClaimStatus.APPROVED;
            case "rejected":
                return InsuranceClaimStatus.REJECTED;
            case "in-review":
                return InsuranceClaimStatus.IN_REVIEW;
            case "clinical-review":
                return InsuranceClaimStatus.CLINICAL_REVIEW;
            case "sent-for-payment-processing":
                return InsuranceClaimStatus.SENT_FOR_PAYMENT_PROCESSING;
            case "sent-to-surveillance":
                return InsuranceClaimStatus.SENT_TO_SURVEILLANCE;
            case "payment-completed":
                return InsuranceClaimStatus.PAYMENT_COMPLETED;
            case "payment-declined":
                return InsuranceClaimStatus.PAYMENT_DECLINED;
            case "queued":
                return InsuranceClaimStatus.QUEUED;
            case "pending":
                return InsuranceClaimStatus.PENDING;
            case "returned back":
                return InsuranceClaimStatus.RETURNED_BACK;
            case "surveillance":
                return InsuranceClaimStatus.SURVEILLANCE;
            case "committee-review":
                return InsuranceClaimStatus.COMMITTEE_REVIEW;
            case "sent-back":
                return InsuranceClaimStatus.SENT_BACK;
            case "sent-for-payment":
                return InsuranceClaimStatus.SENT_FOR_PAYMENT;
            case "medical-review":
                return InsuranceClaimStatus.MEDICAL_REVIEW;
            default:
                return InsuranceClaimStatus.ENTERED;
        }
    }

    @Transactional
    @Override
    public InsuranceClaim updateClaimStatus(String externalId, String status) {
        InsuranceClaim claim = this.insuranceClaimDao.getInsuranceClaimByExternalId(externalId);
        if (claim != null) {
            InsuranceClaimStatus newStatus = mapStatusToLocal(status);
            claim.setStatus(newStatus);
            saveOrUpdate(claim);
        }
        return claim;
    }

}

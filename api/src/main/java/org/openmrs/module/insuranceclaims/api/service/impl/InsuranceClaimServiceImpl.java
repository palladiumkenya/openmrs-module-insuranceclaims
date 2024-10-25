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
        return this.insuranceClaimDao.getInsuranceClaims(uuid, status, usetype, claimCode, createdOnOrAfterDate, createdOnOrBeforeDate);
    }

}

package org.openmrs.module.insuranceclaims.api.service.fhir.impl;

import org.hl7.fhir.r4.model.ClaimResponse;
import org.hl7.fhir.r4.model.ClaimResponse.RemittanceOutcome;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Money;
import org.hl7.fhir.r4.model.ClaimResponse.PaymentComponent;

import org.openmrs.module.insuranceclaims.api.model.InsuranceClaim;
import org.openmrs.module.insuranceclaims.api.model.InsuranceClaimStatus;
import org.openmrs.module.insuranceclaims.api.service.fhir.FHIRClaimItemService;
import org.openmrs.module.insuranceclaims.api.service.fhir.FHIRClaimResponseService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.openmrs.module.insuranceclaims.api.service.fhir.util.ClaimResponseUtil.buildClaimReference;
import static org.openmrs.module.insuranceclaims.api.service.fhir.util.ClaimResponseUtil.buildCommunicationRequestReference;
import static org.openmrs.module.insuranceclaims.api.service.fhir.util.ClaimResponseUtil.createPaymentComponent;
import static org.openmrs.module.insuranceclaims.api.service.fhir.util.ClaimResponseUtil.getClaimCode;
import static org.openmrs.module.insuranceclaims.api.service.fhir.util.ClaimResponseUtil.getClaimErrors;
import static org.openmrs.module.insuranceclaims.api.service.fhir.util.ClaimResponseUtil.getClaimResponseErrorCode;
import static org.openmrs.module.insuranceclaims.api.service.fhir.util.ClaimResponseUtil.getClaimResponseOutcome;
import static org.openmrs.module.insuranceclaims.api.service.fhir.util.ClaimResponseUtil.getClaimResponseStatus;
import static org.openmrs.module.insuranceclaims.api.service.fhir.util.ClaimResponseUtil.getClaimUuid;
import static org.openmrs.module.insuranceclaims.api.service.fhir.util.InsuranceClaimUtil.createClaimIdentifier;

public class FHIRClaimResponseServiceImpl implements FHIRClaimResponseService {

    private FHIRClaimItemService itemService;

    @Override
    public ClaimResponse generateClaimResponse(InsuranceClaim omrsClaim) {
        ClaimResponse claim = new ClaimResponse();

        //id
        IdType claimId = new IdType();
        claimId.setValue(omrsClaim.getClaimCode());
        claim.setId(claimId);

        //identifier
        claim.setIdentifier(createClaimIdentifier(omrsClaim));

        //status
        claim.setOutcome(convertToRemittanceOutcome(getClaimResponseOutcome(omrsClaim)));

        //payment
        claim.setPayment(createPaymentComponent(omrsClaim));

        //adjustiment
        claim.setDisposition(omrsClaim.getAdjustment());

        //totalBenefit
        Money benefit = new Money();
        benefit.setValue(omrsClaim.getApprovedTotal());
        // claim.setTotalBenefit(benefit);
        PaymentComponent paymentComponent = new PaymentComponent();
        paymentComponent.setAmount(benefit);
        claim.setPayment(paymentComponent);

        //date created
        claim.setCreated(omrsClaim.getDateProcessed());

        //error
        claim.setError(getClaimErrors(omrsClaim));

        //processNote
        claim.setProcessNote(itemService.generateClaimResponseNotes(omrsClaim));

        //items
        claim.setItem(itemService.generateClaimResponseItemComponent(omrsClaim));

        //request
        claim.setRequest(buildClaimReference(omrsClaim));

        //communicationRequest
        claim.setCommunicationRequest(buildCommunicationRequestReference(omrsClaim));

        return claim;
    }

    @Override
    public InsuranceClaim generateOmrsClaim(ClaimResponse claim, List<String> errors) {
        InsuranceClaim omrsClaim = new InsuranceClaim();

        //id
        // BaseOpenMRSDataUtil.readBaseExtensionFields(omrsClaim, claim);
        // BaseOpenMRSDataUtil.setBaseExtensionFields(claim, omrsClaim);

        omrsClaim.setUuid(getClaimUuid(claim, errors));

        //identifier
        omrsClaim.setClaimCode(getClaimCode(claim, errors));

        //status
        InsuranceClaimStatus status = getClaimResponseStatus(claim, errors);
        omrsClaim.setStatus(status);

        //adjustiment
        omrsClaim.setAdjustment(claim.getPayment().getAdjustmentReason().getText());

        //approved total
        // omrsClaim.setApprovedTotal(claim.getTotalBenefit().getValue());
        omrsClaim.setApprovedTotal(claim.getPayment().getAmount().getValue());

        //date processed
        //Use date or payment?
        omrsClaim.setDateProcessed(claim.getPayment().getDate());

        //error
        omrsClaim.setRejectionReason(getClaimResponseErrorCode(claim));

        return omrsClaim;
    }

    public void setItemService(FHIRClaimItemService itemService) {
        this.itemService = itemService;
    }

    public static RemittanceOutcome convertToRemittanceOutcome(CodeableConcept codeableConcept) throws IllegalArgumentException {
        Map<String, RemittanceOutcome> outcomeMapping = new HashMap<>();

        outcomeMapping.put("complete", RemittanceOutcome.COMPLETE);
        outcomeMapping.put("error", RemittanceOutcome.ERROR);
        outcomeMapping.put("partial", RemittanceOutcome.PARTIAL);

        if (codeableConcept == null || codeableConcept.getCoding().isEmpty()) {
            throw new IllegalArgumentException("Invalid CodeableConcept");
        }

        for (Coding coding : codeableConcept.getCoding()) {
            String code = coding.getCode();
            if (outcomeMapping.containsKey(code)) {
                return outcomeMapping.get(code);
            }
        }

        throw new IllegalArgumentException("No matching RemittanceOutcome for provided CodeableConcept");
    }
}

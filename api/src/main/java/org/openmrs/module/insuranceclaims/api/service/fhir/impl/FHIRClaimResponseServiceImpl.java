package org.openmrs.module.insuranceclaims.api.service.fhir.impl;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.ClaimResponse;
import org.hl7.fhir.r4.model.ClaimResponse.RemittanceOutcome;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.Element;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Money;
import org.hl7.fhir.r4.model.PrimitiveType;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.ClaimResponse.PaymentComponent;
import org.openmrs.BaseOpenmrsData;
import org.openmrs.BaseOpenmrsMetadata;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.module.insuranceclaims.api.model.InsuranceClaim;
import org.openmrs.module.insuranceclaims.api.model.InsuranceClaimStatus;
import org.openmrs.module.insuranceclaims.api.service.fhir.FHIRClaimItemService;
import org.openmrs.module.insuranceclaims.api.service.fhir.FHIRClaimResponseService;

import java.util.Date;
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
        readBaseExtensionFields(omrsClaim, claim);
        setBaseExtensionFields(claim, omrsClaim);

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
        final String DATE_CREATED_URL = "http://fhir-es.transcendinsights.com/stu3/StructureDefinition/resource-date-created";
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

        final String DATE_CREATED_URL = "http://fhir-es.transcendinsights.com/stu3/StructureDefinition/resource-date-created";
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

    public static Extension createDateCreatedExtension(Date dateCreated) {
        final String DATE_CREATED_URL = "http://fhir-es.transcendinsights.com/stu3/StructureDefinition/resource-date-created";
		return createExtension(DATE_CREATED_URL, new DateTimeType(dateCreated));
	}

    private static Extension createExtension(String url, PrimitiveType data) {
		Extension extension = new Extension();

		extension.setUrl(url);
		extension.setValue(data);

		return extension;
	}

    public static Extension createCreatorExtension(User creator) {
        final String CREATOR_URL = "https://purl.org/elab/fhir/StructureDefinition/Creator-crew-version1";
		if (creator == null) {
			return null;
		}
		return createExtension(CREATOR_URL, new StringType(creator.getUsername()));
	}

    public static void setBaseExtensionFields(DomainResource fhirResource, BaseOpenmrsData openmrsData) {
        fhirResource.addExtension(createDateCreatedExtension(openmrsData.getDateCreated()));
        fhirResource.addExtension(createCreatorExtension(openmrsData.getCreator()));

        if (openmrsData.getDateChanged() != null) {
            fhirResource.addExtension(createDateChangedExtension(openmrsData.getDateChanged()));
            fhirResource.addExtension(createChangedByExtension(openmrsData.getChangedBy()));
        }
        if (openmrsData.getVoided()) {
            fhirResource.addExtension(createVoidedExtension(openmrsData.getVoided()));
            fhirResource.addExtension(createDateVoidedExtension(openmrsData.getDateVoided()));
            fhirResource.addExtension(createVoidedByExtension(openmrsData.getVoidedBy()));
            fhirResource.addExtension(createVoidReasonExtension(openmrsData.getVoidReason()));
        }
    }

    public static void setBaseExtensionFields(Element element, BaseOpenmrsData openmrsData) {
        element.addExtension(createDateCreatedExtension(openmrsData.getDateCreated()));
        element.addExtension(createCreatorExtension(openmrsData.getCreator()));

        if (openmrsData.getDateChanged() != null) {
            element.addExtension(createDateChangedExtension(openmrsData.getDateChanged()));
            element.addExtension(createChangedByExtension(openmrsData.getChangedBy()));
        }
        if (openmrsData.getVoided()) {
            element.addExtension(createVoidedExtension(openmrsData.getVoided()));
            element.addExtension(createDateVoidedExtension(openmrsData.getDateVoided()));
            element.addExtension(createVoidedByExtension(openmrsData.getVoidedBy()));
            element.addExtension(createVoidReasonExtension(openmrsData.getVoidReason()));
        }
    }
    
    public static void setBaseExtensionFields(DomainResource fhirResource, BaseOpenmrsMetadata openmrsMetadata) {
        fhirResource.addExtension(createDateCreatedExtension(openmrsMetadata.getDateCreated()));
        fhirResource.addExtension(createCreatorExtension(openmrsMetadata.getCreator()));

        if (openmrsMetadata.getDateChanged() != null) {
            fhirResource.addExtension(createDateChangedExtension(openmrsMetadata.getDateChanged()));
            fhirResource.addExtension(createChangedByExtension(openmrsMetadata.getChangedBy()));
        }
        if (openmrsMetadata.getRetired()) {
            fhirResource.addExtension(createRetiredExtension(openmrsMetadata.getRetired()));
            fhirResource.addExtension(createDateRetiredExtension(openmrsMetadata.getDateRetired()));
            fhirResource.addExtension(createRetiredByExtension(openmrsMetadata.getRetiredBy()));
            fhirResource.addExtension(createRetireReasonExtension(openmrsMetadata.getRetireReason()));
        }
    }

    public static Extension createRetireReasonExtension(String reason) {
        final String RETIRE_REASON_URL = "retireReason";
		return createExtension(RETIRE_REASON_URL, new StringType(reason));
	}

    public static Extension createRetiredByExtension(User user) {
        final String RETIRED_BY_URL = "retiredBy";
		if (user == null) {
			return null;
		}
		return createExtension(RETIRED_BY_URL, new StringType(user.getUsername()));
	}

    public static Extension createDateRetiredExtension(Date dateRetired) {
        final String DATE_RETIRED_URL = "dateRetired";
		return createExtension(DATE_RETIRED_URL, new DateTimeType(dateRetired));
	}

    public static Extension createVoidReasonExtension(String reason) {
        final String VOID_REASON_URL = "voidReason";
		return createExtension(VOID_REASON_URL, new StringType(reason));
	}

    public static Extension createVoidedByExtension(User user) {
        final String VOIDED_BY_URL = "voidedBy";
		if (user == null) {
			return null;
		}
		return createExtension(VOIDED_BY_URL, new StringType(user.getUsername()));
	}


    public static Extension createDateVoidedExtension(Date dateVoided) {
        final String DATE_VOIDED_URL = "dateVoided";
		return createExtension(DATE_VOIDED_URL, new DateTimeType(dateVoided));
	}

    public static Extension createRetiredExtension(boolean retired) {
        final String RETIRED_URL = "retired";
		return createExtension(RETIRED_URL, new BooleanType(retired));
	}

    public static Extension createVoidedExtension(boolean voided) {
        final String VOIDED_URL = "voided";
		return createExtension(VOIDED_URL, new BooleanType(voided));
	}

    public static Extension createChangedByExtension(User user) {
        final String CHANGED_BY_URL = "changedBy";
		if (user == null) {
			return null;
		}
		return createExtension(CHANGED_BY_URL, new StringType(user.getUsername()));
	}

    public static Extension createDateChangedExtension(Date dateChanged) {
        final String DATE_CHANGED_URL = "dateChanged";
		return createExtension(DATE_CHANGED_URL, new DateTimeType(dateChanged));
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

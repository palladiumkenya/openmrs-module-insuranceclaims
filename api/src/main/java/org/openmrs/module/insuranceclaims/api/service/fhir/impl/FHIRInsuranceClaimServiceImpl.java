package org.openmrs.module.insuranceclaims.api.service.fhir.impl;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.exceptions.FHIRException;

import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.Claim;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.Element;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Money;
import org.hl7.fhir.r4.model.Period;
import org.hl7.fhir.r4.model.PrimitiveType;
import org.openmrs.BaseOpenmrsData;
import org.openmrs.BaseOpenmrsMetadata;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.User;
import org.openmrs.VisitType;
import org.openmrs.api.context.Context;
import org.openmrs.module.insuranceclaims.api.model.InsuranceClaim;
import org.openmrs.module.insuranceclaims.api.service.db.AttributeService;
import org.openmrs.module.insuranceclaims.api.service.fhir.FHIRClaimDiagnosisService;
import org.openmrs.module.insuranceclaims.api.service.fhir.FHIRClaimItemService;
import org.openmrs.module.insuranceclaims.api.service.fhir.FHIRInsuranceClaimService;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.openmrs.module.insuranceclaims.api.service.fhir.util.IdentifierUtil.getIdFromReference;
import static org.openmrs.module.insuranceclaims.api.service.fhir.util.IdentifierUtil.getUnambiguousElement;
import static org.openmrs.module.insuranceclaims.api.service.fhir.util.InsuranceClaimConstants.PERIOD_FROM;
import static org.openmrs.module.insuranceclaims.api.service.fhir.util.InsuranceClaimConstants.PERIOD_TO;
import static org.openmrs.module.insuranceclaims.api.service.fhir.util.InsuranceClaimUtil.createClaimExplanationInformation;
import static org.openmrs.module.insuranceclaims.api.service.fhir.util.InsuranceClaimUtil.createClaimGuaranteeIdInformation;
import static org.openmrs.module.insuranceclaims.api.service.fhir.util.InsuranceClaimUtil.createClaimIdentifier;
import static org.openmrs.module.insuranceclaims.api.service.fhir.util.InsuranceClaimUtil.createClaimVisitType;
import static org.openmrs.module.insuranceclaims.api.service.fhir.util.InsuranceClaimUtil.getClaimBillablePeriod;
import static org.openmrs.module.insuranceclaims.api.service.fhir.util.InsuranceClaimUtil.getClaimCode;
import static org.openmrs.module.insuranceclaims.api.service.fhir.util.InsuranceClaimUtil.getClaimDateCreated;
import static org.openmrs.module.insuranceclaims.api.service.fhir.util.InsuranceClaimUtil.getClaimExplanation;
import static org.openmrs.module.insuranceclaims.api.service.fhir.util.InsuranceClaimUtil.getClaimGuaranteeId;
import static org.openmrs.module.insuranceclaims.api.service.fhir.util.InsuranceClaimUtil.getClaimUuid;
import static org.openmrs.module.insuranceclaims.api.service.fhir.util.LocationUtil.buildLocationReference;
import org.openmrs.module.insuranceclaims.api.service.fhir.util.PatientUtil;
import org.openmrs.module.insuranceclaims.api.service.fhir.util.PractitionerUtil;

public class FHIRInsuranceClaimServiceImpl implements FHIRInsuranceClaimService {

    private AttributeService attributeService;

    private FHIRClaimItemService claimItemService;

    private FHIRClaimDiagnosisService claimDiagnosisService;

    @Override
    public Claim generateClaim(InsuranceClaim omrsClaim) throws FHIRException {
        Claim claim = new Claim();
        PractitionerUtil practitionerUtil = new PractitionerUtil();
        PatientUtil patientUtil = new PatientUtil();
        setBaseExtensionFields(claim, omrsClaim);

        //Set Claim id to fhir Claim
        IdType claimId = new IdType();
        claimId.setValue(omrsClaim.getClaimCode());
        claim.setId(claimId);

        //Set provider
        Reference providerReference = practitionerUtil.buildPractitionerReference(omrsClaim);
        claim.setProvider(providerReference);
        //Set enterer
        claim.setEnterer(providerReference);

        //Set patient
        claim.setPatient(patientUtil.buildPatientReference(omrsClaim));

        //Set facility
        claim.setFacility(buildLocationReference(omrsClaim));

        //Set identifier
        List<Identifier> identifiers = createClaimIdentifier(omrsClaim);
        claim.setIdentifier(identifiers);

        //Set billablePeriod
        Period billablePeriod = new Period();
        billablePeriod.setStart(omrsClaim.getDateFrom());
        billablePeriod.setEnd(omrsClaim.getDateTo());
        claim.setBillablePeriod(billablePeriod);

        //Set total
        Money total = new Money();
        total.setValue(omrsClaim.getClaimedTotal());
        claim.setTotal(total);

        //Set created
        claim.setCreated(omrsClaim.getDateCreated());

        //Set information
        List<Claim.SupportingInformationComponent> claimInformation = new ArrayList<>();

        claimInformation.add(createClaimGuaranteeIdInformation(omrsClaim));
        claimInformation.add(createClaimExplanationInformation(omrsClaim));

        claim.setSupportingInfo(claimInformation);

        //Set type
        claim.setType(createClaimVisitType(omrsClaim));
        //Set items
        claimItemService.assignItemsWithInformationToClaim(claim, omrsClaim);

        //Set diagnosis
        claim.setDiagnosis(claimDiagnosisService.generateClaimDiagnosisComponent(omrsClaim));

        return claim;
    }

    @Override
    public InsuranceClaim generateOmrsClaim(Claim claim, List<String> errors) {
        InsuranceClaim omrsClaim = new InsuranceClaim();

        readBaseExtensionFields(omrsClaim, claim);
        setBaseExtensionFields(claim, omrsClaim);

        omrsClaim.setUuid(getClaimUuid(claim, errors));

        //Set provider
        omrsClaim.setProvider(getClaimProviderByExternalId(claim));

        //Set patient
        omrsClaim.setPatient(getClaimPatientByExternalIdentifier(claim));

        //Set facility
        omrsClaim.setLocation(getClaimLocationByExternalId(claim));

        //Set identifier
        omrsClaim.setClaimCode(getClaimCode(claim, errors));

        //Set billablePeriod
        Map<String, Date> period = getClaimBillablePeriod(claim, errors);
        omrsClaim.setDateFrom(period.get(PERIOD_FROM));
        omrsClaim.setDateTo(period.get(PERIOD_TO));

        //Set total
        Money total = claim.getTotal();
        omrsClaim.setClaimedTotal(total.getValue());

        //Set created
        omrsClaim.setDateCreated(getClaimDateCreated(claim, errors));

        //Set explanation
        omrsClaim.setExplanation(getClaimExplanation(claim, errors));

        //Set guaranteeId
        omrsClaim.setGuaranteeId(getClaimGuaranteeId(claim, errors));

        //Set type
        omrsClaim.setVisitType(getClaimVisitType(claim));

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

    public void setAttributeService(AttributeService attributeService) {
        this.attributeService = attributeService;
    }

    public void setClaimItemService(FHIRClaimItemService claimItemService) {
        this.claimItemService = claimItemService;
    }

    public void setClaimDiagnosisService(FHIRClaimDiagnosisService claimDiagnosisService) {
        this.claimDiagnosisService = claimDiagnosisService;
    }

    private Provider getClaimProviderByExternalId(Claim claim) {
        String practitionerExternalId = getIdFromReference(claim.getEnterer());
        List<Provider> providersWithExernalId = attributeService.getProviderByExternalIdAttribute(practitionerExternalId);
        return getUnambiguousElement(providersWithExernalId);
    }

    private Location getClaimLocationByExternalId(Claim claim) {
        String locationExternalId = getIdFromReference(claim.getFacility());
        List<Location> locationsWithExternalId = attributeService.getLocationByExternalIdAttribute(locationExternalId);
        return getUnambiguousElement(locationsWithExternalId);
    }

    private Patient getClaimPatientByExternalIdentifier(Claim claim) {
        String patientExternalId = getIdFromReference(claim.getPatient());
        List<Patient> patientsWithExternalId = attributeService.getPatientByExternalIdIdentifier(patientExternalId);
        return getUnambiguousElement(patientsWithExternalId);
    }

    private  static VisitType getClaimVisitType(Claim claim) {
        String visitTypeName = claim.getType().getText();
        List<VisitType> visitType = getVisitTypeByName(visitTypeName);
        return getUnambiguousElement(visitType);
    }

    private static List<VisitType> getVisitTypeByName(String visitTypeName) {
        return Context.getVisitService().getVisitTypes(visitTypeName);
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

    public static Extension createDateCreatedExtension(Date dateCreated) {
        final String DATE_CREATED_URL = "http://fhir-es.transcendinsights.com/stu3/StructureDefinition/resource-date-created";
		return createExtension(DATE_CREATED_URL, new DateTimeType(dateCreated));
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

}


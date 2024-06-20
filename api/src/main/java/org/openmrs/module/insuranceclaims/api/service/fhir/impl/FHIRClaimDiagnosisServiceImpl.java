package org.openmrs.module.insuranceclaims.api.service.fhir.impl;

import static org.openmrs.module.insuranceclaims.api.service.fhir.util.IdentifierUtil.getUnambiguousElement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.Claim;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.Element;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.StringType;
import org.openmrs.BaseOpenmrsData;
import org.openmrs.BaseOpenmrsMetadata;
import org.openmrs.Concept;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.module.fhir2.api.translators.ConceptTranslator;
import org.openmrs.module.fhir2.api.translators.impl.ConceptTranslatorImpl;
import org.openmrs.module.insuranceclaims.api.model.InsuranceClaim;
import org.openmrs.module.insuranceclaims.api.model.InsuranceClaimDiagnosis;
import org.openmrs.module.insuranceclaims.api.service.db.DiagnosisDbService;
import org.openmrs.module.insuranceclaims.api.service.fhir.FHIRClaimDiagnosisService;
import org.openmrs.module.insuranceclaims.api.service.fhir.util.InsuranceClaimConstants;
import org.openmrs.module.insuranceclaims.util.OpenmrsUtils;

public class FHIRClaimDiagnosisServiceImpl implements FHIRClaimDiagnosisService {

    private DiagnosisDbService diagnosisDbService;
    private ConceptTranslator conceptTranslator = new ConceptTranslatorImpl();
    // private CodingTranslator medicationCodingTranslator = new MedicationQuantityCodingTranslatorImpl();
    // private ConceptTranslator conceptTranslator = Context.getRegisteredComponent("fhir2.ConceptTranslator", ConceptTranslator.class);
    // private ConceptTranslator conceptTranslator = Context.getRegisteredComponent("conceptTranslator", ConceptTranslator.class);

    @Override
    public Claim.DiagnosisComponent generateClaimDiagnosisComponent(InsuranceClaimDiagnosis omrsClaimDiagnosis) {
        Claim.DiagnosisComponent newDiagnosis = new Claim.DiagnosisComponent();

        try {
            Concept diagnosisConcept = omrsClaimDiagnosis.getConcept();
            System.out.println("Diagnosis concept: " + diagnosisConcept);
            CodeableConcept diagnosis = conceptTranslator.toFhirResource(diagnosisConcept);

            newDiagnosis.setId(omrsClaimDiagnosis.getUuid());
            newDiagnosis.setDiagnosis(diagnosis);
        } catch(Exception ex) {
            System.err.println("Diagnosis error: " + ex.getMessage());
            ex.printStackTrace();
        }

        return newDiagnosis;
    }

    @Override
    public List<Claim.DiagnosisComponent> generateClaimDiagnosisComponent(
            List<InsuranceClaimDiagnosis> omrsClaimDiagnosis) {
        List<Claim.DiagnosisComponent> allDiagnosisComponents = new ArrayList<>();

        for (InsuranceClaimDiagnosis insuranceClaimDiagnosis : omrsClaimDiagnosis) {
            Claim.DiagnosisComponent nextDiagnosis = generateClaimDiagnosisComponent(insuranceClaimDiagnosis);
            allDiagnosisComponents.add(nextDiagnosis);
        }
        return allDiagnosisComponents;
    }

    @Override
    public List<Claim.DiagnosisComponent> generateClaimDiagnosisComponent(InsuranceClaim omrsInsuranceClaim)
    throws FHIRException {
        List<InsuranceClaimDiagnosis> claimDiagnoses =
                diagnosisDbService.findInsuranceClaimDiagnosis(omrsInsuranceClaim.getId());
        List<Claim.DiagnosisComponent> fhirDiagnosisComponent = generateClaimDiagnosisComponent(claimDiagnoses);
        addCodingToDiagnosis(fhirDiagnosisComponent);
        return fhirDiagnosisComponent;
    }

    @Override
    public InsuranceClaimDiagnosis createOmrsClaimDiagnosis(Claim.DiagnosisComponent claimDiagnosis, List<String> errors) {
        InsuranceClaimDiagnosis diagnosis = new InsuranceClaimDiagnosis();
        readBaseExtensionFields(diagnosis, claimDiagnosis);

        if (claimDiagnosis.getId() != null) {
            diagnosis.setUuid(extractUuid(claimDiagnosis.getId()));
        }
        try {
            validateDiagnosisCodingSystem(claimDiagnosis);
            diagnosis.setConcept(getConceptByCodeableConcept(claimDiagnosis.getDiagnosisCodeableConcept(), errors));
        } catch (FHIRException e) {
            errors.add(e.getMessage());
        }

        return diagnosis;
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
    
    public static String extractUuid(String uuid) {
		return uuid.contains("/") ? uuid.substring(uuid.indexOf("/") + 1) : uuid;
	}
      

    public void setDiagnosisDbService(DiagnosisDbService diagnosisDao) {
        this.diagnosisDbService = diagnosisDao;
    }

    private void addCodingToDiagnosis(List<Claim.DiagnosisComponent>  diagnosisComponents) throws FHIRException {
        for (Claim.DiagnosisComponent diagnosis: diagnosisComponents) {
            setDiagnosisPrimaryCoding(diagnosis);
            List<Coding> coding = diagnosis.getDiagnosisCodeableConcept().getCoding();
            List<CodeableConcept> diagnosisType = coding.stream()
                    .map(Coding::getSystem)
                    .map(systemName -> new CodeableConcept().setText(systemName))
                    .collect(Collectors.toList());
            diagnosis.setType(diagnosisType);
        }
    }

    private void setDiagnosisPrimaryCoding(Claim.DiagnosisComponent diagnosis) throws FHIRException {
        String primaryCoding = Context.getAdministrationService().getGlobalProperty(InsuranceClaimConstants.PRIMARY_DIAGNOSIS_MAPPING);
        List<Coding> diagnosisCoding = diagnosis.getDiagnosisCodeableConcept().getCoding();

        for (Coding c : diagnosisCoding) {
            if (c.getSystem().equals(primaryCoding)) {
                Collections.swap(diagnosisCoding, 0, diagnosisCoding.indexOf(c));
                break;
            }
        }
    }

    private void validateDiagnosisCodingSystem(Claim.DiagnosisComponent diagnosis) throws FHIRException {
        //Method assigns diagnosis system (ICD-10, CIEL, etc.) from type to the concept coding if it don't have assigned system
        if (CollectionUtils.isEmpty(diagnosis.getType())) {
            return;
        }
        String codingSystem = diagnosis.getTypeFirstRep().getText();
        CodeableConcept concept = diagnosis.getDiagnosisCodeableConcept();
        for (Coding coding : concept.getCoding()) {
            if (coding.getSystem() == null) {
                coding.setSystem(codingSystem);
            }
        }
    }

    private Concept getConceptByCodeableConcept(CodeableConcept codeableConcept, List<String> errors) {
        List<Coding> diagnosisCoding = codeableConcept.getCoding();
        List<Concept> concept = getConceptByFHIRCoding(diagnosisCoding);

        Concept uniqueConcept = getUnambiguousElement(concept);
        if (uniqueConcept == null) {
            errors.add("No matching concept found for the given codings: \n" + diagnosisCoding);
            return null;
        } else {
            return uniqueConcept;
        }
    }

    private List<Concept> getConceptByFHIRCoding(List<Coding> coding) {
        return coding.stream()
                .map(this::getConceptByFHIRCoding)
                .collect(Collectors.toList());
    }

    private Concept getConceptByFHIRCoding(Coding coding) {
        String conceptCode = coding.getCode();
        String systemName = coding.getSystem();

        Concept concept = null;
        String baseUrl = OpenmrsUtils.getOpenmrsBaseUrl();
        if (baseUrl.equals(systemName)) {
            concept = Context.getConceptService().getConceptByUuid(conceptCode);
        } else { if (StringUtils.isNotEmpty(systemName)) {
                List<Concept> concepts =  Context.getConceptService().getConceptsByMapping(conceptCode, systemName);
                concept = getUnambiguousElement(concepts);
            }
        }
        return concept;
    }
}

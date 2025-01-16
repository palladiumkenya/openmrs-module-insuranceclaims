package org.openmrs.module.insuranceclaims.api.service.fhir.impl;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Claim.ClaimStatus;
import org.hl7.fhir.r4.model.Claim.InsuranceComponent;
import org.hl7.fhir.r4.model.Claim.Use;
import org.hl7.fhir.r4.model.Encounter.EncounterStatus;
import org.hl7.fhir.exceptions.FHIRException;

import org.hl7.fhir.r4.model.Coverage;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.MessageHeader;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.Claim;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
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
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.Person;
import org.openmrs.Provider;
import org.openmrs.ProviderAttribute;
import org.openmrs.ProviderAttributeType;
import org.openmrs.User;
import org.openmrs.VisitType;
import org.openmrs.api.ProviderService;
import org.openmrs.api.context.Context;
import org.openmrs.module.fhir2.api.translators.EncounterTranslator;
import org.openmrs.module.fhir2.api.translators.LocationTranslator;
import org.openmrs.module.fhir2.api.translators.PatientIdentifierTranslator;
import org.openmrs.module.fhir2.api.translators.PatientTranslator;
import org.openmrs.module.insuranceclaims.api.model.InsuranceClaim;
import org.openmrs.module.insuranceclaims.api.service.db.AttributeService;
import org.openmrs.module.insuranceclaims.api.service.fhir.FHIRClaimDiagnosisService;
import org.openmrs.module.insuranceclaims.api.service.fhir.FHIRClaimItemService;
import org.openmrs.module.insuranceclaims.api.service.fhir.FHIRInsuranceClaimService;
import org.openmrs.module.fhir2.api.translators.PractitionerTranslator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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

import org.openmrs.module.insuranceclaims.api.service.fhir.util.GeneralUtil;
import org.openmrs.module.insuranceclaims.api.service.fhir.util.PatientUtil;
import org.openmrs.module.insuranceclaims.api.service.fhir.util.PractitionerUtil;

public class FHIRInsuranceClaimServiceImpl implements FHIRInsuranceClaimService {

    private AttributeService attributeService;

    private FHIRClaimItemService claimItemService;

    private FHIRClaimDiagnosisService claimDiagnosisService;

	private PatientTranslator patientTranslator;

	private PractitionerTranslator<Provider> practitionerTranslator;

	private EncounterTranslator<org.openmrs.Encounter> encounterTranslator;

	private LocationTranslator locationTranslator;

	private PatientIdentifierTranslator patientIdentifierTranslator;

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

        //Set status
        claim.setStatus(ClaimStatus.ACTIVE);

        //Set use
	    if(omrsClaim.getUse().trim().equalsIgnoreCase("claim")) {
		    claim.setUse(Use.CLAIM);
	    } else if(omrsClaim.getUse().trim().equalsIgnoreCase("preauthorization")) {
	    	claim.setUse(Use.PREAUTHORIZATION);
	    } else if(omrsClaim.getUse().trim().equalsIgnoreCase("predetermination")) {
		    claim.setUse(Use.PREDETERMINATION);
	    } else {
		    claim.setUse(Use.CLAIM);
	    }

        // Set type
            // Create a new CodeableConcept
            CodeableConcept claimTypeConcept = new CodeableConcept();       
            // Create a new Coding
            Coding typeCoding = new Coding();
            // Set the system, code, and display values
            typeCoding.setSystem("http://terminology.hl7.org/CodeSystem/claim-type");
            typeCoding.setCode("institutional");
            typeCoding.setDisplay("Institutional");
            // Add the Coding to the CodeableConcept
            claimTypeConcept.addCoding(typeCoding);
            // Optionally, set the text value
            claimTypeConcept.setText("Institutional Claim Type");
        claim.setType(claimTypeConcept);

        // Set Priority
            // Create a new CodeableConcept
            CodeableConcept claimPriorityConcept = new CodeableConcept();
            // Create a new Coding
            Coding priorityCoding = new Coding();
            // Set the system, code, and display values
            priorityCoding.setSystem("http://terminology.hl7.org/CodeSystem/processpriority");
            priorityCoding.setCode("normal");
            priorityCoding.setDisplay("Normal");
            // Add the Coding to the CodeableConcept
            claimPriorityConcept.addCoding(priorityCoding);
            // Optionally, set the text value
            claimPriorityConcept.setText("Normal Priority");
        claim.setPriority(claimPriorityConcept);

        // Set Insurance
        List<InsuranceComponent> insuranceList = new ArrayList<>();
        InsuranceComponent insuranceComponent = new InsuranceComponent();
        // Set the sequence (order of the insurance in case of multiple insurances)
        insuranceComponent.setSequence(1);
        // Set whether this insurance is focal (i.e., primary insurance)
        insuranceComponent.setFocal(true);
        // Set the coverage (reference to the Coverage resource)
        Reference coverageReference = new Reference();
        coverageReference.setReference("Coverage/12345"); // Adjust the reference as necessary
        insuranceComponent.setCoverage(coverageReference);
        // Set the business identifier for the insurance
        Identifier identifier = new Identifier();
        identifier.setSystem("http://example.org/insurance-identifier");
        identifier.setValue("ABC12345"); // Example identifier, adjust as necessary
        insuranceComponent.setIdentifier(identifier);
        insuranceList.add(insuranceComponent);
        claim.setInsurance(insuranceList);

        //Set facility
//        claim.setFacility(buildLocationReference(omrsClaim));

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
        // claim.setType(createClaimVisitType(omrsClaim));

        //Set items
        claimItemService.assignItemsWithInformationToClaim(claim, omrsClaim);

        //Set diagnosis
        claim.setDiagnosis(claimDiagnosisService.generateClaimDiagnosisComponent(omrsClaim));

        return claim;
    }

	@Override
	public Bundle generateClaimBundle(Claim fhirClaim, Patient patient, Provider provider, org.openmrs.Encounter encounter) throws FHIRException {
		Bundle ret = new Bundle();

		ret.setType(Bundle.BundleType.BATCH);
		ret.setTimestamp(new Date());

		// Add message header
		MessageHeader messageHeader = new MessageHeader();
		messageHeader.setEvent(new Coding().setSystem("http://hl7.org/fhir/message-events").setCode("claim"));
		messageHeader.setSource(new MessageHeader.MessageSourceComponent().setName("FHIR Client"));

		// Add Claim to bundle
		ret.addEntry(createBundleEntry(fhirClaim));

		// Add Encounter to bundle
		org.hl7.fhir.r4.model.Encounter fHIREncounter = encounterTranslator.toFhirResource(encounter);
        // status
        fHIREncounter.setStatus(EncounterStatus.FINISHED);
        // period
        if(fHIREncounter.getPeriod().getEnd() == null) {
            Period encounterPeriod = fHIREncounter.getPeriod();
            Date dateNow = new Date();
            encounterPeriod.setEnd(dateNow);
            fHIREncounter.setPeriod(encounterPeriod);
        }
        // priority
        CodeableConcept encounterPriority = new CodeableConcept();
        Coding encounterPriorityCoding = new Coding();
        encounterPriorityCoding.setCode("routine");
        encounterPriorityCoding.setSystem("https://hl7.org/fhir/R4/v3/ActPriority/vs.html");
        encounterPriorityCoding.setDisplay("routine");
        encounterPriority.setCoding(Collections.singletonList(encounterPriorityCoding));
        fHIREncounter.setPriority(encounterPriority);
        // identifier
        List<Identifier> encounterIdentifiers = new ArrayList<>();
        Identifier encounterIdentifier = new Identifier();
        encounterIdentifier.setSystem("https://hmisv15-clone.tiberbu.health");
        encounterIdentifier.setValue(fHIREncounter.getId());
        encounterIdentifiers.add(encounterIdentifier);
        fHIREncounter.setIdentifier(encounterIdentifiers);
        // subject
        Reference encounterSubject = fHIREncounter.getSubject();
        String CR = GeneralUtil.getPatientCRNo(patient);
        String encRef = "https://cr.kenya-hie.health/api/v4/Patient/" + CR;
        encounterSubject.setReference(encRef);
        Identifier encounterPatientIdentifier = new Identifier();
        encounterPatientIdentifier.setSystem("https://cr.kenya-hie.health/api/v4/Patient");
        encounterPatientIdentifier.setValue(CR);
        encounterSubject.setIdentifier(encounterPatientIdentifier);
        fHIREncounter.setSubject(encounterSubject);
        // participant (practitioner)
        String providerRegNo = GeneralUtil.getProviderRegNo(provider);
        List<Encounter.EncounterParticipantComponent> encounterParticipant = new ArrayList<>();
        Encounter.EncounterParticipantComponent participant = new Encounter.EncounterParticipantComponent();
        Reference participantIndividualRef = new Reference();
        participantIndividualRef.setReference("https://hwr.kenya-hie.health/api/v4/Practitioner/" + providerRegNo);
        Identifier participantIdentifier = new Identifier();
        participantIdentifier.setSystem("https://hwr.kenya-hie.health/api/v4/Practitioner");
        participantIdentifier.setValue(providerRegNo);
        participantIndividualRef.setIdentifier(participantIdentifier);
        participant.setIndividual(participantIndividualRef);
        encounterParticipant.add(participant);
        fHIREncounter.setParticipant(encounterParticipant);
        // Service Provider (location)
        String locationRegNo = GeneralUtil.getLocationRegNo();
        Reference encounterServiceProviderRef = new Reference();
        participantIndividualRef.setReference("https://fr.kenya-hie.health/api/v4/Organization/" + locationRegNo);
        Identifier locationIdentifier = new Identifier();
        locationIdentifier.setSystem("https://fr.kenya-hie.health/api/v4/Organization");
        locationIdentifier.setValue(locationRegNo);
        encounterServiceProviderRef.setIdentifier(locationIdentifier);
        fHIREncounter.setServiceProvider(encounterServiceProviderRef);

		ret.addEntry(createBundleEntry(fHIREncounter));

		// Add Patient to bundle
		org.hl7.fhir.r4.model.Patient fhirPatient = patientTranslator.toFhirResource(patient);;
		ret.addEntry(createBundleEntry(fhirPatient));

		// Add Organization to bundle
        Organization mainOrg = GeneralUtil.getSavedLocationFHIRPayload();

        if(mainOrg != null) {
            ret.addEntry(createBundleEntry(mainOrg));
        } else {
            Location location = encounter.getLocation();
            mainOrg = new Organization();
            mainOrg.setId(location.getUuid());
            mainOrg.setName(location.getName());
            mainOrg.addIdentifier(new Identifier().setValue(location.getUuid())); // Add UUID as identifier
            //		organization.setDescription(location.getDescription());
            mainOrg.addAddress(new Address()
                            .setCity(location.getCityVillage())
                            .setState(location.getStateProvince())
                            .setCountry(location.getCountry())
                            .setPostalCode(location.getPostalCode())
                    );
    //		organization.setName("Health Organization");
    //		organization.addIdentifier().setSystem("http://health.org/org").setValue("HOrg-123");
            ret.addEntry(createBundleEntry(mainOrg));
        }

		// Add Coverage to bundle
		Coverage coverage = new Coverage();
		coverage.addIdentifier().setSystem("http://health.org/coverage").setValue("Coverage/12345");
		coverage.setBeneficiary(new Reference(fhirPatient));
		coverage.setPayor(Collections.singletonList(new Reference(mainOrg)));
		UUID uuid = UUID.randomUUID();
		String uuidString = uuid.toString();
		coverage.setId(uuidString);
		coverage.setStatus(Coverage.CoverageStatus.ACTIVE);
		ret.addEntry(createBundleEntry(coverage));

		// Add Practitioner to bundle
        Practitioner mainProvider = GeneralUtil.getSavedProviderFHIRPayload(provider);

        if(mainProvider != null) {
            ret.addEntry(createBundleEntry(mainProvider));
        } else {
            Practitioner practitioner = practitionerTranslator.toFhirResource(provider);
            ProviderService providerService = Context.getService(ProviderService.class);

            // Add National ID to practitioner
            String NATIONAL_ID = "3d152c97-2293-4a2b-802e-e0f1009b7b15";
            ProviderAttributeType providerNationalIdAttributeType = providerService.getProviderAttributeTypeByUuid(NATIONAL_ID);
            for(ProviderAttribute providerAttribute : provider.getActiveAttributes()) {
                if(providerAttribute.getAttributeType().getUuid().equalsIgnoreCase(providerNationalIdAttributeType.getUuid())) {
                    String nationalId = providerAttribute.getValue().toString();
                    System.err.println("Insurance Module: Got provider national ID: " + nationalId);
                    Identifier providerIdentifier = new Identifier();
                    providerIdentifier.setUse(Identifier.IdentifierUse.OFFICIAL);
                    providerIdentifier.setValue(nationalId);
                    CodeableConcept provCode = new CodeableConcept();

                    Coding provCoding = new Coding();
                    provCoding.setSystem("http://hwr-kenyahie.health");
                    provCoding.setCode("national-id");

                    provCode.setCoding(Collections.singletonList(provCoding));
                    providerIdentifier.setType(provCode);

                    practitioner.addIdentifier(providerIdentifier);

                    break;
                }
            }

            // Add Doctors licence to practitioner
            String LICENSE_NUMBER = "bcaaa67b-cc72-4662-90c2-e1e992ceda66";
            ProviderAttributeType providerLicenseAttributeType = providerService.getProviderAttributeTypeByUuid(LICENSE_NUMBER);
            for(ProviderAttribute providerAttribute : provider.getActiveAttributes()) {
                if(providerAttribute.getAttributeType().getUuid().equalsIgnoreCase(providerLicenseAttributeType.getUuid())) {
                    String licenceNumber = providerAttribute.getValue().toString();
                    System.err.println("Insurance Module: Got provider licence number: " + licenceNumber);
                    Identifier providerIdentifier = new Identifier();
                    providerIdentifier.setUse(Identifier.IdentifierUse.OFFICIAL);
                    providerIdentifier.setValue(licenceNumber);
                    CodeableConcept provCode = new CodeableConcept();

                    Coding provCoding = new Coding();
                    provCoding.setSystem("https://shr.tiberbuapps.com/fhir");
                    provCoding.setCode("Board Registration Number");

                    provCode.setCoding(Collections.singletonList(provCoding));
                    providerIdentifier.setType(provCode);

                    practitioner.addIdentifier(providerIdentifier);

                    break;
                }
            }
            ret.addEntry(createBundleEntry(practitioner));
        }

		// Add Location to bundle
		org.hl7.fhir.r4.model.Location fhirLocation = locationTranslator.toFhirResource(encounter.getLocation());
		ret.addEntry(createBundleEntry(fhirLocation));

		return(ret);
	}

	/**
	 * Helper method to create BundleEntryComponent from a resource
	 * @param resource the FHIR resource to add
	 * @return a bundle entry
	 */
	private static Bundle.BundleEntryComponent createBundleEntry(Resource resource) {
		Bundle.BundleEntryComponent entry = new Bundle.BundleEntryComponent();
		entry.setResource(resource);
		String URL = resource.getResourceType().name() + "/" + resource.getId();
		entry.setFullUrl(URL);
		return entry;
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
        final String DATE_CREATED_URL = "http://fhir-es.transcendinsights.com/r4/StructureDefinition/resource-date-created";
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

        final String DATE_CREATED_URL = "http://fhir-es.transcendinsights.com/r4/StructureDefinition/resource-date-created";
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
        final String DATE_CREATED_URL = "http://fhir-es.transcendinsights.com/r4/StructureDefinition/resource-date-created";
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

	public PatientTranslator getPatientTranslator() {
		return patientTranslator;
	}

	public void setPatientTranslator(PatientTranslator patientTranslator) {
		this.patientTranslator = patientTranslator;
	}

	public PractitionerTranslator getPractitionerTranslator() {
		return practitionerTranslator;
	}

	public void setPractitionerTranslator(PractitionerTranslator practitionerTranslator) {
		this.practitionerTranslator = practitionerTranslator;
	}

	public EncounterTranslator<org.openmrs.Encounter> getEncounterTranslator() {
		return encounterTranslator;
	}

	public void setEncounterTranslator(
			EncounterTranslator<org.openmrs.Encounter> encounterTranslator) {
		this.encounterTranslator = encounterTranslator;
	}

	public LocationTranslator getLocationTranslator() {
		return locationTranslator;
	}

	public void setLocationTranslator(LocationTranslator locationTranslator) {
		this.locationTranslator = locationTranslator;
	}

	public PatientIdentifierTranslator getPatientIdentifierTranslator() {
		return patientIdentifierTranslator;
	}

	public void setPatientIdentifierTranslator(
			PatientIdentifierTranslator patientIdentifierTranslator) {
		this.patientIdentifierTranslator = patientIdentifierTranslator;
	}
}


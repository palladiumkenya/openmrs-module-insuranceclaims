package org.openmrs.module.insuranceclaims.api.service.fhir.impl;

import org.hl7.fhir.dstu3.model.codesystems.EligibilityrequestStatus;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Coverage;
import org.hl7.fhir.r4.model.CoverageEligibilityRequest;
import org.hl7.fhir.r4.model.CoverageEligibilityRequest.EligibilityRequestPurpose;
import org.hl7.fhir.r4.model.Enumeration;
import org.hl7.fhir.r4.model.CoverageEligibilityResponse;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Reference;
import org.openmrs.module.insuranceclaims.api.service.fhir.FHIREligibilityService;
import org.hl7.fhir.r4.model.CoverageEligibilityRequest.EligibilityRequestPurposeEnumFactory;

import static org.openmrs.module.insuranceclaims.api.service.fhir.util.IdentifierUtil.buildReference;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Bundle.BundleType;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Provenance;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.MessageHeader;
import org.hl7.fhir.r4.model.Resource;
import org.openmrs.module.fhir2.api.translators.PatientTranslator;
import org.openmrs.module.insuranceclaims.util.ReferencesUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.openmrs.Patient;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Date;
import java.util.List;

@Component("insuranceclaims.eligibilityservice")
public class FHIREligibilityServiceImpl implements FHIREligibilityService {

    public static final String PATIENT = "Patient";
    public static final String INSURER = "Insurer";

//    @Autowired
    private PatientTranslator patientTranslator;

    @Override
    public CoverageEligibilityRequest generateEligibilityRequest(String policyId) {
        CoverageEligibilityRequest request = new CoverageEligibilityRequest();
        Reference patient = buildReference(PATIENT, policyId);
        request.setPatient(patient);

        CodeableConcept priority = new CodeableConcept();

        Coding priorityCoding = new Coding();
        priorityCoding.setSystem("http://terminology.hl7.org/CodeSystem/processpriority");
        priorityCoding.setCode("stat");

        priority.setCoding(Collections.singletonList(priorityCoding));

        request.setPriority(priority);

        request.setStatus(CoverageEligibilityRequest.EligibilityRequestStatus.ACTIVE);

        request.setCreated(new Date());

        Reference insurer = buildReference(INSURER, "http://provider.com/Organization/SHA");
        request.setInsurer(insurer);

        Enumeration<EligibilityRequestPurpose> purposeItem = new Enumeration<>(new EligibilityRequestPurposeEnumFactory());
        purposeItem.setValue(EligibilityRequestPurpose.BENEFITS);

        List<Enumeration<EligibilityRequestPurpose>> purposes = Collections.singletonList(purposeItem);
        request.setPurpose(purposes);

        return request;
    }

    @Override
    public Bundle generateEligibilityRequestBundle(CoverageEligibilityRequest request, Patient patient) {
        Bundle ret = new Bundle();

        ret.setType(Bundle.BundleType.MESSAGE);
        ret.setTimestamp(new Date());

        // Add message header
        MessageHeader messageHeader = new MessageHeader();
        messageHeader.setEvent(new Coding().setSystem("http://hl7.org/fhir/message-events").setCode("eligibility-request"));
        messageHeader.setSource(new MessageHeader.MessageSourceComponent().setName("FHIR Client"));

        // Add CoverageEligibilityRequest to bundle
        ret.addEntry(createBundleEntry(request));

        //Add the patient
        org.hl7.fhir.r4.model.Patient fhirPatient = patientTranslator.toFhirResource(patient);;
        ret.addEntry(createBundleEntry(fhirPatient));

        //Add the organization
        Organization organization = new Organization();
        organization.setName("Health Organization");
        organization.addIdentifier().setSystem("http://health.org/org").setValue("HOrg-123");
        ret.addEntry(createBundleEntry(organization));

        //Add Coverage
        Coverage coverage = new Coverage();
        coverage.addIdentifier().setSystem("http://health.org/coverage").setValue("Coverage-123");
        coverage.setBeneficiary(new Reference(fhirPatient));
        coverage.setPayor(Collections.singletonList(new Reference(organization)));
        ret.addEntry(createBundleEntry(coverage));

        return ret;
    }

    /**
     * Helper method to create BundleEntryComponent from a resource
     * @param resource the FHIR resource to add
     * @return
     */
    private static BundleEntryComponent createBundleEntry(Resource resource) {
        BundleEntryComponent entry = new BundleEntryComponent();
        entry.setResource(resource);
        return entry;
    }

    public PatientTranslator getPatientTranslator() {
        return patientTranslator;
    }

    public void setPatientTranslator(PatientTranslator patientTranslator) {
        this.patientTranslator = patientTranslator;
    }
}

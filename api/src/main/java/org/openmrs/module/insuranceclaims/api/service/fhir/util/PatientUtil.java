package org.openmrs.module.insuranceclaims.api.service.fhir.util;

import org.apache.commons.lang3.math.NumberUtils;
// import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Identifier;

import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PersonName;
import org.openmrs.api.context.Context;
// import org.openmrs.module.fhir.api.util.FHIRConstants;
// import org.openmrs.module.fhir.api.util.FHIRUtils;
import org.openmrs.module.insuranceclaims.api.model.InsuranceClaim;

import java.util.List;

import static org.openmrs.module.insuranceclaims.api.service.fhir.util.InsuranceClaimConstants.PATIENT_EXTERNAL_ID_IDENTIFIER_UUID;

public final class PatientUtil {

    public static final String PATIENT = "Patient";
    public static final String PERSON = "person";
    public static final String IDENTIFIER = "Identifier";

    public Reference buildPatientReference(InsuranceClaim claim) {
        Patient patient = claim.getPatient();
        Reference patientReference = buildPatientOrPersonResourceReference(claim.getPatient());

        String patientId = patient.getActiveIdentifiers()
                .stream()
                .filter(c -> c.getIdentifierType().getUuid().equals(PATIENT_EXTERNAL_ID_IDENTIFIER_UUID))
                .findFirst()
                .map(PatientIdentifier::getIdentifier)
                .orElse(patient.getUuid());

        String reference = PATIENT + "/" + patientId;
        patientReference.setReference(reference);

        return patientReference;
    }

    public static boolean isSamePatient(Patient omrsPatient, org.hl7.fhir.r4.model.Patient fhirPatient) {
        String fhirGivenName = fhirPatient.getNameFirstRep().getGivenAsSingleString();
        String fhirFamilyName = fhirPatient.getNameFirstRep().getFamily();
        return omrsPatient.getGivenName().equals(fhirGivenName)
                && omrsPatient.getFamilyName().equals(fhirFamilyName)
                && omrsPatient.getBirthdate().compareTo(fhirPatient.getBirthDate()) == 0;
    }

    public static boolean isPatientInList(Patient omrsPatient, List<org.hl7.fhir.r4.model.Patient> fhirPatients) {
        boolean isInList = false;
        for (org.hl7.fhir.r4.model.Patient fhirPatient: fhirPatients) {
            isInList = isInList || isSamePatient(omrsPatient, fhirPatient);
        }
        return isInList;
    }


    public static Patient getPatientById(String personId) {
        if (NumberUtils.isNumber(personId)) {
            int parsedId = Integer.parseInt(personId);
            return Context.getPatientService().getPatient(parsedId);
        } else {
            return Context.getPatientService().getPatientByUuid(personId);
        }
    }

    public Reference buildPatientOrPersonResourceReference(org.openmrs.Person person) {
		Reference reference = new Reference();
		PersonName name = person.getPersonName();
		StringBuilder nameDisplay = new StringBuilder();
		nameDisplay.append(name.getGivenName());
		nameDisplay.append(" ");
		nameDisplay.append(name.getFamilyName());
		String uri;
		if (Context.getPatientService().getPatientByUuid(person.getUuid()) != null) {
			nameDisplay.append("(");
			nameDisplay.append(IDENTIFIER);
			nameDisplay.append(":");
			nameDisplay.append(Context.getPatientService().getPatientByUuid(person.getUuid())
					.getPatientIdentifier()
					.getIdentifier());
			nameDisplay.append(")");
			uri = PATIENT + "/" + person.getUuid();
		} else {
			uri = PERSON + "/" + person.getUuid();
		}
		reference.setDisplay(nameDisplay.toString());
		reference.setReference(uri);
		reference.setId(person.getUuid());
		Identifier identifier = new Identifier();
		identifier.setId(person.getUuid());
		reference.setIdentifier(identifier);
		return reference;
	}

    public PatientUtil() {}
}

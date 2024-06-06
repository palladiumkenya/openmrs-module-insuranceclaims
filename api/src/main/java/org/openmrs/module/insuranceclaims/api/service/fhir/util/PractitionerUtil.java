package org.openmrs.module.insuranceclaims.api.service.fhir.util;

import org.openmrs.Provider;
import org.openmrs.ProviderAttribute;
import org.openmrs.module.insuranceclaims.api.model.InsuranceClaim;

//FHIR 2
import org.hl7.fhir.r4.model.Reference;
import org.openmrs.Provider;
import org.hl7.fhir.r4.model.Practitioner;
import org.openmrs.api.context.Context;
import org.openmrs.module.fhir2.api.translators.PractitionerTranslator;

import static org.openmrs.module.insuranceclaims.api.service.fhir.util.InsuranceClaimConstants.PROVIDER_EXTERNAL_ID_ATTRIBUTE_UUID;

public final class PractitionerUtil {

    private final PractitionerTranslator<Provider> practitionerTranslator;
    public static final String PRACTITIONER = "Practitioner";


    public Reference buildPractitionerReference(InsuranceClaim claim) {
        Provider provider = claim.getProvider();
        // Reference pracitionerReference = FHIRUtils.buildPractitionerReference(claim.getProvider());
        Reference practitionerReference = createPractitionerReference(claim.getProvider());


        String providerId = provider.getActiveAttributes()
                .stream()
                .filter(c -> c.getAttributeType().getUuid().equals(PROVIDER_EXTERNAL_ID_ATTRIBUTE_UUID))
                .findFirst()
                .map(ProviderAttribute::getValueReference)
                .orElse(provider.getUuid());

        String reference = PRACTITIONER + "/" + providerId;

        practitionerReference.setReference(reference);

        return practitionerReference;
    }

    public PractitionerUtil() {
        this.practitionerTranslator = Context.getRegisteredComponent("fhirPractitionerTranslator", PractitionerTranslator.class);
    }

    public Reference createPractitionerReference(Provider provider) {
        // Translate the OpenMRS provider to a FHIR Practitioner and create a reference
        Practitioner fhirPractitioner = practitionerTranslator.toFhirResource(provider);
        return new Reference(fhirPractitioner.getIdElement().getValue());
    }
}

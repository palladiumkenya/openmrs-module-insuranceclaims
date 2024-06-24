package org.openmrs.module.insuranceclaims.api.service.fhir.util;

import org.hl7.fhir.r4.model.Claim;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.exceptions.FHIRException;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static org.openmrs.module.insuranceclaims.api.service.fhir.util.IdentifierUtil.getUnambiguousElement;

public final class SpecialComponentUtil {

    public static String getSpecialConditionComponentFromCategory(Claim claim, String category) throws FHIRException {
        List<Claim.SupportingInformationComponent> information =
                getConditionsByCategory(claim.getSupportingInfo(), category);

        Claim.SupportingInformationComponent component = getUnambiguousElement(information);
        return component != null ? component.getValueStringType().getValue() : null;
    }

    public static String getSpecialConditionComponentBySequenceNumber(Claim claim, int sequenceId) throws FHIRException {
        List<Claim.SupportingInformationComponent> information = claim.getSupportingInfo();
        Claim.SupportingInformationComponent requested = information.stream()
                .filter(c -> c.getSequence() == sequenceId)
                .findFirst()
                .orElse(null);

        return requested == null ? null : requested.getValueStringType().getValue();
    }

    public static Claim.SupportingInformationComponent createSpecialComponent(String value, String categoryName) {
        Claim.SupportingInformationComponent information = new Claim.SupportingInformationComponent();
        CodeableConcept category = new CodeableConcept();

        category.setText(categoryName);
        // Create instance of Random class
        Random random = new Random();
        // Generate random number within range
        int randomNum = random.nextInt((9999999 - 1000000) + 1) + 1000000;
        information.setSequence(randomNum);
        information.setValue(new StringType(value));
        information.setCategory(category);
        return information;
    }

    private static List<Claim.SupportingInformationComponent> getConditionsByCategory(List<Claim.SupportingInformationComponent> componentList, String category) {

        return componentList.stream()
                .filter(c -> c.getCategory().getText() != null)
                .filter(c -> c.getCategory().getText().equals(category))
                .collect(Collectors.toList());
    }

    private SpecialComponentUtil() {}
}


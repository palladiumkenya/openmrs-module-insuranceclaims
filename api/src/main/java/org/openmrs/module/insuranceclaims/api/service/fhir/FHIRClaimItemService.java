package org.openmrs.module.insuranceclaims.api.service.fhir;

import org.hl7.fhir.r4.model.Claim;
import org.hl7.fhir.r4.model.ClaimResponse;
import org.hl7.fhir.exceptions.FHIRException;
import org.openmrs.module.insuranceclaims.api.model.InsuranceClaim;
import org.openmrs.module.insuranceclaims.api.model.InsuranceClaimIntervention;
import org.openmrs.module.insuranceclaims.api.model.InsuranceClaimItem;

import java.util.List;

public interface FHIRClaimItemService {
    Claim assignItemsWithInformationToClaim(Claim fhirClaim, InsuranceClaim claim);

    List<Claim.ItemComponent> generateClaimItemComponent(InsuranceClaim claim);

//    List<Claim.ItemComponent> generateClaimItemComponent(List<InsuranceClaimItem> items);

    List<Claim.ItemComponent> generateClaimItemComponent(List<InsuranceClaimIntervention> interventions);

    List<InsuranceClaimItem> generateOmrsClaimItems(Claim claim, List<String> error);

    List<ClaimResponse.ItemComponent> generateClaimResponseItemComponent(InsuranceClaim claim);

    List<InsuranceClaimItem> generateOmrsClaimResponseItems(ClaimResponse claim, List<String> error) throws FHIRException;

    List<ClaimResponse.NoteComponent> generateClaimResponseNotes(InsuranceClaim claim);
}

package org.openmrs.module.insuranceclaims.api.service.fhir.impl;

import static org.openmrs.module.insuranceclaims.api.service.fhir.util.ClaimResponseUtil.getProcessNote;
import static org.openmrs.module.insuranceclaims.api.service.fhir.util.IdentifierUtil.getUnambiguousElement;
import static org.openmrs.module.insuranceclaims.api.service.fhir.util.InsuranceClaimConstants.EXTERNAL_SYSTEM_CODE_SOURCE_MAPPING_NAME;
import static org.openmrs.module.insuranceclaims.api.service.fhir.util.InsuranceClaimConstants.ITEM_ADJUDICATION_GENERAL_CATEGORY;
import static org.openmrs.module.insuranceclaims.api.service.fhir.util.InsuranceClaimConstants.ITEM_ADJUDICATION_REJECTION_REASON_CATEGORY;
import static org.openmrs.module.insuranceclaims.api.service.fhir.util.InsuranceClaimConstants.ITEM_EXPLANATION_CATEGORY;
import static org.openmrs.module.insuranceclaims.api.service.fhir.util.InsuranceClaimConstants.NEXT_SEQUENCE;
import static org.openmrs.module.insuranceclaims.api.service.fhir.util.InsuranceClaimConstants.SEQUENCE_FIRST;
import static org.openmrs.module.insuranceclaims.api.service.fhir.util.InsuranceClaimItemUtil.createFhirItemService;
import static org.openmrs.module.insuranceclaims.api.service.fhir.util.InsuranceClaimItemUtil.createItemGeneralAdjudication;
import static org.openmrs.module.insuranceclaims.api.service.fhir.util.InsuranceClaimItemUtil.createRejectionReasonAdjudication;
import static org.openmrs.module.insuranceclaims.api.service.fhir.util.InsuranceClaimItemUtil.getAdjudicationRejectionReason;
import static org.openmrs.module.insuranceclaims.api.service.fhir.util.InsuranceClaimItemUtil.getAdjudicationStatus;
import static org.openmrs.module.insuranceclaims.api.service.fhir.util.InsuranceClaimItemUtil.getItemCategory;
import static org.openmrs.module.insuranceclaims.api.service.fhir.util.InsuranceClaimItemUtil.getItemCodeBySequence;
import static org.openmrs.module.insuranceclaims.api.service.fhir.util.InsuranceClaimItemUtil.getItemQuantity;
import static org.openmrs.module.insuranceclaims.api.service.fhir.util.InsuranceClaimItemUtil.getItemUnitPrice;
import static org.openmrs.module.insuranceclaims.api.service.fhir.util.SpecialComponentUtil.getSpecialConditionComponentBySequenceNumber;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.r4.model.Claim;
import org.hl7.fhir.r4.model.ClaimResponse;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Money;
import org.hl7.fhir.r4.model.PositiveIntType;
import org.hl7.fhir.r4.model.StringType;
import org.openmrs.Concept;
import org.openmrs.api.context.Context;
import org.openmrs.module.insuranceclaims.api.model.InsuranceClaim;
import org.openmrs.module.insuranceclaims.api.model.InsuranceClaimIntervention;
import org.openmrs.module.insuranceclaims.api.model.InsuranceClaimItem;
import org.openmrs.module.insuranceclaims.api.model.ProvidedItem;
import org.openmrs.module.insuranceclaims.api.service.db.InterventionDbService;
import org.openmrs.module.insuranceclaims.api.service.db.ItemDbService;
import org.openmrs.module.insuranceclaims.api.service.fhir.FHIRClaimItemService;

public class FHIRClaimItemServiceImpl implements FHIRClaimItemService {

    private ItemDbService itemDbService;
    private InterventionDbService interventionDbService;

//    @Override
//    public Claim assignItemsWithInformationToClaim(Claim fhirClaim, InsuranceClaim claim) {
//        List<InsuranceClaimItem> insuranceClaimItems = itemDbService.findInsuranceClaimItems(claim.getId());
//        List<Claim.ItemComponent> fhirItems = generateClaimItemComponent(insuranceClaimItems);
//
//        fhirClaim.setItem(fhirItems);
//        int initialNumberOnClaimInformation = fhirClaim.getSupportingInfo().size();
//        for (int itemIndex = 0; itemIndex < insuranceClaimItems.size(); itemIndex++) {
//            InsuranceClaimItem nextMrsItem = insuranceClaimItems.get(itemIndex);
//            Claim.ItemComponent correspondingFhirItem = fhirItems.get(itemIndex);
//            Claim.SupportingInformationComponent itemInformation = createItemExplanationInformation(nextMrsItem);
//
//            itemInformation.setSequence(++initialNumberOnClaimInformation);
//            fhirClaim.addSupportingInfo(itemInformation);
//            correspondingFhirItem.setInformationSequence(Collections.singletonList(itemInformation.getSequenceElement()));
//        }
//
//        return fhirClaim;
//    }

    @Override
    public Claim assignItemsWithInformationToClaim(Claim fhirClaim, InsuranceClaim claim) {
        List<InsuranceClaimIntervention> insuranceClaimInterventions = interventionDbService.findInsuranceClaimIntervention(claim.getId());
        List<Claim.ItemComponent> fhirItems = generateClaimItemComponent(insuranceClaimInterventions);

        fhirClaim.setItem(fhirItems);
        return fhirClaim;
    }

//    @Override
//    public List<Claim.ItemComponent> generateClaimItemComponent(InsuranceClaim claim) {
//        List<InsuranceClaimItem> insuranceClaimItems = itemDbService.findInsuranceClaimItems(claim.getId());
//        return generateClaimItemComponent(insuranceClaimItems);
//    }

    @Override
    public List<Claim.ItemComponent> generateClaimItemComponent(InsuranceClaim claim) {
        List<InsuranceClaimIntervention> insuranceClaimInterventions = interventionDbService.findInsuranceClaimIntervention(claim.getId());
        return generateClaimItemComponent(insuranceClaimInterventions);
    }

//    @Override
//    public List<Claim.ItemComponent> generateClaimItemComponent(List<InsuranceClaimItem> insuranceClaimItems) {
//        List<Claim.ItemComponent> newItemComponents = new ArrayList<>();
//        int counter = 1;
//        for (InsuranceClaimItem item: insuranceClaimItems) {
//            Claim.ItemComponent next = new Claim.ItemComponent();
//
//            // Set the sequence
//            next.setSequence(counter);
//            counter++;
//            // Set product or service
//            // Create a CodeableConcept for the product or service
//                CodeableConcept productOrService = new CodeableConcept();
//
//                // Create a Coding for the product or service
//                Coding coding = new Coding();
//
//                // Set the system, code, and display values for the product or service
//                coding.setSystem("http://snomed.info/sct"); // Adjust system URL as necessary
//                coding.setCode("303646003"); // Example code, adjust as necessary
//                coding.setDisplay("Application of wound dressing"); // Example display text, adjust as necessary
//
//                // Add the Coding to the CodeableConcept
//                productOrService.addCoding(coding);
//
//                // Optionally, set the text value
//                productOrService.setText("Wound Dressing Service");
//            next.setProductOrService(productOrService);
//            next.setCategory(getItemCategory(item));
//            next.setQuantity(getItemQuantity(item));
//            next.setUnitPrice(getItemUnitPrice(item));
//            // next.setProductOrService(createFhirItemService(item));
//            newItemComponents.add(next);
//        }
//        return newItemComponents;
//    }

    @Override
    public List<Claim.ItemComponent> generateClaimItemComponent(List<InsuranceClaimIntervention> insuranceClaimInterventions) {
        List<Claim.ItemComponent> newItemComponents = new ArrayList<>();
        int counter = 1;
        for (InsuranceClaimIntervention item: insuranceClaimInterventions) {
            Claim.ItemComponent next = new Claim.ItemComponent();

            // Set the sequence
            next.setSequence(counter);
            counter++;
            // Set product or service
            // Create a CodeableConcept for the product or service
            CodeableConcept productOrService = new CodeableConcept();

            // Create a Coding for the product or service
            Coding coding = new Coding();

            // Set the system, code, and display values for the product or service
            coding.setSystem("https://mis.apeiro-digital.com/fhir/CodeSystem/intervention-codes");
            coding.setCode(item.getName());
            coding.setDisplay(item.getName());

            // Add the Coding to the CodeableConcept
            productOrService.addCoding(coding);

            // Optionally, set the text value
            productOrService.setText("intervention");
            next.setProductOrService(productOrService);
//            next.setCategory(getItemCategory(item));
//            next.setQuantity(getItemQuantity(item));
//            next.setUnitPrice(getItemUnitPrice(item));
            // next.setProductOrService(createFhirItemService(item));
            newItemComponents.add(next);
        }
        return newItemComponents;
    }

    @Override
    public List<InsuranceClaimItem> generateOmrsClaimItems(Claim claim, List<String> error) {
        List<Claim.ItemComponent> items = claim.getItem();
        List<InsuranceClaimItem> insuranceClaimItems = new ArrayList<>();

        for (Claim.ItemComponent component: items) {
            try {
                InsuranceClaimItem item = generateOmrsClaimItem(component);
                String linkedExplanation = getLinkedInformation(claim, getItemComponentInformationLinkId(component));
                item.setExplanation(linkedExplanation);
                insuranceClaimItems.add(item);
            } catch (FHIRException e) {
                error.add("Could not found explanation linked to item with code "
                        + component.getProductOrService().getText());
            }

        }
        return insuranceClaimItems;
    }

    @Override
    public List<ClaimResponse.ItemComponent> generateClaimResponseItemComponent(InsuranceClaim claim) {
        List<ClaimResponse.ItemComponent> items = new ArrayList<>();
        List<InsuranceClaimItem> insuranceClaimItems = itemDbService.findInsuranceClaimItems(claim.getId());

        int sequence = SEQUENCE_FIRST;
        for (InsuranceClaimItem insuranceClaimItem: insuranceClaimItems) {
            ClaimResponse.ItemComponent nextItem = new ClaimResponse.ItemComponent();
            //General
            nextItem.addAdjudication(createItemGeneralAdjudication(insuranceClaimItem));
            //Rejection Reason
            nextItem.addAdjudication(createRejectionReasonAdjudication(insuranceClaimItem));

            nextItem.setItemSequence(sequence);
            nextItem.addNoteNumber(sequence);
            sequence += NEXT_SEQUENCE;

            items.add(nextItem);
        }
        return items;
    }

    @Override
    public List<InsuranceClaimItem> generateOmrsClaimResponseItems(ClaimResponse claim, List<String> error) {
        List<InsuranceClaimItem> omrsItems = new ArrayList<>();
        for (ClaimResponse.ItemComponent item: claim.getItem()) {
            InsuranceClaimItem nextItem = new InsuranceClaimItem();
            try {
                //Item
                List<String> itemCodes = getItemCodeBySequence(claim, item.getItemSequence());
                nextItem.setItem(generateProvidedItem(itemCodes));
            } catch (FHIRException exception) {
                error.add(exception.getMessage());
            }
            //Adjudication
            for (ClaimResponse.AdjudicationComponent adjudicationComponent: item.getAdjudication()) {
                CodeableConcept adjudicationCategoryCoding = adjudicationComponent.getCategory();
                String adjudicationCode = adjudicationCategoryCoding.getText();
                if (adjudicationCode.equals(ITEM_ADJUDICATION_GENERAL_CATEGORY)) {
                    nextItem.setQuantityApproved(getAdjudicationQuantityApproved(adjudicationComponent));
                    nextItem.setPriceApproved(getAdjudicationPriceApproved(adjudicationComponent));
                    nextItem.setStatus(getAdjudicationStatus(adjudicationComponent));
                } else {
                    if (adjudicationCode.equals(ITEM_ADJUDICATION_REJECTION_REASON_CATEGORY)) {
                        String reason = getAdjudicationRejectionReason(adjudicationComponent);
                        nextItem.setRejectionReason(StringUtils.isNotEmpty(reason) ? reason : "None");
                    } else {
                        error.add("Cound not found strategy for adjudication code " + adjudicationCode);
                    }
                }
            }
            //Justification
            nextItem.setJustification(getProcessNote(claim, getFirstItemNoteNumber(item)));

            omrsItems.add(nextItem);
        }

        return omrsItems;
    }

    @Override
    public List<ClaimResponse.NoteComponent> generateClaimResponseNotes(InsuranceClaim claim)  {
        List<ClaimResponse.NoteComponent> claimNotes = new ArrayList<>();
        List<InsuranceClaimItem> items = itemDbService.findInsuranceClaimItems(claim.getId());
        int noteNumber = SEQUENCE_FIRST;
        for (InsuranceClaimItem item: items) {
            ClaimResponse.NoteComponent nextNote = new ClaimResponse.NoteComponent();
            nextNote.setText(item.getJustification());
            nextNote.setNumber(noteNumber);

            claimNotes.add(nextNote);
            noteNumber += NEXT_SEQUENCE;
        }
        return claimNotes;
    }

    public void setItemDbService(ItemDbService insuranceClaimItemDao) {
        this.itemDbService = insuranceClaimItemDao;
    }

    private ProvidedItem generateProvidedItem(List<String> itemCodes) {
        ProvidedItem providedItem = new ProvidedItem();
        providedItem.setItem(getConceptByExternalId(itemCodes));
        return providedItem;
    }

    private Concept getConceptByExternalId(List<String> itemCodes) {
        List<Concept> conceptList = itemCodes.stream()
                .map(code -> Context.getConceptService().getConceptByMapping(code, EXTERNAL_SYSTEM_CODE_SOURCE_MAPPING_NAME))
                .collect(Collectors.toList());
        return getUnambiguousElement(conceptList);
    }

    private InsuranceClaimItem generateOmrsClaimItem(Claim.ItemComponent item) throws FHIRException {
        InsuranceClaimItem omrsItem = new InsuranceClaimItem();
        String itemCode = item.getProductOrService().getText();
        ProvidedItem providedItem = generateProvidedItem(Collections.singletonList(itemCode));
        omrsItem.setQuantityProvided(getItemQuantity(item));
        omrsItem.setItem(providedItem);
        if (providedItem.getItem() == null) {
            throw new FHIRException("Could not find object related to code" + itemCode);
        }
        return omrsItem;
    }

    private String getLinkedInformation(Claim claim, Integer informationSequenceId) throws FHIRException {
        return informationSequenceId == null ? null : getSpecialConditionComponentBySequenceNumber(claim, informationSequenceId);
    }

    private Integer getItemComponentInformationLinkId(Claim.ItemComponent item) {
       return CollectionUtils.isEmpty(item.getInformationSequence()) ?
               null : getUnambiguousElement(item.getInformationSequence()).getValue();
    }

    private Integer getFirstItemNoteNumber(ClaimResponse.ItemComponent item) {
        PositiveIntType note = getUnambiguousElement(item.getNoteNumber());
        return note != null ? note.getValue() : null;
    }

    private Integer getAdjudicationQuantityApproved(ClaimResponse.AdjudicationComponent component) {
        BigDecimal approved = component.getValue();
        return approved != null ? approved.intValue() : null;
    }

    private BigDecimal getAdjudicationPriceApproved(ClaimResponse.AdjudicationComponent component) {
        Money approved = component.getAmount();
        return approved != null ? approved.getValue() : null;
    }

    private Claim.SupportingInformationComponent createItemExplanationInformation(InsuranceClaimItem item) {
        Claim.SupportingInformationComponent itemInformation = new Claim.SupportingInformationComponent();

        itemInformation.setCategory(new CodeableConcept().setText(ITEM_EXPLANATION_CATEGORY));
        itemInformation.setValue(new StringType(item.getExplanation()));

        return itemInformation;
    }

    public InterventionDbService getInterventionDbService() {
        return interventionDbService;
    }

    public void setInterventionDbService(
            InterventionDbService interventionDbService) {
        this.interventionDbService = interventionDbService;
    }
}

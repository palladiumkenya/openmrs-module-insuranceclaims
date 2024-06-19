package org.openmrs.module.insuranceclaims;

import org.openmrs.Concept;
import org.openmrs.module.insuranceclaims.api.model.InsuranceClaimItem;
import org.openmrs.module.kenyaemr.cashier.api.model.BillLineItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class ClaimUtils {

    public static Map<String, List<InsuranceClaimItem>> getInsuranceClaimItemsAsMap(List<InsuranceClaimItem> items) {
        HashMap<String, List<InsuranceClaimItem>> itemMapping = new HashMap<>();
        for (InsuranceClaimItem item: items) {
            String name = buildItemName(item.getItem());
            itemMapping.computeIfAbsent(name, k -> new ArrayList<>());
            itemMapping.get(name).add(item);
        }
        return itemMapping;
    }

    public static Map<String, List<BillLineItem>> getProvidedItemsAsMap(List<BillLineItem> items) {
        Map<String, List<BillLineItem>> itemMapping = new HashMap<>();
        for (BillLineItem item: items) {
            String name = buildItemName(item);
            itemMapping.computeIfAbsent(name, k -> new ArrayList<>());
            itemMapping.get(name).add(item);
        }
        return itemMapping;
    }

    public static String buildItemName(BillLineItem item) {
        String name = item.getItem() != null ? getConceptName(item.getItem().getConcept()) : null;
        if (name != null) {
            name = buildKnownProvidedItemName(item);
        } else {
            name = "Unknown item " + item.hashCode();
        }
        return name;
    }

    private static String buildKnownProvidedItemName(BillLineItem item) {
        String name = getConceptName(item.getItem().getConcept());
        String attributes =  concatProvidedItemAttributes(item);
        return name + "(" + attributes + ")";
    }

    private static String concatProvidedItemAttributes(BillLineItem item) {
        // return item.getItem().getActiveAttributes().stream().map(
        //         attr -> attr.getValue().toString()).collect(Collectors.joining(", "));
        return("");
    }

    private static String getConceptName(Concept concept) {
        return concept != null ?
                concept.getName().toString()
                : null;
    }

    private ClaimUtils() {}
}

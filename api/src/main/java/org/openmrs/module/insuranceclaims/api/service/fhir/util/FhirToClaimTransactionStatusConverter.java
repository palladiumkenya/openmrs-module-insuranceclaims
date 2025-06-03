package org.openmrs.module.insuranceclaims.api.service.fhir.util;

import org.openmrs.module.insuranceclaims.api.model.ClaimTransactionStatus;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.Instant;
import java.time.format.DateTimeParseException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Utility class to convert FHIR Bundle JSON to ClaimTransactionStatus.
 * This class parses the FHIR Bundle, extracts Claim and ClaimResponse
 * resources,
 * and maps relevant fields to the ClaimTransactionStatus model.
 */
public class FhirToClaimTransactionStatusConverter {
    private static final Log log = LogFactory.getLog(FhirToClaimTransactionStatusConverter.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ClaimTransactionStatus convertFhirBundleToClaimStatus(String fhirBundleJson) throws Exception {
        JsonNode bundle = objectMapper.readTree(fhirBundleJson);
        JsonNode entries = bundle.get("entry");

        if (entries == null || !entries.isArray()) {
            throw new IllegalArgumentException("--> Invalid FHIR Bundle: no entries found");
        }

        log.info("--> Found " + entries.size() + " entries in bundle");

        JsonNode claimResource = null;
        JsonNode claimResponseResource = null;
        JsonNode taskResource = null;

        // Find Claim and ClaimResponse resources
        for (JsonNode entry : entries) {
            JsonNode resource = entry.get("resource");
            if (resource == null || !resource.has("resourceType")) {
                continue;
            }

            String resourceType = resource.get("resourceType").asText();
            log.info("--> Found resource type: " + resourceType);

            if ("Claim".equals(resourceType)) {
                claimResource = resource;
                log.info("--> Found Claim resource with ID: " + resource.get("id").asText());
            } else if ("ClaimResponse".equals(resourceType)) {
                claimResponseResource = resource;
                log.info("--> Found ClaimResponse resource with ID: " + resource.get("id").asText());
            } else if ("Task".equals(resourceType)) {
                taskResource = resource;
                log.info("--> Found Task resource with ID: " + resource.get("id").asText());
            }
        }

        // If ClaimResponse is not found, try to get status from Task resource
        if (claimResource == null) {
            throw new IllegalArgumentException("Required Claim resource not found");
        }

        ClaimTransactionStatus status = new ClaimTransactionStatus();

        // Map fields from FHIR to ClaimTransactionStatus
        status.setId(1); // Set a default ID or handle as needed
        status.setClaimId(claimResource.get("id").asText());

        // Get approved amount from claim total
        JsonNode totalNode = claimResource.get("total");
        if (totalNode != null && totalNode.has("value")) {
            status.setApprovedAmount((int) totalNode.get("value").asDouble());
        } else {
            status.setApprovedAmount(0);
        }

        // Get status - prioritize Task resource output, then ClaimResponse outcome,
        // then claim-state-extension
        String statusValue = "";

        // First, try to get status from Task resource output
        if (taskResource != null) {
            statusValue = extractStatusFromTaskOutput(taskResource);
            if (!statusValue.isEmpty()) {
                log.info("--> Using Task resource output status: " + statusValue);
            }
        }

        // If no status from Task, try ClaimResponse
        if (statusValue.isEmpty() && claimResponseResource != null && claimResponseResource.has("outcome")) {
            statusValue = claimResponseResource.get("outcome").asText();
            log.info("--> Using ClaimResponse outcome: " + statusValue);
        } else if (statusValue.isEmpty() && claimResponseResource != null && claimResponseResource.has("extension")) {
            // Check for claim-state-extension as fallback
            JsonNode extensions = claimResponseResource.get("extension");
            for (JsonNode ext : extensions) {
                String url = ext.has("url") ? ext.get("url").asText() : "";
                if (url.contains("claim-state-extension") && ext.has("valueCodeableConcept")) {
                    JsonNode coding = ext.get("valueCodeableConcept").get("coding");
                    if (coding.isArray() && coding.size() > 0) {
                        statusValue = coding.get(0).get("code").asText();
                        log.info("--> Using ClaimResponse extension status: " + statusValue);
                        break;
                    }
                }
            }
        }

        if (statusValue.isEmpty()) {
            log.info("--> No status found in Task, ClaimResponse, or extensions, using default: unknown");
            statusValue = "unknown";
        }

        status.setStatus(statusValue);

        // Set null values for fields not present in FHIR
        status.setCommentFromApprover(null);
        status.setNotes(null);

        // Convert created date from Claim
        String createdDateStr = claimResource.get("created").asText();
        Timestamp createdDate = parseDateTime(createdDateStr);
        status.setCreatedAt(createdDate);
        status.setUpdatedAt(createdDate);
        return status;
    }

    private String extractStatusFromTaskOutput(JsonNode taskResource) {
        if (!taskResource.has("output") || !taskResource.get("output").isArray()) {
            log.info("--> Task resource has no output array");
            return "";
        }

        JsonNode outputArray = taskResource.get("output");
        String lastClaimStateDisplay = "";

        // Iterate through output array to find the last entry with claim-state system
        for (JsonNode output : outputArray) {
            if (output.has("valueCodeableConcept")) {
                JsonNode valueCodeableConcept = output.get("valueCodeableConcept");
                if (valueCodeableConcept.has("coding") && valueCodeableConcept.get("coding").isArray()) {
                    JsonNode codingArray = valueCodeableConcept.get("coding");

                    for (JsonNode coding : codingArray) {
                        if (coding.has("system") && coding.has("display")) {
                            String system = coding.get("system").asText();
                            if (system.contains("claim-state")) {
                                lastClaimStateDisplay = coding.get("display").asText();
                                log.info("--> Found claim-state in Task output with display: " + lastClaimStateDisplay);
                            }
                        }
                    }
                }
            }
        }
        System.out.println("--> Last claim state display: " + lastClaimStateDisplay);

        return lastClaimStateDisplay;
    }

    private Timestamp parseDateTime(String dateTimeStr) {
        try {
            OffsetDateTime offsetDateTime = OffsetDateTime.parse(dateTimeStr);

            // Convert to Instant (UTC) then to Timestamp
            Instant instant = offsetDateTime.toInstant();
            return Timestamp.from(instant);

        } catch (DateTimeParseException e) {
            try {
                LocalDateTime localDateTime = LocalDateTime.parse(dateTimeStr);
                return Timestamp.valueOf(localDateTime);
            } catch (DateTimeParseException ex) {
                throw new IllegalArgumentException("Unable to parse date: " + dateTimeStr, ex);
            }
        }
    }
}
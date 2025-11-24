package org.openmrs.module.insuranceclaims.api.service.fhir.util;

import org.openmrs.module.insuranceclaims.api.model.ClaimTransactionStatus;
import org.openmrs.module.insuranceclaims.util.ClaimsUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hl7.fhir.r4.model.ClaimResponse;
import org.hl7.fhir.r4.model.ClaimResponse.NoteComponent;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.Task;
import org.hl7.fhir.r4.model.Type;

import java.sql.Timestamp;

/**
 * Utility class to convert FHIR Bundle JSON to ClaimTransactionStatus.
 * This class parses the FHIR Bundle, extracts Claim and ClaimResponse
 * resources,
 * and maps relevant fields to the ClaimTransactionStatus model.
 */
public class FhirToClaimTransactionStatusConverter {
    private static final Log log = LogFactory.getLog(FhirToClaimTransactionStatusConverter.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ClaimTransactionStatus convertFhirOperationOutputToClaimStatus(String fhirJson) throws Exception {
        ClaimTransactionStatus status = new ClaimTransactionStatus();

        OperationOutcome operationOutcome = ClaimsUtils.getOperationOutcomeFromJson(fhirJson);
        if(operationOutcome != null) {
            status.setId(1); // Set a default ID or handle as needed
            // status.setClaimId(claimResource.get("id").asText());
            // status.setStatus(statusValue);

            // // Set null values for fields not present in FHIR
            // status.setCommentFromApprover(null);
            // status.setNotes(null);

            // // Convert created date from Claim
            // String createdDateStr = claimResource.get("created").asText();
            // Timestamp createdDate = parseDateTime(createdDateStr);
            // status.setCreatedAt(createdDate);
            // status.setUpdatedAt(createdDate);
            status.setApprovedAmount(0);
        }

        return(status);
    }

    /**
     * Extract the claim status from the response FHIR bundle
     * @param fhirJson
     * @return
     * @throws Exception
     */
    public ClaimTransactionStatus convertFhirBundleToClaimStatus(String fhirJson) throws Exception {
        // NB: Response can be a Bundle, OperationOutcome or Generic error
        System.err.println("Insurance Claims: Starting claim processing");
        ClaimTransactionStatus status = new ClaimTransactionStatus();

        JsonNode response = objectMapper.readTree(fhirJson);
        JsonNode error = response.get("errorResponse");
        JsonNode resType = response.get("resourceType");
        JsonNode entries = response.get("entry");
		
        if (error != null && error.isObject()) {
            System.err.println("Insurance Claims: Claim Status: Got error response");
            JsonNode errorMesg = error.get("message");
            String msg = (errorMesg != null && errorMesg.isTextual() && errorMesg.textValue() != null) ? errorMesg.textValue() : "General Error";
            throw new IllegalArgumentException("Insurance Claims: ERROR: " + msg);
        } else if(resType != null && resType.isTextual() && resType.textValue() != null && resType.textValue().trim().equalsIgnoreCase("OperationOutcome")) {
            // The pro way
            System.err.println("Insurance Claims: Claim Status: Got an issue in a OperationOutcome");
            OperationOutcome operationOutcome = ClaimsUtils.getOperationOutcomeFromJson(fhirJson);
            String issues = ClaimsUtils.operationOutcomeIssuesToString(operationOutcome.getIssue());
            throw new IllegalArgumentException("Insurance Claims: ERROR: " + issues);
        } else if (resType != null && resType.isTextual() && resType.textValue() != null && resType.textValue().trim().equalsIgnoreCase("Bundle") && entries != null && entries.isArray()) {
            // We have a Bundle
            System.err.println("Insurance Claims: Claim Status: Found " + entries.size() + " entries in response Bundle");

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
                System.err.println("Insurance Claims: Found resource type: " + resourceType);

                if ("Claim".equals(resourceType)) {
                    claimResource = resource;
                    System.err.println("Insurance Claims: Found Claim resource with ID: " + resource.get("id").asText());
                } else if ("ClaimResponse".equals(resourceType)) {
                    claimResponseResource = resource;
                    System.err.println("Insurance Claims: Found ClaimResponse resource with ID: " + resource.get("id").asText());
                } else if ("Task".equals(resourceType)) {
                    taskResource = resource;
                    System.err.println("Insurance Claims: Found Task resource with ID: " + resource.get("id").asText());
                }
            }

            // Map fields from FHIR to ClaimTransactionStatus
            status.setId(1); // Set a default ID or handle as needed
            if (claimResource != null) {
                status.setClaimId(claimResource.get("id").asText());
            } else if(claimResponseResource != null) {
                status.setClaimId(claimResponseResource.get("id").asText());
            } else if(taskResource != null) {
                status.setClaimId(taskResource.get("id").asText());
            }

            // Get approved amount from claim total
            if (claimResource != null) {
                JsonNode totalNode = claimResource.get("total");
                if (totalNode != null && totalNode.has("value")) {
                    status.setApprovedAmount((int) totalNode.get("value").asDouble());
                } else {
                    status.setApprovedAmount(0);
                }
            }

            // Get status - prioritize Task resource output, then ClaimResponse outcome,
            // then claim-state-extension
            String statusValue = "";

            // First, try to get status from Task resource output
            if (taskResource != null) {
                statusValue = extractStatusFromTaskOutput(taskResource);
                if (!statusValue.isEmpty()) {
                    System.err.println("Insurance Claims: Using Task resource output status: " + statusValue);
                }
            }

            // If no status from Task, try ClaimResponse
            if (statusValue.isEmpty() && claimResponseResource != null && claimResponseResource.has("extension")) {
                statusValue = extractStatusFromClaimResponseOutput(claimResponseResource);
                System.err.println("Insurance Claims: Using ClaimResponseResource status: " + statusValue);
            }

            if (statusValue.isEmpty()) {
                System.err.println("Insurance Claims: No status found in Task, ClaimResponse, or extensions, using default: unknown");
                statusValue = "unknown";
            }

            status.setStatus(statusValue);

            // Get and set the claim notes
            if (claimResponseResource != null) {
                String notes = extractNotesFromClaimResponseOutput(claimResponseResource);
                status.setCommentFromApprover(notes);
                status.setNotes(notes);
            } else {
                status.setCommentFromApprover(null);
                status.setNotes(null);
            }

            // Convert created date from Claim
            if (claimResource != null) {
                String createdDateStr = claimResource.get("created").asText();
                Timestamp createdDate = parseDateTime(createdDateStr);
                status.setCreatedAt(createdDate);
                status.setUpdatedAt(createdDate);
            } else {
                Timestamp timestamp = new Timestamp(new Date().getTime());
                status.setCreatedAt(timestamp);
                status.setUpdatedAt(timestamp);
            }
        } else {
            throw new IllegalArgumentException("Insurance Claims: Invalid Response from remote");
        }

        return status;
    }

    /**
     * Get the claim status from the Task resource - extension
     * @param taskResource
     * @return
     */
    private String extractStatusFromTaskOutput(JsonNode taskResource) {
        String lastClaimStateDisplay = "";

        try {
            String baseResourceURL = GeneralUtil.getBaseURLForResourceAndFullURL();
            String taskStatusExtensionURL = baseResourceURL + "/CodeSystem/category-codes/StructureDefinition/claim-outcome";

            FhirContext ctx = FhirContext.forR4();
            IParser parser = ctx.newJsonParser();

            Task task = (Task) parser.parseResource(taskResource.toString());

            Extension statusExtension = task.getExtensionByUrl(taskStatusExtensionURL);
            
            if (statusExtension != null) {
                Type value = statusExtension.getValue();

                if (value instanceof CodeableConcept) {
                    CodeableConcept cc = (CodeableConcept) value;
                    if (cc != null && cc.hasCoding()) {
                        String code = cc.getCodingFirstRep().getCode();
                        System.out.println("Insurance Claims: Task Claims Outcome code: " + code);
                        lastClaimStateDisplay = code;
                    }
                }
            }

            System.err.println("Insurance Claims: Last claim state display: " + lastClaimStateDisplay);
        } catch (Exception ex) {
            System.err.println("Insurance Claims: ERROR: failed to get claim status from TASK FHIR resource: " + ex.getMessage());
            ex.printStackTrace();
        }

        return lastClaimStateDisplay;
    }

    /**
     * Get the claim status from the ClaimResponse resource - extension
     * @param claimResponseResource
     * @return
     */
    private String extractStatusFromClaimResponseOutput(JsonNode claimResponseResource) {
        String lastClaimStateDisplay = "";

        try {
            String baseResourceURL = GeneralUtil.getBaseURLForResourceAndFullURL();
            String claimResponseStatusExtensionURL = baseResourceURL + "/StructureDefinition/claim-state-extension";

            FhirContext ctx = FhirContext.forR4();
            IParser parser = ctx.newJsonParser();

            ClaimResponse claimResponse = (ClaimResponse) parser.parseResource(claimResponseResource.toString());

            Extension statusExtension = claimResponse.getExtensionByUrl(claimResponseStatusExtensionURL);
            
            if (statusExtension != null) {
                Type value = statusExtension.getValue();

                if (value instanceof CodeableConcept) {
                    CodeableConcept cc = (CodeableConcept) value;
                    if (cc != null && cc.hasCoding()) {
                        String code = cc.getCodingFirstRep().getCode();
                        System.out.println("Insurance Claims: claimResponse Claims Outcome code: " + code);
                        lastClaimStateDisplay = code;
                    }
                }
            }

            System.err.println("Insurance Claims: Last claim state display: " + lastClaimStateDisplay);
        } catch (Exception ex) {
            System.err.println("Insurance Claims: ERROR: failed to get claim status from ClaimResponse FHIR resource: " + ex.getMessage());
            ex.printStackTrace();
        }

        return lastClaimStateDisplay;
    }

    /**
     * Get the claim Notes from the ClaimResponse resource - extension
     * @param claimResponseResource
     * @return
     */
    private String extractNotesFromClaimResponseOutput(JsonNode claimResponseResource) {
        String claimNotes = "";

        try {
            FhirContext ctx = FhirContext.forR4();
            IParser parser = ctx.newJsonParser();

            ClaimResponse claimResponse = (ClaimResponse) parser.parseResource(claimResponseResource.toString());

            List<NoteComponent> notes = claimResponse.getProcessNote();
            
            for( NoteComponent note : notes) {
                claimNotes = claimNotes + " || " + note.getText();
            }

            claimNotes = claimNotes.trim();

            System.err.println("Insurance Claims: Claim Response Notes: " + claimNotes);
        } catch (Exception ex) {
            System.err.println("Insurance Claims: ERROR: failed to get claim notes from ClaimResponse FHIR resource: " + ex.getMessage());
            ex.printStackTrace();
        }

        return claimNotes;
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

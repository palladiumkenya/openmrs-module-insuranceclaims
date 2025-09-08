package org.openmrs.module.insuranceclaims.api.service;

import java.io.IOException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.insuranceclaims.api.model.ClaimTransactionStatus;
import org.openmrs.module.insuranceclaims.api.service.fhir.util.FhirToClaimTransactionStatusConverter;
import org.openmrs.module.insuranceclaims.api.service.fhir.util.GeneralUtil;
import org.openmrs.module.insuranceclaims.util.ConstantValues;
import org.openmrs.util.PrivilegeConstants;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import liquibase.pro.packaged.S;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Service class for handling claim transaction statuses.
 * 
 * This class provides functionality to retrieve the latest status of a claim
 * transaction
 * by its ID from an external API. It uses OkHttpClient for HTTP requests and
 * ObjectMapper
 * for JSON parsing. Logs are used to track the API request and response
 * process.
 */
@Service
public class ClaimTransactionStatusService {

    private static final Log log = LogFactory.getLog(ClaimTransactionStatusService.class);

    private final OkHttpClient httpClient = new OkHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ClaimTransactionStatus getLatestStatusById(String transactionId, boolean isHieEnabled, String accessToken, String claimResponseUrl,  String callbackUrl) {
        try {
            ClaimTransactionStatus status = getStatusFromApi(transactionId, isHieEnabled, accessToken, claimResponseUrl,  callbackUrl);
            System.out.println("---> Status from API for transactionId: " + transactionId);

            if (status == null) {
                System.err.println("---> API call failed or returned null status for transactionId: " + transactionId);
            }
            return status;
        } catch (Exception e) {
            System.out.println("---> EXCEPTION in getLatestStatusById: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Retrieves the claim status from the appropriate API based on the
     * CLAIM_RESPONSE_RESOURCE global property.
     *
     * @param transactionId The response ID of the claim transaction.
     * @return ClaimTransactionStatus object containing the status details, or null
     *         if the request fails.
     */
    private ClaimTransactionStatus getStatusFromApi(String transactionId, boolean isHieEnabled, String accessToken, String claimResponseUrl,  String callbackUrl) {
        if (transactionId == null || transactionId.trim().isEmpty()) {
            System.err.println("---> Transaction ID is null or empty.");
            return null;
        }

        try {
            ClaimTransactionStatus status;
            if (!isHieEnabled) {
                System.err.println("---> Using aggregator API for claim status.");
                status = getStatusFromAggregatorApi(transactionId, callbackUrl);
            } else {
                System.err.println("---> Using HIE API for claim status.");
                status = getStatusFromHieApi(transactionId, accessToken, claimResponseUrl);
            }
            return status;

        } catch (Exception e) {
            System.out.println("---> Error in getStatusFromApi for transactionId: " + transactionId + "error : " + e.getMessage());
            return null;
        }
    }


    /**
     * Retrieves the claim status from the HIE API.
     *
     * @param transactionId The ID of the claim transaction.
     * @return ClaimTransactionStatus object containing the status details, or null
     *         if the request fails.
     */
    private ClaimTransactionStatus getStatusFromHieApi(String transactionId, String accessToken, String claimResponseUrl) {


        try {
            System.out.println("---> Retrieved global property: "+ claimResponseUrl);

            String url = claimResponseUrl + "?claim=" + transactionId;

            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("Authorization", "Bearer " + accessToken)
                    .build();

            System.err.println("---> Requesting claim status from HIE API: " + url);
            Response response = httpClient.newCall(request).execute();
            if (response.isSuccessful() && response.body() != null) {
                String jsonResponse = response.body().string();
                log.debug("API response: " + jsonResponse);

                // Use the converter to parse FHIR Bundle
                FhirToClaimTransactionStatusConverter converter = new FhirToClaimTransactionStatusConverter();
                ClaimTransactionStatus status = null;
                try {
                    status = converter.convertFhirBundleToClaimStatus(jsonResponse);
                    // status = converter.convertFhirOperationOutputToClaimStatus(jsonResponse);
                } catch (Exception ex) {
                    System.err.println("---> Error converting FHIR Bundle to ClaimTransactionStatus: " + ex.getMessage());
                    ex.printStackTrace();
                    return null;
                }
                return status;
            } else {
                System.err.println("---> HIE API returned unsuccessful response: " +
                        (response.code()) +
                        (response.body() != null ? " with body: " + response.body().string() : " with no body"));
            }
        } catch (IOException e) {
            System.err.println("---> Error making HTTP request to HIE API: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Retrieves the claim status from the aggregator API.
     *
     * @param transactionId The ID of the claim transaction.
     * @return ClaimTransactionStatus object containing the status details, or null
     *         if the request fails.
     */
    private ClaimTransactionStatus getStatusFromAggregatorApi(String transactionId, String callbackUrl) {
        ClaimTransactionStatus claimStatus = null;
        try {
            System.out.println("---> Retrieved global property: "+ callbackUrl);

            String url = callbackUrl + "?claimId=" + transactionId;
            Request request = new Request.Builder()
                    .url(url)
                    .build();
            System.err.println("---> Requesting claim status from API: " + url);
            Response response = httpClient.newCall(request).execute();
            if (response.isSuccessful() && response.body() != null) {
                String jsonResponse = response.body().string();
                log.debug("--> API response: " + jsonResponse);
                ClaimTransactionStatus status = new ClaimTransactionStatus();
                ClaimTransactionStatus apiResponse = objectMapper.readValue(jsonResponse, ClaimTransactionStatus.class);

                status.setId(apiResponse.getId());
                status.setClaimId(apiResponse.getClaimId());
                status.setApprovedAmount(apiResponse.getApprovedAmount());
                status.setStatus(apiResponse.getStatus());
                status.setCommentFromApprover(apiResponse.getCommentFromApprover());
                status.setNotes(apiResponse.getNotes());
                status.setCreatedAt(apiResponse.getCreatedAt());
                status.setUpdatedAt(apiResponse.getCreatedAt());

                claimStatus = status;
            } else {
                System.err.println("---> API returned unsuccessful response: " +
                        (response.code()) +
                        (response.body() != null ? " with body: " + response.body().string() : " with no body"));
            }
        } catch (IOException e) {
            System.err.println("---> Error making HTTP request: " + e.getMessage());
            e.printStackTrace();
        }

        return claimStatus;
    }
}
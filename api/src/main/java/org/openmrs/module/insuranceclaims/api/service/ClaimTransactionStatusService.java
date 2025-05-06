package org.openmrs.module.insuranceclaims.api.service;

import java.io.IOException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.insuranceclaims.api.model.ClaimTransactionStatus;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
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
    private static final String CALLBACK_URL = "https://billing.kenyahmis.org/api/hie/claim";
    private static final Log log = LogFactory.getLog(ClaimTransactionStatusService.class);

    private final OkHttpClient httpClient = new OkHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ClaimTransactionStatus getLatestStatusById(String transactionId) {
        ClaimTransactionStatus status = getStatusFromApi(transactionId);
        System.out.println("Status from API: " + status);
        log.info("---> Status from API: " + status);

        // If API fails warning
        if (status == null) {
            log.warn("---> API call failed.");
        }

        return status;
    }

    private ClaimTransactionStatus getStatusFromApi(String transactionId) {
        String url = CALLBACK_URL + "?claimId=" + transactionId;
        Request request = new Request.Builder()
                .url(url)
                .build();

        try {
            log.info("---> Requesting claim status from API: " + url);
            Response response = httpClient.newCall(request).execute();
            if (response.isSuccessful() && response.body() != null) {
                String jsonResponse = response.body().string();
                log.debug("API response: " + jsonResponse);

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

                return status;
            } else {
                log.error("---> API returned unsuccessful response: " +
                        (response.code()) +
                        (response.body() != null ? " with body: " + response.body().string() : " with no body"));
            }
        } catch (IOException e) {
            log.error("---> Error making HTTP request: " + e.getMessage(), e);
        }

        return null;
    }
}
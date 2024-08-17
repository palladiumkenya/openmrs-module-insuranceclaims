package org.openmrs.module.insuranceclaims.api.task;

import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.List;

import org.openmrs.api.context.Context;
import org.openmrs.module.insuranceclaims.api.model.InsuranceClaim;
import org.openmrs.module.insuranceclaims.api.service.InsuranceClaimService;
import org.openmrs.module.insuranceclaims.api.service.request.ExternalApiRequest;
import org.openmrs.scheduler.tasks.AbstractTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Prepares payload and performs remote login to CHAI system
 */
public class PullClaimResponsesTask extends AbstractTask {

    private static final Logger log = LoggerFactory.getLogger(PullClaimResponsesTask.class);
    private String url = "http://www.google.com:80/index.html";

    @Autowired
    private ExternalApiRequest externalApiRequest;

    @Autowired
    private InsuranceClaimService insuranceClaimService;
    // InsuranceClaimService insuranceClaimService = Context.getService(InsuranceClaimService.class);

    /**
     * @see AbstractTask#execute()
     */
    public void execute() {
        System.out.println("Insurance Claims: PULL CLAIM RESPONSES TASK Starting");
        Context.openSession();

        // check first if there is internet connectivity before pushing

        try {
            URLConnection connection = new URL(url).openConnection();
            connection.connect();
            try {
                // Get list of claims with no responses
                List<InsuranceClaim> unprocessedClaims = insuranceClaimService.getUnProcessedInsuranceClaims();

                for(InsuranceClaim claim : unprocessedClaims) {
                    String externalId = claim.getExternalId();
                    // Query remote server for response
                    if (externalId != null) {
                        try {
                            externalApiRequest.updateClaim(claim);
                        } catch (Exception ex) {
                            System.err.println("Insurance Claims: Error: Failed to get claim update: " + ex.getMessage());
                            ex.printStackTrace();
                        }
                    } else {
                        try {
                            externalApiRequest.sendClaimToExternalApi(claim);
                        } catch (Exception ex) {
                            System.err.println("Insurance Claims: Error: Failed to send claim to server: " + ex.getMessage());
                            ex.printStackTrace();
                        }
                    }
                }

            } catch (Exception ex) {
                System.err.println("Insurance Claims: Unable to execute task that pushes viral load lab manifest" + ex.getMessage());
                ex.printStackTrace();
            } finally {
                System.out.println("Insurance Claims: PULL CLAIM RESPONSES TASK Finished");
                Context.closeSession();
            }
        } catch (Exception et) {
                String text = "Insurance Claims: At " + new Date() + " there was an error connecting to the internet. Will not attempt claim response pulling: " + et.getMessage();
                System.err.println(text);
                log.warn(text);
        }
    }
}

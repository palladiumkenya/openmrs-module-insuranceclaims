package org.openmrs.module.insuranceclaims.util;

public final class ConstantValues {

    public static final int DEFAULT_DURATION_BILL_DAYS = 7;

    public static final String INSTANT_EXPECTED = "2019-12-01T10:10:10Z";

    public static final String POLICY_NUMBER_ATTRIBUTE_TYPE_NAME = "Insurance policy number";

    public static final String POLICY_NUMBER_ATTRIBUTE_TYPE_DESCRIPTION = "Contains the actual value of the policy number assign to this specific person.";

    public static final String POLICY_NUMBER_ATTRIBUTE_TYPE_FORMAT = "java.lang.String";

    public static final String POLICY_NUMBER_ATTRIBUTE_TYPE_UUID = "22ee615f-2d5d-4be9-bdd3-c4da8c8ba91e";
    public static final String MPESA_DARAJA_API_CALLBACK_URL = "kenyaemr.cashier.mpesa.daraja.api.callback.url";
    public static final String HIE_CALLBACK_URL = "kenyaemr.hie.claim.callback.url";
    public static final String CLAIM_RESPONSE_URL = "kenyaemr.hie.claim.response.url";
    public static final String CLAIM_RESPONSE_SOURCE = "kenyaemr.hie.claim.response.source";

    public static final String COVERAGE_HTTP_METHOD = "insuranceclaims.coverage.http.method";

    private ConstantValues() {
    }
}

package org.openmrs.module.insuranceclaims.api.service.fhir.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class InsuranceClaimConstants {
    public static final String CATEGORY_SERVICE = "service";
    public static final String CATEGORY_ITEM = "item";

    public static final String CLAIM_REFERENCE = "Claim";
    public static final String COMMUNICATION_REQUEST = "CommunicationRequest";
    public static final String DEFAULT_ERROR_CODE = "0"; // Used when no error occurs
    public static final String PERIOD_FROM = "from";
    public static final String PERIOD_TO = "to";
    public static final String MISSING_DATE_FROM = "Date 'from' is missing";
    public static final String MISSING_DATE_TO = "Date 'to' is missing";
    public static final String MISSING_DATE_CREATED = "Date 'to' is missing";

    public static final String MEDICAL_RECORD_NUMBER = "MR";
    public static final String ACCESSION_ID = "ACSN";
    public static final String HL7_VALUESET_SYSTEM = "https://hl7.org/fhir/valueset-identifier-type.html";

    public static final int ENUMERATION_FROM = 1;
    public static final int SEQUENCE_FIRST = 1;
    public static final int NEXT_SEQUENCE = 1;

    public static final String ITEM_ADJUDICATION_GENERAL_CATEGORY = "general";
    public static final String ITEM_ADJUDICATION_REJECTION_REASON_CATEGORY = "rejected_reason";

    public static final String PROVIDER_EXTERNAL_ID_ATTRIBUTE_UUID = "bbdf67e8-c020-40ff-8ad6-74ba34893882";
    public static final String LOCATION_EXTERNAL_ID_ATTRIBUTE_UUID = "217da59b-6003-43b9-9595-b5c1349f1152";
    public static final String PATIENT_EXTERNAL_ID_IDENTIFIER_UUID = "ee8e82c4-1563-43aa-8c73-c3e4e88cb79b";

    public static final String ELEMENTS = "Elements";

    public static final String IS_SERVICE_CONCEPT_ATTRIBUTE_UUID = "925e4987-3104-4d74-989b-3ec96197b532";
    public static final String CONCEPT_PRICE_ATTRIBUTE_UUID = "ddc082c8-db30-4796-890e-f0d487fb9085";
    public static final String EXTERNAL_SYSTEM_CODE_SOURCE_MAPPING_NAME = "ExternalCode";
    public static final String PRIMARY_DIAGNOSIS_MAPPING = "insuranceclaims.diagnosisPrimaryCode"; //TODO: Get from global value

    public static final String GUARANTEE_ID_CATEGORY = "guarantee_id";
    public static final String EXPLANATION_CATEGORY = "explanation";
    public static final String ITEM_EXPLANATION_CATEGORY = "item_explanation";

    public static final String CONTRACT = "Contract";
    public static final int CONTRACT_POLICY_ID_ORDINAL = 1;
    public static final int CONTRACT_EXPIRE_DATE_ORDINAL = 2;

    public static final String EXPECTED_DATE_PATTERN = "yyyy-MM-dd";
    public static final List<String> CONTRACT_DATE_PATTERN = Collections.unmodifiableList(
            Arrays.asList("yyyy-MM-dd hh:mm:ss", "yyyy-MM-dd"));

    public static final String CONSUMED_ITEMS_CONCEPT_NAME = "Items consumed";
    public static final String CONSUMED_ITEMS_CONCEPT_UUID = "907519e6-4b90-473e-b1db-5167352ddcd0";

    public static final String CONSUMABLES_LIST_CONCEPT_NAME = "CONSUMABLES LIST";
    public static final String CONSUMABLES_LIST_ITEMS_CONCEPT_UUID = "df3f4aab-0e18-43cb-89bf-03ec347faa4a";

    public static final String QUANTITY_CONSUMED_CONCEPT_NAME = "QUANTITY CONSUMED";
    public static final String QUANTITY_CONSUMED_CONCEPT_UUID = "dd75407b-bbb3-465b-976d-023b4d79ac54";
    public static final double ABSOULUTE_LOW_CONSUMED_ITEMS = 1.0;
    public static final double ABSOULUTE_HI_CONSUMED_ITEMS = 1.0;

    public static final String CONSUMED_ITEMS_FORM_NAME = "Consumed Items";
    public static final String CONSUMED_ITEMS_FORM_DESCRIPTION = "Used to add information about services and items consumed by the patient.";
    public static final String CONSUMED_ITEMS_FORM_UUID = "2da13321-5829-41d3-b11c-68520b5e4da4";

    public static final String CONSUMED_ITEM_STRATEGY_PROPERTY = "insuranceclaims.consumeditem.strategy";

    public static final String OPENMRS_ID_DEFAULT_IDENTIFIER_SOURCE = "691eed12-c0f1-11e2-94be-8c13b969e334";
    public static final String OPENMRS_ID_DEFAULT_TYPE = "05a29f94-c0ed-11e2-94be-8c13b969e334";

    public static final String SOCIAL_HEALTH_AUTHORITY_IDENTIFICATION_NUMBER = "24aedd37-b5be-4e08-8311-3721b8d5100d";
    public static final String SOCIAL_HEALTH_INSURANCE_NUMBER = "52c3c0c3-05b8-4b26-930e-2a6a54e14c90";
    public static final String PROVIDER_LICENSE_NUMBER = "bcaaa67b-cc72-4662-90c2-e1e992ceda66";
    public static final String LOCATION_LICENSE_NUMBER = "217da59b-6003-43b9-9595-b5c1349f1152";
    public static final String FACILITY_REGISTRY_CODE = "1d1e2531-6a4a-4ed9-ab0a-02663e82379c";
	public static final String FACILITY_LICENSE_NUMBER = "5f719dc5-3a70-48e5-8404-90bbcc35b36e";
    public static final String PROVIDER_HIE_FHIR_REFERENCE = "67b94e8e-4d61-4810-b0f1-d86497f6e553";
    public static final String LOCATION_HIE_FHIR_REFERENCE = "682f0a48-a642-491b-aa6d-41084bee0ee0";

    public static final String PROVIDER_HIE_NATIONAL_ID = "3d152c97-2293-4a2b-802e-e0f1009b7b15";
    public static final String PATIENT_HIE_NATIONAL_ID = "49af6cdc-7968-4abb-bf46-de10d7f4859f";
    public static final String PATIENT_HIE_TELEPHONE_CONTACT = "b2c38640-2603-4629-aebd-3b54f33f1e3a";
	public static final String PROVIDER_UNIQUE_IDENTIFIER = "dace9d99-9f29-4653-9eae-c05929f34a32";

    // Visit Types
    public static final String OUTPATIENT_VISIT_TYPE = "3371a4d4-f66f-4454-a86d-92c7b3da990c";
    public static final String INPATIENT_VISIT_TYPE = "a73e2ac6-263b-47fc-99fc-e0f2c09fc914";
    public static final String CASUALTY_VISIT_TYPE = "0419d15f-67ad-4fd0-97a1-9b5246b2d0d7";
    public static final String MORGUE_VISIT_TYPE = "6307dbe2-f336-4c11-a393-50c2769f455a";

    // Logging GP
    public static final String CLAIMS_LOGGING_ENABLED = "insuranceclaims.debugmode.enabled";

    // Claim Attachments
    public static final String CLAIM_ATTACHMENTS_URL = "insuranceclaims.attachments.url";
	public static final String CLAIM_ATTACHMENTS_USERNAME = "insuranceclaims.attachments.username";
	public static final String CLAIM_ATTACHMENTS_PASSWORD = "insuranceclaims.attachments.password";
	
    private InsuranceClaimConstants() {}
}

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

    // Visit Attributes
    public static final String PAYMENT_METHOD_VISIT_ATTRIBUTE = "e6cb0c3b-04b0-4117-9bc6-ce24adbda802";

    // Payment Modes
    public static final String INSURANCE_PAYMENT_MODE = "beac329b-f1dc-4a33-9e7c-d95821a137a6";

    // Logging GP
    public static final String CLAIMS_LOGGING_ENABLED = "insuranceclaims.debugmode.enabled";

    // Claim Attachments
    public static final String CLAIM_ATTACHMENTS_URL = "insuranceclaims.attachments.url";
	public static final String CLAIM_ATTACHMENTS_USERNAME = "insuranceclaims.attachments.username";
	public static final String CLAIM_ATTACHMENTS_PASSWORD = "insuranceclaims.attachments.password";

    // Order Types
    public static final String ORDER_TYPE_DRUG = "131168f4-15f5-102d-96e4-000c29c2a5d7";
    public static final String ORDER_TYPE_TEST = "52a447d3-a64a-11e3-9aeb-50e549534c5e";
    public static final String ORDER_TYPE_PROCEDURE = "b4a7c280-369e-4d12-9ce8-18e36783fed6";

    // Enounter Types
    public static final String ENCOUNTER_TYPE_TB_SCREENING = "ed6dacc9-0827-4c82-86be-53c0d8c449be";
    public static final String ENCOUNTER_TYPE_HIV_DISCONTINUATION = "2bdada65-4c72-4a48-8730-859890e25cee";
    public static final String ENCOUNTER_TYPE_CONSULTATION = "465a92f2-baf8-42e9-9612-53064be868e8";
    public static final String ENCOUNTER_TYPE_LAB_RESULTS = "17a381d1-7e29-406a-b782-aa903b963c28";
    public static final String ENCOUNTER_TYPE_REGISTRATION = "de1f9d67-b73e-4e1b-90d0-036166fc6995";
    public static final String ENCOUNTER_TYPE_TRIAGE = "d1059fb9-a079-4feb-a749-eedd709ae542";
    public static final String ENCOUNTER_TYPE_HIV_ENROLLMENT = "de78a6be-bfc5-4634-adc3-5f1a280455cc";
    public static final String ENCOUNTER_TYPE_HIV_CONSULTATION = "a0034eee-1940-4e35-847f-97537a35d05e";
    public static final String ENCOUNTER_TYPE_CWC_ENROLLMENT = "415f5136-ca4a-49a8-8db3-f994187c3af6";
    public static final String ENCOUNTER_TYPE_CWC_CONSULTATION = "bcc6da85-72f2-4291-b206-789b8186a021";
    public static final String ENCOUNTER_TYPE_MCH_CHILD_HEI_EXIT = "01894f88-dc73-42d4-97a3-0929118403fb";
    public static final String ENCOUNTER_TYPE_MCH_CHILD_IMMUNIZATION = "82169b8d-c945-4c41-be62-433dfd9d6c86";
    public static final String ENCOUNTER_TYPE_MCH_CHILD_DISCONTINUATION = "5feee3f1-aa16-4513-8bd0-5d9b27ef1208";
    public static final String ENCOUNTER_TYPE_MCH_MOTHER_ENROLLMENT = "3ee036d8-7c13-4393-b5d6-036f2fe45126";
    public static final String ENCOUNTER_TYPE_MCH_MOTHER_CONSULTATION = "c6d09e05-1f25-4164-8860-9f32c5a02df0";
    public static final String ENCOUNTER_TYPE_MCH_MOTHER_DISCONTINUATION = "7c426cfc-3b47-4481-b55f-89860c21c7de";
    public static final String ENCOUNTER_TYPE_TB_ENROLLMENT = "9d8498a4-372d-4dc4-a809-513a2434621e";
    public static final String ENCOUNTER_TYPE_TB_DISCONTINUATION = "d3e3d723-7458-4b4e-8998-408e8a551a84";
    public static final String ENCOUNTER_TYPE_TB_FOLLOWUP = "fbf0bfce-e9f4-45bb-935a-59195d8a0e35";
    public static final String ENCOUNTER_TYPE_HTS = "9c0a7a57-62ff-4f75-babe-5835b0e921b7";
    public static final String ENCOUNTER_TYPE_ART_REFILL = "e87aa2ad-6886-422e-9dfd-064e3bfe3aad";
    public static final String ENCOUNTER_TYPE_FAMILY_AND_PARTNER_TESTING = "975ae894-7660-4224-b777-468c2e710a2a";
    public static final String ENCOUNTER_TYPE_HIV_CONFIRMATION = "0c61819d-4f82-434e-b24d-aa8c82d49297";
    public static final String ENCOUNTER_TYPE_IPT_INITIATION = "de5cacd4-7d15-4ad0-a1be-d81c77b6c37d";
    public static final String ENCOUNTER_TYPE_IPT_OUTCOME = "bb77c683-2144-48a5-a011-66d904d776c9";
    public static final String ENCOUNTER_TYPE_IPT_FOLLOWUP = "aadeafbe-a3b1-4c57-bc76-8461b778ebd6";
    public static final String ENCOUNTER_TYPE_EXTERNAL_P_SMART = "9bc15e94-2794-11e8-b467-0ed5f89f718b";
    public static final String ENCOUNTER_TYPE_DRUG_ORDER = "7df67b83-1b84-4fe2-b1b7-794b4e9bfcc3";
    public static final String ENCOUNTER_TYPE_DRUG_REGIMEN_EDITOR = "7dffc392-13e7-11e9-ab14-d663bd873d93";
    public static final String ENCOUNTER_TYPE_LAB_ORDER = "e1406e88-e9a9-11e8-9f32-f2801f1b9fd1";
    public static final String ENCOUNTER_TYPE_CCC_DEFAULTER_TRACING = "1495edf8-2df2-11e9-b210-d663bd873d93";
    public static final String ENCOUNTER_TYPE_PREP_ENROLLMENT = "35468fe8-a889-4cd4-9b35-27ac98bdd750";
    public static final String ENCOUNTER_TYPE_PREP_CONSULTATION = "c4a2be28-6673-4c36-b886-ea89b0a42116";
    public static final String ENCOUNTER_TYPE_PREP_BEHAVIOR_RISK_ASSESSMENT = "6e5ec039-8d2a-4172-b3fb-ee9d0ba647b7";
    public static final String ENCOUNTER_TYPE_PREP_CLIENT_DISCONTINUATION = "3c37b7a0-9f83-4fdc-994f-17308c22b423";
    public static final String ENCOUNTER_TYPE_PREP_STI_SCREENING = "83610d13-d4fc-42c3-8c1d-a403cd6dd073";
    public static final String ENCOUNTER_TYPE_PREP_VMMC_SCREENING = "402c10a3-d419-4040-b5d8-bde0af646405";
    public static final String ENCOUNTER_TYPE_FERTILITY_INTENTION_SCREENING = "c4657c33-f252-4ba9-8a4f-b09ed0deda75";
    public static final String ENCOUNTER_TYPE_PREP_ALLERGIES_SCREENING = "119362fb-6af6-4462-9fb2-7a09c43c9874";
    public static final String ENCOUNTER_TYPE_PREP_CHRONIC_ILLNESS_SCREENING = "26bb869b-b569-4acd-b455-02c853e9f1e6";
    public static final String ENCOUNTER_TYPE_PREP_ADVERSE_DRUG_REACTIONS = "d7cfa460-2944-11e9-b210-d663bd873d93";
    public static final String ENCOUNTER_TYPE_PREP_STATUS = "47c73adb-f9db-4c79-b582-e16064f9cee0";
    public static final String ENCOUNTER_TYPE_PREP_PREGNANCY_OUTCOMES = "5feffc6c-3194-43df-9b80-290054216c35";
    public static final String ENCOUNTER_TYPE_PREP_APPOINTMENT_CREATION = "66609dee-3438-11e9-b210-d663bd873d93";
    public static final String ENCOUNTER_TYPE_PREP_MONTHLY_REFILL = "291c0828-a216-11e9-a2a3-2a2ae2dbcce4";
    public static final String ENCOUNTER_TYPE_PREP_INITIAL = "706a8b12-c4ce-40e4-aec3-258b989bf6d3";
    public static final String ENCOUNTER_TYPE_OVC_ENROLLMENT = "5cf0124e-09da-11ea-8d71-362b9e155667";
    public static final String ENCOUNTER_TYPE_OVC_DISCONTINUATION = "5cf00d9e-09da-11ea-8d71-362b9e155667";
    public static final String ENCOUNTER_TYPE_OTZ_ENROLLMENT = "16238574-0464-11ea-9a9f-362b9e155667";
    public static final String ENCOUNTER_TYPE_OTZ_DISCONTINUATION = "162382b8-0464-11ea-9a9f-362b9e155667";
    public static final String ENCOUNTER_TYPE_OTZ_ACTIVITY = "162386c8-0464-11ea-9a9f-362b9e155667";
    public static final String ENCOUNTER_TYPE_KP_ENROLLMENT = "c7f47a56-207b-11e9-ab14-d663bd873d93";
    public static final String ENCOUNTER_TYPE_KP_DISCONTINUATION = "d7142400-2495-11e9-ab14-d663bd873d93";
    public static final String ENCOUNTER_TYPE_KP_TRACING = "ce841b19-0acd-46fd-b223-2ca9b5356237";
    public static final String ENCOUNTER_TYPE_KVP_LHIV_TRACKER_OFFSITE = "999792ec-8854-11e9-bc42-526af7764f64";
    public static final String ENCOUNTER_TYPE_KP_ALCOHOL_SCREENING = "a3ce2705-d72d-458a-a76c-dae0f93398e7";
    public static final String ENCOUNTER_TYPE_KP_DIAGNOSIS_AND_TREATMENT = "928ea6b2-3425-4ee9-854d-daa5ceaade03";
    public static final String ENCOUNTER_TYPE_KP_PEER_OVERDOSE_SCREENING = "c3fb7831-f8fc-4b71-bd54-f23cdd77e305";
    public static final String ENCOUNTER_TYPE_KP_PEER_OVERDOSE_REPORTING = "383974fe-58ef-488f-bdff-8962f4dd7518";
    public static final String ENCOUNTER_TYPE_KP_STI_DETAILED_TREATMENT = "2cc8c535-bbfa-4668-98c7-b12e3550ee7b";
    public static final String ENCOUNTER_TYPE_KP_VIOLENCE_SCREENING = "7b69daf5-b567-4384-9d29-f020c408d613";
    public static final String ENCOUNTER_TYPE_KP_DEPRESSION_SCREENING = "84220f19-9071-4745-9045-3b2f8d3dc128";
    public static final String ENCOUNTER_TYPE_KP_HCW_OVERDOSE_REPORTING = "bd64b3b0-7bc9-4541-a813-8a917f623e2e";
    public static final String ENCOUNTER_TYPE_KP_CONTACT = "ea68aad6-4655-4dc5-80f2-780e33055a9e";
    public static final String ENCOUNTER_TYPE_KP_REFERRAL = "596f878f-5adf-4f8e-8829-6a87aaeda9a3";
    public static final String ENCOUNTER_TYPE_KP_CLINIC_VISIT_FORM = "92e03f22-9686-11e9-bc42-526af7764f64";
    public static final String ENCOUNTER_TYPE_KP_PEER_CALENDAR = "c4f9db39-2c18-49a6-bf9b-b243d673c64d";
    public static final String ENCOUNTER_TYPE_KP_DIAGNOSIS = "119217a4-06d6-11ea-8d71-362b9e155667";
    public static final String ENCOUNTER_TYPE_KP_KPTREATMENTVERIFICATION = "a70a1056-75b3-11ea-bc55-0242ac130003";
    public static final String ENCOUNTER_TYPE_KP_PREP_TREATMENT_VERIFICATION = "5c64e368-7fdc-11ea-bc55-0242ac130003";
    public static final String ENCOUNTER_TYPE_KP_GENDER_BASED_VIOLENCE = "94eebf1a-83a1-11ea-bc55-0242ac130003";
    public static final String ENCOUNTER_TYPE_KP_IDENITFIER = "b046eb36-7bd0-40cf-bdcb-c662bc0f00c3";
    public static final String ENCOUNTER_TYPE_CERVICAL_CANCER_SCREENING = "3fefa230-ea10-45c7-b62b-b3b8eb7274bb";
    public static final String ENCOUNTER_TYPE_VDOT_ENCOUNTER = "d7aaaf20-31ca-4d22-9dd9-0796eb47a341";
    public static final String ENCOUNTER_TYPE_VDOT_ENROLLMENT = "cf805d0a-a470-4194-b375-7e04f56d4dee";
    public static final String ENCOUNTER_TYPE_VDOT_DISCONTINUATION = "90e54c41-da23-4ace-b472-0c8521c97594";
    public static final String ENCOUNTER_TYPE_VDOT_BASELINE = "e360f35f-e496-4f01-843b-e2894e278b5b";
    public static final String ENCOUNTER_TYPE_COVID_19_ASSESSMENT_ENCOUNTER = "86709cfc-1490-11ec-82a8-0242ac130003";
    public static final String ENCOUNTER_TYPE_HIV_SELF_TESTING = "8b706d42-b4ae-4b3b-bd83-b14f15294362";
    public static final String ENCOUNTER_TYPE_VMMC_ENROLLMENT = "85019fbe-9339-49f7-8341-e9a04311bb99";
    public static final String ENCOUNTER_TYPE_VMMC_DISCONTINUATION = "4f02dfed-a2ec-40c2-b546-85dab5831871";
    public static final String ENCOUNTER_TYPE_VMMC_PROCEDURE = "35c6fcc2-960b-11ec-b909-0242ac120002";
    public static final String ENCOUNTER_TYPE_VMMC_MEDICAL_HISTORY_AND_EXAMINATION = "a2010bf5-2db0-4bf4-819f-8a3cffbcb21b";
    public static final String ENCOUNTER_TYPE_VMMC_CLIENT_FOLLOW_UP = "2504e865-638e-4a63-bf08-7e8f03a376f3";
    public static final String ENCOUNTER_TYPE_VMMC_IMMEDIATE_POST_OPERATION_ASSESSMENT = "6632e66c-9ae5-11ec-b909-0242ac120002";
    public static final String ENCOUNTER_TYPE_ALCOHOL_AND_DRUG_ABUSE_SCREENING = "4224f8bf-11b2-4e47-a958-1dbdfd7fa41d";
    public static final String ENCOUNTER_TYPE_ART_PREPARATION = "ec2a91e5-444a-4ca0-87f1-f71ddfaf57eb";
    public static final String ENCOUNTER_TYPE_ENHANCED_ADHERENCE_SCREENING = "54df6991-13de-4efc-a1a9-2d5ac1b72ff8";
    public static final String ENCOUNTER_TYPE_VIOLENCE_SCREENING_ENCOUNTER = "f091b067-bea5-4657-8445-cfec05dc46a2";
    public static final String ENCOUNTER_TYPE_GENERALIZED_ANXIETY_DISORDER_ASSESSMENT = "899d64ad-be13-4071-a879-2153847206b7";
    public static final String ENCOUNTER_TYPE_ONCOLOGY_SCREENING = "e24209cc-0a1d-11eb-8f2a-bb245320c623";
    public static final String ENCOUNTER_TYPE_ATTACHMENT_UPLOAD = "5021b1a1-e7f6-44b4-ba02-da2f2bcf8718";
    public static final String ENCOUNTER_TYPE_IMMUNIZATIONS = "29c02aff-9a93-46c9-bf6f-48b552fcb1fa";
    public static final String ENCOUNTER_TYPE_MAT_CLINICAL_ENCOUNTER = "c3518485-ee22-4a47-b6d4-6d0e8f297b02";
    public static final String ENCOUNTER_TYPE_ILI_SURVEILLANCE = "f60910c7-2edd-4d93-813c-0e57095f892f";
    public static final String ENCOUNTER_TYPE_SARI_SURVEILLANCE = "76d55715-88cc-4851-b5e0-09136426fd46";
    public static final String ENCOUNTER_TYPE_MAT_CESSATION_ENCOUNTER = "b7addaab-bafd-445d-929d-d6631a0de63e";
    public static final String ENCOUNTER_TYPE_MAT_CLINICAL_ELIGIBILITY_ASSESSMENT = "cb0ca111-6142-4d9b-a670-a32a64940cca";
    public static final String ENCOUNTER_TYPE_MAT_DISCONTINUATION_ENCOUNTER = "d36bd361-c155-4c3d-a44b-df99f4e111b3";
    public static final String ENCOUNTER_TYPE_MAT_INITIAL_REGISTRATION_ENCOUNTER = "b4b87535-90b7-4a26-b224-368576095f11";
    public static final String ENCOUNTER_TYPE_MAT_TREATMENT_ENCOUNTER = "9f49a7b8-367c-4a9f-a66e-9f697fc683fe";
    public static final String ENCOUNTER_TYPE_MAT_PSYCHIATRIC_INTAKE_AND_FOLLOWUP = "fbf32c5d-e58e-4001-8330-3776c5691d6e";
    public static final String ENCOUNTER_TYPE_MAT_PSYCHOSOCIAL_INTAKE_AND_FOLLOWUP = "5ab99d58-b337-4796-94fb-101381b46618";
    public static final String ENCOUNTER_TYPE_MAT_TRANSIT_REFERRAL_ENCOUNTER = "1d00fede-7491-485e-86a1-9a9c2bd772fa";
    public static final String ENCOUNTER_TYPE_PROCEDURE_RESULTS = "99a7a6ba-59f4-484e-880d-01cbeaead62f";
    public static final String ENCOUNTER_TYPE_NUTRITION = "160fcc03-4ff5-413f-b582-7e944a770bed";
    public static final String ENCOUNTER_TYPE_NCD_INITIAL = "dfcbe5d0-1afb-48a0-8f1e-5e5988b11f15";
    public static final String ENCOUNTER_TYPE_NCD_FOLLOWUP = "b402d094-bff3-4b31-b167-82426b4e3e28";
    public static final String ENCOUNTER_TYPE_HIGH_IIT_INTERVENTION = "84d66c25-e2bd-48a2-8686-c1652eb9d283";
    public static final String ENCOUNTER_TYPE_VIOLENCE_ENROLLMENT_ENCOUNTER = "b3f8c498-7f17-44c7-993e-6cd981a5f420";
    public static final String ENCOUNTER_TYPE_VIOLENCE_TRAUMA_COUNSELLING_ENCOUNTER = "16048e65-24c6-4833-a342-29000143298f";
    public static final String ENCOUNTER_TYPE_VIOLENCE_DISCONTINUATION_ENCOUNTER = "ab3031fe-d67c-4ae4-9317-f6f5b920b776";
    public static final String ENCOUNTER_TYPE_VIOLENCE_COMMUNITY_LINKAGE_ENCOUNTER = "b38c73ee-6949-4f5f-8013-a89e36474d72";
    public static final String ENCOUNTER_TYPE_VIOLENCE_LEGAL_ENCOUNTER = "8941dc56-afda-4155-866f-e9fc0e71f5fd";
    public static final String ENCOUNTER_TYPE_VIOLENCE_PERPETRATOR_DETAILS_ENCOUNTER = "189e128f-3212-47ca-9d89-ee1876c394a9";
    public static final String ENCOUNTER_TYPE_VIOLENCE_CONSENT_ENCOUNTER = "3809b3fc-bcdd-46fe-a461-60a341a842d0";
    public static final String ENCOUNTER_TYPE_PEP_MANAGEMENT_NON_OCN_ENCOUNTER = "4f718f68-b414-4e27-803e-b4fbbc959d89";
    public static final String ENCOUNTER_TYPE_PEP_MANAGEMENT_OCN_ENCOUNTER = "5c1ecaf1-ec25-46b7-9b5e-ee7fe44f03cf";
    public static final String ENCOUNTER_TYPE_PEP_MANAGEMENT_SURVIVOR_ENCOUNTER = "133c8398-1fdc-437a-a74a-e73b1254c1d6";
    public static final String ENCOUNTER_TYPE_SEXUAL_VIOLENCE_POST_RAPE_CARE_363A = "e571c807-8fcc-4bc3-bc64-4ed372b348e4";
    public static final String ENCOUNTER_TYPE_SEXUAL_VIOLENCE_PRC_PSYCHOLOGICAL_ASSESSMENT_363B = "b5ab0a6b-9425-44da-b0d9-242f609f1605";
    public static final String ENCOUNTER_TYPE_VIOLENCE_PHYSICAL_AND_EMOTIONAL_ABUSE = "39d9350b-6eba-4653-a0cf-c554f44f6e91";
    public static final String ENCOUNTER_TYPE_VIOLENCE_PEP_FOLLOW_UP_ENCOUNTER = "24c11d80-986d-4f13-9086-d0a01a84dae3";
    public static final String ENCOUNTER_TYPE_CPM_ENROLLMENT_ENCOUNTER = "e0700664-90b6-4480-aad2-6e0a00babd66";
    public static final String ENCOUNTER_TYPE_CPM_REFERRAL_ENCOUNTER = "c49952bf-218b-44b4-8d2a-1947cbf00fff";
    public static final String ENCOUNTER_TYPE_CPM_DISCONTINUATION_ENCOUNTER = "ddc73ecc-ad85-40bc-ad43-318ed9abfd00";
    public static final String ENCOUNTER_TYPE_CPM_SCREENING_ENCOUNTER = "f0b27e6c-57cd-4dec-ad6b-43eee6e571ee";
    public static final String ENCOUNTER_TYPE_HOME_VISIT_CHECKLIST = "bfbb5dc2-d3e6-41ea-ad86-101336e3e38f";
    public static final String ENCOUNTER_TYPE_AUDIOLOGY = "49da00fd-5b62-437a-a2d4-a28b3d22fa27";
    public static final String ENCOUNTER_TYPE_PSYCHIATRIC = "7671cc06-b852-46e6-a279-afc8e2343a04";
    public static final String ENCOUNTER_TYPE_ONCOLOGY = "70a0158e-98f3-400b-9c90-a13c84b72065";
    public static final String ENCOUNTER_TYPE_PHYSIOTHERAPY = "a0ee267f-4555-48d7-9b1b-6d0dadee8506";
    public static final String ENCOUNTER_TYPE_GOPC = "92be533c-35f0-4505-bfbd-95724bea0208";
    public static final String ENCOUNTER_TYPE_MOPC = "4c629037-c0cd-4094-84d7-0737ab7b1bd0";
    public static final String ENCOUNTER_TYPE_SOPC = "d14dde5b-95dc-40a1-8ff0-acad34fb58b2";
    public static final String ENCOUNTER_TYPE_POPC = "6f8e49f2-3bff-4aff-909b-20568c316625";
    public static final String ENCOUNTER_TYPE_MAXILLOFACIAL = "92999f52-f352-415a-9e0d-87872e5b2c8d";
    public static final String ENCOUNTER_TYPE_SPEECH_AND_LANGUAGE = "5d0b6d85-5b88-410c-9f0f-4dab3db7ceb2";
    public static final String ENCOUNTER_TYPE_FAMILY_PLANNING = "85b019dc-18ec-4315-b661-5f7037e7ce38";
    public static final String ENCOUNTER_TYPE_DIABETIC_CLINIC = "70dc0091-064d-4428-ade8-119f142a93a2";
    public static final String ENCOUNTER_TYPE_ADVERSE_DRUG_REACTION = "7a185fe4-c56f-4195-b682-d3f5afa9d9c2";
    public static final String ENCOUNTER_TYPE_DERMATOLOGY_CLINIC = "e6eb6328-3f24-43f8-9f75-92daccb6ac48";
    public static final String ENCOUNTER_TYPE_UROLOGY_CLINIC = "1b1d8425-49e0-4cc6-8a66-6a598b5ac0a5";
    public static final String ENCOUNTER_TYPE_HEARING_SCREENING_CLINIC = "9c015ff8-8c5d-42bc-8ebd-dc7ea01e19d3";
    public static final String ENCOUNTER_TYPE_NEUROLOGY_CLINIC = "14d2b0fb-7fac-49df-bcf5-a07463fa3433";
    public static final String ENCOUNTER_TYPE_POST_MORTEM = "32b61a73-4971-4fc0-b20b-9a30176317e2";
    public static final String ENCOUNTER_TYPE_MORTUARY_ADMISSION = "3d2df845-6f3c-45e7-b91a-d828a1f9c2e8";
    public static final String ENCOUNTER_TYPE_MORTUARY_DISCHARGE = "3d618f40b-b5a3-4f17-81c8-2f04e2aad58e";
    public static final String ENCOUNTER_TYPE_INFECTIOUS_DISEASE = "a2cac281-81a8-4f35-9bc5-62493c8ee7df";
    public static final String ENCOUNTER_TYPE_IPD_PROCEDURE = "68634d60-86de-485e-99f9-76622fc5856b";
    public static final String ENCOUNTER_TYPE_NURSING_CARE_PLAN = "b6569074-3b8c-43ba-bd4a-98c445405035";
    public static final String ENCOUNTER_TYPE_DOCTORS_NOTE = "14b36860-5033-4765-b91b-ace856ab64c2";
    public static final String ENCOUNTER_TYPE_POST_OPERATION = "13beea61-7d3d-4860-abe3-d5b874f736fb";
    public static final String ENCOUNTER_TYPE_PRE_OPERATION_CHECKLIST = "9023e0cd-78ef-44af-ba54-47f30f739b4a";
    public static final String ENCOUNTER_TYPE_NEW_BORN_ADMISSION = "454db697-7bc4-49e7-a9fa-097c19f1c9ec";
    public static final String ENCOUNTER_TYPE_PLASTIC_SURGERY_CLINIC = "5748bd8a-06bf-41a0-88df-16eed290db21";
    public static final String ENCOUNTER_TYPE_IIT_SCORE = "1dab4593-b09d-4c5b-83fe-f041092145d3";
    public static final String ENCOUNTER_TYPE_IPD_DISCHARGE = "7e618d13-ffdb-4650-9a97-10ccd16ca36d";
    public static final String ENCOUNTER_TYPE_LEPROSY_INITIAL = "6c15c4d1-91a5-4db1-928b-75ec05ee8e74";
    public static final String ENCOUNTER_TYPE_LEPROSY_FOLLOWUP = "8db0b85a-441b-4ee3-8955-0cf4cb55f726";
    public static final String ENCOUNTER_TYPE_LEPROSY_POSTOPERATIVE = "4469d5f5-18fe-4f47-bf05-36819b9bf5e1";
    public static final String ENCOUNTER_TYPE_FLUID_INTAKE_AND_OUTPUT = "657bdb00-0eab-48a6-8da4-bb1644d5fd48";
    public static final String ENCOUNTER_TYPE_ADR_ASSESSMENT_TOOL = "d18d6d8a-4be2-4115-ac7e-86cc0ec2b263";
    public static final String ENCOUNTER_TYPE_INITIAL_NURSING_CARDEX = "3efe6966-a011-4d24-aa43-d3051bfbb8e3";
    public static final String ENCOUNTER_TYPE_MCH_PARTOGRAPH = "022d62af-e2a5-4282-953b-52dd5cba3296";
    public static final String ENCOUNTER_TYPE_MCH_INPATIENT = "6877a5b4-441d-4dff-ada1-fcc2485c33e6";
    public static final String ENCOUNTER_TYPE_MCH_POST_DELIVERY = "07dc609f-e607-43d6-9dc3-8bd405c4226a";
    public static final String ENCOUNTER_TYPE_NUTRITION_ENROLLMENT = "cff2c1c2-b5ff-4b06-a0aa-6973007b89cb";
    public static final String ENCOUNTER_TYPE_NUTRITION_DISCONTINUATION = "41cc14fa-6011-4939-8c2c-0d1c2554efc8";
    public static final String ENCOUNTER_TYPE_AEFI_INVESTIGATION = "036a0ef4-8197-41ad-bfbf-802e79a14606";
    public static final String ENCOUNTER_TYPE_ANC_MOTHER_ENROLLMENT = "613404ad-d08b-4413-8ea7-1867bf291c55";
    public static final String ENCOUNTER_TYPE_FAMILY_PLANNING_ENROLLMENT = "36416b85-4e86-4b49-9ef6-113dbe69f9ad";
    public static final String ENCOUNTER_TYPE_PNC_MOTHER_ENROLLMENT = "9ac87837-f890-483f-a17a-54d2e128487f";
    public static final String ENCOUNTER_TYPE_ANC_DISCONTINUATION = "97b3aedc-7447-4b2f-a22d-c6fdab6366b3";
    public static final String ENCOUNTER_TYPE_PNC_DISCONTINUATION = "a5e55a35-e203-480a-aae0-b0f051f84277";
    public static final String ENCOUNTER_TYPE_FAMILY_PLANNING_DISCONTINUATION = "eaf7da42-2985-41a5-a89e-eba6223ff2d6";
    public static final String ENCOUNTER_TYPE_PRE_CONCEPTION_CARE = "27db32f2-cf1d-479c-91c7-b2a15fe3bb65";
    public static final String ENCOUNTER_TYPE_NCD_DISCONTINUATION = "c31d1a11-f0a5-4a64-817d-25e5134f2e37";
    public static final String ENCOUNTER_TYPE_PRE_CONCEPTION_CARE_ENROLLMENT = "fe44c39d-0273-412b-b1f2-eed39841bfe4";
    public static final String ENCOUNTER_TYPE_PRE_CONCEPTION_CARE_DISCONTINUATION = "ba25f357-764b-45f7-a8d3-6c1d50325bc7";
    public static final String ENCOUNTER_TYPE_PLHIV_LINK_FACILITY_DOCUMENTATION_TRACKING = "b93ae0fc-9b57-4c79-936b-596553aab8b0";

	
    private InsuranceClaimConstants() {}
}

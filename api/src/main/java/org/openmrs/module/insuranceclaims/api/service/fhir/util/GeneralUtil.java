package org.openmrs.module.insuranceclaims.api.service.fhir.util;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Practitioner;
import org.openmrs.GlobalProperty;
import org.openmrs.Location;
import org.openmrs.LocationAttribute;
import org.openmrs.LocationAttributeType;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonAttributeType;
import org.openmrs.Provider;
import org.openmrs.ProviderAttribute;
import org.openmrs.ProviderAttributeType;
import org.openmrs.api.context.Context;
import org.openmrs.module.metadatadeploy.MetadataUtils;
import org.openmrs.util.PrivilegeConstants;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import java.util.Optional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.openmrs.module.insuranceclaims.api.service.fhir.util.InsuranceClaimConstants.SOCIAL_HEALTH_AUTHORITY_IDENTIFICATION_NUMBER;
import static org.openmrs.module.insuranceclaims.api.service.fhir.util.InsuranceClaimConstants.PROVIDER_LICENSE_NUMBER;
import static org.openmrs.module.insuranceclaims.api.service.fhir.util.InsuranceClaimConstants.LOCATION_LICENSE_NUMBER;
import static org.openmrs.module.insuranceclaims.api.service.fhir.util.InsuranceClaimConstants.FACILITY_LICENSE_NUMBER;
import static org.openmrs.module.insuranceclaims.api.service.fhir.util.InsuranceClaimConstants.FACILITY_REGISTRY_CODE;
import static org.openmrs.module.insuranceclaims.api.service.fhir.util.InsuranceClaimConstants.PROVIDER_HIE_FHIR_REFERENCE;
import static org.openmrs.module.insuranceclaims.api.service.fhir.util.InsuranceClaimConstants.LOCATION_HIE_FHIR_REFERENCE;
import static org.openmrs.module.insuranceclaims.api.service.fhir.util.InsuranceClaimConstants.PATIENT_HIE_NATIONAL_ID;
import static org.openmrs.module.insuranceclaims.api.service.fhir.util.InsuranceClaimConstants.PATIENT_HIE_TELEPHONE_CONTACT;

public class GeneralUtil {

    
    /**
     * Gets the HIE JWT auth token
     * @return
     * @throws IOException
     */
    public static String getJWTAuthToken() throws IOException {
        String ret = null;
        try {
            OkHttpClient client = new OkHttpClient();

            String auth = Context.getAdministrationService().getGlobalProperty("insuranceclaims.hiejwt.custom.encodedpass");
            String hieJwtUrl = Context.getAdministrationService().getGlobalProperty("insuranceclaims.hiejwt.url");
            if(auth != null && hieJwtUrl != null && StringUtils.isNotEmpty(auth) && StringUtils.isNotEmpty(hieJwtUrl)) {
                Request request = new Request.Builder()
                        .url(hieJwtUrl)
                        .header("Authorization", "Basic " + auth)
                        .build();
                Response response = client.newCall(request).execute();
                if (!response.isSuccessful()) {
                    System.err.println("Insurance- Claims: Get HIE Auth: ERROR: Request failed: " + response.code() + " - " + response.message());
                } else {
                    return response.body().string();
                }
            } else {
                System.err.println("Insurance- Claims: Get HIE Auth: ERROR: Request failed: The global properties for hie jwt must be set");
            }
        } catch(Exception ex) {
            System.err.println("Insurance- Claims: Get HIE Auth: ERROR: Request failed: " + ex.getMessage());
            ex.printStackTrace();
        }
        return(ret);
    }

    /**
     * Gets the HIE Staging auth token
     * @return
     * @throws IOException
     */
    public static String getHIEStagingAuthToken() throws IOException {
        String ret = null;
        try {
            OkHttpClient client = new OkHttpClient();

            String auth = Context.getAdministrationService().getGlobalProperty("insuranceclaims.hiestaging.custom.encodedpass");
            String hieStagingAuthUrl = Context.getAdministrationService().getGlobalProperty("insuranceclaims.hiestaging.auth.url");
            if(auth != null && hieStagingAuthUrl != null && StringUtils.isNotEmpty(auth) && StringUtils.isNotEmpty(hieStagingAuthUrl)) {
                System.out.println("Insurance- Claims: Get HIE Staging Auth token URL: " + hieStagingAuthUrl + " Auth: " + auth);
                okhttp3.MediaType mediaType = okhttp3.MediaType.parse("text/plain");
                okhttp3.RequestBody body = okhttp3.RequestBody.create(mediaType, "");
                Request request = new Request.Builder()
                        .url(hieStagingAuthUrl)
                        .method("POST", body)
                        .header("Authorization", "Basic " + auth)
                        .header("Cookie", "incap_ses_1018_2912339=uz4wbZrEaRQGuNQvzKkgDoZOi2cAAAAAh71yBQVsYKTfUsik3X0AHg==; incap_ses_6550_2912339=P9iSNzSCAWGD2agnhkbmWjpNi2cAAAAAtq+a6uIcQS1f7zitlRG7OA==; visid_incap_2912339=+CAiq0KsQoWKzfmVNLKgIQg6b2cAAAAAQUIPAAAAAAB3pFvtYwe7WLmCrwbOPKPg")
                        .build();
                Response response = client.newCall(request).execute();
                if (!response.isSuccessful()) {
                    System.err.println("Insurance- Claims: Get HIE Staging Auth: ERROR: Request failed: " + response.code() + " - " + response.message());
                } else {
                    String payload = response.body().string();
                    System.out.println("Insurance- Claims: Got HIE Staging Auth token payload: " + payload);
                    ObjectMapper objectMapper = new ObjectMapper();
                    JsonNode rootNode = objectMapper.readTree(payload);
                    return rootNode.path("access_token").asText();
                }
            } else {
                System.err.println("Insurance- Claims: Get HIE Staging Auth: ERROR: Request failed: The global properties for hie Staging must be set");
            }
        } catch(Exception ex) {
            System.err.println("Insurance- Claims: Get HIE Staging Auth: ERROR: Request failed: " + ex.getMessage());
            ex.printStackTrace();
        }
        return(ret);
    }

    /**
     * Gets the Apiero ApiKey auth token
     * @return
     * @throws IOException
     */
    public static String getApiKeyAuthToken() throws IOException {
        String auth = Context.getAdministrationService().getGlobalProperty("insuranceclaims.apiero.apikey");
        return auth;
    }

    /**
     * Gets the full URL and resource base URL property from globals 
     * @return
     * @throws IOException
     */
    public static String getBaseURLForResourceAndFullURL() throws IOException {
        String auth = Context.getAdministrationService().getGlobalProperty("insuranceclaims.base.resource.url");
        return auth;
    }

    /**
     * Get the CR number of patient
     * @return
     */
    public static String getPatientCRNo(Patient patient) {
        String ret = "";
        if(patient != null) {

            PatientIdentifierType shaIdentifierType = Context.getPatientService().getPatientIdentifierTypeByUuid(SOCIAL_HEALTH_AUTHORITY_IDENTIFICATION_NUMBER);
            if(shaIdentifierType != null) {
                PatientIdentifier shaObject = patient.getPatientIdentifier(shaIdentifierType);

                if(shaObject != null) {
                    String shaNumber = shaObject.getIdentifier();
                    System.out.println("Insurance Claims: Got Patient SHA ID number as: " + shaNumber);
                    return(shaNumber);
                } else {
                    System.err.println("Insurance Claims: Error Getting Patient SHA ID number: null PatientIdentifier");
                }
            } else {
                System.err.println("Insurance Claims: Error Getting Patient SHA ID number: null PatientIdentifierType");
            }
        } else {
             System.err.println("Insurance Claims: Error Getting Patient SHA ID number: null patient");
        }

        return(ret);
    }

    /**
     * Get the National ID of patient
     * @return
     */
    public static String getPatientsNationalID(Patient patient) {
        String ret = "";
        if(patient != null) {

            PatientIdentifierType shaIdentifierType = Context.getPatientService().getPatientIdentifierTypeByUuid(PATIENT_HIE_NATIONAL_ID);
            if(shaIdentifierType != null) {
                PatientIdentifier shaObject = patient.getPatientIdentifier(shaIdentifierType);

                if(shaObject != null) {
                    String shaNumber = shaObject.getIdentifier();
                    System.out.println("Insurance Claims: Got Patient SHA ID number as: " + shaNumber);
                    return(shaNumber);
                } else {
                    System.err.println("Insurance Claims: Error Getting Patient SHA ID number: null PatientIdentifier");
                }
            } else {
                System.err.println("Insurance Claims: Error Getting Patient SHA ID number: null PatientIdentifierType");
            }
        } else {
             System.err.println("Insurance Claims: Error Getting Patient SHA ID number: null patient");
        }

        return(ret);
    }

    /**
     * Get the Tel Number of patient
     * @return
     */
    public static String getPatientsPhoneNumber(Patient patient) {
        String ret = "";
        if(patient != null) {
            PersonAttributeType personAttributeType = Context.getPersonService().getPersonAttributeTypeByUuid(PATIENT_HIE_TELEPHONE_CONTACT);
            if(personAttributeType != null) {
                PersonAttribute personAttribute = patient.getPerson().getAttributes(personAttributeType)
                .stream()
                .filter(attr -> attr.getAttributeType().equals(personAttributeType))
                .findFirst()
                .orElse(null);

                if(personAttribute != null) {
                    String patientTelNumber = personAttribute.getValue().toString();
                    System.out.println("Insurance Claims: Got Provider Reg number as: " + patientTelNumber);
                    return(patientTelNumber);
                } else {
                    System.err.println("Insurance Claims: Error Getting provider reg number: null providerAttribute");
                }
            } else {
                System.err.println("Insurance Claims: Error Getting Provider reg number: null ProviderAttributeType");
            }
        } else {
             System.err.println("Insurance Claims: Error Getting Provider reg number: null provider");
        }

        return(ret);
    }

    /**
     * Get the registration number of provider (practitioner)
     * @return
     */
    public static String getProviderLicenseNo(Provider provider) {
        String ret = "";
        if(provider != null) {
            ProviderAttributeType providerAttributeType = Context.getProviderService().getProviderAttributeTypeByUuid(PROVIDER_LICENSE_NUMBER);
            if(providerAttributeType != null) {
                ProviderAttribute providerAttribute = provider.getActiveAttributes(providerAttributeType)
                .stream()
                .filter(attr -> attr.getAttributeType().equals(providerAttributeType))
                .findFirst()
                .orElse(null);

                if(providerAttribute != null) {
                    String providerRegNumber = providerAttribute.getValue().toString();
                    System.out.println("Insurance Claims: Got Provider Reg number as: " + providerRegNumber);
                    return(providerRegNumber);
                } else {
                    System.err.println("Insurance Claims: Error Getting provider reg number: null providerAttribute");
                }
            } else {
                System.err.println("Insurance Claims: Error Getting Provider reg number: null ProviderAttributeType");
            }
        } else {
             System.err.println("Insurance Claims: Error Getting Provider reg number: null provider");
        }

        return(ret);
    }

    /**
     * Get the license number of facility (location or organization)
     * @return
     */
    public static String getLocationLicenseNo() {
        String ret = "";
        Location curLocation = getDefaultLocation();
        if(curLocation != null) {
            LocationAttributeType locationAttributeType = Context.getLocationService().getLocationAttributeTypeByUuid(FACILITY_LICENSE_NUMBER);
            if(locationAttributeType != null) {
                LocationAttribute locationAttribute = curLocation.getActiveAttributes(locationAttributeType)
                .stream()
                .filter(attr -> attr.getAttributeType().equals(locationAttributeType))
                .findFirst()
                .orElse(null);

                if(locationAttribute != null) {
                    String locationRegNumber = locationAttribute.getValue().toString();
                    System.out.println("Insurance Claims: Got Location Reg number as: " + locationRegNumber);
                    return(locationRegNumber);
                } else {
                    System.err.println("Insurance Claims: Error Getting location reg number: null locationAttribute");
                }
            } else {
                System.err.println("Insurance Claims: Error Getting location reg number: null locationAttributeType");
            }
        } else {
             System.err.println("Insurance Claims: Error Getting location reg number: null location");
        }

        return(ret);
    }

    /**
     * Get the registry code id of facility (location or organization)
     * @return
     */
    public static String getLocationRegistryId() {
        String ret = "";
        Location curLocation = getDefaultLocation();
        if(curLocation != null) {
            LocationAttributeType locationAttributeType = Context.getLocationService().getLocationAttributeTypeByUuid(FACILITY_REGISTRY_CODE);
            if(locationAttributeType != null) {
                LocationAttribute locationAttribute = curLocation.getActiveAttributes(locationAttributeType)
                .stream()
                .filter(attr -> attr.getAttributeType().equals(locationAttributeType))
                .findFirst()
                .orElse(null);

                if(locationAttribute != null) {
                    String locationRegNumber = locationAttribute.getValue().toString();
                    System.out.println("Insurance Claims: Got Location Reg number as: " + locationRegNumber);
                    return(locationRegNumber);
                } else {
                    System.err.println("Insurance Claims: Error Getting location reg number: null locationAttribute");
                }
            } else {
                System.err.println("Insurance Claims: Error Getting location reg number: null locationAttributeType");
            }
        } else {
             System.err.println("Insurance Claims: Error Getting location reg number: null location");
        }

        return(ret);
    }

    /**
     * Get the current location
     * @return
     */
    public static Location getDefaultLocation() {
        Location location = null;
        try {
            Context.addProxyPrivilege(PrivilegeConstants.GET_LOCATIONS);
            Context.addProxyPrivilege(PrivilegeConstants.GET_GLOBAL_PROPERTIES);
            String GP_DEFAULT_LOCATION = "kenyaemr.defaultLocation";
            GlobalProperty gp = Context.getAdministrationService().getGlobalPropertyObject(GP_DEFAULT_LOCATION);
            location = gp != null ? (Location)gp.getValue() : null;
            if( location != null ) {
                System.out.println("Insurance Claims: Got Local Location ID as: " + location.getId());
            }
        } catch (Exception ex) {
            System.err.println("Insurance Claims: Error Getting local location: " + ex.getMessage());
            ex.printStackTrace();
        }
        finally {
            Context.removeProxyPrivilege(PrivilegeConstants.GET_LOCATIONS);
            Context.removeProxyPrivilege(PrivilegeConstants.GET_GLOBAL_PROPERTIES);
        }

        return location;
    }

    /**
     * Gets the practitioner/provider FHIR payload from the provider attributes
     * @return
     */
    public static Practitioner getSavedProviderFHIRPayload(Provider provider) {
        Practitioner ret = null;
        String jsonProvider = null;

        if(provider != null) {
            ProviderAttributeType providerAttributeType = Context.getProviderService().getProviderAttributeTypeByUuid(PROVIDER_HIE_FHIR_REFERENCE);
            if(providerAttributeType != null) {
                ProviderAttribute providerAttribute = provider.getActiveAttributes(providerAttributeType)
                .stream()
                .filter(attr -> attr.getAttributeType().equals(providerAttributeType))
                .findFirst()
                .orElse(null);

                if(providerAttribute != null) {
                    String providerFHIRPayload = providerAttribute.getValue().toString();
                    System.out.println("Insurance Claims: Got Provider FHIR payload as: " + providerFHIRPayload);
                    jsonProvider = providerFHIRPayload;
                } else {
                    System.err.println("Insurance Claims: Error Getting provider FHIR payload: null providerAttribute");
                }
            } else {
                System.err.println("Insurance Claims: Error Getting Provider FHIR payload: null ProviderAttributeType");
            }
        } else {
             System.err.println("Insurance Claims: Error Getting Provider FHIR payload: null provider");
        }

        if(jsonProvider != null) {
            // de-serialize the object
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                ret = objectMapper.readValue(jsonProvider, Practitioner.class);
            } catch(Exception ex) {
                System.err.println("Insurance Claims: Error Getting provider FHIR payload " + ex.getMessage());
                ex.printStackTrace();
            }
        }

        return(ret);
    }

    /**
     * Gets the organization/location/facility FHIR payload from the location attributes
     * @return
     */
    public static Organization getSavedLocationFHIRPayload() {
        Organization ret = null;
        String jsonOrganization = null;

        Location curLocation = getDefaultLocation();
        if(curLocation != null) {
            LocationAttributeType locationAttributeType = Context.getLocationService().getLocationAttributeTypeByUuid(LOCATION_HIE_FHIR_REFERENCE);
            if(locationAttributeType != null) {
                LocationAttribute locationAttribute = curLocation.getActiveAttributes(locationAttributeType)
                .stream()
                .filter(attr -> attr.getAttributeType().equals(locationAttributeType))
                .findFirst()
                .orElse(null);

                if(locationAttribute != null) {
                    String locationFHIRPayload = locationAttribute.getValue().toString();
                    System.out.println("Insurance Claims: Got Location FHIR Payload as: " + locationFHIRPayload);
                    jsonOrganization = locationFHIRPayload;
                } else {
                    System.err.println("Insurance Claims: Error Getting location FHIR Payload: null locationAttribute");
                }
            } else {
                System.err.println("Insurance Claims: Error Getting location FHIR Payload: null locationAttributeType");
            }
        } else {
             System.err.println("Insurance Claims: Error Getting location FHIR Payload: null location");
        }

        if(jsonOrganization != null) {
            // de-serialize the object
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                ret = objectMapper.readValue(jsonOrganization, Organization.class);
            } catch(Exception ex) {
                System.err.println("Insurance Claims: Error Getting location FHIR payload " + ex.getMessage());
                ex.printStackTrace();
            }
        }

        return(ret);
    }

    /**
     * Removes any trailing slash from a URL
     * @param input
     * @return
     */
    public static String removeTrailingSlash(String input) {
        if (input == null || input.isEmpty()) {
            return input; // Return the input as-is if it's null or empty
        }
        return input.endsWith("/") ? input.substring(0, input.length() - 1) : input;
    }

    /**
     * Returns the patients full name
     * @param patient
     * @return
     */
    public static String getPatientsFullName(Patient patient) {
        String ret = "";

        try {
            // String patientFullName = patient.getGivenName() + " " + patient.getMiddleName() + " " + patient.getFamilyName();
            String patientFullName = patient.getGivenName() != null ? patient.getGivenName() : "";
            patientFullName = patientFullName != null ? patientFullName + " " : patientFullName;
            patientFullName = patient.getMiddleName() != null ? patientFullName + patient.getMiddleName() : patientFullName;
            patientFullName = patient.getFamilyName() != null ? patientFullName + " " + patient.getFamilyName() : patientFullName;
            patientFullName = patientFullName.trim();
            ret = patientFullName;
        } catch(Exception ex) {
            System.err.println("Insurance Claims: Error creating patient full name " + ex.getMessage());
            ex.printStackTrace();
        }

        return ret;
    }
}

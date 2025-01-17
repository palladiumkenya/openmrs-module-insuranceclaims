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

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.openmrs.module.insuranceclaims.api.service.fhir.util.InsuranceClaimConstants.SOCIAL_HEALTH_AUTHORITY_IDENTIFICATION_NUMBER;
import static org.openmrs.module.insuranceclaims.api.service.fhir.util.InsuranceClaimConstants.PROVIDER_LICENSE_NUMBER;
import static org.openmrs.module.insuranceclaims.api.service.fhir.util.InsuranceClaimConstants.LOCATION_LICENSE_NUMBER;
import static org.openmrs.module.insuranceclaims.api.service.fhir.util.InsuranceClaimConstants.PROVIDER_HIE_FHIR_REFERENCE;
import static org.openmrs.module.insuranceclaims.api.service.fhir.util.InsuranceClaimConstants.LOCATION_HIE_FHIR_REFERENCE;

public class GeneralUtil {

    
    /**
     * Gets the HIE JWT auth token
     * @return
     * @throws IOException
     */
    public static String getAuthToken() throws IOException {
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
     * Get the registration number of provider (practitioner)
     * @return
     */
    public static String getProviderRegNo(Provider provider) {
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
     * Get the registration number of facility (location or organization)
     * @return
     */
    public static String getLocationRegNo() {
        String ret = "";
        Location curLocation = getDefaultLocation();
        if(curLocation != null) {
            LocationAttributeType locationAttributeType = Context.getLocationService().getLocationAttributeTypeByUuid(LOCATION_LICENSE_NUMBER);
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
        Location var2;
        try {
            Context.addProxyPrivilege(PrivilegeConstants.GET_LOCATIONS);
            Context.addProxyPrivilege(PrivilegeConstants.GET_GLOBAL_PROPERTIES);
            String GP_DEFAULT_LOCATION = "kenyaemr.defaultLocation";
            GlobalProperty gp = Context.getAdministrationService().getGlobalPropertyObject(GP_DEFAULT_LOCATION);
            var2 = gp != null ? (Location)gp.getValue() : null;
        } finally {
            Context.removeProxyPrivilege(PrivilegeConstants.GET_LOCATIONS);
            Context.removeProxyPrivilege(PrivilegeConstants.GET_GLOBAL_PROPERTIES);
        }

        return var2;
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
}

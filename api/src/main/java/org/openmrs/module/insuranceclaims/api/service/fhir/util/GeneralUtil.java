package org.openmrs.module.insuranceclaims.api.service.fhir.util;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Practitioner;
import org.openmrs.Encounter;
import org.openmrs.EncounterProvider;
import org.openmrs.GlobalProperty;
import org.openmrs.Location;
import org.openmrs.LocationAttribute;
import org.openmrs.LocationAttributeType;
import org.openmrs.Obs;
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
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.openmrs.module.insuranceclaims.api.service.fhir.util.InsuranceClaimConstants.*;

public class GeneralUtil {


	/**
	 * Gets the HIE JWT auth token
	 *
	 * @return
	 * @throws IOException
	 */
	public static String getJWTAuthToken() throws IOException {
		String ret = null;
		try {
			OkHttpClient client = new OkHttpClient();
			String hieJwtAuthMode = Context.getAdministrationService().getGlobalProperty("kenyaemr.sha.jwt.auth.mode");
			String auth = Context.getAdministrationService().getGlobalProperty("insuranceclaims.hiejwt.custom.encodedpass");
			String hieJwtUrl = Context.getAdministrationService().getGlobalProperty("insuranceclaims.hiejwt.url");
			if (hieJwtAuthMode == null || hieJwtAuthMode.trim().isEmpty()) {
				System.out.println("Insurance Claims Module: ERROR: Jwt Auth mode  configs not updated: ");
			}
			if (auth == null || auth.trim().isEmpty()) {
				System.out.println("Insurance Claims Module: ERROR: Jwt encoded auths configs not updated: ");
			}
			if (hieJwtUrl == null || hieJwtUrl.trim().isEmpty()) {
				System.out.println("Insurance Claims Module: ERROR: Jwt token url configs not updated: ");
			}

			//Config to toggle GET and POST requests
			if (hieJwtAuthMode.trim().equalsIgnoreCase("get")) {
				Request request = new Request.Builder()
					.url(hieJwtUrl)
					.header("Authorization", "Basic " + auth)
					.build();
				Response response = client.newCall(request).execute();
				if (!response.isSuccessful()) {
					System.err.println("Insurance Claims Module: Get HIE Auth: ERROR: Request failed: " + response.code() + " - " + response.message());
				} else {
					return response.body().string();
				}
			} else if (hieJwtAuthMode.trim().equalsIgnoreCase("post")) {
				// Build the POST request
				System.out.println("Insurance Claims Module: Auth mode is post: ");
				okhttp3.MediaType mediaType = okhttp3.MediaType.parse("text/plain");
				okhttp3.RequestBody body = okhttp3.RequestBody.create(mediaType, "");
				Request postRequest = new Request.Builder()
					.url(hieJwtUrl)
					.method("POST", body)
					.header("Authorization", "Basic " + auth)
					.header("Cookie", "incap_ses_6550_2912339=Gt0ldtSEr3gzhyyAnUbmWsc39mcAAAAAeRlLAEJa78AYqRNx3TRw5A==; visid_incap_2912339=cOIlO1QSR62/Wo6Ega29z/TjjWcAAAAAQUIPAAAAAAB6TxV9uQ87Ev365z8yUqhP")
					.build();
				Response postResponse = client.newCall(postRequest).execute();
				if (!postResponse.isSuccessful()) {
					System.err.println("Insurance Claims Module: Get HIE Post Auth: ERROR: Request failed: " + postResponse.code() + " - " + postResponse.message());
				} else {
					String payload = postResponse.body().string();
					System.out.println("Insurance Claims Module: Got HIE Post Auth token payload: " + payload);
					com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
					com.fasterxml.jackson.databind.JsonNode rootNode = objectMapper.readTree(payload);
					ret = rootNode.path("access_token").asText();
				}
			}

		} catch (Exception ex) {
			System.err.println("Insurance Claims Module: Get HIE Auth: ERROR: Request failed: " + ex.getMessage());
			ex.printStackTrace();
		}
		return (ret);
	}

	/**
	 * Gets the HIE Staging auth token
	 *
	 * @return
	 * @throws IOException
	 */
	public static String getHIEStagingAuthToken() throws IOException {
		String ret = null;
		try {
			OkHttpClient client = new OkHttpClient();

			String auth = Context.getAdministrationService().getGlobalProperty("insuranceclaims.hiestaging.custom.encodedpass");
			String hieStagingAuthUrl = Context.getAdministrationService().getGlobalProperty("insuranceclaims.hiestaging.auth.url");
			if (auth != null && hieStagingAuthUrl != null && StringUtils.isNotEmpty(auth) && StringUtils.isNotEmpty(hieStagingAuthUrl)) {
				System.out.println("Insurance Claims Module: Get HIE Staging Auth token URL: " + hieStagingAuthUrl + " Auth: " + auth);
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
					System.err.println("Insurance Claims Module: Get HIE Staging Auth: ERROR: Request failed: " + response.code() + " - " + response.message());
				} else {
					String payload = response.body().string();
					System.out.println("Insurance Claims Module: Got HIE Staging Auth token payload: " + payload);
					ObjectMapper objectMapper = new ObjectMapper();
					JsonNode rootNode = objectMapper.readTree(payload);
					return rootNode.path("access_token").asText();
				}
			} else {
				System.err.println("Insurance Claims Module: Get HIE Staging Auth: ERROR: Request failed: The global properties for hie Staging must be set");
			}
		} catch (Exception ex) {
			System.err.println("Insurance Claims Module: Get HIE Staging Auth: ERROR: Request failed: " + ex.getMessage());
			ex.printStackTrace();
		}
		return (ret);
	}

	/**
	 * Gets the Apiero ApiKey auth token
	 *
	 * @return
	 * @throws IOException
	 */
	public static String getApiKeyAuthToken() throws IOException {
		String auth = Context.getAdministrationService().getGlobalProperty("insuranceclaims.apiero.apikey");
		return auth;
	}

	/**
	 * Gets the full URL and resource base URL property from globals
	 *
	 * @return
	 * @throws IOException
	 */
	public static String getBaseURLForResourceAndFullURL() throws IOException {
		String auth = Context.getAdministrationService().getGlobalProperty("insuranceclaims.base.resource.url");
		return auth;
	}

	/**
	 * Checks if claims automation is enabled in the global properties to send claims automatically upon checkout
	 * @return
	 */
	public static Boolean getClaimAutomationEnabled() {
		Boolean ret = false;
		String claimsEnabled = Context.getAdministrationService().getGlobalProperty("insuranceclaims.claims.automation.enabled");
		if(claimsEnabled != null && claimsEnabled.trim().equalsIgnoreCase("true")) {
			ret = true;
		}
		return(ret);
	}

	/**
	 * Gets the current location id
	 * @return
	 */
	public static Integer getCurrentLocationId() {
		Location location = Context.getLocationService().getDefaultLocation();
		if (location != null) {
			return location.getLocationId();  // returns Integer ID
		}
		return null;
	}

	/**
	 * Returns the provider for an encounter
	 * @param encounter
	 * @return
	 */
	public static Provider getProviderForEncounter(Encounter encounter) {
		if (encounter == null) {
			return null;
		}

		// Each encounter can have multiple providers, linked with encounter roles
		Set<EncounterProvider> encounterProviders = encounter.getEncounterProviders();
		if (!encounterProviders.isEmpty()) {
			// Just return the first one, or filter by role if you need a specific type (e.g. "Clinician")
			return encounterProviders.iterator().next().getProvider();
		}

		return null;
	}

	/**
	 * Get the CR number of patient
	 *
	 * @return
	 */
	public static String getPatientCRNo(Patient patient) {
		String ret = "";
		if (patient != null) {

			PatientIdentifierType CRIdentifierType = Context.getPatientService().getPatientIdentifierTypeByUuid(SOCIAL_HEALTH_INSURANCE_NUMBER);
			if (CRIdentifierType != null) {
				PatientIdentifier CRObject = patient.getPatientIdentifier(CRIdentifierType);

				if (CRObject != null) {
					String CRNumber = CRObject.getIdentifier();
					System.out.println("Insurance Claims: Got Patient CR number as: " + CRNumber);
					return (CRNumber);
				} else {
					System.err.println("Insurance Claims: Error Getting Patient CR number: null PatientIdentifier");
				}
			} else {
				System.err.println("Insurance Claims: Error Getting Patient CR number: null PatientIdentifierType");
			}
		} else {
			System.err.println("Insurance Claims: Error Getting Patient CR number: null patient");
		}

		return (ret);
	}

	/**
	 * Get the SHA number of patient
	 *
	 * @return
	 */
	public static String getPatientSHANo(Patient patient) {
		String ret = "";
		if (patient != null) {

			PatientIdentifierType shaIdentifierType = Context.getPatientService().getPatientIdentifierTypeByUuid(SOCIAL_HEALTH_AUTHORITY_IDENTIFICATION_NUMBER);
			if (shaIdentifierType != null) {
				PatientIdentifier shaObject = patient.getPatientIdentifier(shaIdentifierType);

				if (shaObject != null) {
					String shaNumber = shaObject.getIdentifier();
					System.out.println("Insurance Claims: Got Patient SHA ID number as: " + shaNumber);
					return (shaNumber);
				} else {
					System.err.println("Insurance Claims: Error Getting Patient SHA ID number: null PatientIdentifier");
				}
			} else {
				System.err.println("Insurance Claims: Error Getting Patient SHA ID number: null PatientIdentifierType");
			}
		} else {
			System.err.println("Insurance Claims: Error Getting Patient SHA ID number: null patient");
		}

		return (ret);
	}

	/**
	 * Get the National ID of patient
	 *
	 * @return
	 */
	public static String getPatientsNationalID(Patient patient) {
		String ret = "";
		if (patient != null) {

			PatientIdentifierType shaIdentifierType = Context.getPatientService().getPatientIdentifierTypeByUuid(PATIENT_HIE_NATIONAL_ID);
			if (shaIdentifierType != null) {
				PatientIdentifier shaObject = patient.getPatientIdentifier(shaIdentifierType);

				if (shaObject != null) {
					String shaNumber = shaObject.getIdentifier();
					System.out.println("Insurance Claims: Got Patient SHA ID number as: " + shaNumber);
					return (shaNumber);
				} else {
					System.err.println("Insurance Claims: Error Getting Patient SHA ID number: null PatientIdentifier");
				}
			} else {
				System.err.println("Insurance Claims: Error Getting Patient SHA ID number: null PatientIdentifierType");
			}
		} else {
			System.err.println("Insurance Claims: Error Getting Patient SHA ID number: null patient");
		}

		return (ret);
	}

	/**
	 * Get the Tel Number of patient
	 *
	 * @return
	 */
	public static String getPatientsPhoneNumber(Patient patient) {
		String ret = "";
		if (patient != null) {
			PersonAttributeType personAttributeType = Context.getPersonService().getPersonAttributeTypeByUuid(PATIENT_HIE_TELEPHONE_CONTACT);
			if (personAttributeType != null) {
				PersonAttribute personAttribute = patient.getPerson().getAttributes(personAttributeType)
					.stream()
					.filter(attr -> attr.getAttributeType().equals(personAttributeType))
					.findFirst()
					.orElse(null);

				if (personAttribute != null) {
					String patientTelNumber = personAttribute.getValue().toString();
					System.out.println("Insurance Claims: Got patient tel number as: " + patientTelNumber);
					return (patientTelNumber);
				} else {
					System.err.println("Insurance Claims: Error Getting patient tel number: null personAttribute");
				}
			} else {
				System.err.println("Insurance Claims: Error Getting patient tel number: null PersonAttributeType");
			}
		} else {
			System.err.println("Insurance Claims: Error Getting patient tel number: null patient");
		}

		return (ret);
	}

	/**
	 * Get the registration number of provider (practitioner)
	 *
	 * @return
	 */
	public static String getProviderLicenseNo(Provider provider) {
		String ret = "";
		if (provider != null) {
			ProviderAttributeType providerAttributeType = Context.getProviderService().getProviderAttributeTypeByUuid(PROVIDER_LICENSE_NUMBER);
			if (providerAttributeType != null) {
				ProviderAttribute providerAttribute = provider.getActiveAttributes(providerAttributeType)
					.stream()
					.filter(attr -> attr.getAttributeType().equals(providerAttributeType))
					.findFirst()
					.orElse(null);

				if (providerAttribute != null) {
					String providerRegNumber = providerAttribute.getValue().toString();
					System.out.println("Insurance Claims: Got Provider Reg number as: " + providerRegNumber);
					return (providerRegNumber);
				} else {
					System.err.println("Insurance Claims: Error Getting provider reg number: null providerAttribute");
				}
			} else {
				System.err.println("Insurance Claims: Error Getting Provider reg number: null ProviderAttributeType");
			}
		} else {
			System.err.println("Insurance Claims: Error Getting Provider reg number: null provider");
		}

		return (ret);
	}

	/**
	 * Get the license number of facility (location or organization)
	 *
	 * @return
	 */
	public static String getLocationLicenseNo() {
		String ret = "";
		Location curLocation = getDefaultLocation();
		if (curLocation != null) {
			LocationAttributeType locationAttributeType = Context.getLocationService().getLocationAttributeTypeByUuid(FACILITY_LICENSE_NUMBER);
			if (locationAttributeType != null) {
				LocationAttribute locationAttribute = curLocation.getActiveAttributes(locationAttributeType)
					.stream()
					.filter(attr -> attr.getAttributeType().equals(locationAttributeType))
					.findFirst()
					.orElse(null);

				if (locationAttribute != null) {
					String locationRegNumber = locationAttribute.getValue().toString();
					System.out.println("Insurance Claims: Got Location Reg number as: " + locationRegNumber);
					return (locationRegNumber);
				} else {
					System.err.println("Insurance Claims: Error Getting location reg number: null locationAttribute");
				}
			} else {
				System.err.println("Insurance Claims: Error Getting location reg number: null locationAttributeType");
			}
		} else {
			System.err.println("Insurance Claims: Error Getting location reg number: null location");
		}

		return (ret);
	}

	/**
	 * Get the registry code id of facility (location or organization)
	 *
	 * @return
	 */
	public static String getLocationRegistryId() {
		String ret = "";
		try {
			Context.addProxyPrivilege(PrivilegeConstants.GET_LOCATIONS);
			Context.addProxyPrivilege(PrivilegeConstants.GET_GLOBAL_PROPERTIES);
			String GP_HIE_REGISTRY_CODE = "kenyaemr.hie.facility.registry.code";
			GlobalProperty gp = Context.getAdministrationService().getGlobalPropertyObject(GP_HIE_REGISTRY_CODE);		
			if (gp.getValue() != null) {
				System.out.println("Insurance Claims: Got Local Facility Registry Code ID as: " + gp.getValue().toString());
				ret=gp.getValue().toString();
			}
		} catch (Exception ex) {
			System.err.println("Insurance Claims: Error Getting Facility Registry Code: " + ex.getMessage());
			ex.printStackTrace();
		} finally {
				Context.removeProxyPrivilege(PrivilegeConstants.GET_GLOBAL_PROPERTIES);
		}
		return (ret);
	}

	/**
	 * Get the current location
	 *
	 * @return
	 */
	public static Location getDefaultLocation() {
		Location location = null;
		try {
			Context.addProxyPrivilege(PrivilegeConstants.GET_LOCATIONS);
			Context.addProxyPrivilege(PrivilegeConstants.GET_GLOBAL_PROPERTIES);
			String GP_DEFAULT_LOCATION = "kenyaemr.defaultLocation";
			GlobalProperty gp = Context.getAdministrationService().getGlobalPropertyObject(GP_DEFAULT_LOCATION);
			location = gp != null ? (Location) gp.getValue() : null;
			if (location != null) {
				System.out.println("Insurance Claims: Got Local Location ID as: " + location.getId());
			}
		} catch (Exception ex) {
			System.err.println("Insurance Claims: Error Getting local location: " + ex.getMessage());
			ex.printStackTrace();
		} finally {
			Context.removeProxyPrivilege(PrivilegeConstants.GET_LOCATIONS);
			Context.removeProxyPrivilege(PrivilegeConstants.GET_GLOBAL_PROPERTIES);
		}

		return location;
	}

	/**
	 * Gets the practitioner/provider FHIR payload from the provider attributes
	 *
	 * @return
	 */
	public static Practitioner getSavedProviderFHIRPayload(Provider provider) {
		Practitioner ret = null;
		String jsonProvider = null;

		if (provider != null) {
			ProviderAttributeType providerAttributeType = Context.getProviderService().getProviderAttributeTypeByUuid(PROVIDER_HIE_FHIR_REFERENCE);
			if (providerAttributeType != null) {
				ProviderAttribute providerAttribute = provider.getActiveAttributes(providerAttributeType)
					.stream()
					.filter(attr -> attr.getAttributeType().equals(providerAttributeType))
					.findFirst()
					.orElse(null);

				if (providerAttribute != null) {
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

		if (jsonProvider != null) {
			// de-serialize the object
			ObjectMapper objectMapper = new ObjectMapper();
			try {
				ret = objectMapper.readValue(jsonProvider, Practitioner.class);
			} catch (Exception ex) {
				System.err.println("Insurance Claims: Error Getting provider FHIR payload " + ex.getMessage());
				ex.printStackTrace();
			}
		}

		return (ret);
	}

	/**
	 * Gets the organization/location/facility FHIR payload from the location attributes
	 *
	 * @return
	 */
	public static Organization getSavedLocationFHIRPayload() {
		Organization ret = null;
		String jsonOrganization = null;

		Location curLocation = getDefaultLocation();
		if (curLocation != null) {
			LocationAttributeType locationAttributeType = Context.getLocationService().getLocationAttributeTypeByUuid(LOCATION_HIE_FHIR_REFERENCE);
			if (locationAttributeType != null) {
				LocationAttribute locationAttribute = curLocation.getActiveAttributes(locationAttributeType)
					.stream()
					.filter(attr -> attr.getAttributeType().equals(locationAttributeType))
					.findFirst()
					.orElse(null);

				if (locationAttribute != null) {
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

		if (jsonOrganization != null) {
			// de-serialize the object
			ObjectMapper objectMapper = new ObjectMapper();
			try {
				ret = objectMapper.readValue(jsonOrganization, Organization.class);
			} catch (Exception ex) {
				System.err.println("Insurance Claims: Error Getting location FHIR payload " + ex.getMessage());
				ex.printStackTrace();
			}
		}

		return (ret);
	}

	/**
	 * Removes any trailing slash from a URL
	 *
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
	 *
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
		} catch (Exception ex) {
			System.err.println("Insurance Claims: Error creating patient full name " + ex.getMessage());
			ex.printStackTrace();
		}

		return ret;
	}
	/**
	 * Get the Unique provider identifier PUID (practitioner)
	 *
	 * @return
	 */
	public static String getProviderUniqueIdentifierNo(Provider provider) {
		String ret = "";
		if (provider != null) {
			ProviderAttributeType providerAttributeType = Context.getProviderService().getProviderAttributeTypeByUuid(PROVIDER_UNIQUE_IDENTIFIER);
			if (providerAttributeType != null) {
				ProviderAttribute providerAttribute = provider.getActiveAttributes(providerAttributeType)
					.stream()
					.filter(attr -> attr.getAttributeType().equals(providerAttributeType))
					.findFirst()
					.orElse(null);

				if (providerAttribute != null) {
					String providerUniqueIdentifier = providerAttribute.getValue().toString();
					System.out.println("Insurance Claims: Got Provider Unique Identifier as: " + providerUniqueIdentifier);
					return (providerUniqueIdentifier);
				} else {
					System.err.println("Insurance Claims: Error Getting provider unique identifier: null providerAttribute");
				}
			} else {
				System.err.println("Insurance Claims: Error Getting Provider unique identifier: null ProviderAttributeType");
			}
		} else {
			System.err.println("Insurance Claims: Error Getting Provider unique identifier: null provider");
		}

		return (ret);
	}

}

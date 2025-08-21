package org.openmrs.module.insuranceclaims.advice;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Enumerations.AdministrativeGender;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.Reference;
import org.openmrs.Concept;
import org.openmrs.Diagnosis;
import org.openmrs.Encounter;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.Person;
import org.openmrs.PersonAddress;
import org.openmrs.PersonName;
import org.openmrs.Provider;
import org.openmrs.Relationship;
import org.openmrs.RelationshipType;
import org.openmrs.Visit;
import org.openmrs.VisitAttributeType;
import org.openmrs.api.DiagnosisService;
import org.openmrs.api.ProviderService;
import org.openmrs.api.VisitService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.Daemon;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.translators.LocationTranslator;
import org.openmrs.module.fhir2.api.translators.PatientTranslator;
import org.openmrs.module.insuranceclaims.api.model.InsuranceClaim;
import org.openmrs.module.insuranceclaims.api.model.InsuranceClaimStatus;
import org.openmrs.module.insuranceclaims.api.service.fhir.util.GeneralUtil;
import org.openmrs.module.insuranceclaims.api.service.request.ExternalApiRequest;
import org.openmrs.util.PrivilegeConstants;
import org.springframework.aop.AfterReturningAdvice;

import ca.uhn.fhir.context.FhirContext;

import org.openmrs.module.insuranceclaims.forms.ClaimFormService;
import org.openmrs.module.insuranceclaims.forms.NewClaimForm;

/**
 * Detects when a new visit has ended and creates a claim
 */
public class CreateClaimOnCheckout implements AfterReturningAdvice {
	
	@Override
	public void afterReturning(Object returnValue, Method method, Object[] args, Object target) throws Throwable {

		try {
			if(GeneralUtil.getClaimAutomationEnabled() == true) {
				if (method.getName().equals("saveVisit") && args.length > 0 && args[0] instanceof Visit) {
					
					Visit visit = (Visit) args[0];
					
					// check visit info and only process checkouts
					if (visit != null && visit.getStopDatetime() != null) {
						System.out.println("Insurance Claims Module: Visit not processed yet. Now processing");

						System.out.println("Insurance Claims Module: Visit End Date: " + visit.getStopDatetime());
						System.out.println("Insurance Claims Module: Visit UUID: " + visit.getUuid());
						System.out.println("Insurance Claims Module: Visit Date Changed: " + visit.getDateChanged());
						Patient patient = visit.getPatient();
						
						if (patient != null) {
							System.out.println("Insurance Claims Module: A patient was checked out. Checking for diagnosis");
							Boolean diagnosisFound = false;
							String encounterUuid = "";
							String providerUuid = "";
							List<String> diagnosesInEncounter = new ArrayList<>();
							VisitService visitService = Context.getVisitService();
							Set<Encounter> encounters = visit.getEncounters();
							DiagnosisService diagnosisService = Context.getDiagnosisService();
							for(Encounter enc : encounters) {
								List<Diagnosis> diagnoses = diagnosisService.getDiagnosesByEncounter(enc, false, false);
								if(diagnoses != null && diagnoses.size() > 0) {
									for(Diagnosis diagnosis : diagnoses) {
										String diagName = "";
										if (diagnosis.getDiagnosis().getCoded() != null) {
											Concept concept = diagnosis.getDiagnosis().getCoded();
											// Get the preferred name (locale-sensitive)
											diagName = concept.getName().getName();
											diagnosesInEncounter.add(concept.getUuid());
										} else if (diagnosis.getDiagnosis().getNonCoded() != null) {
											diagName = diagnosis.getDiagnosis().getNonCoded();
										}
										System.out.println("Insurance Claims Module: Found diagnosis: " + diagName);
									}
									diagnosisFound = true;
									encounterUuid = enc.getUuid();
									Provider provider = GeneralUtil.getProviderForEncounter(enc);
									if(provider != null) {
										providerUuid = provider.getUuid();
										System.out.println("Insurance Claims Module: Got the provider uuid as: " + providerUuid);
									}
									break;
								}
							}
							if(diagnosisFound == true) {
								// We found a diagnosis
								System.out.println("Insurance Claims Module: This visit had a diagnosis. Prepare and send claim");
								// InsuranceClaim claim = new InsuranceClaim();
								// claim.setPatient(patient);
								// claim.setVisit(visit);
								// claim.setVisitType(visit.getVisitType());
								// claim.setStatus(InsuranceClaimStatus.ENTERED);
								NewClaimForm newClaimForm = new NewClaimForm();
								newClaimForm.setPatient(patient.getUuid());
								newClaimForm.setVisitType(visit.getVisitType().getUuid());
								newClaimForm.setVisitUuid(visit.getUuid());
								newClaimForm.setEncounterUuid(encounterUuid);
								newClaimForm.setUse("claim");
								Integer currentLocationId = GeneralUtil.getCurrentLocationId();
								if(currentLocationId != null) {
									System.out.println("Insurance Claims Module: Got the location id as: " + currentLocationId);
									newClaimForm.setLocation(currentLocationId.toString());
								}
								newClaimForm.setProvider(providerUuid);
								newClaimForm.setClaimJustification("consultation");
								newClaimForm.setClaimExplanation("consultation");
								newClaimForm.setGuaranteeId("auto");
								newClaimForm.setClaimCode("auto");
								newClaimForm.setDiagnoses(diagnosesInEncounter);
								List<String> interventions = new ArrayList<>();
								interventions.add("SHA-12-001"); // Change this in future for more interventions
								newClaimForm.setInterventions(interventions);

								ClaimFormService claimFormService = Context.getService(ClaimFormService.class);
								InsuranceClaim claim = claimFormService.createClaim(newClaimForm);

								// Sending claim to external server
								// Even if immediate sending fails, we can send later
								try {
									System.out.println("Insurance Claims Module: Attempting to send the claim");
									ExternalApiRequest externalApiRequest = Context.getService(ExternalApiRequest.class);
									externalApiRequest.sendClaimToExternalApi(claim);
								} catch (Exception ex) {
									System.err.println("Insurance Claims Module: Claim Sending Error: " + ex.getMessage());
									ex.printStackTrace();
								}

							} else {
								// We found a diagnosis
								System.out.println("Insurance Claims Module: This visit did NOT have a diagnosis");
							}
						} else {
							System.out.println("Insurance Claims Module: Error: No patient attached to the visit");
						}
					} else {
						System.out.println("Insurance Claims Module: Wonder Health: Not a new visit. We ignore.");
					}
				}
			} else {
				System.out.println("Insurance Claims Module: Automated claims is disabled");
			}
		}
		catch (Exception ex) {
			// if (debugMode)
				System.err.println("Insurance Claims Module: Error getting new patient: " + ex.getMessage());
			ex.printStackTrace();
		}
	}
	
	/**
	 * Prepare the claim
	 * 
	 * @param patient
	 * @return
	 */
	// private String preparePatientClaim(@NotNull Patient patient) {
	// 	String ret = "";
		
	// 	try {
	
	// 	}
	// 	catch (Exception ex) {
	// 		System.err.println("Insurance Claims Module: Error preparing claim: " + ex.getMessage());
	// 		ex.printStackTrace();
	// 	}
	// 	finally {
	// 		// Context.closeSession();
	// 	}
		
	// 	return (ret);
	// }
	
	/**
	 * A thread to free up the frontend
	 */
	// private class syncPatientRunnable implements Runnable {
		
	// 	String payload = "";
		
	// 	Patient patient = null;
		
	// 	Boolean debugMode = false;
		
	// 	public syncPatientRunnable(@NotNull String payload, @NotNull Patient patient) {
	// 		this.payload = payload;
	// 		this.patient = patient;
	// 	}
		
	// 	@Override
	// 	public void run() {
			
	// 		try {
	// 			
				
	// 		}
	// 		catch (Exception ex) {
	// 			if (debugMode)
	// 				System.err.println("rmsdataexchange Module: Error. Failed to send patient to Wonder Health: "
	// 				        + ex.getMessage());
	// 			ex.printStackTrace();
	// 		}
	// 		finally {
	// 			// Context.closeSession();
	// 		}
	// 	}
	// }
	
	/**
	 * Trust all certs
	 */
	public static void trustAllCerts() {
		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
			
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return null;
			}
			
			@Override
			public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
			}
			
			@Override
			public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
			}
		} };
		
		SSLContext sc = null;
		try {
			sc = SSLContext.getInstance("SSL");
		}
		catch (NoSuchAlgorithmException e) {
			System.out.println(e.getMessage());
		}
		try {
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
		}
		catch (KeyManagementException e) {
			System.out.println(e.getMessage());
		}
		HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		
		// Optional 
		// Create all-trusting host name verifier
		HostnameVerifier validHosts = new HostnameVerifier() {
			
			@Override
			public boolean verify(String arg0, SSLSession arg1) {
				return true;
			}
		};
		// All hosts will be valid
		HttpsURLConnection.setDefaultHostnameVerifier(validHosts);
		
	}
	
}

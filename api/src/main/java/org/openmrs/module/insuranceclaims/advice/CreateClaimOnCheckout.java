package org.openmrs.module.insuranceclaims.advice;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.validation.constraints.NotNull;

import org.openmrs.Concept;
import org.openmrs.Diagnosis;
import org.openmrs.Encounter;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.Visit;
import org.openmrs.api.DiagnosisService;
import org.openmrs.api.VisitService;
import org.openmrs.api.context.Context;
import org.openmrs.module.fhir2.api.translators.PatientTranslator;
import org.openmrs.module.insuranceclaims.api.model.InsuranceClaim;
import org.openmrs.module.insuranceclaims.api.service.fhir.util.GeneralUtil;
import org.openmrs.module.insuranceclaims.api.service.request.ExternalApiRequest;
// import org.openmrs.module.insuranceclaims.api.service.request.impl.ExternalApiRequestImpl;
import org.openmrs.module.insuranceclaims.forms.ClaimFormService;
import org.openmrs.module.insuranceclaims.forms.NewClaimForm;
import org.springframework.aop.AfterReturningAdvice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * Detects when a new visit has ended and creates a claim
 */
@Component
public class CreateClaimOnCheckout implements AfterReturningAdvice {
	private static Boolean debugMode = false;

	@Autowired
	@Qualifier("insuranceclaims.ExternalApiRequest")
    private ExternalApiRequest externalApiRequest;
	
	// public ExternalApiRequest getExternalApiRequest() {
	// 	return externalApiRequest;
	// }

	// public void setExternalApiRequest(ExternalApiRequest externalApiRequest) {
	// 	System.err.println("Insurance Claims Module: setting externalApiRequest: " + (externalApiRequest == null));
	// 	this.externalApiRequest = externalApiRequest;
	// }

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
							System.out.println("Insurance Claims Module: A patient was checked out. Checking for diagnosis: " + (externalApiRequest == null));
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
								newClaimForm.setStartDate(GeneralUtil.formatDate(new Date(), "yyyy-MM-dd"));
								newClaimForm.setEndDate(GeneralUtil.formatDate(new Date(), "yyyy-MM-dd"));
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

								// Send the claim in a thread
								// SendClaimRunnable runner = new SendClaimRunnable(claim);
								// Thread claimSender = new Thread(runner);
								// claimSender.start();
								// Daemon.runInDaemonThread(runner, InsuranceClaimsActivator.getDaemonToken());

								try {
									System.out.println("Insurance Claims Module: Now Attempting to send the claim");
									// ExternalApiRequest externalApiRequest = Context.getService(ExternalApiRequest.class);
									if(externalApiRequest == null) {
										System.out.println("Insurance Claims Module: Manually create ExternalApiRequest");
										externalApiRequest = Context.getRegisteredComponent("insuranceclaims.ExternalApiRequest", ExternalApiRequest.class);
										if(externalApiRequest == null) {
											System.err.println("Insurance Claims Module: All attempts failed to load ExternalApiRequest");
										}
									}
									externalApiRequest.sendClaimToExternalApi(claim);
								} catch (Exception ex) {
									System.err.println("Insurance Claims Module: Now with Claim Sending Error: " + ex.getMessage());
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
						System.out.println("Insurance Claims Module: Automation Error: Not a checkout. We ignore.");
					}
				}
			} else {
				if(debugMode == true) System.out.println("Insurance Claims Module: Automated claims is disabled. Not processing visit");
			}
		}
		catch (Exception ex) {
			// if (debugMode)
				System.err.println("Insurance Claims Module: Error creating automated claim: " + ex.getMessage());
			ex.printStackTrace();
		}
	}
	
	/**
	 * A thread to free up the frontend
	 */
	private class SendClaimRunnable implements Runnable {
		
		InsuranceClaim insuranceClaim = null;
		
		public SendClaimRunnable(@NotNull InsuranceClaim claim) {
			this.insuranceClaim = claim;
		}
		
		@Override
		public void run() {
			
			try {
				if(insuranceClaim != null) {
					// Sending claim to external server
					// Even if immediate sending fails, we can send later
					try {
						System.out.println("Insurance Claims Module: Thread Attempting to send the claim");
						// ExternalApiRequest externalApiRequest = Context.getService(ExternalApiRequest.class);
						if(externalApiRequest == null) {
							System.out.println("Insurance Claims Module: Manually create ExternalApiRequest");
							externalApiRequest = Context.getRegisteredComponent("insuranceclaims.ExternalApiRequest", ExternalApiRequest.class);
							if(externalApiRequest == null) {
								System.err.println("Insurance Claims Module: All attempts failed to load ExternalApiRequest");
							}
						}
						externalApiRequest.sendClaimToExternalApi(insuranceClaim);
					} catch (Exception ex) {
						System.err.println("Insurance Claims Module: Thread Claim Sending Error: " + ex.getMessage());
						ex.printStackTrace();
					}
				}
			}
			catch (Exception ex) {
				System.err.println("Insurance Claims Module: ERROR: " + ex.getMessage());
				ex.printStackTrace();
			}
		}
	}
	
}

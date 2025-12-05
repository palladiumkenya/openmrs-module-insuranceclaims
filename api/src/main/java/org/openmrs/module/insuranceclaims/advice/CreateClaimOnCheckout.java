package org.openmrs.module.insuranceclaims.advice;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.constraints.NotNull;

import org.openmrs.Concept;
import org.openmrs.Diagnosis;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Order;
import org.openmrs.OrderType;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.Visit;
import org.openmrs.VisitAttribute;
import org.openmrs.VisitAttributeType;
import org.openmrs.VisitType;
import org.openmrs.api.DiagnosisService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.OrderService;
import org.openmrs.api.VisitService;
import org.openmrs.api.context.Context;
import org.openmrs.module.insuranceclaims.api.model.InsuranceClaim;
import org.openmrs.module.insuranceclaims.api.service.fhir.util.GeneralUtil;
import org.openmrs.module.insuranceclaims.api.service.fhir.util.InsuranceClaimConstants;
import org.openmrs.module.insuranceclaims.api.service.request.ExternalApiRequest;
import org.openmrs.module.insuranceclaims.forms.ClaimFormService;
import org.openmrs.module.insuranceclaims.forms.ItemDetails;
import org.openmrs.module.insuranceclaims.forms.NewClaimForm;
import org.openmrs.module.insuranceclaims.forms.ProvidedItemInForm;
import org.openmrs.module.insuranceclaims.util.ClaimsUtils;
import org.openmrs.module.kenyaemr.cashier.api.IBillService;
import org.openmrs.module.kenyaemr.cashier.api.model.Bill;
import org.openmrs.module.kenyaemr.cashier.api.model.BillLineItem;
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

	@Override
	public void afterReturning(Object returnValue, Method method, Object[] args, Object target) throws Throwable {

		debugMode = ClaimsUtils.isClaimsLoggingEnabled();
		try {
			if(GeneralUtil.getClaimAutomationEnabled() == true) {
				if (method.getName().equals("saveVisit") && args.length > 0 && args[0] instanceof Visit) {
					
					Visit visit = (Visit) args[0];
					VisitService visitService = Context.getVisitService();
					VisitType outpatientVisitType = visitService.getVisitTypeByUuid(InsuranceClaimConstants.OUTPATIENT_VISIT_TYPE);
					VisitAttributeType paymentMethod = visitService.getVisitAttributeTypeByUuid(InsuranceClaimConstants.PAYMENT_METHOD_VISIT_ATTRIBUTE);

					// Check if the payment method for the visit is insurance
					Boolean isInsurancePaymentMode = false;
					for(VisitAttribute visitAttribute : visit.getActiveAttributes()) {
						if(visitAttribute.getAttributeType() == paymentMethod) {
							if(visitAttribute.getValueReference() != null && visitAttribute.getValueReference().trim().equalsIgnoreCase(InsuranceClaimConstants.INSURANCE_PAYMENT_MODE)) {
								if(debugMode) System.out.println("Insurance Claims Module: This visit has a payment mode of insurance");
								isInsurancePaymentMode = true;
							}
						}
					}

					if(!isInsurancePaymentMode) {
						if(debugMode) System.out.println("Insurance Claims Module: This visit has NO payment mode of insurance");
					}
					
					// check visit info and only process outpatient checkouts and only visits whose payment is marked as insurance
					if (visit != null && visit.getStopDatetime() != null && outpatientVisitType != null && visit.getVisitType() == outpatientVisitType && isInsurancePaymentMode) {
						if(debugMode) System.out.println("Insurance Claims Module: Visit not processed yet. Now processing");

						if(debugMode) System.out.println("Insurance Claims Module: Visit End Date: " + visit.getStopDatetime());
						if(debugMode) System.out.println("Insurance Claims Module: Visit UUID: " + visit.getUuid());
						if(debugMode) System.out.println("Insurance Claims Module: Visit Date Changed: " + visit.getDateChanged());
						Patient patient = visit.getPatient();
						
						if (patient != null) {
							if(debugMode) System.out.println("Insurance Claims Module: A patient was checked out. Checking for diagnosis: " + (externalApiRequest == null));
							Boolean diagnosisFound = false;
							String encounterUuid = "";
							String providerUuid = "";
							List<String> diagnosesInEncounter = new ArrayList<>();
							
							Set<Encounter> encounters = visit.getEncounters();
							DiagnosisService diagnosisService = Context.getDiagnosisService();
							OrderService orderService = Context.getOrderService();
							EncounterService encounterService = Context.getEncounterService();
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
										if(debugMode) System.out.println("Insurance Claims Module: Found diagnosis: " + diagName);
									}
									diagnosisFound = true;
									encounterUuid = enc.getUuid();
									Provider provider = GeneralUtil.getProviderForEncounter(enc);
									if(provider != null) {
										providerUuid = provider.getUuid();
										if(debugMode) System.out.println("Insurance Claims Module: Got the provider uuid as: " + providerUuid);
									}
									break;
								}
							}
							if(diagnosisFound == true) {
								// We found a diagnosis
								if(debugMode) System.out.println("Insurance Claims Module: This visit had a diagnosis. Prepare and send claim");

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
									if(debugMode) System.out.println("Insurance Claims Module: Got the location id as: " + currentLocationId);
									newClaimForm.setLocation(currentLocationId.toString());
								}
								newClaimForm.setProvider(providerUuid);
								newClaimForm.setClaimJustification("PHC Claim");
								newClaimForm.setClaimExplanation("PHC Claim");
								newClaimForm.setGuaranteeId("auto");
								newClaimForm.setClaimCode("auto");
								newClaimForm.setDiagnoses(diagnosesInEncounter);
								
								// Check for interventions using encounters and orders
								Set<String> interventionsSet = new LinkedHashSet<>();
								List<String> interventions = new ArrayList<>();

								for(Encounter enc : encounters) {
									EncounterType encType = enc.getEncounterType();

									// Consultation encounters
									EncounterType consultationEncounterType = encounterService.getEncounterTypeByUuid(InsuranceClaimConstants.ENCOUNTER_TYPE_CONSULTATION);
									EncounterType hivConsultationET = encounterService.getEncounterTypeByUuid(InsuranceClaimConstants.ENCOUNTER_TYPE_HIV_CONSULTATION);
									EncounterType cwcConsultationET = encounterService.getEncounterTypeByUuid(InsuranceClaimConstants.ENCOUNTER_TYPE_CWC_CONSULTATION);
									EncounterType mchMotherConsultationET = encounterService.getEncounterTypeByUuid(InsuranceClaimConstants.ENCOUNTER_TYPE_MCH_MOTHER_CONSULTATION);
									EncounterType prepConsultationET = encounterService.getEncounterTypeByUuid(InsuranceClaimConstants.ENCOUNTER_TYPE_PREP_CONSULTATION);
									EncounterType kpClinicVisitET = encounterService.getEncounterTypeByUuid(InsuranceClaimConstants.ENCOUNTER_TYPE_KP_CLINIC_VISIT_FORM);

									// Lab encounters
									EncounterType labResultsET = encounterService.getEncounterTypeByUuid(InsuranceClaimConstants.ENCOUNTER_TYPE_LAB_RESULTS);
									EncounterType labOrderET = encounterService.getEncounterTypeByUuid(InsuranceClaimConstants.ENCOUNTER_TYPE_LAB_ORDER);
									EncounterType procedureResultsET = encounterService.getEncounterTypeByUuid(InsuranceClaimConstants.ENCOUNTER_TYPE_PROCEDURE_RESULTS);
									EncounterType ipdProcedureET = encounterService.getEncounterTypeByUuid(InsuranceClaimConstants.ENCOUNTER_TYPE_IPD_PROCEDURE);

									// Drug-related encounters
									EncounterType drugOrderET = encounterService.getEncounterTypeByUuid(InsuranceClaimConstants.ENCOUNTER_TYPE_DRUG_ORDER);
									EncounterType drugRegimenET = encounterService.getEncounterTypeByUuid(InsuranceClaimConstants.ENCOUNTER_TYPE_DRUG_REGIMEN_EDITOR);
									EncounterType artRefillET = encounterService.getEncounterTypeByUuid(InsuranceClaimConstants.ENCOUNTER_TYPE_ART_REFILL);
									EncounterType prepRefillET = encounterService.getEncounterTypeByUuid(InsuranceClaimConstants.ENCOUNTER_TYPE_PREP_MONTHLY_REFILL);

									// Screening encounters
									EncounterType tbScreeningET = encounterService.getEncounterTypeByUuid(InsuranceClaimConstants.ENCOUNTER_TYPE_TB_SCREENING);
									EncounterType cervicalScreeningET = encounterService.getEncounterTypeByUuid(InsuranceClaimConstants.ENCOUNTER_TYPE_CERVICAL_CANCER_SCREENING);
									EncounterType alcoholScreeningET = encounterService.getEncounterTypeByUuid(InsuranceClaimConstants.ENCOUNTER_TYPE_KP_ALCOHOL_SCREENING);
									EncounterType violenceScreeningET = encounterService.getEncounterTypeByUuid(InsuranceClaimConstants.ENCOUNTER_TYPE_KP_VIOLENCE_SCREENING);
									EncounterType depressionScreeningET = encounterService.getEncounterTypeByUuid(InsuranceClaimConstants.ENCOUNTER_TYPE_KP_DEPRESSION_SCREENING);
									EncounterType hearingScreeningET = encounterService.getEncounterTypeByUuid(InsuranceClaimConstants.ENCOUNTER_TYPE_HEARING_SCREENING_CLINIC);
									EncounterType gadET = encounterService.getEncounterTypeByUuid(InsuranceClaimConstants.ENCOUNTER_TYPE_GENERALIZED_ANXIETY_DISORDER_ASSESSMENT);

									// ANC encounters
									EncounterType ancEnrollmentET = encounterService.getEncounterTypeByUuid(InsuranceClaimConstants.ENCOUNTER_TYPE_ANC_MOTHER_ENROLLMENT);
									EncounterType ancDiscontinuationET = encounterService.getEncounterTypeByUuid(InsuranceClaimConstants.ENCOUNTER_TYPE_ANC_DISCONTINUATION);
									EncounterType mchMotherEnrollmentET = encounterService.getEncounterTypeByUuid(InsuranceClaimConstants.ENCOUNTER_TYPE_MCH_MOTHER_ENROLLMENT);

									// PNC encounters
									EncounterType pncEnrollmentET = encounterService.getEncounterTypeByUuid(InsuranceClaimConstants.ENCOUNTER_TYPE_PNC_MOTHER_ENROLLMENT);
									EncounterType pncDiscontinuationET = encounterService.getEncounterTypeByUuid(InsuranceClaimConstants.ENCOUNTER_TYPE_PNC_DISCONTINUATION);
									EncounterType mchPostDeliveryET = encounterService.getEncounterTypeByUuid(InsuranceClaimConstants.ENCOUNTER_TYPE_MCH_POST_DELIVERY);

									// Immunization encounters
									EncounterType immunizationsET = encounterService.getEncounterTypeByUuid(InsuranceClaimConstants.ENCOUNTER_TYPE_IMMUNIZATIONS);
									EncounterType mchChildImmunizationET = encounterService.getEncounterTypeByUuid(InsuranceClaimConstants.ENCOUNTER_TYPE_MCH_CHILD_IMMUNIZATION);
									EncounterType aefiET = encounterService.getEncounterTypeByUuid(InsuranceClaimConstants.ENCOUNTER_TYPE_AEFI_INVESTIGATION);

									if(encType != null) {
										// Consultation encounter - check
										// SHA-12-001: Consultation
										if(encType == consultationEncounterType  ||
											encType == hivConsultationET ||
											encType == cwcConsultationET ||
											encType == mchMotherConsultationET ||
											encType == prepConsultationET ||
											encType == kpClinicVisitET) {

											interventionsSet.add("SHA-12-001");
										}

										// SHA-12-002: Lab investigations
										if (encType == labResultsET ||
											encType == labOrderET ||
											encType == procedureResultsET ||
											encType == ipdProcedureET) {

											interventionsSet.add("SHA-12-002");
										}

										// SHA-12-003: Radiology
										if (encType == procedureResultsET ||
											encType == ipdProcedureET) {

											interventionsSet.add("SHA-12-003");
										}

										// SHA-12-004: Prescription / Dispensing
										if (encType == drugOrderET ||
											encType == drugRegimenET ||
											encType == artRefillET ||
											encType == prepRefillET) {

											interventionsSet.add("SHA-12-004");
										}

										// SHA-12-006: Screening
										if (encType == tbScreeningET ||
											encType == cervicalScreeningET ||
											encType == alcoholScreeningET ||
											encType == violenceScreeningET ||
											encType == depressionScreeningET ||
											encType == hearingScreeningET ||
											encType == gadET) {

											interventionsSet.add("SHA-12-006");
										}

										// SHA-12-007: Daycare procedures
										if (encType == ipdProcedureET ||
											encType == procedureResultsET) {

											interventionsSet.add("SHA-12-007");
										}

										// SHA-12-008: Antenatal care
										if (encType == ancEnrollmentET ||
											encType == ancDiscontinuationET ||
											encType == mchMotherEnrollmentET) {

											interventionsSet.add("SHA-12-008");
										}

										// SHA-12-009: Postnatal care
										if (encType == pncEnrollmentET ||
											encType == pncDiscontinuationET ||
											encType == mchPostDeliveryET) {

											interventionsSet.add("SHA-12-009");
										}

										// SHA-12-010: Immunization
										if (encType == immunizationsET ||
											encType == mchChildImmunizationET ||
											encType == aefiET) {

											interventionsSet.add("SHA-12-010");
										}
									}
									
									// EncounterType
									Set<Order> orders = enc.getOrders();
									for(Order order : orders) {
										OrderType orderType = order.getOrderType();
										OrderType drugOrderType = orderService.getOrderTypeByUuid(InsuranceClaimConstants.ORDER_TYPE_DRUG);
										OrderType testOrderType = orderService.getOrderTypeByUuid(InsuranceClaimConstants.ORDER_TYPE_TEST);
										OrderType procedureOrderType = orderService.getOrderTypeByUuid(InsuranceClaimConstants.ORDER_TYPE_PROCEDURE);

										// Lab Test Order
										if(testOrderType != null && orderType == testOrderType) {
											interventionsSet.add("SHA-12-002");
										}

										// Procedure Order e.g xray
										if(procedureOrderType != null && orderType == procedureOrderType) {
											interventionsSet.add("SHA-12-003");
										}

										// Drug order - Pharmacy
										if(drugOrderType != null && orderType == drugOrderType) {
											interventionsSet.add("SHA-12-004");
										}
									}
								}

								// Everything else goes into SHA-12-005 (Others)
								if (interventionsSet.isEmpty()) {
									interventionsSet.add("SHA-12-005");
								}

								for(String inter: interventionsSet) {
									interventions.add(inter);
								}

								newClaimForm.setInterventions(interventions);

								// Check if there are any pending cashier bills for this patient
								IBillService billService = Context.getService(IBillService.class);
								if(billService != null) {
									List<Bill> patientBills = billService.searchBill(patient);
									if(patientBills != null && patientBills.size() > 0) {
										if(debugMode) System.out.println("Insurance Claims Module: We found pending cashier bills for this patient");
										Map<String, ProvidedItemInForm> providedItems = new HashMap<>();
										for(Bill bill : patientBills) {
											List<BillLineItem> lineItems = bill.getLineItems();
											List<ItemDetails> items = new ArrayList<>();
											if(lineItems != null && lineItems.size() > 0) {
												for(BillLineItem billLineItem : lineItems) {
													ItemDetails item = new ItemDetails();
													item.setUuid(billLineItem.getItemOrServiceConceptUuid());
													item.setPrice(billLineItem.getPrice());
													item.setQuantity(billLineItem.getQuantity());
													items.add(item);
												}
											}
											ProvidedItemInForm providedItemInForm = new ProvidedItemInForm();
											providedItemInForm.setItems(items);
											providedItemInForm.setExplanation("PHC auto claim");
											providedItemInForm.setJustification("PHC auto claim");
											providedItems.put(bill.getUuid(), providedItemInForm);
										}
										
										if(debugMode) System.out.println("Insurance Claims Module: Setting provided items for bills: " + providedItems.size());
										newClaimForm.setProvidedItems(providedItems);
									} else {
										if(debugMode) System.err.println("Insurance Claims Module: NO pending cashier bills for this patient");
									}
								} else {
									if(debugMode) System.err.println("Insurance Claims Module: ERROR: Could not load bill service");
								}

								ClaimFormService claimFormService = Context.getService(ClaimFormService.class);
								InsuranceClaim claim = claimFormService.createClaim(newClaimForm);

								// Send the claim in a thread
								// SendClaimRunnable runner = new SendClaimRunnable(claim);
								// Thread claimSender = new Thread(runner);
								// claimSender.start();
								// Daemon.runInDaemonThread(runner, InsuranceClaimsActivator.getDaemonToken());

								try {
									if(debugMode) System.out.println("Insurance Claims Module: Now Attempting to send the claim");
									if(externalApiRequest == null) {
										if(debugMode) System.out.println("Insurance Claims Module: Manually create ExternalApiRequest");
										externalApiRequest = Context.getRegisteredComponent("insuranceclaims.ExternalApiRequest", ExternalApiRequest.class);
										if(externalApiRequest == null) {
											if(debugMode) System.err.println("Insurance Claims Module: All attempts failed to load ExternalApiRequest");
										}
									}
									externalApiRequest.sendClaimToExternalApi(claim);
								} catch (Exception ex) {
									if(debugMode) System.err.println("Insurance Claims Module: Now with Claim Sending Error: " + ex.getMessage());
									ex.printStackTrace();
								}

							} else {
								// We found a diagnosis
								if(debugMode) System.out.println("Insurance Claims Module: This visit did NOT have a diagnosis");
							}
						} else {
							if(debugMode) System.out.println("Insurance Claims Module: Error: No patient attached to the visit");
						}
					} else {
						if(debugMode) System.out.println("Insurance Claims Module: Automation Error: Not a checkout. We ignore.");
					}
				}
			} else {
				if(debugMode) System.out.println("Insurance Claims Module: Automated claims is disabled. Not processing visit");
			}
		}
		catch (Exception ex) {
			if (debugMode)
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

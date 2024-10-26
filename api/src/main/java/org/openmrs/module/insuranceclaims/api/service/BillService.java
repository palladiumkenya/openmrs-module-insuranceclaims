package org.openmrs.module.insuranceclaims.api.service;

import org.openmrs.Patient;
import org.openmrs.api.APIException;
import org.openmrs.module.insuranceclaims.api.model.Bill;
import org.openmrs.module.insuranceclaims.api.model.ProvidedItem;

import java.util.List;

public interface BillService extends OpenmrsDataService<Bill> {

    Bill generateBill(List<ProvidedItem> providedItems, Patient patient);

    List<Bill> getAllBills(Integer patientId) throws APIException;
}

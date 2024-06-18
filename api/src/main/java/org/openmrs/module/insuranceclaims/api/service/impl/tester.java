package org.openmrs.module.insuranceclaims.api.service.impl;

import org.openmrs.module.kenyaemr.cashier.api.model.Bill;
import org.openmrs.module.kenyaemr.cashier.api.model.BillStatus;

public class tester {
    String pess = "test";

    public tester() {
        Bill mess = new Bill();
        mess.setStatus(BillStatus.ADJUSTED);
    }
}

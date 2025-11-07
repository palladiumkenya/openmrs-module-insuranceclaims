package org.openmrs.module.insuranceclaims.api;

import org.openmrs.module.insuranceclaims.api.service.BillService;
import org.openmrs.module.insuranceclaims.api.service.InsuranceClaimService;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.openmrs.api.context.Context;
import org.openmrs.test.jupiter.BaseModuleContextSensitiveTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:components/insuranceServiceComponents.xml" })
public class ServiceRegistrationTest extends BaseModuleContextSensitiveTest {

    @Test
    public void shouldRegisterInsuranceClaimService() {
        InsuranceClaimService insuranceClaimService = Context.getService(InsuranceClaimService.class);
        assertNotNull(insuranceClaimService, "InsuranceClaimService should be registered in the context");
        // assertEquals(1, 1);
    }

    @Test
    public void shouldRegisterBillService() {
        BillService billService = Context.getService(BillService.class);
        assertNotNull(billService, "BillService should be registered in the context");
    }
}
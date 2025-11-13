/*
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.insuranceclaims.web.resource;

import org.openmrs.annotation.Authorized;
import org.openmrs.api.context.Context;
import org.openmrs.module.insuranceclaims.api.model.Bill;
import org.openmrs.module.insuranceclaims.api.service.BillService;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.RefRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.impl.DataDelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * REST resource for {@link Bill}, exposing limited fields in the default representation.
 */
@CrossOrigin(origins = "*", methods = { RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT,
        RequestMethod.OPTIONS })
@Resource(
        name = RestConstants.VERSION_1 + "/claimbill",
        supportedClass = Bill.class,
        supportedOpenmrsVersions = { "2.0 - 2.*" }
)
@Authorized
public class BillResource extends DataDelegatingCrudResource<Bill> {

    @Override
    public Bill newDelegate() {
        return new Bill();
    }

    @Override
    public Bill save(Bill bill) {
        return Context.getService(BillService.class).saveOrUpdate(bill);
    }

    @Override
    public Bill getByUniqueId(String uuid) {
        return Context.getService(BillService.class).getByUuid(uuid);
    }

    @Override
    protected void delete(Bill bill, String reason, org.openmrs.module.webservices.rest.web.RequestContext context) {
        bill.setVoided(true);
        bill.setVoidReason(reason);
        Context.getService(BillService.class).saveOrUpdate(bill);
    }

    @Override
    public void purge(Bill bill, org.openmrs.module.webservices.rest.web.RequestContext context) {
        Context.getService(BillService.class).delete(bill);
    }

    @Override
    public DelegatingResourceDescription getRepresentationDescription(Representation rep) {

        if (rep instanceof RefRepresentation) {
            DelegatingResourceDescription description = new DelegatingResourceDescription();
            description.addProperty("uuid");
            description.addSelfLink();

            return description;
        } else if (rep instanceof DefaultRepresentation) {
            DelegatingResourceDescription description = new DelegatingResourceDescription();
            description.addProperty("uuid");
            description.addProperty("startDate");
            description.addProperty("endDate");
            description.addProperty("totalAmount");
            description.addProperty("paymentStatus");
            description.addProperty("paymentType");
            description.addProperty("diagnosis", Representation.REF);
            description.addProperty("patient", Representation.REF);
            description.addProperty("providedItems", Representation.DEFAULT);
            description.addSelfLink();
            description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);

            return description;

        } else if (rep instanceof FullRepresentation) {
            DelegatingResourceDescription description = new DelegatingResourceDescription();
            description.addProperty("uuid");
            description.addProperty("startDate");
            description.addProperty("endDate");
            description.addProperty("totalAmount");
            description.addProperty("paymentStatus");
            description.addProperty("paymentType");
            description.addProperty("diagnosis", Representation.DEFAULT);
            description.addProperty("patient", Representation.DEFAULT);
            description.addProperty("providedItems", Representation.DEFAULT);
            description.addProperty("auditInfo");
            description.addSelfLink();

            return description;
        }

        return null;
    }

    @Override
    protected PageableResult doGetAll(RequestContext context) {
        System.out.println("Insurance Claims: Getting all claim bills");
        return new NeedsPaging<>(Context.getService(BillService.class).getAll(false), context);
    }
}

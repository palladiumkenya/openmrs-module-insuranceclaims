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


import org.openmrs.api.context.Context;
import org.openmrs.module.insuranceclaims.api.model.InsuranceClaim;
import org.openmrs.module.insuranceclaims.api.service.InsuranceClaimService;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.impl.DataDelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * REST resource representing a {@link InsuranceClaim}.
 */
@CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS})
@Resource(name = RestConstants.VERSION_1 +  "/claimsearch", supportedClass = InsuranceClaim.class, supportedOpenmrsVersions = {"2.0 - 2.*"})
public class ClaimResource extends DataDelegatingCrudResource<InsuranceClaim> {
	@Override
    public InsuranceClaim getByUniqueId(String uniqueId) {
		InsuranceClaimService insuranceClaimService = Context.getService(InsuranceClaimService.class);
        return insuranceClaimService.getByUuid(uniqueId);
    }

    @Override
    protected void delete(InsuranceClaim claim, String reason, RequestContext context) throws ResponseException {
		InsuranceClaimService insuranceClaimService = Context.getService(InsuranceClaimService.class);
		claim.setVoided(true);
		claim.setVoidReason(reason);
        insuranceClaimService.saveOrUpdate(claim);
    }

    @Override
    public InsuranceClaim newDelegate() {
        return new InsuranceClaim();
    }

    @Override
    public InsuranceClaim save(InsuranceClaim claim) {
		InsuranceClaimService insuranceClaimService = Context.getService(InsuranceClaimService.class);
		return insuranceClaimService.saveOrUpdate(claim);
    }

    @Override
    public void purge(InsuranceClaim claim, RequestContext context) throws ResponseException {
		InsuranceClaimService insuranceClaimService = Context.getService(InsuranceClaimService.class);
		insuranceClaimService.delete(claim);
    }

    @Override
    public DelegatingResourceDescription getRepresentationDescription(Representation representation) {
        DelegatingResourceDescription description = new DelegatingResourceDescription();
        if (representation instanceof DefaultRepresentation) {
            description.addProperty("uuid");
            description.addProperty("claimCode");
            description.addProperty("billNumber");
            description.addProperty("dateFrom");
            description.addProperty("dateTo");
            description.addProperty("claimedTotal");
            description.addProperty("approvedTotal");
            description.addProperty("status");
            description.addSelfLink();
        } else if (representation instanceof FullRepresentation) {
            description.addProperty("uuid");
            description.addProperty("claimCode");
            description.addProperty("billNumber");
            description.addProperty("dateFrom");
            description.addProperty("dateTo");
            description.addProperty("claimedTotal");
            description.addProperty("approvedTotal");
            description.addProperty("status");
            description.addProperty("adjustment");
            description.addProperty("explanation");
            description.addProperty("rejectionReason");
            description.addProperty("guaranteeId");
            description.addProperty("externalId");
            description.addProperty("dateProcessed");
            description.addSelfLink();
            description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
        }
        return description;
    }

    @Override
    protected PageableResult doGetAll(RequestContext context) {
        return new NeedsPaging<>(Context.getService(InsuranceClaimService.class).getAll(false), context);
    }
}
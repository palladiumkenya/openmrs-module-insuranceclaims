package org.openmrs.module.insuranceclaims.web.resource;

import org.openmrs.annotation.Authorized;
import org.openmrs.api.context.Context;
import org.openmrs.module.insuranceclaims.api.model.ProvidedItem;
import org.openmrs.module.insuranceclaims.api.service.ProvidedItemService;
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
 * REST resource for {@link ProvidedItem}, exposing limited fields in the default representation.
 */
@CrossOrigin(origins = "*", methods = { RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT,
        RequestMethod.OPTIONS })
@Resource(
        name = RestConstants.VERSION_1 + "/claimprovideditem",
        supportedClass = ProvidedItem.class,
        supportedOpenmrsVersions = { "2.0 - 2.*" }
)
@Authorized
public class ProvidedItemResource extends DataDelegatingCrudResource<ProvidedItem> {

    @Override
    public ProvidedItem newDelegate() {
        return new ProvidedItem();
    }

    @Override
    public ProvidedItem save(ProvidedItem providedItem) {
        return Context.getService(ProvidedItemService.class).saveOrUpdate(providedItem);
    }

    @Override
    public ProvidedItem getByUniqueId(String uuid) {
        return Context.getService(ProvidedItemService.class).getByUuid(uuid);
    }

    @Override
    protected void delete(ProvidedItem item, String reason, org.openmrs.module.webservices.rest.web.RequestContext context) {
        item.setVoided(true);
        item.setVoidReason(reason);
        Context.getService(ProvidedItemService.class).saveOrUpdate(item);
    }

    @Override
    public void purge(ProvidedItem item, org.openmrs.module.webservices.rest.web.RequestContext context) {
        Context.getService(ProvidedItemService.class).delete(item);
    }

    @Override
    public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
        
        if (rep instanceof RefRepresentation) {
            DelegatingResourceDescription description = new DelegatingResourceDescription();
            description.addProperty("uuid");
            description.addSelfLink();

            return description;

        } else if (rep instanceof DefaultRepresentation) {
            DelegatingResourceDescription desc = new DelegatingResourceDescription();
            desc.addProperty("uuid");
            desc.addProperty("originUuid");
            desc.addProperty("price");
            desc.addProperty("dateOfServed");
            desc.addProperty("status");
            desc.addProperty("numberOfConsumptions");
            desc.addProperty("item", Representation.REF);
            desc.addProperty("patient", Representation.REF);
            desc.addSelfLink();
            desc.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);

            return desc;

        } else if (rep instanceof FullRepresentation) {
            DelegatingResourceDescription desc = new DelegatingResourceDescription();
            desc.addProperty("uuid");
            desc.addProperty("originUuid");
            desc.addProperty("price");
            desc.addProperty("dateOfServed");
            desc.addProperty("status");
            desc.addProperty("numberOfConsumptions");
            desc.addProperty("item", Representation.DEFAULT);
            desc.addProperty("patient", Representation.DEFAULT);
            desc.addProperty("bill", Representation.REF);
            desc.addProperty("auditInfo");
            desc.addSelfLink();

            return desc;
        }

        return null;
    }

    @Override
    protected PageableResult doGetAll(RequestContext context) {
        System.out.println("Insurance Claims: Getting all claim provided items");
        return new NeedsPaging<>(Context.getService(ProvidedItemService.class).getAll(false), context);
    }
}

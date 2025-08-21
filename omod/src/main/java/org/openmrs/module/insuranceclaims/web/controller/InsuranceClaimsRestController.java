package org.openmrs.module.insuranceclaims.web.controller;

import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.json.simple.JSONObject;
// import org.omg.CORBA.Request;
import org.openmrs.Encounter;
import org.openmrs.Form;
import org.openmrs.GlobalProperty;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.metadatadeploy.MetadataUtils;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.openmrs.parameter.EncounterSearchCriteria;
import org.openmrs.parameter.EncounterSearchCriteriaBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
// import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import org.openmrs.module.insuranceclaims.api.service.InsuranceClaimsPackagesService;
import org.openmrs.module.insuranceclaims.api.model.InsuranceClaimPackage;

/**
 * The main controller for insurance claims
 */
@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/insclaims")
public class InsuranceClaimsRestController extends BaseRestController {
    protected final Log log = LogFactory.getLog(getClass());

    /**
	 * Gets the list of insurace packages
	 * @return
	 */
	@CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
	@RequestMapping(method = RequestMethod.GET, value = "/packages")
	@ResponseBody
	public List<SimpleObject> getInsurancePackages(@RequestParam(value = "gender", required = false) String gender) {
		List<SimpleObject> ret = new ArrayList<>();
        InsuranceClaimsPackagesService insuranceClaimsPackagesService = Context.getService(InsuranceClaimsPackagesService.class);

        List<InsuranceClaimPackage> packages = insuranceClaimsPackagesService.getPackages(gender);

        System.err.println("Insurance Claims Module: Got packages list: " + packages.size());

        for(InsuranceClaimPackage icp : packages) {
            SimpleObject prep = new SimpleObject();
            prep.add("code", icp.getCode());
            prep.add("name", icp.getShaCategory());
            prep.add("description", icp.getInterventionName());
            ret.add(prep);
        }

		return(ret);
	}

    /**
	 * Gets the list of insurace interventions
	 * @return
	 */
	@CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
	@RequestMapping(method = RequestMethod.GET, value = "/interventions")
	@ResponseBody
	public SimpleObject getInsuranceInterventions(@RequestParam(value = "gender", required = false) String gender, @RequestParam(value = "package_code", required = false) String packageCode) {
        SimpleObject result = new SimpleObject();
		List<SimpleObject> data = new ArrayList<>();
        InsuranceClaimsPackagesService insuranceClaimsPackagesService = Context.getService(InsuranceClaimsPackagesService.class);

        List<InsuranceClaimPackage> packages = insuranceClaimsPackagesService.getInterventions(gender, packageCode);
        result.add("status", "SUCCESS");
        result.add("customerMessage", "Benefit details successfully retrieved");
        result.add("debugMessage", null);

        for(InsuranceClaimPackage icp : packages) {
            SimpleObject prep = new SimpleObject();
            prep.add("interventionName", icp.getInterventionName());
            prep.add("interventionCode", icp.getInterventionCode());
            prep.add("interventionPackage", icp.getCode());
            prep.add("interventionSubPackage", icp.getInterventionCode());
            prep.add("interventionDescription", null);
            prep.add("insuranceSchemes", null);
            data.add(prep);
        }

        result.add("data", data);

		return(result);
	}
}

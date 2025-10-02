package org.openmrs.module.insuranceclaims.forms.impl;


import ca.uhn.fhir.util.DateUtils;

import org.apache.commons.codec.binary.Base64;
import org.apache.tika.mime.MimeTypeException;
import org.openmrs.Concept;
import org.openmrs.ConceptMap;
import org.openmrs.ConceptMapType;
import org.openmrs.ConceptReferenceTerm;
import org.openmrs.ConceptSource;
import org.openmrs.Encounter;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.Visit;
import org.openmrs.VisitType;
import org.openmrs.api.context.Context;
import org.openmrs.module.insuranceclaims.api.model.InsuranceClaim;
import org.openmrs.module.insuranceclaims.api.model.InsuranceClaimAttachment;
import org.openmrs.module.insuranceclaims.api.model.InsuranceClaimDiagnosis;
import org.openmrs.module.insuranceclaims.api.model.InsuranceClaimIntervention;
import org.openmrs.module.insuranceclaims.api.model.InsuranceClaimItem;
import org.openmrs.module.insuranceclaims.api.model.InsuranceClaimStatus;
import org.openmrs.module.insuranceclaims.api.model.ProvidedItem;
import org.openmrs.module.insuranceclaims.api.model.SupportingDocuments;
import org.openmrs.module.insuranceclaims.api.model.InsuranceClaimAttachment.DocumentType;
import org.openmrs.module.insuranceclaims.api.model.PaymentType;
import org.openmrs.module.insuranceclaims.api.model.ProcessStatus;
import org.openmrs.module.insuranceclaims.api.model.Bill;
import org.openmrs.module.insuranceclaims.api.model.FileUploadResponse;
import org.openmrs.module.insuranceclaims.api.service.BillService;
import org.openmrs.module.insuranceclaims.api.service.InsuranceClaimDiagnosisService;
import org.openmrs.module.insuranceclaims.api.service.InsuranceClaimItemService;
import org.openmrs.module.insuranceclaims.api.service.InsuranceClaimService;
import org.openmrs.module.insuranceclaims.api.service.ProvidedItemService;
import org.openmrs.module.insuranceclaims.api.service.fhir.util.GeneralUtil;
import org.openmrs.module.insuranceclaims.forms.ClaimFormService;
import org.openmrs.module.insuranceclaims.forms.ItemDetails;
import org.openmrs.module.insuranceclaims.forms.NewClaimForm;
import org.openmrs.module.insuranceclaims.forms.ProvidedItemInForm;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpServerErrorException;

import com.mchange.net.MimeUtils;

import org.openmrs.module.insuranceclaims.api.service.InsuranceClaimInterventionService;

import javax.transaction.Transactional;

import java.lang.ref.SoftReference;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.stream.Collectors;

import java.util.UUID;
import org.apache.tika.mime.MimeTypes;
import org.apache.tika.mime.MimeTypeException;

public class ClaimFormServiceImpl implements ClaimFormService {

    private BillService billService;

    private ProvidedItemService providedItemService;

    private InsuranceClaimService insuranceClaimService;

    private InsuranceClaimItemService insuranceClaimItemService;

    private InsuranceClaimDiagnosisService insuranceClaimDiagnosisService;

    private InsuranceClaimInterventionService insuranceClaimInterventionService;

    private static final String[] FORM_DATE_FORMAT = {"yyyy-MM-dd"};

    private static final String INVALID_LOCATION_ERROR = "You must select valid location";

    private static final Map<String, String> MIME_TO_EXT;

    static {
        MIME_TO_EXT = new HashMap<>();
        MIME_TO_EXT.put("image/jpeg", ".jpg");
        MIME_TO_EXT.put("image/png", ".png");
        MIME_TO_EXT.put("image/gif", ".gif");
        MIME_TO_EXT.put("image/bmp", ".bmp");
        MIME_TO_EXT.put("image/webp", ".webp");
        MIME_TO_EXT.put("application/pdf", ".pdf");
        MIME_TO_EXT.put("text/plain", ".txt");
        MIME_TO_EXT.put("text/html", ".html");
        MIME_TO_EXT.put("application/json", ".json");
    }

    @Override
    @Transactional
    public InsuranceClaim createClaim(NewClaimForm form) {
        InsuranceClaim nextClaim = new InsuranceClaim();

        nextClaim.setAdjustment(form.getClaimJustification());
        nextClaim.setExplanation(form.getClaimExplanation());

        VisitType visitType = Context.getVisitService().getVisitTypeByUuid(form.getVisitType());
        nextClaim.setVisitType(visitType);

        System.out.println("Insurance Module: Patient UUID : " + form.getPatient());
        Patient patient = Context.getPatientService().getPatientByUuid(form.getPatient());
        System.out.println("Insurance Module: Patient : " + patient);
        nextClaim.setPatient(patient);

        System.out.println("Insurance Module: Visit UUID : " + form.getVisitUuid());
        Visit visit = Context.getVisitService().getVisitByUuid(form.getVisitUuid());
        System.out.println("Insurance Module: Visit : " + visit);
        nextClaim.setVisit(visit);

        System.out.println("Insurance Module: Encounter UUID : " + form.getEncounterUuid());
        Encounter encounter = Context.getEncounterService().getEncounterByUuid(form.getEncounterUuid());
        // Check for null encounter and if null get the first patient encounter TODO: remove this
        if(encounter == null) {
            System.out.println("Insurance Module: Encounter : Fail control. Encounter was null so we use a random one");
            List<Encounter> encounters = Context.getEncounterService().getEncountersByPatient(patient);
            encounter = encounters.size() > 0 ? encounters.get(encounters.size() - 1) : null;
        }
        System.out.println("Insurance Module: Encounter : " + encounter);
        nextClaim.setEncounter(encounter);

        nextClaim.setGuaranteeId(form.getGuaranteeId());
        nextClaim.setClaimCode(form.getClaimCode());
        nextClaim.setBillNumber(form.getBillNumber());
        nextClaim.setUse(form.getUse());
        nextClaim.setStatus(InsuranceClaimStatus.ENTERED);
        nextClaim.setLocation(getClaimLocation(form));

        System.out.println("Insurance Module: Provider UUID : " + form.getProvider());
        Provider provider = Context.getProviderService().getProviderByUuid(form.getProvider());
        // Check for null provider and if null get the first registered provider TODO: remove this
        if(provider == null) {
            System.out.println("Insurance Module: Provider : Fail control. Provider was null so we use a random one");
            provider = Context.getProviderService().getAllProviders().get(0);
        }
        System.out.println("Insurance Module: Provider : " + provider);

        nextClaim.setProvider(provider);

        assignDatesFromFormToClaim(nextClaim, form);

        List<InsuranceClaimItem> items = generateClaimItems(form.getProvidedItems(), patient);
        List<ProvidedItem> claimProvidedItems = items.stream()
                .map(item -> item.getItem())
                .collect(Collectors.toList());

        createClaimBill(nextClaim, claimProvidedItems);
        nextClaim.getBill().setPaymentType(PaymentType.INSURANCE_CLAIM);

        System.out.println("Insurance Module: Provider Debug 0 : " + nextClaim.getProvider());
        insuranceClaimService.saveOrUpdate(nextClaim);

        List<InsuranceClaimDiagnosis> diagnoses = generateClaimDiagnoses(form.getDiagnoses(), nextClaim);
        diagnoses.stream().forEach(diagnosis -> insuranceClaimDiagnosisService.saveOrUpdate(diagnosis));

        items.stream().forEach(item -> {
            item.setClaim(nextClaim);
            insuranceClaimItemService.saveOrUpdate(item);
        });

        List<InsuranceClaimIntervention> interventions = generateClaimInterventions(form.getInterventions(), nextClaim);
        interventions.stream().forEach(intervention -> insuranceClaimInterventionService.saveOrUpdate(intervention));

        // Add attachments
        addAttachmentsToClaim(form.getSupportingDocuments(), nextClaim);

        return nextClaim;
    }

    @Override
    @Transactional
    public Bill createBill(NewClaimForm form) {
        // List<InsuranceClaimItem> items = generateClaimItems(form.getProvidedItems());
        // List<ProvidedItem> claimProvidedItems = items.stream()
        //         .map(item -> item.getItem())
        //         .collect(Collectors.toList());

        // Bill bill = billService.generateBill(claimProvidedItems);
        // bill.setPaymentType(PaymentType.CASH);
        // billService.saveOrUpdate(bill);
        Bill bill = new Bill();

        return bill;
    }

    public void setBillService(BillService billService) {
        this.billService = billService;
    }

    public void setProvidedItemService(ProvidedItemService providedItemService) {
        this.providedItemService = providedItemService;
    }

    public void setInsuranceClaimService(InsuranceClaimService insuranceClaimService) {
        this.insuranceClaimService = insuranceClaimService;
    }

    public void setInsuranceClaimItemService(InsuranceClaimItemService insuranceClaimItemService) {
        this.insuranceClaimItemService = insuranceClaimItemService;
    }

    public void setInsuranceClaimDiagnosisService(InsuranceClaimDiagnosisService insuranceClaimDiagnosisService) {
        this.insuranceClaimDiagnosisService = insuranceClaimDiagnosisService;
    }

    private List<InsuranceClaimItem> generateClaimItems(Map<String, ProvidedItemInForm> allProvidedItems, Patient patient) {
        List<InsuranceClaimItem> consumptions = new ArrayList<>();

        if(allProvidedItems != null && allProvidedItems.size() > 0) {
            for (ProvidedItemInForm itemConsumptions:  allProvidedItems.values()) {
                List<InsuranceClaimItem> items = getConsumedItemsOfType(itemConsumptions, patient);
                consumptions.addAll(items);
            }
        }

        return consumptions;
    }

    private List<InsuranceClaimItem> getConsumedItemsOfType(ProvidedItemInForm formItems, Patient patient) {
        List<InsuranceClaimItem> items = new ArrayList<>();
        String explanation = formItems.getExplanation();
        String justification = formItems.getJustification();

        for (ItemDetails nextItemDetails : formItems.getItems()) {
            // System.out.println("Insurance Claims: Search ITEM UUID: " + nextItemDetails.getUuid());
            ProvidedItem provideditem = new ProvidedItem();
            provideditem.setOriginUuid(nextItemDetails.getUuid());
            provideditem.setItem(Context.getConceptService().getConceptByUuid(nextItemDetails.getUuid()));
            provideditem.setInterventionPackage(nextItemDetails.getInterventionPackage());
            provideditem.setInterventionCode(nextItemDetails.getInterventionCode());
            provideditem.setPrice(nextItemDetails.getPrice());
            provideditem.setNumberOfConsumptions(nextItemDetails.getQuantity());
            provideditem.setPatient(patient);
            provideditem.setStatus(ProcessStatus.ENTERED);
            providedItemService.saveOrUpdate(provideditem);

            // System.out.println("Insurance Claims: ITEM created");
            InsuranceClaimItem nextInsuranceClaimItem = new InsuranceClaimItem();
            nextInsuranceClaimItem.setItem(provideditem);
            nextInsuranceClaimItem.setQuantityProvided(provideditem.getNumberOfConsumptions());
            nextInsuranceClaimItem.setJustification(justification);
            nextInsuranceClaimItem.setExplanation(explanation);
            items.add(nextInsuranceClaimItem);

        }
        return items;
    }

    private List<InsuranceClaimDiagnosis> generateClaimDiagnoses(List<String> diagnosesUuidList, InsuranceClaim claim) {
        List<InsuranceClaimDiagnosis> diagnoses = new ArrayList<>();

        for (String uuid: diagnosesUuidList) {
            Concept diagnosisConcept = Context.getConceptService().getConceptByUuid(uuid);
            // testDiagnosis(diagnosisConcept);
            InsuranceClaimDiagnosis nextDiagnosis = new InsuranceClaimDiagnosis(diagnosisConcept, claim);
            diagnoses.add(nextDiagnosis);
        }
        return diagnoses;
    }

    private List<InsuranceClaimIntervention> generateClaimInterventions(List<String> interventions, InsuranceClaim claim) {
        List<InsuranceClaimIntervention> claiminterventions = new ArrayList<>();

        for (String intervention: interventions) {
            InsuranceClaimIntervention nextIntervention = new InsuranceClaimIntervention(intervention, claim);
            claiminterventions.add(nextIntervention);
        }
        return claiminterventions;
    }

    public void testDiagnosis(Concept input) {
        try {
            for (ConceptMap mapping : input.getConceptMappings()) {
                if (mapping.getConceptMapType() != null) {
                    ConceptMapType mapType = mapping.getConceptMapType();
                    boolean sameAs = mapType.getUuid() != null && mapType.getUuid().equals(ConceptMapType.SAME_AS_MAP_TYPE_UUID);
                    sameAs = sameAs || (mapType.getName() != null && mapType.getName().equalsIgnoreCase("SAME-AS"));
                    ConceptReferenceTerm crt = mapping.getConceptReferenceTerm();
                    ConceptSource source = crt.getConceptSource();
                    System.err.println("Good Diagnosis source: " + source);
                    System.err.println("Good Diagnosis source code: " + source.getHl7Code());
                }
            }
        } catch (Exception ex) {
            System.err.println("Diagnosis problem: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void assignDatesFromFormToClaim(InsuranceClaim claim, NewClaimForm form) {
        System.err.println("Insurance Claims: Got API form dates as: date to: " + form.getStartDate() + " and date from: " + form.getEndDate());
        Date startDate = parseDate(form.getStartDate(), FORM_DATE_FORMAT);
        Date endDate = parseDate(form.getEndDate(), FORM_DATE_FORMAT);
        claim.setDateFrom(startDate);
        claim.setDateTo(endDate);
        System.err.println("Insurance Claims: Setting date to: " + startDate + " and date from: " + endDate);
        // claim.setProvider(Context.getProviderService().getProviderByUuid(form.getProvider()));
    }

    /** 
     * Add attachments to the claim
     */
    private void addAttachmentsToClaim(List<SupportingDocuments> supportingDocuments, InsuranceClaim nextClaim) {
        try {
            if(supportingDocuments != null && supportingDocuments.size() > 0) {
                System.err.println("Insurance Claims: Now setting claim attachments");
                for(SupportingDocuments doc : supportingDocuments) {
                    InsuranceClaimAttachment att = new InsuranceClaimAttachment();
                    att.setClaim(nextClaim);
                    att.setFilename(doc.getName());
                    byte[] payload = Base64.decodeBase64(doc.getBase64());
                    att.setFileBlob(payload);
                    att.setInterventionCode(doc.getIntervention());
                    att.setDocumentType(doc.getPurpose());
                    att.setFileSize(doc.getSize());
                    att.setMimeType(doc.getType());

                    // Send file to remote - if unsuccessfull, no need to save
                    String filename = generateNewFileName(doc.getName(), doc.getType());
                    if(filename != null && !filename.isEmpty())
                    {
                        FileUploadResponse fileUploadResponse = GeneralUtil.sendClaimAttachmentToRemote(payload, filename, doc.getType());
                        if(fileUploadResponse != null && fileUploadResponse.getSuccess() == true) {
                            att.setConsentToken("test");
                            att.setUrl(fileUploadResponse.getUrl());
                            att.setRetrievalId(fileUploadResponse.getRetrievalId());
                            att.setStatus(fileUploadResponse.getStatus());
                            // Give the attachment a new name (UUID prevents file name collisions)
                            att.setFilename(filename);

                            nextClaim.addAttachment(att);
                        }
                    }
                }
            } else {
                System.err.println("Insurance Claims: ERROR: Found No claim attachments");
            }
        } catch(Exception ex) {
            System.err.println("Insurance Claims: ERROR: setting claim attachments: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * Generate a new file name for a file based on its file type
     * @param type
     * @return
     */
    private String generateNewFileName (String originalFileName, String mimeType) {
        String ret = null;

        // Get a v4 uuid
        String uuid = UUID.randomUUID().toString();
        String extension = null;

        // Get the file extension
        // 1. Try from filename
        if (originalFileName != null && originalFileName.contains(".")) {
            int dotIndex = originalFileName.lastIndexOf('.');
            if (dotIndex != -1 && dotIndex < originalFileName.length() - 1) {
                extension = originalFileName.substring(dotIndex).toLowerCase();
            }
        }

        if(extension == null || extension.isEmpty()) {
            // 2. Try with Apache Tika
            try {
                MimeTypes allTypes = MimeTypes.getDefaultMimeTypes();
                String tikaExt = allTypes.forName(mimeType).getExtension();
                if (tikaExt != null && !tikaExt.isEmpty()) {
                    extension = tikaExt.toLowerCase();
                }
            } catch (Exception ex) {}
        }

        if(extension == null || extension.isEmpty()) {
            // 3. Try custom MimeUtils
            String customExt = MIME_TO_EXT.getOrDefault(mimeType.toLowerCase(), "");
            if (customExt != null && !customExt.isEmpty()) {
                extension = customExt.toLowerCase();
            }
        }

        if(extension == null || extension.isEmpty()) {
            extension = ".jpg";
        }

        ret = uuid + extension;
        System.err.println("Insurance Claims: Got claim attachment file name as: " + ret);

        return(ret);
    }

    private void createClaimBill(InsuranceClaim claim, List<ProvidedItem> claimProvidedItems) {
        Bill bill = billService.generateBill(claimProvidedItems, claim.getPatient());
        claim.setBill(bill);
        claim.setClaimedTotal(claim.getBill().getTotalAmount());
    }

    private Location getClaimLocation(NewClaimForm form) throws HttpServerErrorException {
        try {
            return Context.getLocationService().getLocation(Integer.parseInt(form.getLocation()));
        } catch (NumberFormatException exception) {
            throw new HttpServerErrorException(HttpStatus.BAD_REQUEST, INVALID_LOCATION_ERROR);
        }
    }

    /**
     * Parses a date value.  The formats used for parsing the date value are retrieved from
     * the default http params.
     *
     * @param dateValue the date value to parse
     *
     * @return the parsed date or null if input could not be parsed
     */
    public static Date parseDate(final String dateValue) {
        return parseDate(dateValue, null, null);
    }

    /**
     * Parses the date value using the given date formats.
     *
     * @param dateValue the date value to parse
     * @param dateFormats the date formats to use
     *
     * @return the parsed date or null if input could not be parsed
     */
    public static Date parseDate(final String dateValue, final String[] dateFormats) {
        return parseDate(dateValue, dateFormats, null);
    }

    /**
     * Parses the date value using the given date formats.
     *
     * @param dateValue the date value to parse
     * @param dateFormats the date formats to use
     * @param startDate During parsing, two digit years will be placed in the range
     * {@code startDate} to {@code startDate + 100 years}. This value may
     * be {@code null}. When {@code null} is given as a parameter, year
     * {@code 2000} will be used.
     *
     * @return the parsed date or null if input could not be parsed
     */
    public static Date parseDate(
            final String dateValue,
            final String[] dateFormats,
            final Date startDate) {

        final String PATTERN_RFC1123 = "EEE, dd MMM yyyy HH:mm:ss zzz";
        final String PATTERN_RFC1036 = "EEE, dd-MMM-yy HH:mm:ss zzz";
        final String PATTERN_ASCTIME = "EEE MMM d HH:mm:ss yyyy";

        final String[] DEFAULT_PATTERNS = new String[] {
            PATTERN_RFC1123,
            PATTERN_RFC1036,
            PATTERN_ASCTIME
        };

        final TimeZone GMT = TimeZone.getTimeZone("GMT");
        final Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(GMT);
        calendar.set(2000, Calendar.JANUARY, 1, 0, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        final Date DEFAULT_TWO_DIGIT_YEAR_START = calendar.getTime();

        notNull(dateValue, "Date value");
        final String[] localDateFormats = dateFormats != null ? dateFormats : DEFAULT_PATTERNS;
        final Date localStartDate = startDate != null ? startDate : DEFAULT_TWO_DIGIT_YEAR_START;
        String v = dateValue;
        // trim single quotes around date if present
        // see issue #5279
        if (v.length() > 1 && v.startsWith("'") && v.endsWith("'")) {
            v = v.substring (1, v.length() - 1);
        }

        for (final String dateFormat : localDateFormats) {
            final SimpleDateFormat dateParser = DateFormatHolder.formatFor(dateFormat);
            dateParser.set2DigitYearStart(localStartDate);
            final ParsePosition pos = new ParsePosition(0);
            final Date result = dateParser.parse(v, pos);
            if (pos.getIndex() != 0) {
                return result;
            }
        }
        return null;
    }

    public static <T> T notNull(final T argument, final String name) {
        if (argument == null) {
            throw new IllegalArgumentException(name + " may not be null");
        }
        return argument;
    }

    /**
     * A factory for {@link SimpleDateFormat}s. The instances are stored in a
     * threadlocal way because SimpleDateFormat is not threadsafe as noted in
     * {@link SimpleDateFormat its javadoc}.
     *
     */
    final static class DateFormatHolder {

        private static final ThreadLocal<SoftReference<Map<String, SimpleDateFormat>>>
            THREADLOCAL_FORMATS = new ThreadLocal<SoftReference<Map<String, SimpleDateFormat>>>() {

            @Override
            protected SoftReference<Map<String, SimpleDateFormat>> initialValue() {
                return new SoftReference<Map<String, SimpleDateFormat>>(
                        new HashMap<String, SimpleDateFormat>());
            }

        };

        /**
         * creates a {@link SimpleDateFormat} for the requested format string.
         *
         * @param pattern
         *            a non-{@code null} format String according to
         *            {@link SimpleDateFormat}. The format is not checked against
         *            {@code null} since all paths go through
         *            {@link DateUtils}.
         * @return the requested format. This simple dateformat should not be used
         *         to {@link SimpleDateFormat#applyPattern(String) apply} to a
         *         different pattern.
         */
        public static SimpleDateFormat formatFor(final String pattern) {
            final SoftReference<Map<String, SimpleDateFormat>> ref = THREADLOCAL_FORMATS.get();
            Map<String, SimpleDateFormat> formats = ref.get();
            if (formats == null) {
                formats = new HashMap<String, SimpleDateFormat>();
                THREADLOCAL_FORMATS.set(
                        new SoftReference<Map<String, SimpleDateFormat>>(formats));
            }

            SimpleDateFormat format = formats.get(pattern);
            if (format == null) {
                format = new SimpleDateFormat(pattern, Locale.US);
                format.setTimeZone(TimeZone.getTimeZone("GMT"));
                formats.put(pattern, format);
            }

            return format;
        }

        public static void clearThreadLocal() {
            THREADLOCAL_FORMATS.remove();
        }

    }

    public InsuranceClaimInterventionService getInsuranceClaimInterventionService() {
        return insuranceClaimInterventionService;
    }

    public void setInsuranceClaimInterventionService(
            InsuranceClaimInterventionService insuranceClaimInterventionService) {
        this.insuranceClaimInterventionService = insuranceClaimInterventionService;
    }
}

package org.openmrs.module.insuranceclaims.api.client;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import com.mchange.v1.io.InputStreamUtils;

import org.hl7.fhir.r4.model.OperationOutcome;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Generic message conventer that use application/json type and allows all FHIR Models as response type
 */
public class FhirOperationOutcomeMessageConverter extends AbstractHttpMessageConverter<OperationOutcome> {

    private static final String CHARSET = "UTF-8";
    private static final String TYPE = "application";
    private static final String SUBTYPE_1 = "json";
    private static final String SUBTYPE_2 = "fhir+json";

    private IParser parser = FhirContext.forR4().newJsonParser();

    public FhirOperationOutcomeMessageConverter() {
        super(new MediaType(TYPE, SUBTYPE_1, Charset.forName(CHARSET)), new MediaType(TYPE, SUBTYPE_2, Charset.forName(CHARSET)));
    }

    @Override
    protected boolean supports(Class<?> clazz) {
        return OperationOutcome.class.isAssignableFrom(clazz);
    }

    @Override
    protected OperationOutcome readInternal(Class<? extends OperationOutcome> clazz, HttpInputMessage inputMessage)
            throws HttpMessageNotReadableException {
        try {
            String json = convertStreamToString(inputMessage.getBody());
            System.out.println("Insurance claims module: Got Operation Outcome FHIR response message: " + json);
            return parser.parseResource(OperationOutcome.class, json);
        }
        catch (IOException e) {
            throw new HttpMessageNotReadableException("Insurance claims module: Operation Outcome : Could not read JSON: " + e.getMessage(), e, inputMessage);
        }
    }

    @Override
    protected void writeInternal(OperationOutcome resource, HttpOutputMessage outputMessage)
            throws HttpMessageNotWritableException {
        try {
            String json = parser.encodeResourceToString(resource);
            outputMessage.getBody().write(json.getBytes(StandardCharsets.UTF_8));
        }
        catch (IOException e) {
            throw new HttpMessageNotWritableException("Could not serialize object. Msg: " + e.getMessage(), e);
        }
    }

    private String convertStreamToString(InputStream is) throws IOException {
        return InputStreamUtils.getContentsAsString(is);
    }
}

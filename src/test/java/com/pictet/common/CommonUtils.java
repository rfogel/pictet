package com.pictet.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.jeasy.random.EasyRandom;
import org.jeasy.random.EasyRandomParameters;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.json.JsonMapper;

import java.text.SimpleDateFormat;

public class CommonUtils {

    public static JsonMapper objectMapper() {
        return JsonMapper
                .builderWithJackson2Defaults()
                .changeDefaultPropertyInclusion(incl -> incl.withValueInclusion(JsonInclude.Include.NON_NULL))
                .changeDefaultPropertyInclusion(incl -> incl.withContentInclusion(JsonInclude.Include.NON_NULL))
                .enable(tools.jackson.databind.SerializationFeature.INDENT_OUTPUT)
                .disable(tools.jackson.databind.SerializationFeature.FAIL_ON_EMPTY_BEANS)
                .disable(tools.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .enable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
                .defaultDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ"))
                .build();
    }

    public static EasyRandom generator() {
        EasyRandomParameters parameters = new EasyRandomParameters();
        parameters.stringLengthRange(2, 10);
        parameters.collectionSizeRange(2, 10);
        return new EasyRandom(parameters);
    }
}

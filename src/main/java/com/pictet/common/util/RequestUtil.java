package com.pictet.common.util;

import lombok.experimental.UtilityClass;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponents;

@UtilityClass
public class RequestUtil {

    public static UriComponents getRequestParameters() {
        return ServletUriComponentsBuilder.fromCurrentRequest().build();
    }
}

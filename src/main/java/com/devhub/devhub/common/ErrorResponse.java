package com.devhub.devhub.common;

import java.util.List;
import java.util.Map;

public record ErrorResponse(boolean ok, Map<String, List<String>> errors) {
    public static ErrorResponse of(String field, String message){
        return new ErrorResponse(false, Map.of(field, List.of(message)));
    }
}

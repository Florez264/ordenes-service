package com.subocol.taller.ordenes.infrastructure.web.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ErrorData(String message, Map<String, String> fields) {
  public ErrorData {
    fields = fields == null ? Map.of() : Map.copyOf(fields);
  }
}

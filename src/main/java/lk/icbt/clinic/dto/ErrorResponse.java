package lk.icbt.clinic.dto;

import java.util.Map;

/** One error shape for the whole API, so the client has one thing to render. */
public class ErrorResponse {
    public final String timestamp;
    public final int status;
    public final String error;
    public final String message;
    public final Map<String, String> fieldErrors;

    public ErrorResponse(int status, String error, String message, Map<String, String> fieldErrors) {
        this.timestamp = java.time.LocalDateTime.now().toString();
        this.status = status;
        this.error = error;
        this.message = message;
        this.fieldErrors = fieldErrors == null ? Map.of() : fieldErrors;
    }
}

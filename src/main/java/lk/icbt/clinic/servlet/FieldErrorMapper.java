package lk.icbt.clinic.servlet;

import java.util.Map;

/**
 * Turns a single business-rule message into a field name to highlight on the
 * booking form.
 * <p>
 * There is no Bean Validation here, so there is no framework producing a
 * per-field error map the way Spring's {@code MethodArgumentNotValidException}
 * does. {@code AppointmentService} fails fast on the first invalid field and
 * reports one message; this maps that message back to the field it concerns,
 * by the vocabulary the service itself uses in that message.
 */
final class FieldErrorMapper {

    private FieldErrorMapper() {
    }

    static Map<String, String> mapFrom(String message) {
        if (message == null) return Map.of();
        String lower = message.toLowerCase();
        if (lower.contains("patient name")) return Map.of("patientName", message);
        if (lower.contains("address")) return Map.of("address", message);
        if (lower.contains("contact number")) return Map.of("contactNumber", message);
        if (lower.contains("past")) return Map.of("appointmentDate", message);
        if (lower.contains("opening hours")) return Map.of("appointmentTime", message);
        return Map.of();
    }
}

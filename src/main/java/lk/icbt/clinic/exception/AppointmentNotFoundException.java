package lk.icbt.clinic.exception;

/** No appointment exists for the given reference. */
public class AppointmentNotFoundException extends RuntimeException {
    public AppointmentNotFoundException(String message) {
        super(message);
    }
}

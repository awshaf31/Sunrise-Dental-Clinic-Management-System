package lk.icbt.clinic.exception;

/** The booking details failed a business rule before anything was persisted. */
public class InvalidBookingException extends RuntimeException {
    public InvalidBookingException(String message) {
        super(message);
    }
}

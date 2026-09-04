package lk.icbt.clinic.exception;

/** The requested dentist is already booked at that date and time. */
public class SlotUnavailableException extends RuntimeException {
    public SlotUnavailableException(String message) {
        super(message);
    }
}

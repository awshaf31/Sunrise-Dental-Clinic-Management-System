package lk.icbt.clinic.exception;

/** A report depends on a database feature (a stored procedure) that isn't available. */
public class ReportUnavailableException extends RuntimeException {
    public ReportUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}

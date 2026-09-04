package lk.icbt.clinic.dto;

import lk.icbt.clinic.model.Staff;

/**
 * Staff as returned over the wire.
 * <p>
 * Note what field is absent: there is no {@code passwordHash}. Gson
 * serialises whatever fields exist on this class, so the hash has no route
 * out of the service simply because this class was never given one.
 */
public class StaffResponse {
    public final Long id;
    public final String username;
    public final String fullName;
    public final String role;

    public StaffResponse(Staff s) {
        this.id = s.getId();
        this.username = s.getUsername();
        this.fullName = s.getFullName();
        this.role = s.getRole().name();
    }
}

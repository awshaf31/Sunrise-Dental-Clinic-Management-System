package lk.icbt.clinic.model;

import java.time.LocalDateTime;

/**
 * A member of clinic staff who can log in and operate the system.
 * <p>
 * Plain Java object -- no annotations mapping this to a table. The mapping
 * between this class's fields and the {@code staff} table's columns is done
 * explicitly, by hand, in {@link lk.icbt.clinic.dao.StaffDao}.
 */
public class Staff {

    private Long id;
    private String username;
    private String passwordHash; // BCrypt hash; the plaintext is never stored
    private String fullName;
    private StaffRole role;
    private boolean active = true;
    private LocalDateTime createdAt;

    public Staff() {
    }

    public Staff(String username, String passwordHash, String fullName, StaffRole role) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.role = role;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public StaffRole getRole() { return role; }
    public void setRole(StaffRole role) { this.role = role; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

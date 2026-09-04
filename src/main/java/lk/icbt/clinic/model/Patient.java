package lk.icbt.clinic.model;

import java.time.LocalDateTime;

/** A patient of the clinic. Registered once, then reused across visits. */
public class Patient {

    private Long id;
    private String patientNo;
    private String name;
    private String address;
    private String contactNumber;
    private LocalDateTime createdAt;

    public Patient() {
    }

    public Patient(String patientNo, String name, String address, String contactNumber) {
        this.patientNo = patientNo;
        this.name = name;
        this.address = address;
        this.contactNumber = contactNumber;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPatientNo() { return patientNo; }
    public void setPatientNo(String patientNo) { this.patientNo = patientNo; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

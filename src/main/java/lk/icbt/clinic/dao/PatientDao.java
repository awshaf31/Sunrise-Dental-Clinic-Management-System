package lk.icbt.clinic.dao;

import lk.icbt.clinic.model.Patient;
import java.util.Optional;

public interface PatientDao {

    /** Used to reuse an existing record instead of registering a duplicate patient. */
    Optional<Patient> findByContactNumberAndName(String contactNumber, String name);

    Patient save(Patient patient);

    long count();
}

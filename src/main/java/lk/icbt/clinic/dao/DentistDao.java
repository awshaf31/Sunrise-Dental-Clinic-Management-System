package lk.icbt.clinic.dao;

import lk.icbt.clinic.model.Dentist;
import java.util.List;
import java.util.Optional;

public interface DentistDao {

    List<Dentist> findAllActive();

    Optional<Dentist> findById(Long id);
}

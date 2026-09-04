package lk.icbt.clinic.dao;

import lk.icbt.clinic.model.TreatmentType;
import java.util.List;
import java.util.Optional;

public interface TreatmentTypeDao {

    List<TreatmentType> findAllActive();

    Optional<TreatmentType> findById(Long id);
}

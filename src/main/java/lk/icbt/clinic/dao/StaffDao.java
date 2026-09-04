package lk.icbt.clinic.dao;

import lk.icbt.clinic.model.Staff;
import java.util.Optional;

/**
 * Data access for {@link Staff}.
 * <p>
 * Declared as an interface -- not because a framework requires it, but so
 * that {@code AuthService} can be unit-tested against a Mockito mock without
 * a real database, exactly the way the Repository pattern is meant to work.
 */
public interface StaffDao {

    Optional<Staff> findByUsernameAndActive(String username);

    Optional<Staff> findById(Long id);
}

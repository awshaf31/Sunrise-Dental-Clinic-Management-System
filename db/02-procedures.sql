-- Sunrise Dental Clinic — stored routines and triggers
--
-- Business rules that belong to the data rather than to one application:
-- the audit trail must be written no matter which client changes a row, and the
-- revenue aggregation is cheaper to run where the data already lives than to
-- pull every bill across the network and total it in Java.

USE sunrise_dental;

DROP TRIGGER IF EXISTS trg_appointment_status_audit;
DROP PROCEDURE IF EXISTS sp_daily_revenue;
DROP FUNCTION IF EXISTS fn_dentist_is_free;

DELIMITER $$

-- ---------------------------------------------------------------------------
-- Trigger: record every appointment status change
-- ---------------------------------------------------------------------------
CREATE TRIGGER trg_appointment_status_audit
    AFTER UPDATE ON appointment
    FOR EACH ROW
BEGIN
    IF NEW.status <> OLD.status THEN
        INSERT INTO appointment_audit (appointment_id, appointment_no, old_status, new_status)
        VALUES (NEW.id, NEW.appointment_no, OLD.status, NEW.status);
    END IF;
END$$

-- ---------------------------------------------------------------------------
-- Function: is a dentist free at a given slot?
-- Used by the reporting screens; the authoritative guarantee remains the
-- uk_dentist_slot unique constraint, not this function.
-- ---------------------------------------------------------------------------
CREATE FUNCTION fn_dentist_is_free(
    p_dentist_id BIGINT,
    p_date       DATE,
    p_time       TIME
)
    RETURNS BOOLEAN
    -- READS SQL DATA, and deliberately NOT DETERMINISTIC: the answer depends on
    -- the contents of the appointment table, so the same arguments can return
    -- different results. Declaring it deterministic would invite the optimiser
    -- to cache a result that has since become wrong.
    READS SQL DATA
BEGIN
    DECLARE v_count INT;

    SELECT COUNT(*) INTO v_count
    FROM appointment
    WHERE dentist_id = p_dentist_id
      AND appointment_date = p_date
      AND appointment_time = p_time
      AND status <> 'CANCELLED';

    RETURN v_count = 0;
END$$

-- ---------------------------------------------------------------------------
-- Procedure: daily revenue breakdown, for the management report
-- ---------------------------------------------------------------------------
CREATE PROCEDURE sp_daily_revenue(IN p_date DATE)
BEGIN
    SELECT tt.name                        AS treatment_name,
           COUNT(b.id)                    AS bills_issued,
           SUM(b.consultation_fee)        AS consultation_total,
           SUM(b.treatment_fee)           AS treatment_total,
           SUM(b.total_amount)            AS revenue_total
    FROM bill b
             JOIN appointment a ON a.id = b.appointment_id
             JOIN treatment_type tt ON tt.id = a.treatment_type_id
    WHERE DATE(b.issued_at) = p_date
    GROUP BY tt.id, tt.name
    ORDER BY revenue_total DESC;
END$$

DELIMITER ;

-- Sunrise Dental Clinic — seed data
--
-- Staff passwords are stored as BCrypt hashes, never as plaintext. The hashes
-- below were produced with Spring Security's BCryptPasswordEncoder at the
-- default strength of 10; BCrypt salts each hash internally, which is why two
-- accounts with different passwords share no visible structure.
--
-- Demonstration credentials (for the marker, and for the report's walkthrough):
--     reception / Recept@123    role RECEPTIONIST
--     manager   / Manager@123   role MANAGER

USE sunrise_dental;

DELETE FROM appointment_audit;
DELETE FROM bill;
DELETE FROM appointment;
DELETE FROM patient;
DELETE FROM treatment_type;
DELETE FROM dentist;
DELETE FROM staff;
DELETE FROM clinic_setting;

INSERT INTO clinic_setting (setting_key, setting_value, description) VALUES
    ('CONSULTATION_FEE', '1500.00', 'Flat consultation fee added to every bill, in LKR'),
    ('CLINIC_OPEN_TIME', '09:00',   'Earliest bookable appointment time'),
    ('CLINIC_CLOSE_TIME', '17:00',  'Latest bookable appointment time'),
    ('CLINIC_NAME', 'Sunrise Dental Clinic', 'Name printed on receipts');

INSERT INTO staff (username, password_hash, full_name, role) VALUES
    ('reception', '$2a$10$vgc5TPE66kdtaGqgMh4nUe7lV68rxrpTqkHCTsb0alFjfSYInfgxm', 'Nimali Perera',   'RECEPTIONIST'),
    ('manager',   '$2a$10$P636riVueA6LdO18wzy8le/yHRP/FFXhHNPqnPhIFvh4wCJehGr7q', 'Dr. Ruwan Silva', 'MANAGER');

INSERT INTO dentist (name, specialization) VALUES
    ('Dr. Ruwan Silva',    'General Dentistry'),
    ('Dr. Ayesha Fernando','Orthodontics'),
    ('Dr. Kasun Bandara',  'Oral Surgery');

INSERT INTO treatment_type (code, name, base_fee) VALUES
    ('CONSULT',    'Consultation Only',  0.00),
    ('CLEANING',   'Scaling & Cleaning', 3500.00),
    ('FILLING',    'Tooth Filling',      5000.00),
    ('EXTRACTION', 'Tooth Extraction',   7500.00),
    ('ROOT_CANAL', 'Root Canal Therapy', 25000.00),
    ('WHITENING',  'Teeth Whitening',    15000.00);

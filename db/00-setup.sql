-- Sunrise Dental Clinic — database and application user
--
-- Run this ONCE as the MySQL root user:
--     /usr/local/mysql/bin/mysql -u root -p < db/00-setup.sql
--
-- Before running, replace CHANGE_ME below with a password of your choosing.
-- That same password is what you export as DB_PASSWORD when starting clinic-api.
--
-- The application deliberately does NOT connect as root. It uses a dedicated
-- account holding only the privileges it actually needs on one schema, so a flaw
-- in the application cannot reach anything else on the server.

CREATE DATABASE IF NOT EXISTS sunrise_dental
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS 'clinic_app'@'localhost'
    IDENTIFIED BY 'CHANGE_ME';

GRANT SELECT, INSERT, UPDATE, DELETE, EXECUTE
    ON sunrise_dental.*
    TO 'clinic_app'@'localhost';

FLUSH PRIVILEGES;

SELECT 'sunrise_dental ready' AS status;

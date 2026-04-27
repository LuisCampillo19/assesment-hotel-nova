-- ============================================================================
-- HotelNova - PostgreSQL schema (Neon-compatible)
-- ============================================================================
-- Run this script ONCE in the Neon SQL Editor to create all tables and seed
-- the minimum data needed by the application.
--
-- Tables are created in dependency order so foreign keys resolve correctly
-- on the first run.
--
-- To reset the database completely (CAREFUL: this deletes all data),
-- uncomment the four DROP statements below before running the script again.
-- ============================================================================

-- DROP TABLE IF EXISTS reservations CASCADE;
-- DROP TABLE IF EXISTS rooms        CASCADE;
-- DROP TABLE IF EXISTS guests       CASCADE;
-- DROP TABLE IF EXISTS users        CASCADE;


-- ----------------------------------------------------------------------------
-- 1. users  -  hotel staff that operates the system
-- ----------------------------------------------------------------------------
-- Distinct from guests: these are the people who LOG IN to the application
-- (administrators, receptionists). Passwords are stored as SHA-256 hashes
-- never in plain text. The CHECK constraint pins the role catalog to two
-- values that the application understands.
-- ----------------------------------------------------------------------------
CREATE TABLE users (
    id              SERIAL          PRIMARY KEY,
    username        VARCHAR(50)     NOT NULL UNIQUE,
    password_hash   VARCHAR(255)    NOT NULL,
    role            VARCHAR(20)     NOT NULL,
    full_name       VARCHAR(150)    NOT NULL,
    email           VARCHAR(150)    NOT NULL UNIQUE,
    active          BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_users_role CHECK (role IN ('ADMIN', 'RECEPTIONIST'))
);


-- ----------------------------------------------------------------------------
-- 2. guests  -  people who stay at the hotel
-- ----------------------------------------------------------------------------
-- Distinct from users: a guest never logs in. They are the customers.
-- The "active" flag lets the business deactivate problematic guests without
-- deleting them (preserves historical reservations through the foreign key).
-- ----------------------------------------------------------------------------
CREATE TABLE guests (
    id                SERIAL         PRIMARY KEY,
    document_number   VARCHAR(30)    NOT NULL UNIQUE,
    first_name        VARCHAR(100)   NOT NULL,
    last_name         VARCHAR(100)   NOT NULL,
    phone             VARCHAR(20),
    email             VARCHAR(150),
    active            BOOLEAN        NOT NULL DEFAULT TRUE,
    created_at        TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP
);


-- ----------------------------------------------------------------------------
-- 3. rooms  -  physical hotel rooms
-- ----------------------------------------------------------------------------
-- room_number is the public-facing identifier ("101", "203", etc.) and is
-- enforced as UNIQUE so the application can rely on it for lookups.
-- The "status" column is intentionally stored (not derived) so check-in and
-- check-out are simple, atomic UPDATEs inside their JDBC transactions.
-- ----------------------------------------------------------------------------
CREATE TABLE rooms (
    id                SERIAL         PRIMARY KEY,
    room_number       VARCHAR(10)    NOT NULL UNIQUE,
    type              VARCHAR(20)    NOT NULL,
    price_per_night   NUMERIC(10,2)  NOT NULL,
    status            VARCHAR(20)    NOT NULL DEFAULT 'AVAILABLE',
    CONSTRAINT chk_rooms_type   CHECK (type   IN ('SINGLE', 'DOUBLE', 'SUITE')),
    CONSTRAINT chk_rooms_status CHECK (status IN ('AVAILABLE', 'OCCUPIED', 'MAINTENANCE')),
    CONSTRAINT chk_rooms_price  CHECK (price_per_night > 0)
);


-- ----------------------------------------------------------------------------
-- 4. reservations  -  bookings (central transactional table)
-- ----------------------------------------------------------------------------
-- check_in_date / check_out_date are the planned dates.
-- actual_check_in / actual_check_out are timestamps filled when the guest
-- physically arrives or leaves; both are NULL until those events happen.
-- total_cost is computed once at creation: nights * price_per_night * (1+IVA).
--
-- Status lifecycle:
--   PENDING    -> reservation created, guest has not arrived yet
--   ACTIVE     -> guest checked in (actual_check_in is set)
--   COMPLETED  -> guest checked out (actual_check_out is set)
--   CANCELLED  -> reservation voided before check-in
--
-- Note on overlap prevention:
--   This schema does NOT enforce overlap with an EXCLUDE constraint
--   (which PostgreSQL supports via btree_gist + daterange). The validation
--   lives in the Java service layer because the rubric requires a JUnit
--   test for it. A DB-level constraint could be added later as defense in
--   depth without changing application behaviour.
-- ----------------------------------------------------------------------------
CREATE TABLE reservations (
    id                 SERIAL         PRIMARY KEY,
    guest_id           INT            NOT NULL,
    room_id            INT            NOT NULL,
    user_id            INT            NOT NULL,
    check_in_date      DATE           NOT NULL,
    check_out_date     DATE           NOT NULL,
    actual_check_in    TIMESTAMP,
    actual_check_out   TIMESTAMP,
    status             VARCHAR(20)    NOT NULL DEFAULT 'PENDING',
    total_cost         NUMERIC(12,2)  NOT NULL,
    created_at         TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_reservation_guest FOREIGN KEY (guest_id) REFERENCES guests(id),
    CONSTRAINT fk_reservation_room  FOREIGN KEY (room_id)  REFERENCES rooms(id),
    CONSTRAINT fk_reservation_user  FOREIGN KEY (user_id)  REFERENCES users(id),
    CONSTRAINT chk_reservation_dates  CHECK (check_out_date > check_in_date),
    CONSTRAINT chk_reservation_status CHECK (status IN ('PENDING', 'ACTIVE', 'COMPLETED', 'CANCELLED')),
    CONSTRAINT chk_reservation_cost   CHECK (total_cost >= 0)
);


-- ----------------------------------------------------------------------------
-- Indexes  -  speed up the queries the application runs most often
-- ----------------------------------------------------------------------------
-- The composite index on (room_id, check_in_date, check_out_date) is the
-- one that matters most: it accelerates the overlap check executed before
-- creating every new reservation.
-- ----------------------------------------------------------------------------
CREATE INDEX idx_reservations_room_dates ON reservations(room_id, check_in_date, check_out_date);
CREATE INDEX idx_reservations_guest      ON reservations(guest_id);
CREATE INDEX idx_reservations_status     ON reservations(status);
CREATE INDEX idx_rooms_status            ON rooms(status);
CREATE INDEX idx_guests_active           ON guests(active);


-- ============================================================================
-- Seed data  -  minimum rows so the app is usable right after first launch
-- ============================================================================

-- Default administrator
--   username: admin
--   password: admin123
-- The hash below is SHA-256("admin123"). The Java PasswordEncoder will
-- produce the same hash and the login will succeed out of the box.
-- ----------------------------------------------------------------------------
INSERT INTO users (username, password_hash, role, full_name, email) VALUES
    ('admin',
     '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9',
     'ADMIN',
     'Default Administrator',
     'admin@hotelnova.local');


-- Sample rooms across the three supported types
-- ----------------------------------------------------------------------------
INSERT INTO rooms (room_number, type, price_per_night) VALUES
    ('101', 'SINGLE', 120000.00),
    ('102', 'SINGLE', 120000.00),
    ('201', 'DOUBLE', 180000.00),
    ('301', 'SUITE',  350000.00);


-- Sample guests for manual testing
-- ----------------------------------------------------------------------------
INSERT INTO guests (document_number, first_name, last_name, phone, email) VALUES
    ('1001234567', 'Maria',  'Rodriguez', '3001112233', 'maria@example.com'),
    ('1009876543', 'Carlos', 'Gomez',     '3004445566', 'carlos@example.com');


-- ============================================================================
-- Verification queries  -  run these after the script to confirm setup
-- ============================================================================
-- SELECT COUNT(*) FROM users;          -- expected: 1
-- SELECT COUNT(*) FROM rooms;          -- expected: 4
-- SELECT COUNT(*) FROM guests;         -- expected: 2
-- SELECT COUNT(*) FROM reservations;   -- expected: 0

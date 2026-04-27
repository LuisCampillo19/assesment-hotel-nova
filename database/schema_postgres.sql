-- HotelNova - PostgreSQL schema (Neon-compatible)

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

INSERT INTO users (username, password_hash, role, full_name, email) VALUES
    ('admin',
     '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9',
     'ADMIN',
     'Default Administrator',
     'admin@hotelnova.local');


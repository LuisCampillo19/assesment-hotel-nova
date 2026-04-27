# HotelNova

> Hotel reservation management system. Java 17 + JDBC + PostgreSQL (Neon).

This README will be expanded with full setup instructions, architecture
notes, screenshots and run instructions in a later commit.

For now: see `database/schema_postgres.sql` to provision the database
and `src/main/resources/database.properties.example` for connection setup.


```mermaid
classDiagram
    direction TB

    %% ============ MODEL ============
    class User {
        -int id
        -String username
        -String passwordHash
        -UserRole role
        -String fullName
        -boolean active
    }
    class Guest {
        -int id
        -String documentNumber
        -String firstName
        -String lastName
        -boolean active
        +getFullName() String
    }
    class Room {
        -int id
        -String roomNumber
        -RoomType type
        -BigDecimal pricePerNight
        -RoomStatus status
    }
    class Reservation {
        -int id
        -int guestId
        -int roomId
        -int userId
        -LocalDate checkInDate
        -LocalDate checkOutDate
        -ReservationStatus status
        -BigDecimal totalCost
        +nights() long
    }

    %% ============ DAO ============
    class GenericDao~T,ID~ {
        <<interface>>
        +save(T) T
        +findById(ID) Optional~T~
        +findAll() List~T~
        +update(T) boolean
        +deleteById(ID) boolean
    }
    class UserDao {
        <<interface>>
        +findByUsername(String) Optional~User~
    }
    class GuestDao {
        <<interface>>
        +findByDocumentNumber(String) Optional~Guest~
    }
    class RoomDao {
        <<interface>>
        +findByRoomNumber(String) Optional~Room~
        +findAvailable() List~Room~
        +updateStatus(...) boolean
    }
    class ReservationDao {
        <<interface>>
        +countOverlapping(...) int
        +findByGuestId(int) List~Reservation~
        +save(Connection, Reservation) Reservation
    }

    %% ============ SERVICE ============
    class AuthService {
        -UserDao userDao
        +login(username, password) User
    }
    class RoomService {
        -RoomDao roomDao
        +createRoom(Room) Room
        +findAvailable() List~Room~
    }
    class GuestService {
        -GuestDao guestDao
        +createGuest(Guest) Guest
        +deactivate(int) boolean
    }
    class ReservationService {
        -ReservationDao reservationDao
        -RoomDao roomDao
        -GuestDao guestDao
        +createReservation(...) Reservation
        +checkIn(int) void
        +checkOut(int) void
    }

    %% ============ VIEW ============
    class View {
        <<interface>>
        +showMessage(String) void
        +askString(String) String
        +showMenu(...) int
    }
    class ConsoleView
    class JOptionPaneView

    %% ============ EXCEPTIONS ============
    class BusinessException
    class DuplicateRoomNumberException
    class RoomNotAvailableException
    class InactiveGuestException
    class OverlappingReservationException
    class CheckoutWithoutCheckinException
    class InvalidDateRangeException

    %% ============ INHERITANCE ============
    GenericDao <|-- UserDao
    GenericDao <|-- GuestDao
    GenericDao <|-- RoomDao
    GenericDao <|-- ReservationDao

    View <|.. ConsoleView
    View <|.. JOptionPaneView

    BusinessException <|-- DuplicateRoomNumberException
    BusinessException <|-- RoomNotAvailableException
    BusinessException <|-- InactiveGuestException
    BusinessException <|-- OverlappingReservationException
    BusinessException <|-- CheckoutWithoutCheckinException
    BusinessException <|-- InvalidDateRangeException

    %% ============ DEPENDENCIES ============
    AuthService --> UserDao
    RoomService --> RoomDao
    GuestService --> GuestDao
    ReservationService --> ReservationDao
    ReservationService --> RoomDao
    ReservationService --> GuestDao

    Reservation --> Guest : guestId
    Reservation --> Room : roomId
    Reservation --> User : userId
```

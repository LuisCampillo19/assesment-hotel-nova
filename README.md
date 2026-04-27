# HotelNova

> Hotel reservation management system. Java 17 + JDBC + PostgreSQL (Neon).

This README will be expanded with full setup instructions, architecture
notes, screenshots and run instructions in a later commit.

For now: see `database/schema_postgres.sql` to provision the database
and `src/main/resources/database.properties.example` for connection setup.


```mermaid
classDiagram
    class User {
        -int id
        -String username
        -String passwordHash
        -UserRole role
        -String fullName
        -String email
        -boolean active
    }

    class Guest {
        -int id
        -String documentNumber
        -String firstName
        -String lastName
        -String phone
        -String email
        -boolean active
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

    class AuthService {
        -UserDao userDao
        +login(username, password) User
    }

    class ReservationService {
        -ReservationDao reservationDao
        -RoomDao roomDao
        -GuestDao guestDao
        +createReservation(...) Reservation
        +checkIn(id) void
        +checkOut(id) void
    }

    class RoomService {
        -RoomDao roomDao
        +createRoom(Room) Room
        +findAvailable() List
    }

    class GuestService {
        -GuestDao guestDao
        +createGuest(Guest) Guest
        +deactivate(id) boolean
    }

    class GenericDao~T,ID~ {
        <>
        +save(T) T
        +findById(ID) Optional
        +findAll() List
        +update(T) boolean
        +deleteById(ID) boolean
    }

    class UserDao {
        <>
        +findByUsername(String) Optional
    }

    class GuestDao {
        <>
        +findByDocumentNumber(String) Optional
    }

    class RoomDao {
        <>
        +findByRoomNumber(String) Optional
        +findAvailable() List
        +updateStatus(...) boolean
    }

    class ReservationDao {
        <>
        +countOverlapping(...) int
        +findByGuestId(int) List
    }

    class View {
        <>
        +showMessage(String)
        +askString(String) String
        +showMenu(...) int
    }

    class ConsoleView
    class JOptionPaneView

    GenericDao  UserDao
    RoomService --> RoomDao
    GuestService --> GuestDao
    ReservationService --> ReservationDao
    ReservationService --> RoomDao
    ReservationService --> GuestDao

    Reservation --> Guest : guestId
    Reservation --> Room : roomId
    Reservation --> User : userId
```

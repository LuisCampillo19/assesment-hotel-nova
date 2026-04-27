package com.luiscampillo.hotelnova.dao.impl;

import com.luiscampillo.hotelnova.dao.ReservationDao;
import com.luiscampillo.hotelnova.model.entity.Reservation;
import com.luiscampillo.hotelnova.model.enums.ReservationStatus;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReservationDaoImpl extends AbstractGenericDao<Reservation, Integer>
        implements ReservationDao {

    private static final String INSERT =
            "INSERT INTO reservations (guest_id, room_id, user_id, check_in_date, " +
            "check_out_date, actual_check_in, actual_check_out, status, total_cost) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String UPDATE =
            "UPDATE reservations SET guest_id=?, room_id=?, user_id=?, check_in_date=?, " +
            "check_out_date=?, actual_check_in=?, actual_check_out=?, status=?, " +
            "total_cost=? WHERE id=?";
    private static final String DELETE          = "DELETE FROM reservations WHERE id=?";
    private static final String FIND_BY_ID      = "SELECT * FROM reservations WHERE id=?";
    private static final String FIND_ALL        = "SELECT * FROM reservations ORDER BY id";
    private static final String FIND_BY_GUEST   = "SELECT * FROM reservations WHERE guest_id=? ORDER BY id";
    private static final String FIND_BY_ROOM    = "SELECT * FROM reservations WHERE room_id=? ORDER BY id";
    private static final String FIND_BY_STATUS  = "SELECT * FROM reservations WHERE status=? ORDER BY id";
    private static final String COUNT_OVERLAP =
            "SELECT COUNT(*) FROM reservations " +
            "WHERE room_id = ? " +
            "  AND status IN ('PENDING', 'ACTIVE') " +
            "  AND check_in_date < ? " +
            "  AND check_out_date > ?";

    @Override
    protected Reservation mapRow(ResultSet rs) throws SQLException {
        Timestamp tsCheckIn  = rs.getTimestamp("actual_check_in");
        Timestamp tsCheckOut = rs.getTimestamp("actual_check_out");
        Timestamp tsCreated  = rs.getTimestamp("created_at");

        return new Reservation(
                rs.getInt("id"),
                rs.getInt("guest_id"),
                rs.getInt("room_id"),
                rs.getInt("user_id"),
                rs.getDate("check_in_date").toLocalDate(),
                rs.getDate("check_out_date").toLocalDate(),
                tsCheckIn  != null ? tsCheckIn.toLocalDateTime()  : null,
                tsCheckOut != null ? tsCheckOut.toLocalDateTime() : null,
                ReservationStatus.valueOf(rs.getString("status")),
                rs.getBigDecimal("total_cost"),
                tsCreated  != null ? tsCreated.toLocalDateTime()  : null
        );
    }

    @Override protected String getInsertSql()   { return INSERT; }
    @Override protected String getUpdateSql()   { return UPDATE; }
    @Override protected String getDeleteSql()   { return DELETE; }
    @Override protected String getFindByIdSql() { return FIND_BY_ID; }
    @Override protected String getFindAllSql()  { return FIND_ALL; }

    @Override
    protected void setInsertParams(PreparedStatement ps, Reservation r) throws SQLException {
        ps.setInt(1, r.getGuestId());
        ps.setInt(2, r.getRoomId());
        ps.setInt(3, r.getUserId());
        ps.setDate(4, Date.valueOf(r.getCheckInDate()));
        ps.setDate(5, Date.valueOf(r.getCheckOutDate()));
        ps.setTimestamp(6, r.getActualCheckIn()  != null ? Timestamp.valueOf(r.getActualCheckIn())  : null);
        ps.setTimestamp(7, r.getActualCheckOut() != null ? Timestamp.valueOf(r.getActualCheckOut()) : null);
        ps.setString(8, r.getStatus().name());
        ps.setBigDecimal(9, r.getTotalCost());
    }

    @Override
    protected void setUpdateParams(PreparedStatement ps, Reservation r) throws SQLException {
        setInsertParams(ps, r);
        ps.setInt(10, r.getId());
    }

    @Override
    protected void setIdParam(PreparedStatement ps, Integer id) throws SQLException {
        ps.setInt(1, id);
    }

    @Override
    public Reservation save(Reservation r) {
        try (Connection conn = cm.getConnection()) {
            return saveInternal(conn, r);
        } catch (SQLException e) {
            throw new RuntimeException("Reservation save failed", e);
        }
    }

    @Override
    public Reservation save(Connection conn, Reservation r) {
        try {
            return saveInternal(conn, r);
        } catch (SQLException e) {
            throw new RuntimeException("Reservation save (tx) failed", e);
        }
    }

    private Reservation saveInternal(Connection conn, Reservation r) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
            setInsertParams(ps, r);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) r.setId(keys.getInt(1));
            }
            return r;
        }
    }

    @Override
    public boolean update(Connection conn, Reservation r) {
        try (PreparedStatement ps = conn.prepareStatement(UPDATE)) {
            setUpdateParams(ps, r);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Reservation update (tx) failed", e);
        }
    }

    @Override
    public List<Reservation> findByGuestId(int guestId) {
        return findByIntColumn(FIND_BY_GUEST, guestId);
    }

    @Override
    public List<Reservation> findByRoomId(int roomId) {
        return findByIntColumn(FIND_BY_ROOM, roomId);
    }

    @Override
    public List<Reservation> findByStatus(ReservationStatus status) {
        List<Reservation> out = new ArrayList<>();
        try (Connection conn = cm.getConnection();
             PreparedStatement ps = conn.prepareStatement(FIND_BY_STATUS)) {

            ps.setString(1, status.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(mapRow(rs));
            }
            return out;
        } catch (SQLException e) {
            throw new RuntimeException("findByStatus failed", e);
        }
    }

    @Override
    public int countOverlapping(int roomId, LocalDate checkIn, LocalDate checkOut) {
        try (Connection conn = cm.getConnection();
             PreparedStatement ps = conn.prepareStatement(COUNT_OVERLAP)) {

            ps.setInt(1, roomId);
            ps.setDate(2, Date.valueOf(checkOut));  // existing check_in_date < new checkOut
            ps.setDate(3, Date.valueOf(checkIn));   // existing check_out_date > new checkIn
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("countOverlapping failed", e);
        }
    }

    private List<Reservation> findByIntColumn(String sql, int value) {
        List<Reservation> out = new ArrayList<>();
        try (Connection conn = cm.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, value);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(mapRow(rs));
            }
            return out;
        } catch (SQLException e) {
            throw new RuntimeException("query failed: " + sql, e);
        }
    }
}

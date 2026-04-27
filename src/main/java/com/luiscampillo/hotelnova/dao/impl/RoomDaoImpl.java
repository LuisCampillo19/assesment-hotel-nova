package com.luiscampillo.hotelnova.dao.impl;

import com.luiscampillo.hotelnova.dao.RoomDao;
import com.luiscampillo.hotelnova.model.entity.Room;
import com.luiscampillo.hotelnova.model.enums.RoomStatus;
import com.luiscampillo.hotelnova.model.enums.RoomType;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RoomDaoImpl extends AbstractGenericDao<Room, Integer> implements RoomDao {

    private static final String INSERT =
            "INSERT INTO rooms (room_number, type, price_per_night, status) " +
            "VALUES (?, ?, ?, ?)";
    private static final String UPDATE =
            "UPDATE rooms SET room_number=?, type=?, price_per_night=?, status=? " +
            "WHERE id=?";
    private static final String DELETE          = "DELETE FROM rooms WHERE id=?";
    private static final String FIND_BY_ID      = "SELECT * FROM rooms WHERE id=?";
    private static final String FIND_ALL        = "SELECT * FROM rooms ORDER BY room_number";
    private static final String FIND_BY_NUMBER  = "SELECT * FROM rooms WHERE room_number=?";
    private static final String FIND_AVAILABLE  =
            "SELECT * FROM rooms WHERE status='AVAILABLE' ORDER BY room_number";
    private static final String UPDATE_STATUS   = "UPDATE rooms SET status=? WHERE id=?";

    @Override
    protected Room mapRow(ResultSet rs) throws SQLException {
        return new Room(
                rs.getInt("id"),
                rs.getString("room_number"),
                RoomType.valueOf(rs.getString("type")),
                rs.getBigDecimal("price_per_night"),
                RoomStatus.valueOf(rs.getString("status"))
        );
    }

    @Override protected String getInsertSql()   { return INSERT; }
    @Override protected String getUpdateSql()   { return UPDATE; }
    @Override protected String getDeleteSql()   { return DELETE; }
    @Override protected String getFindByIdSql() { return FIND_BY_ID; }
    @Override protected String getFindAllSql()  { return FIND_ALL; }

    @Override
    protected void setInsertParams(PreparedStatement ps, Room r) throws SQLException {
        ps.setString(1, r.getRoomNumber());
        ps.setString(2, r.getType().name());
        ps.setBigDecimal(3, r.getPricePerNight());
        ps.setString(4, r.getStatus().name());
    }

    @Override
    protected void setUpdateParams(PreparedStatement ps, Room r) throws SQLException {
        setInsertParams(ps, r);
        ps.setInt(5, r.getId());
    }

    @Override
    protected void setIdParam(PreparedStatement ps, Integer id) throws SQLException {
        ps.setInt(1, id);
    }

    @Override
    public Room save(Room r) {
        try (Connection conn = cm.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {

            setInsertParams(ps, r);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) r.setId(keys.getInt(1));
            }
            return r;
        } catch (SQLException e) {
            throw new RuntimeException("Room save failed", e);
        }
    }

    @Override
    public Optional<Room> findByRoomNumber(String roomNumber) {
        try (Connection conn = cm.getConnection();
             PreparedStatement ps = conn.prepareStatement(FIND_BY_NUMBER)) {

            ps.setString(1, roomNumber);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("findByRoomNumber failed", e);
        }
    }

    @Override
    public List<Room> findAvailable() {
        List<Room> out = new ArrayList<>();
        try (Connection conn = cm.getConnection();
             PreparedStatement ps = conn.prepareStatement(FIND_AVAILABLE);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) out.add(mapRow(rs));
            return out;
        } catch (SQLException e) {
            throw new RuntimeException("findAvailable failed", e);
        }
    }

    @Override
    public boolean updateStatus(int roomId, RoomStatus status) {
        try (Connection conn = cm.getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE_STATUS)) {

            ps.setString(1, status.name());
            ps.setInt(2, roomId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("updateStatus failed", e);
        }
    }

    /**
     * Transactional overload: uses the supplied Connection so this update
     * commits or rolls back together with the surrounding service operation.
     * Note that the PreparedStatement IS closed (via try-with-resources) but
     * the Connection is NOT - the caller owns its lifecycle.
     */
    @Override
    public boolean updateStatus(Connection conn, int roomId, RoomStatus status) {
        try (PreparedStatement ps = conn.prepareStatement(UPDATE_STATUS)) {
            ps.setString(1, status.name());
            ps.setInt(2, roomId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("updateStatus (tx) failed", e);
        }
    }
}

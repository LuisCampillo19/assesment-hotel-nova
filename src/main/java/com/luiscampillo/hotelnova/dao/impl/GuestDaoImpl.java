package com.luiscampillo.hotelnova.dao.impl;

import com.luiscampillo.hotelnova.dao.GuestDao;
import com.luiscampillo.hotelnova.model.entity.Guest;

import java.sql.*;
import java.util.Optional;

public class GuestDaoImpl extends AbstractGenericDao<Guest, Integer> implements GuestDao {

    private static final String INSERT =
            "INSERT INTO guests (document_number, first_name, last_name, phone, email, active) " +
            "VALUES (?, ?, ?, ?, ?, ?)";
    private static final String UPDATE =
            "UPDATE guests SET document_number=?, first_name=?, last_name=?, " +
            "phone=?, email=?, active=? WHERE id=?";
    private static final String DELETE       = "DELETE FROM guests WHERE id=?";
    private static final String FIND_BY_ID   = "SELECT * FROM guests WHERE id=?";
    private static final String FIND_ALL     = "SELECT * FROM guests ORDER BY id";
    private static final String FIND_BY_DOC  = "SELECT * FROM guests WHERE document_number=?";

    @Override
    protected Guest mapRow(ResultSet rs) throws SQLException {
        Timestamp createdAt = rs.getTimestamp("created_at");
        return new Guest(
                rs.getInt("id"),
                rs.getString("document_number"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("phone"),
                rs.getString("email"),
                rs.getBoolean("active"),
                createdAt != null ? createdAt.toLocalDateTime() : null
        );
    }

    @Override protected String getInsertSql()   { return INSERT; }
    @Override protected String getUpdateSql()   { return UPDATE; }
    @Override protected String getDeleteSql()   { return DELETE; }
    @Override protected String getFindByIdSql() { return FIND_BY_ID; }
    @Override protected String getFindAllSql()  { return FIND_ALL; }

    @Override
    protected void setInsertParams(PreparedStatement ps, Guest g) throws SQLException {
        ps.setString(1, g.getDocumentNumber());
        ps.setString(2, g.getFirstName());
        ps.setString(3, g.getLastName());
        ps.setString(4, g.getPhone());
        ps.setString(5, g.getEmail());
        ps.setBoolean(6, g.isActive());
    }

    @Override
    protected void setUpdateParams(PreparedStatement ps, Guest g) throws SQLException {
        setInsertParams(ps, g);
        ps.setInt(7, g.getId());
    }

    @Override
    protected void setIdParam(PreparedStatement ps, Integer id) throws SQLException {
        ps.setInt(1, id);
    }

    @Override
    public Guest save(Guest g) {
        try (Connection conn = cm.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {

            setInsertParams(ps, g);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) g.setId(keys.getInt(1));
            }
            return g;
        } catch (SQLException e) {
            throw new RuntimeException("Guest save failed", e);
        }
    }

    @Override
    public Optional<Guest> findByDocumentNumber(String documentNumber) {
        try (Connection conn = cm.getConnection();
             PreparedStatement ps = conn.prepareStatement(FIND_BY_DOC)) {

            ps.setString(1, documentNumber);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("findByDocumentNumber failed", e);
        }
    }
}

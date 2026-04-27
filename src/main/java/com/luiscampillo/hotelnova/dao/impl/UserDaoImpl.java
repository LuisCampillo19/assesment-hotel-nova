package com.luiscampillo.hotelnova.dao.impl;

import com.luiscampillo.hotelnova.dao.UserDao;
import com.luiscampillo.hotelnova.model.entity.User;
import com.luiscampillo.hotelnova.model.enums.UserRole;

import java.sql.*;
import java.util.Optional;

public class UserDaoImpl extends AbstractGenericDao<User, Integer> implements UserDao {

    private static final String INSERT =
            "INSERT INTO users (username, password_hash, role, full_name, email, active) " +
            "VALUES (?, ?, ?, ?, ?, ?)";
    private static final String UPDATE =
            "UPDATE users SET username=?, password_hash=?, role=?, full_name=?, " +
            "email=?, active=? WHERE id=?";
    private static final String DELETE       = "DELETE FROM users WHERE id=?";
    private static final String FIND_BY_ID   = "SELECT * FROM users WHERE id=?";
    private static final String FIND_ALL     = "SELECT * FROM users ORDER BY id";
    private static final String FIND_BY_USER = "SELECT * FROM users WHERE username=?";

    @Override
    protected User mapRow(ResultSet rs) throws SQLException {
        Timestamp createdAt = rs.getTimestamp("created_at");
        return new User(
                rs.getInt("id"),
                rs.getString("username"),
                rs.getString("password_hash"),
                UserRole.valueOf(rs.getString("role")),
                rs.getString("full_name"),
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
    protected void setInsertParams(PreparedStatement ps, User u) throws SQLException {
        ps.setString(1, u.getUsername());
        ps.setString(2, u.getPasswordHash());
        ps.setString(3, u.getRole().name());
        ps.setString(4, u.getFullName());
        ps.setString(5, u.getEmail());
        ps.setBoolean(6, u.isActive());
    }

    @Override
    protected void setUpdateParams(PreparedStatement ps, User u) throws SQLException {
        setInsertParams(ps, u);
        ps.setInt(7, u.getId());
    }

    @Override
    protected void setIdParam(PreparedStatement ps, Integer id) throws SQLException {
        ps.setInt(1, id);
    }

    @Override
    public User save(User u) {
        try (Connection conn = cm.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {

            setInsertParams(ps, u);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) u.setId(keys.getInt(1));
            }
            return u;
        } catch (SQLException e) {
            throw new RuntimeException("User save failed", e);
        }
    }

    @Override
    public Optional<User> findByUsername(String username) {
        try (Connection conn = cm.getConnection();
             PreparedStatement ps = conn.prepareStatement(FIND_BY_USER)) {

            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("findByUsername failed", e);
        }
    }
}

package com.luiscampillo.hotelnova.dao.impl;

import com.luiscampillo.hotelnova.dao.GenericDao;
import com.luiscampillo.hotelnova.db.ConnectionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Template-method base for every DAO implementation.
 *
 * Concrete subclasses provide:
 *   - the SQL strings (insert / update / delete / find-by-id / find-all)
 *   - the parameter binding for each statement
 *   - the row-to-entity mapping
 *
 * The CRUD methods themselves are implemented here once and reused by all
 * DAOs, which keeps each concrete DAO focused on its entity-specific bits.
 *
 * Every JDBC resource (Connection, PreparedStatement, ResultSet) is acquired
 * inside try-with-resources so they are closed even when an exception is
 * thrown. SQLException is wrapped in RuntimeException so service code does
 * not have to declare "throws" for every operation.
 */
public abstract class AbstractGenericDao<T, ID> implements GenericDao<T, ID> {

    protected final ConnectionManager cm = ConnectionManager.getInstance();

    // ---- Hooks the subclass MUST implement -------------------------------

    protected abstract T      mapRow(ResultSet rs) throws SQLException;
    protected abstract String getInsertSql();
    protected abstract String getUpdateSql();
    protected abstract String getDeleteSql();
    protected abstract String getFindByIdSql();
    protected abstract String getFindAllSql();

    protected abstract void   setInsertParams(PreparedStatement ps, T entity) throws SQLException;
    protected abstract void   setUpdateParams(PreparedStatement ps, T entity) throws SQLException;
    protected abstract void   setIdParam(PreparedStatement ps, ID id)         throws SQLException;

    // ---- Generic CRUD reused by every concrete DAO -----------------------

    @Override
    public Optional<T> findById(ID id) {
        try (Connection conn = cm.getConnection();
             PreparedStatement ps = conn.prepareStatement(getFindByIdSql())) {

            setIdParam(ps, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("findById failed", e);
        }
    }

    @Override
    public List<T> findAll() {
        List<T> out = new ArrayList<>();
        try (Connection conn = cm.getConnection();
             PreparedStatement ps = conn.prepareStatement(getFindAllSql());
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) out.add(mapRow(rs));
            return out;
        } catch (SQLException e) {
            throw new RuntimeException("findAll failed", e);
        }
    }

    @Override
    public boolean update(T entity) {
        try (Connection conn = cm.getConnection();
             PreparedStatement ps = conn.prepareStatement(getUpdateSql())) {

            setUpdateParams(ps, entity);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("update failed", e);
        }
    }

    @Override
    public boolean deleteById(ID id) {
        try (Connection conn = cm.getConnection();
             PreparedStatement ps = conn.prepareStatement(getDeleteSql())) {

            setIdParam(ps, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("deleteById failed", e);
        }
    }
}

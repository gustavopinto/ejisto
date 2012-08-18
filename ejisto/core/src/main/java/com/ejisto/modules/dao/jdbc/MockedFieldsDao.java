/*
 * Ejisto, a powerful developer assistant
 *
 * Copyright (C) 2010-2012  Celestino Bellone
 *
 * Ejisto is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Ejisto is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.ejisto.modules.dao.jdbc;

import com.ejisto.core.ApplicationException;
import com.ejisto.modules.dao.entities.MockedField;
import com.ejisto.modules.dao.entities.MockedFieldImpl;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;

import java.sql.*;
import java.util.Collection;
import java.util.List;

public class MockedFieldsDao extends BaseJdbcDao implements com.ejisto.modules.dao.MockedFieldsDao {
    private static final String LOAD_ALL = "SELECT * FROM MOCKEDFIELDS";
    private static final String LOAD_BY_CLASSNAME = "SELECT * FROM MOCKEDFIELDS WHERE CONTEXTPATH=? AND CLASSNAME = ? AND FIELDNAME = ?";
    private static final String LOAD_BY_CONTEXTPATH = "SELECT * FROM MOCKEDFIELDS WHERE CONTEXTPATH = ?";
    private static final String LOAD_BY_CONTEXTPATH_CLASSNAME = "SELECT * FROM MOCKEDFIELDS WHERE CONTEXTPATH = ? AND CLASSNAME = ?";
    private static final String COUNT_BY_CONTEXTPATH_CLASSNAME = "SELECT COUNT(*) FROM MOCKEDFIELDS WHERE CONTEXTPATH = ? AND CLASSNAME = ?";
    private static final String UPDATE = "UPDATE MOCKEDFIELDS SET CONTEXTPATH = ?, CLASSNAME = ? , FIELDNAME = ?, FIELDTYPE=?, FIELDVALUE=?,FIELDELEMENTTYPE=?, EXPRESSION=?, ACTIVE = ? WHERE ID=?";
    private static final String INSERT = "INSERT INTO MOCKEDFIELDS (CONTEXTPATH,CLASSNAME,FIELDNAME,FIELDTYPE,FIELDVALUE,FIELDELEMENTTYPE,EXPRESSION, ACTIVE) VALUES(?,?,?,?,?,?,?,?)";
    private static final String DELETE_CONTEXT = "DELETE FROM MOCKEDFIELDS WHERE CONTEXTPATH=?";

    @Override
    public List<MockedField> loadAll() {
        return getJdbcTemplate().query(LOAD_ALL, new RowMapper<MockedField>() {
            @Override
            public MockedField mapRow(ResultSet rs, int rowNum) throws SQLException {
                return loadFromResultSet(rs);
            }
        });
    }

    @Override
    public Collection<MockedField> loadContextPathFields(String contextPath) {
        return getJdbcTemplate().query(LOAD_BY_CONTEXTPATH, new Object[]{contextPath}, new RowMapper<MockedField>() {
            @Override
            public MockedField mapRow(ResultSet rs, int rowNum) throws SQLException {
                return loadFromResultSet(rs);
            }
        });
    }

    @Override
    public List<MockedField> loadByContextPathAndClassName(String contextPath, String className) {
        return getJdbcTemplate().query(LOAD_BY_CONTEXTPATH_CLASSNAME, new Object[]{contextPath, className},
                                       new RowMapper<MockedField>() {
                                           @Override
                                           public MockedField mapRow(ResultSet rs, int rowNum) throws SQLException {
                                               return loadFromResultSet(rs);
                                           }
                                       });
    }

    @Override
    public int countByContextPathAndClassName(String contextPath, String className) {
        return getJdbcTemplate().queryForInt(COUNT_BY_CONTEXTPATH_CLASSNAME, contextPath, className);
    }

    @Override
    public MockedField getMockedField(String contextPath, String className, String fieldName) {
        return getJdbcTemplate().query(LOAD_BY_CLASSNAME, new Object[]{contextPath, className, fieldName},
                                       new ResultSetExtractor<MockedField>() {
                                           @Override
                                           public MockedField extractData(ResultSet rs) throws SQLException, DataAccessException {
                                               if (rs.next()) {
                                                   return loadFromResultSet(rs);
                                               } else {
                                                   throw new ApplicationException("No mockedFields found.");
                                               }
                                           }
                                       });
    }

    @Override
    public boolean update(final MockedField field) {
        return getJdbcTemplate().update(new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
                PreparedStatement pstm = con.prepareStatement(UPDATE);
                pstm.setString(1, field.getContextPath());
                pstm.setString(2, field.getClassName());
                pstm.setString(3, field.getFieldName());
                pstm.setString(4, field.getFieldType());
                pstm.setString(5, field.getFieldValue());
                pstm.setString(6, field.getFieldElementType());
                pstm.setString(7, field.getExpression());
                pstm.setBoolean(8, field.isActive());
                pstm.setLong(9, field.getId());
                return pstm;
            }
        }) == 1;
    }

    @Override
    public long insert(final MockedField field) {
        GeneratedKeyHolder holder = new GeneratedKeyHolder();
        getJdbcTemplate().update(new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
                PreparedStatement pstm = con.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS);
                pstm.setString(1, field.getContextPath());
                pstm.setString(2, field.getClassName());
                pstm.setString(3, field.getFieldName());
                pstm.setString(4, field.getFieldType());
                pstm.setString(5, field.getFieldValue());
                pstm.setString(6, field.getFieldElementType());
                pstm.setString(7, field.getExpression());
                pstm.setBoolean(8, field.isActive());
                return pstm;
            }
        }, holder);
        return holder.getKey().longValue();
    }

    @Override
    public void insert(Collection<MockedField> mockedFields) {
        for (MockedField mockedField : mockedFields) {
            insert(mockedField);
        }
    }

    @Override
    public boolean deleteContext(final String contextPath) {
        return getJdbcTemplate().update(DELETE_CONTEXT, new PreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps) throws SQLException {
                ps.setString(1, contextPath);
            }
        }) > 0;
    }

    private MockedField loadFromResultSet(ResultSet rs) throws SQLException {
        MockedField mockedField = new MockedFieldImpl();
        mockedField.setId(rs.getLong("ID"));
        mockedField.setContextPath(rs.getString("CONTEXTPATH"));
        mockedField.setClassName(rs.getString("CLASSNAME"));
        mockedField.setFieldName(rs.getString("FIELDNAME"));
        mockedField.setFieldType(rs.getString("FIELDTYPE"));
        mockedField.setFieldValue(rs.getString("FIELDVALUE"));
        mockedField.setActive(rs.getBoolean("ACTIVE"));
        mockedField.setExpression(rs.getString("EXPRESSION"));
        mockedField.setFieldElementType(rs.getString("FIELDELEMENTTYPE"));
        return mockedField;
    }

}
package com.epam.project.dao;

import com.epam.project.builder.RowMapper;
import com.epam.project.entity.Identifiable;
import com.epam.project.exceptions.DaoException;

import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class AbstractDao<T extends Identifiable> {
    private Connection connection;

    protected AbstractDao(Connection connection){
        this.connection = connection;
    }

    protected List<T> executeQuery(String query, RowMapper<T> mapper, Object... params) throws DaoException{
        try(PreparedStatement statement = createStatement(query, params);
            ResultSet resultset = statement.executeQuery()) {
            List<T> entities = new ArrayList<>();
            while (resultset.next()){
                T entity = mapper.map(resultset);
                entities.add(entity);
            }
            return entities;
        } catch (SQLException e) {
           throw new DaoException(e);
        }
    }

    private PreparedStatement createStatement(String query, Object... params) throws SQLException{
        PreparedStatement statement = connection.prepareStatement(query);
        for(int i =1; i <= params.length; i++){
            statement.setObject(i,params[i - 1]);
        }
        System.out.println(statement);
        return statement;
    }

    public List<T> getAll() throws DaoException{
        String table = getTableName();
        RowMapper<T> mapper = (RowMapper<T>) RowMapper.create(table);
        return executeQuery("select * from " + table, mapper);
    }

    protected Optional<T> executeForSingleResult(String query, RowMapper<T> builder, Object... params) throws DaoException {
        List<T> items = executeQuery(query,builder,params);
        if(items.size() == 1){
            return Optional.of(items.get(0));
        } else if (items.size() > 1){
            throw new IllegalArgumentException("More than one record found");
        } else {
            return Optional.empty();
        }
    }

    protected abstract String getTableName();

}

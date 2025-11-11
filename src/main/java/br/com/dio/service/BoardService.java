package br.com.dio.service;

import br.com.dio.persistence.dao.BoardColumnDAO;
import br.com.dio.persistence.dao.BoardDAO;
import br.com.dio.persistence.entity.BoardEntity;
import lombok.AllArgsConstructor;

import java.sql.Connection;
import java.sql.SQLException;

@AllArgsConstructor
public class BoardService {

    private final Connection connection;

    public BoardEntity insert(final BoardEntity entity) throws SQLException {
        connection.setAutoCommit(false); 

        var dao = new BoardDAO(connection);
        var boardColumnDAO = new BoardColumnDAO(connection);

        try {
            dao.insert(entity);

            var columns = entity.getBoardColumns();
            if (columns == null || columns.isEmpty()) {
                throw new IllegalStateException("[Erro] O board precisa ter ao menos uma coluna.");
            }

            var linkedColumns = columns.stream().map(c -> {
                c.setBoard(entity);
                return c;
            }).toList();

            for (var column : linkedColumns) {
                boardColumnDAO.insert(column);
            }

            connection.commit();
            return entity;

        } catch (SQLException e) {
            connection.rollback();
            throw e;
        }
    }

    public boolean delete(final Long id) throws SQLException {
        connection.setAutoCommit(false); 

        var dao = new BoardDAO(connection);

        try {
            if (!dao.exists(id)) {
                return false;
            }

            dao.delete(id);
            connection.commit();
            return true;

        } catch (SQLException e) {
            connection.rollback();
            throw e;
        }
    }
}


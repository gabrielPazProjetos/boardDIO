package br.com.dio.dao;

import br.com.dio.persistence.config.ConnectionConfig;
import br.com.dio.persistence.dao.BoardDAO;
import br.com.dio.persistence.entity.BoardEntity;
import org.junit.jupiter.api.Test;

import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.*;

class BoardDAOTest {

    @Test
    void shouldInsertAndFindBoard() throws Exception {
        try (Connection connection = ConnectionConfig.getConnection()) {
            BoardDAO dao = new BoardDAO(connection);

            BoardEntity board = new BoardEntity();
            board.setName("Test Board");

            dao.insert(board);
            BoardEntity found = dao.findById(board.getId());

            assertNotNull(found);
            assertEquals("Test Board", found.getName());
        }
    }
}

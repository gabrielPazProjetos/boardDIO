package br.com.dio.service;

import br.com.dio.dto.BoardDetailsDTO;
import br.com.dio.persistence.config.ConnectionConfig;
import br.com.dio.service.BoardQueryService;
import org.junit.jupiter.api.Test;

import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.*;

class BoardQueryServiceTest {

    @Test
    void shouldReturnBoardDetails() throws Exception {
        try (Connection connection = ConnectionConfig.getConnection()) {
            BoardQueryService service = new BoardQueryService(connection);
            BoardDetailsDTO dto = service.findBoardDetails(1L);

            assertNotNull(dto);
            assertEquals(1L, dto.id());
        }
    }
}

package br.com.dio.ui;

import br.com.dio.dto.BoardColumnInfoDTO;
import br.com.dio.persistence.entity.BoardColumnEntity;
import br.com.dio.persistence.entity.BoardEntity;
import br.com.dio.persistence.entity.CardEntity;
import br.com.dio.service.BoardColumnQueryService;
import br.com.dio.service.BoardQueryService;
import br.com.dio.service.CardQueryService;
import br.com.dio.service.CardService;
import lombok.AllArgsConstructor;

import java.sql.SQLException;
import java.util.Scanner;

import static br.com.dio.persistence.config.ConnectionConfig.getConnection;

@AllArgsConstructor
public class BoardMenu {

    private final Scanner scanner = new Scanner(System.in).useDelimiter("\n");
    private final BoardEntity entity;

    public void execute() {
        try {
            System.out.printf("Board %s selecionado. Escolha uma operação:\n", entity.getId());
            int option = -1;
            while (option != 9) {
                System.out.println("1 - Criar um card");
                System.out.println("2 - Mover um card");
                System.out.println("3 - Bloquear um card");
                System.out.println("4 - Desbloquear um card");
                System.out.println("5 - Cancelar um card");
                System.out.println("6 - Ver board");
                System.out.println("7 - Ver coluna com cards");
                System.out.println("8 - Ver card");
                System.out.println("9 - Voltar para o menu anterior");
                System.out.println("10 - Sair");

                try {
                    option = Integer.parseInt(scanner.next());
                } catch (NumberFormatException e) {
                    System.out.println("Entrada inválida. Digite um número entre 1 e 10.");
                    continue;
                }

                switch (option) {
                    case 1 -> createCard();
                    case 2 -> moveCardToNextColumn();
                    case 3 -> blockCard();
                    case 4 -> unblockCard();
                    case 5 -> cancelCard();
                    case 6 -> showBoard();
                    case 7 -> showColumn();
                    case 8 -> showCard();
                    case 9 -> System.out.println("Retornando ao menu anterior...");
                    case 10 -> {
                        System.out.println("Encerrando o programa.");
                        System.exit(0);
                    }
                    default -> System.out.println("Opção inválida. Tente novamente.");
                }
            }
        } catch (SQLException ex) {
            System.out.println("Erro ao executar operação no board:");
            ex.printStackTrace();
            System.exit(0);
        }
    }

    private void createCard() throws SQLException {
        var card = new CardEntity();
        System.out.println("Informe o título do card:");
        card.setTitle(scanner.next());
        System.out.println("Informe a descrição do card:");
        card.setDescription(scanner.next());
        card.setBoardColumn(entity.getInitialColumn());

        try (var connection = getConnection()) {
            new CardService(connection).create(card);
            System.out.println("Card criado com sucesso.");
        }
    }

    private void moveCardToNextColumn() throws SQLException {
        System.out.println("Informe o ID do card que deseja mover:");
        long cardId = scanner.nextLong();

        var boardColumnsInfo = entity.getBoardColumns().stream()
                .map(c -> new BoardColumnInfoDTO(c.getId(), c.getOrder(), c.getKind()))
                .toList();

        try (var connection = getConnection()) {
            new CardService(connection).moveToNextColumn(cardId, boardColumnsInfo);
            System.out.println("Card movido com sucesso.");
        } catch (RuntimeException ex) {
            System.out.println("Erro: " + ex.getMessage());
        }
    }

    private void blockCard() throws SQLException {
        System.out.println("Informe o ID do card que será bloqueado:");
        long cardId = scanner.nextLong();
        System.out.println("Informe o motivo do bloqueio:");
        String reason = scanner.next();

        var boardColumnsInfo = entity.getBoardColumns().stream()
                .map(c -> new BoardColumnInfoDTO(c.getId(), c.getOrder(), c.getKind()))
                .toList();

        try (var connection = getConnection()) {
            new CardService(connection).block(cardId, reason, boardColumnsInfo);
            System.out.println("Card bloqueado.");
        } catch (RuntimeException ex) {
            System.out.println("Erro: " + ex.getMessage());
        }
    }

    private void unblockCard() throws SQLException {
        System.out.println("Informe o ID do card que será desbloqueado:");
        long cardId = scanner.nextLong();
        System.out.println("Informe o motivo do desbloqueio:");
        String reason = scanner.next();

        try (var connection = getConnection()) {
            new CardService(connection).unblock(cardId, reason);
            System.out.println("Card desbloqueado.");
        } catch (RuntimeException ex) {
            System.out.println("Erro: " + ex.getMessage());
        }
    }

    private void cancelCard() throws SQLException {
        System.out.println("Informe o ID do card que deseja cancelar:");
        long cardId = scanner.nextLong();
        var cancelColumn = entity.getCancelColumn();

        var boardColumnsInfo = entity.getBoardColumns().stream()
                .map(c -> new BoardColumnInfoDTO(c.getId(), c.getOrder(), c.getKind()))
                .toList();

        try (var connection = getConnection()) {
            new CardService(connection).cancel(cardId, cancelColumn.getId(), boardColumnsInfo);
            System.out.println("Card movido para a coluna de cancelamento.");
        } catch (RuntimeException ex) {
            System.out.println("Erro: " + ex.getMessage());
        }
    }

    private void showBoard() throws SQLException {
        try (var connection = getConnection()) {
            var optional = new BoardQueryService(connection).showBoardDetails(entity.getId());
            optional.ifPresent(board -> {
                System.out.printf("Board [%s] - %s\n", board.id(), board.name());
                board.columns().forEach(col ->
                        System.out.printf("Coluna [%s] tipo [%s] - %d cards\n",
                                col.name(), col.kind(), col.cardsAmount())
                );
            });
        }
    }

    private void showColumn() throws SQLException {
        var columnIds = entity.getBoardColumns().stream()
                .map(BoardColumnEntity::getId)
                .toList();

        long selectedColumnId = -1;
        while (!columnIds.contains(selectedColumnId)) {
            System.out.printf("Escolha uma coluna do board '%s' pelo ID:\n", entity.getName());
            entity.getBoardColumns().forEach(c ->
                    System.out.printf("%d - %s [%s]\n", c.getId(), c.getName(), c.getKind())
            );
            selectedColumnId = scanner.nextLong();
        }

        try (var connection = getConnection()) {
            var column = new BoardColumnQueryService(connection).findById(selectedColumnId);
            column.ifPresent(col -> {
                System.out.printf("Coluna %s [%s]\n", col.getName(), col.getKind());
                col.getCards().forEach(card -> {
                    System.out.printf("Card %d - %s\n", card.getId(), card.getTitle());
                    System.out.printf("Descrição: %s\n\n", card.getDescription());
                });
            });
        }
    }

    private void showCard() throws SQLException {
        System.out.println("Informe o ID do card que deseja visualizar:");
        long cardId = scanner.nextLong();

        try (var connection = getConnection()) {
            new CardQueryService(connection).findById(cardId)
                    .ifPresentOrElse(card -> {
                        System.out.printf("Card %d - %s\n", card.id(), card.title());
                        System.out.printf("Descrição: %s\n", card.description());
                        System.out.println(card.blocked()
                                ? "Está bloqueado. Motivo: " + card.blockReason()
                                : "Não está bloqueado.");
                        System.out.printf("Foi bloqueado %d vezes\n", card.blocksAmount());
                        System.out.printf("Está na coluna %d - %s\n", card.columnId(), card.columnName());
                    }, () -> System.out.printf("Card com ID %d não encontrado.\n", cardId));
        }
    }
}

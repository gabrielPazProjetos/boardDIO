package br.com.dio.ui;

import br.com.dio.persistence.entity.BoardColumnEntity;
import br.com.dio.persistence.entity.BoardColumnKindEnum;
import br.com.dio.persistence.entity.BoardEntity;
import br.com.dio.service.BoardQueryService;
import br.com.dio.service.BoardService;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static br.com.dio.persistence.config.ConnectionConfig.getConnection;

public class MainMenu {

    private final Scanner scanner = new Scanner(System.in).useDelimiter("\n");

    public void execute() throws SQLException {
        System.out.println("Bem-vindo ao Gerenciador de Boards!");
        System.out.println("Escolha uma das opções abaixo para continuar:\n");

        while (true) {
            System.out.println("1 - Criar um novo board");
            System.out.println("2 - Selecionar um board existente");
            System.out.println("3 - Excluir um board");
            System.out.println("4 - Sair");

            int option;
            try {
                option = Integer.parseInt(scanner.next());
            } catch (NumberFormatException e) {
                System.out.println("Entrada inválida. Digite um número entre 1 e 4.");
                continue;
            }

            switch (option) {
                case 1 -> createBoard();
                case 2 -> selectBoard();
                case 3 -> deleteBoard();
                case 4 -> {
                    System.out.println("Encerrando o programa. Até a próxima!");
                    System.exit(0);
                }
                default -> System.out.println("Opção inválida. Tente novamente.");
            }
        }
    }

    private void createBoard() throws SQLException {
        var entity = new BoardEntity();

        System.out.println("Informe o nome do seu board:");
        entity.setName(scanner.next());

        System.out.println("Deseja adicionar colunas além das 3 padrões? Digite a quantidade ou '0' para nenhuma:");
        int additionalColumns = scanner.nextInt();

        List<BoardColumnEntity> columns = new ArrayList<>();

        System.out.println("Informe o nome da coluna inicial:");
        columns.add(createColumn(scanner.next(), BoardColumnKindEnum.INITIAL, 0));

        for (int i = 0; i < additionalColumns; i++) {
            System.out.printf("Informe o nome da coluna de tarefa pendente %d:%n", i + 1);
            columns.add(createColumn(scanner.next(), BoardColumnKindEnum.PENDING, i + 1));
        }

        System.out.println("Informe o nome da coluna final:");
        columns.add(createColumn(scanner.next(), BoardColumnKindEnum.FINAL, additionalColumns + 1));

        System.out.println("Informe o nome da coluna de cancelamento:");
        columns.add(createColumn(scanner.next(), BoardColumnKindEnum.CANCEL, additionalColumns + 2));

        entity.setBoardColumns(columns);

        try (var connection = getConnection()) {
            var service = new BoardService(connection);
            service.insert(entity);
            System.out.printf("Board '%s' criado com sucesso!%n", entity.getName());
        }
    }

    private void selectBoard() throws SQLException {
        System.out.println("Informe o ID do board que deseja acessar:");
        long id = scanner.nextLong();

        try (var connection = getConnection()) {
            var queryService = new BoardQueryService(connection);
            var optional = queryService.findById(id);

            optional.ifPresentOrElse(
                board -> new BoardMenu(board).execute(),
                () -> System.out.printf("Nenhum board encontrado com o ID %d.%n", id)
            );
        }
    }

    private void deleteBoard() throws SQLException {
        System.out.println("Informe o ID do board que deseja excluir:");
        long id = scanner.nextLong();

        try (var connection = getConnection()) {
            var service = new BoardService(connection);
            boolean deleted = service.delete(id);

            if (deleted) {
                System.out.printf("Board %d excluído com sucesso.%n", id);
            } else {
                System.out.printf("Nenhum board encontrado com o ID %d.%n", id);
            }
        }
    }

    private BoardColumnEntity createColumn(String name, BoardColumnKindEnum kind, int order) {
        var column = new BoardColumnEntity();
        column.setName(name);
        column.setKind(kind);
        column.setOrder(order);
        return column;
    
        return boardColumn;
    }

}

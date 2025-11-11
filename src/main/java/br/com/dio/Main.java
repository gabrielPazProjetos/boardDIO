package br.com.dio;

import br.com.dio.persistence.migration.MigrationStrategy;
import br.com.dio.ui.MainMenu;

import java.sql.SQLException;

import static br.com.dio.persistence.config.ConnectionConfig.getConnection;

public class Main {

    public static void main(String[] args) throws SQLException {
        System.out.println("===============================================");
        System.out.println("Bem-vindo ao seu Board de Tarefas em Java!");
        System.out.println("Projeto desenvolvido no desafio DIO - Java Fundamentals");
        System.out.println("===============================================\n");

        try (var connection = getConnection()) {
            new MigrationStrategy(connection).executeMigration();
        }

        new MainMenu().execute();
    }
}

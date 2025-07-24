package com.example.feiraviva.Util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class DBConnection {

    // ✅ Conexão local à instância SQLEXPRESS, TCP/IP ativado, usando porta 1433
    private static final String URL =
            "jdbc:sqlserver://localhost:1433;" +
                    "databaseName=feira_viva;" +
                    "encrypt=true;" +
                    "trustServerCertificate=true;" +
                    "integratedSecurity=true";

    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(URL);
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao conectar ao SQL Server: " + e.getMessage(), e);
        }
    }
}
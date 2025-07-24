package com.example.feiraviva.Dao;

import com.example.feiraviva.Util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SessaoDAO {
    public void registarLogout(int utilizadorId) throws SQLException {
        String sql = "INSERT INTO sessoes_utilizador (utilizador_id, data_hora_logout) VALUES (?, GETDATE())";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, utilizadorId);
            stmt.executeUpdate();
        }
    }

    public List<LocalDateTime> listarUltimasSessoes(int utilizadorId, int limite) throws SQLException {
        List<LocalDateTime> sessoes = new ArrayList<>();
        String sql = "SELECT TOP (?) data_hora_logout FROM sessoes_utilizador WHERE utilizador_id = ? ORDER BY data_hora_logout DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(2, utilizadorId);
            stmt.setInt(1, limite);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                sessoes.add(rs.getTimestamp("data_hora_logout").toLocalDateTime());
            }
        }
        return sessoes;
    }
}
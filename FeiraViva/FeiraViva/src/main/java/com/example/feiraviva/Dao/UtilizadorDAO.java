package com.example.feiraviva.Dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.example.feiraviva.Model.Utilizador;
import com.example.feiraviva.Util.DBConnection;

import static com.example.feiraviva.Util.DBConnection.getConnection;

public class UtilizadorDAO {

    public void atualizarNome(int id, String novoNome) {
        String sql = "UPDATE utilizadores SET nome = ? WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, novoNome);
            stmt.setInt(2, id);
            stmt.executeUpdate();

            System.out.println("🟢 Nome atualizado com sucesso no SQL Server.");
        } catch (SQLException e) {
            System.out.println("❌ Erro ao atualizar nome.");
            e.printStackTrace();
        }
    }

    public void atualizarPassword(int id, String novaPassword) {
        String sql = "UPDATE utilizadores SET senha = ? WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, novaPassword);
            stmt.setInt(2, id);
            stmt.executeUpdate();

            System.out.println("🟢 Password atualizada com sucesso no SQL Server.");
        } catch (SQLException e) {
            System.out.println("❌ Erro ao atualizar password.");
            e.printStackTrace();
        }
    }

    public void atualizarImagemPerfil(int userId, byte[] imagem) throws SQLException {
        String sql = "UPDATE utilizadores SET imagem_perfil = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBytes(1, imagem);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        }
    }

    public Utilizador getUtilizadorPorId(int id) throws SQLException {
        String sql = "SELECT * FROM utilizadores WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Utilizador u = new Utilizador();
                // outros campos...
                u.setImagemPerfil(rs.getBytes("imagem_perfil"));
                return u;
            }
        }
        return null;
    }

    public void atualizarNotas(int userId, String notas) throws SQLException {
        String sql = "UPDATE utilizadores SET notas = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, notas);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        }
    }


}

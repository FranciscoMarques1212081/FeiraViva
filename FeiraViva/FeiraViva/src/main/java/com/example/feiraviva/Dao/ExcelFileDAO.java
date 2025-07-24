package com.example.feiraviva.Dao;

import com.example.feiraviva.Util.DBConnection;
import com.example.feiraviva.Model.ExcelFileUpload;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ExcelFileDAO {


    public static XSSFWorkbook getExcelFromDB(String nomeFicheiro) throws SQLException, IOException {
        String sql = "SELECT ficheiro FROM excel_files WHERE nome = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nomeFicheiro);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                byte[] fileBytes = rs.getBytes("ficheiro");
                try (InputStream is = new ByteArrayInputStream(fileBytes)) {
                    return new XSSFWorkbook(is);
                }
            }
        }
        return null;
    }

    public static void saveExcelToDB(String nomeFicheiro, int utilizadorId, XSSFWorkbook workbook) throws SQLException, IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        workbook.write(bos);
        byte[] fileBytes = bos.toByteArray();

        String sqlUpdate = "UPDATE excel_files SET ficheiro = ?, data_upload = GETDATE(), uploaded_by = ? WHERE nome = ?";
        String sqlInsert = "INSERT INTO excel_files (nome, ficheiro, uploaded_by) VALUES (?, ?, ?)";

        try (Connection conn = DBConnection.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(sqlUpdate)) {
                ps.setBytes(1, fileBytes);
                ps.setInt(2, utilizadorId);
                ps.setString(3, nomeFicheiro);
                int affected = ps.executeUpdate();
                if (affected == 0) {
                    try (PreparedStatement psi = conn.prepareStatement(sqlInsert)) {
                        psi.setString(1, nomeFicheiro);
                        psi.setBytes(2, fileBytes);
                        psi.setInt(3, utilizadorId);
                        psi.executeUpdate();
                    }
                }
            }
        }
    }

    public static List<ExcelFileUpload> listarUltimosUploads() throws SQLException {
        List<ExcelFileUpload> uploads = new ArrayList<>();
        String sql = "SELECT TOP 10 ef.nome, ef.data_upload, u.nome AS uploaded_by, u.imagem_perfil " +
                "FROM excel_files ef " +
                "LEFT JOIN utilizadores u ON ef.uploaded_by = u.id " +
                "ORDER BY ef.data_upload DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String nome = rs.getString("nome");
                Timestamp dataUpload = rs.getTimestamp("data_upload");
                String uploadedBy = rs.getString("uploaded_by");
                byte[] imagemPerfil = rs.getBytes("imagem_perfil"); // <- aqui!
                uploads.add(new ExcelFileUpload(
                        nome,
                        dataUpload != null ? dataUpload.toLocalDateTime() : null,
                        uploadedBy != null ? uploadedBy : "Desconhecido",
                        imagemPerfil
                ));
            }
        }
        return uploads;
    }


}
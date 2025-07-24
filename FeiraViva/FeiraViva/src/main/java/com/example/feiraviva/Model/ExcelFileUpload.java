package com.example.feiraviva.Model;

import java.time.LocalDateTime;

public class ExcelFileUpload {
    private String nome;
    private LocalDateTime dataUpload;
    private String uploadedBy;
    private byte[] imagemPerfil;

    public ExcelFileUpload(String nome, LocalDateTime dataUpload, String uploadedBy, byte[] imagemPerfil) {
        this.nome = nome;
        this.dataUpload = dataUpload;
        this.uploadedBy = uploadedBy;
        this.imagemPerfil = imagemPerfil;
    }

    public String getNome() { return nome; }
    public LocalDateTime getDataUpload() { return dataUpload; }
    public String getUploadedBy() { return uploadedBy; }

    public byte[] getImagemPerfil() { return imagemPerfil; }
}

package com.example.feiraviva.Model;

public class Utilizador {
    private int id;
    private String nome;
    private String email;
    private String senha;
    private String perfil;
    private byte[] imagemPerfil;
    private String notas;

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }

    public String getPerfil() { return perfil; }
    public void setPerfil(String perfil) { this.perfil = perfil; }

    public byte[] getImagemPerfil() { return imagemPerfil; }
    public void setImagemPerfil(byte[] imagemPerfil) { this.imagemPerfil = imagemPerfil; }

    public String getNotas() {
        return notas;
    }

    // Setter
    public void setNotas(String notas) {
        this.notas = notas;
    }
}

package com.example.feiraviva.Controller;

import com.example.feiraviva.Model.Utilizador;
import com.example.feiraviva.Util.AppSession;
import com.example.feiraviva.Util.DBConnection;
import com.example.feiraviva.Util.HashUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;

public class LoginController {

    @FXML
    private TextField loginUsernameTextField;

    @FXML
    private PasswordField loginPasswordPasswordField;

    @FXML
    private Label invalidLoginCredentials;

    // Evento do botão "Login"
    @FXML
    private void onLoginButtonClick() {
        String email = loginUsernameTextField.getText().trim();
        String senha = loginPasswordPasswordField.getText().trim();

        Utilizador user = autenticar(email, senha);

        if (user != null) {
            invalidLoginCredentials.setText("");
            AppSession.set("utilizador", user);
            abrirTelaPrincipal(user);
        } else {
            invalidLoginCredentials.setText("Credenciais inválidas.");
        }
    }

    // Pressionar ENTER também faz login
    @FXML
    private void CheckEnterKey(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            onLoginButtonClick();
        }
    }

    @FXML
    private void onCancelButtonClick() {
        Stage stage = (Stage) loginUsernameTextField.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void onDragButtonHandle() {
        // Arrastar janela (opcional)
    }

    @FXML
    private void onDragButtonPress() {
        // Pressionar arrastar (opcional)
    }

    // --- Autenticação SQL Server + SHA-256 ---
    private Utilizador autenticar(String email, String senha) {
        String sql = "SELECT * FROM utilizadores WHERE email = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String hashBD = rs.getString("senha");

                if (senha.equals(hashBD)) { // <- sem hash
                    Utilizador u = new Utilizador();
                    u.setId(rs.getInt("id"));
                    u.setNome(rs.getString("nome"));
                    u.setEmail(rs.getString("email"));
                    u.setSenha(hashBD);
                    u.setPerfil(rs.getString("perfil"));
                    u.setImagemPerfil(rs.getBytes("imagem_perfil"));
                    u.setNotas(rs.getString("notas"));
                    return u;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // --- Abre a tela principal após autenticação ---
    private void abrirTelaPrincipal(Utilizador user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/fxml/feiraviva/HomePage.fxml"));
            Parent root = loader.load();

            HomePageController controller = loader.getController();
            controller.setUtilizador(user);

            Stage stage = new Stage();
            stage.setTitle("Feira Viva - Página Inicial");
            stage.setScene(new Scene(root));
            stage.show();

            // Fecha o login
            Stage currentStage = (Stage) loginUsernameTextField.getScene().getWindow();
            currentStage.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
package com.example.feiraviva.Controller;

import com.example.feiraviva.Dao.SessaoDAO;
import com.example.feiraviva.Model.Utilizador;
import com.example.feiraviva.Dao.UtilizadorDAO;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;


public class SettingsController implements Initializable {

    @FXML
    private TextField updateNameTF;

    @FXML
    private TextField updatePasswordTF;

    @FXML
    private PasswordField updatePasswordPF;

    @FXML
    private TextField confirmPasswordTF;

    @FXML
    private PasswordField confirmPasswordPF;

    @FXML
    private Button updateNameButton;

    @FXML
    private Button updatePasswordButton;

    @FXML
    private CheckBox viewPasswordCB;

    @FXML private ImageView profileImageView;

    @FXML private Label greetingLbl;

    @FXML
    private PasswordField adminPasswordPF;

    private Utilizador utilizador;
    private final UtilizadorDAO utilizadorDAO = new UtilizadorDAO();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Atualiza UI se o utilizador já foi setado
        if (utilizador != null) {
            updateProfileImage();
            updateGreeting();
        }
    }

    public void setUtilizador(Utilizador utilizador) {
        this.utilizador = utilizador;
        if (utilizador != null) {
            updateNameTF.setText(utilizador.getNome());
        }
        if (profileImageView != null && greetingLbl != null) {
            updateProfileImage();
            updateGreeting();
        }
    }

    private void updateProfileImage() {
        if (utilizador != null && utilizador.getImagemPerfil() != null) {
            Image image = new Image(new ByteArrayInputStream(utilizador.getImagemPerfil()));
            profileImageView.setImage(image);
        } else {
            // Imagem padrão
            profileImageView.setImage(new Image(getClass().getResourceAsStream("/com/fxml/Images/user.png")));
        }

        // Aplica o clip circular só depois do layout estar feito!
        profileImageView.layoutBoundsProperty().addListener((obs, oldVal, newVal) -> {
            double radius = Math.min(profileImageView.getBoundsInLocal().getWidth(), profileImageView.getBoundsInLocal().getHeight()) / 2;
            if (radius > 0) {
                Circle clip = new Circle(
                        profileImageView.getBoundsInLocal().getWidth() / 2,
                        profileImageView.getBoundsInLocal().getHeight() / 2,
                        radius
                );
                profileImageView.setClip(clip);
            }
        });

        // Força o primeiro layout (caso já esteja pronto)
        Platform.runLater(() -> {
            double radius = Math.min(profileImageView.getBoundsInLocal().getWidth(), profileImageView.getBoundsInLocal().getHeight()) / 2;
            if (radius > 0) {
                Circle clip = new Circle(
                        profileImageView.getBoundsInLocal().getWidth() / 2,
                        profileImageView.getBoundsInLocal().getHeight() / 2,
                        radius
                );
                profileImageView.setClip(clip);
            }
        });
    }


    private void updateGreeting() {
        if (utilizador == null) {
            greetingLbl.setText("");
        } else {
            greetingLbl.setText(utilizador.getNome());
        }
    }

    @FXML
    private void onUpdateNameButtonClick() {
        String novoNome = updateNameTF.getText().trim();
        if (!novoNome.isEmpty()) {
            try {
                utilizadorDAO.atualizarNome(utilizador.getId(), novoNome);
                utilizador.setNome(novoNome);
                System.out.println("✅ Nome atualizado com sucesso.");
            } catch (Exception e) {
                System.out.println("❌ Erro ao atualizar nome na base de dados.");
                e.printStackTrace();
            }
        } else {
            System.out.println("⚠️ O campo de nome está vazio.");
        }
    }

    @FXML
    private void onUpdatePasswordButtonClick() {
        String pass1 = updatePasswordPF.getText();
        String pass2 = confirmPasswordPF.getText();

        if (pass1.isEmpty() || pass2.isEmpty()) {
            System.out.println("⚠️ As passwords não podem estar vazias.");
        } else if (!pass1.equals(pass2)) {
            System.out.println("❌ As passwords não coincidem.");
        } else {
            try {
                utilizadorDAO.atualizarPassword(utilizador.getId(), pass1);
                utilizador.setSenha(pass1);
                System.out.println("✅ Password atualizada com sucesso.");
            } catch (Exception e) {
                System.out.println("❌ Erro ao atualizar password na base de dados.");
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void onViewPasswordCBClick() {
        boolean mostrar = viewPasswordCB.isSelected();

        updatePasswordTF.setVisible(mostrar);
        confirmPasswordTF.setVisible(mostrar);
        updatePasswordPF.setVisible(!mostrar);
        confirmPasswordPF.setVisible(!mostrar);

        updatePasswordTF.setText(updatePasswordPF.getText());
        confirmPasswordTF.setText(confirmPasswordPF.getText());

        updatePasswordTF.textProperty().bindBidirectional(updatePasswordPF.textProperty());
        confirmPasswordTF.textProperty().bindBidirectional(confirmPasswordPF.textProperty());
    }

    @FXML
    private void onHomePageButtonClick() {
        navigateTo("/com/fxml/feiraviva/HomePage.fxml");
    }

    @FXML
    private void onDashboardButtonClick() {
        navigateTo("/com/fxml/feiraviva/DashboardAdministrator.fxml");
    }

    @FXML
    private void onSettingsButtonClick() {
        System.out.println("🛠️ Já estás nas definições.");
    }

    @FXML
    private void onLogoutButtonClick() {
        try {
            new SessaoDAO().registarLogout(utilizador.getId());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        Platform.exit();
    }

    @FXML
    private void onManageUsersButtonClick(ActionEvent event) {
        String password = adminPasswordPF.getText();
        if ("admin123".equals(password)) {
            navigateTo("/com/fxml/feiraviva/UserManagement.fxml");
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Password incorreta!", ButtonType.OK);
            alert.showAndWait();
        }
    }

    private void navigateTo(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Object controller = loader.getController();
            if (controller instanceof HomePageController) {
                ((HomePageController) controller).setUtilizador(utilizador);
            } else if (controller instanceof SettingsController) {
                ((SettingsController) controller).setUtilizador(utilizador);
            } else if (controller instanceof DashboardAdministratorController) {
                ((DashboardAdministratorController) controller).setUtilizador(utilizador);
            } else if (controller instanceof UserManagementController) {
                ((UserManagementController) controller).setUtilizador(utilizador);
            }

            Stage stage = (Stage) updateNameButton.getScene().getWindow();
            stage.setScene(new Scene(root, 1000, 700));
            stage.show();
        } catch (IOException e) {
            System.out.println("❌ Erro ao carregar " + fxmlPath);
            e.printStackTrace();
        }
    }

}
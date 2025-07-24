package com.example.feiraviva.Controller;

import com.example.feiraviva.Dao.SessaoDAO;
import com.example.feiraviva.Model.Utilizador;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;


public class DashboardP4GeralController implements Initializable {

    @FXML
    private Button homepageButton, dashboardButton, settingsButton, logoutButton;

    @FXML private ImageView profileImageView;

    @FXML private Label greetingLbl;

    private Utilizador utilizador;

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
    private void onHomePageButtonClick() {
        navigateTo("/com/fxml/feiraviva/HomePage.fxml");
    }

    @FXML
    private void onDashboardButtonClick() {
        navigateTo("/com/fxml/feiraviva/DashboardAdministrator.fxml");
    }

    @FXML
    private void onSettingsButtonClick() {
        navigateTo("/com/fxml/feiraviva/Settings.fxml");
    }

    @FXML
    private void onDesempenhoFornecedores1ButtonClick() {
        navigateTo("/com/fxml/feiraviva/DashboardP4.1.1.fxml");
    }

    @FXML
    private void onDesempenhoFornecedores2ButtonClick() {
        navigateTo("/com/fxml/feiraviva/DashboardP4.1.2.fxml");
    }

    @FXML
    private void onResultadoLiquidoButtonClick() {
        navigateTo("/com/fxml/feiraviva/DashboardP4.2.fxml");
    }

    @FXML
    private void onPagamentoFornecedoresButtonClick() {
        navigateTo("/com/fxml/feiraviva/DashboardP4.3.fxml");
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
    private void onBackLabelClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/fxml/feiraviva/DashboardAdministrator.fxml"));
            Parent root = loader.load();

            // Corrige: chama setUtilizador no controller certo!
            Object controller = loader.getController();
            if (controller instanceof DashboardAdministratorController) {
                ((DashboardAdministratorController) controller).setUtilizador(utilizador);
            }

            Stage stage = (Stage) homepageButton.getScene().getWindow();
            stage.setScene(new Scene(root, 1000, 700));
            stage.show();

        } catch (IOException e) {
            System.out.println("❌ Erro ao voltar ao dashboard");
            e.printStackTrace();
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
            }
            else if (controller instanceof DashboardP4Ponto1Ponto1Controller) {
                ((DashboardP4Ponto1Ponto1Controller) controller).setUtilizador(utilizador);
            } else if (controller instanceof DashboardP4Ponto1Ponto2Controller) {
                ((DashboardP4Ponto1Ponto2Controller) controller).setUtilizador(utilizador);
            } else if (controller instanceof DashboardP4Ponto2Controller) {
                ((DashboardP4Ponto2Controller) controller).setUtilizador(utilizador);
            } else if (controller instanceof DashboardP4Ponto3Controller) {
                ((DashboardP4Ponto3Controller) controller).setUtilizador(utilizador);
            }

            Stage stage = (Stage) homepageButton.getScene().getWindow();
            stage.setScene(new Scene(root, 1000, 700));
            stage.show();

        } catch (IOException e) {
            System.out.println("❌ Erro ao navegar para: " + fxmlPath);
            e.printStackTrace();
        }
    }
}
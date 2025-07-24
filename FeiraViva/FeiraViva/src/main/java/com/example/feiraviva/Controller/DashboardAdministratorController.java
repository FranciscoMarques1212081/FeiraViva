package com.example.feiraviva.Controller;

import com.example.feiraviva.Dao.SessaoDAO;
import com.example.feiraviva.Model.Utilizador;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import java.io.IOException;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;
import java.io.ByteArrayInputStream;
import java.net.URL;
import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.util.ResourceBundle;


public class DashboardAdministratorController implements Initializable {
    private Utilizador utilizador;

    @FXML
    private Button dashboardButton;

    @FXML private ImageView profileImageView;

    @FXML private Label greetingLbl;

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
    public void onLogoutButtonClick(MouseEvent event) {
        try {
            new SessaoDAO().registarLogout(utilizador.getId());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        Platform.exit();
    }

    @FXML
    public void onGestaoEstrategicaButtonClick(MouseEvent event) {
        navigateTo("/com/fxml/feiraviva/DashboardP1Geral.fxml");
    }

    @FXML
    public void onGestaoRecursosHumanosButtonClick(MouseEvent event) {
        navigateTo("/com/fxml/feiraviva/DashboardP2Geral.fxml");
    }

    @FXML
    public void onGestaoFinanceiraButtonClick(MouseEvent event) {
        navigateTo("/com/fxml/feiraviva/DashboardP4Geral.fxml");
    }

    @FXML
    public void onGestaoMarketingButtonClicked(MouseEvent event) {
        navigateTo("/com/fxml/feiraviva/DashboardP5Geral.fxml");
    }

    @FXML
    public void onGestaoAtividadesFisicasButtonClick(MouseEvent event) {
        navigateTo("/com/fxml/feiraviva/DashboardP6Geral.fxml");
    }

    @FXML
    public void onGestaoParqueZoologicoButtonClick(MouseEvent event) {
        navigateTo("/com/fxml/feiraviva/DashboardP7Geral.fxml");
    }

    @FXML
    public void onGestaoEventosButtonClick(MouseEvent event) {
        navigateTo("/com/fxml/feiraviva/DashboardP8Geral.fxml");
    }

    @FXML
    public void onGestaoEuroparqueButtonClick(MouseEvent event) {
        navigateTo("/com/fxml/feiraviva/DashboardP9Geral.fxml");
    }

    @FXML
    public void onGestaoContratacaoPublicaButtonClick(MouseEvent event) {
        navigateTo("/com/fxml/feiraviva/DashboardP10Geral.fxml");
    }

    @FXML
    public void onGestaoContratoProgramaButtonClick(MouseEvent event) {
        navigateTo("/com/fxml/feiraviva/DashboardP11Geral.fxml");
    }

    @FXML
    public void onPlanoMonitorizacaoButtonClick(MouseEvent event) {
        navigateTo("/com/fxml/feiraviva/PlanoMonitorizacao.fxml");
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
            else if (controller instanceof DashboardP1GeralController) {
                ((DashboardP1GeralController) controller).setUtilizador(utilizador);
            }
            else if (controller instanceof DashboardP2GeralController) {
                ((DashboardP2GeralController) controller).setUtilizador(utilizador);
            }
            else if (controller instanceof DashboardP4GeralController) {
                ((DashboardP4GeralController) controller).setUtilizador(utilizador);
            }
            else if (controller instanceof DashboardP5GeralController) {
                ((DashboardP5GeralController) controller).setUtilizador(utilizador);
            }
            else if (controller instanceof DashboardP6GeralController) {
                ((DashboardP6GeralController) controller).setUtilizador(utilizador);
            }
            else if (controller instanceof DashboardP7GeralController) {
                ((DashboardP7GeralController) controller).setUtilizador(utilizador);
            }
            else if (controller instanceof DashboardP8GeralController) {
                ((DashboardP8GeralController) controller).setUtilizador(utilizador);
            }
            else if (controller instanceof DashboardP9GeralController) {
                ((DashboardP9GeralController) controller).setUtilizador(utilizador);
            }
            else if (controller instanceof DashboardP10GeralController) {
                ((DashboardP10GeralController) controller).setUtilizador(utilizador);
            }
            else if (controller instanceof UserManagementController) {
                ((UserManagementController) controller).setUtilizador(utilizador);
            }
            else if (controller instanceof DashboardP11GeralController) {
                ((DashboardP11GeralController) controller).setUtilizador(utilizador);
            }
            else if (controller instanceof PlanoMonitorizacaoController) {
                ((PlanoMonitorizacaoController) controller).setUtilizador(utilizador);
            }

            //Usa botão FXML real
            Stage stage = (Stage) dashboardButton.getScene().getWindow();
            stage.setScene(new Scene(root, 1000, 700));
            stage.show();

        } catch (IOException e) {
            System.out.println("❌ Erro ao navegar para: " + fxmlPath);
            e.printStackTrace();
        }
    }

}
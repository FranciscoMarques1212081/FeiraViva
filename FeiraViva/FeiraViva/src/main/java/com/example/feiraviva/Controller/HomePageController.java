package com.example.feiraviva.Controller;

import com.example.feiraviva.Dao.ExcelFileDAO;
import com.example.feiraviva.Dao.SessaoDAO;
import com.example.feiraviva.Model.ExcelFileUpload;
import com.example.feiraviva.Model.Utilizador;
import com.example.feiraviva.Util.AppSession;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.ZoneId;
import com.example.feiraviva.Dao.UtilizadorDAO;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class HomePageController {

    @FXML
    private Label welcomeLbl;

    @FXML
    private Label infoLbl;

    @FXML
    private ImageView profileImageView;

    @FXML
    private TextArea notasTextArea;

    @FXML
    private AnchorPane centerPane;

    @FXML
    private ListView<String> sessoesListView;

    @FXML
    private ListView<ExcelFileUpload> excelUploadsListView;

    private Utilizador utilizador;
    private volatile boolean stopClock = false;

    public void setUtilizador(Utilizador utilizador) {
        if (utilizador == null) {
            this.utilizador = AppSession.get("utilizador", Utilizador.class);
        } else {
            this.utilizador = utilizador;
            AppSession.set("utilizador", utilizador); // mantém a sessão sincronizada!
        }

        if (this.utilizador == null) {
            // Não há utilizador, não continues! Mostra erro ou return.
            System.err.println("Utilizador não encontrado!");
            return;
        }

        changeGreetingWelcome();
        startClockThread();
        updateProfileImage();
        setupProfileImageClick();
        mostrarUltimasSessoes();
        mostrarUltimosExcelUploads();
        excelUploadsListView.setSelectionModel(null);
        sessoesListView.setSelectionModel(null);
        notasTextArea.setText(this.utilizador.getNotas() != null ? this.utilizador.getNotas() : "");
        notasTextArea.setPromptText("As minhas notas...");
        notasTextArea.setEditable(false);

        Platform.runLater(() -> welcomeLbl.requestFocus());

        notasTextArea.setOnMouseClicked(e -> {
            if (!notasTextArea.isEditable()) {
                notasTextArea.setEditable(true);
                notasTextArea.requestFocus();
                notasTextArea.positionCaret(notasTextArea.getText().length());
            }
        });

        notasTextArea.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) { // Saiu do foco
                terminarEdicaoNotas();
            }
        });

        centerPane.setOnMouseClicked(event -> {
            if (!notasTextArea.isFocused()) return;
            // Dá foco a outro node para forçar perder o foco
            welcomeLbl.requestFocus(); // ou outro node focável, até um label invisível serve!
        });
    }

    private void terminarEdicaoNotas() {
        String textoFinal = notasTextArea.getText().trim();
        notasTextArea.setText(textoFinal);
        guardarNotas();
        notasTextArea.setEditable(false);
        if (textoFinal.isEmpty()) {
            notasTextArea.setPromptText("As minhas notas...");
        }
    }

    private void guardarNotas() {
        String novasNotas = notasTextArea.getText();
        utilizador.setNotas(novasNotas);
        try {
            new UtilizadorDAO().atualizarNotas(utilizador.getId(), novasNotas);
            AppSession.set("utilizador", utilizador);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateProfileImage() {
        if (utilizador != null && utilizador.getImagemPerfil() != null) {
            Image image = new Image(new ByteArrayInputStream(utilizador.getImagemPerfil()));
            profileImageView.setImage(image);
        } else {
            // Imagem padrão do projeto
            profileImageView.setImage(new Image(getClass().getResourceAsStream("/com/fxml/Images/user.png")));
        }
        // Aplica clip circular
        Circle clip = new Circle(profileImageView.getFitWidth() / 2, profileImageView.getFitHeight() / 2, profileImageView.getFitWidth() / 2);
        profileImageView.setClip(clip);
    }

    private void setupProfileImageClick() {
        profileImageView.setOnMouseClicked(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Escolha a imagem de perfil");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Imagens", "*.png", "*.jpg", "*.jpeg")
            );
            File file = fileChooser.showOpenDialog(profileImageView.getScene().getWindow());
            if (file != null) {
                try (FileInputStream fis = new FileInputStream(file)) {
                    byte[] imageBytes = fis.readAllBytes();
                    UtilizadorDAO dao = new UtilizadorDAO();
                    dao.atualizarImagemPerfil(utilizador.getId(), imageBytes);
                    utilizador.setImagemPerfil(imageBytes);
                    updateProfileImage();
                } catch (IOException | SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void mostrarUltimasSessoes() {
        sessoesListView.getItems().clear();
        try {
            SessaoDAO sessaoDAO = new SessaoDAO();
            List<LocalDateTime> sessoes = sessaoDAO.listarUltimasSessoes(utilizador.getId(), 4);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            for (LocalDateTime dt : sessoes) {
                sessoesListView.getItems().add("Sessão terminada em: " + dt.format(formatter));
            }
            if (sessoes.isEmpty()) {
                sessoesListView.getItems().add("Sem sessões anteriores.");
            }
        } catch (SQLException e) {
            sessoesListView.getItems().add("Erro ao carregar sessões.");
            e.printStackTrace();
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
    public void onLogoutButtonClick(ActionEvent event) {
        try {
            new SessaoDAO().registarLogout(utilizador.getId());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        Platform.exit(); // ou reabrir Login.fxml
    }

    private void changeGreetingWelcome() {
        if (utilizador == null) return;

        String role = capitalize(utilizador.getPerfil());

        int horaAtual = ZonedDateTime.now().getHour();
        String saudacao;

        if (horaAtual >= 20 || horaAtual < 7) {
            saudacao = "Boa noite";
        } else if (horaAtual >= 12) {
            saudacao = "Boa tarde";
        } else {
            saudacao = "Bom dia";
        }

        welcomeLbl.setText(saudacao + "\n" + utilizador.getNome());
    }

    private void startClockThread() {
        Thread clock = new Thread(() -> {
            try {
                while (!stopClock) {
                    ZonedDateTime zdt = ZonedDateTime.now(ZoneId.systemDefault());

                    String hora = String.format("%02d:%02d",
                            zdt.getHour(), zdt.getMinute());

                    String data = String.format("%02d/%02d/%04d",
                            zdt.getDayOfMonth(), zdt.getMonthValue(), zdt.getYear());

                    Platform.runLater(() -> {
                        infoLbl.setText("" + hora + "\n" + data);
                    });

                    Thread.sleep(1000);
                }
            } catch (Exception e) {
                Platform.runLater(() ->
                        infoLbl.setText("Erro ao atualizar hora."));
            }
        });

        clock.setDaemon(true);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            stopClock = true;
            clock.interrupt();
        }));

        clock.start();
    }

    private void mostrarUltimosExcelUploads() {
        excelUploadsListView.getItems().clear();
        try {
            List<ExcelFileUpload> uploads = ExcelFileDAO.listarUltimosUploads();
            if (uploads.isEmpty()) {
                excelUploadsListView.getItems().add(new ExcelFileUpload("Nenhum ficheiro Excel encontrado.", null, "", null));
            } else {
                excelUploadsListView.getItems().addAll(uploads);
            }
        } catch (SQLException e) {
            excelUploadsListView.getItems().add(new ExcelFileUpload("Erro ao carregar uploads de Excel.", null, "", null));
            e.printStackTrace();
        }

        // Define o cell factory só uma vez (podes pôr isto aqui ou no initialize)
        excelUploadsListView.setCellFactory(listView -> new javafx.scene.control.ListCell<ExcelFileUpload>() {
            private final ImageView imageView = new ImageView();

            @Override
            protected void updateItem(ExcelFileUpload item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                    String data = item.getDataUpload() != null ? item.getDataUpload().format(formatter) : "";
                    // Mostra só o texto para linhas especiais
                    if (item.getDataUpload() == null && item.getUploadedBy().isEmpty()) {
                        setText(item.getNome());
                        setGraphic(null);
                        return;
                    }
                    setText(String.format("Nome: %s\nData: %s\nPor: %s", item.getNome(), data, item.getUploadedBy()));
                    if (item.getImagemPerfil() != null) {
                        imageView.setImage(new Image(new ByteArrayInputStream(item.getImagemPerfil())));
                    } else {
                        imageView.setImage(null);
                    }
                    imageView.setFitHeight(32);
                    imageView.setFitWidth(32);
                    imageView.setPreserveRatio(true);
                    setGraphic(imageView);
                }
            }
        });
    }


    private void navigateTo(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            // ✅ Passar utilizador se o controller for compatível
            Object controller = loader.getController();
            if (controller instanceof HomePageController) {
                ((HomePageController) controller).setUtilizador(utilizador);
            } else if (controller instanceof SettingsController) {
                ((SettingsController) controller).setUtilizador(utilizador);
            } else if (controller instanceof DashboardAdministratorController) {
                ((DashboardAdministratorController) controller).setUtilizador(utilizador);
            }

            Stage stage = (Stage) welcomeLbl.getScene().getWindow();
            stage.setScene(new Scene(root, 1000, 700));
            stage.show();
        } catch (IOException e) {
            System.out.println("❌ Erro ao navegar para: " + fxmlPath);
            e.printStackTrace();
        }
    }


    private String capitalize(String text) {
        if (text == null || text.isEmpty()) return text;
        return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
    }
}
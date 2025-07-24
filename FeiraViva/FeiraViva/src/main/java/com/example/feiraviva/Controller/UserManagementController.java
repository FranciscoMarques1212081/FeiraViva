package com.example.feiraviva.Controller;

import com.example.feiraviva.Dao.SessaoDAO;
import com.example.feiraviva.Model.Utilizador;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import java.io.*;
import java.nio.file.Files;
import java.sql.*;
import com.example.feiraviva.Util.DBConnection; // ajusta ao teu package
import javafx.stage.Stage;

public class UserManagementController {

    @FXML private TableView<User> usersTable;
    @FXML private TableColumn<User, String> colId, colNome, colEmail, colPerfil, colNotas;
    @FXML private TableColumn<User, ImageView> colImagem;
    @FXML private TextField nomeField, emailField, notasField;
    @FXML private PasswordField senhaField;
    @FXML private ComboBox<String> perfilBox;
    @FXML private ImageView previewImageView;

    @FXML
    private Button homepageButton, dashboardButton, settingsButton, logoutButton;
    private Utilizador utilizador;

    private byte[] imagemPerfilBytes = null;
    private final ObservableList<User> usersList = FXCollections.observableArrayList();

    public void setUtilizador(Utilizador utilizador) {
        this.utilizador = utilizador;
    }

    @FXML
    public void initialize() {
        perfilBox.setItems(FXCollections.observableArrayList("administrador", "utilizador"));
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colPerfil.setCellValueFactory(new PropertyValueFactory<>("perfil"));
        colNotas.setCellValueFactory(new PropertyValueFactory<>("notas"));
        colImagem.setCellValueFactory(new PropertyValueFactory<>("imagemView"));

        usersTable.setItems(usersList);
        loadUsers();

        usersTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) fillForm(newSel);
        });
    }

    private void loadUsers() {
        usersList.clear();
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM utilizadores")) {
            while (rs.next()) {
                int id = rs.getInt("id");
                String nome = rs.getString("nome");
                String email = rs.getString("email");
                String perfil = rs.getString("perfil");
                String notas = rs.getString("notas");
                byte[] img = rs.getBytes("imagem_perfil");

                ImageView imgView = null;
                if (img != null && img.length > 0) {
                    imgView = new ImageView(new Image(new ByteArrayInputStream(img)));
                    imgView.setFitHeight(40);
                    imgView.setFitWidth(40);
                    imgView.setPreserveRatio(true);
                }
                usersList.add(new User(id, nome, email, perfil, notas, imgView, img));
            }
        } catch (SQLException e) {
            showAlert("Erro", "Erro a carregar utilizadores: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void onAdicionarBtn(ActionEvent event) {
        String nome = nomeField.getText();
        String email = emailField.getText();
        String senha = senhaField.getText();
        String perfil = perfilBox.getValue();
        String notas = notasField.getText();

        if (nome.isBlank() || email.isBlank() || senha.isBlank() || perfil == null) {
            showAlert("Campos obrigatórios", "Preencha todos os campos obrigatórios!", Alert.AlertType.WARNING);
            return;
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO utilizadores (nome, email, senha, perfil, imagem_perfil, notas) VALUES (?, ?, ?, ?, ?, ?)")) {
            ps.setString(1, nome);
            ps.setString(2, email);
            ps.setString(3, senha); // Nota: em produção, guardar hash!
            ps.setString(4, perfil);
            ps.setBytes(5, imagemPerfilBytes);
            ps.setString(6, notas);
            ps.executeUpdate();
            showAlert("Sucesso", "Utilizador adicionado!", Alert.AlertType.INFORMATION);
            clearForm();
            loadUsers();
        } catch (SQLException e) {
            showAlert("Erro", "Erro ao adicionar: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void onEditarBtn(ActionEvent event) {
        User selected = usersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Selecione", "Selecione um utilizador para editar.", Alert.AlertType.WARNING);
            return;
        }
        String nome = nomeField.getText();
        String email = emailField.getText();
        String senha = senhaField.getText();
        String perfil = perfilBox.getValue();
        String notas = notasField.getText();

        if (nome.isBlank() || email.isBlank() || senha.isBlank() || perfil == null) {
            showAlert("Campos obrigatórios", "Preencha todos os campos obrigatórios!", Alert.AlertType.WARNING);
            return;
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE utilizadores SET nome=?, email=?, senha=?, perfil=?, imagem_perfil=?, notas=? WHERE id=?")) {
            ps.setString(1, nome);
            ps.setString(2, email);
            ps.setString(3, senha);
            ps.setString(4, perfil);
            ps.setBytes(5, imagemPerfilBytes);
            ps.setString(6, notas);
            ps.setInt(7, selected.getId());
            ps.executeUpdate();
            showAlert("Sucesso", "Utilizador atualizado!", Alert.AlertType.INFORMATION);
            clearForm();
            loadUsers();
        } catch (SQLException e) {
            showAlert("Erro", "Erro ao atualizar: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void onRemoverBtn(ActionEvent event) {
        User selected = usersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Selecione", "Selecione um utilizador para remover.", Alert.AlertType.WARNING);
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Remover utilizador?", ButtonType.YES, ButtonType.NO);
        if (alert.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            try (Connection conn = DBConnection.getConnection()) {
                // Primeiro, remove as sessões desse utilizador
                try (PreparedStatement ps1 = conn.prepareStatement(
                        "DELETE FROM sessoes_utilizador WHERE utilizador_id=?")) {
                    ps1.setInt(1, selected.getId());
                    ps1.executeUpdate();
                }
                // Depois, remove o próprio utilizador
                try (PreparedStatement ps2 = conn.prepareStatement(
                        "DELETE FROM utilizadores WHERE id=?")) {
                    ps2.setInt(1, selected.getId());
                    ps2.executeUpdate();
                }
                showAlert("Sucesso", "Utilizador removido!", Alert.AlertType.INFORMATION);
                clearForm();
                loadUsers();
            } catch (SQLException e) {
                showAlert("Erro", "Erro ao remover: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }


    @FXML
    private void onEscolherImagemBtn(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Escolher imagem");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Imagens", "*.png", "*.jpg", "*.jpeg", "*.gif"));
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            try {
                imagemPerfilBytes = Files.readAllBytes(file.toPath());
                Image image = new Image(new FileInputStream(file));
                previewImageView.setImage(image);
                previewImageView.setFitHeight(40);
                previewImageView.setFitWidth(40);
                previewImageView.setPreserveRatio(true);
            } catch (IOException e) {
                showAlert("Erro", "Erro ao ler imagem: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    private void fillForm(User user) {
        nomeField.setText(user.getNome());
        emailField.setText(user.getEmail());
        senhaField.setText(""); // Segurança: não mostrar senha real
        perfilBox.setValue(user.getPerfil());
        notasField.setText(user.getNotas());
        imagemPerfilBytes = user.getImagemBytes();
        if (user.getImagemBytes() != null) {
            previewImageView.setImage(new Image(new ByteArrayInputStream(user.getImagemBytes())));
        } else {
            previewImageView.setImage(null);
        }
    }

    private void clearForm() {
        nomeField.clear();
        emailField.clear();
        senhaField.clear();
        perfilBox.setValue(null);
        notasField.clear();
        previewImageView.setImage(null);
        imagemPerfilBytes = null;
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
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
    private void onLogoutButtonClick() {
        try {
            new SessaoDAO().registarLogout(utilizador.getId());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        Platform.exit();
    }

    // Classe interna User
    public static class User {
        private final int id;
        private final SimpleStringProperty nome;
        private final SimpleStringProperty email;
        private final SimpleStringProperty perfil;
        private final SimpleStringProperty notas;
        private final ImageView imagemView;
        private final byte[] imagemBytes;

        public User(int id, String nome, String email, String perfil, String notas, ImageView imagemView, byte[] imagemBytes) {
            this.id = id;
            this.nome = new SimpleStringProperty(nome);
            this.email = new SimpleStringProperty(email);
            this.perfil = new SimpleStringProperty(perfil);
            this.notas = new SimpleStringProperty(notas);
            this.imagemView = imagemView;
            this.imagemBytes = imagemBytes;
        }
        public int getId() { return id; }
        public String getNome() { return nome.get(); }
        public String getEmail() { return email.get(); }
        public String getPerfil() { return perfil.get(); }
        public String getNotas() { return notas.get(); }
        public ImageView getImagemView() { return imagemView; }
        public byte[] getImagemBytes() { return imagemBytes; }
    }

    @FXML
    private void onBackLabelClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/fxml/feiraviva/Settings.fxml"));
            Parent root = loader.load();

            // Corrige: chama setUtilizador no controller certo!
            Object controller = loader.getController();
            if (controller instanceof SettingsController) {
                ((SettingsController) controller).setUtilizador(utilizador);
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
            } else if (controller instanceof UserManagementController) {
                ((UserManagementController) controller).setUtilizador(utilizador);
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

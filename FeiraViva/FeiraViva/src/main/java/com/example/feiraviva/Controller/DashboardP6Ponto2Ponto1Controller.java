package com.example.feiraviva.Controller;

import com.example.feiraviva.Dao.ExcelFileDAO;
import com.example.feiraviva.Dao.SessaoDAO;
import com.example.feiraviva.Model.Utilizador;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Stage;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.awt.*;
import java.io.*;
import java.sql.SQLException;

public class DashboardP6Ponto2Ponto1Controller {

    @FXML
    private Button homepageButton, dashboardButton, settingsButton, logoutButton;

    private Utilizador utilizador;
    @FXML
    private TableView<ObservableList<String>> excelTable;
    @FXML
    private BarChart<String, Number> barChart;
    @FXML
    private void onUpdateChart() {
        showBarChartFromTable();
    }

    private XSSFWorkbook workbook;
    private Sheet sheet;
    private FormulaEvaluator evaluator;
    private long ultimaEdicaoTabela = 0; // Marca o último commit na tabela
    private final String pastaExcels = System.getProperty("user.home") + File.separator + "FeiraVivaData" + File.separator + "P6";
    private final String caminhoLocal = pastaExcels + File.separator + "P6.2.1 Indice de reclamações.xlsx";

    public void setUtilizador(Utilizador utilizador) {
        this.utilizador = utilizador;
    }

    @FXML
    private void onLoadExcel() {
        try {
            // Cria a pasta local se não existir
            File pasta = new File(pastaExcels);
            if (!pasta.exists()) pasta.mkdirs();

            workbook = ExcelFileDAO.getExcelFromDB("P6.2.1 Indice de reclamações.xlsx");
            if (workbook == null) {
                // Se não houver na BD, importa dos resources e salva na BD para futura utilização
                try (InputStream is = getClass().getResourceAsStream("/com/ExcelFiles/P6/P6.2.1 Indice de reclamações.xlsx")) {
                    if (is == null) {
                        System.out.println("❌ Ficheiro inicial não encontrado nos resources!");
                        return;
                    }
                    workbook = new XSSFWorkbook(is);
                    ExcelFileDAO.saveExcelToDB("P6.2.1 Indice de reclamações.xlsx", utilizador.getId(), workbook);
                    // Salva uma cópia local do excel
                    try (FileOutputStream fos = new FileOutputStream(caminhoLocal)) {
                        workbook.write(fos);
                    }
                }
            }
            sheet = workbook.getSheetAt(0);
            evaluator = workbook.getCreationHelper().createFormulaEvaluator();
            loadExcelFile();
            File file = new File(caminhoLocal);
            ultimaEdicaoTabela = file.exists() ? file.lastModified() : System.currentTimeMillis();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onSaveExcel() {
        try {
            File file = new File(caminhoLocal);

            if (file.exists() && file.lastModified() > ultimaEdicaoTabela) {
                // O ficheiro local (Excel externo) foi modificado mais recentemente
                try (FileInputStream fis = new FileInputStream(file)) {
                    XSSFWorkbook editedWb = new XSSFWorkbook(fis);
                    ExcelFileDAO.saveExcelToDB("P6.2.1 Indice de reclamações.xlsx", utilizador.getId(), editedWb);
                    workbook = editedWb;
                    sheet = workbook.getSheetAt(0);
                    evaluator = workbook.getCreationHelper().createFormulaEvaluator();
                    System.out.println("Excel guardado a partir do ficheiro local!");
                }
            } else {
                // A tabela foi modificada mais recentemente
                ExcelFileDAO.saveExcelToDB("P6.2.1 Indice de reclamações.xlsx", utilizador.getId(), workbook);
                try (FileOutputStream fos = new FileOutputStream(file)) {
                    workbook.write(fos);
                }
                System.out.println("Excel guardado a partir da tabela!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadExcelFile() {
        try {
            excelTable.getColumns().clear();
            excelTable.getItems().clear();

            if (sheet == null) {
                System.out.println("A folha do Excel não está carregada!");
                return;
            }

            // Encontrar a primeira linha não vazia para ser o cabeçalho
            Row headerRow = null;
            int headerRowIndex = -1;
            for (int i = 0; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row != null && row.getPhysicalNumberOfCells() > 0) {
                    headerRow = row;
                    headerRowIndex = i;
                    break;
                }
            }

            if (headerRow == null) {
                System.out.println("A linha de cabeçalho não foi encontrada no ficheiro Excel selecionado.");
                return;
            }

            int colCount = headerRow.getLastCellNum();
            if (colCount <= 0) {
                System.out.println("O cabeçalho do ficheiro Excel está vazio.");
                return;
            }

            // Torna headerRowIndex final para usar nos lambdas
            final int headerRowIndexFinal = headerRowIndex;

            // Criar as colunas da tabela
            for (int i = 0; i < colCount; i++) {
                final int colIndex = i;
                Cell headerCell = headerRow.getCell(i, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                String colName = (headerCell != null) ? headerCell.toString() : "Coluna " + (i + 1);

                TableColumn<ObservableList<String>, String> col = new TableColumn<>(colName);
                col.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(colIndex)));
                col.setCellFactory(TextFieldTableCell.forTableColumn());
                col.setOnEditCommit(event -> {
                    event.getRowValue().set(colIndex, event.getNewValue());
                    Row excelRow = sheet.getRow(excelTable.getItems().indexOf(event.getRowValue()) + headerRowIndexFinal + 1);
                    if (excelRow == null) {
                        excelRow = sheet.createRow(excelTable.getItems().indexOf(event.getRowValue()) + headerRowIndexFinal + 1);
                    }
                    Cell cell = excelRow.getCell(colIndex, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                    String value = event.getNewValue();
                    if (value.startsWith("=")) {
                        cell.setCellFormula(value.substring(1));
                    } else {
                        try {
                            cell.setCellValue(Double.parseDouble(value));
                        } catch (NumberFormatException ex) {
                            cell.setCellValue(value);
                        }
                    }
                    evaluator.notifySetFormula(cell);
                    ultimaEdicaoTabela = System.currentTimeMillis();
                });
                excelTable.getColumns().add(col);
            }

            // Ler os dados a partir da linha imediatamente após o cabeçalho
            for (int r = headerRowIndex + 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) {
                    continue;
                }
                ObservableList<String> rowData = FXCollections.observableArrayList();
                for (int c = 0; c < colCount; c++) {
                    Cell cell = row.getCell(c, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                    String value = "";
                    switch (cell.getCellType()) {
                        case STRING:
                            value = cell.getStringCellValue();
                            break;
                        case NUMERIC:
                            value = String.valueOf(cell.getNumericCellValue());
                            break;
                        case FORMULA:
                            value = "=" + cell.getCellFormula();
                            break;
                        case BOOLEAN:
                            value = String.valueOf(cell.getBooleanCellValue());
                            break;
                        case BLANK:
                            value = "";
                            break;
                        default:
                            value = cell.toString();
                            break;
                    }
                    rowData.add(value);
                }
                excelTable.getItems().add(rowData);
            }
            excelTable.setEditable(true);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void showBarChartFromTable() {
        barChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        // Começa da segunda linha (índice 1), pois índice 0 é cabeçalho
        for (ObservableList<String> row : excelTable.getItems()) {
            String category = row.get(0);
            String valueStr = row.get(1);
            try {
                double value;
                if (valueStr.startsWith("=")) {
                    int rowIndex = excelTable.getItems().indexOf(row) + 1; // +1 para coincidir com excel
                    Cell cell = sheet.getRow(rowIndex).getCell(1, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                    CellValue evaluated = evaluator.evaluate(cell);
                    value = evaluated.getNumberValue();
                } else {
                    value = Double.parseDouble(valueStr);
                }
                series.getData().add(new XYChart.Data<>(category, value));
            } catch (Exception e) {
                // Ignora erros de parsing
            }
        }
        barChart.getData().add(series);
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

    @FXML
    private void onExcelButtonClick() {
        try {
            File localExcel = new File(caminhoLocal);

            // Garante que o ficheiro está atualizado a partir da BD antes de abrir
            XSSFWorkbook wb = ExcelFileDAO.getExcelFromDB("P6.2.1 Indice de reclamações.xlsx");
            if (wb != null) {
                try (FileOutputStream fos = new FileOutputStream(localExcel)) {
                    wb.write(fos);
                }
            } else {
                System.out.println("❌ Ficheiro não encontrado na base de dados.");
                return;
            }

            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(localExcel);
            } else {
                System.out.println("❌ Desktop não suportado no ambiente atual.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onBackLabelClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/fxml/feiraviva/DashboardP6Geral.fxml"));
            Parent root = loader.load();

            Object controller = loader.getController();
            if (controller instanceof DashboardP6GeralController) {
                ((DashboardP6GeralController) controller).setUtilizador(utilizador);
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

            Stage stage = (Stage) homepageButton.getScene().getWindow();
            stage.setScene(new Scene(root, 1000, 700));
            stage.show();

        } catch (IOException e) {
            System.out.println("❌ Erro ao navegar para: " + fxmlPath);
            e.printStackTrace();
        }
    }
}
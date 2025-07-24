module com.example.feiraviva {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.desktop;

    // POI (Apache Excel)
    requires org.apache.poi.poi;
    requires org.apache.poi.ooxml;

    opens com.example.feiraviva to javafx.fxml;
    opens com.example.feiraviva.Controller to javafx.fxml;
    opens com.example.feiraviva.Model to javafx.base;

    exports com.example.feiraviva;
    exports com.example.feiraviva.Controller;
}
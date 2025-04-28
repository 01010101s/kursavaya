module org.example.rockpaperscissors {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    opens org.example.rockpaperscissors to javafx.fxml;
    exports org.example.rockpaperscissors;
}

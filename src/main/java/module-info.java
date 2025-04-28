module org.example.rockpaperscissors {
    requires javafx.controls;
    requires javafx.fxml;
    opens org.example.rockpaperscissors to javafx.fxml;
    exports org.example.rockpaperscissors;
}

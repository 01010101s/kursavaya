package org.example.rockpaperscissors;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;

import java.io.*;
import java.net.*;

public class MainController {

    @FXML
    private ComboBox<String> choiceBox;

    @FXML
    private Label resultLabel;

    @FXML
    private Button hostBtn;

    @FXML
    private Button connectBtn;

    @FXML
    private ImageView rockImageView, paperImageView, scissorsImageView;

    private String connectionType;
    private static Socket socket;
    private static ServerSocket serverSocket;
    private Thread gameThread;

    private boolean turnSubmitted = false; // флаг, блокирующий повторный ход

    @FXML
    private void initialize() {
        if (choiceBox.getItems().isEmpty()) {
            choiceBox.getItems().addAll("Камень", "Бумага", "Ножницы");
        }

        if (choiceBox == null) {
            System.out.println("choiceBox не был инициализирован.");
        }
    }

    @FXML
    private void handlePlay() {
        if (turnSubmitted) {
            showAlert("Информация", "Вы уже выбрали свой ход. Дождитесь окончания раунда.");
            return;
        }

        String turn = choiceBox.getValue();

        if (turn == null || turn.isEmpty()) {
            showAlert("Ошибка", "Выберите ваш ход.");
            return;
        }

        String turnEng = convertToEnglish(turn);

        // Отображение изображения сразу
        rockImageView.setVisible(false);
        paperImageView.setVisible(false);
        scissorsImageView.setVisible(false);
        switch (turnEng) {
            case "rock" -> rockImageView.setVisible(true);
            case "paper" -> paperImageView.setVisible(true);
            case "scissors" -> scissorsImageView.setVisible(true);
        }

        // Обновление текста "Ваш ход" сразу после нажатия
        resultLabel.setText("Ваш ход: " + turn + "\nОжидаем ход противника...");

        if (connectionType == null) {
            showAlert("Ошибка", "Сначала создайте или подключитесь к серверу.");
            return;
        }

        turnSubmitted = true;

        try {
            sendTurn(turnEng);
            gameThread = new Thread(() -> {
                String enemyTurn = receiveTurn();
                Platform.runLater(() -> {
                    String result = checkResult(turnEng, enemyTurn);
                    resultLabel.setText("Ваш ход: " + turn + "\nХод противника: " + convertToRussian(enemyTurn) + "\nРезультат: " + result);
                    showRoundEndDialog(result);
                    turnSubmitted = false;
                });
            });
            gameThread.start();
        } catch (Exception e) {
            showAlert("Ошибка", "Ошибка соединения");
            turnSubmitted = false;
        }
    }


    @FXML
    public void startHosting() {
        connectionType = "server";

        TextInputDialog ipDialog = new TextInputDialog("localhost");
        ipDialog.setTitle("Создание сервера");
        ipDialog.setHeaderText("Введите IP адрес");
        ipDialog.setContentText("IP:");

        ipDialog.showAndWait().ifPresent(ip -> {
            TextInputDialog portDialog = new TextInputDialog("5555");
            portDialog.setTitle("Создание сервера");
            portDialog.setHeaderText("Введите порт");
            portDialog.setContentText("Порт:");

            portDialog.showAndWait().ifPresent(portStr -> {
                try {
                    int port = Integer.parseInt(portStr);
                    new Thread(() -> {
                        try {
                            serverSocket = new ServerSocket(port);
                            socket = serverSocket.accept();
                        } catch (IOException e) {
                            Platform.runLater(() -> {
                                showAlert("Ошибка", "Не удалось создать сервер: " + e.getMessage());
                                hostBtn.setDisable(false);
                                connectBtn.setDisable(false);
                            });
                        }
                    }).start();
                } catch (NumberFormatException e) {
                    showAlert("Ошибка", "Неверный порт");
                }
            });
        });
    }

    @FXML
    public void connectToServer() {
        connectionType = "client";

        TextInputDialog ipDialog = new TextInputDialog("localhost");
        ipDialog.setTitle("Подключение");
        ipDialog.setHeaderText("Введите IP сервера");
        ipDialog.setContentText("IP:");

        ipDialog.showAndWait().ifPresent(ip -> {
            TextInputDialog portDialog = new TextInputDialog("5555");
            portDialog.setTitle("Подключение");
            portDialog.setHeaderText("Введите порт сервера");
            portDialog.setContentText("Порт:");

            portDialog.showAndWait().ifPresent(portStr -> {
                try {
                    int port = Integer.parseInt(portStr);
                    new Thread(() -> {
                        try {
                            socket = new Socket(ip, port);
                        } catch (IOException e) {
                            Platform.runLater(() -> {
                                showAlert("Ошибка", "Не удалось подключиться: " + e.getMessage());
                                hostBtn.setDisable(false);
                                connectBtn.setDisable(false);
                            });
                        }
                    }).start();
                } catch (NumberFormatException e) {
                    showAlert("Ошибка", "Неверный порт");
                }
            });
        });
    }

    private void sendTurn(String turn) throws IOException {
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        out.println(turn);
    }

    private String receiveTurn() {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            return in.readLine();
        } catch (IOException e) {
            Platform.runLater(() -> showAlert("Ошибка", "Не удалось получить ход: " + e.getMessage()));
            return "";
        }
    }

    private String checkResult(String myTurn, String enemyTurn) {
        if (myTurn.equals(enemyTurn)) return "Ничья";
        return switch (myTurn) {
            case "rock" -> enemyTurn.equals("scissors") ? "Победа" : "Поражение";
            case "paper" -> enemyTurn.equals("rock") ? "Победа" : "Поражение";
            case "scissors" -> enemyTurn.equals("paper") ? "Победа" : "Поражение";
            default -> "Ошибка";
        };
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private String convertToEnglish(String russian) {
        return switch (russian) {
            case "Камень" -> "rock";
            case "Ножницы" -> "scissors";
            case "Бумага" -> "paper";
            default -> "";
        };
    }

    private String convertToRussian(String english) {
        return switch (english) {
            case "rock" -> "Камень";
            case "scissors" -> "Ножницы";
            case "paper" -> "Бумага";
            default -> "";
        };
    }

    private void showRoundEndDialog(String result) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Конец раунда");
        alert.setHeaderText("Результат раунда: " + result);
        alert.setContentText("Нажмите 'Ок' для продолжения");
        alert.showAndWait();
    }

    public static void closeGame() {
        try {
            if (socket != null) socket.close();
            if (serverSocket != null) serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Platform.exit();
    }
}

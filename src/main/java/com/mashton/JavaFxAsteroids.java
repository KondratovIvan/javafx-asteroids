package com.mashton;

import com.mashton.Sprite;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Optional;

public class JavaFxAsteroids extends Application {
    public static final String WINDOW_TITLE = "A S T E R O I D S";
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final int ASTEROID_COUNT = 6;
    private int lives = 3;
    private int score;

    private Stage primaryStage;
    private Scene startScene;
    private Scene gameScene;

    private ArrayList<Sprite> asteroidList;
    private ArrayList<Sprite> heartList;
    private Sprite spaceShip;
    private AnimationTimer gameLoop;

    ArrayList<Sprite> laserList = new ArrayList<>();

    private double asteroidSpeed = 50;
    private int scoreThreshold = 1000;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle(WINDOW_TITLE);
        this.primaryStage.setResizable(false);

        createStartScene();

        primaryStage.setScene(startScene);
        primaryStage.show();
    }

    public void createStartScene() {
        Pane startPane = new Pane();
        startPane.setPrefSize(WIDTH, HEIGHT);

        Image backgroundImage = new Image("background.jpg");
        ImageView backgroundImageView = new ImageView(backgroundImage);
        backgroundImageView.setFitWidth(WIDTH);
        backgroundImageView.setFitHeight(HEIGHT);

        Button startButton = new Button("Start");
        startButton.setFont(Font.font("Arial", 24));
        startButton.setTextFill(Color.WHITE);
        startButton.setStyle("-fx-background-color: #336699;");
        startButton.setOnAction(event -> {
            startGame();
        });

        VBox vbox = new VBox(20, startButton);
        vbox.setAlignment(Pos.CENTER);
        vbox.setLayoutX((WIDTH - vbox.getWidth()) / 2);
        vbox.setLayoutY((HEIGHT - vbox.getHeight()) / 2);

        startPane.getChildren().addAll(backgroundImageView, vbox);

        startScene = new Scene(startPane, WIDTH, HEIGHT);
    }

    private void startGame() {
        Pane gamePane = new Pane();
        gamePane.setPrefSize(WIDTH, HEIGHT);
        gamePane.setStyle("-fx-background-color: #000000;");

        Canvas canvas = new Canvas(WIDTH, HEIGHT);
        GraphicsContext context = canvas.getGraphicsContext2D();
        gamePane.getChildren().add(canvas);

        context.setFill(Color.BLUE);
        context.fillRect(0, 0, WIDTH, HEIGHT);

        ArrayList<String> keyPressedList = new ArrayList<>();
        ArrayList<String> keyJustPressedList = new ArrayList<>();

        gameScene = new Scene(gamePane, WIDTH, HEIGHT);
        gameScene.setOnKeyPressed(
                (KeyEvent event) -> {
                    String keyName = event.getCode().toString();
                    if (!keyPressedList.contains(keyName)) {
                        keyPressedList.add(keyName);
                        keyJustPressedList.add(keyName);
                    }
                }
        );
        gameScene.setOnKeyReleased(
                (KeyEvent event) -> {
                    String keyName = event.getCode().toString();
                    if (keyPressedList.contains(keyName)) {
                        keyPressedList.remove(keyName);
                    }
                }
        );

        Sprite background = new Sprite("Space003-opengameart-800x600.png");
        background.position.set(400, 300);
        spaceShip = new Sprite("DurrrSpaceShip-opengameart-50x50.png");
        spaceShip.position.set(100, 300);
        asteroidList = new ArrayList<>();
        heartList = new ArrayList<>();
        spawnAsteroids();

        score = 0;
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long nanoTime) {
                // обработка пользовательского ввода
                if (keyPressedList.contains("LEFT")) {
                    spaceShip.rotationInDegrees -= 3;
                }

                if (keyPressedList.contains("RIGHT")) {
                    spaceShip.rotationInDegrees += 3;
                }

                if (keyPressedList.contains("UP")) {
                    spaceShip.velocity.setLength(150);
                    spaceShip.velocity.setAngleInDegrees(spaceShip.rotationInDegrees);
                } else {
                    spaceShip.velocity.setLength(0);
                }

                if (keyJustPressedList.contains("SPACE")) {
                    Sprite laser = new Sprite("orb_red-opengameart-10x10.png");
                    laser.position.set(spaceShip.position.x, spaceShip.position.y);
                    laser.velocity.setLength(400);
                    laser.velocity.setAngleInDegrees(spaceShip.rotationInDegrees);
                    laserList.add(laser);
                }

                keyJustPressedList.clear();

                spaceShip.update(1 / 60.0);
                for (Sprite asteroid : asteroidList) {
                    asteroid.update(1 / 60.0);
                }
                for (Sprite heart : heartList) {
                    heart.update(1 / 60.0);
                }

                checkCollisions(gameLoop);

                if (asteroidList.size() < ASTEROID_COUNT) {
                    spawnAsteroid();
                }
                if (heartList.size() < 1) {
                    spawnHeart();
                }

                for (int n = 0; n < laserList.size(); n++) {
                    Sprite laser = laserList.get(n);
                    laser.update(1 / 60.0);
                    if (laser.elapseTimeSeconds > 2) {
                        laserList.remove(n);
                    }
                }

                for (int laserNum = 0; laserNum < laserList.size(); laserNum++) {
                    Sprite laser = laserList.get(laserNum);
                    for (int asteroidNum = 0; asteroidNum < asteroidList.size(); asteroidNum++) {
                        Sprite asteroid = asteroidList.get(asteroidNum);
                        if (laser.overlaps(asteroid)) {
                            laserList.remove(laserNum);
                            asteroidList.remove(asteroidNum);
                            score += 100;
                            if (score % scoreThreshold == 0) {
                                asteroidSpeed *= 1.5;
                            }
                        }
                    }
                }

                for (int heartNum = 0; heartNum < heartList.size(); heartNum++) {
                    Sprite heart = heartList.get(heartNum);
                    if (spaceShip.overlaps(heart)) {
                        heartList.remove(heartNum);
                        lives++;
                    }
                }

                context.clearRect(0, 0, WIDTH, HEIGHT);

                background.render(context);
                spaceShip.render(context);
                for (Sprite laser : laserList) {
                    laser.render(context);
                }
                for (Sprite asteroid : asteroidList) {
                    asteroid.render(context);
                }
                for (Sprite heart : heartList) {
                    heart.render(context);
                }

                context.setFill(Color.WHITE);
                context.setStroke(Color.GREEN);
                context.setFont(new Font("Arial Black", 48));
                context.setLineWidth(3);
                String text = "Score: " + score;
                int textX = WIDTH - 300;
                int textY = 50;
                context.fillText(text, textX, textY);
                context.strokeText(text, textX, textY);
// Отображение жизней корабля
                context.setFill(Color.RED);
                context.setFont(new Font("Arial Black", 24));
                String livesText = "Lives: " + lives;
                int livesTextX = 50;
                int livesTextY = 50;
                context.fillText(livesText, livesTextX, livesTextY);
                context.strokeText(livesText, livesTextX, livesTextY);
                // Проверка количества жизней корабля
                if (lives <= 0) {
                    gameLoop.stop();
                    showGameOverAlert();
                }
            }
        };

        gameLoop.start();
        primaryStage.setScene(gameScene);
    }

    private void spawnAsteroid() {
        Sprite asteroid = new Sprite("asteroid-opengameeart-100x100.png");
        double x = WIDTH * Math.random();
        double y = -100;
        asteroid.position.set(x, y);
        double angle = 360 * Math.random();
        asteroid.velocity.setLength(asteroidSpeed);
        asteroid.velocity.setAngleInDegrees(angle);
        asteroidList.add(asteroid);
    }

    private void spawnAsteroids() {
        for (int i = 0; i < ASTEROID_COUNT; i++) {
            spawnAsteroid();
        }
    }

    private void spawnHeart() {
        Sprite heart = new Sprite("heart.png");
        double x = WIDTH * Math.random();
        double y = -100;
        heart.position.set(x, y);
        double angle = 360 * Math.random();
        heart.velocity.setLength(50);
        heart.velocity.setAngleInDegrees(angle);

        heartList.add(heart);
    }

    private void checkCollisions(AnimationTimer gameLoop) {
        for (Sprite asteroid : asteroidList) {
            if (spaceShip.overlaps(asteroid)) {
                lives--; // Уменьшаем количество жизней на 1
                spaceShip.position.set(100, 300); // Возвращаем корабль в начальную позицию
                asteroidList.remove(asteroid); // Удаляем астероид
            }
        }
    }

    private void showGameOverAlert() {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Game Over");
            alert.setHeaderText(null);

            if (lives > 0) {
                alert.setContentText("You lost! Remaining lives: " + lives);
            } else {
                alert.setContentText("Game Over. Your score: " + score);
            }

            ImageView imageView = new ImageView(new Image("space_ship.png"));
            imageView.setFitWidth(100);
            imageView.setFitHeight(100);
            alert.setGraphic(imageView);

            ButtonType restartButtonType = new ButtonType("Restart");
            ButtonType exitButtonType = new ButtonType("Exit");
            alert.getButtonTypes().setAll(restartButtonType, exitButtonType);
            Optional<ButtonType> result = alert.showAndWait();

            if (result.isPresent()) {
                if (result.get() == restartButtonType) {
                    restartGame();
                } else if (result.get() == exitButtonType) {
                    primaryStage.close();
                }
            }
        });
    }


    private void restartGame() {
        lives = 3;
        score = 0;
        asteroidSpeed = 50;

        asteroidList.clear();
        heartList.clear();
        laserList.clear();

        spaceShip.position.set(100, 300);

        spawnAsteroids();

        gameLoop.start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
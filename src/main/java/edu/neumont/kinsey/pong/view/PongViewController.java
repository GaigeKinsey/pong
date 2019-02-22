package edu.neumont.kinsey.pong.view;

import java.util.Random;

import edu.neumont.kinsey.controller.PongController;
import edu.neumont.kinsey.view.PongView;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;

public class PongViewController implements PongView {

	private Stage stage;

	private PongController controller;

	@FXML
	private Canvas pongCanvas;

	private GraphicsContext ctx;

	private int canvasWidth = 1280;
	private int canvasHeight = 720;
	
	private int canvasCenterX = canvasWidth / 2;
	private int canvasCenterY = canvasHeight / 2;
	
	private int centerLineWidth = 2;

	private int paddleHeight = 80;
	private int paddleWidth = 20;

	private int paddle1PosX;
	private int paddle2PosX;
	private int paddle1PosY;
	private int paddle2PosY;
	private int paddleVel = 6;

	private Color objectColor = Color.WHITE;

	private int ballSize = 15;
	private int ballPosX;
	private int ballPosY;
	private int ballXVel = -7;
	private int ballYVel = 0;
	private int bufferCount = 0;
	private boolean ballMoving = false;
	private boolean ballHittable = true;

	private int player1Score;
	private int player2Score;

	private int numberFontSize = 50;
	private int numberPadding = 10;
	private int letterFontSize = 40;

	private boolean running = false, paused = false;

	private boolean leftPressed = false, rightPressed = false;
	
	private boolean win = false;

	private Timeline pongTimeline;

	public Stage getStage() {
		return this.stage;
	}

	public void setStage(Stage stage) {
		this.stage = stage;
	}

	public void registerController(PongController controller) {
		this.controller = controller;
	}

	public void init() {
		paddle1PosX = 50;
		paddle2PosX = canvasWidth - 50 - paddleWidth;
		paddle1PosY = canvasCenterY - paddleHeight / 2;
		paddle2PosY = canvasCenterY - paddleHeight / 2;
		ballPosX = canvasCenterX - ballSize / 2;
		ballPosY = canvasCenterY - ballSize / 2;
		ctx = pongCanvas.getGraphicsContext2D();
		pongCanvas.requestFocus();
		ctx.setFont(Font.font("Press Start 2P", letterFontSize));
		drawStartScreen();
		this.stage.setTitle("Pong");
		this.stage.setResizable(false);
		this.stage.show();
	}

	private void drawStartScreen() {
		ctx.setFill(objectColor);
		drawPaddles();
		drawScore();
		drawMedian();
		ctx.fillText("CLICK To Start", canvasCenterX - 300, canvasCenterY);
	}

	public void onKeyPressed(KeyEvent e) {
		if (running) {
			// Toggle pause when escape is pressed while game running
			if (e.getCode().equals(KeyCode.ESCAPE) && !win) {
				if (paused) {
					paused = false;

					// Resume physics and remove paused text
					pongTimeline.play();

				} else {
					paused = true;

					// Pause physics and display paused text
					pongTimeline.pause();
					ctx.fillText("Paused", canvasCenterX - 125, canvasCenterY);

				}
			}
		}
	}

	public void onMousePressed(MouseEvent e) {
		// Used to start game, no going back after starting
		if (!running) {
			running = true;

			// Start the time line to run physics and drawing
			pongTimeline = new Timeline(new KeyFrame(Duration.millis(16.6), d -> draw()));
			pongTimeline.setCycleCount(Timeline.INDEFINITE);
			pongTimeline.play();
		}

		if (running && !paused) {

			if (e.getButton().equals(MouseButton.PRIMARY)) {
				leftPressed = true;
			}

			if (e.getButton().equals(MouseButton.SECONDARY)) {
				rightPressed = true;
			}
		}
	}

	public void onMouseReleased(MouseEvent e) {
		if (running && !paused) {

			if (e.getButton().equals(MouseButton.PRIMARY)) {
				leftPressed = false;
			}

			if (e.getButton().equals(MouseButton.SECONDARY)) {
				rightPressed = false;
			}
		}
	}

	private void draw() {
		ctx.setFill(Color.BLACK);
		ctx.fillRect(0, 0, canvasWidth, canvasHeight);
		ctx.setFill(objectColor);
		paddleBouncePhysics();
		paddleMovementPhysics();
		ballMovementPhysics();
		drawMedian();
		drawScore();
		drawPaddles();
		drawBall();
	}

	private void paddleBouncePhysics() {
		// Only check to see if the ball can be hit after passing the middle
		if (ballHittable) {
			// Left paddle hit
			if (ballPosX <= paddle1PosX + paddleWidth && ballPosX >= paddle1PosX && ballPosY + ballSize >= paddle1PosY
					&& ballPosY <= paddle1PosY + paddleHeight) {
				ballHittable = false;
				ballXVel *= -1;

				// Change ballYVelocity based on distance to center of paddle
				int centerOffset = (ballPosY - ballSize / 2) - (paddle1PosY + paddleHeight / 2);

				ballYVel = (int) (centerOffset * .3);
			}

			// Right paddle hit
			if (ballPosX + ballSize >= paddle2PosX && ballPosX + ballSize <= paddle2PosX + paddleWidth
					&& ballPosY + ballSize >= paddle2PosY && ballPosY <= paddle2PosY + paddleHeight) {
				ballHittable = false;
				ballXVel *= -1;

				// Change ballYVelocity based on distance to center of paddle
				int centerOffset = (ballPosY - ballSize / 2) - (paddle2PosY + paddleHeight / 2);

				ballYVel = (int) (centerOffset * .3);
			}
		}
	}

	private void ballMovementPhysics() {
		if (!ballMoving && bufferCount <= 60) {
			bufferCount++;
		} else if (!ballMoving && bufferCount > 60) {
			ballMoving = true;
			Random rng = new Random();
			int vel = rng.nextInt(7);
			if (vel >= 2) {
				ballYVel = vel;
			} else {
				ballYVel = vel * -1;
			}
		}

		if (player1Score == 11 || player2Score == 11) {
			pongTimeline.stop();
			win();
		}

		if (ballMoving) {

			if (ballPosY + ballSize >= canvasHeight || ballPosY <= 0) {
				ballYVel *= -1;
			}

			if (ballPosX >= canvasCenterX - ballSize && ballPosX <= canvasCenterX + ballSize) {
				ballHittable = true;
			}

			ballPosX += ballXVel;
			ballPosY += ballYVel;

			// If it goes off the Left Side
			if (ballHittable && ballPosX + ballSize <= 0) {
				player2Score++;
				score();
			}

			// If it goes off the Right Side
			if (ballHittable && ballPosX > canvasWidth) {
				player1Score++;
				score();
			}
		}
	}

	private void score() {
		ballMoving = false;
		bufferCount = 0;
		paddle1PosX = 50;
		paddle2PosX = canvasWidth - 50 - paddleWidth;
		paddle1PosY = canvasCenterY - paddleHeight / 2;
		paddle2PosY = canvasCenterY - paddleHeight / 2;
		ballPosX = canvasCenterX - ballSize / 2;
		ballPosY = canvasCenterY - ballSize / 2;
	}

	private void win() {
		win = true;
		if (player1Score > player2Score) {
			ctx.fillText("WINNER", canvasCenterX - 400, canvasCenterY);
		} else {
			ctx.fillText("WINNER", canvasCenterX + 100, canvasCenterY);
		}
	}

	private void paddleMovementPhysics() {
		if (leftPressed) {
			if (paddle1PosY >= 0) {
				paddle1PosY -= paddleVel;
			}
		} else {
			if (paddle1PosY + paddleHeight <= canvasHeight) {
				paddle1PosY += paddleVel;
			}
		}

		if (rightPressed) {
			if (paddle2PosY >= 0) {
				paddle2PosY -= paddleVel;
			}
		} else {
			if (paddle2PosY + paddleHeight <= canvasHeight) {
				paddle2PosY += paddleVel;
			}
		}
	}

	private void drawBall() {
		ctx.fillRect(ballPosX, ballPosY, ballSize, ballSize);
	}

	private void drawPaddles() {
		ctx.fillRect(paddle1PosX, paddle1PosY, paddleWidth, paddleHeight);
		ctx.fillRect(paddle2PosX, paddle2PosY, paddleWidth, paddleHeight);
	}

	private void drawScore() {
		if (player1Score > 9) {
			ctx.fillText("" + player1Score, canvasCenterX - numberFontSize * 2 - numberPadding,
					numberFontSize + numberPadding);
		} else {
			ctx.fillText("" + player1Score, canvasCenterX - numberFontSize - numberPadding,
					numberFontSize + numberPadding);
		}
		ctx.fillText("" + player2Score, canvasCenterX + numberPadding, numberFontSize + numberPadding);
	}

	private void drawMedian() {
		ctx.fillRect(canvasCenterX - centerLineWidth, 0, centerLineWidth, canvasHeight);
	}

	public void onRestartAction(ActionEvent e) {

	}

	public void onExitAction(ActionEvent e) {
		this.shutdown();
	}

	public void onAboutAction(ActionEvent e) {
		if (pongTimeline != null) {
			pongTimeline.pause();
			new Alert(AlertType.INFORMATION, "This game of Pong was made by Gaige Kinsey.", ButtonType.CLOSE).showAndWait();
			if (!paused) {
				pongTimeline.play();
			}
		} else {
			new Alert(AlertType.INFORMATION, "This game of Pong was made by Gaige Kinsey.", ButtonType.CLOSE).showAndWait();
		}
	}

	public void shutdown() {
		this.stage.close();
	}
}

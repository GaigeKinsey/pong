package edu.neumont.kinsey.pong;

import java.net.URL;

import edu.neumont.kinsey.controller.PongController;
import edu.neumont.kinsey.pong.view.PongViewController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class Pong extends Application {

	public static void main(String[] args) {
		Application.launch(Pong.class, args);
	}

	@Override
	public void start(Stage stage) throws Exception {
		URL location = this.getClass().getClassLoader().getResource("PongView.fxml");
		FXMLLoader loader = new FXMLLoader(location);
		Parent root = loader.load();
		Scene scene = new Scene(root);
		stage.setScene(scene);
		PongViewController viewController = loader.getController();
		viewController.setStage(stage);
		PongController controller = new PongController(viewController);
		Font.loadFont(this.getClass().getClassLoader().getResourceAsStream("PressStart2P.ttf"), 50);
		controller.run();
	}

}

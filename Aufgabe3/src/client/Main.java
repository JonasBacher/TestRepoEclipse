package client;
	
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.Pane;


public class Main extends Application {
	
	@Override
	public void start(Stage primaryStage) {
		try {
			FXMLLoader loader = new FXMLLoader(
					  getClass()
					  .getResource("FileTransfer.fxml")
			);
			Parent root = (Pane) loader.load();
			Controller controller;

			primaryStage.setTitle("Aufgabe 3");
			primaryStage.setScene(new Scene(root));
			primaryStage.setResizable(false);				// Fenstergröße kann nicht geändert werden
			
			primaryStage.show();
			
			controller = loader.<Controller>getController();
			primaryStage.setOnCloseRequest(e -> controller.closeAll());			// Wenn das Fenster geschlossen wird, wird controller.closeAll() aufgerufen
		} catch (Exception e) {
			Alert alert = new Alert(AlertType.ERROR);

			alert.setTitle("Error");
			alert.setHeaderText("Die Seite konnte nicht geladen werden");
			alert.setContentText("Versuchen Sie das Programm neu zu starten");
			alert.showAndWait();
		}
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}

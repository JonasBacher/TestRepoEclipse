package client;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

/**
 * Diese Klasse dient zum Darstellen einzelnerer Error-Ausgaben. 
 * Die Klasse ist nur in dieser Package sichtbar.
 * @author Patrick
 */
final class ErrorMessage {
	/**
	 * Stellt den Standard-Error dar.
	 */
	public static void standard() {
		standard("Die Verbindung zum Server konnte nicht hergestellt werden.");
	}

	/**
	 * Stellt den Error mit den übergebenen Inhalt 'contentText' und einer Meldung das Programm neuzustarten dar. 
	 * 
	 * @param contentText
	 */
	public static void standard(String contentText) {
		newMsg(contentText + "\nBitte starten Sie den Server und das aktuelle Programm neu.");
	}
	
	/**
	 * Stellt den Error mit den übergebenen Inhalt 'contentText' dar.
	 * @param contentText
	 */
	public static void newMsg(String contentText) {
		Alert alert = new Alert(AlertType.ERROR);

		alert.setHeaderText("Error");
		alert.setContentText(contentText);
		alert.showAndWait();
	}
}

package client;

import java.io.Closeable;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;

public class Controller {
	@FXML
	private Label lblServerNotReachable, lblFileDownloading;
	@FXML
	private TextField txfServerFilePath, txfClientFilePath;
	private Socket server;
	private DataInputStream in;
	private PrintWriter out;
	private final Object lockObject = new Object();
	private int downloadCounter = 0;
	
	@FXML
	public void initialize() {
		try {
			server = new Socket("localhost", 5555); // Socket zum Server erstellen
			in = new DataInputStream(server.getInputStream());
			out = new PrintWriter(server.getOutputStream(), true);		// autoflush ein
			
			
		} catch (UnknownHostException e) {
			displaySocketCreationFailedMsg("Der angegebene Server konnte nicht erreicht werden.");
		} catch (IOException e) {
			displaySocketCreationFailedMsg("Es ist mindestens ein Fehler aufgetreten, beim Versuch den Server zu erreichen.");
		}
	}
	
	/**
	 * Öffnet einen DirectoryChooser und setzt den erhaltenen Pfad auf 'txfClientFilePath'.
	 */
	public void chooseDirectory() {
		DirectoryChooser directoryChooser = new DirectoryChooser();
		File dir = directoryChooser.showDialog(null);

		if(dir != null) {			// Ordner 'dir' wurde ausgewählt 
			txfClientFilePath.setText(dir.getPath());
		}
	}
	
	/**
	 * Lädt die Datei in einem Thread herunter und speichert sie im clientPath ab.
	 */
	public void downloadFile() {
		String clientPath, serverPath;
		
		clientPath = txfClientFilePath.getText();
		serverPath = txfServerFilePath.getText();

		if(stringNotEmpty(clientPath) && stringNotEmpty(serverPath) && pathIsDirectory(clientPath)) {			// beide Pfade sind nicht leer und der clientPath ist ein Ordner
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					try {
						sendDownloadRequest(serverPath);
						System.out.println("Download-Anfrage gesendet.");
						
						saveReceivedFile(clientPath);			// Die heruntergeladene Datei wird gespeichert
					} catch (Exception e) {
						ErrorMessage.standard();
					}							// Das Downloaden wird gestartet
				}
			}).start();
		}else {
			ErrorMessage.newMsg("Mindestens einer der beiden Pfade ist ungültig, bitte geben Sie einen gültigen Pfad ein.\nAchtung: der untere Pfad muss ein gültiger Ordner sein");
		}
	}

	/**
	 * 
	 * @param str
	 * @return Gibt true zurück, wenn der übergebene String 'str' nicht leer ist.
	 */
	private boolean stringNotEmpty(String str) {
		return (str != null && !str.equals(""));
	}

	/**
	 * 
	 * @param path
	 * @return Gibt true zurück, wenn das Dokument des übergebenen Pfads 'path' existiert und ein Ordner ist.
	 */
	private boolean pathIsDirectory(String path) {
		File file = new File(path);
		
		return (file != null && file.exists() && file.isDirectory());
	}
	
	/**
	 * Sendet eine Downloadanfrage mit dem von 'serverPath' erhaltenen Pfad an den Server.
	 * @param serverPath
	 * @throws Exception
	 */
	private void sendDownloadRequest(String serverPath) throws Exception{
		out.println(serverPath);
	}
	
	/**
	 * Speichert die vom Server erhaltene Datei auf dem Pfad 'clientPath'.
	 * @param clientPath
	 */
	private void saveReceivedFile(String clientPath) {
		FileOutputStream fos = null;
		String fileName = "";
		boolean downloadSuccess;
		
		// TODO: mit Übergabe einer großen Datei probieren, parallelen Download testen
		
		try {
			increaseCounter();			// Counter wird erhöht, da der Download jetzt gestartet wird
			
			fileName = in.readUTF();
			synchronized (this) {		// Datei wird gelesen und im Download-Ordner gespeichert, nur eine Datei darf den Stream zum Server benutzen (Stream wird nur von einem Thread gelesen, sodass es keine Komplikationen gibt, da der andere Thread ihn bytes "stahl")
				downloadSuccess = saveFile(clientPath, fileName);
			}
			
			decreaseCounter();			// Counter wird erniedrigt, da der aktuelle Download beendet wurde 
			if(downloadSuccess) {						// Erfolgreicher Download
				if(fileName.startsWith("*")) {				// Ordner
					fileName = fileName.substring(1);		// Stern wird entfernt (Stern wird bei Ordnern mitgesendet)
				}
				
				displayFileDownloaded(clientPath, fileName);			// User auf erfolgreichen Download hinweisen
			}else {									// Datei wurde beim Server nicht gefunden
				showFileNotFoundError();				// User wird auf Nichtfinden der Datei informiert
			}
		} catch (IOException e) {
			decreaseCounter();			// Counter wird erniedrigt, da der aktuelle Download beendet wurde
			showError();
		} finally {
			if(fos != null) {
				try {
					fos.close();
				} catch (IOException e) {}
			}
		}
	}

	/**
	 * Speichert die vom Server erhaltenen Dateien an den übergebenen Pfad. Es werden Dateien und Ordner gespeichert.
	 * @param clientPath
	 * @param fileName
	 * @return Gibt true zurück, wenn der Server alle Dateien finden konnte, ansonsten wird false zurückgegeben.
	 * @throws IOException 
	 */
	private boolean saveFile(String clientPath, String fileName) throws IOException {
		FileOutputStream fos;
		
		if(stringNotEmpty(fileName)) {				// Datei wurde beim Server gefunden -> das Kopieren kann begonnen werden
			if(fileName.startsWith("*")) {				// erhaltene Datei ist ein Ordner
				saveDirectory(clientPath, fileName.substring(1));		// * wird vom Namen entfernt
			}else {										// erhaltene Datei ist ein Dokument
				fos = new FileOutputStream(clientPath + "\\" + fileName);		// OutputStream zur Datei wird erstellt
				saveDocument(fos);
				if(fos != null) fos.close();			// Stream zur Datei wird geschlossen
			}
			
			return true;
		}else {											// Datei wurde beim Server nicht gefunden 
			return false;
		}
	}
	
	/**
	 * Erstellt einen Ordner und speichert die im Ordner enthaltenen Datein ab.
	 * @param clientPath
	 * @param dirName
	 * @throws IOException
	 */
	private void saveDirectory(String clientPath, String dirName) throws IOException {
		File dir;
		int amountFiles;
		
		clientPath = clientPath + "\\" + dirName;				// Der Ordnername wird zum aktuellen Pfad hinzugefügt
		dir = new File(clientPath);
		
		if(dir != null) {
			if(dir.exists() || dir.mkdir()) {					// Ordner besteht bereits oder ein neuer Ordner konnte erstellt werden
				amountFiles = in.readInt();		// Anzahl an Dateien wird auf amountFiles geschrieben
				
				for(int i = 0; i < amountFiles; i++) {
					saveFile(clientPath, in.readUTF());		// Datei oder Ordner wird gespeichert
				}
			}
		}
	}

	/**
	 * Leitet die vom Server erhaltenen Bytes zum übergebenen FileOutputStream 'fos' weiter.
	 * @param fos
	 * @throws IOException
	 */
	private void saveDocument(FileOutputStream fos) throws IOException {
		long fileSize = 0;
		int bytesRead = 0;
		byte[] buf = new byte[1024];
		
		fileSize = in.readLong();
		while(fileSize > 0 && (bytesRead = in.read(buf, 0, (int)Math.min(buf.length, fileSize))) != -1) {	// byteArray ist nicht leer und es kann gelesen werden (zum Lesen wird entweder die Länge des Puffers 'buf' verwendet oder fileSize, falls nicht mehr 1024 bytes zu Empfangen sind)		
			fos.write(buf, 0, bytesRead);
			fos.flush();
			fileSize -= bytesRead;		// Die Dateigröße wird nach jeder Runde minimiert, da ja nach jeder Runde 'bytesRead'-bytes empfangen werden
		}
	}

	private void increaseCounter() {
		synchronized (lockObject) {
			downloadCounter++;
			displayDownloads();
		}
	}
	
	private void decreaseCounter() {
		synchronized (lockObject) {
			downloadCounter--;
			displayDownloads();
		}
	}
	// TODO: mit parallelen Download diese Ausgabe unten testen
	/**
	 * Stellt beim lblFileDownloading die Anzahl der heruntergeladenen Dateien dar.
	 */
	private void displayDownloads() {
		Platform.runLater(new Runnable() {
			
			@Override
			public void run() {
				if(downloadCounter == 0) {						// Wenn keine Downloads mehr im Gange sind wird das Label ausgeblendet
					lblFileDownloading.setVisible(false);
				}else {
					lblFileDownloading.setText((downloadCounter == 1 ? "Datei wird" : downloadCounter + " Dateien werden") + " heruntergeladen...");		// Wenn nur ein Downloader gestartet wurde kommt die Ausgabe Datei wird heruntergeladen ansonsten wird die Anzahl an Dateien angegeben
					lblFileDownloading.setVisible(true); 								// Label wird sichtbar gemacht
				}
			}
		});
	}

	/**
	 * Stellt eine Ausgabe dar, in der der User benachrichtigt wird, dass der Download erfolgreich beendet worden ist. Dies wird im UI-Thread erledigt, der für die ganzen UserInterface(UI)- Elemente zuständig ist.
	 * @param fileName: Name der heruntergeladenen Datei.
	 * @param filePath: Der Pfad, wo die Datei gespeichert wurde
	 */
	private void displayFileDownloaded(String filePath, String fileName) {
		Platform.runLater(new Runnable() {				// die Ausgabe läuft im UIThread
			
			@Override
			public void run() {
				Alert alert = new Alert(AlertType.INFORMATION);
				
				alert.setHeaderText("Datei erfolgreich heruntergeladen");
				alert.setContentText(fileName + " wurde erfolgreich in " + filePath + " heruntergeladen.");
				alert.show();
			}
		});
	}
	
	/**
	 * Stellt einen "Datei nicht gefunden" Error dar. Dies wird im UI-Thread erledigt, der für die ganzen UserInterface(UI)- Elemente zuständig ist.
	 */
	private void showFileNotFoundError() {
		Platform.runLater(new Runnable() {
			
			@Override
			public void run() {
				ErrorMessage.newMsg("Error: Die Datei wurde beim Server nicht gefunden.");
			}
		});
	}
	
	/**
	 * Stellt einen Standard-Error dar. Dies wird im UI-Thread erledigt, der für die ganzen UserInterface(UI)- Elemente zuständig ist.
	 */
	private void showError() {
		Platform.runLater(new Runnable() {
			
			@Override
			public void run() {
				ErrorMessage.standard("Mindestens ein Fehler beim Download der Datei aufgetreten.");
			}
		});
	}

	/**
	 * Stellt eine Fehlermeldung dar und gleichzeitig wird das Label 'lblServerNotReachable' sichtbar gemacht.
	 * @param contentText
	 */
	private void displaySocketCreationFailedMsg(String contentText) {
		ErrorMessage.standard(contentText);
		lblServerNotReachable.setVisible(true);
	}
	
	/**
	 * Schließt alle offenen Input- und Output-Streams und den Socket 'server'.
	 */
	public void closeAll() {
		close(in);					// InputStream wird geschlossen
		close(out);					// OutputStream wird geschlossen
		close(server);	 			// Socket 'server' wird geschlossen
		System.out.println("closed");
	}
	
	/**
	 * Schließt, falls möglich, das übergebene Closeable-Element.
	 * @param closeable
	 */
	private <T extends Closeable> void close(T closeable){
		if(closeable != null) {
			try {
				closeable.close();
			} catch (IOException e) {}
		}
	}
}

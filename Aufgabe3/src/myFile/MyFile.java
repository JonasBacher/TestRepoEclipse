package myFile;

import java.io.File;
import java.io.IOException;

public abstract class MyFile {
	/**
	 * Sendet die Datei an den Client.
	 * @throws IOException
	 */
	public abstract void send() throws IOException;

	/**
	 * 
	 * @param file
	 * @return Gibt die erstellte MyFile zurück, wenn die übergebene File ungültig ist wird null zurückgegeben.
	 * @throws IOException 
	 */
	public static MyFile create(File file) throws IOException {
		if(file != null && file.exists()) {				// Datei ist gültig
			if(file.isDirectory()) {					// Datei ist ein Ordner
				return new MyDirectory(file);
			}else {										// Datei ist ein Dokument
				return new MyDocument(file);
			}
		}else {											// Datei nicht gefunden
			return null;
		}
	}
}

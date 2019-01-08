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
	 * @return Gibt die erstellte MyFile zur�ck, wenn die �bergebene File ung�ltig ist wird null zur�ckgegeben.
	 * @throws IOException 
	 */
	public static MyFile create(File file) throws IOException {
		if(file != null && file.exists()) {				// Datei ist g�ltig
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

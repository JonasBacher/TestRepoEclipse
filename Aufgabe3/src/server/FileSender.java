package server;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import myFile.MyFile;

public abstract class FileSender {
	private static DataOutputStream dos = null;	// wird synchronisiert --> nur eine Datei darf den Stream zum Client verwenden (Dateien werden somit nicht vermischt)
	
	/**
	 * Setzt den OutputStream. Dies muss als erstes gemacht werden
	 * @param dos
	 * @throws Exception 
	 */
	public static void setDataOutputStream(DataOutputStream dos) throws Exception {
		if(dos != null) {
			FileSender.dos = dos;
		}else {
			throw new Exception();
		}
	}
	
	/**
	 * Sendet den übergebenen Dateinamen und die bytes der Datei an den Client.
	 * @param fileName
	 * @param fileBytes
	 * @throws IOException
	 */
	public static void sendDocumentData(String fileName, byte[] fileBytes) throws IOException {
		synchronized (FileSender.dos) {
			dos.writeUTF(fileName);					// Name der Datei wird gesendet
			dos.writeLong(fileBytes.length);	// Anzahl der Bytes wird gesendet
			dos.write(fileBytes);				// Bytes werden gesendet
			dos.flush();
		}
	}
	
	/**
	 * Sendet den übergebenen Ordnernamen und die Anzahl an Dateien zum Client. Zudem werden alle Dateien des Ordners gesendet.
	 * @param name
	 * @param amountFiles
	 * @param files
	 * @throws IOException
	 */
	public static void sendDirectoryData(String name, int amountFiles, ArrayList<MyFile> files) throws IOException {
		synchronized (FileSender.dos) {
			dos.writeUTF("*" + name);		// Key, sodass der Client weiß, dass es ein Ordner ist und Name der Datei wird gesendet
			dos.writeInt(amountFiles);		// Anzahl an Dateien wird gesendet
			dos.flush();
			for (MyFile file : files) {			// die im Ordner enthaltenen Dateien werden gesendet
				file.send();
			}
		}
	}
	
	/**
	 * Sendet eine leere Nachricht an den Client.
	 * @throws IOException
	 */
	public static void sendEmptyMsg() throws IOException {
		synchronized (FileSender.dos) {
			dos.writeUTF("");
			dos.flush();
		}
	}
}

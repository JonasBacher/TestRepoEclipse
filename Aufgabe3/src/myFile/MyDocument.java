package myFile;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import server.FileSender;

public class MyDocument extends MyFile{
	private String fileName;
	private byte[] fileBytes;
	
	public MyDocument(File file) throws IOException {
		this.fileName = file.getName();
		this.fileBytes = getByteArr(file);
	}
	
	@Override
	public void send() throws IOException {
		FileSender.sendDocumentData(fileName, fileBytes);
	}

	@Override
	public String toString() {
		return "doc";
	}
	
	/**
	 * 
	 * @param file
	 * @return Gibt die Bytes der übergebenen Datei 'file' als Byte-Array zurück.
	 * @throws IOException 
	 */
	private byte[] getByteArr(File file) throws IOException {
		BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
		byte[] fileBytes = new byte[(int)file.length()];

		bis.read(fileBytes, 0, fileBytes.length);			// Datei-Bytes werden gelesen
		bis.close();
		
		return fileBytes;
	}
}

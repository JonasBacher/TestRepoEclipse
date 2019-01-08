package server;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import myFile.MyFile;

public class Session extends Thread{
	private Socket client;
	private boolean clientReachable = true;
	
	public Session(Socket client) {
		this.client = client;
		this.start();     			// Thread (Session) startet sich selbst
	}
	
	@Override
	public void run() {
		System.out.println("Der Client hat mit der IP " + client.getInetAddress() + " hat sich verbunden.");
		BufferedReader in = null;
		DataOutputStream out = null;
		
		try {
			in = new BufferedReader(new InputStreamReader(client.getInputStream()));
			out = new DataOutputStream(client.getOutputStream());
			
			FileSender.setDataOutputStream(out); 				// OutputStream wird gesetzt, sodass die Klasse FileSender ihn benutzen kann
			while(clientReachable) {	
				sendFile(in);
			}	
		}catch(Exception e) {}
		
		// Verbindung zum Client beendet
		System.out.println("Verbindung zum Client mit der IP " + client.getInetAddress() + " wurde beendet.");
		try {		// Streams und Socket schließen
			if(out != null) {
				out.close();
			}
			if(in != null) {
				in.close();
			}
			client.close();
		} catch (IOException e) {}
	}

	/**
	 * Sendet die vom Client angeforderte Datei an den Client zurück.
	 * @param os
	 * @throws IOException 
	 */
	private void sendFile(BufferedReader in) throws IOException {
		String input = in.readLine();		// befindet sich außerhalb des Threads, da vom Input gelesen wird und dies nicht von mehreren Threads gemacht werden soll
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					MyFile myFile = MyFile.create(getFile(input));
					
					if(myFile != null) {
						if(myFile.toString().equals("doc")) {
							System.out.println("Datei ist ein Dokument.");
						}else {
							System.out.println("Datei ist ein Ordner.");
						}
						
						myFile.send();
						System.out.println("Die Datei wurde an den Client zurückgesendet.");
					}else {											// Datei wurde nicht gefunden
						FileSender.sendEmptyMsg();					// Client wird informiert
						System.out.println("Der Client wurde darüber benachrichtigt, dass die Datei nicht gefunden wurde.");
					}
				} catch(IOException e) {
					clientReachable = false;			// Wenn ein Fehler bei der Kommunikation mit dem Client auftritt wird die Schleife unterbrochen (clientrReachable = false beendet die Schleife)
				}
			}
		}).start();
	}

	/**
	 * 
	 * @param path
	 * @return Gibt die mit dem Pfad 'path' gefundene Datei als File zurück. Falls der Pfad ungültig ist wird null zurückgegeben und 'clientReachable' wird auf false gesetzt.
	 */
	private File getFile(String path) {
		if(path != null && !path.equals("")) {			// Pfad nicht leer
			System.out.println("Pfad erhalten: " + path);
			return new File(path);
		}else {
			clientReachable = false;					// null oder "" erhalten -> Verbindung zum Client wird getrennt
			return null;
		}
	}
}

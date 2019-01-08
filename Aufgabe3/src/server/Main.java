package server;

import java.io.IOException;
import java.net.ServerSocket;

public class Main {

	public static void main(String[] args) {
		ServerSocket serverSocket = null;
		
		try {
			serverSocket = new ServerSocket(5555);
			
			System.out.println("Server gestartet...");
			while(true) {
				new Session(serverSocket.accept());
			}
		} catch(IOException e) {
			System.err.println("Der Server konnte nicht gestartet werden. Möglicherweise läuft bereits ein anderer Server mit der Portnummer 5555.");
		}
		if(serverSocket != null) {
			try {
				serverSocket.close();
			} catch (IOException e) {}
		}
	}

}

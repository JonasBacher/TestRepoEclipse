package myFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import server.FileSender;

public class MyDirectory extends MyFile{
	private File dir;
	private ArrayList<MyFile> files = new ArrayList<>();
	
	public MyDirectory(File file) throws IOException {
		this.dir = file;
		
		for (File f : file.listFiles()) {		// Die im Ordner gefundenen Dateien werden in die List gegeben
			files.add(MyFile.create(f));
		}
	}
	
	@Override
	public void send() throws IOException {
		FileSender.sendDirectoryData(dir.getName(), files.size(), files);	// name und size wird gesendet, sodass der Client weiﬂ, dass es ein Ordner ist anschlieﬂend werden die Dateien des Ordners gesendet
	}

	@Override
	public String toString() {
		return "dir";
	}
	
}
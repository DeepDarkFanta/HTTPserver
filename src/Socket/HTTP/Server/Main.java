package Socket.HTTP.Server;


import java.io.*;
import java.net.Inet4Address;
import java.net.ServerSocket;

public class Main {
	private static final int PORT = 8000;
	static PrintWriter logFile;
	static String logger;
	static String directory = "./files";
	public static DataBase dataBase;
	
	public static void main( String[] args ) throws IOException {
		ServerSocket serverConnect = new ServerSocket( PORT );
		System.out.println( "Server started.\nListening for connections : "  + Inet4Address.getLocalHost().getHostAddress() + ":" + PORT );
		logFile = new PrintWriter( new FileOutputStream( "log.txt" ) );
		dataBase = new DataBase();
		while (true) new ClientManager( serverConnect.accept(), dataBase, directory ).start();
	}
	
	
}





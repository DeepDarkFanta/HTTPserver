package Socket.HTTP.Server;

import IO.FileIOManager;
import IO.InputReader;
import IO.OutputWriter;

import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.StringTokenizer;

public class ClientManager implements Runnable {
	
	private String name;
	private Socket client;
	private DataBase dataBase;
	private String directory;
	private String secret;
	private String requestType = "";
	
	private InputReader in;
	private OutputWriter out;
	
	private String receivedContents;
	
	private final String NOT_FOUND = "<html>\n" + "<head><title>404 Not Found</title></head>\n" +
			                                 "<body bgcolor=\"white\">\n" +
			                                 "<center><h1>404 Not Found</h1></center>\n" +
			                                 "<hr><center>:/</center>\n" +
			                                 "</body>\n" +
			                                 "</html>";
	
	public ClientManager( String name , Socket client ) {
		this.name = name;
		this.client = client;
	}
	
	ClientManager( Socket client, DataBase dataBase, String directory ) {
		this.client = client;
		this.dataBase = dataBase;
		this.directory = directory;
		name = "Client";
	}
	
	void start() {
		writeLog( "Accepting Connection" );
		new Thread( this , name ).start();
	}
	
	@Override
	public void run() {
		try {
			in = new InputReader( this.client.getInputStream() );
			out = new OutputWriter( this.client.getOutputStream() );
			String startLine = in.readNextLine();
			dataBase.addClient( client );

			if (startLine != null) {
				receivedContents = startLine + "\r\n" + new String( in.read() ).trim();
				writeLog( "New Request: " + receivedContents );
			}
			
			if (startLine == null || startLine.isEmpty() || startLine.isBlank()) {
				INVALID_Handler();
				HTTP_Write( "400 BAD REQUEST",null,null );
				closeConnection();
			} else {
				StringTokenizer stk;
				stk = new StringTokenizer( startLine , " " );
				String req = stk.nextToken(), path = "", httpType = "";
				if (stk.hasMoreTokens()) path = stk.nextToken();
				if (stk.hasMoreTokens()) httpType = stk.nextToken();
				
				if (!httpType.equalsIgnoreCase( "HTTP/1.1" )) {
					INVALID_Handler();
				} else if (req.equalsIgnoreCase( "GET" )) {
					requestType = "GET";
					GET_Handler( path );
				} else if (req.equalsIgnoreCase( "POST" )) {
					requestType = "POST";
					POST_Handler( path );
				} else {
					INVALID_Handler();
				}
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	private void writeLog( String log ) {
		String logMsg = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" ).format( Calendar.getInstance().getTime() ) + " >> " + client.getInetAddress().getHostAddress() + ":" + client.getPort() + " >> " + log;
		Main.logger += logMsg + "\r\n";
		Main.logFile.println( logMsg );
		System.out.println( logMsg );

		if (logMsg.contains("username"))
			secret = logMsg.substring(logMsg.indexOf("username"));

		Main.logFile.flush();
	}
	
	
	private void GET_Handler( String path ) throws IOException {

		String statusText = "200 ok";
		String extension;
		String type;
		System.out.println( "url " + path );

		if ( !dataBase.isClientAuto(client) )
		{
			System.out.println("client is not authorized");
			if ( !path.equals("/style.css") && !path.equals("/wallpaper.png") )
			{
				path = "/login.html";
				statusText = "401 unauthorized";
			}
			path = "/login" + path;
		}

		var filePath = Path.of( directory, path );

		if ( Files.exists(filePath) && !Files.isDirectory(filePath) )
		{
			extension = this.getFileExtension( filePath );
			type = Files.probeContentType( filePath );
			byte[] fileBytes = Files.readAllBytes( filePath );

			writeLog( "requested file: " + filePath );
			HTTP_Write( statusText , type , fileBytes );
		}
		else
		{
			System.out.println( "does not exsist" );
			type = "text";
			statusText = "404 not found";

			writeLog( "requested file: " + filePath );
			HTTP_Write( statusText , "text/html" , NOT_FOUND.getBytes() );
		}



//		if (!file.canread()) {
//			writelog( path + " file not found in directory" );
//			http_write( "404 not found" , "text/html" , not_found.getbytes() );
//		} else {
//			writelog( "requested file: " + file.getpath() );
//			http_write( "200 ok" , files.probecontenttype( file.topath() ) , fileiomanager.readfilebytes( file.getpath() ) );
//		}
	}
	
	private void POST_Handler( String path ) throws IOException {
		String data = receivedContents.substring( receivedContents.lastIndexOf( "user=" ) + "user=".length() );
		writeLog( "Received Form Data: " + data );
		String postReply = new String( FileIOManager.readFileBytes( "./files/login/successfully.html" ) ).replaceFirst( "<h2> Post-> </h2>" , "<h2> Post->\"" + data + "\" </h2>" );
		System.out.println("-------------Secret: " + secret);

		HTTP_Write( "200 OK" , "text/html" , postReply.getBytes() );
	}
	
	private void INVALID_Handler() throws IOException {
		writeLog( "Invalid request line without GET/POST" );
		HTTP_Write( "400 BAD REQUEST" , null , null );
	}
	
	
	private void HTTP_Write( String status , String MMI , byte[] contents ) throws IOException {
		if (MMI != null) writeLog( "Sending Contents With MIME Type: " + MMI );
		
		out.writeLine( "HTTP/1.1 " + status );
		if (MMI != null) {
			out.writeLine( "Content-Type: " + MMI );
			out.writeLine( "Content-Length: " + contents.length );
			out.writeLine( "Connection: close" );
		}
		out.writeLine();
		if (contents != null) out.write( contents );
		
		closeConnection();
	}
	
	void closeConnection() throws IOException {
		writeLog( "Terminating Connection" );
		out.close();
		in.close();
		client.close();
		writeLog( "Terminated Connection" );
	}

	private String getFileExtension(Path path)
	{
		var name = path.getFileName().toString();
		var extensionStart = name.lastIndexOf(".");
		return extensionStart == -1 ? "" : name.substring(extensionStart + 1);
	}


}

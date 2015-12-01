
import java.net.*;
import java.util.Arrays;
import java.io.*;
import java.lang.String;

public class TCPServer { 
	public void run() {
		try {
			
			int serverPort = 3330; // On linux only root can bind port < 1024
			ServerSocket serverSocket = new ServerSocket(serverPort);
			
			serverSocket.setSoTimeout(10000); 
			while(true) {
				System.out.println("Waiting for client on port " + serverSocket.getLocalPort() + "..."); 
		
				String[] strArray = { "Вика,#FF0000", "Пётр,#00FF00", "Лена,#0000FF", "Вова,#999900", "Екатерина,#FF00FF","Ирина,#00FFFF"};
				
				Socket clientConnected;
				try {
					clientConnected = serverSocket.accept();
				} catch (Exception e) {
					continue;
				}
			
				System.out.println("Just connected to " + clientConnected.getRemoteSocketAddress()); 			
				String line;
				
				// Send string to client
				BufferedWriter toClient = new BufferedWriter(new OutputStreamWriter(clientConnected.getOutputStream(), "UTF-8"));
				line = concatStringsWSep(strArray, ";");
				System.out.println("Send to client: " + line); 
				toClient.write(line);
				toClient.newLine();
				toClient.flush();
				
				// Get answer
				BufferedReader fromClient = new BufferedReader(new InputStreamReader(clientConnected.getInputStream(), "UTF-8"));
				line = fromClient.readLine();
				System.out.println("Server received: " + line); 
				
				// All done, close connection
				clientConnected.close();
			}
		}
		catch(UnknownHostException ex) {
			ex.printStackTrace();
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
	
	
		TCPServer srv = new TCPServer();
		srv.run();
	
	}

	public static String concatStringsWSep(String[] strings, String separator) {
		StringBuilder sb = new StringBuilder();
		String sep = "";
		for(String s: strings) {
			sb.append(sep).append(s);
			sep = separator;
		}
		return sb.toString();                           
	}
}
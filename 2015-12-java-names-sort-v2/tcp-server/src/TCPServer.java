
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import java.io.*;
import java.lang.String;

public class TCPServer { 
	
	public static final int serverPort = 3330; // On linux only root can bind port < 1024
	public ServerSocket serverSocket;
	Socket clientConnected;
	
	BufferedWriter clientWriter;
	BufferedReader clientReader;
	
	public class NameItem {
		public String name;
		public String color;		
	};
	
	public ArrayList<NameItem> nameItems;
	
	public class NameItemSorter implements Comparator<NameItem> {
		public int compare(NameItem o1, NameItem o2) {
			return o1.name.compareTo(o2.name);
		}
	}
		
	public static void main(String[] args) {	
		TCPServer srv = new TCPServer();
		srv.run();	
	}
	
	public void run() {
		try {
			serverSocket = new ServerSocket(serverPort);			
			serverSocket.setSoTimeout(10000); 
			while(true) {
				System.out.println("Waiting for client on port " + serverSocket.getLocalPort() + "..."); 
										
				try {
					clientConnected = serverSocket.accept();
				} catch (Exception e) {
					continue;
				}
			
				System.out.println("Just connected to " + clientConnected.getRemoteSocketAddress());
				
				nameItems = new ArrayList<NameItem>(); // Create new list
				
				clientWriter = new BufferedWriter(new OutputStreamWriter(clientConnected.getOutputStream(), "UTF-8"));
				clientReader = new BufferedReader(new InputStreamReader(clientConnected.getInputStream(), "UTF-8"));
				
				while (true)
				{
					// Main commands loop					
					String line;
					String commandName = "";
					String commandData = "";
					
					// Read command from client
					line = clientReader.readLine();
					if (null == line) break;
					// System.out.println("Command received: " + line);
					
					String[] commandString = line.split("\t",2);
					commandName = commandString[0];
					if (commandString.length == 2) commandData = commandString[1];
					
					System.out.println("Line received: " + commandName);
					
					// Process command
					if (commandName.equals("add")) {
						commandAdd(commandData);
					} else if (commandName.equals("sort")) {
						commandSort(commandData);
					} else if (commandName.equals("close")) {
						break;
					} else {
						System.out.println("Unknown command: " + line);
					}
				}
								
				// All done, close connection
				System.out.println("Closing connection");
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
		
	public void commandAdd(String commandData)
	{
		String[] nameData = commandData.split(",",2);
		if (nameData.length < 2) return; // Wrong data
		NameItem item = new NameItem();
		item.name = nameData[0];
		item.color = nameData[1];
		nameItems.add(item);
		sendString("OK");	
		System.out.println("Added item name " + item.name + ", total items " + nameItems.size());
	}
		
	public void commandSort(String commandData)
	{
		Collections.sort(nameItems, new NameItemSorter());
		
		ArrayList<String> outData = new ArrayList<String>();
		for (NameItem item : nameItems)
		{
			outData.add(item.name + "," + item.color);
		}
		sendString(concatStringsWSep(outData.toArray(new String[outData.size()]), ";"));	
	}
		
	public void sendString(String data)
	{
		System.out.println("Send to client: " + data); 
		try {
			clientWriter.write(data);
			clientWriter.newLine();
			clientWriter.flush();
		} catch (IOException e) {			
			e.printStackTrace();
		}		
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
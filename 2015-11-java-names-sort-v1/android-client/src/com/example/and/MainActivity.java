package com.example.and;



import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import java.lang.String;
import java.util.Arrays;


public class MainActivity extends Activity {
	private Socket socket;
			 
	
		    private static final int SERVERPORT = 3330;
		    private static final String SERVER_IP = "192.168.80.3";
	 
	
		    @Override
	
	    public void onCreate(Bundle savedInstanceState) {
	
		        super.onCreate(savedInstanceState);
	
		        setContentView(R.layout.main);     
	
		 
	
		        new Thread(new ClientThread()).start();
	
		    }
	
		 
	
		    public void onClick(View view) {
	
		    	DataOutputStream dataOutputStream=null;
		    	DataInputStream dataInputStream=null;
		    	
		    	try {
	
	            EditText et = (EditText) findViewById(R.id.EditText01);
	
		            String str = et.getText().toString();
	
		            PrintWriter out = new PrintWriter(new BufferedWriter(
	
		                    new OutputStreamWriter(socket.getOutputStream())),
	
		                    true);
	
		            out.println(str);
	     //   dataOutputStream  = new DataOutputStream(socket.getOutputStream());
	       // dataInputStream  = new DataInputStream(socket.getInputStream());
	       // String[] strArray = { "string1", "string2", "string3" };
			 // System.out.println("Joining array of strings");
			  //System.out.println(StringUtils.join(strArray));
		        } catch (UnknownHostException e) {
	
		            e.printStackTrace();
	
		        } catch (IOException e) {
	
		            e.printStackTrace();
	
		        } catch (Exception e) {
	
		            e.printStackTrace();
	
		        }
	
		    }
	
		 
	
		    class ClientThread implements Runnable {
	
		 
	
		        @Override
	
		        public void run() {
	

	
		            try {
	
		                InetAddress serverAddr = InetAddress.getByName(SERVER_IP);
	
		 
	
		                socket = new Socket(serverAddr, SERVERPORT);
	
		 
	
		            } catch (UnknownHostException e1) {
	
		                e1.printStackTrace();
	
		            } catch (IOException e1) {
	
		                e1.printStackTrace();
	
		            }
	
		 
	
		        }
	
		 
	
		    }
	
		}
package com.example.lenaclient;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends Activity {
    private static final int SERVERPORT = 3330;
    private static final String SERVER_IP = "192.168.80.3";
    private static final String TAG = "MainActivity";
    private Socket socket;
    private ConnectTask connectTask;
    private SendDataTask sendTask;
    private Context ctx = null;

    public class DataItem {
        public String name;
        public String color;
    }

    // Class for sort items
    class DataItemSorter implements Comparator<DataItem> {
        public int compare(DataItem itemA, DataItem itemB) {
            return itemA.name.compareToIgnoreCase(itemB.name);
        }
    }

    ArrayList<DataItem> itemsRecived = null;
    ArrayList<DataItem> itemsSent = null;

    public class DataAdapter extends ArrayAdapter<DataItem> {
        private final DataItem[] values;

        public DataAdapter(DataItem[] values) {
            super(ctx, R.layout.row, values);
            this.values = values;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.row, parent, false);
            TextView textView = (TextView) rowView.findViewById(R.id.textName);

            // Set name
            textView.setText(values[position].name);
            // Set color
            try {
                textView.setTextColor(Color.parseColor(values[position].color));
            } catch (Exception e) {
                Log.e(TAG, "Color error!");
                e.printStackTrace();
            }

            return rowView;
        }
    }

    DataAdapter listAdapter = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ctx = this;
        setContentView(R.layout.main);
    }

    public void onClickConnect(View view) {
        connectTask = new ConnectTask();
        connectTask.execute();
    }
    public void onClickSort(View view) {
        if (socket.isConnected())
        {
            // Start send data task!
            sendTask = new SendDataTask();
            sendTask.execute();
        }
    }

    private class ConnectTask extends AsyncTask<Void, Void, Boolean> {
        private String error = "";
        private String recievedString = null;
        private ConnectTask() { }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                InetAddress serverAddr = InetAddress.getByName(SERVER_IP);
                socket = new Socket(serverAddr, SERVERPORT);
                DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());

                // 1. get string
                BufferedReader br = new BufferedReader(new InputStreamReader(dataInputStream, "UTF-8"));
                recievedString = br.readLine();
                Log.d(TAG, "Got string: " + recievedString);

                itemsRecived = new ArrayList<DataItem>();

                // 2. split string
                List<String> strings =Arrays.asList(recievedString.split(";"));

                for (String s : strings)
                {
                    List<String> stringsColors =Arrays.asList(s.split(","));
                    DataItem item = new DataItem();
                    item.name = stringsColors.get(0);
                    item.color = stringsColors.get(1);
                    itemsRecived.add(item);
                }

            } catch (Exception e1) {
                e1.printStackTrace();
                error = e1.getMessage();
            }
            return true;
        }

        protected void onPostExecute(Boolean result) {
            TextView txt = (TextView)findViewById(R.id.textView);
            if (null == socket)
            {
                txt.setText("Connection error: " + error);
            } else {
                txt.setText("Connected: " + String.valueOf(socket.isConnected()));

                // Okay, now show the data
                listAdapter = new DataAdapter(itemsRecived.toArray(new DataItem[itemsRecived.size()]));
                ListView list = (ListView)findViewById(R.id.listView);
                list.setAdapter(listAdapter);
            }
        }
    }

    private class SendDataTask extends AsyncTask<Void, Void, Boolean> {
        private String error = "";
        private String recievedString = "";
        private String sendedString = "";

        private SendDataTask() { }

        @Override
        protected Boolean doInBackground(Void... params) {

            // Send and sort data
            try {
                DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());

                // 3. sort string
                itemsSent = itemsRecived;
                Collections.sort(itemsSent, new DataItemSorter());

                ArrayList<String> strToSend = new ArrayList<String>();

                for(DataItem item : itemsSent)
                {
                    strToSend.add(item.name + "," + item.color);
                }

                sendedString = TextUtils.join(";", strToSend);
                Log.d(TAG, "Send string: " + sendedString);
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(dataOutputStream, "UTF-8"));
                bw.write(sendedString);
                bw.newLine();
                bw.flush();
                // socket.close();
            } catch (Exception e) {
                e.printStackTrace();
                error = e.getMessage();
            }
            return true;
        }

        protected void onPostExecute(Boolean result) {
            listAdapter = new DataAdapter(itemsSent.toArray(new DataItem[itemsSent.size()]));
            ListView list = (ListView)findViewById(R.id.listView);
            list.setAdapter(listAdapter);
        }
    }
}
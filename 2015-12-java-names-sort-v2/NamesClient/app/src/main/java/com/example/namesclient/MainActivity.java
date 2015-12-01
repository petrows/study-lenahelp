package com.example.namesclient;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.larswerkman.lobsterpicker.LobsterPicker;

public class MainActivity extends Activity implements View.OnClickListener {

    private static final int SERVERPORT = 3330;
    private static final String TAG = "MainActivity";
    private Socket socket = null;
    private Context ctx;

    private ConnectTask taskConnect;
    private CmdAddTask taskCommandAdd;
    private CmdSortTask taskCommandSort;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ctx = this;
        onConnected();
    }

    public void onConnected()
    {
        if (null != socket)
        {
            findViewById(R.id.buttonConnect).setEnabled(false);
            findViewById(R.id.buttonAdd).setEnabled(true);
            findViewById(R.id.buttonSort).setEnabled(true);
        } else {
            findViewById(R.id.buttonConnect).setEnabled(true);
            findViewById(R.id.buttonAdd).setEnabled(false);
            findViewById(R.id.buttonSort).setEnabled(false);
        }
    }

    public void onConnectClick(View view)
    {
        taskConnect = new ConnectTask();
        taskConnect.connectHost = ((TextView)findViewById(R.id.editHost)).getText().toString();
        taskConnect.execute();
    }

    public void onAddClick(View view)
    {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.color_dialog);
        dialog.setTitle("Add name");
        Button dialogButton = (Button) dialog.findViewById(R.id.buttonAdd);
        // if button is clicked, close the custom dialog
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();

                // Start add name
                taskCommandAdd = new CmdAddTask();
                taskCommandAdd.addName = ((TextView)dialog.findViewById(R.id.editName)).getText().toString();
                String hexColor = String.format("#%06X", (0xFFFFFF & ((LobsterPicker)dialog.findViewById(R.id.lobsterpicker)).getColor()));
                taskCommandAdd.addColor = hexColor;
                taskCommandAdd.execute();
            }
        });
        dialog.show();
    }

    public void onSortClick(View view)
    {
        taskCommandSort = new CmdSortTask();
        taskCommandSort.execute();
    }

    public class DataItem {
        public String name;
        public String color;
    }

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

    public void onSortDone(ArrayList<DataItem> items)
    {
        Log.d(TAG, "Items size: " + items);
        listAdapter = new DataAdapter(items.toArray(new DataItem[items.size()]));

        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.list_dialog);
        dialog.setTitle("Sorted list");
        ((ListView)dialog.findViewById(R.id.listView)).setAdapter(listAdapter);
        dialog.show();
    }

    @Override public void onClick(View v) {}

    private class ConnectTask extends AsyncTask<Void, Void, Boolean> {
        private String error = "";
        private String recievedString = null;
        private ConnectTask() { }

        public String connectHost = "";

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                InetAddress serverAddr = InetAddress.getByName(connectHost);
                socket = new Socket(serverAddr, SERVERPORT);
            } catch (Exception e1) {
                e1.printStackTrace();
                error = e1.getMessage();
            }
            return true;
        }

        protected void onPostExecute(Boolean result) {
            if (null == socket)
            {
                Toast.makeText(ctx, "Connection error: " + error, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(ctx, "Connected: " + String.valueOf(socket.isConnected()), Toast.LENGTH_SHORT).show();
            }
            onConnected();
        }
    }

    private class CmdAddTask extends AsyncTask<Void, Void, Boolean> {
        private String error = "";
        private String recievedString = null;
        private CmdAddTask() { }

        public String addName = "";
        public String addColor = "";

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
                bw.write("add\t" + addName + "," + addColor);
                bw.newLine();
                bw.flush();

                // Read answer
                BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
                recievedString = br.readLine();

            } catch (Exception e1) {
                e1.printStackTrace();
                error = e1.getMessage();
            }
            return true;
        }

        protected void onPostExecute(Boolean result) {
            Toast.makeText(ctx, recievedString, Toast.LENGTH_SHORT).show();
        }
    }

    private class CmdSortTask extends AsyncTask<Void, Void, Boolean> {
        private String error = "";
        private String recievedString = null;
        private CmdSortTask() { }

        ArrayList<DataItem> itemsRecived;

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
                bw.write("sort");
                bw.newLine();
                bw.flush();

                // 1. get string
                BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
                recievedString = br.readLine();
                Log.d(TAG, "Got string: " + recievedString);

                itemsRecived = new ArrayList<DataItem>();

                // 2. split string
                List<String> strings = Arrays.asList(recievedString.split(";"));

                for (String s : strings)
                {
                    List<String> stringsColors =Arrays.asList(s.split(","));
                    DataItem item = new DataItem();
                    item.name = stringsColors.get(0);
                    item.color = stringsColors.get(1);
                    itemsRecived.add(item);

                    Log.d(TAG, "Got item: " + item.name + ", " + item.color);
                }

            } catch (Exception e1) {
                e1.printStackTrace();
                error = e1.getMessage();
            }
            return true;
        }

        protected void onPostExecute(Boolean result) {
            Toast.makeText(ctx, "Items: " + itemsRecived.size(), Toast.LENGTH_SHORT).show();
            onSortDone(itemsRecived);
        }
    }
}

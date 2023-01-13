package com.example.stm_3;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private BluetoothDevice targetDevice;
    private static final int REQUEST_ENABLE_BT = 1;
    BluetoothAdapter mBluetoothAdapter = null;
    private SocketApp app;
    final static UUID uuid = UUID.fromString("c7d1abff-c8b6-4bd8-a1d4-b37c4ca207df");
    private MainActivity activity;
    private TextView statusBar;
    private ListView listMessages;
    private EditText etSend;
    private Button btnSend;
    private ArrayList<String> messagesArray = new ArrayList<>();
    private ArrayAdapter<String> messagesAdapter;

    public ArrayList<String> getMessagesArray() {
        return messagesArray;
    }

    public TextView getStatusBar() {
        return statusBar;
    }

    public EditText getEtSend() {
        return etSend;
    }

    public ListView getListMessages() {
        return listMessages;
    }

    public Button getBtnSend() {
        return btnSend;
    }

    public ArrayAdapter<String> getMessagesAdapter() {
        return messagesAdapter;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        activity = this;

        statusBar = (TextView) findViewById(R.id.statusBar);
        listMessages = (ListView) findViewById(R.id.listMessages);
        etSend = (EditText) findViewById(R.id.etSend);
        btnSend = (Button) findViewById(R.id.btnSend);
        activity.getStatusBar().setTextColor(Color.argb(255,255, 255, 255));
        final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBluetoothAdapter == null) {
            System.out.println("BT not supported");
            return;
        }

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        initialize(mBluetoothAdapter);

        messagesAdapter = new ArrayAdapter<>(activity,
                android.R.layout.simple_list_item_1, messagesArray);

        listMessages.setAdapter(messagesAdapter);
        messagesArray.add("TEST");
//    final Server server = new Server();
//    server.bond(this, mBluetoothAdapter);
        Button btnBondServer = (Button) findViewById(R.id.btnBondServer);
        Button btnBondClient = (Button) findViewById(R.id.btnBondClient);
        btnBondServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (targetDevice == null) {
                    System.out.println("|======================|");
                    System.out.println("The target device was not set");
                    System.out.println("|======================|");
                    return;
                }
                app = new ServerApp(activity, mBluetoothAdapter);
                app.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        });
        btnBondClient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (targetDevice == null) {
                    System.out.println("|======================|");
                    System.out.println("The target device was not set");
                    System.out.println("|======================|");
                    return;
                }

                app = new ClientApp(activity, targetDevice);
                app.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = etSend.getText().toString();
                try {
                    etSend.setText("");
                    messagesAdapter.add("ME: " + text);
                    app.sendMsg(text);
                } catch (IOException e) {
                    return;
                }
            }
        });
    }

    public void initialize(BluetoothAdapter mBluetoothAdapter) {
        Spinner btSpinner = (Spinner) activity.findViewById(R.id.btSpinner);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            System.out.println("|======================|");
            System.out.println("Do not have permission BLUETOOTH_CONNECT");
            System.out.println("|======================|");
            return;
        }
        final Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        System.out.println("|======================|");
        System.out.println("Got "+pairedDevices.size()+" paired devices");
        System.out.println("|======================|");
        ArrayList<String> pairedDevicesList = new ArrayList<>();
        for (BluetoothDevice device :
                pairedDevices) {
            pairedDevicesList.add(device.getName() + "\n[" + device.getAddress() + "]");
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(activity,
                android.R.layout.simple_list_item_1, pairedDevicesList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        btSpinner.setAdapter(adapter);

        btSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String targetDeviceDesc = parent.getItemAtPosition(position).toString();
                String mac = targetDeviceDesc.substring(
                        targetDeviceDesc.indexOf('[') + 1, targetDeviceDesc.indexOf(']'));

                for (BluetoothDevice device :
                        pairedDevices) {
                    if (device.getAddress().toString().equals(mac.toString())) {
                        targetDevice = device;
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
}
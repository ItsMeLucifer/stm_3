package com.example.stm_3;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.content.pm.PackageManager;
import android.graphics.Color;

import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class ServerApp extends SocketApp {

    private MainActivity activity;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothServerSocket socket;

    public ServerApp(MainActivity activity, BluetoothAdapter mBluetoothAdapter) {
        this.activity = activity;
        this.mBluetoothAdapter = mBluetoothAdapter;
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return null;
            }
            socket = mBluetoothAdapter
                    .listenUsingRfcommWithServiceRecord(APP_NAME, APP_UUID);

            while (true) {
                waitForConnection();
                setupStreams();
                synchronize();
                whileRunning();
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
              activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    activity.getStatusBar().setBackgroundColor(Color.argb(255,255, 0, 0));
                    activity.getStatusBar().setText("DISCONNECTED");
                }
              });
            closeConnections();
            return null;
        }
    }

    private void waitForConnection() throws IOException {
        try {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    activity.getStatusBar().setBackgroundColor(Color.argb(255,0, 150, 0));

                    activity.getStatusBar().setText("WAITING FOR A CONNECTION");
                }
            });
            connection = socket.accept();
        } catch (IOException e) {
            System.out.println("Ex in listening RFCOMM: " + e.getMessage());
            return;
        }
    }

    private void synchronize() throws IOException, ClassNotFoundException {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.getStatusBar().setBackgroundColor(Color.argb(255,0, 255, 0));
                activity.getStatusBar().setText("CONNECTED");
            }
        });
    }

    private void whileRunning() throws IOException, ClassNotFoundException {
        while (true) {
            final String msg = (String) input.readObject();
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    activity.getMessagesAdapter().add("CLIENT: " + msg);
                }
            });

        }
    }
}



package com.example.stm_3;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import java.io.IOException;

public class ClientApp extends SocketApp {
    private final MainActivity activity;
    private final BluetoothDevice targetDevice;

    public ClientApp(MainActivity activity, BluetoothDevice targetDevice) {
        this.activity = activity;
        this.targetDevice = targetDevice;
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            connectToServer();
            setupStreams();
            synchronize();
            whileRunning();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
      activity.runOnUiThread(new Runnable() {
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void run() {
          activity.getStatusBar().setBackgroundColor(Color.argb(255, 255, 0, 0));
          activity.getStatusBar().setText("DISCONNECTED");
        }
      });
            closeConnections();
            return null;
        }
    }

    private void connectToServer() throws IOException {
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        connection = targetDevice.createRfcommSocketToServiceRecord(APP_UUID);
        connection.connect();
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.getStatusBar().setBackgroundColor(Color.argb(255, 0, 150, 0));
                activity.getStatusBar().setText("CONNECTING");
            }
        });
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
            String msg = (String) input.readObject();
            activity.getMessagesAdapter().add("SERVER: " + msg);
        }
    }
}


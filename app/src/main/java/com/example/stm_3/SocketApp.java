package com.example.stm_3;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;

import androidx.annotation.RequiresApi;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.UUID;

abstract class SocketApp extends AsyncTask<Void, Void, Void> {
    public static final int PERIOD = 50;
    public static final int RUN_DELAY = 1000;
    protected static final String APP_NAME = "STM Lab 3";
    public static final UUID APP_UUID = UUID.fromString("5b1e84a5-24c5-4a6d-b3ee-3c2ea01b02c4");

    protected ObjectInputStream input;
    protected ObjectOutputStream output;
    protected BluetoothSocket connection;

    protected void setupStreams() throws IOException {
        output = new ObjectOutputStream(connection.getOutputStream());
        output.flush();
        input = new ObjectInputStream(connection.getInputStream());
    }

    protected void closeConnections() {
        try {
            output.close();
            input.close();
            connection.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void sendMsg(String msg) throws IOException {
        output.writeObject(msg);
        output.flush();
    }

    protected void showMsg(String msg) {
        int d = 1;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    protected int getArgb(int r, int g, int b) {
        return Color.argb(
                0.5f,
                Color.red(r),
                Color.green(g),
                Color.blue(b));
    }
}


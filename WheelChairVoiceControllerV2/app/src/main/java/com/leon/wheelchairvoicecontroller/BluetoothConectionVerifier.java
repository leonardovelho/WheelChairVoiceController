package com.leon.wheelchairvoicecontroller;

import android.bluetooth.BluetoothSocket;
import android.os.AsyncTask;

import java.io.IOException;

public class BluetoothConectionVerifier extends AsyncTask<BluetoothSocket, Void, Integer> {

    @Override
    protected Integer doInBackground(BluetoothSocket... bluetoothSockets) {
        while (bluetoothSockets[0] != null && bluetoothSockets[0].isConnected()) {
            try {
                bluetoothSockets[0].getOutputStream().write(0);
                Thread.sleep(200);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return 9;
    }
}

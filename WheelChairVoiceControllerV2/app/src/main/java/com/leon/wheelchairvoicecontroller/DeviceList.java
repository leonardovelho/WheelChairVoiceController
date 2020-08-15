package com.leon.wheelchairvoicecontroller;

import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import androidx.annotation.Nullable;

import java.util.Set;

public class DeviceList extends ListActivity {

    BluetoothAdapter m_BluetoothAdapter = null;

    public static String m_DevicesMacAddress;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ArrayAdapter<String> BluetoothArray = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);

        m_BluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        Set<BluetoothDevice> m_ParedDevices = m_BluetoothAdapter.getBondedDevices();

        if (m_ParedDevices.size() > 0){
            for (BluetoothDevice device : m_ParedDevices){
                BluetoothArray.add(device.getName() + '\n' + device.getAddress());
            }
        }
        setListAdapter(BluetoothArray);
    }


    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        String selectedDevice = ((TextView) v).getText().toString();
        String selectedMacAddress = selectedDevice.substring(selectedDevice.length() - 17);

        Intent returnedMacAddressFromClick = new Intent();
        returnedMacAddressFromClick.putExtra(m_DevicesMacAddress, selectedMacAddress);

        setResult(RESULT_OK, returnedMacAddressFromClick);

        finish();
    }
}

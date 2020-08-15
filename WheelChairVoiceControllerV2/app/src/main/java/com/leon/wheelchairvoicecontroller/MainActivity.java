package com.leon.wheelchairvoicecontroller;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    Button m_HelpButton;
    Button m_ConnectButton;
    TextView m_TextView;
    TextView m_ConDeviceAddress;
    TextView m_StatusTextView;
    BluetoothAdapter m_BluetoothAdapter = null;
    BluetoothDevice m_ConnectedDevice = null;
    BluetoothSocket m_BluetoothSocket;
    String m_MacAddress;

    FirebaseDatabase database;
    DatabaseReference myRef;

    static final UUID m_MyUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private static final int VOICE_LISTENING_REQUEST = 1;
    private static final int ACTIVATE_BLUETOOTH_REQUEST = 2;
    private static final int OPEN_PARED_DEVICES_LIST = 3;

    boolean isConected = false;
    boolean m_IsInDangerous = false;

    HashMap<String, String> m_CommandList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        m_TextView = findViewById(R.id.saidPhrase);
        m_ConDeviceAddress = findViewById(R.id.conDeviceAddress);
        m_StatusTextView = findViewById(R.id.statusTxtView);
        m_ConnectButton = findViewById(R.id.connectBtn);
        m_HelpButton = findViewById(R.id.helpButton);

        m_StatusTextView.setText(getString(R.string.disconnected));
        m_StatusTextView.setTextColor(Color.BLUE);

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference(getString(R.string.firebase_database));
        myRef.setValue(false);

        setOnClicks();
        m_CommandList = createCommandList();

        try {
            m_BluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

            if (m_BluetoothAdapter == null) {
                Toast.makeText(this, getString(R.string.error_initialize_bluetooth), Toast.LENGTH_LONG).show();
            }
            else if (!m_BluetoothAdapter.isEnabled()){
                Intent activateBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(activateBluetooth, ACTIVATE_BLUETOOTH_REQUEST);
            }
        } catch (Exception ex){
            Toast.makeText(this, getString(R.string.error_initialize_bluetooth), Toast.LENGTH_LONG).show();
        }

        refreshHelpButton();
    }

    public void startListening(View view) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        if (intent.resolveActivity((getPackageManager())) != null) {
            startActivityForResult(intent, VOICE_LISTENING_REQUEST);
        } else {
            Toast.makeText(this, getString(R.string.device_does_not_surpot_voice_recognition), Toast.LENGTH_LONG).show();
        }
    }

    private void setOnClicks() {
        m_HelpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                m_IsInDangerous = !m_IsInDangerous;
                myRef.setValue(m_IsInDangerous);
                refreshHelpButton();
            }
        });

        m_ConnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isConected){
                    try {
                        m_BluetoothSocket.close();
                        isConected = false;
                        m_ConnectButton.setText(getString(R.string.connect));
                        m_StatusTextView.setText(getString(R.string.disconnected));
                        m_StatusTextView.setTextColor(Color.BLUE);
                    } catch (IOException e) {

                    }
                } else {
                    Intent openParedDevicesList = new Intent(MainActivity.this, DeviceList.class);
                    startActivityForResult(openParedDevicesList, OPEN_PARED_DEVICES_LIST);
                }
            }
        });
    }

    private void refreshHelpButton(){
        if (m_IsInDangerous) {
            m_HelpButton.setText(getString(R.string.resolve));
            m_HelpButton.setBackgroundColor(Color.RED);
            m_HelpButton.setTextColor(Color.DKGRAY);
        } else {
            m_HelpButton.setText(getString(R.string.help));
            m_HelpButton.setBackgroundColor(Color.LTGRAY);
            m_HelpButton.setTextColor(Color.RED);
        }
    }

    private HashMap<String, String> createCommandList() {
        HashMap<String, String> vCommandList = new HashMap<String, String>();

        vCommandList.put("PARAR", "5");
        vCommandList.put("FRENTE", "8");
        vCommandList.put("TRAS", "2");
        vCommandList.put("ESQUERDA", "4");
        vCommandList.put("DIREITA", "6");

        return vCommandList;
    }

    public void forceStop(View view) throws IOException {
        if (m_BluetoothSocket != null) {
            try {
                m_BluetoothSocket.getOutputStream().write(Integer.parseInt(m_CommandList.get(getString(R.string.stop))));
                m_TextView.setText(getString(R.string.stop));
            } catch (IOException e) {
                Toast.makeText(this, getString(R.string.error_sending_message), Toast.LENGTH_LONG).show();
            }
        } else{
            Toast.makeText(this, getString(R.string.no_device_connected), Toast.LENGTH_LONG).show();
        }
    }

    private String getCommandFromSaidPhrase(String saidPhrase) {
        if (saidPhrase.contains("PARAR") || saidPhrase.contains("PARE"))
            return m_CommandList.get("PARAR");
        else if (saidPhrase.contains("FRENTE"))
            return m_CommandList.get("FRENTE");
        else if (saidPhrase.contains("TRAS") || saidPhrase.contains("TRÁS") || saidPhrase.contains("TRAZ") || saidPhrase.contains("TRÁZ"))
            return m_CommandList.get("TRAS");
        else if (saidPhrase.contains("ESQUERDA") || saidPhrase.contains("ESQUERDO"))
            return m_CommandList.get("ESQUERDA");
        else if (saidPhrase.contains("DIREITA") || saidPhrase.contains("DIREITO"))
            return m_CommandList.get("DIREITA");
        return m_CommandList.get("PARAR");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){
            case VOICE_LISTENING_REQUEST:
                if (resultCode == RESULT_OK && data != null){
                    ArrayList<String> speachResponse = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    String saidPhrase = speachResponse.get(0);
                    if (m_BluetoothSocket != null) {
                        try {
                            String commandToSend = getCommandFromSaidPhrase(saidPhrase.toUpperCase());
                            m_BluetoothSocket.getOutputStream().write(Integer.parseInt(commandToSend));
                            m_TextView.setText(saidPhrase);
                        } catch (IOException e) {
                            Toast.makeText(this, getString(R.string.error_sending_message), Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(this, getString(R.string.no_device_connected), Toast.LENGTH_LONG).show();
                    }
                }
                break;
            case ACTIVATE_BLUETOOTH_REQUEST:
                if (resultCode == RESULT_OK){
                    Toast.makeText(this, getString(R.string.bt_activated), Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(this, getString(R.string.bt_not_activated), Toast.LENGTH_LONG).show();
                    finish();
                }
                break;
            case OPEN_PARED_DEVICES_LIST:
                if (resultCode == RESULT_OK){
                    m_MacAddress = data.getExtras().getString(DeviceList.m_DevicesMacAddress);

                    m_ConnectedDevice = m_BluetoothAdapter.getRemoteDevice(m_MacAddress);

                    try{
                        m_BluetoothSocket = m_ConnectedDevice.createRfcommSocketToServiceRecord(m_MyUUID);

                        m_BluetoothSocket.connect();

                        Toast.makeText(this, "Conectado a: " + m_MacAddress, Toast.LENGTH_LONG).show();

                        m_ConDeviceAddress.setText(m_MacAddress);

                        m_StatusTextView.setText(getString(R.string.connected));
                        m_StatusTextView.setTextColor(Color.GREEN);
                        isConected = true;

                        m_ConnectButton.setText(getString(R.string.disconnect));

                        BluetoothConectionVerifier btConectionVerifier = new BluetoothConectionVerifier();
                        btConectionVerifier.execute(m_BluetoothSocket);

                    } catch (IOException ex){
                        Toast.makeText(this, getString(R.string.impossible_connect), Toast.LENGTH_LONG).show();
                        m_StatusTextView.setText(getString(R.string.error));
                        m_StatusTextView.setTextColor(Color.RED);
                    }
                } else{
                    Toast.makeText(this, getString(R.string.error_getting_device_address), Toast.LENGTH_LONG).show();
                    m_StatusTextView.setText(getString(R.string.error));
                    m_StatusTextView.setTextColor(Color.RED);
                }
                break;
        }
    }
}

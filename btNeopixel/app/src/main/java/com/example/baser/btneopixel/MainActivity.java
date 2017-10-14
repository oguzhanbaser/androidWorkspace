package com.example.baser.btneopixel;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorSelectedListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    Button bConnect, bSend, effButton;
    private Context context;
    BluetoothAdapter mBluetoothAdapter;
    Switch autoSwitch;
    TextView btText, effectText;
    BluetoothSocket btSocket = null;
    int aIndex = 0;
    boolean isBtConnected = false;
    ColorPickerView cView;
    SeekBar lightSeekbar;

    public int getRValue(String p_str)
    {
        return Integer.parseInt(p_str.substring(2, 4), 16);
    }

    public int getGValue(String p_str)
    {
        return Integer.parseInt(p_str.substring(4, 6), 16);
    }

    public int getBValue(String p_str)
    {
        return Integer.parseInt(p_str.substring(6, 8), 16);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bConnect = (Button)findViewById(R.id.button);
        bSend = (Button)findViewById(R.id.button3);
        cView = (ColorPickerView)findViewById(R.id.color_picker_view);
        autoSwitch = (Switch)findViewById(R.id.switch1);
        lightSeekbar = (SeekBar) findViewById(R.id.seekBar);
        effButton = (Button) findViewById(R.id.button2);
        effectText = (TextView) findViewById(R.id.textView);
        btText = (TextView) findViewById(R.id.textView2);

        context = this;

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if(mBluetoothAdapter == null)
        {
            Toast.makeText(getApplicationContext(), "Cihazda Bluetooth Yok", Toast.LENGTH_LONG).show();
            return;
        }

        if(!mBluetoothAdapter.isEnabled())
        {
            Toast.makeText(getApplicationContext(), "Litfen Bluetooth Açin!", Toast.LENGTH_LONG).show();
        }

        autoSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b)
                {
                    if(isBtConnected) {
                        lightSeekbar.setEnabled(false);
                        try {
                            btSocket.getOutputStream().write(("+1").getBytes());
                        } catch (IOException e) {
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(), "Gönderme Hatası!", Toast.LENGTH_LONG).show();
                        }
                    }

                }else {
                    if(isBtConnected) {
                        lightSeekbar.setEnabled(true);
                        try {
                            btSocket.getOutputStream().write(("+0").getBytes());
                        } catch (IOException e) {
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(), "Gönderme Hatası!", Toast.LENGTH_LONG).show();
                        }
                    }
                }
            }
        });

        lightSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(isBtConnected)
                {
                    String sendVal = "&" + Integer.toString(i);
                    try {
                        btSocket.getOutputStream().write(sendVal.getBytes());
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), "Gönderme Hatası!", Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        effButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builderSingle = new AlertDialog.Builder(MainActivity.this);

                builderSingle.setNegativeButton("İptal", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });

                if(isBtConnected)
                {
                    final ArrayList<String> effectNames = new ArrayList<String>();

                    effectNames.add(0, "Renk Çemberi");
                    effectNames.add(1, "Gökkuşağı");
                    effectNames.add(2, "Effekt 1");
                    effectNames.add(3, "Effekt 2");


                    final ArrayAdapter<String> veriAdaptoru = new ArrayAdapter<String>
                            (MainActivity.this, android.R.layout.simple_list_item_1, effectNames);

                    builderSingle.setAdapter(veriAdaptoru, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            String sendVal = "$|";
                            sendVal += Integer.toString(i);

                            if(isBtConnected) {
                                try {
                                    btSocket.getOutputStream().write(sendVal.getBytes());
                                    effectText.setText(veriAdaptoru.getItem(i));
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    Toast.makeText(getApplicationContext(), "Gönderme Hatası!", Toast.LENGTH_LONG).show();
                                }
                            }else{
                                Toast.makeText(getApplicationContext(), "Önce Bağlanmalısınız!", Toast.LENGTH_LONG).show();
                            }
                        }
                    }).show();
                }
            }
        });

        bConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder builderSingle = new AlertDialog.Builder(MainActivity.this);

                builderSingle.setNegativeButton("İptal", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });

                if(!isBtConnected) {

                    if (!mBluetoothAdapter.isEnabled()) {
                        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(enableBtIntent, 1);      //REQUEST_ENABLE_BT = 1
                    }

                    final ArrayList<String> btNames = new ArrayList<String>();

                    Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
                    aIndex = 0;

                    if (pairedDevices.size() > 0) {
                        // There are paired devices. Get the name and address of each paired device.
                        for (BluetoothDevice device : pairedDevices) {
                            String deviceName = device.getName();
                            String deviceHardwareAddress = device.getAddress(); // MAC address
                            btNames.add(aIndex++, deviceName + "\n" + deviceHardwareAddress);

                            final ArrayAdapter<String> veriAdaptoru = new ArrayAdapter<String>
                                    (MainActivity.this, android.R.layout.simple_list_item_1, btNames);

                            //btList.setAdapter(veriAdaptoru);
                            builderSingle.setAdapter(veriAdaptoru, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    String addr = veriAdaptoru.getItem(i).substring(veriAdaptoru.getItem(i).indexOf('\n') + 1);

                                    try{
                                        if((btSocket == null || !isBtConnected) && !addr.isEmpty())
                                        {
                                            BluetoothDevice mBtDevice = mBluetoothAdapter.getRemoteDevice(addr);
                                            btSocket = mBtDevice.createInsecureRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
                                            BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                                            btSocket.connect();
                                            isBtConnected = true;
                                            btText.setText("Bağlı");
                                        }
                                    }catch(IOException ex)
                                    {
                                        Toast.makeText(getApplicationContext(), "Cihaz Kapalı yada Timeout aşıldı", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });

                        }
                    }

                }else{
                    Toast.makeText(getApplicationContext(), "Zaten Bağlı", Toast.LENGTH_LONG).show();
                }

                builderSingle.show();

            }


        });

        cView.addOnColorSelectedListener(new OnColorSelectedListener() {
            @Override
            public void onColorSelected(int i) {
                String str = Integer.toHexString(i);

                int val1 = getRValue(str);
                int val2 = getGValue(str);
                int val3 = getBValue(str);

                String sendVal = "#" + Integer.toString(val1) + ","
                        + Integer.toString(val2) + ","
                        + Integer.toString(val3);

                if(isBtConnected) {
                    try {
                        btSocket.getOutputStream().write(sendVal.getBytes());
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), "Gönderme Hatası!", Toast.LENGTH_LONG).show();
                    }
                }else{
                    Toast.makeText(getApplicationContext(), "Önce Bağlanmalısınız!", Toast.LENGTH_LONG).show();
                }
            }
        });

        bSend.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                ColorPickerDialogBuilder
                        .with(context)
                        .setTitle("Renk Seç")
                        .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                        .density(12)
                        .setOnColorSelectedListener(new OnColorSelectedListener() {
                            @Override
                            public void onColorSelected(int i) {
                                String str = Integer.toHexString(i);

                                int val1 = getRValue(str);
                                int val2 = getGValue(str);
                                int val3 = getBValue(str);

                                String sendVal = "#" + Integer.toString(val1) + ","
                                        + Integer.toString(val2) + ","
                                        + Integer.toString(val3);

                                if(isBtConnected) {
                                    try {
                                        btSocket.getOutputStream().write(sendVal.getBytes());
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                        Toast.makeText(getApplicationContext(), "Gönderme Hatası!", Toast.LENGTH_LONG).show();
                                    }
                                }else{
                                    Toast.makeText(getApplicationContext(), "Önce Bağlanmalısınız!", Toast.LENGTH_LONG).show();
                                }
                            }

                        }).build().show();
            }

        });


    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            btSocket.close();
            mBluetoothAdapter.cancelDiscovery();
            mBluetoothAdapter = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

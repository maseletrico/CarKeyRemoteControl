package com.maseletrico.remotecontrolkey;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private ImageView imageViewCadeado;
    private ImageView ivLockButton;
    private ImageView ivUnlockButton;
    private ImageView silentMode;
    private ImageView ivCarName;
    private ImageView ivBuzina;
    private ImageView ivPin;
    private ToneGenerator toneG;
    private Context context;

    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothDevice mmDevice;
    private static String TAG = "BLUE_TOOTH";
    private ConnectedThread mConnectedThread;
    // SPP UUID service - this should work for most devices
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private final static int REQUEST_ENABLE_BT = 1;
    private BluetoothSocket mSocket;
    private TextView textViewData;
    private Toolbar myToolbar;
    private String btName;
    private boolean txConectado=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myToolbar =  findViewById(R.id.my_toolbar_main);
        myToolbar.setLogo(R.mipmap.ic_launcher);
        //myToolbar.setTitle(R.string.config);
        setSupportActionBar(myToolbar);
        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();

        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);

        context = MainActivity.this;
        imageViewCadeado = findViewById(R.id.imageViewPad);
        ivLockButton = findViewById(R.id.imageViewLockButton);
        ivUnlockButton = findViewById(R.id.imageViewUnlockButton);
        textViewData = findViewById(R.id.textView_data);
        silentMode = findViewById(R.id.imageViewSilentMode);
        ivCarName = findViewById(R.id.imageView_car_name);
        ivBuzina = findViewById(R.id.imageView_buzina);
        ivPin = findViewById(R.id.iv_pin);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        checkBTState();

        //Get MAC address from DeviceListActivity via intent
        Intent intent = getIntent();

        //Get the MAC address from the DeviceListActivty via EXTRA
        String address = intent.getStringExtra(BluetoothList.BLUETOOTH_ADDRESS);
        btName = intent.getStringExtra(BluetoothList.BLUETOOTH_NAME);

        //create device and set the MAC address
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);


        ConnectThread connect = new ConnectThread(device,MY_UUID);
        connect.start();

        if(isSilentMode()){
            silentMode.setImageResource(R.mipmap.silent_mode);
        }else{
            silentMode.setImageResource(R.mipmap.silent_mode_on);
        }

        silentMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isSilentMode()){
                   setSilentMode(false);
                   silentMode.setImageResource(R.mipmap.silent_mode_on);
                }else{
                    setSilentMode(true);
                    silentMode.setImageResource(R.mipmap.silent_mode);

                }
            }
        });

        toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 100);

        //Lock button listener
        ivLockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isSilentMode()) {
                    toneG.startTone(ToneGenerator.TONE_DTMF_1, 50);
                }
                String sendData = "CML";
                byte[] messageBytes = sendData.getBytes();
                mConnectedThread.write(messageBytes);
            }
        });
        //Unlock button listener
        ivUnlockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isSilentMode()) {
                    toneG.startTone(ToneGenerator.TONE_DTMF_3, 50);
                }
                String sendData = "CMU";
                byte[] messageBytes = sendData.getBytes();
                mConnectedThread.write(messageBytes);
            }
        });
        //Buzina button listener
        ivBuzina.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isSilentMode()) {
                    toneG.startTone(ToneGenerator.TONE_DTMF_5, 50);
                }
                String sendData = "CMB";
                byte[] messageBytes = sendData.getBytes();
                mConnectedThread.write(messageBytes);
            }
        });
        //Change car name
        ivCarName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    builder = new AlertDialog.Builder(context, android.R.style.Theme_Material_Dialog_Alert);
                } else {
                    builder = new AlertDialog.Builder(context);
                }
                final EditText edittext = new EditText(context);
                edittext.setTextColor(Color.YELLOW);
                builder.setTitle(R.string.bluetoothID)
                        .setMessage("Preencha a caixa de texto para trocar a identificação do módulo")
                        .setView(edittext)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                //Limita entrada de texto a 20 caracteres
                                edittext.setFilters(new InputFilter[] { new InputFilter.LengthFilter(20) });
                                String sendData = "AT+NAME="+edittext.getText();
                                //String sendData = "AT+NAME=?";
                                byte[] messageBytes = sendData.getBytes();
                                mConnectedThread.write(messageBytes);
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        });

        //Troca o pin do módulo bluetooth
        ivPin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    builder = new AlertDialog.Builder(context, android.R.style.Theme_Material_Dialog_Alert);
                } else {
                    builder = new AlertDialog.Builder(context);
                }
                final EditText edittext = new EditText(context);
                edittext.setTextColor(Color.YELLOW);
                builder.setTitle(R.string.troca_de_pin)
                        .setMessage("Preencha a caixa de texto para trocar o PIN do módulo Bluetooth." +
                                " Anote o novo PIN em local seguro, esta operação não poderá ser desfeita.")
                        .setView(edittext)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                //Limita entrada de texto a 4 caracteres
                                edittext.setFilters(new InputFilter[] { new InputFilter.LengthFilter(4) });
                                String sendData = "AT+PSWD="+"\""+edittext.getText()+"\"" ;
                                byte[] messageBytes = sendData.getBytes();
                                mConnectedThread.write(messageBytes);
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        });

    }

    private class ConnectThread extends Thread {
        private BluetoothSocket mmSocket;


        public ConnectThread(BluetoothDevice device, UUID uuid) {
            Log.d(TAG, "ConnectThread: started.");
            mmDevice = device;

        }


        public void run() {
            // Cancel discovery because it will slow down the connection
            mBluetoothAdapter.cancelDiscovery();

            BluetoothSocket tmp = null;
            Log.i(TAG, "RUN mConnectThread ");

            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
                Log.d(TAG, "ConnectThread: Trying to create InsecureRfcommSocket using UUID: "
                        +MY_UUID);
                tmp = mmDevice.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "ConnectThread: Could not create InsecureRfcommSocket " + e.getMessage());
            }

            mmSocket = tmp;

            // Make a connection to the BluetoothSock
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                if (mmSocket != null) {
                    mmSocket.connect();
                }
                connected(mmSocket);
            } catch (IOException e) {
                // Close the socket
                try {
                    mmSocket.close();
                    Log.d(TAG, "run: Closed Socket.");
                } catch (IOException e1) {
                    Log.e(TAG, "mConnectThread: run: Unable to close connection in socket " + e1.getMessage());
                }
                Log.d(TAG, "run: ConnectThread: Could not connect to UUID: " + MY_UUID );

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        textViewData.setText(R.string.could_not_connect);
                        textViewData.append(" a "+ btName);
                    }
                });

            }

            //connected(mmSocket);

        }

        private void connected(BluetoothSocket mmSocket) {
            // Start the thread to manage the connection and perform transmissions
            mConnectedThread = new ConnectedThread(mmSocket);
            mConnectedThread.start();
        }

        /** Will cancel an in-progress connection, and close the socket */
        public void cancel() {
            try {
                mSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Error Closing Socket " + e.getMessage());
            }
        }
    }
    boolean isSilentMode() {
        SharedPreferences prefs = context.getSharedPreferences("silent_mode", MODE_PRIVATE);
        return prefs.getBoolean("silentmode",false); // will return 0 if no  value is saved

    }

    void setSilentMode(boolean mSilentMode) {
        SharedPreferences.Editor editor = context.getSharedPreferences("silent_mode", MODE_PRIVATE).edit();
        editor.putBoolean("silentmode", mSilentMode);//
        editor.apply();
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        ConnectedThread(BluetoothSocket socket) {
            Log.d(TAG, "Connected");

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    textViewData.setTextColor(Color.BLUE);
                    textViewData.setText(R.string.conectado);
                    textViewData.append(" a "+ btName);
                    //Show image views
                    imageViewCadeado.setVisibility(View.VISIBLE);
                    ivLockButton.setVisibility(View.VISIBLE);
                    ivUnlockButton.setVisibility(View.VISIBLE);
                    silentMode.setVisibility(View.VISIBLE);
                    ivCarName.setVisibility(View.VISIBLE);
                    ivBuzina.setVisibility(View.VISIBLE);
                    ivPin.setVisibility(View.VISIBLE);
                }
            });

            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.i(TAG, String.valueOf(e));
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
            //transmitr comando questiona hardware
            txConectado = true;
            String sendData = "CMQ";
            byte[] messageBytes = sendData.getBytes();
            write(messageBytes);


        }

        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);
                    final String incommingMessage = new String(buffer, 0, bytes);
                    // Send the obtained bytes to the UI activity
                    Log.i(TAG, "incomming: "+incommingMessage);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String horimetro=incommingMessage.substring(incommingMessage.indexOf("h") + 1);
                            //Log.i(TAG,"reading horimetro: "+horimetro);
                            //textViewHorimetro.setText(horimetro);
                            //textViewData.append(" a "+incommingMessage);
                            //textViewData.setText(incommingMessage);
                        }
                    });
                } catch (IOException e) {
                    Log.i(TAG,"Erro reading inputstream: "+String.valueOf(e));
                    break;
                }
            }
        }

        void write(byte[] bytes) {
            //create line feed character
            byte[] newLine = {13,10};
            //create byteArray
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            //add /r/n to byteArray
            try {
                output.write(bytes);
                output.write(newLine);
            } catch (IOException e) {
                e.printStackTrace();
            }


            byte[] out = output.toByteArray();

            try {
                mmOutStream.write(out);
            } catch (IOException e) {
                Log.e(TAG, "write: Error writing to output stream. " + e.getMessage() );
            }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "cancel error " + e );
            }
        }

    }
    @Override
    protected void onStop() {
        super.onStop();
        //Sai do modo de programação
//        String sendData = "CMS";
//        byte[] messageBytes = sendData.getBytes();
//        mConnectedThread.write(messageBytes);
        if(mConnectedThread != null){
            mConnectedThread.cancel();
        }
        //Verifica se o bluetooth está ligado
//        if (mBluetoothAdapter.isEnabled()) {
//            mBluetoothAdapter.disable();
////            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
////            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
//        }else{
//            Toast.makeText(this, "BlueTooth Desabilitado", Toast.LENGTH_SHORT).show();
//        }


    }

    @Override
    protected void onResume() {
        super.onResume();
        textViewData.setTextColor(Color.BLUE);
        textViewData.setText(R.string.conectando);
        textViewData.append(" a "+ btName);
        //Hide image views
        imageViewCadeado.setVisibility(View.GONE);
        ivLockButton.setVisibility(View.GONE);
        ivUnlockButton.setVisibility(View.GONE);
        silentMode.setVisibility(View.GONE);
        ivCarName.setVisibility(View.GONE);
        ivBuzina.setVisibility(View.GONE);
        ivPin.setVisibility(View.GONE);
    }

    //Checks that the Android device Bluetooth is available and prompts to be turned on if off
    private void checkBTState() {

        if(mBluetoothAdapter==null) {
            Toast.makeText(getBaseContext(), "Device does not support bluetooth", Toast.LENGTH_LONG).show();
        } else {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case R.id.action_exit:
//                // User chose the "Settings" item, show the app settings UI...
//                finish();
//                System.exit(0);
//                return true;
//            case R.id.action_revoque:
//                // User chose the "revoque sign in " item, show the app settings UI...
//                Intent intent = new Intent(this, MainActivity.class);
//                startActivity(intent);
//                return true;
//            default:
//                // If we got here, the user's action was not recognized.
//                // Invoke the superclass to handle it.
//
//        }
        return super.onOptionsItemSelected(item);
    }
}

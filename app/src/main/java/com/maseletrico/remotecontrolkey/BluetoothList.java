package com.maseletrico.remotecontrolkey;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.graphics.Movie;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


public class BluetoothList extends AppCompatActivity {

    private final static int REQUEST_ENABLE_BT = 1;
    private BluetoothAdapter mBluetoothAdapter;
    private ArrayAdapter<String> mArrayAdapter;
    private ListView lvDispositivosPareados;
    private List btPareadList;
    public final static String BLUETOOTH_ADDRESS = "BLUETOOTH_ADDRESS";
    public final static String BLUETOOTH_NAME = "BLUETOOTH_NAME";
    public final static String LAYOUT = "LAYOUT_TYPE";
    public final static String BULB = "Layout_Bulb";
    public final static String REMOTE_CONTROL = "Layout_Remote";

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    private RecyclerView mRecyclerView;
    //private RecyclerView.Adapter mAdapter;
    private MyAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private ImageView ivIconCar,ivIconLight,ivIconGarage,ivIconSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_list);

        Toolbar myToolbar =  findViewById(R.id.my_toolbar);
        myToolbar.setLogo(R.mipmap.ic_launcher);
        myToolbar.setTitle(R.string.dispositivos_pareados);
        setSupportActionBar(myToolbar);

        ivIconCar = findViewById(R.id.iv_icon_car);
        ivIconLight = findViewById(R.id.iv_icon_light);
        ivIconGarage = findViewById(R.id.iv_icon_garage);
        ivIconSwitch = findViewById(R.id.iv_icon_switch);

        ivIconCar.setVisibility(View.GONE);
        ivIconLight.setVisibility(View.GONE);
        ivIconGarage.setVisibility(View.GONE);
        ivIconSwitch.setVisibility(View.GONE);


        mRecyclerView = findViewById(R.id.recycler_view);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);




    }

    @Override
    protected void onResume() {
        super.onResume();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        if(currentUser==null){
//            Intent intent = new Intent(this, LoginActivity.class);
//            startActivity(intent);
        }

        //Verifica se o dispositivo tem bluetooth
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
            Toast.makeText(this, "Este dispositivo Android não permite conexão BlueTooth", Toast.LENGTH_SHORT).show();
        }

        //Verifica se o bluetooth está ligado
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }else{
            Toast.makeText(this, "BlueTooth Habilitado", Toast.LENGTH_SHORT).show();
        }

        //Consulta dispositivos pareados
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        // If there are paired devices
        if (pairedDevices.size() > 0) {
            btPareadList = new ArrayList<String>();
            // Loop through paired devices
            for (BluetoothDevice device : pairedDevices) {
                // Add the name and address to an array adapter to show in a ListView

                btPareadList.add(device.getName() + "\n" + device.getAddress());
                Log.i("BlueTooth: ",device.getName() + " " + device.getAddress());

            }

            // specify an adapter (see also next example)
            mAdapter = new MyAdapter(btPareadList);
            mRecyclerView.setAdapter(mAdapter);
            mAdapter.setOnItemClickListener(new MyAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(int position) {
                    String listViewString = (String) btPareadList.get(position);
                    String bluetoothAddress=listViewString.substring(listViewString.indexOf("\n") +1 );
                    String bluetoothName=listViewString.substring(0,listViewString.lastIndexOf("\n"));
                    // Make an intent to start next activity while taking an extra which is the MAC address.
                    Intent i = new Intent(BluetoothList.this, MainActivity.class);
                    i.putExtra(BLUETOOTH_ADDRESS,bluetoothAddress );
                    i.putExtra(BLUETOOTH_NAME,bluetoothName );
                    i.putExtra(LAYOUT,BULB );//Layout bulb ou RemoteControl
                    startActivity(i);
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case R.id.action_settings:
//                // User chose the "Settings" item, show the app settings UI...
//
//                return true;
//        }
//    }
}

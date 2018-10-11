package com.example.android.quakenotification;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.support.design.widget.Snackbar;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.UUID;

public class MainActivity extends AppCompatActivity  implements AdapterView.OnItemClickListener {
    private static final String Tag = MainActivity.class.getSimpleName();
    BluetoothAdapter mBluetoothAdapter;
    private MenuItem menuItem;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mDatabaseReference;
    Switch connect;
    private ArrayList<BluetoothDevice> DevicesList;
    private ListView listDeviceView;
    listDevicesAdapter adapter;
    private DrawerLayout mDrawerLayout;
    private ChildEventListener mChildEventListener;

    private BluetoothConnectionService mBluetoothConnectionService;
    private static final UUID MY_UUID_INSECURE =
            UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");
    private BluetoothDevice mBTdevice;

    // Create a BroadcastReceiver for ACTION_FOUND.
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            testBluetoothStatue(context, intent);
        }
    };

    @Override
    protected void onDestroy() {

        super.onDestroy();

        unregisterReceiver(mReceiver);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        createFireBaseDataBase();
        DevicesList = new ArrayList<>();
        connect = findViewById(R.id.connect_switch);
        mDrawerLayout = findViewById(R.id.drawer_layout);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_menu_black_24dp);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem item) {
                        // set item as selected to persist highlight
                        menuItem = item;


                        int id = item.getItemId();
                        switch (id) {
                            case R.id.on_off_button:
                                changeBluetooth();
                                break;
                            case R.id.scan_button:
                                scanDevices();
                                break;


                        }
                        // Add code here to update the UI based on the item selected
                        // For example, swap UI fragments here

                        return true;
                    }
                });
        //Broadcast when bound state change (pairing)
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
       listDeviceView.setOnItemClickListener(this);
        registerReceiver(mReceiver, filter);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        connect.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    startBTConnection(mBTdevice, MY_UUID_INSECURE);
            }
        });
    }

    private void startBTConnection(BluetoothDevice device, UUID uuid) {

        Log.d(Tag, "startBTConnection: Initializing RFCOM Bluetooth Connection.");
        if (device == null) {
            Toast.makeText(this, "Choose device ", Toast.LENGTH_SHORT).show();
            connect.setChecked(false);
        } else {
            mBluetoothConnectionService.startClient(device, uuid);
        }


    }

    private void testBluetoothStatue(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
            final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

            switch (state) {

                case BluetoothAdapter.STATE_TURNING_OFF:
                    Snackbar.make(getWindow().getDecorView().findViewById(android.R.id.content), "Turning off", Snackbar.LENGTH_LONG).show();

                    break;
                case BluetoothAdapter.STATE_TURNING_ON:
                    Snackbar.make(getWindow().getDecorView().findViewById(android.R.id.content), "Turning on ", Snackbar.LENGTH_LONG).show();

                    break;


            }
        } else if (action.equals(BluetoothDevice.ACTION_FOUND)) {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            //if (!DevicesList.contains(device))
            DevicesList.add(device);
            adapter = new listDevicesAdapter(context, R.layout.list_devices, DevicesList);

            // Apply the adapter to the listview
            listDeviceView.setAdapter(adapter);


        }
    }

    private void changeBluetooth() {

        if (menuItem.isChecked()) {

            menuItem.setChecked(false);
            Log.d(Tag, "Disable");
            mBluetoothAdapter.disable();
            IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mReceiver, filter);

        } else {
            menuItem.setChecked(true);
            // to open bluetooth and make it discoverable for 200 sec
            Intent dicoverIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            dicoverIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 200);
            startActivity(dicoverIntent);
            // create intent filter and pass it to Broadcast Reciver to test the bluetooth statue
            IntentFilter dicoverFilter = new IntentFilter(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
            registerReceiver(mReceiver, dicoverFilter);
            scanDevices();
        }

    }

    private void scanDevices() {

        // Cancel discovery because it otherwise slows down the connection.
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
            mBluetoothAdapter.startDiscovery();
            IntentFilter decoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mReceiver, decoverDevicesIntent);
        } else {
            // to check manifest permission
            /* checkBTPermission();*/
            // need permission if api greater than lollipop
            Log.d(Tag, "listDevice");
            mBluetoothAdapter.startDiscovery();
            IntentFilter decoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mReceiver, decoverDevicesIntent);
        }
        Snackbar.make(getWindow().getDecorView().findViewById(android.R.id.content), "Scanning", Snackbar.LENGTH_LONG).show();

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int i, long id) {
        mBluetoothAdapter.cancelDiscovery();
        String name = DevicesList.get(i).getName();
        String address = DevicesList.get(i).getAddress();
        Log.d(Tag, "name " + name + "address" + address);

        // to check version must greater than jelly bean (not important her)
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2) {
            Log.d(Tag, "Trying to pairing with " + name);
            DevicesList.get(i).createBond();
            mBTdevice = DevicesList.get(i);
        }
        mBluetoothConnectionService = new BluetoothConnectionService(this);
    }

    private void createFireBaseDataBase() {
        mDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mDatabase.getReference().child("earthquake");
        mDatabaseReference.push().setValue(new Earhquake(5, "egypt", "high"));
        mChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Earhquake earhquake = (Earhquake) dataSnapshot.getValue(Earhquake.class);
                NotificationUtilites.showNotification(earhquake, MainActivity.this);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        mDatabaseReference.addChildEventListener(mChildEventListener);
    }
}


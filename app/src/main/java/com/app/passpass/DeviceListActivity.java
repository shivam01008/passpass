package com.app.passpass;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;

public class DeviceListActivity extends AppCompatActivity {
    private ListView listPairdDevices, listAvailableDevices;

    private ProgressBar progressScanDevices;
    private ArrayAdapter<String> adapterPairedDevices, adapterAvailableDevices;
    private Context context;
    private BluetoothAdapter bluetoothAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);
        context = this ;

        init();
    }
    private void init(){
        listPairdDevices = findViewById(R.id.list_paired_Devices);
        listAvailableDevices = findViewById(R.id.list_available_Devices);
        progressScanDevices = findViewById(R.id.progress_scan_devices);

        adapterAvailableDevices = new ArrayAdapter<String>(context,R.layout.device_list_item);
        adapterAvailableDevices = new ArrayAdapter<String>(context,R.layout.device_list_item);

        listPairdDevices.setAdapter(adapterPairedDevices);
        listAvailableDevices.setAdapter(adapterAvailableDevices);

        listAvailableDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String info = ((TextView) view).getText().toString();
                String address = info.substring(info.length()-17);

                Intent intent = new Intent();
                intent.putExtra("DeviceAddress",address);
                setResult(RESULT_OK,intent);
                finish();

            }
        });





        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

        if(pairedDevices != null && pairedDevices.size()>0){
            for (BluetoothDevice device : pairedDevices){
                adapterAvailableDevices.add(device.getName() + "\n"+ device.getAddress());
            }
        }
        IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(bluetoothDevicesListener,intentFilter);
        IntentFilter intentFilter1  = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(bluetoothDevicesListener,intentFilter1);


    }
    private BroadcastReceiver bluetoothDevicesListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_NAME);
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    adapterAvailableDevices.add(device.getName() + "\n" + device.getAddress());

                } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                    progressScanDevices.setVisibility(View.GONE);
                    if (adapterAvailableDevices.getCount() == 0) {
                        Toast.makeText(context, "No new devices found", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Click on devices to chat", Toast.LENGTH_SHORT).show();
                    }

                }
            }
        }

    };

        @Override
        public boolean onCreateOptionsMenu(@NonNull Menu menu) {
            getMenuInflater().inflate(R.menu.menu_device_list, menu);
            return super.onCreateOptionsMenu(menu);
        }


        @Override
        public boolean onOptionsItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.menu_scan_devices:
                    scanDevices();
                    return true;

                default:
                    return super.onOptionsItemSelected(item);
            }
        }

        private void scanDevices() {
            progressScanDevices.setVisibility(View.VISIBLE);
            adapterAvailableDevices.clear();
            Toast.makeText(context, "Scan started", Toast.LENGTH_SHORT).show();
            if (bluetoothAdapter.isDiscovering()) {
                bluetoothAdapter.cancelDiscovery();
            }
            bluetoothAdapter.startDiscovery();

        }
    }

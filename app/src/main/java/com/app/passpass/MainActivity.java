package com.app.passpass;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Switch;
import android.widget.Toast;

import com.chatutils;

import org.chromium.base.Callback;
import org.w3c.dom.Text;

import java.util.logging.LogRecord;

public class MainActivity extends AppCompatActivity {
    public static final int MESSAGE_STATE_CHANGED = 0;
    public static final int MESSAGE_TOAST = 4;
    public static final int MESSAGE_DEVICE_NAME = 3;
    private Context context;
    private chatutils ChatUtils;
    private BluetoothAdapter bluetoothAdapter;
    private final int LOCATION_PERMISSION_REQUEST =101;
    private final int SELECTED_DEVICE =102;

    private static final int MESSAGE_READ = 1;
    private static final int MESSAGE_WRITE = 2;
   // private static final int MESSAGE_DEVICE_NAME = 3;
    //private static final int MESSAGE_TOAST = 4;

    public static final String DEVICE_NAME = "deviceName";
    public static final String TOAST = "toast";
    private String connectedDevic;

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message message) {
            switch (message.what){
                case MESSAGE_STATE_CHANGED:
                    switch (message.what){
                        case chatutils.STATE_NONE:
                            setstate("Not Connected");
                            break;
                            case chatutils.STATE_LISTEN:
                                setstate("Not Connected");
                                break;
                                case chatutils.STATE_CONNECTING:
                                    setstate("Connecting...");
                                    break;
                                    case chatutils.STATE_CONNECTED:
                                        setstate("Connected: "+ connectedDevic);
                                        break;
                    }

                    break;
                case MESSAGE_READ:
                    break;
                case  MESSAGE_WRITE:
                    break;
                case  MESSAGE_DEVICE_NAME:
                    connectedDevic = message.getData().getString(DEVICE_NAME);
                    Toast.makeText(context,connectedDevic,Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(context,message.getData().getString(TOAST), Toast.LENGTH_SHORT).show();
                    break;
            }
            return false;
        }
    });
    private void setstate (CharSequence subtitle){
        getSupportActionBar().setSubtitle(subtitle);
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        ChatUtils = new chatutils(context,handler);
        intitBluetooth();
    }
    private void intitBluetooth(){
        bluetoothAdapter =BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter == null){
            Toast.makeText(context, "N0 bluetooth found", Toast.LENGTH_SHORT).show();

        }
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main_activity, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_search_device:
                checkpermission();
                return true;

            case R.id.menu_enable_bluetooth:
                enableBluetooth();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }


    }
    private void checkpermission(){
        if(ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},LOCATION_PERMISSION_REQUEST);
        }else {
            Intent intent = new Intent(context,DeviceListActivity.class);
            startActivityForResult(intent,SELECTED_DEVICE);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == SELECTED_DEVICE && resultCode == RESULT_OK){
            String address = data.getStringExtra("deviceaddress");
           chatutils.connect(bluetoothAdapter.getRemoteDevice(address));
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(context,DeviceListActivity.class);
                startActivityForResult(intent,SELECTED_DEVICE);

            } else {
                new AlertDialog.Builder(context)
                        .setCancelable(false)
                        .setMessage("Location permission is requered.\n Please grant")
                        .setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                checkpermission();
                            }
                        })
                        .setPositiveButton("Deny", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                MainActivity.this.finish();
                            }
                        }).show();
            }


        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void enableBluetooth(){
        if(!bluetoothAdapter.isEnabled()){

            bluetoothAdapter.enable();
        }
        if(bluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE){
            Intent discoveryIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoveryIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,300);
            startActivity(discoveryIntent);
        }


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(chatutils!=null){
            chatutils.stop();
        }
    }
}

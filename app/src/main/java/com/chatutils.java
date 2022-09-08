package com;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.app.passpass.MainActivity;

import java.io.IOException;
import java.net.Socket;
import java.util.UUID;


public class chatutils {
    private Context contex;
    private  final Handler handler;
    private BluetoothAdapter bluetoothAdapter;
    private ConnectTHread connectTHread;
    private AcceptThread acceptThread;
    private  final UUID APP_UUID = UUID.fromString("fa870d0-8a39-0800200c9a66");
    private final String APP_NAME ="PassPass";


    public static final int STATE_NONE = 0;
    public static final int STATE_LISTEN = 1;
    public static final int STATE_CONNECTING = 2;
    public static final int STATE_CONNECTED = 3;

    private  int state;
    public chatutils(Context context,Handler handler) {
        this.contex = context;
        this.handler = handler;

        state = STATE_NONE;
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public int getState() {
        return state;
    }

    public synchronized void setState(int state){
        this.state = state;
        handler.obtainMessage(MainActivity.MESSAGE_STATE_CHANGED, state,-1).sendToTarget();
    }
    private synchronized void start(){
        if(connectTHread != null){
            connectTHread.cancel();
            connectTHread = null;
        }
        if(acceptThread == null){
            acceptThread = new AcceptThread();
            acceptThread.start();
        }
        setState(STATE_LISTEN);

    }
    public synchronized void stop(){
        if(connectTHread != null){
            connectTHread.cancel();
            connectTHread = null;

        }
        if (acceptThread != null){
            acceptThread.cancel();
            acceptThread = null;
        }
        setState(STATE_NONE);

    }
    public void connect(BluetoothDevice device){
        if (state == STATE_CONNECTING){
            connectTHread.cancel();
            connectTHread = null;
        }
        connectTHread = new ConnectTHread(device);
        connectTHread.start();
        setState(STATE_CONNECTING);
    }
    private class AcceptThread<socket> extends Thread{
        private BluetoothServerSocket serverSocket;
        public  AcceptThread(){
            BluetoothServerSocket tmp = null;
            try{
                tmp= bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(APP_NAME,APP_UUID);
            }catch (IOException e){
                Log.e("Accept->Constructor",e.toString());
            }
            serverSocket = tmp;
        }
        public void run() {
            BluetoothAdapter socket = null;
            try {
                socket = serverSocket.accept();
            } catch (IOException e) {
                Log.e("Accept->Run", e.toString());
                try {
                        serverSocket.close();
                    }catch (IOException e1){
                    Log.e("Accept->Close",e.toString());
                }
            }


            if (socket != null) {

                switch (state) {
                    case STATE_LISTEN:
                    case STATE_CONNECTING:
                        connect(socket.getRemoteDevice());
                        break;
                    case STATE_NONE:
                    case STATE_CONNECTED:
                        try {
                            Socket.close();
                        } catch (IOException e) {
                            Log.e("Accept->Close", e.toString());
                        }
                        break;
                }


            }
        }

            public void cancel(){
            try{
                serverSocket.close();
            }catch (IOException e){
                Log.e("Accept->Close",e.toString());
            }
        }
    }


    private  class  ConnectTHread extends Thread {
        private final BluetoothSocket socket;
        private final BluetoothDevice device;

        public ConnectTHread(BluetoothDevice device){
            this.device = device;
            BluetoothDevice tmp = null;
            try {
                tmp = device.createInsecureRfcommSocketToServiceRecord(APP_UUID);
            }catch (IOException e){
                Log.e("Connect->Constructor",e.toString());
            }
            socket = tmp;
        }
        public  void run (){
            try{
                socket.connect();
            }catch (IOException e){
                Log.e("Connect->Run",e.toString());
                try{
                    socket.close();
                }catch (IOException e1){
                    Log.e("Connect->Constructor",e.toString());
                }

                return;;
            }
            synchronized (chatutils.this){
                connectTHread = null;
            }
            connect(device);

        }
        public void cancel(){
            try{
                socket.close();
            }catch (IOException e){
                Log.e("Connect->Cancel",e.toString());
            }
        }
    }
    private  synchronized void connectionFailed(){
        Message message = handler.obtainMessage(MainActivity.MESSAGE_TOAST);
        Bundle bundle= new Bundle();
        bundle.putString(MainActivity.TOAST,"Cant connect to this device");
        message.setData(bundle);
        handler.sendMessage(message);


        chatutils.this.start();
    }
    private synchronized  void  connect (BluetoothDevice device){
        if (connectTHread != null){
            connectTHread.cancel();
            connectTHread = null;
        }
        Message message = handler.obtainMessage(MainActivity.MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(MainActivity.DEVICE_NAME,device.getName());
        message.setData(bundle);
        handler.sendMessage(message);
        setState(STATE_CONNECTED);
    }

}

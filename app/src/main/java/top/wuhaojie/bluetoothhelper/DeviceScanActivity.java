package top.wuhaojie.bluetoothhelper;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import top.wuhaojie.bthelper.Constants;
import top.wuhaojie.bthelper.MessageItem;
import top.wuhaojie.bthelper.OnSendMessageListener;

public class DeviceScanActivity extends AppCompatActivity {
    private static final String TAG = "DeviceScanActivity";
    public static final int REQUEST_ENABLE_BT = 0x006;
    public static final int MESSAGE_READ = 0x010;
    public static final int MESSAGE_WRITE = 0x020;

    //============View====================
    private TextView deviceInfo;
    private TextView bleResponse;

    private List<BluetoothDevice> mDatas = new ArrayList<>(); //已配对设备
    private ListViewAdapter adapter;
    private List<BluetoothDevice> mDatas2 = new ArrayList<>(); //发现的新设备
    private ListViewAdapter adapter2;
    //================View=================



    BluetoothAdapter mBluetoothAdapter;
    private ConnectThread mConnectThread;
    private ReadConnectThread mReadConnectThread;

    // Create a BroadcastReceiver for ACTION_FOUND
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // Add the name and address to an array adapter to show in a ListView
                logdd("新设备设备 : "+device.getName() + "  " + device.getAddress());
                mDatas2.add(device);
                adapter2.notifyDataSetChanged();
            }
        }
    };
    // Register the BroadcastReceiver
    IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);


    //==================Handler==================
    StringBuilder responseBUffer = new StringBuilder();
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    /*for (byte r : readBuf) {
                        responseBUffer.append(r);
                    }
                    logdd(responseBUffer.toString());*/
                    String readMsg = new String(readBuf, 0, msg.arg1);
                    responseBUffer.append(readMsg);
                    responseBUffer.append("\n==============================\n");
                    logdd(responseBUffer.toString());
                    bleResponse.setText(responseBUffer.toString());
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_scan);

        registerReceiver(mReceiver, filter);

        //=======view========
        deviceInfo = (TextView) findViewById(R.id.device_info);
        bleResponse = (TextView) findViewById(R.id.ble_response);
        //-------------------

        //获取蓝牙适配器
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBluetoothAdapter == null) {
            toast("not support bluetooth");
        }

        //开启蓝牙
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            deviceInfo.setText("本机蓝牙 : " + mBluetoothAdapter.getName() +" "+ mBluetoothAdapter.getAddress());
        }


        ListView bleListView = (ListView) findViewById(R.id.list_view);
        adapter = new ListViewAdapter(mDatas, this);
        bleListView.setAdapter(adapter);
        bleListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mConnectThread = new ConnectThread(mDatas.get(position));
                mConnectThread.start();
            }
        });

        ListView bleListView2 = (ListView) findViewById(R.id.list_view2);
        adapter2 = new ListViewAdapter(mDatas2, this);
        bleListView2.setAdapter(adapter2);
        bleListView2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mConnectThread = new ConnectThread(mDatas2.get(position));
                mConnectThread.start();
            }
        });


        findViewById(R.id.btn_search).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDatas.clear();
                Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
                if (pairedDevices.size() > 0) {
                    // Loop through paired devices
                    for (BluetoothDevice device : pairedDevices) {
                        // Add the name and address to an array adapter to show in a ListView
                        logdd("已配对设备 : "+device.getName() + "  " + device.getAddress());
                        mDatas.add(device);
                        adapter.notifyDataSetChanged();
                    }
                }

                if (mBluetoothAdapter.startDiscovery()) {
                    mDatas2.clear();
                    toast("已启动发现设备操作");
                }
            }
        });

        findViewById(R.id.btn_send).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDatas.clear();
                adapter.notifyDataSetChanged();
                mDatas2.clear();
                adapter2.notifyDataSetChanged();
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_ENABLE_BT) {
                deviceInfo.setText("本机蓝牙 : " + mBluetoothAdapter.getName() +" "+  mBluetoothAdapter.getAddress());
            }
        }else {
            toast("已取消");
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }



    public void toast(String msg) {
        Toast.makeText(getBaseContext(), msg, Toast.LENGTH_LONG).show();
    }
    public void logdd(String msg){
        Log.d(TAG, msg);
    }


    /**
     * 客户端
     */
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        private ReadConnectThread readConnectThread;

        public ConnectThread(BluetoothDevice mmDevice) {
            this.mmDevice = mmDevice;
            BluetoothSocket tmp = null;
            try {
                tmp = mmDevice.createRfcommSocketToServiceRecord(UUID.fromString(Constants.STR_UUID));
            } catch (IOException e) {
                e.printStackTrace();
            }
            mmSocket = tmp;
        }

        @Override
        public void run() {
            mBluetoothAdapter.cancelDiscovery();

            try {
                mmSocket.connect();
            } catch (IOException e) {
                // Unable to connect; close the socket and get out
                try {
                    mmSocket.close();
                } catch (IOException closeException) { e.printStackTrace();}
                return;
            }
            manageConnectedSocket(mmSocket);
        }

        private void manageConnectedSocket(BluetoothSocket mmSocket) {
            readConnectThread = new ReadConnectThread(mmSocket);
            readConnectThread.start();
        }

        /** Will cancel the listening socket, and cause the thread to finish */
        public void cancel() {
            try {
                mmSocket.close();
                readConnectThread.cancel();
            } catch (IOException e) { }
        }
    }

    private class ReadConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInputStream;
        private final OutputStream mmOutputStream;

        public ReadConnectThread(BluetoothSocket socket) {
            this.mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            mmInputStream = tmpIn;
            mmOutputStream = tmpOut;
        }

        @Override
        public void run() {
            Log.i(TAG, "BEGIN Read Data Thread 开始读取");
            byte[] buffer = new byte[1024];
            int bytes;

            while (true) {
                try {
                    bytes = mmInputStream.read(buffer);
                    mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
                            .sendToTarget();

                } catch (IOException e) {
                    break;
                }
            }
        }

        /**
         * Write to the connected OutStream.
         *
         * @param buffer The bytes to write
         */
        public void write(byte[] buffer) {
            try {
                mmOutputStream.write(buffer);

                // Share the sent message back to the UI Activity
                mHandler.obtainMessage(MESSAGE_WRITE, -1, -1, buffer)
                        .sendToTarget();
            } catch (IOException e) {
                Log.e(TAG, "Exception during write", e);
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }

    }



    /**
     * 服务端
     */
    private class AcceptThread extends Thread{
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            BluetoothServerSocket tmp = null;
            try {
                tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord("YJ", UUID.fromString(Constants.STR_UUID));
            } catch (IOException e) {
                logdd("连接为服务器 : 服务器套接字获取失败");
            }
            mmServerSocket = tmp;
        }

        @Override
        public void run() {
            BluetoothSocket socket = null;
            while (true) {
                try {
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (socket != null) {
                    manageConnectedSocket(socket);
                    try {
                        mmServerSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }

        private void manageConnectedSocket(BluetoothSocket socket) {

        }
        /** Will cancel the listening socket, and cause the thread to finish */
        public void cancel() {
            try {
                mmServerSocket.close();
            } catch (IOException e) { }
        }

    }
}

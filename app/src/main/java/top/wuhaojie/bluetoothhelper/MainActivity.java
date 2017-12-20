package top.wuhaojie.bluetoothhelper;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import top.wuhaojie.bthelper.BtHelperClient;
import top.wuhaojie.bthelper.Filter;
import top.wuhaojie.bthelper.MessageItem;
import top.wuhaojie.bthelper.OnSearchDeviceListener;
import top.wuhaojie.bthelper.OnSendMessageListener;

import static android.R.attr.data;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity";
    private BtHelperClient btHelperClient;

    private ListViewAdapter adapter;
    private List<BluetoothDevice> mDatas = new ArrayList<>();

    private TextView bleResponse;

    public void toast(String msg){
        Toast.makeText(getBaseContext(), msg, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btHelperClient = BtHelperClient.from(MainActivity.this);

        btHelperClient.requestEnableBt();

        btHelperClient.setFilter(new Filter() {
            @Override
            public boolean isCorrect(String response) {
                return response.trim().length() >= 5;
            }
        });

        bleResponse = (TextView) findViewById(R.id.ble_response);

        ListView bleListView = (ListView) findViewById(R.id.list_view);
        adapter = new ListViewAdapter(mDatas, this);
        bleListView.setAdapter(adapter);
        bleListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MessageItem item = new MessageItem("Hello World");
                BluetoothDevice device = mDatas.get(position);
                btHelperClient.sendMessage(device.getAddress(), item, true, new OnSendMessageListener() {
                    @Override
                    public void onSuccess(int status, String response) {
                        adapter.notifyDataSetChanged();
                        Log.d(TAG, "sendmessage onSuccess status :" + status + " , response :" + response);

                        bleResponse.setText(response);

                        Toast.makeText(MainActivity.this, "收到设备回应信息: " + response, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onConnectionLost(Exception e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onError(Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        });

        findViewById(R.id.btn_search).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivity(new Intent(MainActivity.this, DeviceScanActivity.class));
                /*Toast.makeText(MainActivity.this, "寻找中ing...", Toast.LENGTH_LONG).show();

                btHelperClient.searchDevices(new OnSearchDeviceListener() {
                    @Override
                    public void onStartDiscovery() {
                        Log.d(TAG, "onStartDiscovery()");
                    }

                    @Override
                    public void onNewDeviceFound(BluetoothDevice device) {
                        Log.d(TAG, "new device: " + device.getName() + " " + device.getAddress());
                    }

                    @Override
                    public void onSearchCompleted(List<BluetoothDevice> bondedList, List<BluetoothDevice> newList) {
                        Log.d(TAG, "SearchCompleted: bondedList" + bondedList.toString());
                        Log.d(TAG, "SearchCompleted: newList" + newList.toString());
                        mDatas.clear();
                        mDatas.addAll(bondedList);
                        mDatas.addAll(newList);
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onError(Exception e) {
                        e.printStackTrace();
                    }
                });*/


            }
        });


        findViewById(R.id.btn_send).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btHelperClient.close();
            }
        });




    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //btHelperClient.close();
    }

    public static BigDecimal div(String v1, String v2, int scale) {
        if (scale < 0) {
            throw new IllegalArgumentException(
                    "The   scale   must   be   a   positive   integer   or   zero");
        }
        BigDecimal b1 = new BigDecimal(v1);
        BigDecimal b2 = new BigDecimal(v2);
        return b1.divide(b2, scale, BigDecimal.ROUND_HALF_UP);
    }
    public static final String bytesToHexString(byte[] bArray) {
        StringBuffer sb = new StringBuffer(bArray.length);
        String sTemp;
        for (int i = 0; i < bArray.length; i++) {
            sTemp = Integer.toHexString(0xFF & bArray[i]);
            if (sTemp.length() < 2)
                sb.append(0);
            sb.append(sTemp.toUpperCase());
        }
        return sb.toString();
    }

}

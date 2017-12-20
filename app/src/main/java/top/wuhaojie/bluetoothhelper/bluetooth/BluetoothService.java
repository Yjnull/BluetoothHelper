package top.wuhaojie.bluetoothhelper.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import java.util.UUID;

/**
 * Created by yangy on 2017/12/20.
 */

public class BluetoothService {
    private static final String TAG = "BluetoothService";

    //服务名称 : 当创建 server socket 时, 用于写入SDP(新服务发现协议)数据库条目
    private static final String NAME_SECURE = "RTBluetoothSecure";
    private static final String NAME_INSECURE = "RTBluetoothInsecure";

    //UUID
    private static final UUID MY_UUID_SECURE =
            UUID.fromString("28802d51-58de-4d62-a7c9-3d7ce68ba7a3");
    private static final UUID MY_UUID_INSECURE =
            UUID.fromString("572199bd-0eb1-46d0-abeb-3490a8bac286");

    //成员变量
    private final BluetoothAdapter mAdapter;
    private final Handler mHandler;
    private AcceptThread mSecureAcceptThread;
    private AcceptThread mInsecureAcceptThread;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private int mState;
    private int mNewState;

    //常量 指出当前连接状态
    public static final int STATE_NONE = 0;       //nothing
    public static final int STATE_LISTEN = 1;     //监听incoming connections
    public static final int STATE_CONNECTING = 2; //开始outgoing connection
    public static final int STATE_CONNECTED = 3;  //已经连接到一个远程设备


    /**
     * Constructor
     *
     * @param context The UI Activity Context
     * @param handler A Handler to send messages back to UI Activity
     */
    public BluetoothService(Context context, Handler handler) {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = STATE_NONE;
        mNewState = mState;
        mHandler = handler;
    }

    /**
     * 当前连接状态
     */
    public synchronized int getState() {
        return mState;
    }

    /**
     * 根据当前连接状态更新UI title
     */
    private synchronized void updateUserInterfaceTitle() {
        mState = getState();
        log("更新用户状态" + mNewState + " -> " + mState);
    }





    /**
     * 服务端
     */
    private class AcceptThread extends Thread {

    }

    /**
     * 客户端
     */
    private class ConnectThread extends Thread {

    }

    /**
     * 当连接上远程设备时, 处理所有的I/O传输
     */
    private class ConnectedThread extends Thread {

    }

    private void log(String msg){
        Log.d(TAG, msg);
    }
}

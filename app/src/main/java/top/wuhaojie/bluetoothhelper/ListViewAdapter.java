package top.wuhaojie.bluetoothhelper;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yangy on 2017/12/15.
 */

public class ListViewAdapter extends BaseAdapter {
    private List<BluetoothDevice> mDatas = new ArrayList<>();
    private LayoutInflater inflater;

    public ListViewAdapter(List<BluetoothDevice> mDatas, Context mContext) {
        this.mDatas = mDatas;
        this.inflater = LayoutInflater.from(mContext);
    }

    @Override
    public int getCount() {
        return mDatas.size();
    }

    @Override
    public Object getItem(int position) {
        return mDatas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        BLEViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_listview, parent, false);
            holder = new BLEViewHolder();
            holder.bleName = (TextView) convertView.findViewById(R.id.ble_name);
            holder.bleMac = (TextView) convertView.findViewById(R.id.ble_mac);
            holder.bleState = (TextView) convertView.findViewById(R.id.ble_state);
            convertView.setTag(holder);
        }else {
            holder = (BLEViewHolder) convertView.getTag();
        }

        BluetoothDevice device = mDatas.get(position);
        holder.bleName.setText(device.getName());
        holder.bleMac.setText(device.getAddress());
        holder.bleState.setText(String.valueOf(device.getBondState()));

        return convertView;
    }


    private static class BLEViewHolder{
        TextView bleName;
        TextView bleMac;
        TextView bleState;
    }
}

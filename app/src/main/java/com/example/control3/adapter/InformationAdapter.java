package com.example.control3.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.control3.MainActivity;
import com.example.control3.OperateActivity_;
import com.example.control3.R;

import java.util.List;

/**
 * Created by Saber on 2018/3/19.
 */

public class InformationAdapter extends RecyclerView.Adapter<InformationAdapter.ViewHolder>{
    private Context mContext;
    private List<BluetoothInformation> mBluetoothInformationList;
    private static final String TAG = "InformationAdapter";

    static class ViewHolder extends RecyclerView.ViewHolder
    {
        CardView cardView;
        TextView name;
        TextView address;

        public ViewHolder(View view)
        {
            super(view);
            cardView=(CardView)view;
            name=(TextView)view.findViewById(R.id.bluetooth_name);
            address=(TextView)view.findViewById(R.id.bluetooth_address);
        }
    }

    public InformationAdapter(List<BluetoothInformation> bluetoothInformationList)
    {
        mBluetoothInformationList=bluetoothInformationList;
    }

    //这里处理点击事件
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(mContext==null)
        {
            mContext=parent.getContext();
        }
        View view= LayoutInflater.from(mContext).inflate(R.layout.bluetooth_information,parent,false);

        //点击监听器
        final ViewHolder holder=new ViewHolder(view);
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position=holder.getAdapterPosition();
                if(position!=-1)
                {
                    BluetoothInformation bluetoothInformation=mBluetoothInformationList.get(position);
                    //这里获得了具体的类，内含名字地址
                    Intent intent=new Intent(mContext,OperateActivity_.class);
                    //记得改
                    intent.putExtra(MainActivity.BLUETOOTH_ADDRESS,bluetoothInformation.getAddress());
                    mContext.startActivity(intent);
                }
                Log.e(TAG, "onClick: "+position );
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        BluetoothInformation bluetoothInformation=mBluetoothInformationList.get(position);
        holder.name.setText(bluetoothInformation.getName());
        holder.address.setText(bluetoothInformation.getAddress());
    }

    @Override
    public int getItemCount() {
        return mBluetoothInformationList.size();
    }

    public void clear()
    {
        if(mBluetoothInformationList!=null)
        {
            mBluetoothInformationList.clear();
        }
    }


}

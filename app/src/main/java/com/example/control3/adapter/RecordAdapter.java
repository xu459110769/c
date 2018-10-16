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
import com.example.control3.pojo.RecordItem;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by Saber on 2018/3/19.
 */

public class RecordAdapter extends RecyclerView.Adapter<RecordAdapter.ViewHolder>{
    private Context mContext;
    private List<RecordItem> mRecordItemList;
    private static final String TAG = "RecordAdapter";

    static class ViewHolder extends RecyclerView.ViewHolder
    {
        CardView cardView;
        //TextView first_line;
        TextView second_line;

        public ViewHolder(View view)
        {
            super(view);
            cardView=(CardView)view;
            //first_line=view.findViewById(R.id.first_line);
            second_line=view.findViewById(R.id.second_line);
        }
    }

    public RecordAdapter(List<RecordItem> recordItemList)
    {
        mRecordItemList=recordItemList;
    }

    //这里处理点击事件
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(mContext==null)
        {
            mContext=parent.getContext();
        }
        View view= LayoutInflater.from(mContext).inflate(R.layout.record_information,parent,false);

        //点击监听器
        final ViewHolder holder=new ViewHolder(view);
//        holder.cardView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                int position=holder.getAdapterPosition();
//                if(position!=-1)
//                {
//                    RecordItem RecordItem=mRecordItemList.get(position);
//                    //这里获得了具体的类，内含名字地址
//                    Intent intent=new Intent(mContext,OperateActivity_.class);
//                    //记得改
//                    intent.putExtra(MainActivity.BLUETOOTH_ADDRESS,RecordItem.getAddress());
//                    mContext.startActivity(intent);
//                }
//                Log.e(TAG, "onClick: "+position );
//            }
//        });
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        RecordItem recordItem=mRecordItemList.get(position);
        if(recordItem.getrMode().equals("第一行"))
        {
            holder.second_line.setText("      日期          时长   强度                 模式");
        }else {
            String dateString = new SimpleDateFormat("yyyy-MM-dd").format(recordItem.getrStartTime());
            holder.second_line.setText((dateString+"   "+(recordItem.getrLastTime()<10?"0":"")+recordItem.getrLastTime()+"min   "+recordItem.getrStrong()+"   "+recordItem.getrMode()));
        }
        //holder.first_line.setText(("记录号:"+recordItem.getrCode()+" 设备号:"+recordItem.getiCode()));

    }

    @Override
    public int getItemCount() {
        return mRecordItemList.size();
    }

    public void clear()
    {
        if(mRecordItemList!=null)
        {
            mRecordItemList.clear();
        }
    }


}

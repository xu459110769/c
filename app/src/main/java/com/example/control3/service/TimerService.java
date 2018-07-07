package com.example.control3.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.example.control3.OperateActivity;
import com.example.control3.R;

public class TimerService extends Service {

    private static final String TAG = "TimerService";

    public TimerService() {
    }

    private TimerBinder mBinder=new TimerBinder();

    @Override
    public IBinder onBind(Intent intent) {
        Log.e(TAG, "onBind: ");
        return mBinder;
    }

    public class TimerBinder extends Binder
    {
        public void StartTimer(int minute,int second)
        {
            startForeground(1,getNotification("剩余时间：",minute,second));
        }

        public void UpdateTimer(int minute,int second)
        {
            getNotificationManager().notify(1,getNotification("剩余时间：",minute,second));
        }

        public void EndTimer()
        {
            stopForeground(true);
            getNotificationManager().notify(1,getNotification("完成！",0,0));
        }

        public void CancelTimer()
        {
            getNotificationManager().cancel(1);
            stopForeground(true);
        }
    }

    private NotificationManager getNotificationManager()
    {
        return (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
    }

    private Notification getNotification(String title,int minute,int second)
    {
        Intent intent=new Intent(this,OperateActivity.class);
        PendingIntent pi=PendingIntent.getActivity(this,0,intent,0);
        NotificationCompat.Builder builder=new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher));
        builder.setContentIntent(pi);
        builder.setContentTitle(title);
        String minuteString;
        String secondString;
        if(minute<10)
        {
            minuteString="0"+minute;
        }
        else
        {
            minuteString=""+minute;
        }
        if(second<10)
        {
            secondString="0"+second;
        }
        else
        {
            secondString=""+second;
        }
        builder.setContentText(minuteString+":"+secondString);
        return builder.build();
    }
}

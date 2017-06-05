package com.example.nachito.spear;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Handler;
import android.os.Message;

/**
 * Created by ines on 6/2/17.
 */

public class TimerWifi {
    long millisecs;
    Runnable runnable;
    boolean running = false;

    Handler handle = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            if(running)
            {
                post(runnable);
                sendMessageDelayed(new Message(), millisecs);
            }
        }
    };

    public TimerWifi(Runnable runnable, long delay)
    {
        this.runnable = runnable;
        millisecs = delay;
    }

    @TargetApi(Build.VERSION_CODES.N)
    public void start()
    {
        running = true;
        handle.sendMessageDelayed(new Message(), 0); // Start immediately
         }
    public void stop()
    {
        running = false;
    }
    public void setDelay(long delay)
    {
        millisecs = delay;
    }
}

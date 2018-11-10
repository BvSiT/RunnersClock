package com.bvsit.runnersclock;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.Vibrator;
import android.util.Log;

import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

public class VibrationTimerService extends Service {
    final Messenger myMessenger = new Messenger(new IncomingHandler());
    private final static String TAG = "VibrationTimerService";
    private Long mSecInterval;
    private Vibrator mVibrator;
    private Timer mTimer;
    private boolean mTimerStarted;
    private boolean debug_min_as_sec=false;

    PowerManager pm;
    PowerManager.WakeLock wl;

    public VibrationTimerService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return myMessenger.getBinder();
    }

    public class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg){
            Bundle data = msg.getData();
            /* debug
            for (String key : data.keySet())
            {
                Log.i("Bundle Debug", key + " = \"" + data.get(key) + "\"");
            }
            */
            if (data.getBoolean("bRun")){
                Log.i(TAG,"bRun="+data.getBoolean("bRun"));
                mSecInterval=data.getLong("lngSecInterval");
                startTimer();
            }
            else {
                stopTimer();
            }
        }
    }

    public void startTimer(){
        if (mSecInterval<=0) return;
        if (mTimerStarted) return;//??

        //Without wakelock vibration will not continue during doze
        //even if allowed to ignore Battery Optimizations
        pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"VibrationTimerService:newWakeLock");
        wl.acquire();  //TODO: timeout?

        mTimer = new Timer();
        mVibrator=(Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        /*
        When debugging shorten the interval between vibrations.
        In the main activity one sets the vibration interval in minutes (mIntervalVibrationMinutes).
        The interval is passed from the activity into lngSecInterval in seconds.
        When debugging by dividing by 60 an interval in minutes is translated into seconds, i.e.
        5 minutes = 5 seconds.
         */
        if (debug_min_as_sec) mSecInterval/=60;

        final long startTime= SystemClock.elapsedRealtime();
        mTimer.scheduleAtFixedRate( new TimerTask(){
            public void run(){
                //Log.i(TAG,"mSecInterval:"+mSecInterval);
                long elapsedMSecs= SystemClock.elapsedRealtime() - startTime;
                long elapsedSecs=elapsedMSecs/1000; // get seconds from elapsedMSecs
                long elapsedMin=elapsedSecs/60;
                //Log.i(TAG,"elapsedMSecs:"+elapsedMSecs);
                long[] pattern;
                if( elapsedSecs % mSecInterval == 0 )
                {
                    //When debugging create vibration pattern as if seconds are minutes
                    if (debug_min_as_sec) elapsedMin=elapsedSecs;
                    Log.i(TAG,"elapsedMin:"+elapsedMin);
                    Log.i(TAG,"mSecInterval:"+mSecInterval);
                    pattern = Helper.vibrationPattern(elapsedMin,150,75,true);
                    Log.i(TAG,"pattern:"+Arrays.toString(pattern));
                    if (elapsedMin>0) mVibrator.vibrate(pattern, -1); //-1 means: Don't repeat
                }
            }
        }, 0, 1000 * mSecInterval);
        mTimerStarted=true;
    }

    public void stopTimer(){
        if (mTimerStarted){
            mTimer.cancel(); //TODO use try catch?
            mVibrator.cancel();
            mTimerStarted=false;
            try {
                wl.release();
            }
            catch (Exception e) {
                Log.e(getClass().getSimpleName(),
                        "Exception when releasing wakelock", e);
            }
        }
    }
}

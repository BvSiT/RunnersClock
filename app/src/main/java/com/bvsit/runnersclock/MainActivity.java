/*
TODO
- one button Start Stop Reset
- Color theme
- Icon

 */






package com.bvsit.runnersclock;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {
    Messenger myService = null;
    public boolean isBound;
    private String TAG = "Runnersclock";
    private long mIntervalVibrationMinutes = 5;
    private long mChronoBase = 0; //Start time chronometer in msecs. Used to save state chronometer
    Chronometer mChronometer;

    public VibrationTimerService myVibrationTimerService;

    private ServiceConnection myConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            myService = new Messenger(service);
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            myService = null;
            isBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        askDisableBatteryOptimizations();

        //Note: one can first use startService and then bindService. In this case the service will continue even
        // if the activity is not bound any more.
        bindService(new Intent(this, VibrationTimerService.class), myConnection, Context.BIND_AUTO_CREATE);
        final EditText etInterval = findViewById(R.id.editTextInterval);
        etInterval.setText(String.valueOf(mIntervalVibrationMinutes));
        mChronometer = findViewById(R.id.myChronometer);

        Button btnStart = findViewById(R.id.buttonStartChronometer);
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                {
                    if (mChronoBase == 0) { //Only if chronometer is not started
                        mIntervalVibrationMinutes = Long.parseLong(etInterval.getText().toString());
                        Message msg = Message.obtain();
                        Bundle bundle = new Bundle();
                        bundle.putLong("lngSecInterval", mIntervalVibrationMinutes * 60);
                        bundle.putBoolean("bRun", true);
                        msg.setData(bundle);
                        mChronometer.setBase(SystemClock.elapsedRealtime());
                        mChronoBase = mChronometer.getBase();
                        mChronometer.setBase(mChronoBase);
                        mChronometer.start();
                        try {
                            myService.send(msg);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });

        Button btnStop = findViewById(R.id.buttonStopChronometer);
        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mChronometer.stop();
                mChronoBase = 0;
                Message msg = Message.obtain();
                Bundle bundle = new Bundle();
                bundle.putBoolean("bRun", false);
                msg.setData(bundle);
                try {
                    myService.send(msg);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });

        Button btnReset = findViewById(R.id.buttonResetChronometer);
        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mChronometer.stop();
                mChronometer.setBase(SystemClock.elapsedRealtime());  //show 00:00
                mChronoBase = 0;
                Message msg = Message.obtain();
                Bundle bundle = new Bundle();
                bundle.putBoolean("bRun", false);
                msg.setData(bundle);
                try {
                    myService.send(msg);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mChronoBase != 0) {
            outState.putLong("chrono_base", mChronoBase);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState.getLong("chrono_base") > 0) {
            mChronoBase = savedInstanceState.getLong("chrono_base");
            mChronometer.setBase(mChronoBase);
            mChronometer.start();
        }
    }

    void askDisableBatteryOptimizations(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            final Context context = this.getApplicationContext();
            final String packageName = context.getPackageName();
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);

            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                /* Explain to the user:
                "You need to disable Battery Optimizations for this app to ensure
                that vibration will continue in Doze mode.
                Also in Settings > Battery > Launch change Manage automatically to Manage manually"
                */
                AlertDialog.Builder dialog = new AlertDialog.Builder(this);
                dialog.setMessage(R.string.dialog_message_battery_optimizations);
                dialog.setTitle(R.string.dialog_title_battery_optimizations);
                dialog.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Show dialog to allow disable Battery Optimizations
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                        intent.setData(Uri.parse("package:" + packageName));
                        context.startActivity(intent);
                        dialog.dismiss();
                    }
                });
                dialog.show();
            }
        }

        /*  TODO  How to take the user to Settings > Battery > Launch after
            the user has given permission to allow to ignore battery optimizations
            for this app?
            Also there is no way to ensure the user has set Launch to manual?
            There seems not to exist a unified way to show Settings > Battery > Launch
            For Huawei:
                Intent intent = new Intent();
                intent.setClassName("com.huawei.systemmanager", "com.huawei.systemmanager.appcontrol.activity.StartupAppControlActivity");
                startActivity(intent);
            See also https://stackoverflow.com/questions/48166206/how-to-start-power-manager-of-all-android-manufactures-to-enable-background-and
        */
    }
}
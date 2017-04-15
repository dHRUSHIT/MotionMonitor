package com.example.dhrushit.motionmonitor;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.icu.text.SimpleDateFormat;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;


/**
 * Created by dHRUSHIT on 4/15/2017.
 */

@RequiresApi(api = Build.VERSION_CODES.N)
public class MotionMonitorService extends Service implements SensorEventListener {

    private static final String TAG = "MotionMonitorService";
    private SensorManager mSensorManager;
    private Sensor mAccelerator;
    private Sensor mGyroscope;

    String root = Environment.getExternalStorageDirectory().toString();
    String gyroFilename = "gyrodata.txt";
    String acclFilename = "accldata.txt";

    boolean newFileBool = false;

    File folder = new File(root);
    File gyroFile;
    File acclFile;

    FileWriter gyroWriter,acclWriter;

    Calendar calendar;
    Date curTime;
    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v(TAG,"onStartCommand");
//        checkAndGetPermission();
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccelerator = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mSensorManager.registerListener((SensorEventListener) this,mGyroscope,SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener((SensorEventListener) this,mAccelerator,SensorManager.SENSOR_DELAY_NORMAL);
        getFileInstances();
        getWriterInstances(newFileBool);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(this, "Service Destroyed", Toast.LENGTH_SHORT).show();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.v(TAG,"onCreate");

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        String s;
        s = sdf.format(new Date()) + "\t" + Float.toString(event.values[0]) + "\t" + Float.toString(event.values[1]) + "\t" + Float.toString(event.values[2]) + "\n";
        int type = event.sensor.getType();
        switch (type){
            case Sensor.TYPE_GYROSCOPE:
                writeToGyroLog(s);

                break;
            case Sensor.TYPE_ACCELEROMETER:
                writeToAcclLog(s);

                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void getFileInstances() {
        if(!folder.exists()){
            folder.mkdirs();
        }

        gyroFile = new File(folder,gyroFilename);
        acclFile = new File(folder,acclFilename);

        if(!gyroFile.exists()){
            try {
                gyroFile.createNewFile();
                newFileBool = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(!acclFile.exists()){
            try {
                acclFile.createNewFile();
                newFileBool = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private void addDeviceInfo() {
        String deviceInfo = "Device Information\n------------------\n";
        deviceInfo += "BOARD : " + Build.BOARD + "\nBRAND : " + Build.BRAND
                + "\nDEVICE : " + Build.DEVICE + "\nHARDWARE : " + Build.HARDWARE
                + "\nMANUFACTURER : " + Build.MANUFACTURER + "\nMODEL : " + Build.MODEL
                + "\nPRODUCT : " + Build.PRODUCT + "\nTYPE : " + Build.TYPE;
        try {
            gyroWriter.append(deviceInfo);
            acclWriter.append(deviceInfo);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void getWriterInstances(boolean isFileNew) {

        try {
            if(gyroWriter == null || acclWriter == null){
                gyroWriter = new FileWriter(gyroFile.getAbsolutePath(),true);
                acclWriter = new FileWriter(acclFile.getAbsolutePath(),true);
            }

            if(gyroWriter != null && acclWriter != null){
                Toast.makeText(this, "logging now", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(this, "could not get FileWriter", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(isFileNew){
            newFileBool = false;
            addDeviceInfo();
        }
    }

    public void writeToGyroLog(String s){
        try {
            if(gyroWriter != null){
                gyroWriter.append(s);
                gyroWriter.flush();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeToAcclLog(String s){
        try {
            if(acclWriter != null){
                acclWriter.append(s);
                acclWriter.flush();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

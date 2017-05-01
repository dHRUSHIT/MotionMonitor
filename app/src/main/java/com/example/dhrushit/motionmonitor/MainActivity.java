package com.example.dhrushit.motionmonitor;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

@TargetApi(Build.VERSION_CODES.N)
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Intent i;
    ImageButton walking;
    ImageButton running;
    ImageButton climbUp;
    ImageButton climbDown;
    boolean walking_state,running_state,climbUp_state,climbDown_state;

    String root = Environment.getExternalStorageDirectory().toString();
    String gyroFilename = "gyrodata.txt";
    String acclFilename = "accldata.txt";

    boolean newFileBool = true;

    File folder = new File(root);
    File gyroFile;
    File acclFile;

    FileWriter gyroWriter,acclWriter;

    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 4337;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        walking = (ImageButton) findViewById(R.id.btn_walking);
        running = (ImageButton) findViewById(R.id.btn_running);
        climbUp = (ImageButton) findViewById(R.id.btn_climb_up);
        climbDown = (ImageButton) findViewById(R.id.btn_climb_down);

        walking.setOnClickListener(this);
        running.setOnClickListener(this);
        climbUp.setOnClickListener(this);
        climbDown.setOnClickListener(this);

        walking_state = false;
        running_state = false;
        climbUp_state = false;
        climbDown_state = false;

        i = new Intent(this,MotionMonitorService.class);
        checkAndGetPermission();

//        moveTaskToBack(true);
    }

    private void checkAndGetPermission() {
        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permissionCheck
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);

        }else{
            getFileInstances();
            getWriterInstances(newFileBool);
            startService(i);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getFileInstances();
            getWriterInstances(newFileBool);
            startService(i);
         }
        return;
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

    private void getWriterInstances(boolean isFileNew) {

        try {
            if(gyroWriter == null || acclWriter == null){
                gyroWriter = new FileWriter(gyroFile.getAbsolutePath(),true);
                acclWriter = new FileWriter(acclFile.getAbsolutePath(),true);
            }

            if(gyroWriter != null && acclWriter != null){
                Toast.makeText(this, "Main activity logging now", Toast.LENGTH_SHORT).show();
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


    @Override
    public void onClick(View v) {
//        Toast.makeText(this,"click",Toast.LENGTH_SHORT).show();
        int id = v.getId();
        String s = "-----------------------------------------";

        switch (id){
            case R.id.btn_walking:
                s += "walking ";
                if(walking_state == true){
                    ((ImageButton)v).setImageResource(R.drawable.walk_off);
                    s+="stop";
                    walking_state = false;
                }else{
                    ((ImageButton)v).setImageResource(R.drawable.walk_on);
                    s += "start";
                    walking_state = true;
                }
                break;
            case R.id.btn_running:
                s += "running ";
                if(running_state == true){
                    ((ImageButton)v).setImageResource(R.drawable.run_off);
                    s+="stop";
                    running_state = false;
                }else{
                    ((ImageButton)v).setImageResource(R.drawable.run_on);
                    s += "start";
                    running_state = true;
                }
                break;
            case R.id.btn_climb_up:
                s += "climbing up ";
                if(climbUp_state == true){
                    ((ImageButton)v).setImageResource(R.drawable.climb_up_off);
                    s+="stop";
                    climbUp_state = false;
                }else{
                    ((ImageButton)v).setImageResource(R.drawable.climb_up_on);
                    s += "start";
                    climbUp_state = true;
                }
                break;
            case R.id.btn_climb_down:
                s += "climbing down ";
                if(climbDown_state == true){
                    ((ImageButton)v).setImageResource(R.drawable.climb_down_off);
                    s+="stop";
                    climbDown_state = false;
                }else{
                    ((ImageButton)v).setImageResource(R.drawable.climb_down_on);
                    s += "start";
                    climbDown_state = true;
                }
                break;
        }

        s += "-----------------------------------------";
        writeToAcclLog(s);
        writeToGyroLog(s);
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

    private void addDeviceInfo() {
        String deviceInfo = "Device Information\n------------------\n";
        deviceInfo += "BOARD : " + Build.BOARD + "\nBRAND : " + Build.BRAND
                + "\nDEVICE : " + Build.DEVICE + "\nHARDWARE : " + Build.HARDWARE
                + "\nMANUFACTURER : " + Build.MANUFACTURER + "\nMODEL : " + Build.MODEL
                + "\nPRODUCT : " + Build.PRODUCT + "\nTYPE : " + Build.TYPE;
        writeToAcclLog(deviceInfo);
        writeToGyroLog(deviceInfo);

    }
}

package com.dailyutility.torch;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class LightActivity extends AppCompatActivity {

    private Button torchSwitch;
    private ImageView toggle;
    private View rootLayout;
    private static final int CAMERA_PERMISSION_CODE = 1;
    private Camera camera;
    private CameraManager cameraManager;
    private String cameraId;
    private boolean isFlashOn;
    Camera.Parameters params;
    MediaPlayer mp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_light);
        torchSwitch = findViewById(R.id.torch_switch);
        toggle = findViewById(R.id.off);
        rootLayout = findViewById(R.id.root_layout);

        init();
        toggle.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                if(torchSwitch.getText().toString().equalsIgnoreCase(getString(R.string.torch_on))){
                    turnOnFlashLight();
                }
                else{
                    turnOffFlashLight();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(isFlashOn){
            turnOnFlashLight();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(camera != null){
            camera.release();
            camera = null;
        }
    }

    private void turnOffFlashLight() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                cameraManager.setTorchMode(cameraId, false);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
        else{
            setUpCamera();
            params = camera.getParameters();
            params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            camera.setParameters(params);
            camera.stopPreview();
        }
        toggle.setImageResource(R.drawable.on);
        torchSwitch.setBackgroundColor(getResources().getColor(R.color.colorWhite));
        torchSwitch.setTextColor(getResources().getColor(R.color.colorRed));
        torchSwitch.setText(R.string.torch_on);
        rootLayout.setBackgroundColor(getResources().getColor(R.color.colorBlack));
        isFlashOn = false;
        playSound();
    }

    private void setUpCamera() {
        if (camera == null) {
            try {
                camera = Camera.open();
                params = camera.getParameters();
            } catch (RuntimeException e) {
                Log.e("Camera Error! ", e.getMessage());
            }
        }
    }

    private void turnOnFlashLight() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                cameraManager.setTorchMode(cameraId, true);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
        else{
            setUpCamera();
            params = camera.getParameters();
            params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            camera.setParameters(params);
            camera.startPreview();
        }
        toggle.setImageResource(R.drawable.off);
        torchSwitch.setBackgroundColor(getResources().getColor(R.color.colorBlack));
        torchSwitch.setTextColor(getResources().getColor(R.color.colorWhite));
        torchSwitch.setText(R.string.torch_off);
        rootLayout.setBackgroundColor(getResources().getColor(R.color.colorWhite));
        isFlashOn = true;
        playSound();
    }

    private void playSound() {
        if(isFlashOn){
            mp = MediaPlayer.create(LightActivity.this, R.raw.light_switch_off);
        }else{
            mp = MediaPlayer.create(LightActivity.this, R.raw.light_switch_on);
        }
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.release();
            }
        });
        mp.start();
    }

    private void init() {
        boolean hasFlash = getApplicationContext().getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
        if(hasFlash){

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[] {Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
                }
            }

            cameraManager = (CameraManager)getSystemService(Context.CAMERA_SERVICE);
            try{
                if(cameraManager!=null) {
                    cameraId = cameraManager.getCameraIdList()[0];
                }
            }catch (CameraAccessException e){
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == CAMERA_PERMISSION_CODE){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                //do nothing
            }
            else{
                new AlertDialog.Builder(this).setMessage("Grant me the permission to show you the light!")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        requestPermissions(new String[] {Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
                    }
                });
            }
        }
    }
}

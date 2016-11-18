package com.qsm365.qrcodetest;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.google.zxing.Result;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

/**
 * Created by QSM on 2016/11/15.
 */

public class ScanActivity extends Activity implements ZXingScannerView.ResultHandler{

    private ZXingScannerView mScannerView;
    private String TAG = "MainActivity";

    public static final int PERMISSION_REQUEST_CAMERA = 1;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mScannerView = new ZXingScannerView(this);   // Programmatically initialize the scanner view
        setContentView(mScannerView);                // Set the scanner view as the content view

        if (!haveCameraPermission())
            requestPermissions(new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CAMERA);
    }

    private boolean haveCameraPermission()
    {
        if (Build.VERSION.SDK_INT < 23)
            return true;
        return checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
    {
        // This is because the dialog was cancelled when we recreated the activity.
        if (permissions.length == 0 || grantResults.length == 0)
            return;

        switch (requestCode)
        {
            case PERMISSION_REQUEST_CAMERA:
            {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    startCamera();
                }
                else
                {
                    finish();
                }
            }
            break;
        }
    }

    public void startCamera()
    {
        mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
        mScannerView.startCamera();          // Start camera on resume
    }

    public void stopCamera()
    {
        mScannerView.stopCamera();
    }


    @Override
    public void onResume() {
        super.onResume();
        startCamera();
        mScannerView.setAutoFocus(true);
        mScannerView.setKeepScreenOn(true);
    }

    @Override
    public void onPause() {
        super.onPause();
        stopCamera();
    }

    @Override
    public void handleResult(Result rawResult) {
        // Do something with the result here
        Log.e(TAG, rawResult.getText()); // Prints scan results
        Log.e(TAG, rawResult.getBarcodeFormat().toString()); // Prints the scan format (qrcode, pdf417 etc.)
        if(rawResult.getBarcodeFormat().toString().equals("QR_CODE")){
            Intent mIntent = new Intent();
            mIntent.putExtra("result", rawResult.getText());
            this.setResult(1, mIntent);
            this.finish();
        }else{
            // If you would like to resume scanning, call this method below:
            mScannerView.resumeCameraPreview(this);
        }
    }

}

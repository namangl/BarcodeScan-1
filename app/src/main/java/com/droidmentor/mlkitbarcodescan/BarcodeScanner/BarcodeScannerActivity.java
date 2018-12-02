package com.droidmentor.mlkitbarcodescan.BarcodeScanner;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.droidmentor.mlkitbarcodescan.BarCodeScannerUtil.BarcodeScanningProcessor;
import com.droidmentor.mlkitbarcodescan.BarCodeScannerUtil.BarcodeScanningProcessor.BarcodeResultListener;
import com.droidmentor.mlkitbarcodescan.BarCodeScannerUtil.OverlayView;
import com.droidmentor.mlkitbarcodescan.BarCodeScannerUtil.common.CameraSource;
import com.droidmentor.mlkitbarcodescan.BarCodeScannerUtil.common.CameraSourcePreview;
import com.droidmentor.mlkitbarcodescan.BarCodeScannerUtil.common.FrameMetadata;
import com.droidmentor.mlkitbarcodescan.BarCodeScannerUtil.common.GraphicOverlay;
import com.droidmentor.mlkitbarcodescan.LocalData.ContactDetail;
import com.droidmentor.mlkitbarcodescan.LocalData.DBHandler;
import com.droidmentor.mlkitbarcodescan.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions;

import java.io.IOException;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.droidmentor.mlkitbarcodescan.Util.BarcodeScanner.Constants.KEY_CAMERA_PERMISSION_GRANTED;
import static com.droidmentor.mlkitbarcodescan.Util.BarcodeScanner.Constants.PERMISSION_REQUEST_CAMERA;

public class BarcodeScannerActivity extends AppCompatActivity {

    String TAG = "BarcodeScannerActivity";

    @BindView(R.id.barcodeOverlay)
    GraphicOverlay barcodeOverlay;
    @BindView(R.id.preview)
    CameraSourcePreview preview;
    @BindView(R.id.overlayView)
    OverlayView overlayView;

    private static final String EXTRA_BARCODE_FORMAT = "EXTRA_BARCODE_FORMAT";
    public final static String RETURN_BARCODE = "RETURN_BARCODE";

    BarcodeScanningProcessor barcodeScanningProcessor;

    private CameraSource mCameraSource = null;

    boolean isCalled;

    DBHandler dbHandler;

    private Toast toast;

    boolean isAdded = false;

    public static Intent getStartingIntent(Context context){
        return new Intent(context, BarcodeScannerActivity.class);
    }

    public static Intent getStartingIntent(Context context, int barcodeFormat){
        return getStartingIntent(context)
                .putExtra(EXTRA_BARCODE_FORMAT,barcodeFormat);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if (getWindow() != null) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            Log.e(TAG, "Barcode scanner could not go into fullscreen mode!");
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcode_scanner);
//        setFlash = (Button) findViewById(R.id.setFlash);
//        setFlash.setOnClickListener(new View.OnClickListener() {
//            @Override public void onClick(View v) {
//                if (mCameraSource!=null){
//                    mCameraSource.setFlash(!mCameraSource.getFlash());
//                    updateFlashUi();
//                }
//            }
//        });

        ButterKnife.bind(this);

        dbHandler = new DBHandler(this);

        if (preview != null)
            if (preview.isPermissionGranted(true, mMessageSender))
                new Thread(mMessageSender).start();
    }

    @OnClick(R.id.setFlash)
    public void onViewClicked() {
        if (mCameraSource!=null){
            mCameraSource.setFlash(!mCameraSource.getFlash());
            updateFlashUi();
        }
    }


    private void updateFlashUi(){
        FloatingActionButton fab = findViewById(R.id.setFlash);
        if (mCameraSource==null){
            fab.hide();
        } else {
            fab.show();

            if (mCameraSource.getFlash()){
                fab.setImageResource(R.drawable.ic_flash_on);
            }
            else{
                fab.setImageResource(R.drawable.ic_flash_off);
            }
        }
    }


    private void createCameraSource() {

        // To initialise the detector

        FirebaseVisionBarcodeDetectorOptions options =
                new FirebaseVisionBarcodeDetectorOptions.Builder()
                        .setBarcodeFormats(
                                FirebaseVisionBarcode.FORMAT_ALL_FORMATS)
                        .build();

        FirebaseVisionBarcodeDetector detector = FirebaseVision.getInstance()
                .getVisionBarcodeDetector(options);


        // To connect the camera resource with the detector

        mCameraSource = new CameraSource(this, barcodeOverlay);
        mCameraSource.setFacing(CameraSource.CAMERA_FACING_BACK);

        barcodeScanningProcessor = new BarcodeScanningProcessor(detector);
        barcodeScanningProcessor.setBarcodeResultListener(getBarcodeResultListener());

        mCameraSource.setMachineLearningFrameProcessor(barcodeScanningProcessor);

        startCameraSource();
    }

    private void startCameraSource() {

        // check that the device has play services available.
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
                getApplicationContext());

        Log.d(TAG, "startCameraSource: " + code);

        if (code != ConnectionResult.SUCCESS) {
            Dialog dlg =
                    GoogleApiAvailability.getInstance().getErrorDialog(this, code, PERMISSION_REQUEST_CAMERA);
            dlg.show();
        }

        if (mCameraSource != null && preview != null && barcodeOverlay != null) {
            try {
                Log.d(TAG, "startCameraSource: ");
                preview.start(mCameraSource, barcodeOverlay);
            } catch (IOException e) {
                Log.d(TAG, "Unable to start camera source.", e);
                mCameraSource.release();
                mCameraSource = null;
            }
        } else
            Log.d(TAG, "startCameraSource: not started");

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        Log.d(TAG, "onRequestPermissionsResult: " + requestCode);
        preview.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    /**
     * Restarts the camera.
     */
    @Override
    protected void onResume() {
        super.onResume();
        startCameraSource();
    }

    /**
     * Stops the camera.
     */
    @Override
    protected void onPause() {
        super.onPause();
        if (preview != null)
            preview.stop();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        isCalled = true;
    }

    /**
     * Releases the resources associated with the camera source, the associated detector, and the
     * rest of the processing pipeline.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCameraSource != null) {
            mCameraSource.release();
        }
    }

    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            Log.d(TAG, "handleMessage: ");

            if (preview != null)
                createCameraSource();

        }
    };

    private final Runnable mMessageSender = () -> {
        Log.d(TAG, "mMessageSender: ");
        Message msg = mHandler.obtainMessage();
        Bundle bundle = new Bundle();
        bundle.putBoolean(KEY_CAMERA_PERMISSION_GRANTED, false);
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    };

    public BarcodeResultListener getBarcodeResultListener() {

        return new BarcodeResultListener() {
            @Override
            public void onSuccess(@Nullable Bitmap originalCameraImage, @NonNull List<FirebaseVisionBarcode> barcodes, @NonNull FrameMetadata frameMetadata, @NonNull GraphicOverlay graphicOverlay) {


                for (FirebaseVisionBarcode barCode : barcodes)
                {


                    String text = barCode.getRawValue();
                    Integer type = barCode.getValueType();

                    ContactDetail contactDetail = new ContactDetail();

                    if (text != null) {
                            contactDetail.setText(text);
                            contactDetail.setType(type);
                            preview.stop();
                            if (!dbHandler.isAccountAlreadyExist(contactDetail.getText(), contactDetail.getType())) {
                                dbHandler.insertAccountDetails(contactDetail);
                                isAdded = true;
                                setResult(Activity.RESULT_OK, new Intent().putExtra(RETURN_BARCODE,contactDetail.getText()));
                                finish();
                            } else{
                                showToast("Already Added");
                                setResult(Activity.RESULT_OK, new Intent().putExtra(RETURN_BARCODE,contactDetail.getText()));
                                finish();
                            }
                    }
                }

                if (isAdded)
                    finish();
            }

            @Override
            public void onFailure(@NonNull Exception e) {

            }
        };
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (toast != null) {
            toast.cancel();
        }
    }

    public void showToast(String message) {
        if (toast != null) {
            toast.cancel();
        }
        toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT);
        toast.show();
    }
}

package ro.utcn.foodapp.engenoid.tessocrtest;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import java.io.File;

import ro.utcn.foodapp.R;
import ro.utcn.foodapp.engenoid.tessocrtest.Core.CameraEngine;
import ro.utcn.foodapp.engenoid.tessocrtest.Core.ExtraViews.FocusBoxView;
import ro.utcn.foodapp.engenoid.tessocrtest.Core.Imaging.BitmapTools;
import ro.utcn.foodapp.ocr.OcrRecognizeAsyncTask;


public class CaptureActivity extends Activity implements SurfaceHolder.Callback, View.OnClickListener,
        Camera.PictureCallback, Camera.ShutterCallback {

    public static final String TEMP_FILE_PATH = "TEMP_FILE_PATH";
    public static final String TEMP_DIR_PATH = "TEMP_DIR_PATH";
    static final String TAG = "DBG_" + CaptureActivity.class.getName();
    public static File tempDir;
    public static File tempFilePath;

    private Button shutterButton;
    private FocusBoxView focusBox;
    private SurfaceView cameraFrame;
    private CameraEngine cameraEngine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_capture_activity);

        Intent intent = getIntent();
        final String fp = intent.getStringExtra(TEMP_FILE_PATH);
        this.tempFilePath = new File(fp);
        final String dp = intent.getStringExtra(TEMP_DIR_PATH);
        this.tempDir = new File(dp);

        cameraEngine = CameraEngine.getInstance(getApplicationContext());
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        initCamera(holder);
    }

    /**
     * Initializes the camera and starts the handler to begin previewing.
     */
    private void initCamera(SurfaceHolder holder) {
        if (holder == null) {
            throw new IllegalStateException("No SurfaceHolder provided");
        }
        if (cameraEngine != null) {
            // Open and initialize the camera
            cameraEngine.openDriver(holder);
            cameraEngine.startPreview();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    protected void onResume() {
        super.onResume();

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        cameraFrame = (SurfaceView) findViewById(R.id.camera_frame);
        shutterButton = (Button) findViewById(R.id.shutter_button);
        focusBox = (FocusBoxView) findViewById(R.id.focus_box);

        shutterButton.setOnClickListener(this);

        SurfaceHolder surfaceHolder = cameraFrame.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        cameraFrame.setOnClickListener(this);
        shutterButton.setEnabled(true);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Stop using the camera, to avoid conflicting with other camera-based apps
        cameraEngine.closeDriver();

        SurfaceHolder surfaceHolder = cameraFrame.getHolder();
        surfaceHolder.removeCallback(this);
    }
//
//    @Override
//    public void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
//
//        outState.putString(this.TEMP_FILE_PATH, this.tempFilePath.getAbsolutePath());
//        outState.putString(this.TEMP_DIR_PATH, this.tempDir.getAbsolutePath());
//    }
//
//    @Override
//    public void onRestoreInstanceState(Bundle savedInstanceState) {
//    }

//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        // Check which request we're responding to
////        if (requestCode == SAVE_PHOTO) {
////            // Make sure the request was successful
////            if (resultCode == getActivity().RESULT_OK) {
////                int value = data.getExtras().getInt("save");
////                if (value == 1) {
////
////                    Intent intent = new Intent();
////                    FCameraFragment.this.getActivity().setResult(Activity.RESULT_OK, intent);
////                    getActivity().finish();
////                }
////            }
////        }
//    }

    @Override
    public void onClick(View v) {
        if (v == shutterButton) {
            if (cameraEngine != null && cameraEngine.isOn()) {
                cameraEngine.requestAutoFocus(350L);
                cameraEngine.takeShot(this, this, this);
                shutterButton.setEnabled(false);
                shutterButton.setVisibility(View.INVISIBLE);
                focusBox.setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {

        Log.d(TAG, "Picture taken");

        if (data == null) {
            Log.d(TAG, "Got null data");
            return;
        }

        Bitmap bmp = BitmapTools.getFocusedBitmap(this, camera, data, focusBox.getBox());
        // TODO uncomment this line to save the photo
        //BitmapTools.savePicture(bmp, this.tempFilePath, this.tempDir);

        //new TessAsyncEngine().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, this, bmp);

        //Bitmap bmp = focusBox.buildLuminanceSource(data, focusBox.getWidth(), focusBox.getHeight()).renderCroppedGreyscaleBitmap();
        restartPreview();
        OcrRecognizeAsyncTask ocrRecognizeAsyncTask = new OcrRecognizeAsyncTask(CaptureActivity.this, bmp);
        ocrRecognizeAsyncTask.execute();

    }

    @Override
    public void onShutter() {

    }

    private void restartPreview() {
        if (cameraEngine != null && cameraEngine.isOn()) {
            //cameraEngine.closeDriver();
            cameraEngine.stopPreview();
            //cameraEngine.openDriver(cameraFrame.getHolder());
            cameraEngine.startPreview();
        }
    }

    public void enableCameraButtons() {
        shutterButton.setEnabled(true);
        focusBox.setVisibility(View.VISIBLE);
        shutterButton.setVisibility(View.VISIBLE);
    }
}
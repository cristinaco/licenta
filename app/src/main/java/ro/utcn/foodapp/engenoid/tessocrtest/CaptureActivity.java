package ro.utcn.foodapp.engenoid.tessocrtest;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;

import java.io.File;

import ro.utcn.foodapp.R;
import ro.utcn.foodapp.engenoid.tessocrtest.Core.CameraEngine;
import ro.utcn.foodapp.engenoid.tessocrtest.Core.ExtraViews.FocusBoxView;
import ro.utcn.foodapp.ocr.OcrRecognizeAsyncTask;


public class CaptureActivity extends Activity implements SurfaceHolder.Callback, View.OnClickListener,
        Camera.PictureCallback, Camera.ShutterCallback {

    public static final String TEMP_FILE_PATH = "TEMP_FILE_PATH";
    public static final String TEMP_DIR_PATH = "TEMP_DIR_PATH";
    static final String TAG = "DBG_" + CaptureActivity.class.getName();
    public static File tempDir;
    public static File tempFilePath;

    private ImageButton shutterButton;
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
        shutterButton = (ImageButton) findViewById(R.id.shutter_button);
        focusBox = (FocusBoxView) findViewById(R.id.focus_box);
        focusBox.setCameraEngine(cameraEngine);

        shutterButton.setOnClickListener(this);

        SurfaceHolder surfaceHolder = cameraFrame.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        cameraFrame.setOnClickListener(this);
        shutterButton.setEnabled(true);

        setListeners();
    }

    private void setListeners() {

        focusBox.setOnTouchListener(new View.OnTouchListener() {
            int lastX = -1;
            int lastY = -1;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        lastX = -1;
                        lastY = -1;
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        int currentX = (int) event.getX();
                        int currentY = (int) event.getY();

                        try {
                            Rect rect = cameraEngine.getFramingRect();

                            final int BUFFER = 50;
                            final int BIG_BUFFER = 60;
                            if (lastX >= 0) {
                                // Adjust the size of the viewfinder rectangle. Check if the touch event occurs in the corner areas first, because the regions overlap.
                                if (((currentX >= rect.left - BIG_BUFFER && currentX <= rect.left + BIG_BUFFER) || (lastX >= rect.left - BIG_BUFFER && lastX <= rect.left + BIG_BUFFER))
                                        && ((currentY <= rect.top + BIG_BUFFER && currentY >= rect.top - BIG_BUFFER) || (lastY <= rect.top + BIG_BUFFER && lastY >= rect.top - BIG_BUFFER))) {
                                    // Top left corner: adjust both top and left sides
                                    cameraEngine.adjustFramingRect(2 * (lastX - currentX), 2 * (lastY - currentY));
                                } else if (((currentX >= rect.right - BIG_BUFFER && currentX <= rect.right + BIG_BUFFER) || (lastX >= rect.right - BIG_BUFFER && lastX <= rect.right + BIG_BUFFER))
                                        && ((currentY <= rect.top + BIG_BUFFER && currentY >= rect.top - BIG_BUFFER) || (lastY <= rect.top + BIG_BUFFER && lastY >= rect.top - BIG_BUFFER))) {
                                    // Top right corner: adjust both top and right sides
                                    cameraEngine.adjustFramingRect(2 * (currentX - lastX), 2 * (lastY - currentY));
                                } else if (((currentX >= rect.left - BIG_BUFFER && currentX <= rect.left + BIG_BUFFER) || (lastX >= rect.left - BIG_BUFFER && lastX <= rect.left + BIG_BUFFER))
                                        && ((currentY <= rect.bottom + BIG_BUFFER && currentY >= rect.bottom - BIG_BUFFER) || (lastY <= rect.bottom + BIG_BUFFER && lastY >= rect.bottom - BIG_BUFFER))) {
                                    // Bottom left corner: adjust both bottom and left sides
                                    cameraEngine.adjustFramingRect(2 * (lastX - currentX), 2 * (currentY - lastY));
                                } else if (((currentX >= rect.right - BIG_BUFFER && currentX <= rect.right + BIG_BUFFER) || (lastX >= rect.right - BIG_BUFFER && lastX <= rect.right + BIG_BUFFER))
                                        && ((currentY <= rect.bottom + BIG_BUFFER && currentY >= rect.bottom - BIG_BUFFER) || (lastY <= rect.bottom + BIG_BUFFER && lastY >= rect.bottom - BIG_BUFFER))) {
                                    // Bottom right corner: adjust both bottom and right sides
                                    cameraEngine.adjustFramingRect(2 * (currentX - lastX), 2 * (currentY - lastY));
                                } else if (((currentX >= rect.left - BUFFER && currentX <= rect.left + BUFFER) || (lastX >= rect.left - BUFFER && lastX <= rect.left + BUFFER))
                                        && ((currentY <= rect.bottom && currentY >= rect.top) || (lastY <= rect.bottom && lastY >= rect.top))) {
                                    // Adjusting left side: event falls within BUFFER pixels of left side, and between top and bottom side limits
                                    cameraEngine.adjustFramingRect(2 * (lastX - currentX), 0);
                                } else if (((currentX >= rect.right - BUFFER && currentX <= rect.right + BUFFER) || (lastX >= rect.right - BUFFER && lastX <= rect.right + BUFFER))
                                        && ((currentY <= rect.bottom && currentY >= rect.top) || (lastY <= rect.bottom && lastY >= rect.top))) {
                                    // Adjusting right side: event falls within BUFFER pixels of right side, and between top and bottom side limits
                                    cameraEngine.adjustFramingRect(2 * (currentX - lastX), 0);
                                } else if (((currentY <= rect.top + BUFFER && currentY >= rect.top - BUFFER) || (lastY <= rect.top + BUFFER && lastY >= rect.top - BUFFER))
                                        && ((currentX <= rect.right && currentX >= rect.left) || (lastX <= rect.right && lastX >= rect.left))) {
                                    // Adjusting top side: event falls within BUFFER pixels of top side, and between left and right side limits
                                    cameraEngine.adjustFramingRect(0, 2 * (lastY - currentY));
                                } else if (((currentY <= rect.bottom + BUFFER && currentY >= rect.bottom - BUFFER) || (lastY <= rect.bottom + BUFFER && lastY >= rect.bottom - BUFFER))
                                        && ((currentX <= rect.right && currentX >= rect.left) || (lastX <= rect.right && lastX >= rect.left))) {
                                    // Adjusting bottom side: event falls within BUFFER pixels of bottom side, and between left and right side limits
                                    cameraEngine.adjustFramingRect(0, 2 * (currentY - lastY));
                                }
                            }
                        } catch (NullPointerException e) {
                            Log.e(TAG, "Framing rect not available", e);
                        }
                        v.invalidate();
                        lastX = currentX;
                        lastY = currentY;
                        return true;
                    case MotionEvent.ACTION_UP:
                        lastX = -1;
                        lastY = -1;
                        return true;
                }
                return false;
            }
        });
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

        //Bitmap bmp = BitmapTools.getFocusedBitmap(this, camera, data, focusBox.getFramingRect());
        // TODO uncomment this line to save the photo
        //BitmapTools.savePicture(bmp, this.tempFilePath, this.tempDir);

        //new TessAsyncEngine().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, this, bmp);

        Bitmap bmp = cameraEngine.buildLuminanceSource(data, focusBox.getWidth(), focusBox.getHeight()).renderCroppedGreyscaleBitmap();
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
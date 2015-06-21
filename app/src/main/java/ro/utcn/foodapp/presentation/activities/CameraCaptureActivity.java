package ro.utcn.foodapp.presentation.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import java.io.File;
import java.io.IOException;

import ro.utcn.foodapp.R;
import ro.utcn.foodapp.engenoid.tesseract.Core.CameraEngine;
import ro.utcn.foodapp.engenoid.tesseract.Core.CaptureActivityHandler;
import ro.utcn.foodapp.presentation.customViews.FocusBoxView;
import ro.utcn.foodapp.presentation.customViews.ShutterButton;
import ro.utcn.foodapp.model.OcrResult;
import ro.utcn.foodapp.utils.Constants;


public class CameraCaptureActivity extends Activity implements SurfaceHolder.Callback, ShutterButton.OnShutterButtonListener {

    public static final String TEMP_FILE_PATH = "TEMP_FILE_PATH";
    public static final String TEMP_DIR_PATH = "TEMP_DIR_PATH";
    static final String TAG = "DBG_" + CameraCaptureActivity.class.getName();
    public static File tempDir;
    public static File tempFilePath;

    private ShutterButton shutterButton;
    private FocusBoxView focusBox;
    private SurfaceView surfaceView;
    private CameraEngine cameraEngine;
    private CaptureActivityHandler handler;
    private SurfaceHolder surfaceHolder;
    private MaterialDialog ocrProgressDialog;
    private Rect rect;
    private boolean performOcr;
    private boolean hasSurface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.camera_capture_activity);
        surfaceView = (SurfaceView) findViewById(R.id.camera_surface_view);
        shutterButton = (ShutterButton) findViewById(R.id.shutter_button);
        focusBox = (FocusBoxView) findViewById(R.id.focus_box);

        cameraEngine = CameraEngine.getInstance(CameraCaptureActivity.this);
        focusBox.setCameraEngine(cameraEngine);
        setListeners();

        Intent intent = getIntent();
        final String fp = intent.getStringExtra(TEMP_FILE_PATH);
        this.tempFilePath = new File(fp);
        final String dp = intent.getStringExtra(TEMP_DIR_PATH);
        this.tempDir = new File(dp);
        performOcr = intent.getBooleanExtra(String.valueOf(Constants.PERFORM_OCR),false);

        handler = null;
        hasSurface = false;
    }

    @Override
    protected void onResume() {
        super.onResume();

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        surfaceHolder = surfaceView.getHolder();
        if (!hasSurface) {
            surfaceHolder.addCallback(this);
            surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }
        // TODO shutterButton.setEnabled(true);
//        if (handler != null) {
//            handler.resetState();
//        }
        if (hasSurface) {
            // The activity was paused but not stopped, so the surface still exists. Therefore
            // surfaceCreated() won't be called, so init the camera here.
            initCamera(surfaceHolder);
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (holder == null) {
            Log.e(TAG, "surfaceCreated gave us a null surface");
        }
        // Only initialize the camera if the OCR engine is ready to go.
        if (!hasSurface) {
            Log.d(TAG, "surfaceCreated(): calling initCamera()...");
            initCamera(holder);
        }
        hasSurface = true;
    }

    /**
     * Initializes the camera and starts the handler to begin previewing.
     */
    private void initCamera(SurfaceHolder holder) {
        if (holder == null) {
            throw new IllegalStateException("No SurfaceHolder provided");
        }
        if (getCameraEngine() != null) {
            // Open and initialize the camera
            try {
                getCameraEngine().openDriver(holder);
            } catch (IOException e) {
                e.printStackTrace();
            }
            handler = new CaptureActivityHandler(this, cameraEngine, performOcr);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (handler != null) {
            handler.quitSynchronously();
        }

        // Stop using the camera, to avoid conflicting with other camera-based apps
        getCameraEngine().closeDriver();
        if (!hasSurface) {
            SurfaceHolder surfaceHolder = surfaceView.getHolder();
            surfaceHolder.removeCallback(this);
        }
    }

    public void stopHandler() {
        if (handler != null) {
            handler.stop();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_CAMERA) {
            handler.hardwareShutterButtonClick();
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_FOCUS) {
            // Only perform autofocus if user is not holding down the button.
            if (event.getRepeatCount() == 0) {
                cameraEngine.requestAutoFocus(500L);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(this.TEMP_FILE_PATH, this.tempFilePath.getAbsolutePath());
        outState.putString(this.TEMP_DIR_PATH, this.tempDir.getAbsolutePath());
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        tempFilePath = new File(savedInstanceState.getString(this.TEMP_FILE_PATH));
        tempDir = new File(savedInstanceState.getString(this.TEMP_DIR_PATH));

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == Constants.SAVE_PHOTO) {
            // Make sure the request was successful
            if (resultCode == this.RESULT_OK) {
                int value = data.getExtras().getInt("save");
                if (value == 1) {
                    setResult(Activity.RESULT_OK, data);
                    this.finish();
                }
            }
        }
    }

    @Override
    public void onShutterButtonClick(ShutterButton b) {

        if (handler != null) {
            handler.shutterButtonClick();

        }
    }

    @Override
    public void onShutterButtonFocus(ShutterButton b, boolean pressed) {
        requestDelayedAutoFocus();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        hasSurface = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (ocrProgressDialog != null) {
            ocrProgressDialog.dismiss();
            ocrProgressDialog = null;
        }

    }

    /**
     * Displays information relating to the result of OCR.
     *
     * @param ocrResult Object representing successful OCR results
     * @return True if a non-null result was received for OCR
     */
    public boolean handleOcrDecode(OcrResult ocrResult) {
        // Test whether the result is null
        if (ocrResult.getText() == null || ocrResult.getText().equals("")) {
            Toast toast = Toast.makeText(this, "OCR failed. Please try again.", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.TOP, 0, 0);
            toast.show();
            return false;
        }else{
            Intent displayPhotoIntent = new Intent(this, PreviewOcrResultActivity.class);
            displayPhotoIntent.putExtra(this.TEMP_FILE_PATH, this.tempFilePath.getAbsolutePath());
            displayPhotoIntent.putExtra(this.TEMP_DIR_PATH, this.tempDir.getAbsolutePath());
            displayPhotoIntent.putExtra(Constants.OCR_RESULT_TEXT_KEY, ocrResult.getText());
            displayPhotoIntent.putExtra(Constants.OCR_WORD_BOUNDING_BOXES_KEY, (java.io.Serializable) ocrResult.getWordBoundingBoxes());
            //displayPhotoIntent.putExtra(Constants.OCR_RESULT_OBJECT_KEY, ocrResult);

            this.startActivityForResult(displayPhotoIntent, Constants.SAVE_PHOTO);
        }
        return true;
    }
    public void handleCapturingPhoto() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("save", 1);
        resultIntent.putExtra(this.TEMP_FILE_PATH, this.tempFilePath.getAbsolutePath());
        resultIntent.putExtra(this.TEMP_DIR_PATH, this.tempDir.getAbsolutePath());
        setResult(Activity.RESULT_OK, resultIntent);

        CameraCaptureActivity.this.finish();
    }
    private void setListeners() {
        shutterButton.setOnShutterButtonListener(this);

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

                            rect = getCameraEngine().getFramingRect();
                            final int BUFFER = 50;
                            final int BIG_BUFFER = 60;
                            if (lastX >= 0) {
                                // Adjust the size of the viewfinder rectangle. Check if the touch event occurs in the corner areas first, because the regions overlap.
                                if (((currentX >= rect.left - BIG_BUFFER && currentX <= rect.left + BIG_BUFFER) || (lastX >= rect.left - BIG_BUFFER && lastX <= rect.left + BIG_BUFFER))
                                        && ((currentY <= rect.top + BIG_BUFFER && currentY >= rect.top - BIG_BUFFER) || (lastY <= rect.top + BIG_BUFFER && lastY >= rect.top - BIG_BUFFER))) {
                                    // Top left corner: adjust both top and left sides
                                    getCameraEngine().adjustFramingRect(2 * (lastX - currentX), 2 * (lastY - currentY));
                                } else if (((currentX >= rect.right - BIG_BUFFER && currentX <= rect.right + BIG_BUFFER) || (lastX >= rect.right - BIG_BUFFER && lastX <= rect.right + BIG_BUFFER))
                                        && ((currentY <= rect.top + BIG_BUFFER && currentY >= rect.top - BIG_BUFFER) || (lastY <= rect.top + BIG_BUFFER && lastY >= rect.top - BIG_BUFFER))) {
                                    // Top right corner: adjust both top and right sides
                                    getCameraEngine().adjustFramingRect(2 * (currentX - lastX), 2 * (lastY - currentY));
                                } else if (((currentX >= rect.left - BIG_BUFFER && currentX <= rect.left + BIG_BUFFER) || (lastX >= rect.left - BIG_BUFFER && lastX <= rect.left + BIG_BUFFER))
                                        && ((currentY <= rect.bottom + BIG_BUFFER && currentY >= rect.bottom - BIG_BUFFER) || (lastY <= rect.bottom + BIG_BUFFER && lastY >= rect.bottom - BIG_BUFFER))) {
                                    // Bottom left corner: adjust both bottom and left sides
                                    getCameraEngine().adjustFramingRect(2 * (lastX - currentX), 2 * (currentY - lastY));
                                } else if (((currentX >= rect.right - BIG_BUFFER && currentX <= rect.right + BIG_BUFFER) || (lastX >= rect.right - BIG_BUFFER && lastX <= rect.right + BIG_BUFFER))
                                        && ((currentY <= rect.bottom + BIG_BUFFER && currentY >= rect.bottom - BIG_BUFFER) || (lastY <= rect.bottom + BIG_BUFFER && lastY >= rect.bottom - BIG_BUFFER))) {
                                    // Bottom right corner: adjust both bottom and right sides
                                    getCameraEngine().adjustFramingRect(2 * (currentX - lastX), 2 * (currentY - lastY));
                                } else if (((currentX >= rect.left - BUFFER && currentX <= rect.left + BUFFER) || (lastX >= rect.left - BUFFER && lastX <= rect.left + BUFFER))
                                        && ((currentY <= rect.bottom && currentY >= rect.top) || (lastY <= rect.bottom && lastY >= rect.top))) {
                                    // Adjusting left side: event falls within BUFFER pixels of left side, and between top and bottom side limits
                                    getCameraEngine().adjustFramingRect(2 * (lastX - currentX), 0);
                                } else if (((currentX >= rect.right - BUFFER && currentX <= rect.right + BUFFER) || (lastX >= rect.right - BUFFER && lastX <= rect.right + BUFFER))
                                        && ((currentY <= rect.bottom && currentY >= rect.top) || (lastY <= rect.bottom && lastY >= rect.top))) {
                                    // Adjusting right side: event falls within BUFFER pixels of right side, and between top and bottom side limits
                                    getCameraEngine().adjustFramingRect(2 * (currentX - lastX), 0);
                                } else if (((currentY <= rect.top + BUFFER && currentY >= rect.top - BUFFER) || (lastY <= rect.top + BUFFER && lastY >= rect.top - BUFFER))
                                        && ((currentX <= rect.right && currentX >= rect.left) || (lastX <= rect.right && lastX >= rect.left))) {
                                    // Adjusting top side: event falls within BUFFER pixels of top side, and between left and right side limits
                                    getCameraEngine().adjustFramingRect(0, 2 * (lastY - currentY));
                                } else if (((currentY <= rect.bottom + BUFFER && currentY >= rect.bottom - BUFFER) || (lastY <= rect.bottom + BUFFER && lastY >= rect.bottom - BUFFER))
                                        && ((currentX <= rect.right && currentX >= rect.left) || (lastX <= rect.right && lastX >= rect.left))) {
                                    // Adjusting bottom side: event falls within BUFFER pixels of bottom side, and between left and right side limits
                                    getCameraEngine().adjustFramingRect(0, 2 * (currentY - lastY));
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

    /**
     * Requests autofocus after a 350 ms delay. This delay prevents requesting focus when the user
     * just wants to click the shutter button without focusing. Quick button press/release will
     * trigger onShutterButtonClick() before the focus kicks in.
     */
    private void requestDelayedAutoFocus() {
        // Wait 350 ms before focusing to avoid interfering with quick button presses when
        // the user just wants to take a picture without focusing.
        cameraEngine.requestAutoFocus(350L);
    }

    public CameraEngine getCameraEngine() {
        return cameraEngine;
    }

    public CaptureActivityHandler getHandler() {
        return handler;
    }

    public void drawFocusBoxView() {
        focusBox.redraw();
    }


    public void displayOcrProgressDialog() {
        ocrProgressDialog = new MaterialDialog.Builder(CameraCaptureActivity.this)
                .content(R.string.wait_while_performing_ocr)
                .progress(true, 0)
                .cancelable(false)
                .show();
    }

    public MaterialDialog getOcrProgressDialog() {
        return ocrProgressDialog;
    }

    public void setShutterBtnClickable(boolean clickable) {
        shutterButton.setClickable(clickable);
    }

    public void startPreviewPhotoActivity(OcrResult ocrResult) {
        Intent displayPhotoIntent = new Intent(this, PreviewOcrResultActivity.class);
        displayPhotoIntent.putExtra(this.TEMP_FILE_PATH, this.tempFilePath.getAbsolutePath());
        displayPhotoIntent.putExtra(this.TEMP_DIR_PATH, this.tempDir.getAbsolutePath());
        displayPhotoIntent.putExtra(Constants.OCR_RESULT_TEXT_KEY, ocrResult.getText());
        displayPhotoIntent.putExtra(Constants.OCR_WORD_BOUNDING_BOXES_KEY, (java.io.Serializable) ocrResult.getWordBoundingBoxes());
        //displayPhotoIntent.putExtra(Constants.OCR_RESULT_OBJECT_KEY, ocrResult);

        this.startActivityForResult(displayPhotoIntent, Constants.SAVE_PHOTO);

    }


}
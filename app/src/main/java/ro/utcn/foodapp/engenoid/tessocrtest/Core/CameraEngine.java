package ro.utcn.foodapp.engenoid.tessocrtest.Core;

import android.content.Context;
import android.graphics.Point;
import android.hardware.Camera;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.WindowManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Coni on 25/05/2015.
 */
public class CameraEngine {

    static final String TAG = "DBG_" + CameraUtils.class.getName();
    private static final int MIN_PREVIEW_PIXELS = 470 * 320; // normal screen
    private static final int MAX_PREVIEW_PIXELS = 800 * 600; // more than large/HD screen
    private Context context;
    private AutoFocusManager autoFocusManager;
    private Camera camera;
    private boolean on;
    private boolean initialized;
    private boolean previewing;
    private Point screenResolution;
    private Point cameraResolution;

    private CameraEngine(Context context) {
        this.context = context;
    }

    static public CameraEngine getInstance(Context context) {
        Log.d(TAG, "Creating camera engine");
        return new CameraEngine(context);
    }


//    public void requestFocus() {
//        if (camera == null)
//            return;
//
//        if (isOn()) {
//            camera.autoFocus(autoFocusCallback);
//        }
//    }


    public boolean isOn() {
        return on;
    }

    public synchronized void openDriver(SurfaceHolder holder) {
        this.camera = CameraUtils.getCamera();

        if (this.camera == null) {
            return;
        }
        try {
            this.camera.setPreviewDisplay(holder);
        } catch (IOException e) {
            e.printStackTrace();
        }
        autoFocusManager = new AutoFocusManager(context, camera);

        if (!initialized) {
            initialized = true;
            Camera.Parameters parameters = camera.getParameters();
            WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            Display display = manager.getDefaultDisplay();
            int width = display.getWidth();
            int height = display.getHeight();
            // We're landscape-only, and have apparently seen issues with display thinking it's portrait
            // when waking from sleep. If it's not landscape, assume it's mistaken and reverse them:
            if (width < height) {
                Log.i(TAG, "Display reports portrait orientation; assuming this is incorrect");
                int temp = width;
                width = height;
                height = temp;
            }
            screenResolution = new Point(width, height);
            Log.i(TAG, "Screen resolution: " + screenResolution);
            cameraResolution = findBestPreviewSizeValue(parameters, screenResolution);
            Log.i(TAG, "Camera resolution: " + cameraResolution);
        }


        this.camera.setDisplayOrientation(90);
        setDesiredCameraParameters(this.camera);
        this.camera.startPreview();

        on = true;
    }

    public void takeShot(Camera.ShutterCallback shutterCallback,
                         Camera.PictureCallback rawPictureCallback,
                         Camera.PictureCallback jpegPictureCallback) {
        if (isOn()) {
            camera.takePicture(shutterCallback, rawPictureCallback, jpegPictureCallback);
        }
    }

    /**
     * Asks the camera hardware to perform an autofocus.
     *
     * @param delay Time delay to send with the request
     */
    public synchronized void requestAutoFocus(long delay) {
        autoFocusManager.start(delay);
    }

    void setDesiredCameraParameters(Camera camera) {
        Camera.Parameters parameters = camera.getParameters();

        if (parameters == null) {
            Log.w(TAG, "Device error: no camera parameters are available. Proceeding without configuration.");
            return;
        }

        //initializeTorch(parameters, prefs);
        String focusMode = null;
//        if (prefs.getBoolean(PreferencesActivity.KEY_AUTO_FOCUS, true)) {
//            if (prefs.getBoolean(PreferencesActivity.KEY_DISABLE_CONTINUOUS_FOCUS, false)) {
//                focusMode = findSettableValue(parameters.getSupportedFocusModes(),
//                        Camera.Parameters.FOCUS_MODE_AUTO);
//            } else {
//        focusMode = findSettableValue(parameters.getSupportedFocusModes(),
//                "continuous-video", // Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO in 4.0+
//                "continuous-picture", // Camera.Paramters.FOCUS_MODE_CONTINUOUS_PICTURE in 4.0+
//                Camera.Parameters.FOCUS_MODE_AUTO);
////            }
////        }
//        // Maybe selected auto-focus but not available, so fall through here:
//        if (focusMode == null) {
//            focusMode = findSettableValue(parameters.getSupportedFocusModes(),
//                    Camera.Parameters.FOCUS_MODE_MACRO,
//                    "edof"); // Camera.Parameters.FOCUS_MODE_EDOF in 2.2+
//        }
        focusMode = Camera.Parameters.FOCUS_MODE_MACRO;
        if (focusMode != null) {
            parameters.setFocusMode(focusMode);
        }

        parameters.setPreviewSize(cameraResolution.x, cameraResolution.y);
        camera.setParameters(parameters);
    }

    public synchronized void startPreview() {
        Camera theCamera = camera;
        if (theCamera != null && !previewing) {
            theCamera.startPreview();
            previewing = true;
            autoFocusManager = new AutoFocusManager(context, camera);
        }
    }

    /**
     * Tells the camera to stop drawing preview frames.
     */
    public synchronized void stopPreview() {
        if (autoFocusManager != null) {
            autoFocusManager.stop();
            autoFocusManager = null;
        }
        if (camera != null && previewing) {
            camera.stopPreview();
            previewing = false;
        }
//        if (camera != null) {
//            //this.autoFocusEngine.stop();
//            camera.release();
//            camera = null;
//        }
//
//        on = false;
//
//        Log.d(TAG, "CameraEngine Stopped");
    }

    /**
     * Closes the camera driver if still in use.
     */
    public synchronized void closeDriver() {
        if (camera != null) {
            camera.release();
            camera = null;
        }
    }

    private Point findBestPreviewSizeValue(Camera.Parameters parameters, Point screenResolution) {

        // Sort by size, descending
        List<Camera.Size> supportedPreviewSizes = new ArrayList<Camera.Size>(parameters.getSupportedPreviewSizes());
        Collections.sort(supportedPreviewSizes, new Comparator<Camera.Size>() {
            @Override
            public int compare(Camera.Size a, Camera.Size b) {
                int aPixels = a.height * a.width;
                int bPixels = b.height * b.width;
                if (bPixels < aPixels) {
                    return -1;
                }
                if (bPixels > aPixels) {
                    return 1;
                }
                return 0;
            }
        });

        Point bestSize = null;
        float screenAspectRatio = (float) screenResolution.x / (float) screenResolution.y;

        float diff = Float.POSITIVE_INFINITY;
        for (Camera.Size supportedPreviewSize : supportedPreviewSizes) {
            int realWidth = supportedPreviewSize.width;
            int realHeight = supportedPreviewSize.height;
            int pixels = realWidth * realHeight;
            if (pixels < MIN_PREVIEW_PIXELS || pixels > MAX_PREVIEW_PIXELS) {
                continue;
            }
            boolean isCandidatePortrait = realWidth < realHeight;
            int maybeFlippedWidth = isCandidatePortrait ? realHeight : realWidth;
            int maybeFlippedHeight = isCandidatePortrait ? realWidth : realHeight;
            if (maybeFlippedWidth == screenResolution.x && maybeFlippedHeight == screenResolution.y) {
                Point exactPoint = new Point(realWidth, realHeight);
                Log.i(TAG, "Found preview size exactly matching screen size: " + exactPoint);
                return exactPoint;
            }
            float aspectRatio = (float) maybeFlippedWidth / (float) maybeFlippedHeight;
            float newDiff = Math.abs(aspectRatio - screenAspectRatio);
            if (newDiff < diff) {
                bestSize = new Point(realWidth, realHeight);
                diff = newDiff;
            }
        }

        if (bestSize == null) {
            Camera.Size defaultSize = parameters.getPreviewSize();
            bestSize = new Point(defaultSize.width, defaultSize.height);
            Log.i(TAG, "No suitable preview sizes, using default: " + bestSize);
        }

        Log.i(TAG, "Found best approximate preview size: " + bestSize);
        return bestSize;
    }

//    Point getCameraResolution() {
//        return cameraResolution;
//    }

    Point getScreenResolution() {
        return screenResolution;
    }

    private void doSetTorch(Camera.Parameters parameters, boolean newSetting) {
        String flashMode;
        if (newSetting) {
            flashMode = findSettableValue(parameters.getSupportedFlashModes(),
                    Camera.Parameters.FLASH_MODE_TORCH,
                    Camera.Parameters.FLASH_MODE_ON);
        } else {
            flashMode = findSettableValue(parameters.getSupportedFlashModes(),
                    Camera.Parameters.FLASH_MODE_OFF);
        }
        if (flashMode != null) {
            parameters.setFlashMode(flashMode);
        }
    }

    private String findSettableValue(Collection<String> supportedValues,
                                     String... desiredValues) {
        Log.i(TAG, "Supported values: " + supportedValues);
        String result = null;
        if (supportedValues != null) {
            for (String desiredValue : desiredValues) {
                if (supportedValues.contains(desiredValue)) {
                    result = desiredValue;
                    break;
                }
            }
        }
        Log.i(TAG, "Settable value: " + result);
        return result;
    }


}

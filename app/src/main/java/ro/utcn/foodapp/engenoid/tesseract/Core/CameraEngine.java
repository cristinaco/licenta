package ro.utcn.foodapp.engenoid.tesseract.Core;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Handler;
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

import ro.utcn.foodapp.engenoid.tesseract.Core.ocr.PlanarYUVLuminanceSource;

/**
 * Created by Coni on 25/05/2015.
 */
public class CameraEngine {
    static final String TAG = "DBG_" + CameraEngine.class.getName();
    private static final int MIN_FRAME_WIDTH = 50; // originally 240
    private static final int MIN_FRAME_HEIGHT = 20; // originally 240
    private static final int MAX_FRAME_WIDTH = 800; // originally 480
    private static final int MAX_FRAME_HEIGHT = 600; // originally 360
    private static final int MIN_PREVIEW_PIXELS = 470 * 320; // normal screen
    private static final int MAX_PREVIEW_PIXELS = 800 * 600; // more than large/HD screen
    private static CameraEngine cameraEngine;
    // Preview frames are delivered here, which we pass on to the registered handler. Make sure to clear the handler so it will only receive one message.
    private static PreviewCallback previewCallback;
    private Context context;
    private ro.utcn.foodapp.engenoid.tesseract.Core.AutoFocusManager autoFocusManager;
    private Camera camera;
    private boolean on;
    private boolean initialized;
    private boolean previewing;
    private Point screenResolution;
    private Point cameraResolution;
    private Rect framingRect;
    private Rect framingRectInPreview;
    private int requestedFramingRectWidth;
    private int requestedFramingRectHeight;

    private CameraEngine(Context context) {
        this.context = context;

    }

    static public CameraEngine getInstance(Context context) {
        Log.d(TAG, "Creating camera engine");
        cameraEngine = new CameraEngine(context);
        previewCallback = new PreviewCallback(cameraEngine);
        return cameraEngine;
    }

    public static Camera getCamera() {
        try {
            return Camera.open();
        } catch (Exception e) {
            Log.e(TAG, "Cannot getCamera()");
            return null;
        }
    }

    public boolean isOn() {
        return on;
    }

    public synchronized void openDriver(SurfaceHolder holder) throws IOException {
        Camera theCamera = camera;
        if (theCamera == null) {
            theCamera = Camera.open();
            if (theCamera == null) {
                throw new IOException();
            }
            camera = theCamera;
        }
        camera.setPreviewDisplay(holder);

        if (!initialized) {
            initialized = true;
            initFromCameraParameters(theCamera);

            if (requestedFramingRectWidth > 0 && requestedFramingRectHeight > 0) {
                adjustFramingRect(requestedFramingRectWidth, requestedFramingRectHeight);
                requestedFramingRectWidth = 0;
                requestedFramingRectHeight = 0;
            }
        }
        // Landscape
        this.camera.setDisplayOrientation(0);
        setDesiredCameraParameters(this.camera);
        //this.camera.startPreview();

        // on = true;
    }

    /**
     * Closes the camera driver if still in use.
     */
    public synchronized void closeDriver() {
        if (camera != null) {
            camera.release();
            camera = null;
        }
        // Make sure to clear these each time we close the camera, so that any scanning rect
        // requested by intent is forgotten.
        framingRect = null;
        framingRectInPreview = null;

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
            previewCallback.setHandler(null, 0);
        }
    }

    /**
     * A single preview frame will be returned to the handler supplied. The data will arrive as byte[]
     * in the message.obj field, with width and height encoded as message.arg1 and message.arg2,
     * respectively.
     *
     * @param handler The handler to send the message to.
     * @param message The what field of the message to be sent.
     */
    public synchronized void requestOcrDecode(Handler handler, int message) {
        Camera theCamera = camera;
        if (theCamera != null) {
            previewCallback.setHandler(handler, message);
            theCamera.setOneShotPreviewCallback(previewCallback);
        }
    }

    private void initFromCameraParameters(Camera theCamera) {
        Camera.Parameters parameters = theCamera.getParameters();
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
        String focusMode = null;

        focusMode = findSettableValue(parameters.getSupportedFocusModes(),
                Camera.Parameters.FOCUS_MODE_MACRO,
                "edof"); // Camera.Parameters.FOCUS_MODE_EDOF in 2.2+
        if (focusMode != null) {
            parameters.setFocusMode(focusMode);
        }

        parameters.setPreviewSize(cameraResolution.x, cameraResolution.y);
        camera.setParameters(parameters);
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

    public Point getCameraResolution() {
        return cameraResolution;
    }

    Point getScreenResolution() {
        return screenResolution;
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

    public Rect getBoxRect() {

        if (framingRect == null) {
            if (camera == null) {
                return null;
            }
            Point screenResolution = getScreenResolution();
            if (screenResolution == null) {
                // Called early, before init even finished
                return null;
            }
            int width = screenResolution.x * 3 / 5;
            if (width < MIN_FRAME_WIDTH) {
                width = MIN_FRAME_WIDTH;
            } else if (width > MAX_FRAME_WIDTH) {
                width = MAX_FRAME_WIDTH;
            }
            int height = screenResolution.y * 1 / 5;
            if (height < MIN_FRAME_HEIGHT) {
                height = MIN_FRAME_HEIGHT;
            } else if (height > MAX_FRAME_HEIGHT) {
                height = MAX_FRAME_HEIGHT;
            }
            int leftOffset = (screenResolution.x - width) / 2;
            int topOffset = (screenResolution.y - height) / 2;
            framingRect = new Rect(leftOffset, topOffset, leftOffset + width, topOffset + height);
        }
        return framingRect;
    }

    /**
     * Like {@link #getFramingRect} but coordinates are in terms of the preview frame,
     * not UI / screen.
     */
    public synchronized Rect getFramingRectInPreview() {
        if (framingRectInPreview == null) {
            Rect rect = new Rect(getFramingRect());
            Point cameraResolution = getCameraResolution();
            Point screenResolution = getScreenResolution();
            if (cameraResolution == null || screenResolution == null) {
                // Called early, before init even finished
                return null;
            }
            rect.left = rect.left * cameraResolution.x / screenResolution.x;
            rect.right = rect.right * cameraResolution.x / screenResolution.x;
            rect.top = rect.top * cameraResolution.y / screenResolution.y;
            rect.bottom = rect.bottom * cameraResolution.y / screenResolution.y;
            framingRectInPreview = rect;
        }
        return framingRectInPreview;
    }

    public void adjustFramingRect(int deltaWidth, int deltaHeight) {

        if (initialized) {
            Point screenResolution = getScreenResolution();

            // Set maximum and minimum sizes
            if ((framingRect.width() + deltaWidth > screenResolution.x - 4) || (framingRect.width() + deltaWidth < 50)) {
                deltaWidth = 0;
            }
            if ((framingRect.height() + deltaHeight > screenResolution.y - 4) || (framingRect.height() + deltaHeight < 50)) {
                deltaHeight = 0;
            }

            int newWidth = framingRect.width() + deltaWidth;
            int newHeight = framingRect.height() + deltaHeight;
            int leftOffset = (screenResolution.x - newWidth) / 2;
            int topOffset = (screenResolution.y - newHeight) / 2;
            framingRect = new Rect(leftOffset, topOffset, leftOffset + newWidth, topOffset + newHeight);
            framingRectInPreview = null;
        } else {
            requestedFramingRectWidth = deltaWidth;
            requestedFramingRectHeight = deltaHeight;
        }
    }

    public Rect getFramingRect() {
        return framingRect;
    }

    /**
     * A factory method to build the appropriate LuminanceSource object based on the format
     * of the preview buffers, as described by Camera.Parameters.
     *
     * @param data   A preview frame.
     * @param width  The width of the image.
     * @param height The height of the image.
     * @return A PlanarYUVLuminanceSource instance.
     */
    public PlanarYUVLuminanceSource buildLuminanceSource(byte[] data, int width, int height) {
        Rect rect = getFramingRectInPreview();
        if (rect == null) {
            return null;
        }
        // Go ahead and assume it's YUV rather than die.
        return new PlanarYUVLuminanceSource(data, width, height, rect.left, rect.top,
                rect.width(), rect.height(), false);
    }

}

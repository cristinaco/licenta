package ro.utcn.foodapp.engenoid.tesseract.Core;

import android.graphics.Point;
import android.hardware.Camera;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * Called when the next preview frame is received.
 */
final class PreviewCallback implements Camera.PreviewCallback {

    private static final String TAG = PreviewCallback.class.getSimpleName();

    private CameraEngine cameraEngine;
    private Handler previewHandler;
    private int previewMessage;

    public PreviewCallback(CameraEngine cameraEngine) {
        this.cameraEngine = cameraEngine;
    }

    void setHandler(Handler previewHandler, int previewMessage) {
        this.previewHandler = previewHandler;
        this.previewMessage = previewMessage;
    }

    // Since we're not calling setPreviewFormat(int), the data arrives here in the YCbCr_420_SP
    // (NV21) format.
    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        Point cameraResolution = cameraEngine.getCameraResolution();
        Handler thePreviewHandler = previewHandler;
        if (cameraResolution != null && thePreviewHandler != null) {
            Message message = thePreviewHandler.obtainMessage(previewMessage, cameraResolution.x,
                    cameraResolution.y, data);
            message.sendToTarget();
            previewHandler = null;
        } else {
            Log.d(TAG, "Got preview callback, but no handler or resolution available");
        }
    }

}

package ro.utcn.foodapp.engenoid.tesseract.Core;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import ro.utcn.foodapp.R;
import ro.utcn.foodapp.model.OcrResult;
import ro.utcn.foodapp.presentation.activities.CameraCaptureActivity;

/**
 * Created by coponipi on 02.05.2015.
 */
public class CaptureActivityHandler extends Handler {
    private static final String TAG = CaptureActivityHandler.class.getSimpleName();
    private static State state;
    private CameraCaptureActivity cameraCaptureActivity;
    private CameraEngine cameraEngine;
    private DecodeThread decodeThread;
    private boolean performOcr;

    public CaptureActivityHandler(CameraCaptureActivity cameraCaptureActivity, CameraEngine cameraEngine, boolean performOcr) {
        this.cameraCaptureActivity = cameraCaptureActivity;
        this.cameraEngine = cameraEngine;
        this.performOcr = performOcr;

        // Start ourselves capturing previews (and decoding if using continuous recognition mode).
        cameraEngine.startPreview();
        decodeThread = new DecodeThread(this.cameraCaptureActivity, performOcr);
        decodeThread.start();

        state = State.SUCCESS;
        restartOcrPreview();
    }

    @Override
    public void handleMessage(Message message) {
        Toast toast;
        switch (message.what) {
            case R.id.restart_preview:
                restartOcrPreview();
                break;
            case R.id.ocr_decode_succeded:
                state = State.SUCCESS;
                cameraCaptureActivity.setShutterBtnClickable(true);
                cameraCaptureActivity.handleOcrDecode((OcrResult) message.obj);
                break;
            case R.id.ocr_decode_failed:
                state = State.PREVIEW;
                cameraCaptureActivity.setShutterBtnClickable(true);
                toast = Toast.makeText(cameraCaptureActivity, "OCR failed. Please try again.", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.TOP, 0, 0);
                toast.show();
                break;
            case R.id.capture_photo_failed:
                state = State.PREVIEW;
                cameraCaptureActivity.setShutterBtnClickable(true);
                toast = Toast.makeText(cameraCaptureActivity, "Capturing photo failed. Please try again.", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.TOP, 0, 0);
                toast.show();
                break;
            case R.id.capture_photo_succeded:
                state = State.SUCCESS;
                cameraCaptureActivity.setShutterBtnClickable(true);
                cameraCaptureActivity.handleCapturingPhoto();
                break;
        }
    }

    /**
     * Start the preview, but don't try to OCR anything until the user presses the shutter button.
     */
    private void restartOcrPreview() {
        // Display the shutter buttons

        if (state == State.SUCCESS) {
            state = State.PREVIEW;

//            // Draw the viewfinder.
            cameraCaptureActivity.drawFocusBoxView();
        }
    }

    /**
     * Request OCR on the current preview frame.
     */
    private void ocrDecode() {
        state = State.PREVIEW_PAUSED;
        cameraEngine.requestOcrDecode(decodeThread.getHandler(), R.id.ocr_decode);
    }

    /**
     * Request OCR when the hardware shutter button is clicked.
     */
    public void hardwareShutterButtonClick() {
        // Ensure that we're not in continuous recognition mode
        if (state == State.PREVIEW) {
            ocrDecode();
        }
    }

    /**
     * Request OCR when the on-screen shutter button is clicked.
     */
    public void shutterButtonClick() {
        // Disable further clicks on this button until OCR request is finished
        cameraCaptureActivity.setShutterBtnClickable(false);
        ocrDecode();
    }

    public void quitSynchronously() {
        state = State.DONE;
        if (cameraEngine != null) {
            cameraEngine.stopPreview();
        }
        //Message quit = Message.obtain(decodeThread.getHandler(), R.id.quit);
        try {
            //quit.sendToTarget(); // This always gives "sending message to a Handler on a dead thread"

            // Wait at most half a second; should be enough time, and onPause() will timeout quickly
            decodeThread.join(500L);
        } catch (InterruptedException e) {
            Log.w(TAG, "Caught InterruptedException in quitSyncronously()", e);
            // continue
        } catch (RuntimeException e) {
            Log.w(TAG, "Caught RuntimeException in quitSyncronously()", e);
            // continue
        } catch (Exception e) {
            Log.w(TAG, "Caught unknown Exception in quitSynchronously()", e);
        }

        // Be absolutely sure we don't send any queued up messages
        removeMessages(R.id.ocr_decode);

    }

    public void stop() {
        // TODO See if this should be done by sending a quit message to decodeHandler as is done
        // below in quitSynchronously().

        Log.d(TAG, "Setting state to CONTINUOUS_PAUSED.");
        state = State.CONTINUOUS_PAUSED;
        removeMessages(R.id.ocr_decode);

        // Freeze the view displayed to the user.
//    CameraManager.get().stopPreview();
    }

    private enum State {
        PREVIEW,
        PREVIEW_PAUSED,
        CONTINUOUS,
        CONTINUOUS_PAUSED,
        SUCCESS,
        DONE
    }
}

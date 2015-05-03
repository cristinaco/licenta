package ro.utcn.foodapp.engenoid.tesseract.Core;

import android.os.Handler;
import android.os.Message;

import com.googlecode.tesseract.android.TessBaseAPI;

import ro.utcn.foodapp.engenoid.tesseract.Core.ocr.OcrRecognizeAsyncTask;
import ro.utcn.foodapp.presentation.activities.CaptureActivity;

/**
 * Class to send bitmap data for OCR.
 * Created by coponipi on 02.05.2015.
 */
public class DecodeHandler extends Handler {
    private CaptureActivity captureActivity;
    private TessBaseAPI tessBaseAPI;

    public DecodeHandler(CaptureActivity activity) {
        this.captureActivity = activity;
        tessBaseAPI = new TessBaseAPI();

    }

    @Override
    public void handleMessage(Message message) {
        ocrDecode((byte[]) message.obj, message.arg1, message.arg2);
    }

    /**
     * Launch an AsyncTask to perform an OCR decode for single-shot mode.
     *
     * @param data   Image data
     * @param width  Image width
     * @param height Image height
     */
    private void ocrDecode(byte[] data, int width, int height) {
        // Launch OCR asynchronously, so we get the dialog box displayed immediately
        new OcrRecognizeAsyncTask(captureActivity, tessBaseAPI, data, width, height).execute();
        //beepManager.playBeepSoundAndVibrate();
        captureActivity.runOnUiThread(new Runnable() {
            public void run() {
                captureActivity.displayOcrProgressDialog();
            }
        });
    }
}

package ro.utcn.foodapp.engenoid.tesseract.Core;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.CountDownLatch;

import ro.utcn.foodapp.presentation.activities.CameraCaptureActivity;

/**
 * This thread does all the heavy lifting of decoding the images.
 */
final class DecodeThread extends Thread {

    private final CameraCaptureActivity activity;
    private final CountDownLatch handlerInitLatch;
    private Handler handler;
    private boolean performOcr;

    DecodeThread(CameraCaptureActivity activity, boolean performOcr) {
        this.activity = activity;
        this.performOcr = performOcr;
        handlerInitLatch = new CountDownLatch(1);
    }

    Handler getHandler() {
        try {
            handlerInitLatch.await();
        } catch (InterruptedException ie) {
            // continue?
        }
        return handler;
    }

    @Override
    public void run() {
        Looper.prepare();
        handler = new DecodeHandler(activity, performOcr);
        handlerInitLatch.countDown();
        Looper.loop();
    }
}

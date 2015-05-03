package ro.utcn.foodapp.engenoid.tessocrtest.Core;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.CountDownLatch;

import ro.utcn.foodapp.engenoid.tessocrtest.CaptureActivity;

/**
 * This thread does all the heavy lifting of decoding the images.
 */
final class DecodeThread extends Thread {

    private final CaptureActivity activity;
    private final CountDownLatch handlerInitLatch;
    private Handler handler;

    DecodeThread(CaptureActivity activity) {
        this.activity = activity;
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
        handler = new DecodeHandler(activity);
        handlerInitLatch.countDown();
        Looper.loop();
    }
}

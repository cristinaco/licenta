package ro.utcn.foodapp.utils;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

import ro.utcn.foodapp.R;
import ro.utcn.foodapp.presentation.activities.CaptureActivity;

/**
 * Task used to perform the character recognition on a given bitmap
 * </p>
 * Created by coponipi on 17.04.2015.
 */
public class CapturePhotoAsyncTask extends AsyncTask<Void, Void, Boolean> {
    private Bitmap bmp;
    private CaptureActivity captureActivity;
    private byte[] data;
    private int bitmapWidth;
    private int bitmapHeight;

    public CapturePhotoAsyncTask(CaptureActivity captureActivity, byte[] data, int width, int height) {
        this.captureActivity = captureActivity;
        this.data = data;
        this.bitmapWidth = width;
        this.bitmapHeight = height;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        bmp = captureActivity.getCameraEngine().buildLuminanceSource(data, bitmapWidth, bitmapHeight).renderCroppedGreyscaleBitmap();
        // Check for failure
        if (bmp == null) {
            return false;
        }
        return true;
    }

    @Override
    protected void onPostExecute(Boolean result) {

        Handler handler = captureActivity.getHandler();
        if (handler != null) {
            // Send results for single-shot mode recognition.
            if (result) {
                Message message = Message.obtain(handler, R.id.capture_photo_succeded);
                message.sendToTarget();
                BitmapTools.savePicture(bmp, captureActivity.tempFilePath, captureActivity.tempDir);
            } else {
                Message message = Message.obtain(handler, R.id.capture_photo_failed);
                message.sendToTarget();
            }
        }
    }
}

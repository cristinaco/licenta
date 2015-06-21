package ro.utcn.foodapp.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

import java.nio.ByteBuffer;

import ro.utcn.foodapp.R;
import ro.utcn.foodapp.engenoid.tesseract.Core.ocr.PlanarYUVLuminanceSource;
import ro.utcn.foodapp.presentation.activities.CameraCaptureActivity;

/**
 * Task used to perform the character recognition on a given bitmap
 * </p>
 * Created by coponipi on 17.04.2015.
 */
public class CaptureSimplePhotoAsyncTask extends AsyncTask<Void, Void, Boolean> {
    private Bitmap bmp;
    private CameraCaptureActivity cameraCaptureActivity;
    private byte[] data;
    private int bitmapWidth;
    private int bitmapHeight;

    public CaptureSimplePhotoAsyncTask(CameraCaptureActivity cameraCaptureActivity, byte[] data, int width, int height) {
        this.cameraCaptureActivity = cameraCaptureActivity;
        this.data = data;
        this.bitmapWidth = width;
        this.bitmapHeight = height;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        bmp = cameraCaptureActivity.getCameraEngine().buildLuminanceSource(data, bitmapWidth, bitmapHeight).renderCroppedGreyscaleBitmap();
        if (bmp == null) {
            return false;
        }
        return true;
    }

    @Override
    protected void onPostExecute(Boolean result) {

        Handler handler = cameraCaptureActivity.getHandler();
        if (handler != null) {
            // Send results for single-shot mode recognition.
            if (result) {
                Message message = Message.obtain(handler, R.id.capture_photo_succeded);
                message.sendToTarget();
                BitmapTools.savePicture(bmp, cameraCaptureActivity.tempFilePath, cameraCaptureActivity.tempDir);
            } else {
                Message message = Message.obtain(handler, R.id.capture_photo_failed);
                message.sendToTarget();
            }
        }
    }
}

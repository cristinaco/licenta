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

        //bmp = Bitmap.createBitmap(data, bitmapWidth,bitmapHeight,Bitmap.Config.ARGB_8888);





//        final int pixCount = bitmapWidth * bitmapHeight;
//        int[] intGreyBuffer = new int[pixCount];
//        for(int i=0; i < pixCount; i++)
//        {
//            int greyValue = (int)intGreyBuffer[i] & 0xff;
//            intGreyBuffer[i] = 0xff000000 | (greyValue << 16) | (greyValue << 8) | greyValue;
//        }
//        Bitmap grayScaledPic = Bitmap.createBitmap(intGreyBuffer, bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888);
        // Bitmap bit = BitmapTools.decodeByteArray(data,bitmapWidth,bitmapHeight, BitmapTools.ScalingLogic.FIT);

//        byte[] rgbData = data;
//        int nrOfPixels = rgbData.length / 3; // Three bytes per pixel.
//        int pixels[] = new int[nrOfPixels];
//        for(int i = 0; i < nrOfPixels; i++) {
//            int r = data[3*i];
//            int g = data[3*i + 1];
//            int b = data[3*i + 2];
//            pixels[i] = Color.rgb(r, g, b);
//        }
        //Bitmap bitmap = Bitmap.createBitmap(pixels, bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888);
        //Bitmap bm = BitmapFactory.decodeByteArray(data, 0, data.length);

        //bmp = new PlanarYUVLuminanceSource(data, bitmapWidth, bitmapHeight, 0, 0, 0, 0, false).renderCroppedGreyscaleBitmap();
//        bmp = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888);
//        //bmp.copyPixelsFromBuffer(ByteBuffer.wrap(data));
//
//        byte[] Bits = new byte[data.length * 4]; //That's where the RGBA array goes.
//        int i;
//        for (i = 0; i < data.length; i++) {
//            Bits[i * 4] =
//                    Bits[i * 4 + 1] =
//                            Bits[i * 4 + 2] = (byte) ~data[i]; //Invert the source bits
//            Bits[i * 4 + 3] = -1;//0xff, that's the alpha.
//        }
//
////Now put these nice RGBA pixels into a Bitmap object
//
//        bmp = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888);
//        bmp.copyPixelsFromBuffer(ByteBuffer.wrap(Bits));


        // Check for failure
        //bmp = bm;
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

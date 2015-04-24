package ro.utcn.foodapp.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;

import ro.utcn.foodapp.camera.CameraConfigurationManager;

/**
 * Created by coponipi on 23.04.2015.
 */
public class BitmapUtils {

    public static Bitmap decodeByteArray(byte[] bytes, int dstWidth, int dstHeight,
                                         ScalingLogic scalingLogic) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
        options.inJustDecodeBounds = false;
        options.inSampleSize = calculateSampleSize(options.outWidth, options.outHeight, dstWidth,
                dstHeight, scalingLogic);
        Bitmap unscaledBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);

        return unscaledBitmap;
    }

    public static int calculateSampleSize(int srcWidth, int srcHeight, int dstWidth, int dstHeight,
                                          ScalingLogic scalingLogic) {
        if (scalingLogic == ScalingLogic.FIT) {
            final float srcAspect = (float) srcWidth / (float) srcHeight;
            final float dstAspect = (float) dstWidth / (float) dstHeight;

            if (srcAspect > dstAspect) {
                return srcWidth / dstWidth;
            } else {
                return srcHeight / dstHeight;
            }
        } else {
            final float srcAspect = (float) srcWidth / (float) srcHeight;
            final float dstAspect = (float) dstWidth / (float) dstHeight;

            if (srcAspect > dstAspect) {
                return srcHeight / dstHeight;
            } else {
                return srcWidth / dstWidth;
            }
        }
    }

    public static Bitmap createScaledBitmap(Bitmap unscaledBitmap, int dstWidth, int dstHeight,
                                            ScalingLogic scalingLogic) {
        Rect srcRect = calculateSrcRect(unscaledBitmap.getWidth(), unscaledBitmap.getHeight(),
                dstWidth, dstHeight, scalingLogic);
        Rect dstRect = calculateDstRect(unscaledBitmap.getWidth(), unscaledBitmap.getHeight(),
                dstWidth, dstHeight, scalingLogic);
        Bitmap scaledBitmap = Bitmap.createBitmap(dstRect.width(), dstRect.height(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(scaledBitmap);
        canvas.drawBitmap(unscaledBitmap, srcRect, dstRect, new Paint(Paint.FILTER_BITMAP_FLAG));

        return scaledBitmap;
    }

    public static Rect calculateSrcRect(int srcWidth, int srcHeight, int dstWidth, int dstHeight,
                                        ScalingLogic scalingLogic) {
        if (scalingLogic == ScalingLogic.CROP) {
            final float srcAspect = (float) srcWidth / (float) srcHeight;
            final float dstAspect = (float) dstWidth / (float) dstHeight;

            if (srcAspect > dstAspect) {
                final int srcRectWidth = (int) (srcHeight * dstAspect);
                final int srcRectLeft = (srcWidth - srcRectWidth) / 2;
                return new Rect(srcRectLeft, 0, srcRectLeft + srcRectWidth, srcHeight);
            } else {
                final int srcRectHeight = (int) (srcWidth / dstAspect);
                final int scrRectTop = (int) (srcHeight - srcRectHeight) / 2;
                return new Rect(0, scrRectTop, srcWidth, scrRectTop + srcRectHeight);
            }
        } else {
            return new Rect(0, 0, srcWidth, srcHeight);
        }
    }

    public static Rect calculateDstRect(int srcWidth, int srcHeight, int dstWidth, int dstHeight,
                                        ScalingLogic scalingLogic) {
        if (scalingLogic == ScalingLogic.FIT) {
            final float srcAspect = (float) srcWidth / (float) srcHeight;
            final float dstAspect = (float) dstWidth / (float) dstHeight;

            if (srcAspect > dstAspect) {
                return new Rect(0, 0, dstWidth, (int) (dstWidth / srcAspect));
            } else {
                return new Rect(0, 0, (int) (dstHeight * srcAspect), dstHeight);
            }
        } else {
            return new Rect(0, 0, dstWidth, dstHeight);
        }
    }

    public static Bitmap cropImage(Bitmap bitmap, Rect box) {
        Point CamRes = CameraConfigurationManager.getCameraResolution();
        Point ScrRes = CameraConfigurationManager.getScreenResolution();

        int SW = ScrRes.x; //SCREEN WIDTH - HEIGHT
        int SH = ScrRes.y;

        int RW = box.width(); // FOCUS BOX RECT WIDTH - HEIGHT - TOP - LEFT
        int RH = box.height();
        int RL = box.left;
        int RT = box.top;

        float RSW = (float) (RW * Math.pow(SW, -1)); //DIMENSION RATIO OF FOCUSBOX OVER SCREEN
        float RSH = (float) (RH * Math.pow(SH, -1));

        float RSL = (float) (RL * Math.pow(SW, -1));
        float RST = (float) (RT * Math.pow(SH, -1));

        float k = 0.5f;

        int CW = CamRes.x;
        int CH = CamRes.y;

        int X = (int) (k * CW); //SCALED BITMAP FROM CAMERA
        int Y = (int) (k * CH);

        //SCALING WITH SONY TOOLS
        // http://developer.sonymobile.com/2011/06/27/how-to-scale-images-for-your-android-application/

        //Bitmap unscaledBitmap = BitmapUtils.decodeByteArray(data, X, Y, ScalingLogic.CROP);
        Bitmap bmp = createScaledBitmap(bitmap, X, Y, ScalingLogic.CROP);
        bitmap.recycle();

        if (CW > CH)
            bmp = rotateBitmap(bmp, 90);

        int BW = bmp.getWidth();   //NEW FULL CAPTURED BITMAP DIMENSIONS
        int BH = bmp.getHeight();

        int RBL = (int) (RSL * BW); // NEW CROPPED BITMAP IN THE FOCUS BOX
        int RBT = (int) (RST * BH);

        int RBW = (int) (RSW * BW);
        int RBH = (int) (RSH * BH);

        Bitmap res = Bitmap.createBitmap(bmp, RBL, RBT, RBW, RBH);
        bmp.recycle();

        return res;
    }

    public static Bitmap rotateBitmap(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    public static Bitmap preRotateBitmap(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.preRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, false);
    }

    public static enum ScalingLogic {
        CROP, FIT
    }
}

package ro.utcn.foodapp.engenoid.tesseract.Core.ocr;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import ro.utcn.foodapp.R;
import ro.utcn.foodapp.model.OcrResult;
import ro.utcn.foodapp.presentation.activities.CameraCaptureActivity;
import ro.utcn.foodapp.utils.BitmapTools;
import ro.utcn.foodapp.utils.Constants;

/**
 * Task used to perform the character recognition on a given bitmap
 * </p>
 * Created by coponipi on 17.04.2015.
 */
public class OcrRecognizeAsyncTask extends AsyncTask<Void, Void, Boolean> {
    private TessBaseAPI tessBaseAPI;
    private String language;
    private Bitmap bmp;
    private CameraCaptureActivity cameraCaptureActivity;
    private String recognizedText;
    private byte[] data;
    private int bitmapWidth;
    private int bitmapHeight;

    public OcrRecognizeAsyncTask(CameraCaptureActivity cameraCaptureActivity, TessBaseAPI tessBaseAPI, byte[] data, int width, int height) {
        this.cameraCaptureActivity = cameraCaptureActivity;
        this.tessBaseAPI = tessBaseAPI;
        this.data = data;
        this.bitmapWidth = width;
        this.bitmapHeight = height;
        this.language = Constants.OCR_TRAINED_DATA_LANGUAGE;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        String destinationPath = Environment.getExternalStorageDirectory() + File.separator + "tesseract" + File.separator;

        // If traineddata file does not exists in tesseract/tessdata, copy it from assets to device storage
        if (!new File(destinationPath + File.separator + "tessdata" + File.separator + "ron.traineddata").exists()) {
            AssetManager assetManager = cameraCaptureActivity.getAssets();
            InputStream in = null;
            OutputStream out = null;
            try {
                in = assetManager.open("tesseract" + File.separator + "tessdata" + File.separator + "ron.traineddata");
                File outFile = new File(destinationPath + File.separator + "tessdata" + File.separator);
                if (!outFile.exists())
                    outFile.mkdirs();
                out = new FileOutputStream(outFile + File.separator + "ron.traineddata");

                copyFile(in, out);
                in.close();
                out.flush();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        tessBaseAPI.setDebug(true);
        // Init the tesseract engine with the path of traineddata and the used language
        tessBaseAPI.init(destinationPath, language);
        bmp = cameraCaptureActivity.getCameraEngine().buildLuminanceSource(data, bitmapWidth, bitmapHeight).renderCroppedGreyscaleBitmap();
        tessBaseAPI.setImage(bmp);
        // Get the recognized text from image
        recognizedText = tessBaseAPI.getUTF8Text();
        // Check for failure
        if (recognizedText == null || recognizedText.equals("") || bmp == null) {
            return false;
        }
        return true;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        //captureActivity.setRecognizedText(recognizedText);
        //captureActivity.displayRecognizedText();
        Log.d("Result", recognizedText);
        OcrResult ocrResult = new OcrResult();
        ocrResult.setText(recognizedText);
        ocrResult.setPhotoFilePath(cameraCaptureActivity.tempFilePath);
        ocrResult.setPhotoDirPath(cameraCaptureActivity.tempDir);
        ocrResult.setWordBoundingBoxes(tessBaseAPI.getWords().getBoxRects());
        tessBaseAPI.end();

        Handler handler = cameraCaptureActivity.getHandler();
        if (handler != null) {
            // Send results for single-shot mode recognition.
            if (result) {
                Message message = Message.obtain(handler, R.id.ocr_decode_succeded, ocrResult);
                message.sendToTarget();
                BitmapTools.savePicture(bmp, cameraCaptureActivity.tempFilePath, cameraCaptureActivity.tempDir);
                //captureActivity.startPreviewPhotoActivity(ocrResult);
            } else {
                Message message = Message.obtain(handler, R.id.ocr_decode_failed, ocrResult);
                message.sendToTarget();
            }
            cameraCaptureActivity.getOcrProgressDialog().dismiss();

        }
        if (tessBaseAPI != null) {
            tessBaseAPI.clear();
        }
    }

    private void copyFile(InputStream in, OutputStream out) {
        byte[] buffer = new byte[2048];
        int read;
        try {
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

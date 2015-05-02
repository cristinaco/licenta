package ro.utcn.foodapp.ocr;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.afollestad.materialdialogs.MaterialDialog;
import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import ro.utcn.foodapp.R;
import ro.utcn.foodapp.engenoid.tessocrtest.CaptureActivity;
import ro.utcn.foodapp.engenoid.tessocrtest.Core.Dialogs.ImageDialog;
import ro.utcn.foodapp.engenoid.tessocrtest.Core.Imaging.BitmapTools;
import ro.utcn.foodapp.model.OcrResult;
import ro.utcn.foodapp.utils.Constants;

/**
 * Created by coponipi on 17.04.2015.
 */
public class OcrRecognizeAsyncTask extends AsyncTask<Void, Void, Void> {
    private TessBaseAPI tessBaseAPI;
    private Bitmap bitmap;
    private String language;
    private CaptureActivity captureActivity;
    private String recognizedText;
    private MaterialDialog progressDialog;

    public OcrRecognizeAsyncTask(CaptureActivity captureActivity, Bitmap bitmap) {
        this.captureActivity = captureActivity;
        this.bitmap = bitmap;
        this.language = Constants.OCR_TRAINED_DATA_LANGUAGE;
        tessBaseAPI = new TessBaseAPI();

    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressDialog = new MaterialDialog.Builder(captureActivity)
                .content(R.string.wait_while_performing_ocr)
                .progress(true, 0)
                .cancelable(false)
                .show();

    }


    @Override
    protected Void doInBackground(Void... params) {
        String destinationPath = Environment.getExternalStorageDirectory() + File.separator + "tesseract" + File.separator;

        // If traineddata file does not exists in tesseract/tessdata, copy it from assets to device storage
        if (!new File(destinationPath + File.separator + "tessdata" + File.separator + "ron.traineddata").exists()) {
            AssetManager assetManager = captureActivity.getAssets();
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
        tessBaseAPI = new TessBaseAPI();
        tessBaseAPI.setDebug(true);

        // Init the tesseract engine with the path of traineddata and the used language
        tessBaseAPI.init(destinationPath, language);
        tessBaseAPI.setImage(bitmap);
        // Get the recognized text from image
        recognizedText = tessBaseAPI.getUTF8Text();
        tessBaseAPI.end();

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        //captureActivity.setRecognizedText(recognizedText);
        //captureActivity.displayRecognizedText();
        Log.d("Result", recognizedText);
        OcrResult ocrResult = new OcrResult();
        ocrResult.setText(recognizedText);
        ocrResult.setBitmap(bitmap);
        ocrResult.setWordBoundingBoxes(tessBaseAPI.getWords().getBoxRects());
        tessBaseAPI.end();

        if (progressDialog != null) {
            progressDialog.dismiss();
        }

        ImageDialog.New()
                .addBitmap(BitmapTools.getAnnotatedBitmap(ocrResult))
                .addTitle(recognizedText)
                .show(captureActivity.getFragmentManager(), "TAG");
        captureActivity.enableCameraButtons();
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

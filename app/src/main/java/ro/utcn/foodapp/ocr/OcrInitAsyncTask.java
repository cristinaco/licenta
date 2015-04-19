package ro.utcn.foodapp.ocr;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Environment;

import com.afollestad.materialdialogs.MaterialDialog;
import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import ro.utcn.foodapp.R;
import ro.utcn.foodapp.camera.PreviewPhotoActivity;
import ro.utcn.foodapp.utils.Constants;

/**
 * Created by coponipi on 17.04.2015.
 */
public class OcrInitAsyncTask extends AsyncTask<Void, Void, Void> {
    private TessBaseAPI tessBaseAPI;
    private Bitmap bitmap;
    private String language;
    private PreviewPhotoActivity previewPhotoActivity;
    private String recognizedText;
    private MaterialDialog progressDialog;

    public OcrInitAsyncTask(PreviewPhotoActivity previewPhotoActivity, TessBaseAPI tessBaseAPI, Bitmap bitmap) {
        this.previewPhotoActivity = previewPhotoActivity;
        this.tessBaseAPI = tessBaseAPI;
        this.bitmap = bitmap;
        this.language = Constants.OCR_TRAINED_DATA_LANGUAGE;

    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressDialog = new MaterialDialog.Builder(previewPhotoActivity)
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
            AssetManager assetManager = previewPhotoActivity.getAssets();
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
        // Send the image to tesseract engine to perform ocr
        tessBaseAPI.setImage(bitmap);
        // Get the recognized text from image
        recognizedText = tessBaseAPI.getUTF8Text();

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        previewPhotoActivity.setRecognizedText(recognizedText);
        previewPhotoActivity.displayRecognizedText();
        tessBaseAPI.end();

        if (progressDialog != null) {
            progressDialog.dismiss();
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

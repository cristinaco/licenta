package ro.utcn.foodapp.ocr;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Environment;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import ro.utcn.foodapp.camera.PreviewPhotoActivity;

/**
 * Created by coponipi on 17.04.2015.
 */
public class OcrInitAsyncTask extends AsyncTask<Void, Void, Void> {
    private TessBaseAPI tessBaseAPI;
    private Bitmap bitmap;
    private String language;
    private PreviewPhotoActivity previewPhotoActivity;
    private String recognizedText;

    public OcrInitAsyncTask(PreviewPhotoActivity previewPhotoActivity, TessBaseAPI tessBaseAPI, Bitmap bitmap) {
        this.previewPhotoActivity = previewPhotoActivity;
        this.tessBaseAPI = tessBaseAPI;
        this.bitmap = bitmap;
        this.language = "ron";

    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

    }

    private void copyFile(InputStream in, OutputStream out) {
        byte[] buffer = new byte[1024];
        int read;
        try {
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected Void doInBackground(Void... params) {
        AssetManager assetManager = previewPhotoActivity.getAssets();
        String destinationPath = Environment.getExternalStorageDirectory() + "/tesseract/tessdata/";
        InputStream in = null;
        OutputStream out = null;
        if (!new File(destinationPath).exists()) {
            try {
                in = assetManager.open("tesseract/tessdata/ron.traineddata");
                File outFile = new File(destinationPath);
                if (!outFile.exists())
                    outFile.mkdirs();
                //datapath + File.separator + "tessdata" + File.separator + "ron" + ".traineddata"

                out = new FileOutputStream(outFile + "/ron.traineddata");

                copyFile(in, out);
                in.close();
                out.flush();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
//        File dir = new File(datapath + "tessdata/");
//
            tessBaseAPI = new TessBaseAPI();
            tessBaseAPI.setDebug(true);


            tessBaseAPI.init(destinationPath, language);
            tessBaseAPI.setImage(bitmap);
            recognizedText = tessBaseAPI.getUTF8Text();
        }


        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        previewPhotoActivity.setRecognizedText(recognizedText);
        previewPhotoActivity.displayRecognizedText();
        tessBaseAPI.end();
    }

}

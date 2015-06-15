package ro.utcn.foodapp.presentation.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;

import ro.utcn.foodapp.R;
import ro.utcn.foodapp.model.OcrResult;
import ro.utcn.foodapp.utils.BitmapTools;
import ro.utcn.foodapp.utils.Constants;

public class PreviewOcrResultActivity extends ActionBarActivity {

    public static final String TEMP_FILE_PATH = "TEMP_FILE_PATH";
    public static final String TEMP_DIR_PATH = "TEMP_DIR_PATH";
    private ImageView imageView;
    private EditText ocrRecognizedEditText;
    private File photoFilePath;
    private File photoDirPath;
    private List<Rect> wordBoundingBoxes;
    private String recognizedText;
    private OcrResult ocrResult;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview_photo);

        imageView = (ImageView) findViewById(R.id.camera_display_photo_container);
        ocrRecognizedEditText = (EditText) findViewById(R.id.activity_preview_photo_recognized_text_edit_text);
        ocrRecognizedEditText.setEnabled(false);

        Intent intent = getIntent();
        photoFilePath = new File((intent.getStringExtra(TEMP_FILE_PATH)));
        photoDirPath = new File(intent.getStringExtra(TEMP_DIR_PATH));
        //ocrResult = (OcrResult) intent.getSerializableExtra(Constants.OCR_RESULT_OBJECT_KEY);
        recognizedText = intent.getStringExtra(Constants.OCR_RESULT_TEXT_KEY);
        wordBoundingBoxes = (List<Rect>) intent.getSerializableExtra(Constants.OCR_WORD_BOUNDING_BOXES_KEY);

        // Used the Picasso library to display and scale the photo to fit in the image view
        // Picasso.with(this).load(photoFilePath).fit().into(imageView);
//        BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
//        Bitmap bitmap = BitmapFactory.decodeFile(photoFilePath.getAbsolutePath(), options);
//
//        if (wordBoundingBoxes != null && bitmap != null) {
//            Bitmap mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
//            Bitmap annotatedBitmap = BitmapTools.getAnnotatedBitmap(mutableBitmap, wordBoundingBoxes);
//            imageView.setImageBitmap(annotatedBitmap);
//        } else {
//            imageView.setImageBitmap(bitmap);
//        }
        BitmapWorkerTask bitmapWorkerTask = new BitmapWorkerTask(imageView, photoFilePath, wordBoundingBoxes);
        bitmapWorkerTask.execute();

        ocrRecognizedEditText.setText(recognizedText);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    @Override
    public void onPause() {
        super.onPause();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_preview_photo, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save_ocr_result:
                saveOcrResult();
                return true;
            case R.id.action_edit_ocr_result:
                editOcrResult();
                return true;
            case R.id.action_discard_ocr_result:
                discardOcrResult();
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Unsaved data will be lost");

            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    discardOcrResult();
                    finish();
                }
            });

            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            AlertDialog alert = builder.create();
            alert.show();
        }

        return super.onKeyDown(keyCode, event);
    }

    private void saveOcrResult() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("save", 1);
        resultIntent.putExtra(Constants.OCR_RESULT_TEXT_KEY, ocrRecognizedEditText.getText().toString());
        setResult(Activity.RESULT_OK, resultIntent);

        PreviewOcrResultActivity.this.finish();
    }

    private void editOcrResult() {
        ocrRecognizedEditText.setEnabled(true);
        ocrRecognizedEditText.setFocusable(true);
    }

    private void discardOcrResult() {
//        // Delete the photoFilePath from the temporary directory
        Picasso.with(getApplicationContext()).invalidate(photoFilePath);
//        photoFilePath.delete();

        Intent resultIntent = new Intent();
        resultIntent.putExtra("save", 0);
        setResult(Activity.RESULT_OK, resultIntent);

        PreviewOcrResultActivity.this.finish();
    }
}


class BitmapWorkerTask extends AsyncTask<Integer, Void, Bitmap> {
    private ImageView imageView;
    private File photoFilePath;
    private List<Rect> wordBoundingBoxes;

    public BitmapWorkerTask(ImageView imageView, File photoFilePath, List<Rect> wordBoundingBoxes) {
        this.photoFilePath = photoFilePath;
        this.wordBoundingBoxes = wordBoundingBoxes;
        this.imageView = imageView;
    }

    // Decode image in background.
    @Override
    protected Bitmap doInBackground(Integer... params) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = BitmapFactory.decodeFile(photoFilePath.getAbsolutePath(), options);
        return bitmap;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if (wordBoundingBoxes != null && bitmap != null) {
            Bitmap mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
            Bitmap annotatedBitmap = BitmapTools.getAnnotatedBitmap(mutableBitmap, wordBoundingBoxes);
            imageView.setImageBitmap(annotatedBitmap);
        } else {
            imageView.setImageBitmap(bitmap);
        }
    }
}
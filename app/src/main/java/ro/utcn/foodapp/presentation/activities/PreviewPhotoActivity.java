package ro.utcn.foodapp.presentation.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
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

public class PreviewPhotoActivity extends ActionBarActivity {

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

        Intent intent = getIntent();
        photoFilePath = new File((intent.getStringExtra(TEMP_FILE_PATH)));
        photoDirPath = new File(intent.getStringExtra(TEMP_DIR_PATH));
        //ocrResult = (OcrResult) intent.getSerializableExtra(Constants.OCR_RESULT_OBJECT_KEY);
        recognizedText = intent.getStringExtra(Constants.OCR_RESULT_TEXT_KEY);
        wordBoundingBoxes = (List<Rect>) intent.getSerializableExtra(Constants.OCR_WORD_BOUNDING_BOXES_KEY);

        // Used the Picasso library to display and scale the photo to fit in the image view
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = BitmapFactory.decodeFile(photoFilePath.getAbsolutePath(), options);

        if (wordBoundingBoxes != null) {
            Bitmap mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
            Bitmap annotatedBitmap = BitmapTools.getAnnotatedBitmap(mutableBitmap, wordBoundingBoxes);
            imageView.setImageBitmap(annotatedBitmap);
        } else {
            imageView.setImageBitmap(bitmap);
        }

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

    private void saveOcrResult() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("save", 1);
        resultIntent.putExtra(Constants.OCR_RESULT_TEXT_KEY, recognizedText);
        resultIntent.putExtra(this.TEMP_FILE_PATH, photoFilePath);
        setResult(Activity.RESULT_OK, resultIntent);

        PreviewPhotoActivity.this.finish();
    }

    private void editOcrResult() {

    }

    private void discardOcrResult() {
        // Delete the photoFilePath from the temporary directory
        photoFilePath.delete();
        photoDirPath.delete();
        Picasso.with(getApplicationContext()).invalidate(photoFilePath);

        Intent resultIntent = new Intent();
        resultIntent.putExtra("save", 0);
        setResult(Activity.RESULT_OK, resultIntent);

        PreviewPhotoActivity.this.finish();
    }
}
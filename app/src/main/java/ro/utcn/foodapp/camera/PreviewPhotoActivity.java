package ro.utcn.foodapp.camera;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.googlecode.tesseract.android.TessBaseAPI;
import com.squareup.picasso.Picasso;

import java.io.File;

import ro.utcn.foodapp.R;
import ro.utcn.foodapp.ocr.OcrInitAsyncTask;

public class PreviewPhotoActivity extends Activity {
    public static final String TEMP_FILE_PATH = "TEMP_FILE_PATH";
    private ImageView imageView;
    private TextView retakePhoto;
    private TextView usePhoto;
    private ScrollView displayOcrScrollView;
    private TextView displayOcrTextView;
    private File file;
    private Bitmap bitmap;
    private TessBaseAPI tessBaseAPI;
    private String recognizedText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_display_photo);

        imageView = (ImageView) findViewById(R.id.camera_display_photo_container);
        retakePhoto = (TextView) findViewById(R.id.camera_display_retake);
        usePhoto = (TextView) findViewById(R.id.camera_display_use_photo);
        displayOcrTextView = (TextView) findViewById(R.id.ocr_recognized_text);
        displayOcrScrollView = (ScrollView) findViewById(R.id.ocr_recognized_text_scroll_view);
        displayOcrScrollView.setVisibility(View.INVISIBLE);

        Intent intent = getIntent();
        file = new File((intent.getStringExtra(TEMP_FILE_PATH)));

        // Use the Picasso library to display and scale the photo to fit in the image view
        Picasso.with(this)
                .load(file)
                .fit().centerInside()
                .into(imageView);

        retakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Delete the file from the temporary directory
                file.delete();
                Picasso.with(getApplicationContext()).invalidate(file);

                Intent resultIntent = new Intent();

                resultIntent.putExtra("save", 0);
                setResult(Activity.RESULT_OK, resultIntent);

                PreviewPhotoActivity.this.finish();
            }
        });

        usePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tessBaseAPI = new TessBaseAPI();
                bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();

                OcrInitAsyncTask ocrInitAsyncTask = new OcrInitAsyncTask(PreviewPhotoActivity.this, tessBaseAPI, bitmap);
                ocrInitAsyncTask.execute();
            }
        });
    }

    public void displayRecognizedText() {
        Log.d("Recognized text", recognizedText);
        displayOcrScrollView.setVisibility(View.VISIBLE);
        displayOcrTextView.setText(recognizedText);
    }

    @Override
    public void onPause() {
        super.onPause();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (tessBaseAPI != null)
            tessBaseAPI.end();
    }

    public String getRecognizedText() {
        return recognizedText;
    }

    public void setRecognizedText(String recognizedText) {
        this.recognizedText = recognizedText;
    }
}

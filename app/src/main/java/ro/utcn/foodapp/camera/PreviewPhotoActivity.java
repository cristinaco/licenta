package ro.utcn.foodapp.camera;

import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.googlecode.tesseract.android.TessBaseAPI;
import com.squareup.picasso.Picasso;

import java.io.File;

import ro.utcn.foodapp.R;

public class PreviewPhotoActivity extends Activity {
    public static final String TEMP_FILE_PATH = "TEMP_FILE_PATH";
    private ImageView imageView;
    private TextView retakePhoto;
    private TextView usePhoto;
    private File file;
    private TessBaseAPI tessBaseAPI;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_display_photo);

        imageView = (ImageView) findViewById(R.id.camera_display_photo_container);
        retakePhoto = (TextView) findViewById(R.id.camera_display_retake);
        usePhoto = (TextView) findViewById(R.id.camera_display_use_photo);

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
                tessBaseAPI.setDebug(true);


                String datapath = Environment.getExternalStorageDirectory() + "/tesseract/";
                String language = "eng";
                File dir = new File(datapath + "tessdata/");
                if (!dir.exists())
                    dir.mkdirs();
                tessBaseAPI.init(datapath, language);

                BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                tessBaseAPI.setImage(BitmapFactory.decodeFile(file.getAbsolutePath(),bmOptions));
                String recognizedText = tessBaseAPI.getUTF8Text();
                Toast.makeText(getApplicationContext(),recognizedText,Toast.LENGTH_LONG).show();



//                Intent resultIntent = new Intent();
//
//                resultIntent.putExtra("save", 1);
//                setResult(Activity.RESULT_OK, resultIntent);
//
//                PreviewPhotoActivity.this.finish();
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();

    }
    @Override
    public void onDestroy() {
        if (tessBaseAPI != null)
            tessBaseAPI.end();
    }
}

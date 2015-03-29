package ro.utcn.foodapp.presentation.activities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;

import ro.utcn.foodapp.R;
import ro.utcn.foodapp.camera.CameraActivity;
import ro.utcn.foodapp.utils.FileUtil;


public class MainActivity extends ActionBarActivity {
    public static final int TAKE_PICTURE = 1;

    public static final String TEMP_FILE_PATH = "TEMP_FILE_PATH";
    public static final String TEMP_DIR_PATH = "TEMP_DIR_PATH";
    public static final String CURRENT_FILE_PATH = "CURRENT_FILE_PATH";

    private File tempDir;
    private File tempFilePath;

    private Button takePicture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        takePicture = (Button) findViewById(R.id.activity_main_button_take_picture);
        // TODO Create the directory with the user's username
        this.tempDir = new File(FileUtil.getDrTempDir(this), "username");
        this.tempDir.mkdirs();

        setListeners();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setListeners() {
        takePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {

                    final Intent takePictureIntent = new Intent(MainActivity.this, CameraActivity.class);
                    tempFilePath = new File(tempDir, String.valueOf(System.currentTimeMillis() + ".jpg"));
                    takePictureIntent.putExtra(TEMP_FILE_PATH, tempFilePath.getAbsolutePath());
                    takePictureIntent.putExtra(TEMP_DIR_PATH, tempDir.getAbsolutePath());

                    tempFilePath.getParentFile().mkdirs();
                    //takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(InputDefectDataFragment.this.tempFilePath));

                    startActivityForResult(takePictureIntent, TAKE_PICTURE);
                } else {
                    Toast.makeText(MainActivity.this, R.string.no_camera_available, Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}

package ro.utcn.foodapp.presentation.activities;

import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.melnykov.fab.FloatingActionButton;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import ro.utcn.foodapp.R;
import ro.utcn.foodapp.camera.CameraActivity;
import ro.utcn.foodapp.model.Product;
import ro.utcn.foodapp.utils.FileUtil;

public class AddProductActivity extends ActionBarActivity {

    public static final int TAKE_PICTURE = 1;
    public static final String TEMP_FILE_PATH = "TEMP_FILE_PATH";
    public static final String TEMP_DIR_PATH = "TEMP_DIR_PATH";

    private File tempDir;
    private File tempFilePath;
    private FloatingActionButton takePicture;
    private MaterialEditText productNameEditText;
    private MaterialEditText productDescriptionEditText;
    private TextView productExpDateEditText;
    private TextView productExpStatusEditText;

    private Product newProduct;
    private List<String> urls;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);
        ActionBar ab = getSupportActionBar();
        ab.setTitle(R.string.title_activity_add_product);
        takePicture = (FloatingActionButton) findViewById(R.id.activity_add_product_button_take_picture);
        productNameEditText = (MaterialEditText) findViewById(R.id.activity_add_product_name);
        productDescriptionEditText = (MaterialEditText) findViewById(R.id.activity_add_product_description);
        productExpDateEditText = (TextView) findViewById(R.id.activity_add_product_exp_date);
        productExpStatusEditText = (TextView) findViewById(R.id.activity_add_product_exp_status);

        // TODO Create the directory with the user's username or with the product uid/name
        this.tempDir = new File(FileUtil.getDrTempDir(this), "username");
        this.tempDir.mkdirs();

        newProduct = new Product();
        newProduct.setUid(String.valueOf(UUID.randomUUID()));
        urls = new ArrayList<>();
        setListeners();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_product, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_save) {
            saveProduct();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == TAKE_PICTURE){
            if(resultCode == RESULT_OK){
                urls.add(this.tempFilePath.getAbsolutePath());
            }
        }
    }

    private void saveProduct() {


    }

    private void setListeners() {
        takePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Camera.getNumberOfCameras() > 0) {
                    final Intent takePictureIntent = new Intent(AddProductActivity.this, CameraActivity.class);
                    tempFilePath = new File(tempDir, String.valueOf(System.currentTimeMillis() + ".jpg"));
                    takePictureIntent.putExtra(TEMP_FILE_PATH, tempFilePath.getAbsolutePath());
                    takePictureIntent.putExtra(TEMP_DIR_PATH, tempDir.getAbsolutePath());
                    tempFilePath.getParentFile().mkdirs();

                    startActivityForResult(takePictureIntent, TAKE_PICTURE);
                } else {
                    Toast.makeText(AddProductActivity.this, R.string.no_camera_available, Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}

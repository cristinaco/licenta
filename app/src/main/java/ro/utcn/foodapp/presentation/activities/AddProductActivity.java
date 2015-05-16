package ro.utcn.foodapp.presentation.activities;

import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.melnykov.fab.FloatingActionButton;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import ro.utcn.foodapp.R;
import ro.utcn.foodapp.business.PhotoPathManager;
import ro.utcn.foodapp.business.ProductManager;
import ro.utcn.foodapp.business.RegistrationManager;
import ro.utcn.foodapp.model.Product;
import ro.utcn.foodapp.utils.Constants;
import ro.utcn.foodapp.utils.FileUtil;

public class AddProductActivity extends ActionBarActivity {

    public static final int TAKE_PICTURE = 1;
    public static final String TEMP_FILE_PATH = "TEMP_FILE_PATH";
    public static final String TEMP_DIR_PATH = "TEMP_DIR_PATH";

    private File tempDir;
    private File tempFilePath;
    private File productNameDir;
    private File productIngredientsDir;
    private File productExpirationDateDir;
    private FloatingActionButton takePicture;
    private EditText productNameEditText;
    private EditText productIngredientsEditText;
    private EditText productExpirationDateEditText;
    private EditText productPiecesNumberEditText;
    private ImageView productNameCamBtn;
    private ImageView productIngredientsCamBtn;
    private ImageView productExpirationDateCamBtn;
    private ImageView productPiecesNumberCamBtn;
    private Product newProduct;
    private String productUUID = null;
    private String ocrForAction = "";
    private String timestamp;
    private Map<String, String> productPhotoPaths;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);
        setTitle(getResources().getString(R.string.activity_add_product_title));

        takePicture = (FloatingActionButton) findViewById(R.id.activity_add_product_button_take_picture);
        productNameEditText = (EditText) findViewById(R.id.activity_add_product_name_edit_text);
        productIngredientsEditText = (EditText) findViewById(R.id.activity_add_product_ingredients_edit_text);
        productExpirationDateEditText = (EditText) findViewById(R.id.activity_add_product_expiration_date_edit_text);
        productPiecesNumberEditText = (EditText) findViewById(R.id.activity_add_product_pieces_number_edit_text);
        productNameCamBtn = (ImageView) findViewById(R.id.activity_add_product_name_cam_btn);
        productIngredientsCamBtn = (ImageView) findViewById(R.id.activity_add_product_ingredients_cam_btn);
        productExpirationDateCamBtn = (ImageView) findViewById(R.id.activity_add_product_expiration_date_cam_btn);
        productPiecesNumberCamBtn = (ImageView) findViewById(R.id.activity_add_product_pieces_number_cam_btn);


        // TODO Create the directory with the user's username or with the product uid/name
        productUUID = String.valueOf(UUID.randomUUID());
        this.tempDir = new File(FileUtil.getDrTempDir(this), productUUID);
        this.tempDir.mkdirs();

        newProduct = new Product();
        newProduct.setUuid(productUUID);
        productPhotoPaths = new HashMap<>();
        timestamp = String.valueOf(System.currentTimeMillis());
        setListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (timestamp == null) {
            timestamp = String.valueOf(System.currentTimeMillis());
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString("ocrForAction", ocrForAction);
        outState.putString(this.TEMP_FILE_PATH, this.tempFilePath.getAbsolutePath());
        outState.putString(this.TEMP_DIR_PATH, this.tempDir.getAbsolutePath());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        ocrForAction = savedInstanceState.getString("ocrForAction");
        tempFilePath = new File(savedInstanceState.getString(this.TEMP_FILE_PATH));
        tempDir = new File(savedInstanceState.getString(this.TEMP_DIR_PATH));
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
        if (id == R.id.action_save_product_data) {
            saveProduct();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == TAKE_PICTURE) {
            if (resultCode == RESULT_OK) {
                int value = data.getExtras().getInt("save");
                if (value == 1) {
                    if (ocrForAction.equals("name")) {
                        saveProductName(data);
                    } else if (ocrForAction.equals("ingredients")) {
                        saveProductIngredients(data);
                    } else if (ocrForAction.equals("expirationDate")) {
                        saveProductExpirationDate(data);
                    }
                }
            }
        }
    }

    private void saveProductName(Intent data) {
        newProduct.setName(data.getStringExtra(Constants.OCR_RESULT_TEXT_KEY));
        productPhotoPaths.put(Constants.PRODUCT_NAME_PHOTO_PATH_KEY, data.getStringExtra(this.tempFilePath.getAbsolutePath()));
        productNameEditText.setText(newProduct.getName());
    }

    private void saveProductIngredients(Intent data) {
        newProduct.setIngredients(data.getStringExtra(Constants.OCR_RESULT_TEXT_KEY));
        productPhotoPaths.put(Constants.PRODUCT_INGREDIENTS_PHOTO_PATH_KEY, data.getStringExtra(this.tempFilePath.getAbsolutePath()));
        productIngredientsEditText.setText(newProduct.getIngredients());
    }

    private void saveProductExpirationDate(Intent data) {
        Date date = new Date();
        //newProduct.setExpirationDate(data.getStringExtra(Constants.OCR_RESULT_EXPIRATION_DATE_KEY));
        newProduct.setExpirationDate(date);
        productPhotoPaths.put(Constants.PRODUCT_EPIRATION_DATE_PHOTO_PATH_KEY, data.getStringExtra(this.tempFilePath.getAbsolutePath()));
        productExpirationDateEditText.setText(newProduct.getExpirationDate().toString());
    }

    private void saveProduct() {
//        if (newProduct.getName() == null || newProduct.getIngredients() == null || newProduct.getExpirationDate() == null) {
//            Toast.makeText(this, getResources().getString(R.string.activity_add_product_complete_all_fields), Toast.LENGTH_SHORT).show();
//        } else {
//            List<String> urls = new ArrayList<>();
//            for (String url : productPhotoPaths.keySet()) {
//                urls.add(url);
//            }
//            newProduct.setUrls(urls);
//            newProduct.setExpirationStatus(Constants.PRODUCT_EXPIRATION_STATUS_EXPIRED);
//            ProductManager.getInstance().saveProduct(newProduct);
//        }
        List<String> urls = new ArrayList<>();
        for (Map.Entry<String, String> entry : productPhotoPaths.entrySet()) {
            urls.add(entry.getValue());
        }

        newProduct.setUrls(urls);
        newProduct.setExpirationStatus(Constants.PRODUCT_EXPIRATION_STATUS_EXPIRED);
        newProduct.setPiecesNumber(Integer.parseInt(productPiecesNumberEditText.getText().toString()));
        long rowId = ProductManager.getInstance().saveProduct(newProduct);
        Calendar regDate = Calendar.getInstance();
        regDate.setTime(new Date());
        RegistrationManager.getInstance().saveRegistration(regDate.getTime(), rowId);
        for (String url : urls) {
            PhotoPathManager.getInstance().savePhotoPath(url, rowId);
        }


    }

    private void setListeners() {

        productNameCamBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Camera.getNumberOfCameras() > 0) {
                    ocrForAction = "name";
                    final Intent takePictureIntent = new Intent(AddProductActivity.this, CaptureActivity.class);
                    productNameDir = new File(tempDir, Constants.PRODUCT_NAME_DIRECTORY);
                    productNameDir.mkdirs();
                    tempFilePath = new File(productNameDir, timestamp + Constants.UNDERSCORE + ocrForAction + ".jpg");
                    takePictureIntent.putExtra(TEMP_FILE_PATH, tempFilePath.getAbsolutePath());
                    takePictureIntent.putExtra(TEMP_DIR_PATH, productNameDir.getAbsolutePath());
                    tempFilePath.getParentFile().mkdirs();

                    startActivityForResult(takePictureIntent, TAKE_PICTURE);
                } else {
                    Toast.makeText(AddProductActivity.this, R.string.no_camera_available, Toast.LENGTH_LONG).show();
                }
            }
        });
        productIngredientsCamBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Camera.getNumberOfCameras() > 0) {
                    ocrForAction = "ingredients";
                    final Intent takePictureIntent = new Intent(AddProductActivity.this, CaptureActivity.class);
                    productIngredientsDir = new File(tempDir, Constants.PRODUCT_INGREDIENTS_DIRECTORY);
                    productIngredientsDir.mkdirs();
                    tempFilePath = new File(productIngredientsDir, timestamp + Constants.UNDERSCORE + ocrForAction + ".jpg");
                    takePictureIntent.putExtra(TEMP_FILE_PATH, tempFilePath.getAbsolutePath());
                    takePictureIntent.putExtra(TEMP_DIR_PATH, productIngredientsDir.getAbsolutePath());
                    tempFilePath.getParentFile().mkdirs();

                    startActivityForResult(takePictureIntent, TAKE_PICTURE);
                } else {
                    Toast.makeText(AddProductActivity.this, R.string.no_camera_available, Toast.LENGTH_LONG).show();
                }
            }
        });
        productExpirationDateCamBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Camera.getNumberOfCameras() > 0) {
                    ocrForAction = "expirationDate";
                    final Intent takePictureIntent = new Intent(AddProductActivity.this, CaptureActivity.class);
                    productExpirationDateDir = new File(tempDir, Constants.PRODUCT_EXPIRATION_DATE_DIRECTORY);
                    productExpirationDateDir.mkdirs();
                    tempFilePath = new File(productExpirationDateDir, timestamp + Constants.UNDERSCORE + ocrForAction + ".jpg");
                    takePictureIntent.putExtra(TEMP_FILE_PATH, tempFilePath.getAbsolutePath());
                    takePictureIntent.putExtra(TEMP_DIR_PATH, productExpirationDateDir.getAbsolutePath());
                    tempFilePath.getParentFile().mkdirs();

                    startActivityForResult(takePictureIntent, TAKE_PICTURE);
                } else {
                    Toast.makeText(AddProductActivity.this, R.string.no_camera_available, Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}

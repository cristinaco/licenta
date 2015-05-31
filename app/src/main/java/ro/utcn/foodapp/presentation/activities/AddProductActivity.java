package ro.utcn.foodapp.presentation.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
import ro.utcn.foodapp.model.Registration;
import ro.utcn.foodapp.utils.Constants;
import ro.utcn.foodapp.utils.DateUtils;
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
    private File productDepictingPhotosDir;
    private EditText productNameEditText;
    private EditText productIngredientsEditText;
    private EditText productExpirationDateEditText;
    private EditText productPiecesNumberEditText;
    private ImageView productNameCamBtn;
    private ImageView productIngredientsCamBtn;
    private ImageView productExpirationDateCamBtn;
    private ImageView productDepicting1;
    private ImageView productDepicting2;
    private ImageView productDepicting3;
    private Product newProduct;
    private Registration registration;
    private String registrationUuid = null;
    private String ocrForAction = "";
    private String timestamp;
    private Map<String, String> productPhotoPaths;

    public static boolean isNumeric(String str) {
        return str.matches("-?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);
        setTitle(getResources().getString(R.string.activity_add_product_title));

        productNameEditText = (EditText) findViewById(R.id.activity_add_product_name_edit_text);
        productIngredientsEditText = (EditText) findViewById(R.id.activity_add_product_ingredients_edit_text);
        productExpirationDateEditText = (EditText) findViewById(R.id.activity_add_product_expiration_date_edit_text);
        productPiecesNumberEditText = (EditText) findViewById(R.id.activity_add_product_pieces_number_edit_text);
        productNameCamBtn = (ImageView) findViewById(R.id.activity_add_product_name_cam_btn);
        productIngredientsCamBtn = (ImageView) findViewById(R.id.activity_add_product_ingredients_cam_btn);
        productExpirationDateCamBtn = (ImageView) findViewById(R.id.activity_add_product_expiration_date_cam_btn);
        productDepicting1 = (ImageView) findViewById(R.id.photo_depicting_product_1);
        productDepicting2 = (ImageView) findViewById(R.id.photo_depicting_product_2);
        productDepicting3 = (ImageView) findViewById(R.id.photo_depicting_product_3);

        productNameEditText.setEnabled(false);
        productIngredientsEditText.setEnabled(false);
        productExpirationDateEditText.setEnabled(false);

        ScrollView view = (ScrollView) findViewById(R.id.scrollview);
        view.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
        view.setFocusable(true);
        view.setFocusableInTouchMode(true);

        // TODO Create the directory with the user's username or with the product uid/name
        registrationUuid = String.valueOf(UUID.randomUUID());
        this.tempDir = new File(FileUtil.getDrTempDir(this), registrationUuid);
        this.tempDir.mkdirs();
        productDepictingPhotosDir = new File(tempDir, Constants.PRODUCT_DEPICTING_PHOTOS);
        productDepictingPhotosDir.mkdirs();

        newProduct = new Product();
        registration = new Registration();
        registration.setUuid(registrationUuid);
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
        if(this.tempFilePath!=null){
            outState.putString(this.TEMP_FILE_PATH, this.tempFilePath.getAbsolutePath());
        }
        outState.putString(this.TEMP_DIR_PATH, this.tempDir.getAbsolutePath());
        outState.putString("productName", newProduct.getName());
        outState.putString("productIngredients", newProduct.getIngredients());
        if (newProduct.getExpirationDate() != null) {
            outState.putLong("productExpirationDate", newProduct.getExpirationDate().getTime());
        }
        outState.putLong("productPiecesNumber", newProduct.getPiecesNumber());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        ocrForAction = savedInstanceState.getString("ocrForAction");
        tempFilePath = new File(savedInstanceState.getString(this.TEMP_FILE_PATH));
        tempDir = new File(savedInstanceState.getString(this.TEMP_DIR_PATH));
        newProduct.setName(savedInstanceState.getString("productName"));
        newProduct.setIngredients(savedInstanceState.getString("productIngredients"));
        newProduct.setExpirationDate(new Date(savedInstanceState.getLong("productExpirationDate")));
        newProduct.setPiecesNumber((int) savedInstanceState.getLong("productPiecesNumber"));
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
                    } else if (ocrForAction.equals("depicting1")) {
                        savePic1(data);
                    } else if (ocrForAction.equals("depicting2")) {
                        savePic2(data);
                    } else if (ocrForAction.equals("depicting3")) {
                        savePic3(data);
                    }
                }
            }
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
                    discardProductData(tempDir);
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

    private void discardProductData(File dir) {
        if (dir.isDirectory())
            for (File child : dir.listFiles())
                discardProductData(child);

        dir.delete();
    }

    private void savePic3(Intent data) {
        Picasso.with(this).invalidate(this.tempFilePath);
        Picasso.with(this).load(this.tempFilePath).fit().into(productDepicting3);
    }

    private void savePic2(Intent data) {
        Picasso.with(this).invalidate(this.tempFilePath);
        Picasso.with(this).load(this.tempFilePath).fit().into(productDepicting2);
    }

    private void savePic1(Intent data) {
        Picasso.with(this).invalidate(this.tempFilePath);
        Picasso.with(this).load(this.tempFilePath).fit().into(productDepicting1);
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
        productExpirationDateEditText.setText(data.getStringExtra(Constants.OCR_RESULT_TEXT_KEY));
    }

    private void saveProduct() {
        if (productNameEditText.getText() == null || productIngredientsEditText.getText() == null || productExpirationDateEditText.getText() == null || productPiecesNumberEditText.getText() == null) {
            Toast.makeText(this, getResources().getString(R.string.activity_add_product_complete_all_fields), Toast.LENGTH_SHORT).show();
        } else {
            Date date = isExpirationDateValid(productExpirationDateEditText.getText().toString());
            if (date != null) {

                newProduct.setExpirationDate(date);

                if (newProduct.getExpirationDate().before(new Date())) {
                    newProduct.setExpirationStatus(Constants.PRODUCT_EXPIRATION_STATUS_EXPIRED);
                } else {
                    newProduct.setExpirationStatus(Constants.PRODUCT_EXPIRATION_STATUS_VALID);
                }
                productPhotoPaths.put(Constants.PRODUCT_EXPIRATION_DATE_PHOTO_PATH_KEY, "");

                List<String> urls = new ArrayList<>();
                for (String url : productPhotoPaths.keySet()) {
                    urls.add(url);
                }
                newProduct.setUrls(urls);
                newProduct.setPiecesNumber(Integer.parseInt(productPiecesNumberEditText.getText().toString()));
                long rowId = ProductManager.getInstance().saveProduct(newProduct);
                Calendar regDate = Calendar.getInstance();
                regDate.setTime(new Date());
                RegistrationManager.getInstance().saveRegistration(registrationUuid, regDate.getTime(), rowId);
                for (String url : urls) {
                    PhotoPathManager.getInstance().savePhotoPath(url, rowId);
                }
                this.finish();
            } else {
                Toast.makeText(this, "Invalid date format! Take a new photo or edit it manually", Toast.LENGTH_LONG).show();
                productExpirationDateEditText.setEnabled(true);
            }

        }
    }

    private Date isExpirationDateValid(String stringDate) {
        Map<Integer, SimpleDateFormat> dateFormats = DateUtils.getInstance().getDateFormats();
        for (Map.Entry<Integer, SimpleDateFormat> entry : dateFormats.entrySet()) {
            try {
                Date date = entry.getValue().parse(stringDate);
                return date;
            } catch (ParseException e) {
                continue;
            }
        }
        return null;
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
                    takePictureIntent.putExtra(String.valueOf(Constants.PERFORM_OCR), true);
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
                    takePictureIntent.putExtra(String.valueOf(Constants.PERFORM_OCR), true);
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
                    takePictureIntent.putExtra(String.valueOf(Constants.PERFORM_OCR), true);
                    tempFilePath.getParentFile().mkdirs();

                    startActivityForResult(takePictureIntent, TAKE_PICTURE);
                } else {
                    Toast.makeText(AddProductActivity.this, R.string.no_camera_available, Toast.LENGTH_LONG).show();
                }
            }
        });
        productDepicting1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Camera.getNumberOfCameras() > 0) {
                    ocrForAction = "depicting1";
                    final Intent takePictureIntent = new Intent(AddProductActivity.this, CaptureActivity.class);
                    tempFilePath = new File(productDepictingPhotosDir, timestamp + Constants.UNDERSCORE + ocrForAction + ".jpg");
                    takePictureIntent.putExtra(TEMP_FILE_PATH, tempFilePath.getAbsolutePath());
                    takePictureIntent.putExtra(TEMP_DIR_PATH, productDepictingPhotosDir.getAbsolutePath());
                    takePictureIntent.putExtra(String.valueOf(Constants.PERFORM_OCR), false);
                    tempFilePath.getParentFile().mkdirs();

                    startActivityForResult(takePictureIntent, TAKE_PICTURE);
                } else {
                    Toast.makeText(AddProductActivity.this, R.string.no_camera_available, Toast.LENGTH_LONG).show();
                }
            }
        });
        productDepicting2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Camera.getNumberOfCameras() > 0) {
                    ocrForAction = "depicting2";
                    final Intent takePictureIntent = new Intent(AddProductActivity.this, CaptureActivity.class);
                    tempFilePath = new File(productDepictingPhotosDir, timestamp + Constants.UNDERSCORE + ocrForAction + ".jpg");
                    takePictureIntent.putExtra(TEMP_FILE_PATH, tempFilePath.getAbsolutePath());
                    takePictureIntent.putExtra(TEMP_DIR_PATH, productDepictingPhotosDir.getAbsolutePath());
                    takePictureIntent.putExtra(String.valueOf(Constants.PERFORM_OCR), false);
                    tempFilePath.getParentFile().mkdirs();

                    startActivityForResult(takePictureIntent, TAKE_PICTURE);
                } else {
                    Toast.makeText(AddProductActivity.this, R.string.no_camera_available, Toast.LENGTH_LONG).show();
                }
            }
        });
        productDepicting3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Camera.getNumberOfCameras() > 0) {
                    ocrForAction = "depicting3";
                    final Intent takePictureIntent = new Intent(AddProductActivity.this, CaptureActivity.class);
                    tempFilePath = new File(productDepictingPhotosDir, timestamp + Constants.UNDERSCORE + ocrForAction + ".jpg");
                    takePictureIntent.putExtra(TEMP_FILE_PATH, tempFilePath.getAbsolutePath());
                    takePictureIntent.putExtra(TEMP_DIR_PATH, productDepictingPhotosDir.getAbsolutePath());
                    takePictureIntent.putExtra(String.valueOf(Constants.PERFORM_OCR), false);
                    tempFilePath.getParentFile().mkdirs();

                    startActivityForResult(takePictureIntent, TAKE_PICTURE);
                } else {
                    Toast.makeText(AddProductActivity.this, R.string.no_camera_available, Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}

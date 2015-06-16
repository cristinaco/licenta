package ro.utcn.foodapp.access.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ro.utcn.foodapp.utils.ApplicationContext;

/**
 * Represents the database manager and it's used to implement the methods for CRUD operations
 * Created by coponipi on 19.04.2015.
 */
public class DatabaseManager {
    private DbHelper dbHelper;

    private DatabaseManager() {

        dbHelper = new DbHelper(ApplicationContext.context());
    }

    /**
     * Returns a new instance of the class
     *
     * @return created instance
     */
    public static DatabaseManager getInstance() {
        return new DatabaseManager();
    }

    public long saveProduct(ro.utcn.foodapp.model.Product newProduct) {
        // Open connection to database
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(Product.COLUMN_NAME_NAME, newProduct.getName());
        values.put(Product.COLUMN_NAME_INGREDIENTS, newProduct.getIngredients());
        values.put(Product.COLUMN_NAME_EXPIRATION_DATE, newProduct.getExpirationDate().getTime());
        values.put(Product.COLUMN_NAME_EXPIRATION_STATUS, newProduct.getExpirationStatus());

        // Insert the new row, returning the primary key value of the new row
        long newRowId = db.insert(
                Product.TABLE_NAME,
                DbHelper.COLUMN_NAME_NULLABLE,
                values);

        if (newRowId == -1) {
            Log.e("INSERT INTO PRODUCT", "Values couldn't be inserted. An error has occurred");
        }
        db.close();
        // Return the row ID
        return newRowId;
    }

    public long saveRegistration(String productUUID, Date date, long productId, int itemsNumber) {
        // Open connection to database
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(Registration.COLUMN_NAME_UUID, productUUID);
        values.put(Registration.COLUMN_NAME_REGISTRATION_DATE, date.getTime());
        values.put(Registration.COLUMN_NAME_PRODUCT_ID, productId);
        values.put(Registration.COLUMN_NAME_ITEMS_NUMBER, itemsNumber);

        // Insert the new row, returning the primary key value of the new row
        long newRowId = db.insert(
                Registration.TABLE_NAME,
                DbHelper.COLUMN_NAME_NULLABLE,
                values);

        if (newRowId == -1) {
            Log.e("INSERT TO REGISTRATION", "Values couldn't be inserted. An error has occurred");
        }
        db.close();
        // Return the row ID
        return newRowId;
    }

    public long savePhotoPath(String url, long productId) {
        // Open connection to database
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(PhotoPath.COLUMN_NAME_PATH, url);
        values.put(PhotoPath.COLUMN_PRODUCT_ID, productId);

        // Insert the new row, returning the primary key value of the new row
        long newRowId = db.insert(
                PhotoPath.TABLE_NAME,
                DbHelper.COLUMN_NAME_NULLABLE,
                values);

        if (newRowId == -1) {
            Log.e("INSERT INTO PATH", "Values couldn't be inserted. An error has occurred");
        }

        db.close();
        // Return the row ID
        return newRowId;
    }

    public List<ro.utcn.foodapp.model.Registration> getAllRegistrations() {
        // Open connection to database
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<ro.utcn.foodapp.model.Registration> registrations = new ArrayList<>();

        // Projection specifies which columns from the database
        // will be used after this query.
        String[] projection = {
                Registration._ID,
                Registration.COLUMN_NAME_UUID,
                Registration.COLUMN_NAME_REGISTRATION_DATE,
                Registration.COLUMN_NAME_ITEMS_NUMBER,
                Registration.COLUMN_NAME_PRODUCT_ID};

        Cursor cursor = db.query(
                Registration.TABLE_NAME,                    //the table to query
                projection,                                 //the columns to return
                null,                                       //the columns for the WHERE clause
                null,                                       //the values for the WHERE clause
                null,                                       //don't group the rows
                null,                                       //don't filter by row groups
                null);                                      //the sort order

        // Iterated through records that were found
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                ro.utcn.foodapp.model.Registration registration = new ro.utcn.foodapp.model.Registration();
                Long regDate = cursor.getLong(cursor.getColumnIndex(Registration.COLUMN_NAME_REGISTRATION_DATE));
                Date date = new Date();
                date.setTime(regDate);
                registration.setId(cursor.getInt(cursor.getColumnIndex(Registration._ID)));
                registration.setUuid(cursor.getString(cursor.getColumnIndex(Registration.COLUMN_NAME_UUID)));
                registration.setRegistrationDate(date);
                registration.setProductId(cursor.getInt(cursor.getColumnIndex(Registration.COLUMN_NAME_PRODUCT_ID)));
                registration.setItemsNumber(cursor.getInt(cursor.getColumnIndex(Registration.COLUMN_NAME_ITEMS_NUMBER)));
                registrations.add(registration);
            }
        } else {
            Log.e("DatabaseManager ", "No records found!");
        }

        // Close cursor and connection to database
        cursor.close();
        db.close();

        // Return the list with all bookings
        return registrations;
    }

    public ro.utcn.foodapp.model.Product getProduct(int productId) {
        ro.utcn.foodapp.model.Product product = new ro.utcn.foodapp.model.Product();
        // Open connection to database
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Projection that specifies which columns from the database
        // will be used after this query.
        String[] projection = {
                Product._ID,
                Product.COLUMN_NAME_NAME,
                Product.COLUMN_NAME_INGREDIENTS,
                Product.COLUMN_NAME_EXPIRATION_DATE,
                Product.COLUMN_NAME_EXPIRATION_STATUS};

        String selection = Product._ID + " = ?";
        String[] selectionArgs = {String.valueOf(productId)};

        Cursor cursor = db.query(
                Product.TABLE_NAME,                    //the table to query
                projection,                             //the columns to return
                selection,                              //the columns for the WHERE clause
                selectionArgs,                          //the values for the WHERE clause
                null,                                   //don't group the rows
                null,                                   //don't filter by row groups
                null);                                  //the sort order
        // Iterated through records that were found
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                product.setId(cursor.getInt(cursor.getColumnIndex(Product._ID)));
                product.setName(cursor.getString(cursor.getColumnIndex(Product.COLUMN_NAME_NAME)));
                product.setIngredients(cursor.getString(cursor.getColumnIndex(Product.COLUMN_NAME_INGREDIENTS)));
                product.setExpirationStatus(cursor.getString(cursor.getColumnIndex(Product.COLUMN_NAME_EXPIRATION_STATUS)));
                Long expDate = cursor.getLong(cursor.getColumnIndex(Product.COLUMN_NAME_EXPIRATION_DATE));
                Date date = new Date();
                date.setTime(expDate);
                product.setExpirationDate(date);
                product.setUrls(getPhotoUrlsForProduct(product.getId()));
            }
        } else {
            Log.e("DatabaseManager ", "No records found!");
        }

        // Close cursor and connection to database
        cursor.close();
        db.close();

        // Return the list with all bookings
        return product;
    }

    private List<String> getPhotoUrlsForProduct(int productId) {
        // Open connection to database
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        List<String> urls = new ArrayList<>();

        // Projection that specifies which columns from the database
        // will be used after this query.
        String[] projection = {
                PhotoPath._ID,
                PhotoPath.COLUMN_NAME_PATH,
                PhotoPath.COLUMN_PRODUCT_ID};

        String selection = PhotoPath.COLUMN_PRODUCT_ID + " = ?";
        String[] selectionArgs = {String.valueOf(productId)};

        Cursor cursor = db.query(
                PhotoPath.TABLE_NAME,                    //the table to query
                projection,                             //the columns to return
                selection,                              //the columns for the WHERE clause
                selectionArgs,                          //the values for the WHERE clause
                null,                                   //don't group the rows
                null,                                   //don't filter by row groups
                null);                                  //the sort order
        // Iterated through records that were found
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                urls.add(cursor.getString(cursor.getColumnIndex(PhotoPath.COLUMN_NAME_PATH)));
            }
        } else {
            Log.e("DatabaseManager ", "No records found!");
        }

        // Close cursor and connection to database
        cursor.close();
        db.close();
        return urls;
    }

    public void deleteAllProducts() {
        // Open connection to database
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Execute queries
        db.execSQL("DELETE FROM " + Product.TABLE_NAME);

        //Close connection to database
        db.close();
    }

    public void deleteProduct(int id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL("DELETE FROM " + Product.TABLE_NAME + " WHERE " + Product._ID + "=" + id);
        //Close connection to database
        db.close();
    }

    public void deleteRegistration(int id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL("DELETE FROM " + Registration.TABLE_NAME + " WHERE " + Registration._ID + "=" + id);
        //Close connection to database
        db.close();
    }

    public void updateProduct(ro.utcn.foodapp.model.Product product) {
        // Open connection to database
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(Product.COLUMN_NAME_NAME, product.getName());
        values.put(Product.COLUMN_NAME_INGREDIENTS, product.getIngredients());
        values.put(Product.COLUMN_NAME_EXPIRATION_DATE, product.getExpirationDate().getTime());
        values.put(Product.COLUMN_NAME_EXPIRATION_STATUS, product.getExpirationStatus());

        // Insert the new row, returning the primary key value of the new row
        db.update(
                Product.TABLE_NAME,
                values,
                Product._ID + "=" + product.getId(),
                null);

        db.close();
    }

    public void updateRegistration(String registrationUuid, Date date, int productId, int itemsNumber) {
        // Open connection to database
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(Registration.COLUMN_NAME_REGISTRATION_DATE, date.getTime());
        values.put(Registration.COLUMN_NAME_ITEMS_NUMBER, itemsNumber);

        // Insert the new row, returning the primary key value of the new row
        db.update(
                Registration.TABLE_NAME,
                values,
                Registration.COLUMN_NAME_UUID + "='" + registrationUuid+"'",
                null);

        db.close();
    }

    public ro.utcn.foodapp.model.Registration getRegistration(String uuid) {
        // Open connection to database
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        ro.utcn.foodapp.model.Registration registration = new ro.utcn.foodapp.model.Registration();

        // Projection specifies which columns from the database
        // will be used after this query.
        String[] projection = {
                Registration._ID,
                Registration.COLUMN_NAME_UUID,
                Registration.COLUMN_NAME_REGISTRATION_DATE,
                Registration.COLUMN_NAME_ITEMS_NUMBER,
                Registration.COLUMN_NAME_PRODUCT_ID};
        String selection = Registration.COLUMN_NAME_UUID + " = ?";
        String[] selectionArgs = {uuid};
        Cursor cursor = db.query(
                Registration.TABLE_NAME,                    //the table to query
                projection,                                 //the columns to return
                selection,                                       //the columns for the WHERE clause
                selectionArgs,                                       //the values for the WHERE clause
                null,                                       //don't group the rows
                null,                                       //don't filter by row groups
                null);                                      //the sort order

        // Iterated through records that were found
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                Long regDate = cursor.getLong(cursor.getColumnIndex(Registration.COLUMN_NAME_REGISTRATION_DATE));
                Date date = new Date();
                date.setTime(regDate);
                registration.setId(cursor.getInt(cursor.getColumnIndex(Registration._ID)));
                registration.setUuid(cursor.getString(cursor.getColumnIndex(Registration.COLUMN_NAME_UUID)));
                registration.setRegistrationDate(date);
                registration.setItemsNumber(cursor.getInt(cursor.getColumnIndex(Registration.COLUMN_NAME_ITEMS_NUMBER)));
                registration.setProductId(cursor.getInt(cursor.getColumnIndex(Registration.COLUMN_NAME_PRODUCT_ID)));
            }
        } else {
            Log.e("DatabaseManager ", "No records found!");
        }

        // Close cursor and connection to database
        cursor.close();
        db.close();

        // Return the list with all bookings
        return registration;
    }
}

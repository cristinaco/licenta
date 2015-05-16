package ro.utcn.foodapp.access.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

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
        values.put(Product.COLUMN_NAME_UUID, newProduct.getUuid());
        values.put(Product.COLUMN_NAME_NAME, newProduct.getName());
        values.put(Product.COLUMN_NAME_INGREDIENTS, newProduct.getIngredients());
        values.put(Product.COLUMN_NAME_EXPIRATION_DATE, newProduct.getExpirationDate().getTime());
        values.put(Product.COLUMN_NAME_PIECES_NUMBER, newProduct.getPiecesNumber());
        values.put(Product.COLUMN_NAME_EXPIRATION_STATUS, newProduct.getExpirationStatus());

        // Insert the new row, returning the primary key value of the new row
        long newRowId = db.insert(
                Product.TABLE_NAME,
                DbHelper.COLUMN_NAME_NULLABLE,
                values);

        if (newRowId == -1) {
            Log.e("INSERT INTO PRODUCT", "Values couldn't be inserted. An error has occurred");
        }

        // Return the row ID
        return newRowId;
    }

    public long saveRegistration(Date date, long productId) {
        // Open connection to database
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(Registration.COLUMN_NAME_REGISTRATION_DATE, date.getTime());
        values.put(Registration.COLUMN_NAME_PRODUCT_ID, productId);

        // Insert the new row, returning the primary key value of the new row
        long newRowId = db.insert(
                Registration.TABLE_NAME,
                DbHelper.COLUMN_NAME_NULLABLE,
                values);

        if (newRowId == -1) {
            Log.e("INSERT TO REGISTRATION", "Values couldn't be inserted. An error has occurred");
        }

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
                Registration.COLUMN_NAME_REGISTRATION_DATE,
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
                registration.setRegistrationDate(date);
                registration.setProductId(cursor.getInt(cursor.getColumnIndex(Registration.COLUMN_NAME_PRODUCT_ID)));
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
                Product.COLUMN_NAME_UUID,
                Product.COLUMN_NAME_NAME,
                Product.COLUMN_NAME_INGREDIENTS,
                Product.COLUMN_NAME_EXPIRATION_DATE,
                Product.COLUMN_NAME_PIECES_NUMBER,
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
                product.setUuid(cursor.getString(cursor.getColumnIndex(Product.COLUMN_NAME_UUID)));
                product.setName(cursor.getString(cursor.getColumnIndex(Product.COLUMN_NAME_NAME)));
                product.setIngredients(cursor.getString(cursor.getColumnIndex(Product.COLUMN_NAME_INGREDIENTS)));
                product.setPiecesNumber(cursor.getInt(cursor.getColumnIndex(Product.COLUMN_NAME_PIECES_NUMBER)));
                product.setExpirationStatus(cursor.getString(cursor.getColumnIndex(Product.COLUMN_NAME_EXPIRATION_STATUS)));
                Long expDate = cursor.getLong(cursor.getColumnIndex(Product.COLUMN_NAME_EXPIRATION_DATE));
                Date date = new Date();
                date.setTime(expDate);
                product.setExpirationDate(date);
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
}

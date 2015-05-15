package ro.utcn.foodapp.access.database;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

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

        // Insert the new row, returning the primary key value of the new row
        long newRowId = db.insert(
                Product.TABLE_NAME,
                DbHelper.COLUMN_NAME_NULLABLE,
                values);

        if (newRowId == -1) {
            Log.e("INSERT", "Values couldn't be inserted. An error has occurred");
        }

        // Return the row ID
        return newRowId;
    }
}

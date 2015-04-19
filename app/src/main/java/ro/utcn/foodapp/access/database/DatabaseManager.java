package ro.utcn.foodapp.access.database;

/**
 * Represents the database manager and it's used to implement the methods for CRUD operations
 * Created by coponipi on 19.04.2015.
 */
public class DatabaseManager {
    private DbHelper dbHelper;

    /**
     * Returns a new instance of the class
     *
     * @return created instance
     */
    public static DatabaseManager getInstance() {
        return new DatabaseManager();
    }
}

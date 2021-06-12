package com.example.studienarbeit_kugellabyrinth;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/** Provides Database Operations for ScoreboardActivity
 * @author Philip Bartmann
 * @version 1.0
 * @since 1.0
 */
public class DatabaseTransformations extends SQLiteOpenHelper {

    final String TAG = "DatabaseTransformations";

    /** instance of the Database the transformations will be used on
     */
    private SQLiteDatabase db;
    private final String tableSQL;
    private final String table;


    /**
     * Creates a DatabaseTransformations instance
     * @param activity calling Activity
     * @param dbName name of the database
     * @param tableSQL SQL statement which will be used to create the table
     */
    public DatabaseTransformations(Context activity, String dbName, String tableSQL) {
        super(activity, dbName, null, 1);
        this.tableSQL = tableSQL;
        //bestimmeTabelle();

        db = this.getWritableDatabase();

        this.table = dbName.replace(".dat", "").toUpperCase();
        db.execSQL(tableSQL);
    }


    /**
     * Creates a Database if there is none
     * @param db name of the database which will be created
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            // create Table if it doesn't exist
            db.execSQL(tableSQL);
        }
        catch(Exception ex) {
            Log.e("carpelibrum", ex.getMessage());
        }
    }

    /**
     * if the database needs an upgrade the database is dropped and newly created
     * in this method
     * @param db name of the database which dropped and created
     * @param oldVersion id of the old version of the database
     * @param newVersion id of the new version of the database
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + table);
        onCreate(db);
    }


    /**
     * Closing the access to the database
     */
    @Override
    public synchronized void close() {
        if(db != null) {
            db.close();
            db = null;
        }

        super.close();
    }


    /**
     * inserts given dataset into the database
     * @param dataset dataset, which will be inserted into the database
     */
    public long insertDataset(DatabaseTemplate dataset) {
        try {
            ContentValues data = createDataTemplate(dataset);
            return db.insert(table, null, data);
        }
        catch(Exception ex) {
            Log.d("carpelibrum", ex.getMessage());
            return -1;
        }
    }

    /**
     * changes name of the dataset with the given id to prevent
     * empty name strings in the database
     * @param name name to which the name of the dataset will be updated to
     * @param currID id of the dataset, which will be updated
     */
    public void changeCurrName(String name, long currID){
        try{
            ContentValues cv = new ContentValues();
            cv.put("name", name);
            db.update(table, cv, "_id = " + currID, null);
        }
        catch(Exception ex) {
            Log.d("carpelibrum", ex.getMessage());
        }
    }


    /**
     * creates a cursor of the last first 10 entries ordered by the column "Zeit"
     * to show in the scoreboard
     */
    public Cursor createListViewCursor() {
        String[] columns = new String[]{"_id", "Name", "Zeit"};
        return  db.query(table, columns, null, null, null, null, "Zeit", "10");
    }

    /**
     * gets the position of the dataset with the given id ordered by the column "Zeit"
     * to get the rank in the whole database
     * @param currDataset id of the dataset, to which the position will be given
     * @return position of the dataset in the database ordered by the column "Zeit"
     */
    public int getPosition(long currDataset) {
        Cursor cursor = null;

        try {
            cursor = db.query(table, null, null, null, null, null, "Zeit");
            int length = cursor.getCount();
            cursor.moveToFirst();
            int i = 0;
            for(; i < length; i++) {
                if(cursor.getLong(0)== currDataset){
                    break;
                }
                cursor.moveToNext();
            }
            return ++i;
        }
        catch(Exception ex) {
            Log.e("carpelibrum", ex.getMessage());
        }
        finally {
            // close cursor no matter what happens
            if(cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return 0;
    }


    /**
     * creates a ContentValues object to insert in the database from a DatabaseTemplate instance
     * @param dataset DatabaseTemplate instance to provide the data for the insertion
     */
    private ContentValues createDataTemplate(DatabaseTemplate dataset) {
        ContentValues data = new ContentValues();
        data.put("Name", dataset.name);
        data.put("Zeit", dataset.time);

        return data;
    }
}

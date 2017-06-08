package de.codereddev.howtoandroidsoundboard;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.Arrays;
import java.util.List;

public class DatabaseHandler extends SQLiteOpenHelper {

    // Define a tag that is used to log any kind of error or comment
    private static final String LOG_TAG = "DATABASEHANDLER";

    // Define a database name and version
    private static final String DATABASE_NAME = "soundboard.db";
    private static final int DATABASE_VERSION = 1;

    // MAIN_TABLE contains all sounds for the soundboard
    // Define information about the main table
    private static final String MAIN_TABLE = "main_table";

    private static final String MAIN_ID = "_id";
    private static final String MAIN_NAME = "soundName";
    private static final String MAIN_ITEM_ID = "soundId";

    // FAVORITES_TABLE contains all sounds that were set as favorites by the user
    // Define information about the favorites table
    private static final String FAVORITES_TABLE = "favorites_table";

    private static final String FAVORITES_ID = "_id";
    private static final String FAVORITES_NAME = "favoName";
    private static final String FAVORITES_ITEM_ID = "favoId";

    // Define the SQL statements to create both tables
    private static final String SQL_CREATE_MAIN_TABLE = "CREATE TABLE IF NOT EXISTS " + MAIN_TABLE + "(" + MAIN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + MAIN_NAME + " TEXT, " + MAIN_ITEM_ID + " INTEGER unique);";
    // The sound resource id in FAVORITES_TABLE is not unique because we have to set it again on every app update because every resource id changes if you add new resources
    private static final String SQL_CREATE_FAVORITES_TABLE = "CREATE TABLE IF NOT EXISTS " + FAVORITES_TABLE + "(" + FAVORITES_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + FAVORITES_NAME + " TEXT, " + FAVORITES_ITEM_ID + " INTEGER);";

    // Create a constructor to start an instance of DatabaseHandler that will create the database
    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        Log.d(LOG_TAG, "Database successfully initialised: " + getDatabaseName());
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        try{
            // Execute the creation statements
            db.execSQL(SQL_CREATE_MAIN_TABLE);
            db.execSQL(SQL_CREATE_FAVORITES_TABLE);

        } catch(Exception e){

            Log.e(LOG_TAG, "Failed to create: " + e.getMessage());
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        // We are using the app version instead of the database version to upgrade the database, so this method is unnecessary
        // If it gets called somehow it should only delete the main table that will be refilled again in the SoundboardActivity
        db.execSQL("DROP TABLE IF EXISTS " + MAIN_TABLE);
        onCreate(db);
    }

    // Defining the sound buttons
    public void createSoundCollection(Context context){

        // Get all entries of the name StringArray(soundNames) declared in strings.xml
        List<String> nameList = Arrays.asList(context.getResources().getStringArray(R.array.soundNames));

        // Declare all sound buttons
        SoundObject[] soundItems = {new SoundObject(nameList.get(0), R.raw.audio01), new SoundObject(nameList.get(1), R.raw.audio02), new SoundObject(nameList.get(2), R.raw.audio03)};

        // Call putIntoMain() for each SoundObject in soundItems to fill the MAIN_TABLE with all necessary information
        for (SoundObject i: soundItems){
            putIntoMain(i);
        }
    }

    // Check if the sound id allready exists in the selected table
    private boolean verification(SQLiteDatabase database, String tableName, String idRow, Integer soundId){

        int count = -1;
        Cursor cursor = null;

        try {

            // Get all rows from the selected table that contain the given sound id
            String query = "SELECT * FROM " + tableName + " WHERE " + idRow + " = " + soundId;
            cursor = database.rawQuery(query, null);

            // If the entry with the given sound id exists get the rows _id as count value
            if (cursor.moveToFirst()){

                count = cursor.getInt(0);
            }

            // Return true if sound exists in the selected table
            return (count > 0);

        } finally {

            // close the cursor after the verification and if it is filled with something
            if (cursor != null){

                cursor.close();
            }
        }
    }

    // Add sounds to MAIN_TABLE
    private void putIntoMain(SoundObject soundObject){

        // Get a writable instance of the database
        SQLiteDatabase database = this.getWritableDatabase();

        // Check if the soundId allready exists in the table then add it to the table if it does not exist
        if (!verification(database, MAIN_TABLE, MAIN_ITEM_ID, soundObject.getItemID()) ){

            try {

                // Put the information into a ContentValues object
                ContentValues contentValues = new ContentValues();

                contentValues.put(MAIN_NAME, soundObject.getItemName());
                contentValues.put(MAIN_ITEM_ID, soundObject.getItemID());

                // Insert the SoundObject into the MAIN_TABLE
                database.insert(MAIN_TABLE, null, contentValues);
            } catch (Exception e){

                Log.e(LOG_TAG, "(MAIN) Failed to insert sound: " + e.getMessage());
            } finally {

                database.close();
            }
        }
    }

    // Returns a Cursor with all entries of the MAIN_TABLE
    // Cursor will be closed after SoundObjects were added to the ArrayList in SoundboardActivity
    public Cursor getSoundCollection(){

        // Get a readable instance of the database
        SQLiteDatabase database = this.getReadableDatabase();

        return database.rawQuery("SELECT * FROM " + MAIN_TABLE + " ORDER BY " + MAIN_NAME, null);
    }

    // Add a sound to favorites / FAVORITES_TABLE
    public void addFavorite(SoundObject soundObject){

        // Get a writable instance of the database
        SQLiteDatabase database = this.getWritableDatabase();

        // Check if the soundId allready exists in the table then add it to the table if it does not exist
        if (!verification(database, FAVORITES_TABLE, FAVORITES_ITEM_ID, soundObject.getItemID()) ) {

            try{

                // Put the information into a ContentValues object
                ContentValues contentValues = new ContentValues();

                contentValues.put(FAVORITES_NAME, soundObject.getItemName());
                contentValues.put(FAVORITES_ITEM_ID, soundObject.getItemID());

                // Insert the SoundObject into the FAVORITES_TABLE
                database.insert(FAVORITES_TABLE, null, contentValues);
            } catch (Exception e){

                Log.e(LOG_TAG, "(FAVORITES) Failed to insert sound: " + e.getMessage());
            } finally {

                database.close();
            }
        }
    }

    // Remove a sound from favorites / FAVORITES_TABLE
    public void removeFavorite(Context context, SoundObject soundObject){

        // Get a writable instance of the database
        SQLiteDatabase database = this.getWritableDatabase();

        // Check if the soundId allready exists in the table then remove it from the table if it exists
        if (verification(database, FAVORITES_TABLE, FAVORITES_ITEM_ID, soundObject.getItemID()) ) {

            try {

                // Remove entry from database table
                database.delete(FAVORITES_TABLE, FAVORITES_ITEM_ID + " = " + soundObject.getItemID(), null);

                // Restart the activity to display changes
                Activity activity = (Activity) context;
                Intent intent = activity.getIntent();
                // Disable activity transitioning animations
                activity.overridePendingTransition(0,0);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                activity.finish();
                activity.overridePendingTransition(0,0);
                context.startActivity(intent);

            } catch (Exception e){

                Log.e(LOG_TAG, "(FAVORITES) Failed to remove sound: " + e.getMessage());
            } finally {

                database.close();
            }
        }
    }

    // Returns a Cursor with all entries of the FAVORITES_TABLE
    // Cursor will be closed after SoundObjects were added to the ArrayList in SoundboardActivity
    public Cursor getFavorites(){

        // Get a readable instance of the database
        SQLiteDatabase database = this.getReadableDatabase();

        return database.rawQuery("SELECT * FROM " + FAVORITES_TABLE + " ORDER BY " + FAVORITES_NAME, null);
    }

    // When adding sounds to the soundboard and updating the resource ids might change
    // This method will update the resource ids in the FAVORITES_TABLE
    public void updateFavorites(){

        // Get a writable instance of the database
        SQLiteDatabase database = this.getWritableDatabase();

        try {

            // Get all data from the FAVORITES_TABLE
            Cursor favorite_content = database.rawQuery("SELECT * FROM " + FAVORITES_TABLE, null);

            // Check if the cursor is empty or failed to convert the data
            if (favorite_content.getCount() == 0){

                Log.d(LOG_TAG, "Cursor is empty or failed to convert data");
                favorite_content.close();
            }


            while (favorite_content.moveToNext()){

                // Set a String that will contain the name of the current sound
                String entryName = favorite_content.getString(favorite_content.getColumnIndex(FAVORITES_NAME));

                // Get the entry of MAIN_TABLE where the name of the current favorite sound appears
                Cursor updateEntry = database.rawQuery("SELECT * FROM " + MAIN_TABLE + " WHERE " + MAIN_NAME + " = '" + entryName + "'", null);

                // You can log the name of the sound that is in the update order right now for debug reasons
                //Log.d(LOG_TAG, "Currently working on: " + entryName);

                // Check if the cursor is empty or failed to convert the data
                if (updateEntry.getCount() == 0){

                    Log.d(LOG_TAG, "Cursor is empty or failed to convert data");
                    updateEntry.close();
                }

                // Move to the cursors first position (should only have 1 position)
                updateEntry.moveToFirst();

                // Check if the resource ids match and update the favorite resource id if necessary
                if (favorite_content.getInt(favorite_content.getColumnIndex(FAVORITES_ITEM_ID)) != updateEntry.getInt(updateEntry.getColumnIndex(MAIN_ITEM_ID)) ){

                    ContentValues contentValues = new ContentValues();
                    contentValues.put(FAVORITES_ITEM_ID, updateEntry.getInt(updateEntry.getColumnIndex(MAIN_ITEM_ID)));
                    database.update(FAVORITES_TABLE, contentValues, FAVORITES_NAME + " = '" + entryName + "'", null);

                    // You can log the name of the sound that has been updated for debug reasons
                    //Log.d(LOG_TAG, "Updated sound: " + entryName);
                }
                // You can log the name of the sound if it is allready up to date for debug reasons
                //else {
                //
                //    Log.d(LOG_TAG, "Allready up to date: " + entryName);
                //}
            }


        } catch (Exception e) {

            Log.e(LOG_TAG, "Failed to update favorites: " + e.getMessage());
        } finally {

            database.close();
        }
    }

    // Gets called when app is updated and recreates the MAIN_TABLE
    public void appUpdate(){

        try {

            SQLiteDatabase database = this.getWritableDatabase();

            database.execSQL("DROP TABLE IF EXISTS " + MAIN_TABLE);

            database.execSQL(SQL_CREATE_MAIN_TABLE);

            database.close();

        } catch (Exception e) {

            Log.e(LOG_TAG, "Failed to update the main table on app update: " + e.getMessage());
        }
    }

}

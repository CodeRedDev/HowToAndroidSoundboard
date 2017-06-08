package de.codereddev.howtoandroidsoundboard;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;

public class SoundboardActivity extends AppCompatActivity {

    // Define a tag that is used to log any kind of error or comment
    private static final String LOG_TAG = "SoundboardActivity";

    // Declare a toolbar to use instead of the system standard toolbar
    Toolbar toolbar;

    // Declare an ArrayList that you fill with SoundObjects that contain all information needed for a sound button
    ArrayList<SoundObject> soundList = new ArrayList<>();

    // Declare a RecyclerView and its components
    // You can assign the RecyclerView.Adapter right away
    RecyclerView SoundView;
    SoundboardRecyclerAdapter SoundAdapter = new SoundboardRecyclerAdapter(soundList);
    RecyclerView.LayoutManager SoundLayoutManager;

    // Declare a View that will contain the layout of the activity and serves as the parent of a Snackbar
    private View mLayout;

    // Declare a DatabaseHandler to support database usage
    DatabaseHandler databaseHandler = new DatabaseHandler(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_soundboard);

        // If the gets an update or runs for the first time fill the database with all SoundObjects
        if (appUpdate()){

            databaseHandler.createSoundCollection(this);

            databaseHandler.updateFavorites();
        }

        // Assign layout view
        // Take a look at activity_soundboard.xml change the id
        mLayout = findViewById(R.id.activity_soundboard);

        // Assign toolbar to the Toolbar item declared in activity_soundboard.xml
        toolbar = (Toolbar) findViewById(R.id.soundboard_toolbar);

        // Set toolbar as new action bar
        setSupportActionBar(toolbar);

        // Calls a method that adds data from a database to the soundList
        addDataToArrayList();

        // Assign SoundView to the RecyclerView item declared in activity_soundboard.xml
        SoundView = (RecyclerView) findViewById(R.id.soundboardRecyclerView);

        // Define the RecyclerView.LayoutManager to have 3 columns
        SoundLayoutManager = new GridLayoutManager(this, 3);

        // Set the RecyclerView.LayoutManager
        SoundView.setLayoutManager(SoundLayoutManager);

        // Set the RecyclerView.Adapter
        SoundView.setAdapter(SoundAdapter);

        // Calls a method that handles all permission events
        requestPermissions();
    }

    // Create an options menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the layout
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.action_favorite_show)
            this.startActivity(new Intent(this, FavoriteActivity.class));

        return super.onOptionsItemSelected(item);
    }

    // Takes care of some things when the user closes the activity
    @Override
    protected void onDestroy(){
        super.onDestroy();

        // Calls a method that releases all data from the used MediaPlayer instance
        EventHandlerClass.releaseMediaPlayer();
    }

    // Fill the soundList with all information given in the MAIN_TABLE
    private void addDataToArrayList(){

        soundList.clear();

        // Get a cursor filled with all information from the MAIN_TABLE
        Cursor cursor = databaseHandler.getSoundCollection();

        // Check if the cursor is empty or failed to convert the data
        if (cursor.getCount() == 0){

            Log.e(LOG_TAG, "Cursor is empty or failed to convert data");
            cursor.close();
        }

        // Prevent the method from adding SoundObjects again everytime the Activity starts
        if (cursor.getCount() != soundList.size() ){

            // Add each item of MAIN_TABLE to soundList and refresh the RecyclerView by notifying the adapter about changes
            while (cursor.moveToNext() ){

                String NAME = cursor.getString(cursor.getColumnIndex("soundName"));
                Integer ID = cursor.getInt(cursor.getColumnIndex("soundId"));

                soundList.add(new SoundObject(NAME, ID));

                SoundAdapter.notifyDataSetChanged();
            }

            cursor.close();
        }
    }

    // Handles all permission events
    private void requestPermissions(){

        // Check if the users Android version is equal to or higher than Android 6 (Marshmallow)
        // Since Android 6 you have to request permissions at runtime to provide a better security
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){

            // Check if the permission to write and read the users external storage is not granted
            // You need this permission if you want to share sounds via WhatsApp or the like
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){

                // You can log this little text if you want to see if this method works in your Android Monitor
                //Log.i(LOG_TAG, "Permission not granted");

                // If the permission is not granted request it
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
            }

            // Check if the permission to write the users settings is not granted
            // You need this permission to set a sound as ringtone or the like
            if(!Settings.System.canWrite(this)){

                // Displays a little bar on the bottom of the activity with an OK button that will open a so called permission management screen
                Snackbar.make(mLayout, "The app needs access to your settings", Snackbar.LENGTH_INDEFINITE).setAction("OK",
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                Context context = v.getContext();
                                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                                intent.setData(Uri.parse("package:" + context.getPackageName()));
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        }).show();
            }

        }
    }

    // Check if the app has been updated
    private boolean appUpdate(){

        // We are saving the current app version into a preference file
        // There are two ways to get a handle to a SharedPreferences, we are creating a unique preference file that is not bound to a context
        // Check the android developer documentation if you want to find out more

        // Define a name for the preference file and a key name to save the version code to it
        final String PREFS_NAME = "VersionPref";
        final String PREF_VERSION_CODE_KEY = "version_code";
        // Define a value that is set if the key does not exist
        final int DOESNT_EXIST = -1;

        // Get the current version code from the package
        int currentVersionCode = 0;
        try{

            currentVersionCode = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;

        } catch (PackageManager.NameNotFoundException e){

            Log.e(LOG_TAG, e.getMessage());
        }

        // Get the SharedPreferences from the preference file
        // Creates the preference file if it does not exist
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        // Get the saved version code or set it if it does not exist
        int savedVersionCode = prefs.getInt(PREF_VERSION_CODE_KEY, DOESNT_EXIST);

        // Create an editor to edit the shared preferences on app update
        SharedPreferences.Editor edit = prefs.edit();

        //Check for updates
        if (savedVersionCode == DOESNT_EXIST){

            databaseHandler.appUpdate();
            // First run of the app
            // Set the saved version code to the current version code
            edit.putInt(PREF_VERSION_CODE_KEY, currentVersionCode);
            edit.commit();
            return true;
        }
        else if (currentVersionCode > savedVersionCode){

            // App update
            databaseHandler.appUpdate();
            edit.putInt(PREF_VERSION_CODE_KEY, currentVersionCode);
            edit.commit();
            return true;
        }

        return false;
    }
}

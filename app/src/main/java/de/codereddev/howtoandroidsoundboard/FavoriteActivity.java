package de.codereddev.howtoandroidsoundboard;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;

public class FavoriteActivity extends AppCompatActivity {

    // Define a tag that is used to log any kind of error or comment
    private static final String LOG_TAG = "FAVORITEACTIVITY";

    // Declare a toolbar to use instead of the system standard toolbar
    Toolbar toolbar;

    // Declare an ArrayList that you fill with SoundObjects that contain all information needed for a sound button
    ArrayList<SoundObject> favoriteList = new ArrayList<>();

    // Declare a RecyclerView and its components
    // You can assign the RecyclerView.Adapter right away
    RecyclerView FavoriteView;
    SoundboardRecyclerAdapter FavoriteAdapter = new SoundboardRecyclerAdapter(favoriteList);
    RecyclerView.LayoutManager FavoriteLayoutManager;

    // Declare a DatabaseHandler to support database usage
    DatabaseHandler databaseHandler = new DatabaseHandler(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);

        // Assign toolbar to the Toolbar item declared in activity_favorite.xml
        toolbar = (Toolbar) findViewById(R.id.favorite_toolbar);

        // Set toolbar as new action bar
        setSupportActionBar(toolbar);

        // Calls a method that adds data from a database to the soundList
        addDataToArrayList();

        // Assign SoundView to the RecyclerView item declared in activity_soundboard.xml
        FavoriteView = (RecyclerView) findViewById(R.id.favoriteRecyclerView);

        // Define the RecyclerView.LayoutManager to have 3 columns
        FavoriteLayoutManager = new GridLayoutManager(this, 3);

        // Set the RecyclerView.LayoutManager
        FavoriteView.setLayoutManager(FavoriteLayoutManager);

        // Set the RecyclerView.Adapter
        FavoriteView.setAdapter(FavoriteAdapter);

    }

    // Create/Inflate options menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the layout
        getMenuInflater().inflate(R.menu.toolbar_menu_fav, menu);

        return super.onCreateOptionsMenu(menu);
    }

    // Handle 'onClicks' in the options menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.action_favorite_hide)
            finish();

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();

        EventHandlerClass.releaseMediaPlayer();
    }

    // Fill the soundList with all information given in the FAVORITES_TABLE
    private void addDataToArrayList(){

        favoriteList.clear();

        // Get a cursor filled with all information from the FAVORITES_TABLE
        Cursor cursor = databaseHandler.getFavorites();

        // Check if the cursor is empty or failed to convert the data
        if (cursor.getCount() == 0){

            Log.e(LOG_TAG, "Cursor is empty or failed to convert data");
            cursor.close();
        }

        // Prevent the method from adding SoundObjects again everytime the Activity starts
        if (cursor.getCount() != favoriteList.size() ){

            // Add each item of FAVORITES_TABLE to soundList and refresh the RecyclerView by notifying the adapter about changes
            while (cursor.moveToNext() ){

                String NAME = cursor.getString(cursor.getColumnIndex("favoName"));
                Integer ID = cursor.getInt(cursor.getColumnIndex("favoId"));

                favoriteList.add(new SoundObject(NAME, ID));

                FavoriteAdapter.notifyDataSetChanged();
            }

            cursor.close();
        }
    }
}

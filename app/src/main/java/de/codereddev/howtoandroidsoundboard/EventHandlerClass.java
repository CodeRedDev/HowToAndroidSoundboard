package de.codereddev.howtoandroidsoundboard;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class EventHandlerClass {

    // Define a tag that is used to log any kind of error or comment
    private static final String LOG_TAG = "EVENTHANDLER";

    // Declare a MediaPlayer to be used by the app
    private static MediaPlayer mp;

    // Declare a DatabaseHandler to support database usage
    private static DatabaseHandler databaseHandler;

    // Creates and starts a MediaPlayer instance to play a sound
    public static void startMediaPlayer(View view, Integer soundID){

        try {

            // Check if the sound id was set correctly
            if (soundID != null){

                // Check if the MediaPlayer maybe is in use
                // If so the MediaPlayer will be reset
                if (mp != null){

                    mp.reset();
                }

                // Create and start the MediaPlayer on the given sound id
                mp = MediaPlayer.create(view.getContext(), soundID);
                mp.start();
            }
        } catch (Exception e){

            // Log error if process failed
            Log.e(LOG_TAG, "Failed to start the MediaPlayer: " + e.getMessage());
        }
    }

    // Releases all data from the MediaPlayer
    public static void releaseMediaPlayer(){

        if (mp != null){

            mp.release();
            mp = null;
        }
    }

    // Creates a PopupMenu at the pressed sound button and handles the users input
    public static void popupManager(final View view, final SoundObject soundObject){

        // Assign the DatabaseHandler
        databaseHandler = new DatabaseHandler(view.getContext());

        // Declare PopupMenu and assign it to the design created in longclick.xml
        PopupMenu popup = new PopupMenu(view.getContext(), view);

        // Identify the current activity and inflate the right popup menu
        if (view.getContext() instanceof FavoriteActivity)
            popup.getMenuInflater().inflate(R.menu.favo_longclick, popup.getMenu());
        else
            popup.getMenuInflater().inflate(R.menu.longclick, popup.getMenu());

        // Handle user clicks on the popupmenu
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                // Check if the user wants to share a sound or set a sound as system audio
                if (item.getItemId() == R.id.action_send || item.getItemId() == R.id.action_ringtone){

                    // Define a filename on the given information from the SoundObject AND add the .mp3 tag to it
                    final String fileName = soundObject.getItemName() + ".mp3";

                    // Get the path to the users external storage
                    File storage = Environment.getExternalStorageDirectory();
                    // Define the directory path to the soundboard apps folder
                    // Change my_soundboard to whatever you want as your folder but keep the slash
                    // TODO: When changing the path be sure to also modify the path in filepaths.xml (res/xml/filepaths.xml)
                    File directory = new File(storage.getAbsolutePath() + "/my_soundboard/");
                    // Creates the directory if it doesn't exist
                    // mkdirs() gives back a boolean. You can use it to do some processes as well but we don't really need it.
                    directory.mkdirs();

                    // Finally define the file by giving over the directory and the filename
                    final File file = new File(directory, fileName);

                    // Define an InputStream that will read the data from your sound-raw.mp3 file into a buffer
                    InputStream in = view.getContext().getResources().openRawResource(soundObject.getItemID());

                    try{

                        // Log the name of the sound that is being saved
                        Log.i(LOG_TAG, "Saving sound " + soundObject.getItemName());

                        // Define an OutputStream/FileOutputStream that will write the buffer data into the sound.mp3 on the external storage
                        OutputStream out = new FileOutputStream(file);
                        // Define a buffer of 1kb (you can make it a little bit bigger but 1kb will be adequate)
                        byte[] buffer = new byte[1024];

                        int len;
                        // Write the data to the sound.mp3 file while reading it from the sound-raw.mp3
                        // if (int) InputStream.read() returns -1 stream is at the end of file
                        while ((len = in.read(buffer, 0, buffer.length)) != -1){
                            out.write(buffer, 0 , len);
                        }

                        // Close both streams
                        in.close();
                        out.close();

                    } catch (IOException e){

                        // Log error if process failed
                        Log.e(LOG_TAG, "Failed to save file: " + e.getMessage());
                    }

                    // Send a sound via WhatsApp or the like
                    if (item.getItemId() == R.id.action_send){

                        try{

                            // Check if the users device Android version is 5.1 or higher
                            // If it is you'll have to use FileProvider to get the sharing function to work properly
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1){

                                final String AUTHORITY = view.getContext().getPackageName() + ".fileprovider";

                                Uri contentUri = FileProvider.getUriForFile(view.getContext(), AUTHORITY, file);

                                final Intent shareIntent = new Intent(Intent.ACTION_SEND);

                                final Intent intent = new Intent(Intent.ACTION_SEND);
                                intent.putExtra(Intent.EXTRA_STREAM, contentUri);
                                // Define the intent to be of type audio/mp3
                                intent.setType("audio/mp3");
                                // Start a new chooser dialog where the user can choose an app to share the sound
                                view.getContext().startActivity(Intent.createChooser(intent, "Share sound via..."));
                            }
                            else {
                                final Intent intent = new Intent(Intent.ACTION_SEND);

                                // Uri refers to a name or location
                                // .parse() analyzes a given uri string and creates a Uri from it

                                // Define a "link" (Uri) to the saved file
                                Uri fileUri = Uri.parse(file.getAbsolutePath());
                                intent.putExtra(Intent.EXTRA_STREAM, fileUri);
                                // Define the intent to be of type audio/mp3
                                intent.setType("audio/mp3");
                                // Start a new chooser dialog where the user can choose an app to share the sound
                                view.getContext().startActivity(Intent.createChooser(intent, "Share sound via..."));
                            }

                        } catch (Exception e){

                            // Log error if process failed
                            Log.e(LOG_TAG, "Failed to share sound: " + e.getMessage());
                        }
                    }

                    // Save as ringtone, alarm or notification
                    if (item.getItemId() == R.id.action_ringtone) {

                        // Create a little popup like dialog that gives the user the choice between the 3 types
                        // THEME_HOLO_LIGHT was deprecated in API 23 but to support older APIs you should use it
                        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext(), AlertDialog.THEME_HOLO_LIGHT);
                        builder.setTitle("Save as...");
                        builder.setItems(new CharSequence[]{"Ringtone", "Notification", "Alarm"}, new DialogInterface.OnClickListener(){

                            @Override
                            public void onClick(DialogInterface dialog, int which){

                                // Decide on the users choice which information will be send to a method that handles the settings for all kinds of system audio
                                switch (which) {

                                    // Ringtone
                                    case 0:
                                        changeSystemAudio(context, RingtoneManager.TYPE_RINGTONE, file);
                                        break;
                                    // Notification
                                    case 1:
                                        changeSystemAudio(context, RingtoneManager.TYPE_NOTIFICATION, file);
                                        break;
                                    // Alarmton
                                    case 2:
                                        changeSystemAudio(context, RingtoneManager.TYPE_ALARM, file);
                                        break;
                                    default:
                                }
                            }
                        });
                        builder.create();
                        builder.show();
                    }

                }

                // Add sound to favorites / Remove sound from favorites
                if (item.getItemId() == R.id.action_favorite) {

                    DatabaseHandler databaseHandler = DatabaseHandler
                            .getInstance(context.getApplicationContext());

                    // Identify the current activity
                    if (context instanceof FavoriteActivity) {
                        databaseHandler.removeFavorite(context, soundObject);
                    } else {
                        databaseHandler.addFavorite(soundObject);
                    }
                }

                return true;
            }
        });

        popup.show();
    }

    private static void changeSystemAudio(Context context, int type, File file) {

        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DATA, file.getAbsolutePath());
        values.put(MediaStore.MediaColumns.TITLE, file.getName());
        values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/mp3");
        values.put(MediaStore.Audio.Media.ARTIST, "HandOfBlood");
        values.put(MediaStore.Audio.Media.IS_RINGTONE, true);
        values.put(MediaStore.Audio.Media.IS_NOTIFICATION, true);
        values.put(MediaStore.Audio.Media.IS_ALARM, true);
        values.put(MediaStore.Audio.Media.IS_MUSIC, false);
        values.put(MediaStore.Audio.Media.IS_PODCAST, false);

        final Uri baseUri = MediaStore.Audio.Media.getContentUriForPath(file.getAbsolutePath());
        Uri toneUri = getUriForExistingTone(context, baseUri, file.getAbsolutePath());
        if (toneUri == null) {
            toneUri = context.getContentResolver().insert(baseUri, values);
        }
        RingtoneManager.setActualDefaultRingtoneUri(context, type, toneUri);
    }

    /**
     * Calculates a URI for an existing tone.
     *
     * @param context Context of the current activity.
     * @param uri Base URI to the tone.
     * @param filePath Path of the file that is linked to the tone.
     * @return The URI of the existing tone or null if it does not exist.
     */
    private static Uri getUriForExistingTone(Context context, Uri uri, String filePath) {

        Cursor cursor = null;
        try {

            cursor = context.getContentResolver()
                    .query(uri,
                            new String[] {MediaStore.MediaColumns._ID, MediaStore.MediaColumns.DATA},
                            MediaStore.MediaColumns.DATA + " = ?",
                            new String[] {filePath},
                            null, null);

            if (cursor != null && cursor.getCount() != 0) {

                cursor.moveToFirst();
                int mediaPos = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
                return Uri.parse(uri.toString() + "/" + mediaPos);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }
}

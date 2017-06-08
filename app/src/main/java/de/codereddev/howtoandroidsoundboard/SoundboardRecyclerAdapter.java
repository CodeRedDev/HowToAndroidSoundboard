package de.codereddev.howtoandroidsoundboard;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class SoundboardRecyclerAdapter extends RecyclerView.Adapter<SoundboardRecyclerAdapter.SoundboardViewHolder>{

    // Declare an ArrayList that will contain all SoundObjects
    private ArrayList<SoundObject> soundObjects;

    // Demand all needed informations for the RecyclerView
    // ArrayList<SoundObject> : Main content provider
    public SoundboardRecyclerAdapter(ArrayList<SoundObject> soundObjects){

        // Hand over all data to the private ArrayList
        this.soundObjects = soundObjects;
    }

    // Initialises each RecyclerView item
    @Override
    public SoundboardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        // Set the default design for a element in the RecyclerView that is based on sound_item.xml
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.sound_item, null);

        // Returns a new ViewHolder for each RecyclerView item
        return new SoundboardViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(SoundboardViewHolder holder, int position) {

        // Get a SoundObject from the ArrayList that also contains
        // Simplifies the set processes
        final SoundObject object = soundObjects.get(position);

        // Define an id for a simplified committing process
        final Integer soundID = object.getItemID();

        // Set the name of each sound button that is represented by the (SoundObject)object
        holder.itemTextView.setText(object.getItemName());

        // Handle actions when the user simply clicks on a sound button
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Calls a method that plays the sound
                EventHandlerClass.startMediaPlayer(v, soundID);
            }
        });

        // Handle actions when the user presses a sound button
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                // Calls a method that creates a PopupMenu where the user can choose between several actions
                EventHandlerClass.popupManager(v, object);
                return true;
            }
        });
    }

    // Tells the RecyclerView how many items are accessible to be displayed
    // Should return the size of the given content provider (here: SoundObject ArrayList)
    @Override
    public int getItemCount() {
        return soundObjects.size();
    }

    // Gets all accessible areas that are declared by you
    public class SoundboardViewHolder extends RecyclerView.ViewHolder{

        // TextView to display the name of a sound button
        TextView itemTextView;

        public SoundboardViewHolder(View itemView) {
            super(itemView);

            // Assign itemTextView to the TextView item declared in sound_item.xml
            itemTextView = (TextView) itemView.findViewById(R.id.textViewItem);
        }
    }
}
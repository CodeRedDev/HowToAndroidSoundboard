package de.codereddev.howtoandroidsoundboard;

public class SoundObject {

    // SoundObject is an object that stores all kind of information you need for a sound button
    // like a name and the soundID/itemID
    // The itemID will be the resource id for a raw .mp3 file that is stored in the raw folder in the projects res folder
    private String itemName;
    private Integer itemID;

    public SoundObject(String itemName, Integer itemID){

        this.itemName = itemName;
        this.itemID = itemID;
    }

    public String getItemName(){

        return itemName;
    }

    public Integer getItemID(){

        return itemID;
    }
}

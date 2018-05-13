# AndroidSoundboardRemastered
This repository contains the reworked and improved soundboard that is based on my [YouTube playlist](https://youtu.be/-1v3ihyIsFY) "How To: Android Soundboard"

# Features
The soundboard supports favorite functions to give the user the ability to easily access his favorite sounds on an extra page.

Further the soundboard features functions to set sounds as ringtone, notification tone or alarm tone.

Also included in this soundboard is the function to share a sound via several apps like WhatsApp, Facebook, Google Mail or the like.

# Improvements
This version of the soundboard adds several improvements to the soundboard versions in the other repositories you can find at the end of this README.

#### Performance
   * Due the use of Loaders to retrieve the sound database improves the performance particularly when resuming the Activity.
   * Moving some tasks like the playback of a sound onto another thread improves the stability of the app.
   * DatabaseHandler is now a Singleton because only one database instance is needed in the app.
    
    
#### Code
   * Exceptions are now properly handled.
   * Full Javadoc documentation of non system functions.
   * Slightly modified Google checkstyle should support the readability.
   * More code improvements...

# Other Repositories
A less improved version of the soundboard can be found [here](https://github.com/CodeRedDev/HowToAndroidSoundboard) or without favorite functions [here](https://github.com/CodeRedDev/SoundboardWithoutFavorites).

# New Door Hostel Food Ordering System (NDHFOS)

This is a prototype created by Shardav Lad, student of Manipal University Jaipur, for an online food ordering system for the hostel premises.

1. To run the app first create a Firebase app and enable Firestore.

2. Open up your terminal and type:
    
    ## For Windows
    ```cmd
    keytool -list -v --alias androiddebugkey -keystore %USERPROFILE%\.android\debug.keystore
    ```

    ## For Mac/Linux
    ```bash
    keytool -list -v --alias androiddebugkey -keystore ~/.android/debug.keystore
    ```

    Then copy the **SHA1** fingerprint

3. In the project settings for your firebase app, scroll down and add the SHA1 fingerprint to your project.

4. Download the google-services.json file and save it in the $PROJECT_DIR/app/ folder.

5. Run the debug build.

##### P.S: I don't know how to build the release apk yet. I'll look into it when I finish building this app.
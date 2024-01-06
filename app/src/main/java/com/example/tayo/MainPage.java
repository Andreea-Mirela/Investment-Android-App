package com.example.tayo;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;
import com.squareup.okhttp.ResponseBody;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MainPage extends AppCompatActivity {

    private static FirebaseAuth firebaseAuth = FirebaseAuth.getInstance(); // user to log users in and get uid
    private static FirebaseFirestore firestore = FirebaseFirestore.getInstance(); // cloud database to store progress and user details
    private static FirebaseStorage firebaseStorage = FirebaseStorage.getInstance(); // image storage for user profile pictures

    private static DatabaseService db; // local database for offline mode

    private static ImageView profilePicture; // the profile picture of the user
    private static CircularProgressBar circularProgressBar; // shows the ratio of [capitoleCompletate] and the max number of chapters
    private static TextView nume, prenume, email, varsta, medalii, orePetrecuteInvatand; // textfields to display user details
    private Button setari, cursuri; // two main buttons of the page
    private static float capitoleCompletate; //how many chapters has the user completed

    public static Map<String, Object> userDetails = new HashMap<>(); // stores information from firestores about the user
    boolean internetAccess = false;
    public static Uri profilePictureUri;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_screen);

        // try the internet connection to know if
        // we will retrieve data from firestore or
        // local database
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                internetAccess = hasActiveInternetConnection();
            }
        });
        thread.start();

        db = new DatabaseService(MainPage.this);

        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        fetchData(internetAccess);

        profilePicture = (ImageView)findViewById(R.id.mainScreenProfilePicture);
        nume = (TextView)findViewById(R.id.numeMainPage);
        prenume = (TextView)findViewById(R.id.prenumeMainPage);
        email = (TextView)findViewById(R.id.emailMainPage);
        varsta = (TextView)findViewById(R.id.varstaMainPage);
        medalii = (TextView)findViewById(R.id.numarMedaliiMainPage);
        orePetrecuteInvatand = (TextView)findViewById(R.id.orePetrecuteInvatand);
        setari = (Button)findViewById(R.id.buttonSetari);
        cursuri = (Button)findViewById(R.id.buttonCursuri);


        circularProgressBar = (CircularProgressBar)findViewById(R.id.courseProgressBar);
        circularProgressBar.setColor(0xffFFCE6B);
        circularProgressBar.setBackgroundColor(0xffeeeeee);
        circularProgressBar.setProgressBarWidth((float) 12.0);
        circularProgressBar.setBackgroundProgressBarWidth((float) 12.0);

        setari.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainPage.this, Setari.class));
            }
        });
        cursuri.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainPage.this, Courses.class));
            }
        });
    }

    private static void fetchData(boolean internetAccess){
        if(internetAccess){
            getUserDetails();
        } else{
            getLocalUserDetails();
        }
    }

    /**
     * Retrieves data about the current user from the firestore
     * and stores the output to the userDetails map from the MainPage
     */
    public static void getUserDetails(){
        firestore.collection("users")
                .document(firebaseAuth.getCurrentUser().getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            // extract the details into a map to feed the UI
                            userDetails = task.getResult().getData();
                            System.out.println("Data retrieved with success from firestore");

                            // calculate the age from the date of birth
                            Calendar dateOfBirth = Calendar.getInstance();
                            Calendar now = Calendar.getInstance();
                            String[] partsFromDoB = userDetails.get("dateOfBirth").toString().split("/");
                            dateOfBirth.set(Integer.parseInt(partsFromDoB[2]), Integer.parseInt(partsFromDoB[1]), Integer.parseInt(partsFromDoB[0]));
                            int age = now.get(Calendar.YEAR) - dateOfBirth.get(Calendar.YEAR);
                            if(now.get(Calendar.DAY_OF_YEAR) < dateOfBirth.get(Calendar.DAY_OF_YEAR))
                                age--;

                            // put data into the UItes
                            nume.setText(userDetails.get("name").toString());
                            prenume.setText(userDetails.get("firstName").toString());
                            email.setText(userDetails.get("email").toString());
                            orePetrecuteInvatand.setText(new DecimalFormat("##.##").format(Double.parseDouble(userDetails.get("hoursLearning").toString())));
                            medalii.setText(String.valueOf(userDetails.get("medals")));
                            varsta.setText(String.valueOf(age));
                            capitoleCompletate = Float.parseFloat(userDetails.get("chaptersCompleted").toString());
                            circularProgressBar.setProgressWithAnimation(capitoleCompletate / 25 * 100, 2500);

                            // if there isn't a image in the firebase storage, then a avatar will show
                            // else, the chosen image will be displayed
                            if(userDetails.get("image") == null || userDetails.get("image") == "")
                                profilePicture.setImageResource(R.drawable.avatar_foreground);
                            else{
                                // extract the profile image from the firebase storage
                                StorageReference ref = firebaseStorage.getReference().child(userDetails.get("image").toString());
                                ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        Picasso.get().load(uri).into(profilePicture);
                                        profilePictureUri = uri;
                                    }
                                });
                            }

                            // update the local databasetes
                            try{
                                db.addUserDetails(userDetails.get("name").toString(),
                                                    userDetails.get("firstName").toString(),
                                                    userDetails.get("email").toString(),
                                                    userDetails.get("dateOfBirth").toString(),
                                                    Double.parseDouble(userDetails.get("hoursLearning").toString()),
                                                    Integer.parseInt(userDetails.get("medals").toString()),
                                                    Integer.parseInt(userDetails.get("chaptersCompleted").toString()));
                            } catch (Exception e){
                                System.out.println("Local database clone failed, " + e.getMessage());
                            }
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                System.out.println("Error on getting data from firestore " + e.getMessage());
            }
        });
    }

    private static void getLocalUserDetails(){
        Cursor cursor = db.readUserDetails();

        if(cursor.getCount() == 0){
            System.out.println("Nothing found in the local database");
        } else{
            Calendar dateOfBirth = Calendar.getInstance();
            Calendar now = Calendar.getInstance();

            String[] partsFromDoB = cursor.getString(3).split("/");
            dateOfBirth.set(Integer.parseInt(partsFromDoB[2]), Integer.parseInt(partsFromDoB[1]), Integer.parseInt(partsFromDoB[0]));
            int age = now.get(Calendar.YEAR) - dateOfBirth.get(Calendar.YEAR);
            if(now.get(Calendar.DAY_OF_YEAR) < dateOfBirth.get(Calendar.DAY_OF_YEAR))
                age--;

            nume.setText(cursor.getString(0));
            prenume.setText(cursor.getString(1));
            email.setText(cursor.getString(2));
            orePetrecuteInvatand.setText((String.valueOf(cursor.getDouble(4))));
            medalii.setText(String.valueOf(cursor.getInt(5)));
            varsta.setText(String.valueOf(age));
        }
    }

    public static boolean hasActiveInternetConnection(){
        boolean success = false;
        try {
            URL url = new URL("https://google.com");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(2000);
            connection.connect();
            success = connection.getResponseCode() == 200;
            connection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return success;
    }
}

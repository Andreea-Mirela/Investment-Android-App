package com.example.tayo;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.example.tayo.MainPage.profilePictureUri;
import static com.example.tayo.MainPage.userDetails;

public class Setari extends AppCompatActivity {

    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance(); // connection to the auth service of the firebase
    FirebaseFirestore firestore = FirebaseFirestore.getInstance(); // the firestore database to store user data
    FirebaseStorage firebaseStorage = FirebaseStorage.getInstance(); // stores images uploaded by the users

    ImageView profilePicture; // displays the image of the user
    TextView nume, prenume, email; // displays user details
    Button addPhoto, changePassword, done; // buttons to interact with the page
    private static final int photo_id = 100; // the result code of the pick image intent
    static boolean alreadyResetPassword = false; // stops the spam in the page to reset the password
    boolean internetAccess = false; // shows if the app is connected to the internet

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setari);

        profilePicture = (ImageView)findViewById(R.id.settingsProfilePicture);
        // load the profile picture
        Picasso.get().load(profilePictureUri).into(profilePicture);

        nume = (TextView) findViewById(R.id.numeSetari);
        prenume = (TextView) findViewById(R.id.prenumeSetari);
        email = (TextView) findViewById(R.id.emailSetari);

        // set data to the page
        nume.setText(userDetails.get("name").toString());
        prenume.setText(userDetails.get("firstName").toString());
        email.setText(userDetails.get("email").toString());

        addPhoto = (Button) findViewById(R.id.buttonSetariAddPhoto);
        changePassword = (Button) findViewById(R.id.buttonSetariChangePassword);
        done = (Button) findViewById(R.id.buttonSetariDone);

        /**
         * Creates an intent to the gallery of the phone
         * to select a profile image
         */
        addPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Selectează poza"), photo_id);
            }
        });

        /**
         * This calls the firebase auth to send an email to the current user's email
         * to reset the current password
         */
        changePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!alreadyResetPassword)
                    firebaseAuth.sendPasswordResetEmail(userDetails.get("email").toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(Setari.this, "Email-ul de resetare a parolei a fost trimis", Toast.LENGTH_SHORT).show();
                                System.out.println("Password reset success -----------------------------------------");
                                alreadyResetPassword = true;
                            }
                            else{
                                Toast.makeText(Setari.this, "A aparut o problema la resetarea parolei", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                else{
                    Toast.makeText(Setari.this, "Email-ul de resetare a fost deja trimis!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        /**
         * After all modifications are done
         * if the app is not connected to the internet, save modifications to the local database
         * to be uploaded after into the firestore
         * and navigate back to the main page
         */
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Setari.this, MainPage.class));
            }
        });

    }
    // when the user selected a picture from the gallery
    // it is returned in this method which uploads the image
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Load the new profile image into the firebase storage
        if(requestCode == photo_id){
            if(resultCode == Activity.RESULT_OK){
                if(data != null){
                    try{

                        Thread thread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                internetAccess =  MainPage.hasActiveInternetConnection();
                            }
                        });
                        thread.start();

                        Uri imageUri = data.getData();
                        profilePicture.setImageURI(imageUri);

                        thread.join(); // return the method to see if the device has internet or not

                        // check if the device has internet access,
                        // if it does, load to firestore
                        // else load it to local database
                        if (internetAccess){
                            try {
                                // create a random uuid to give to the uploaded image
                                String uuid = UUID.randomUUID().toString();
                                // create a map object to link the storage image to the user details in firestore
                                Map<String, Object> imageObject = new HashMap<>();
                                imageObject.put("image", uuid);

                                // create a reference of the storage to upload the image
                                StorageReference ref = firebaseStorage.getReference().child(uuid);
                                ref.putFile(imageUri)
                                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                            @Override
                                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                Toast.makeText(Setari.this, "Imaginea a fost incărcată cu succes", Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(Setari.this, "A apărut o eroare la încărcarea imaginii", Toast.LENGTH_SHORT).show();
                                                System.out.println(e.getMessage());
                                            }
                                        });
                                // put the image link into the user's details to know which picture
                                // belongs to it
                                firestore.collection("users")
                                        .document(firebaseAuth.getCurrentUser().getUid())
                                        .update(imageObject);

                            } catch (Exception e){
                                System.out.println(e.getMessage());
                            }
                        }

                    } catch (Exception e){
                        System.out.println(e.getMessage());
                    }
                }
            }
        }
    }
}

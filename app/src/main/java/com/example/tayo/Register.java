package com.example.tayo;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PatternMatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Register extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private EditText emailInput, passwordInput, nameInput, firstNameInput, dateOfBirthInput;
    private Button registerButton;
    private TextView goToLogin;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);

        firebaseAuth = FirebaseAuth.getInstance();
        emailInput = (EditText)findViewById(R.id.registerEmailInput);
        passwordInput = (EditText)findViewById(R.id.registerPasswordInput);
        nameInput = (EditText)findViewById(R.id.registerNameInput);
        firstNameInput = (EditText)findViewById(R.id.registerFirstNameInput);
        dateOfBirthInput = (EditText)findViewById(R.id.registerDataNastereInput);
        registerButton = (Button)findViewById(R.id.registerButton);
        goToLogin = (TextView)findViewById(R.id.registerGoToLogin);

        goToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Register.this, MainActivity.class));
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerUser();
            }
        });
    }

    void registerUser(){
        final String email = emailInput.getText().toString().trim();
        final String password = passwordInput.getText().toString().trim();
        final String name = nameInput.getText().toString().trim();
        final String firstName = firstNameInput.getText().toString().trim();
        final String dateOfBirth = dateOfBirthInput.getText().toString().trim();

        // field verification in case a input is left empty
        if(email.isEmpty()){
            emailInput.setError("Email-ul este necesar!");
            emailInput.requestFocus();
            return;
        }

        if(password.isEmpty()){
            passwordInput.setError("Parola este necesară!");
            passwordInput.requestFocus();
            return;
        }

        if(password.length() < 6){
            passwordInput.setError("Parola trebuie sa aiba cel putin 6 caractere");
            passwordInput.requestFocus();
            return;
        }

        if(name.isEmpty()){
            nameInput.setError("Numele este obligatoriu!");
            nameInput.requestFocus();
            return;
        }

        if(firstName.isEmpty()){
            firstNameInput.setError("Numele este obligatoriu!");
            firstNameInput.requestFocus();
            return;
        }

        if(dateOfBirth.isEmpty()){
            dateOfBirthInput.setError("Data de naștere este obligatorie!");
            dateOfBirthInput.requestFocus();
            return;
        }

        String[] checkDate = dateOfBirth.split("/");

        if(Integer.parseInt(checkDate[0]) > 31 || Integer.parseInt(checkDate[0]) < 0 ||
            Integer.parseInt(checkDate[1]) > 12 || Integer.parseInt(checkDate[1]) < 0 ||
            Integer.parseInt(checkDate[2]) > 2018 || Integer.parseInt(checkDate[2]) < 1940){
            dateOfBirthInput.setError("Data de naștere este incorectă!");
            dateOfBirthInput.requestFocus();
            return;
        }

        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            User user = new User(name, firstName, email, dateOfBirth, 0,0,0);

                            // this map contains all the information we want to store about a user
                            // it will be uploaded into the firebase database
                            Map<String, Object> userDetails = new HashMap<>();
                            // this is an array with all tests from the application
                            // it will store the state of completion of the tests so the user
                            // cannot take the test again
                            Map<String, Object> completedTests = new HashMap<>();

                            for(int i = 0; i<25; i++){
                                completedTests.put(String.valueOf(i), false);
                            }

                            userDetails.put("email", user.email);
                            userDetails.put("name", user.name);
                            userDetails.put("firstName",user.firstName);
                            userDetails.put("dateOfBirth", user.dateOfBirth);
                            userDetails.put("hoursLearning", user.hoursLearning);
                            userDetails.put("medals", user.medals);
                            userDetails.put("chaptersCompleted", user.chaptersCompleted);
                            userDetails.put("image", "");
                            userDetails.put("completedTests", completedTests);

                            // create a user with information from the userDetails map
                            // this is used for better management over the records
                            firestore.collection("users")
                                    .document(firebaseAuth.getCurrentUser().getUid())
                                    .set(userDetails)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(Register.this, "Account created", Toast.LENGTH_SHORT).show();
                                            System.out.println("created user to database");

                                            // after an account was successfully created
                                            // restart the application to let the libraries implement properly
                                            Intent mStartActivity = new Intent(Register.this, MainActivity.class);
                                            int mPendingIntentId = 123456;
                                            PendingIntent mPendingIntent = PendingIntent.getActivity(Register.this, mPendingIntentId,    mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
                                            AlarmManager mgr = (AlarmManager)Register.this.getSystemService(Context.ALARM_SERVICE);
                                            mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
                                            System.exit(0);


                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            System.out.println("FIRESTORE ERROR -------------------------------");
                                            System.out.println(e.getMessage());
                                        }
                                    });

                            // store a copy of the information in a local database
                            // to make the app usable in offline mode with no internet
                            DatabaseService localDatabase = new DatabaseService(Register.this);
                            try{
                                localDatabase.addUserDetails(user.name, user.firstName, user.email, user.dateOfBirth, 0, 0, 0);
                            } catch (Exception e){
                                System.out.println("LOCAL DATABASE EXCEPTION -------------------------------");
                                System.out.println(e.getMessage());
                            }
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        System.out.println("AUTH ERROR -------------------------------");
                        System.out.println(e.getMessage());
                    }
                });
    }
}

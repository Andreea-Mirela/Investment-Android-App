package com.example.tayo;

import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.github.barteksc.pdfviewer.PDFView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.common.util.concurrent.AtomicDouble;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

import static com.example.tayo.MainPage.getUserDetails;
import static com.example.tayo.MainPage.userDetails;

public class Teorie extends AppCompatActivity {

    FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    DatabaseService db = new DatabaseService(Teorie.this);
    PDFView pdfView;
    Timer timer = new Timer();
    AtomicDouble counter = new AtomicDouble(1);
    double currentLearningTime = 0;


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        timer.cancel();
        this.finish();

    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.teorie_pdf);

        getUserDetails();
        currentLearningTime = Double.parseDouble(userDetails.get("hoursLearning").toString());

        pdfView = findViewById(R.id.teoriePDF);

        String pdf = "Cap " + getIntent().getIntExtra("courseNumber", 0) + " - teorie.pdf";

        pdfView.fromAsset(pdf)
                .pages(0, 2, 1, 3, 3, 3) // all pages are displayed by default
                .enableSwipe(true) // allows to block changing pages using swipe
                .swipeHorizontal(false)
                .enableDoubletap(true)
                .defaultPage(0)
                .enableAnnotationRendering(false) // render annotations (such as comments, colors or forms)
                .password(null)
                .scrollHandle(null)
                .enableAntialiasing(true) // improve rendering a little bit on low-res screens
                // spacing between pages in dp. To define spacing color, set view background
                .spacing(0)
                .load();

        // updates the firestore and sqlite database in another thread

        // count the learning time of the user
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                System.out.println(counter);
                // when the timer completes a minute
                // update the learningTime
                if(counter.get()%60 == 0){
                    System.out.println("divizibil cu 60");
                    // start a thread to upload the work to the firestore and sqlite database
                    Thread updateDatabases = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Map<String, Object> timeLearning = new HashMap<>();
                            // adds the counter to the existing time at the start of the activity
                            timeLearning.put("hoursLearning", currentLearningTime + (counter.get()/60)/60);
                            firestore.collection("users")
                                    .document(firebaseAuth.getCurrentUser().getUid())
                                    .update(timeLearning);

                            db.updateTimeLearning(currentLearningTime + (counter.get()/60)/60);
                        }
                    });
                    updateDatabases.start();
                }
                counter.set(counter.get() + 1);
            }
        }, 1000, 1000);



    }
}

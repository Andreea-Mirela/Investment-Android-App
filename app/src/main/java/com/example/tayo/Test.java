package com.example.tayo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import static com.example.tayo.MainPage.userDetails;

public class Test extends AppCompatActivity {

    FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    FirebaseAuth auth = FirebaseAuth.getInstance();
    RecyclerView questionList;
    TestAdapter adapter;
    Button done;
    static Map<String, Object> questions = new HashMap<String, Object>();
    int courseNumber;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test);

        // the number of the current chapter
        courseNumber = getIntent().getIntExtra("courseNumber", -1);

        // fetch all questions and answers from firestore and put
        // them into the TestAdapter to be displayed on the screen
        getQuestions();

        done = (Button) findViewById(R.id.buttonTestDone);

        // the button that is pressed when the user finishes the test
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(adapter.answers.size() < 5){
                    Toast.makeText(Test.this,"Completează răspunsul la toate intrebările", Toast.LENGTH_LONG).show();
                } else{
                    // if every question has been answered
                    // check if every answer is correct
                    boolean testPassed = true;
                    for(boolean value : adapter.answers.values()){
                        if (!value) {
                            testPassed = false;
                            break;
                        }
                    }
                    if(!testPassed){
                        Toast.makeText(Test.this, "Nu ai trecut testul, mai incearcă", Toast.LENGTH_SHORT).show();
                        finish();
                    } else{
                        try {
                            // if all the answers are correct, the user passed the test
                            Toast.makeText(Test.this, "Felicitări! Ai trecut", Toast.LENGTH_SHORT).show();
                            // create a map of the new value to store into the database
                            // this will add 1 to the current chaptersCompleted field into the firestore
                            Map<String, Object> testObject = new HashMap<>();
                            Map<String, Boolean> completedTests = (Map<String, Boolean>) userDetails.get("completedTests");
                            testObject.put("chaptersCompleted", Integer.parseInt(userDetails.get("chaptersCompleted").toString()) + 1);

                            System.out.println("test passed " + testPassed);

                            if(completedTests.get(String.valueOf(courseNumber)) == false){
                                // update the number of passed tests into the firestore
                                completedTests.replace(String.valueOf(courseNumber), true);
                                testObject.put("completedTests", completedTests);
                                testObject.put("medals", Integer.parseInt(userDetails.get("medals").toString()) + 5);
                                // change the state of the completedTests
                                firestore.collection("users")
                                        .document(auth.getCurrentUser().getUid())
                                        .update(testObject);

                            }

                            // update the number of passed tests into the local
                            DatabaseService db = new DatabaseService(Test.this);
                            db.updateCompletedChapters(Integer.parseInt(userDetails.get("chaptersCompleted").toString()) + 1);

                            // close the activity to return to the TeoriePracticaTest page
                            finish();

                        } catch (Exception e){
                            Toast.makeText(Test.this, "Nu s-a putut inregistra testul", Toast.LENGTH_SHORT).show();
                            System.out.println(e.getMessage());
                        }
                    }
                }
            }
        });

    }

    /**
     * Fetch the questions from the current chapter from firestore
     * and put them into TestAdapter
     */
    void getQuestions(){
        firestore.collection("tests")
                .document(String.valueOf(courseNumber))
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        // bind the userdata to questions
                        questions = task.getResult().getData();

                        // bind the test questions to the test map
                        Map<String, String> test = (Map<String, String>) questions.get("question1");
                        System.out.println("in Test, the question1: " + test.get("question"));

                        questionList = (RecyclerView) findViewById(R.id.testRecyclerView);
                        // create the adapter to show all this info
                        adapter = new TestAdapter(Test.this, questions);

                        GridLayoutManager gridLayoutManager = new GridLayoutManager(Test.this, 1, GridLayoutManager.VERTICAL, false);
                        // set the adapter and layout onto the Test page to be displayed
                        questionList.setLayoutManager(gridLayoutManager);
                        questionList.setAdapter(adapter);
                    }
                });
    }
}

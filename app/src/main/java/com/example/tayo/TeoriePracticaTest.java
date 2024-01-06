package com.example.tayo;

import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;

import static com.example.tayo.MainPage.userDetails;

public class TeoriePracticaTest extends AppCompatActivity {

    Button teorie, practica, test;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.teorie_practica_test);

        final int courseNumber = getIntent().getIntExtra("courseNumber", -1);

        teorie = (Button) findViewById(R.id.buttonTeorie);
        practica = (Button) findViewById(R.id.buttonPractica);
        test = (Button) findViewById(R.id.buttonTest);

        teorie.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(TeoriePracticaTest.this, Teorie.class);
                intent.putExtra("courseNumber", courseNumber);
                startActivity(intent);
            }
        });

        practica.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                try {
                    InputStream inputStream = getAssets().open("Cap " + courseNumber + " - practica.pdf");

                    Intent intent = new Intent(TeoriePracticaTest.this, Practica.class);
                    intent.putExtra("courseNumber", courseNumber);
                    startActivity(intent);
                } catch (IOException e) {
                    Toast.makeText(TeoriePracticaTest.this, "Nu există practică pentru acest capitol", Toast.LENGTH_SHORT).show();
                }
            }
        });

        test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(TeoriePracticaTest.this, Test.class);
                System.out.println(courseNumber);
                Map<String, Boolean> completedTests = (Map<String, Boolean>) userDetails.get("completedTests");

                if(completedTests.get(String.valueOf(courseNumber)) == false){
                    intent.putExtra("courseNumber", courseNumber);
                    startActivity(intent);
                } else{
                    Toast.makeText(TeoriePracticaTest.this, "Ai dat deja testul pentru acest capitol", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}

package com.example.tayo;

import android.net.Uri;
import android.os.Bundle;
import android.widget.Adapter;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.barteksc.pdfviewer.PDFView;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;

public class Courses extends AppCompatActivity {

    RecyclerView courseList;
    CourseAdapter adapter;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cursuri);

        courseList = (RecyclerView) findViewById(R.id.courseRecyclerView);

        adapter = new CourseAdapter(this);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2, GridLayoutManager.VERTICAL, false);
        courseList.setLayoutManager(gridLayoutManager);
        courseList.setAdapter(adapter);


    }
}

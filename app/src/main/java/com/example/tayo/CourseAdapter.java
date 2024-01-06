package com.example.tayo;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.ViewHolder> {

    LayoutInflater inflater;
    Context context;

    public CourseAdapter(Context context){
        this.inflater = LayoutInflater.from(context);
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.course_button, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.course.setText("Capitolul " + (position + 1));
    }

    @Override
    public int getItemCount() {
        return 25;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        Button course;

        public ViewHolder(@NonNull final View itemView) {
            super(itemView);
            course = itemView.findViewById(R.id.courseButton);

            course.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    System.out.println("button works " + getAdapterPosition());
                    Intent intent = new Intent(context, TeoriePracticaTest.class);
                    intent.putExtra("courseNumber", getAdapterPosition() + 1);
                    context.startActivity(intent);
                }
            });
        }
    }
}

package com.example.tayo;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class TestAdapter extends RecyclerView.Adapter<TestAdapter.ViewHolder> {

    LayoutInflater inflater; // the question card, this is the building block of the Test page
    Context context; // where do show the cards
    Map<String, Object> questions; // a list of questions from firebase
    public Map<String, Boolean> answers; // the answers of the users

    public TestAdapter(Context context, Map<String, Object> questions){
        this.inflater = LayoutInflater.from(context);
        this.context = context;
        this.questions = questions;
        this.answers = new HashMap<>();
    }

    @NonNull
    @Override
    public TestAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.test_question, parent, false);
        return new TestAdapter.ViewHolder(view);
    }

    // in this method is implemented the logic behind the questions
    @Override
    public void onBindViewHolder(@NonNull final TestAdapter.ViewHolder holder, final int position) {
        // get the questions from [Test]
        final Map<String, String> questionsMap = (Map<String, String>) questions.get("question" + (position + 1) );
        // update TextViews with test data
        holder.question.setText(questionsMap.get("question"));
        holder.questionNumber.setText(String.valueOf(position + 1) + ".");
        holder.answer1.setText(questionsMap.get("answer1"));
        holder.answer2.setText(questionsMap.get("answer2"));
        holder.answer3.setText(questionsMap.get("answer3"));
        // when an answer is tapped, it is checked if it's correct
        // or not, and put the true or false into the [answers] list
        holder.answersGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                String correctAnswer = questionsMap.get("answer");

                if(holder.answer1.getId() == i && holder.answer1.getText().equals(correctAnswer)){
                    if(answers.containsKey("question" + position)){
                        answers.replace("question" + position, true);
                    } else{
                        answers.put("question" + position, true);
                    }
                } else if(holder.answer2.getId() == i && holder.answer2.getText().equals(correctAnswer)){
                    if(answers.containsKey("question" + position)){
                        answers.replace("question" + position, true);
                    } else{
                        answers.put("question" + position, true);
                    }
                } else if(holder.answer3.getId() == i && holder.answer3.getText().equals(correctAnswer)){
                    if(answers.containsKey("question" + position)){
                        answers.replace("question" + position, true);
                    } else{
                        answers.put("question" + position, true);
                    }
                } else{
                    if(answers.containsKey("question" + position)){
                        answers.replace("question" + position, false);
                    } else{
                        answers.put("question" + position, false);
                    }
                }

                System.out.println(answers.get("question" + position));
            }
        });
    }

    @Override
    public int getItemCount() {
        return 5;
    }

    // in this class are declared all the variables from test_question view
    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView questionNumber;
        TextView question;
        RadioGroup answersGroup;
        RadioButton answer1;
        RadioButton answer2;
        RadioButton answer3;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            questionNumber = itemView.findViewById(R.id.questionNumber);
            question = itemView.findViewById(R.id.question);
            answersGroup = itemView.findViewById(R.id.answersGroup);
            answer1 = itemView.findViewById(R.id.answer1);
            answer2 = itemView.findViewById(R.id.answer2);
            answer3 = itemView.findViewById(R.id.answer3);

           // questionNumber.setText( getItemCount() + String.valueOf(1) );
            //question.setText(questionMap.get("question"));
        }
    }
}

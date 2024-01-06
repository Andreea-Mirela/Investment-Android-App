package com.example.tayo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import androidx.annotation.Nullable;

public class DatabaseService extends SQLiteOpenHelper {

    private Context context;

    public DatabaseService(@Nullable Context context) {
        super(context, "Tayo.db", null, 1);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String query =
                "CREATE TABLE user_details (name TEXT, firstName TEXT, email TEXT, dateOfBirth TEXT, hoursLearning REAL, medals INTEGER, chaptersCompleted INTEGER);";

        sqLiteDatabase.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS user_details");
        onCreate(sqLiteDatabase);
    }

    /**
     * Inserts necessary details into the user details table,
     * this table is used when the app is in offline mode
     * @param name
     * @param firstName
     * @param email
     * @param dateOfBirth
     * @param hoursLearning
     * @param medals
     * @param chaptersCompleted
     */
    public void addUserDetails(String name, String firstName, String email, String dateOfBirth, double hoursLearning, int medals, int chaptersCompleted){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put("name", name);
        contentValues.put("firstName", firstName);
        contentValues.put("email", email);
        contentValues.put("dateOfBirth", dateOfBirth);
        contentValues.put("hoursLearning", hoursLearning);
        contentValues.put("medals", medals);
        contentValues.put("chaptersCompleted", chaptersCompleted);

        long result = db.insert("user_details", null, contentValues);

        if(result == -1){
            Toast.makeText(context, "Ceva a eșuat adăugând în baza de date",Toast.LENGTH_SHORT).show();
        }
    }

    public Cursor readUserDetails(){
        String query = "SELECT * FROM user_details";
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = null;
        if(db != null){
            cursor = db.rawQuery(query, null);
        }
        return cursor;
    }

    public void updateTimeLearning(double time){
        SQLiteDatabase db = this.getWritableDatabase();

        String query = "UPDATE user_details SET hoursLearning = " + time + ";";

        try {
            db.execSQL(query);
        } catch(Exception e){
            System.out.println("Something happened updating learing time");
        }
    }

    public void updateCompletedChapters(int number){
        SQLiteDatabase db = this.getWritableDatabase();

        String query = "UPDATE user_details SET chaptersCompleted = " + number + ";";

        try{
            db.execSQL(query);
        } catch (Exception e){
            System.out.println("Something happened updating chapters time");
        }
    }
}

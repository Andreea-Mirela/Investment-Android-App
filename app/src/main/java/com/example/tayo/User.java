package com.example.tayo;

public class User {

    public String name;
    public String firstName;
    public String email;
    public String dateOfBirth;
    public double hoursLearning;
    public int medals;
    public int chaptersCompleted;

    public User(){}

    public User(String name, String firstName, String email, String dateOfBirth, double hoursLearning, int medals, int chaptersCompleted) {
        this.name = name;
        this.firstName = firstName;
        this.email = email;
        this.dateOfBirth = dateOfBirth;
        this.hoursLearning = hoursLearning;
        this.medals = medals;
        this.chaptersCompleted = chaptersCompleted;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public double getHoursLearning() {
        return hoursLearning;
    }

    public void setHoursLearning(double hoursLearning) {
        this.hoursLearning = hoursLearning;
    }

    public int getMedals() {
        return medals;
    }

    public void setMedals(int medals) {
        this.medals = medals;
    }

    public int getChaptersCompleted() {
        return chaptersCompleted;
    }

    public void setChaptersCompleted(int chaptersCompleted) {
        this.chaptersCompleted = chaptersCompleted;
    }
}

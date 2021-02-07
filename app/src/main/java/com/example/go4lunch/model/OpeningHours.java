package com.example.go4lunch.model;

import com.example.go4lunch.util.Constants;

import java.util.Calendar;

public class OpeningHours {
    private int firstOpeningHour;
    private int firstClosingHour;
    private int lastOpeningHour;
    private int lastClosingHour;

    public OpeningHours() {
    }

    public OpeningHours(int firstOpeningHour, int firstClosingHour, int lastOpeningHour, int lastClosingHour) {
        this.firstOpeningHour = firstOpeningHour;
        this.firstClosingHour = firstClosingHour;
        this.lastOpeningHour = lastOpeningHour;
        this.lastClosingHour = lastClosingHour;
    }

    public String getOpeningStatus(){
        String openingStatus = "";
        int currentHour = Calendar.HOUR_OF_DAY;

        if (currentHour < lastClosingHour){ //If we are not at the closing time of the day
            if (firstOpeningHour <= currentHour && currentHour < firstClosingHour){ //If we are at the first opening time
                //open until firstClosingHour
                openingStatus = Constants.OPEN_UNTIL_TEXT + firstClosingHour + Constants.H_TEXT;
            }
            else if (lastOpeningHour <= currentHour && currentHour < lastClosingHour){ //If we are at the second opening time
                //open until lastClosingHour
                openingStatus = Constants.OPEN_UNTIL_TEXT + lastClosingHour + Constants.H_TEXT;
            }

            else if (firstClosingHour <= currentHour && currentHour < lastOpeningHour){ //If we are at the break time
                //Closed. Open at lastOpeningHour
                openingStatus = Constants.CLOSE_AND_OPEN_AT_TEXT + lastOpeningHour + Constants.H_TEXT;
            }
        }

        return openingStatus;
    }

    public int getFirstOpeningHour() {
        return firstOpeningHour;
    }

    public void setFirstOpeningHour(int firstOpeningHour) {
        this.firstOpeningHour = firstOpeningHour;
    }

    public int getFirstClosingHour() {
        return firstClosingHour;
    }

    public void setFirstClosingHour(int firstClosingHour) {
        this.firstClosingHour = firstClosingHour;
    }

    public int getLastOpeningHour() {
        return lastOpeningHour;
    }

    public void setLastOpeningHour(int lastOpeningHour) {
        this.lastOpeningHour = lastOpeningHour;
    }

    public int getLastClosingHour() {
        return lastClosingHour;
    }

    public void setLastClosingHour(int lastClosingHour) {
        this.lastClosingHour = lastClosingHour;
    }
}

package com.example.go4lunch.model;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Parcel;

import androidx.annotation.RequiresApi;

import com.example.go4lunch.util.Constants;

import com.google.android.libraries.places.api.model.LocalTime;

public class MyOpeningHours {
    private LocalTime firstOpeningTime;
    private LocalTime firstClosingTime;
    private LocalTime lastOpeningTime;
    private LocalTime lastClosingTime;

    public MyOpeningHours() {
    }

    public MyOpeningHours(LocalTime firstOpeningTime, LocalTime firstClosingTime, LocalTime lastOpeningTime, LocalTime lastClosingTime) {
        this.firstOpeningTime = firstOpeningTime;
        this.firstClosingTime = firstClosingTime;
        this.lastOpeningTime = lastOpeningTime;
        this.lastClosingTime = lastClosingTime;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public String getOpeningStatus(){
        String status;
        String openingStatus = "No hour found!";
        java.time.LocalTime ct = java.time.LocalTime.now();
        LocalTime currentTime = new LocalTime() {
            @Override
            public int getHours() {
                return ct.getHour();
            }

            @Override
            public int getMinutes() {
                return ct.getMinute();
            }

            @Override
            public int describeContents() {
                return 0;
            }

            @Override
            public void writeToParcel(Parcel dest, int flags) {

            }
        };

        if (compareLocalTime(currentTime, firstOpeningTime) < 0) // (currentTime < firstOpeningTime) If we do not reach the first opening time
            openingStatus = Constants.CLOSE_AND_OPEN_AT_TEXT + firstOpeningTime.getHours() + Constants.H_TEXT + + firstOpeningTime.getMinutes();

        if (compareLocalTime(currentTime, lastClosingTime) < 0){ // (currentTime < lastClosingTime) If we are not at the closing time of the day
            if (compareLocalTime(firstOpeningTime, currentTime) <= 0 && compareLocalTime(currentTime, firstClosingTime) < 0){ //If we are at the first opening time
                //open until firstClosingHour
                status = Constants.OPEN_UNTIL_TEXT + firstClosingTime.getHours() + Constants.H_TEXT + firstClosingTime.getMinutes();
                openingStatus = closingSoon(currentTime, firstClosingTime, status);
            }
            else if (compareLocalTime(firstClosingTime, currentTime) <= 0 && compareLocalTime(currentTime, lastClosingTime) < 0){ //If we are at the break time
                //Closed. Open at lastOpeningHour
                openingStatus = Constants.CLOSE_AND_OPEN_AT_TEXT + lastOpeningTime.getHours() + Constants.H_TEXT + lastOpeningTime.getMinutes();
            }
            else if (compareLocalTime(firstOpeningTime, currentTime) <= 0){ //If we are at the second opening time
                //open until lastClosingHour
                status = Constants.OPEN_UNTIL_TEXT + lastClosingTime.getHours() + Constants.H_TEXT + lastClosingTime.getMinutes();
                openingStatus = closingSoon(currentTime, lastClosingTime, status);
            }
        }

        if (compareLocalTime(currentTime, lastClosingTime) > 0) //if we past the closing hour of the day
            openingStatus = Constants.CLOSED;

        return openingStatus;
    }

    private String closingSoon(LocalTime current, LocalTime closing, String status){
//        if (current.getHours()+1 == closing.getHours())
        String result = status;
        int minus;
        int currentMinutes = current.getHours() * 60 + current.getMinutes();
        int closingMinutes = closing.getHours() * 60 + closing.getMinutes();

        if (currentMinutes > closingMinutes)
            minus = currentMinutes - closingMinutes;
        else
            minus = closingMinutes - currentMinutes;

        if (minus <= 60)
            result = Constants.CLOSING_SOON;

        return result;
    }

    private LocalTime setHourTo24WhenMidnight(LocalTime time){
        int minutesSaved = time.getMinutes();

        if (time.getHours() == 0){
            time = new LocalTime() {
                @SuppressLint("Range")
                @Override
                public int getHours() {
                    return 24;
                }

                @Override
                public int getMinutes() {
                    return minutesSaved;
                }

                @Override
                public int describeContents() {
                    return 0;
                }

                @Override
                public void writeToParcel(Parcel dest, int flags) {

                }
            };
        }

        return time;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private int compareLocalTime(LocalTime l1, LocalTime l2){
        int comparaison = 0;

        if (l1 != null && l2 != null) {
            l1 = setHourTo24WhenMidnight(l1);
            l2 = setHourTo24WhenMidnight(l2);

            if (l1.getHours() < l2.getHours())
                comparaison = -1;
            else if (l1.getHours() > l2.getHours())
                comparaison = 1;
            else if (l1.getHours() == l2.getHours()){
                if (l1.getMinutes() < l2.getMinutes())
                    comparaison = -1;
                else if (l1.getMinutes() > l2.getMinutes())
                    comparaison = 1;
            }
        }

        return comparaison;
    }

    public LocalTime getFirstOpeningTime() {
        return firstOpeningTime;
    }

    public LocalTime getFirstClosingTime() {
        return firstClosingTime;
    }

    public LocalTime getLastOpeningTime() {
        return lastOpeningTime;
    }

    public LocalTime getLastClosingTime() {
        return lastClosingTime;
    }

    public void setFirstOpeningTime(LocalTime time) {
        this.firstOpeningTime = time;
    }

    public void setFirstClosingTime(LocalTime time) {
        this.firstClosingTime = time;
    }

    public void setLastOpeningTime(LocalTime time) {
        this.lastOpeningTime = time;
    }

    public void setLastClosingTime(LocalTime time) {
        this.lastClosingTime = time;
    }
}

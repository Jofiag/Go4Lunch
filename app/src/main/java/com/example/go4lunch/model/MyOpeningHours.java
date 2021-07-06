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
    private boolean isOpenToday = false;

    public MyOpeningHours() {
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public String getOpeningStatus(){
        String status;
        String openingStatus = "Opening hour not available!";
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

        if (isOpenToday) {
            if (compareLocalTime(currentTime, firstOpeningTime) < 0) // (currentTime < firstOpeningTime) If we do not reach the first opening time
                    openingStatus = Constants.CLOSE_AND_OPEN_AT_TEXT + firstOpeningTime.getHours() + Constants.H_TEXT + getMinuteIfNotZero(firstOpeningTime);

            if (compareLocalTime(currentTime, lastClosingTime) < 0){ // (currentTime < lastClosingTime) If we are not at the closing time of the day
                if (compareLocalTime(firstOpeningTime, currentTime) <= 0 && compareLocalTime(currentTime, firstClosingTime) < 0){ //If we are at the first opening time
                    //open until firstClosingHour
                    status = Constants.OPEN_UNTIL_TEXT + firstClosingTime.getHours() + Constants.H_TEXT + getMinuteIfNotZero(firstClosingTime);
                    openingStatus = closingSoon(currentTime, firstClosingTime, status);
                }
                else if (compareLocalTime(firstClosingTime, currentTime) <= 0 && compareLocalTime(currentTime, lastOpeningTime) < 0) //If we are at the break time
                    //Closed. Open at lastOpeningHour
                    openingStatus = Constants.CLOSE_AND_OPEN_AT_TEXT + lastOpeningTime.getHours() + Constants.H_TEXT + getMinuteIfNotZero(lastOpeningTime);
                else if (compareLocalTime(lastOpeningTime, currentTime) <= 0){ //If we are at the second opening time
                    //open until lastClosingHour
                    status = Constants.OPEN_UNTIL_TEXT + lastClosingTime.getHours() + Constants.H_TEXT + getMinuteIfNotZero(lastClosingTime);
                    openingStatus = closingSoon(currentTime, lastClosingTime, status);
                }
            }

            if (compareLocalTime(currentTime, lastClosingTime) > 0) //if we past the closing hour of the day
                openingStatus = Constants.CLOSED;
        }
        else
            openingStatus = Constants.CLOSED_TODAY;

        return openingStatus;
    }

    private String getMinuteIfNotZero(LocalTime time){
        String result ="";
        int minute = time.getMinutes();

        if (minute != 0)
            result = String.valueOf(minute);

        return result;
    }

    private String closingSoon(LocalTime current, LocalTime nextClosing, String status){
//        if (current.getHours()+1 == nextClosing.getHours())
        String result = status;
        int minus;
        int currentMinutes = current.getHours() * 60 + current.getMinutes();
        int closingMinutes = nextClosing.getHours() * 60 + nextClosing.getMinutes();

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

        if (time.getHours() == 0){ //When we're at midnight
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
            }; //We set the hour to 24 instead of letting it to , so that we can compare localTime.
        }

        return time;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private int compareLocalTime(LocalTime l1, LocalTime l2){
        int compare = 0;

        if (l1 != null && l2 != null) {
            l1 = setHourTo24WhenMidnight(l1);
            l2 = setHourTo24WhenMidnight(l2);

            if (l1.getHours() < l2.getHours())
                compare = -1;
            else if (l1.getHours() > l2.getHours())
                compare = 1;
            else if (l1.getHours() == l2.getHours()){
                if (l1.getMinutes() < l2.getMinutes())
                    compare = -1;
                else if (l1.getMinutes() > l2.getMinutes())
                    compare = 1;
            }
        }

        return compare;
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

    public boolean isOpenToday() {
        return isOpenToday;
    }

    public void setOpenToday(boolean openToday) {
        isOpenToday = openToday;
    }
}

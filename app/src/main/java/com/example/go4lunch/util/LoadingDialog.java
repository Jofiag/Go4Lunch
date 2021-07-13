package com.example.go4lunch.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.view.LayoutInflater;

import com.example.go4lunch.R;

public class LoadingDialog {
    @SuppressLint("StaticFieldLeak")
    private static LoadingDialog INSTANCE;

    private final Activity activity;
    private final AlertDialog.Builder builder;
    private AlertDialog alertDialog;

    public LoadingDialog(Activity activity) {
        this.activity = activity;
        this.builder = new AlertDialog.Builder(activity);

        initializeDialogBuilder();
    }

    public static LoadingDialog getInstance(Activity activity){
        if (INSTANCE == null)
            INSTANCE = new LoadingDialog(activity);

        return INSTANCE;
    }

    @SuppressLint("InflateParams")
    private void initializeDialogBuilder(){
        LayoutInflater inflater = activity.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.loading_dialog, null));

        setDialogCancelable(false);
    }

    public void setDialogCancelable(boolean isCancelable){
        builder.setCancelable(isCancelable);
        alertDialog = builder.create();
    }

    public void startLoadingDialog(){
        alertDialog.show();
    }

    public void dismissLoadingDialog(){
        alertDialog.dismiss();
    }
}

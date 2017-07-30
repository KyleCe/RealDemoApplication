package com.ce.game.realdemoapplication.test;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.Toast;

import com.ce.game.realdemoapplication.MyApplication;
import com.ce.game.realdemoapplication.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.ScheduledExecutorService;


public class GuideActivity extends Activity {

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        final ScheduledExecutorService executor = ((MyApplication) getApplication()).getmExecutor();
        io();
        View button = findViewById(R.id.text_view);
        button.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
//                startAsyncTask();
                executor.shutdown();
                Toast.makeText(GuideActivity.this, "click", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @SuppressLint("StaticFieldLeak")
    void startAsyncTask() {
        // This async task is an anonymous class and therefore has a hidden reference to the outer
        // class MainActivity. If the activity gets destroyed before the task finishes (e.g. rotation),
        // the activity instance will leak.
        new AsyncTask<Void, Void, Void>() {
            @Override protected Void doInBackground(Void... params) {
                // Do some slow work in background
                SystemClock.sleep(20000);
                return null;
            }
        }.execute();
    }

    private void io() {
        String name = "/storage/emulated/0/Download/com.ksmobile.launcher.hprof";
        try {
            FileInputStream is = new FileInputStream(new File(name));
            is.available();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

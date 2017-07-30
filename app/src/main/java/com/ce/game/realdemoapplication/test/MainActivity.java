package com.ce.game.realdemoapplication.test;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import java.lang.ref.WeakReference;

public class MainActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        async(this);
        asyncWithExecutor(this);
        startActivity(new Intent(this, BTaskActivity.class));
        finish();
    }

    private void async(final Activity activity) {
        new AsyncTask<Void, Void, Void>() {
            protected Void doInBackground(Void... params) {
                try {
                    Thread.sleep(500L);
                } catch (InterruptedException var4) {
                }
                try {
                    WeakReference<Activity> weakActivity = new WeakReference(activity);
                    weakActivity.get().setContentView(null);
                    weakActivity.clear();
                } catch (Exception var3) {
                    this.cancel(true);
                }
                return null;
            }
        }.execute();
    }

    private void asyncWithExecutor(final Activity activity) {
        final WeakReference<Activity> weakActivity = new WeakReference(activity);
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    weakActivity.get().setContentView(null);
                } catch (Exception var3) {
                }
            }
        });
    }
}

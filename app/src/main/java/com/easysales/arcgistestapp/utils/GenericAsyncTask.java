package com.easysales.arcgistestapp.utils;

/**
 * Created by lordp on 07.07.2017.
 */

import android.os.AsyncTask;
import android.util.Log;

import java.util.concurrent.Callable;

public class GenericAsyncTask<T> extends AsyncTask<Void, Void, T> {
    private final RunnableWithParam<T> doneRunnable;
    private final Callable<T> workerCallable;

    public GenericAsyncTask(RunnableWithParam<T> doneRunnable, Callable<T> workerCallable) {
        this.doneRunnable = doneRunnable;
        this.workerCallable = workerCallable;
    }

    @Override
    protected T doInBackground(Void... params) {
        try {
            if (workerCallable != null) {
                return workerCallable.call();
            }
        } catch (Exception e) {
            Log.e("GenericAsyncTask", "Failed execute task");
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(T t) {
        super.onPostExecute(t);
        if (doneRunnable != null) {
            doneRunnable.run(t);
        }
    }
}

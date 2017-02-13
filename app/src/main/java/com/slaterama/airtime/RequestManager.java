package com.slaterama.airtime;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.os.AsyncTaskCompat;

import com.slaterama.airtime.http.request.HttpRequest;
import com.slaterama.airtime.http.response.HttpResponse;

import org.json.JSONException;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;

/**
 * The main class that handles the HTTP request queue.
 */
public class RequestManager {

   /**
    * The actual {@link Queue} of HTTP requests.
    */
   @NonNull
   private final Queue<HttpRequest> mQueue;

   /**
    * The {@link AsyncTask} that will loop through and process HTTP requests.
    */
   @NonNull
   private final RequestTask mRequestTask;

   /**
    * A listener that will listen for important messages from this manager.
    */
   @NonNull
   private final RequestListener mListener;

   public RequestManager(@NonNull RequestListener listener) {
      mListener = listener;
      mQueue = new LinkedList<>();
      mRequestTask = new RequestTask();
   }

   /**
    * Adds the requests to the request queue.
    * @param requests
    * @return
    */
   public boolean add(HttpRequest... requests) {
      return addAll(Arrays.asList(requests));
   }

   /**
    * Adds all of the requests in the collection to the request queue.
    * @param requests
    * @return
    */
   public boolean addAll(Collection<HttpRequest> requests) {
      AsyncTask.Status status = mRequestTask.getStatus();
      if (status == AsyncTask.Status.FINISHED) {
         return false;
      }

      boolean changed ;
      synchronized (mQueue) {
         changed = mQueue.addAll(requests);
         if (changed) {
            mQueue.notify();
         }
      }

      if (status == AsyncTask.Status.PENDING) {
         AsyncTaskCompat.executeParallel(mRequestTask);
      }
      return changed;
   }

   public void reset() {
      synchronized (mQueue) {
         mQueue.clear();
      }
   }

   private class RequestTask
       extends AsyncTask<Void, HttpResponse, Void> {

      @Override
      protected Void doInBackground(Void... voids) {
         HttpRequest request;

         while (!isCancelled()) {
            synchronized (mQueue) {
               while (mQueue.isEmpty()) {
                  try {
                     mQueue.wait();
                  } catch (InterruptedException e) {
                     // Ignore interruptions
                  }
               }

               request = mQueue.poll();
            }

            try {
               final HttpResponse response = request.getResponse();
               publishProgress(response);
            } catch (IOException | JSONException e) {
               e.printStackTrace();
               mListener.onRequestError(e);
            }
         }

         return null;
      }

      @Override
      protected void onProgressUpdate(HttpResponse... values) {
         HttpResponse response = values[0];
         mListener.onRequestResponse(response);
      }
   }

   public interface RequestListener {
      void onRequestResponse(HttpResponse response);
      void onRequestError(Exception e);
   }
}

package com.slaterama.airtime.http.request;

import android.support.annotation.NonNull;

import com.slaterama.airtime.http.response.HttpResponse;
import com.slaterama.airtime.http.response.StartResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;

/**
 * An HttpRequest class that encapsulates a "start" request.
 */
public class StartRequest extends HttpRequest {

   private static final String START_FILE = "start";

   /**
    * Opens the {@link HttpURLConnection} needed by this request.
    * @return The open HttpURLConnection.
    * @throws IOException
    * @throws JSONException
    */
   @Override
   public HttpURLConnection openConnection()
       throws IOException, JSONException {
      HttpURLConnection conn = createConnection(START_FILE);
      conn.setRequestMethod(METHOD_GET);
      return conn;
   }

   /**
    * Create a new {@link StartResponse} instance.
    * @param responseCode The reponse code.
    * @param obj The {@link JSONObject} to use to populate the response.
    * @return The "start" HTTP response.
    * @throws JSONException
    */
   @Override
   protected HttpResponse getResponse(
       int responseCode,
       @NonNull JSONObject obj)
       throws JSONException {
      return new StartResponse(responseCode, obj);
   }
}

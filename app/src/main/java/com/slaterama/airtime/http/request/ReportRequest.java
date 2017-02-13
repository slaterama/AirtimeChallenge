package com.slaterama.airtime.http.request;

import android.support.annotation.NonNull;
import android.util.SparseArray;

import com.slaterama.airtime.http.response.HttpResponse;
import com.slaterama.airtime.http.response.ReportResponse;
import com.slaterama.airtime.model.Writing;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;

/**
 * An HttpRequest class that encapsulates a "report" request.
 */
public class ReportRequest extends HttpRequest {

   private static final String REPORT_FILE = "report";
   private static final String JSON_MESSAGE = "message";

   /**
    * The concatenated writings string.
    */
   @NonNull
   private final String mMessage;

   /**
    * Concatenates the writings into a single string.
    * @param writings An (ordered) SparseArray of individual writings.
    */
   public ReportRequest(SparseArray<Writing> writings) {
      StringBuilder builder = new StringBuilder();
      int size = writings.size();
      for (int i = 0; i < size; i++) {
         Writing writing = writings.valueAt(i);
         builder.append(writing.getText());
      }
      mMessage = builder.toString();
   }

   /**
    * Opens the {@link HttpURLConnection} needed by this request.
    * @return The open HttpURLConnection.
    * @throws IOException
    * @throws JSONException
    */
   @Override
   public HttpURLConnection openConnection()
       throws IOException, JSONException {
      HttpURLConnection conn = createConnection(REPORT_FILE);
      conn.setRequestMethod(METHOD_POST);
      writeBody(conn, createBody());
      return conn;
   }

   /**
    * Create a new {@link ReportResponse} instance.
    * @param responseCode The reponse code.
    * @param obj The {@link JSONObject} to use to populate the response.
    * @return The "report" HTTP response.
    * @throws JSONException
    */
   @Override
   protected HttpResponse getResponse(
       int responseCode,
       @NonNull JSONObject obj)
       throws JSONException {
      return new ReportResponse(responseCode, obj);
   }

   /**
    * Creates the body to send along with the request.
    * @return The body as a JSON string.
    * @throws JSONException
    */
   private String createBody()
       throws JSONException {
      JSONObject json = new JSONObject();
      json.put(JSON_MESSAGE, mMessage);
      return json.toString();
   }
}

package com.slaterama.airtime.http.response;

import android.support.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * An {@link HttpResponse} class that represents the response from a
 * "report" request.
 */
public class ReportResponse extends HttpResponse {

   private static final String JSON_RESPONSE = "response";

   /**
    * The response string returned by the "report" request.
    */
   @NonNull
   private final String mResponse;

   public ReportResponse(int responseCode, @NonNull JSONObject obj)
      throws JSONException {
      super(responseCode);
      mResponse = obj.getString(JSON_RESPONSE);
   }

   @NonNull
   public String getResponse() {
      return mResponse;
   }

   @Override
   public String toString() {
      return "ReportResponse{" +
          "mResponse='" + mResponse + '\'' +
          '}';
   }
}

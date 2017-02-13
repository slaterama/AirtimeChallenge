package com.slaterama.airtime.http.response;

import android.support.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * An {@link HttpResponse} class that represents an error.
 */
public class ErrorResponse extends HttpResponse {

   /**
    * The error message returned along with this response.
    */
   private static final String JSON_ERROR = "error";

   @NonNull
   private final String mError;

   public ErrorResponse(int responseCode, @NonNull JSONObject obj)
       throws JSONException {
      super(responseCode);
      mError = obj.getString(JSON_ERROR);
   }

   @NonNull
   public String getError() {
      return mError;
   }
}

package com.slaterama.airtime.http.response;

/**
 * A base class that represents an HTTP response.
 */
public abstract class HttpResponse {

   /**
    * The response code encountered when parsing this response.
    */
   protected final int mReponseCode;

   public HttpResponse(int responseCode) {
      mReponseCode = responseCode;
   }

   public int getReponseCode() {
      return mReponseCode;
   }
}

package com.slaterama.airtime.http.request;

import android.support.annotation.NonNull;

import com.slaterama.airtime.http.response.ErrorResponse;
import com.slaterama.airtime.http.response.HttpResponse;
import com.slaterama.airtime.http.response.NotFoundResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * The base class representing all HTTP requests.
 */
public abstract class HttpRequest {

   private static final String URL_PROTOCOL = "http";
   private static final String URL_HOST = "challenge2.airtime.com";
   private static final int URL_PORT = 10001;

   private static final String HEADER_EMAIL_KEY = "x-commander-email";
   private static final String HEADER_EMAIL_VALUE = "slaterama@gmail.com";

   private static final String UTF_8 = "UTF-8";
   private static final String NEWLINE = "\n";

   protected static final String METHOD_GET = "GET";
   protected static final String METHOD_POST = "POST";

   /**
    * Creates a basic {@link HttpURLConnection} instance.
    * @param file The file to use when creating the URL for this request.
    * @return The HttpURLConnection instance.
    * @throws IOException
    * @throws JSONException
    */
   protected static HttpURLConnection createConnection(String file)
       throws IOException, JSONException {
      URL url = new URL(URL_PROTOCOL, URL_HOST, URL_PORT, file);
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setRequestProperty(HEADER_EMAIL_KEY, HEADER_EMAIL_VALUE);
      return conn;
   }

   /**
    * Writes the given JSON string to the request body.
    * @param conn An {@link HttpURLConnection} instance.
    * @param json The JSON string to write.
    * @return The original HttpURLConnection instance.
    * @throws IOException
    */
   protected static HttpURLConnection writeBody(
       @NonNull HttpURLConnection conn,
       @NonNull String json)
       throws IOException {
      byte[] bytes = json.getBytes(UTF_8);
      conn.setRequestProperty("Content-Type", "application/json");
      conn.setRequestProperty("Accept", "application/json");
      conn.setRequestProperty(
          "Content-length",
          String.valueOf(bytes.length));
      OutputStream outputStream = conn.getOutputStream();
      outputStream.write(bytes);
      outputStream.close();
      return conn;
   }

   public abstract HttpURLConnection openConnection()
       throws IOException, JSONException;

   protected abstract HttpResponse getResponse(
       int responseCode,
       @NonNull JSONObject obj)
       throws JSONException;

   /**
    * Creates an {@link HttpResponse} of the appropriate type based on
    * the result of this request.
    * @return An HttpResponse instance.
    * @throws IOException
    * @throws JSONException
    */
   public HttpResponse getResponse()
       throws IOException, JSONException {
      HttpResponse response = null;
      HttpURLConnection connection = null;
      try {
         // Open the connection and read the response, one line at a time.
         // Then attempt to create the appropriate response type.
         connection = openConnection();
         final InputStream stream =
             new BufferedInputStream(connection.getInputStream());
         final int responseCode = connection.getResponseCode();
         switch (responseCode) {
            case HttpURLConnection.HTTP_NOT_FOUND:
               // Create a "not found" response
               response = new NotFoundResponse(responseCode);
               break;
            case HttpURLConnection.HTTP_BAD_REQUEST:
            case HttpURLConnection.HTTP_OK:
            default:
               // Read the response into a JSONObject
               BufferedReader reader =
                   new BufferedReader(new InputStreamReader(stream, UTF_8));
               StringBuilder builder = new StringBuilder();
               String line;
               while ((line = reader.readLine()) != null) {
                  if (builder.length() > 0) {
                     builder.append(NEWLINE);
                  }
                  builder.append(line);
               }
               final JSONObject obj = new JSONObject(builder.toString());
               if (responseCode == HttpURLConnection.HTTP_BAD_REQUEST) {
                  // Create an error response
                  response = new ErrorResponse(responseCode, obj);
               } else {
                  // Create a response based on the request
                  response = getResponse(responseCode, obj);
               }
         }
      } catch (IOException | JSONException e) {
         throw e;
      } finally {
         if (connection != null) {
            connection.disconnect();
         }
      }
      return response;
   }
}

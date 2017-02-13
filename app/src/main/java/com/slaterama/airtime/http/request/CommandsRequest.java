package com.slaterama.airtime.http.request;

import android.support.annotation.NonNull;

import com.slaterama.airtime.http.response.CommandsResponse;
import com.slaterama.airtime.http.response.HttpResponse;
import com.slaterama.airtime.model.Command;
import com.slaterama.airtime.model.Drone;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * An HttpRequest class that encapsulates a "commands" request.
 */
public class CommandsRequest extends HttpRequest {

   private static final String COMMANDS_FILE = "drone/%s/commands";

   /**
    * The {@link Drone} associated with this request.
    */
   @NonNull
   private final Drone mDrone;

   /**
    * The list of commands being sent with this request.
    */
   @NonNull
   private final List<Command> mCommands;

   public CommandsRequest(
       @NonNull Drone drone,
       @NonNull List<Command> commands) {
      mDrone = drone;
      mCommands = Collections.unmodifiableList(commands);
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
      HttpURLConnection conn = createConnection(
          String.format(Locale.US, COMMANDS_FILE, mDrone.getId()));
      conn.setRequestMethod(METHOD_POST);
      writeBody(conn, createBody());
      return conn;
   }

   /**
    * Create a new {@link CommandsResponse} instance.
    * @param responseCode The reponse code.
    * @param obj The {@link JSONObject} to use to populate the response.
    * @return The "commands" HTTP response.
    * @throws JSONException
    */
   @Override
   protected HttpResponse getResponse(
       int responseCode,
       @NonNull JSONObject obj)
       throws JSONException {
      return new CommandsResponse(responseCode, mDrone, obj);
   }

   /**
    * Creates the body to send along with the request.
    * @return The body as a JSON string.
    * @throws JSONException
    */
   private String createBody()
       throws JSONException {
      JSONObject json = new JSONObject();
      for (Command command : mCommands) {
         String commandId = command.getCommandId();
         JSONObject value = new JSONObject();
         value.put(command.getName(), command.getRoomId());
         json.put(commandId, value);
      }
      return json.toString();
   }
}

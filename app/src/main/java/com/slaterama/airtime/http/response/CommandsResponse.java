package com.slaterama.airtime.http.response;

import android.support.annotation.NonNull;
import android.util.SparseArray;

import com.slaterama.airtime.model.Command;
import com.slaterama.airtime.model.Drone;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * An {@link HttpResponse} class that represents the response from a
 * "commands" request.
 */
public class CommandsResponse extends HttpResponse {

   private static final String JSON_CONNECTIONS = "connections";
   private static final String JSON_ERROR = "error";
   private static final String JSON_ORDER = "order";
   private static final String JSON_WRITING = "writing";

   /**
    * Convenience method that creates a {@link Result} class of the
    * appropriate type based on the supplied JSON.
    * @param command The {@link Command} associated with this result.
    * @param obj A {@link JSONObject} containing the data associated with this
    *            result.
    * @return A Result instance.
    * @throws JSONException
    */
   private static Result newResult(
       @NonNull Command command,
       @NonNull JSONObject obj)
      throws JSONException {
      if (obj.has(JSON_ERROR)) {
         return new ErrorResult(command, obj);
      }

      String name = command.getName();
      if (name.equals(Command.EXPLORE)) {
         return new ConnectionsResult(command, obj);
      } else if (name.equals(Command.READ)) {
         return new WritingResult(command, obj);
      } else {
         throw new JSONException("Error parsing commands response");
      }
   }

   /**
    * The {@link Drone} associated with this reponse.
    */
   @NonNull
   private final Drone mDrone;

   /**
    * A list of {@link Result}s returned along with this response.
    */
   @NonNull
   private final SparseArray<Result> mResults;

   public CommandsResponse(
       int responseCode,
       @NonNull Drone drone,
       @NonNull JSONObject obj)
       throws JSONException {
      super(responseCode);
      mDrone = drone;
      mResults = new SparseArray<>();

      Iterator<String> keys = obj.keys();
      while (keys.hasNext()) {
         String commandId = keys.next();
         Command command = Command.fromCommandId(commandId);
         if (command != null) {
            mResults.put(
                command.getIndex(),
                newResult(
                    command,
                    obj.getJSONObject(commandId)));
         }
      }
   }

   @NonNull
   public Drone getDrone() {
      return mDrone;
   }

   public int getResultCount() {
      return mResults.size();
   }

   public Result resultAt(int index) {
      return mResults.valueAt(index);
   }

   @Override
   public String toString() {
      return "CommandsResponse{" +
          "mDrone=" + mDrone +
          ", mResults=" + mResults +
          '}';
   }

   /**
    * A generic result class.
    */
   public static abstract class Result {

      @NonNull
      private final Command mCommand;

      public Result(@NonNull Command command, @NonNull JSONObject obj)
          throws JSONException {
         mCommand = command;
      }

      @NonNull
      public Command getCommand() {
         return mCommand;
      }
   }

   /**
    * A result class representing a "connections" result (i.e. a list of
    * room IDs.)
    */
   public static class ConnectionsResult extends Result {

      @NonNull
      private final List<String> mConnections;

      public ConnectionsResult(
          @NonNull Command command,
          @NonNull JSONObject obj)
          throws JSONException {
         super(command, obj);
         JSONArray arr = obj.getJSONArray(JSON_CONNECTIONS);
         int length = arr.length();
         List<String> connections = new ArrayList<>(length);
         for (int i = 0; i < length; i++) {
            connections.add(arr.getString(i));
         }
         mConnections = Collections.unmodifiableList(connections);
      }

      @NonNull
      public List<String> getConnections() {
         return mConnections;
      }
   }

   /**
    * A result class representing a "writing" result (i.e. writing text
    * and an order number.)
    */
   public static class WritingResult extends Result {

      @NonNull
      private final String mText;

      private final int mOrder;

      public WritingResult(
          @NonNull Command command,
          @NonNull JSONObject obj)
          throws JSONException {
         super(command, obj);
         mText = obj.getString(JSON_WRITING);
         mOrder = obj.getInt(JSON_ORDER);
      }

      @NonNull
      public String getText() {
         return mText;
      }

      public int getOrder() {
         return mOrder;
      }
   }

   /**
    * A result class representing an error result. (i.e. drone is still
    * busy, too many commands, etc.)
    */
   public static class ErrorResult extends Result {

      @NonNull
      private final String mError;

      public ErrorResult(
          @NonNull Command command,
          @NonNull JSONObject obj)
          throws JSONException {
         super(command, obj);
         mError = obj.getString(JSON_ERROR);
      }

      @NonNull
      public String getError() {
         return mError;
      }
   }
}

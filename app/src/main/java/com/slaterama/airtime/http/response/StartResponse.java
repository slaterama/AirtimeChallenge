package com.slaterama.airtime.http.response;

import android.support.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * An {@link HttpResponse} class that represents the response from a
 * "start" request.
 */
public class StartResponse extends HttpResponse {

   private static final String JSON_DRONES = "drones";
   private static final String JSON_ROOM_ID = "roomId";

   /**
    * The room ID returned in this response.
    */
   @NonNull
   private final String mRoomId;

   /**
    * A list of drone IDs returned by this response.
    */
   @NonNull
   private final List<String> mDroneIds;

   public StartResponse(int responseCode, @NonNull JSONObject obj)
       throws JSONException {
      super(responseCode);
      mRoomId = obj.getString(JSON_ROOM_ID);
      JSONArray arr = obj.getJSONArray(JSON_DRONES);
      int length = arr.length();
      List<String> droneIds = new ArrayList<>(length);
      for (int i = 0; i < length; i++) {
         droneIds.add(arr.getString(i));
      }
      mDroneIds = Collections.unmodifiableList(droneIds);
   }

   @NonNull
   public String getRoomId() {
      return mRoomId;
   }

   @NonNull
   public List<String> getDroneIds() {
      return mDroneIds;
   }

   @Override
   public String toString() {
      return "StartResponse{" +
          "mResponseCode='" + mReponseCode + '\'' +
          ", mRoomId='" + mRoomId + '\'' +
          ", mDroneIds=" + mDroneIds +
          '}';
   }
}

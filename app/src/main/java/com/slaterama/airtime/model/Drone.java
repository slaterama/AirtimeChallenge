package com.slaterama.airtime.model;

import android.support.annotation.NonNull;

/**
 * A class representing a drone.
 */
public class Drone {

   @NonNull
   private final String mId;

   public Drone(@NonNull String id) {
      mId = id;
   }

   @NonNull
   public String getId() {
      return mId;
   }

   @Override
   public String toString() {
      return "Drone{" +
          "mId='" + mId + '\'' +
          '}';
   }
}

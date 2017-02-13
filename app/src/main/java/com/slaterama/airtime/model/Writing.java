package com.slaterama.airtime.model;

import android.support.annotation.NonNull;

/**
 * A class that represents writing found in a room.
 */
public class Writing {

   public static final int INVALID_WRITING = -1;

   @NonNull
   private final String mText;

   private final int mOrder;

   public Writing(@NonNull String text, int order) {
      mText = text;
      mOrder = order;
   }

   @NonNull
   public String getText() {
      return mText;
   }

   public int getOrder() {
      return mOrder;
   }

   @Override
   public String toString() {
      return "Writing{" +
          "mText='" + mText + '\'' +
          ", mOrder=" + mOrder +
          '}';
   }
}

package com.slaterama.airtime.model;

import android.support.annotation.NonNull;
import android.text.TextUtils;

/**
 * A class representing a drone command, i.e. "explore" or "read".
 */
public class Command {

   private static final String DELIMITER = ".";
   private static final String DELIMITER_REGEX = "\\.";

   public static final String EXPLORE = "explore";
   public static final String READ = "read";

   /**
    * Convenience method to create a command ID string.
    * @param index The index number of the command.
    * @param name The name ("explore" or "read") of the command.
    * @param roomId The room ID associated with the command.
    * @return The command ID string.
    */
   private static String makeCommandId(
       int index,
       @NonNull String name,
       @NonNull String roomId) {
      return TextUtils.join(
          DELIMITER,
          new Object[]{ index, name, roomId });
   }

   /**
    * Convenience method to create a {@link Command} instance based on
    * a command ID string.
    * @param commandId The command ID string.
    * @return A Command instance.
    */
   public static Command fromCommandId(@NonNull String commandId) {
      String[] tokens = TextUtils.split(commandId, DELIMITER_REGEX);
      try {
         return new Command(Integer.parseInt(tokens[0]), tokens[1], tokens[2]);
      } catch (IndexOutOfBoundsException e) {
         return null;
      }
   }

   /**
    * The numberical index of this command.
    */
   private int mIndex;

   /**
    * The name ("explore", "read") of this command.
    */
   @NonNull
   private final String mName;

   /**
    * The room ID associated with this command.
    */
   @NonNull
   private final String mRoomId;

   /**
    * The command ID string.
    */
   @NonNull
   private String mCommandId;

   public Command(@NonNull String name, @NonNull String roomId) {
      this(-1, name, roomId);
   }

   public Command(int index, @NonNull String name, @NonNull String roomId) {
      mIndex = index;
      mName = name;
      mRoomId = roomId;
      mCommandId = makeCommandId(index, name, roomId);
   }

   public int getIndex() {
      return mIndex;
   }

   public void setIndex(int index) {
      mIndex = index;
      mCommandId = makeCommandId(index, mName, mRoomId);
   }

   @NonNull
   public String getName() {
      return mName;
   }

   @NonNull
   public String getRoomId() {
      return mRoomId;
   }

   @NonNull
   public String getCommandId() {
      return mCommandId;
   }

   @Override
   public String toString() {
      return "Command{" +
          "mIndex=" + mIndex +
          ", mName='" + mName + '\'' +
          ", mRoomId='" + mRoomId + '\'' +
          '}';
   }
}

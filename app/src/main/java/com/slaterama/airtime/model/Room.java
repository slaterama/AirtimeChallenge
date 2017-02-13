package com.slaterama.airtime.model;

import android.support.annotation.NonNull;

import java.util.List;

/**
 * A class representing a room.
 */
public class Room {

   @NonNull
   private final String mId;

   /**
    * Whether connections have been requested for this room (but haven't
    * come back yet).
    */
   private boolean mAwaitingConnections = false;

   /**
    * Whether writing has been requested for this room (but hasn't
    * come back yet).
    */
   private boolean mAwaitingWriting = false;

   /**
    * The connecting rooms associated with this room.
    */
   private List<Room> mConnections;

   /**
    * The writing associated with this room.
    */
   private Writing mWriting;

   public Room(@NonNull String id) {
      mId = id;
   }

   @NonNull
   public String getId() {
      return mId;
   }

   public boolean isAwaitingConnections() {
      return mAwaitingConnections;
   }

   public void setAwaitingConnections(boolean awaitingConnections) {
      mAwaitingConnections = awaitingConnections;
   }

   public boolean isAwaitingWriting() {
      return mAwaitingWriting;
   }

   public void setAwaitingWriting(boolean awaitingWriting) {
      mAwaitingWriting = awaitingWriting;
   }

   /**
    * Returns whether connections have been returned for this room.
    * @return Whether connections have been returned for this room.
    */
   public boolean hasConnections() {
      return (mConnections != null);
   }

   public List<Room> getConnections() {
      return mConnections;
   }

   public void setConnections(List<Room> connections) {
      mConnections = connections;
      mAwaitingConnections = false;
   }

   /**
    * Returns whether writing has been returned for this room. Note that
    * "hasWriting" refers not only to "valid" writing, but also
    * {@link Writing} instances that represent rooms with no writing.
    * @return Whether writing has been returned for this room.
    */
   public boolean hasWriting() {
      return (mWriting != null);
   }

   public Writing getWriting() {
      return mWriting;
   }

   public void setWriting(Writing writing) {
      mWriting = writing;
      mAwaitingWriting = false;
   }

   @Override
   public String toString() {
      return "Room{" +
          "mId='" + mId + '\'' +
          '}';
   }
}

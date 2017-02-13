package com.slaterama.airtime;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import com.slaterama.airtime.model.Command;
import com.slaterama.airtime.model.Drone;
import com.slaterama.airtime.model.Room;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

/**
 * The main algorithm for drone exploration!
 */
public class DroneTask extends AsyncTask<Void, Room, Integer> {

   private static final int BUFFER_SIZE = 5;

   /**
    * A little trick to randomize connecting rooms to try to get the
    * drones on separate paths.
    */
   private static final boolean SHUFFLE = true;

   /**
    * The {@link Drone} associated with this task.
    */
   @NonNull
   private final Drone mDrone;

   /**
    * A listener that will listen for important drone notifications.
    */
   @NonNull
   private final DroneTaskListener mListener;

   /**
    * A command buffer that will batch a series of commands.
    */
   @NonNull
   private final Queue<Command> mCommandBuffer;

   /**
    * A linked list of rooms that will be maintained as a depth-first
    * search of rooms. To accomplish this in an asynchronous environment,
    * newly-discovered rooms will always be pre-pended to the list as they
    * arrive.
    */
   @NonNull
   private final Deque<Room> mRooms;

   /**
    * A set of rooms known by this task. Even if we don't visit the room in
    * question, we consider it "known" if we take a look at whether it has
    * already been visited by another drone.
    */
   @NonNull
   private final Set<Room> mKnown;

   /**
    * The room currently being queried by this task. This does not necessarily
    * mean the drone is "in" the room.
    */
   private Room mCurrentRoom;

   /**
    * Whether the drone is busy (i.e. it has dispatch a set of commands and
    * is awaiting the result.
    */
   private boolean mBusy;

   public DroneTask(
       @NonNull Drone drone,
       @NonNull DroneTaskListener listener) {
      mDrone = drone;
      mListener = listener;
      mCommandBuffer = new LinkedList<>();
      mRooms = new LinkedList<>();
      mKnown = new HashSet<>();
   }

   @NonNull
   public Drone getDrone() {
      return mDrone;
   }

   /**
    * Sets whether this task is busy (i.e. awaiting the response of a
    * "command" HTTP request.
    * @param busy Whether this task is busy.
    */
   public void setBusy(boolean busy) {
      mBusy = busy;
      if (!busy) {
         synchronized (mRooms) {
            mRooms.notify();
         }
      }
   }

   /**
    * Returns true if we are currently querying the given room.
    * @param room The room to test.
    * @return Whether we are currently querying the given room.
    */
   public boolean isRoomCurrent(@NonNull Room room) {
      return room.equals(mCurrentRoom);
   }

   @Override
   protected Integer doInBackground(Void... voids) {
      int roomsProcessed = 0;

      synchronized (mRooms) {
         mCurrentRoom = mRooms.pollFirst();
      }

      // As long as we have rooms to query, keep looping.
      // Note that more rooms may get pre-pended to the list.
      while (mCurrentRoom != null && !isCancelled()) {
         // Mark this room as "known"
         mKnown.add(mCurrentRoom);

         String roomId = mCurrentRoom.getId();

         // Optionally add a read command
         if (!mCurrentRoom.hasWriting() &&
             !mCurrentRoom.isAwaitingWriting()) {
            mCommandBuffer.add(new Command(Command.READ, roomId));
            mCurrentRoom.setAwaitingWriting(true);
         }

         // Optionally add an explore command
         if (!mCurrentRoom.hasConnections() &&
             !mCurrentRoom.isAwaitingConnections()) {
            mCommandBuffer.add(new Command(Command.EXPLORE, roomId));
            mCurrentRoom.setAwaitingConnections(true);
         }

         // If the room already has connections, we should add them now.
         if (mCurrentRoom.hasConnections()) {
            addAll(mCurrentRoom.getConnections());
         }

         // If we are awaiting connections or our command buffer is full,
         // we need to send commands.
         if (mCurrentRoom.isAwaitingConnections()
             || mCommandBuffer.size() >= BUFFER_SIZE) {
            sendCommands();
         }

         roomsProcessed++;

         // We need to wait here if the current room is awaiting connections,
         // or if we are awaiting the results of a commands request.
         synchronized (mRooms) {
            while (mCurrentRoom.isAwaitingConnections() || mBusy) {
               try {
                  mRooms.wait();
               } catch (InterruptedException e) {
                  // Ignore interruptions
               }
            }

            mCurrentRoom = mRooms.pollFirst();
         }
      }

      return roomsProcessed;
   }

   @Override
   protected void onPostExecute(Integer integer) {
      mListener.onFinished(this);
   }

   private void sendCommands() {
      if (mCommandBuffer.isEmpty()) {
         // No commands to send
         return;
      }

      mBusy = true;

      int index = 0;
      List<Command> commands = new ArrayList<>(BUFFER_SIZE);
      while (!mCommandBuffer.isEmpty() && commands.size() < BUFFER_SIZE) {
         Command command = mCommandBuffer.poll();
         command.setIndex(++index);
         commands.add(command);
      }

      mListener.onSendCommands(this, Collections.unmodifiableList(commands));
   }

   public boolean add(@NonNull Room... rooms) {
      return addAll(Arrays.asList(rooms));
   }

   /**
    * Adds all of the rooms in the given collection to the beginning of the
    * linked this. This will essentially create a BFS search. NOTE that even
    * without shuffling, the rooms will be added to the front of the linked
    * list in opposite order. However order shouldn't matter in this case.
    * @param rooms The collection of rooms to add.
    * @return
    */
   public boolean addAll(@NonNull List<Room> rooms) {
      if (rooms.size() > 1 && SHUFFLE) {
         Collections.shuffle(rooms);
      }

      boolean changed = false;
      synchronized (mRooms) {
         for (Room room : rooms) {
            if (!mKnown.contains(room) &&
                mRooms.offerFirst(room)) {
               changed = true;
            }
         }
         mRooms.notify();
      }
      return changed;
   }

   /**
    * An interface with important methods needed by this task.
    */
   public interface DroneTaskListener {
      void onSendCommands(
          @NonNull DroneTask task,
          @NonNull List<Command> commands);
      void onFinished(@NonNull DroneTask task);
   }
}

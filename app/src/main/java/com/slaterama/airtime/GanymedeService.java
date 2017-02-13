package com.slaterama.airtime;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.util.SparseArray;

import com.slaterama.airtime.http.request.CommandsRequest;
import com.slaterama.airtime.http.request.ReportRequest;
import com.slaterama.airtime.http.request.StartRequest;
import com.slaterama.airtime.http.response.CommandsResponse;
import com.slaterama.airtime.http.response.CommandsResponse.ConnectionsResult;
import com.slaterama.airtime.http.response.CommandsResponse.ErrorResult;
import com.slaterama.airtime.http.response.CommandsResponse.Result;
import com.slaterama.airtime.http.response.CommandsResponse.WritingResult;
import com.slaterama.airtime.http.response.ErrorResponse;
import com.slaterama.airtime.http.response.HttpResponse;
import com.slaterama.airtime.http.response.NotFoundResponse;
import com.slaterama.airtime.http.response.ReportResponse;
import com.slaterama.airtime.http.response.StartResponse;
import com.slaterama.airtime.model.Command;
import com.slaterama.airtime.model.Drone;
import com.slaterama.airtime.model.Room;
import com.slaterama.airtime.model.Writing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static com.slaterama.airtime.model.Writing.INVALID_WRITING;

/**
 * The main Service that manages drones, rooms, and writing.
 */
public class GanymedeService extends Service
   implements RequestManager.RequestListener,
              DroneTask.DroneTaskListener {

   private static final boolean DEBUG = true;

   private static final String TAG = GanymedeService.class.getSimpleName();

   public static final String ACTION_FINISHED = "finished";
   public static final String EXTRA_RESPONSE = "response";

   /**
    * Class that manages HTTP requests.
    */
   @NonNull
   private final RequestManager mRequestManager;

   /**
    * Whether {@link GanymedeBinder#start()} has been previously called.
    */
   private boolean mStarted;

   /**
    * A collection of all drones currently in play.
    */
   @NonNull
   private final Map<String, Drone> mDrones;

   /**
    * A collection of all drone tasks currently running.
    */
   @NonNull
   private final Map<String, DroneTask> mDroneTasks;

   /**
    * A collection of all rooms currently known by the system.
    */
   @NonNull
   private final Map<String, Room> mRooms;

   /**
    * A collection of all valid writings discovered by the drones.
    */
   @NonNull
   private final SparseArray<Writing> mWritings;

   private GanymedeBinder mBinder;

   public GanymedeService() {
      super();
      mRequestManager = new RequestManager(this);
      mDrones = new HashMap<>();
      mDroneTasks = new HashMap<>();
      mRooms = new HashMap<>();
      mWritings = new SparseArray<>();
   }

   @Override
   public void onCreate() {
      super.onCreate();
   }

   @Override
   public IBinder onBind(Intent intent) {
      if (mBinder == null) {
         mBinder = new GanymedeBinder();
      }
      return mBinder;
   }

   /**
    * Handles HTTP request errors.
    * @param e The error that was encountered
    */
   @Override
   public void onRequestError(Exception e) {
      if (DEBUG) {
         Log.e(TAG, "onRequestError", e);
      }
   }

   /**
    * Handles all successful HTTP request responses.
    * @param response The {@link HttpResponse}.
    */
   @Override
   public void onRequestResponse(HttpResponse response) {
      if (DEBUG) {
         Log.d(TAG, "Received " + response);
      }
      if (response instanceof StartResponse) {
         handleStartResponse((StartResponse) response);
      } else if (response instanceof CommandsResponse) {
         handleCommandsReponse((CommandsResponse) response);
      } else if (response instanceof ReportResponse) {
         handleReportResponse((ReportResponse) response);
      } else if (response instanceof NotFoundResponse) {
         handleNotFoundResponse((NotFoundResponse) response);
      } else if (response instanceof ErrorResponse) {
         handleErrorResponse((ErrorResponse) response);
      }
   }

   /**
    * Handles when a drone task signifies that a command request should
    * be sent.
    * @param task The drone task that sent this message.
    * @param commands A {@link List} of commands to send.
    */
   @Override
   public void onSendCommands(
       @NonNull DroneTask task,
       @NonNull List<Command> commands) {
      mRequestManager.add(new CommandsRequest(
          task.getDrone(), commands));
   }

   /**
    * Sent by a drone task when a drone has queried all rooms.
    * NOTE: "Queried" does not mean that a drone physically visited a room.
    * In each case we check whether a room needs processing before we would
    * send a drone to examine it. But even if a room has already been
    * processed, we "query" that room in the data to be sure.
    * @param task The drone task that sent this message.
    */
   @Override
   public void onFinished(@NonNull DroneTask task) {
      Drone drone = task.getDrone();
      if (DEBUG) {
         Log.d(TAG, "Finished: " + drone);
      }
      mDroneTasks.remove(drone.getId());
      if (mDroneTasks.size() == 0) {
         if (DEBUG) {
            Log.d(TAG, "All drones have finished!!!");
         }
         mRequestManager.add(
             new ReportRequest(mWritings));
      }
   }

   /**
    * Returns the {@link Drone} that corresponds to the given id,
    * creating one if necessary.
    * @param droneId The drone id.
    * @return The drone with the given id.
    */
   private Drone resolveDrone(@NonNull String droneId) {
      Drone drone = mDrones.get(droneId);
      if (drone == null) {
         drone = new Drone(droneId);

         if (DEBUG) {
            Log.d(TAG, "Created " + drone);
         }

         mDrones.put(droneId, drone);
      }
      return drone;
   }

   /**
    * Returns the {@link DroneTask} that corresponds to the given drone id,
    * creating one if necessary.
    * @param droneId The drone id.
    * @return The drone task associated with the given id.
    */
   private DroneTask resolveDroneTask(@NonNull String droneId) {
      DroneTask task = mDroneTasks.get(droneId);
      if (task == null) {
         Drone drone = resolveDrone(droneId);
         task = new DroneTask(drone, this);

         if (DEBUG) {
            Log.d(TAG, "Created " + task);
         }

         mDroneTasks.put(droneId, task);
      }
      return task;
   }

   /**
    * Returns the {@link Room} that corresponds to the given id,
    * creating one if necessary.
    * @param roomId The room id.
    * @return The room with the given id.
    */
   private Room resolveRoom(@NonNull String roomId) {
      Room room = mRooms.get(roomId);
      if (room == null) {
         room = new Room(roomId);

         if (DEBUG) {
            Log.d(TAG, "Created " + room);
         }

         mRooms.put(roomId, room);
      }
      return room;
   }

   /**
    * Returns a {@link Writing} with the given parameters, saving the
    * writing if it is valid, that is, has an order != -1. If we were to
    * get another writing with the same order, the old one will be overritten.
    * @param text The writing text.
    * @param order The writing order.
    * @return A writing instance.
    */
   private Writing resolveWriting(@NonNull String text, int order) {
      Writing writing = new Writing(text, order);
      if (order != INVALID_WRITING) {
         mWritings.put(order, writing);

         if (DEBUG) {
            Log.d(TAG, "Found valid writing! " + writing);
         }
      }
      return writing;
   }

   /**
    * Handles the response from a "start" HTTP request.
    * @param response The HTTP response.
    */
   private void handleStartResponse(StartResponse response) {
      String roomId = response.getRoomId();
      Room room = resolveRoom(roomId);
      List<String> droneIds = response.getDroneIds();

      // Resolve all drones first
      for (String droneId : droneIds) {
         resolveDrone(droneId);
      }

      // Create an executor with enough threads for our drones
      Executor executor = Executors.newFixedThreadPool(droneIds.size());

      // Now that all drones are resolved, resolve and execute
      // a task for each one
      for (Drone drone : mDrones.values()) {
         DroneTask task = resolveDroneTask(drone.getId());
         task.add(room);
         task.executeOnExecutor(executor);
      }
   }

   /**
    * Handles the response from a "command" HTTP request.
    * @param response The HTTP response.
    */
   private void handleCommandsReponse(CommandsResponse response) {
      int resultCount = response.getResultCount();
      for (int i = 0; i < resultCount; i++) {
         Result result = response.resultAt(i);
         Command command = result.getCommand();
         Room room = resolveRoom(command.getRoomId());

         if (result instanceof WritingResult) {
            // We've encoutered a writing result
            WritingResult writingResult = (WritingResult) result;
            Writing writing = resolveWriting(
                writingResult.getText(),
                writingResult.getOrder());
            room.setWriting(writing);

         } else if (result instanceof ConnectionsResult) {
            // We've encountered a connections result
            ConnectionsResult connectionsResult =
                (ConnectionsResult) result;
            List<String> roomIds =
                connectionsResult.getConnections();
            List<Room> connections = new ArrayList<>();
            for (String roomId : roomIds) {
               connections.add(resolveRoom(roomId));
            }
            room.setConnections(connections);

            // Any drones that are waiting in this room need to have
            // their room paths updated with the new connections.
            for (DroneTask task : mDroneTasks.values()) {
               if (task.isRoomCurrent(room)) {
                  task.addAll(connections);
               }
            }
         } else if (result instanceof ErrorResult) {
            ErrorResult errorResult = (ErrorResult) result;
            if (DEBUG) {
               Log.e(TAG, "Error: " + errorResult.getError());
            }
         }
      }

      // Tell the task that spawned this request that we are no longer
      // awaiting commands.
      Drone drone = response.getDrone();
      for (DroneTask task : mDroneTasks.values()) {
         if (drone.equals(task.getDrone())) {
            task.setBusy(false);
            break;
         }
      }
   }

   /**
    * Handles the response from a "report" HTTP request.
    * @param response The HTTP response.
    */
   private void handleReportResponse(ReportResponse response) {
      if (DEBUG) {
         Log.d(TAG, "**************************************************");
         Log.d(TAG, response.getResponse());
         Log.d(TAG, "**************************************************");
      }

      Intent intent = new Intent(ACTION_FINISHED);
      intent.putExtra(EXTRA_RESPONSE, response.getResponse());
      LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
   }

   private void handleNotFoundResponse(NotFoundResponse response) {
      if (DEBUG) {
         Log.w(TAG, "404 Not Found encountered");
      }
   }

   private void handleErrorResponse(ErrorResponse response) {
      if (DEBUG) {
         Log.e(TAG, "400 or other error encountered");
      }
   }

   /**
    * Entry point into this Service from {@link MainActivity}.
    */
   public class GanymedeBinder extends Binder {
      public void start() {
         if (mStarted) {
            return;
         }
         mStarted = true;
         mDrones.clear();
         for (DroneTask task : mDroneTasks.values()) {
            task.cancel(true);
         }
         mDroneTasks.clear();
         mRooms.clear();
         mWritings.clear();
         mRequestManager.add(new StartRequest());
      }

      public void cancel() {
         mRequestManager.reset();
         mStarted = false;
      }
   }
}

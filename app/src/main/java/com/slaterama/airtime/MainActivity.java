package com.slaterama.airtime;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.slaterama.airtime.GanymedeService.GanymedeBinder;

/**
 * An {@link android.app.Activity} that really does nothing useful
 * other than shows a "Start" button for the whole proccess.
 */
public class MainActivity extends AppCompatActivity
      implements ServiceConnection {

   private TextView mStatusText;
   private Button mStartButton;
   private TextView mResultText;

   private Intent mGanymedeIntent;
   private GanymedeBinder mGanymedeBinder;

   private BroadcastReceiver mBroadcastReceiver =
       new BroadcastReceiver() {
          @Override
          public void onReceive(Context context, Intent intent) {

          }
       };

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main);
      mStatusText = (TextView) findViewById(R.id.status_txt);
      mStartButton = (Button) findViewById(R.id.start_btn);
      mResultText = (TextView) findViewById(R.id.result_txt);

      mStatusText.setText(R.string.main_connecting);
      mStartButton.setEnabled(false);

      mGanymedeIntent = new Intent(this, GanymedeService.class);
      startService(mGanymedeIntent);
   }

   @Override
   protected void onStart() {
      super.onStart();
      bindService(
          new Intent(this, GanymedeService.class),
          this,
          BIND_AUTO_CREATE);
   }

   @Override
   protected void onStop() {
      super.onStop();
      unbindService(this);
   }

   @Override
   public void onServiceConnected(
       ComponentName componentName,
       IBinder iBinder) {
      mGanymedeBinder = (GanymedeBinder) iBinder;
      mStatusText.setText(R.string.main_connected);
      mStartButton.setEnabled(true);
   }

   @Override
   public void onServiceDisconnected(ComponentName componentName) {
      mGanymedeBinder = null;
      mStatusText.setText(R.string.main_disconnected);
      mStartButton.setEnabled(false);
   }

   public void onStartClicked(View view) {
      mGanymedeBinder.start();
   }
}

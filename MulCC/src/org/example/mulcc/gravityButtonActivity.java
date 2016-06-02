package org.example.mulcc;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class gravityButtonActivity extends Activity{
	private TextView text;
	private ImageButton bHome;

	float x,y,z;
	//int fbstate;
	private SensorManager sensorManager;
	private Sensor sensor;
	private MySensorEventListener sensor_event_listener;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gravity);
		text = (TextView)findViewById(R.id.text1);
		bHome =(ImageButton) findViewById(R.id.buttonhome1);

		text.setText("Gravity Control Mode is on");
		bHome.setOnClickListener(homeclick); //click bHome, back to the mainActivity menu

		sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
		sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		sensor_event_listener = new MySensorEventListener();
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		sensorManager.registerListener(sensor_event_listener, sensor, SensorManager.SENSOR_DELAY_NORMAL);
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		sensorManager.unregisterListener(sensor_event_listener);
	}

	private final class MySensorEventListener implements  SensorEventListener{
		@SuppressWarnings("deprecation")
		@Override
		public void onSensorChanged(SensorEvent se) {
			//gravity sensor and send inst
			if(se.sensor.getType()==Sensor.TYPE_ACCELEROMETER){
				x = se.values[SensorManager.DATA_X];
				y = se.values[SensorManager.DATA_Y];
				z = se.values[SensorManager.DATA_Z];
				text.setText("x="+(int)x+"y="+(int)y+"z="+(int)z);
				sendInstruction(x,y);
			}
		}
		public void onAccuracyChanged(Sensor arg0, int arg1) {
		}
	}

	private void sendInstruction(float x,float y){
		int a=(int)Math.abs(x);
		int b=(int)Math.abs(y);

		if(a<3 & b<3){
			MainActivity.Stop();
			return;
		}
		if(x == 0){
			if(y > 3) MainActivity.Go();
			else MainActivity.Back();
		}
		else{
			if(x > 3) MainActivity.TurnLeft();
			else MainActivity.TurnRight();
		}
		return;
	}

	OnClickListener homeclick=new OnClickListener(){
		@Override
		public void onClick(View v)
		{
			Intent homeIntent=new Intent();
			homeIntent.setClass(gravityButtonActivity.this,MainActivity.class);
			startActivity(homeIntent);
		}
	};

}
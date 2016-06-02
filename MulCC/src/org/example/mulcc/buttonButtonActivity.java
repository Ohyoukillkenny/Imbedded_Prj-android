package org.example.mulcc;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
//import android.widget.SeekBar;
//import android.widget.SeekBar.OnSeekBarChangeListener;


public class buttonButtonActivity extends Activity{

	private ImageButton bUp = null;
	private ImageButton bDown = null;
	private ImageButton bLeft = null;
	private ImageButton bRight = null;
	private ImageButton bStop = null;
	private ImageButton bHome = null;
	//private int speed;
	private int fbstate; //means test wether the car is moving forward
	private void findItem()
	{
		bUp = (ImageButton) findViewById(R.id.buttonup);
		bDown = (ImageButton) findViewById(R.id.buttondown);
		bLeft = (ImageButton) findViewById(R.id.buttonleft);
		bRight = (ImageButton) findViewById(R.id.buttonright);
		bStop = (ImageButton) findViewById(R.id.buttonstop);
		bHome = (ImageButton) findViewById(R.id.buttonhome);
		bUp.getBackground().setAlpha(0);
		bDown.getBackground().setAlpha(0);
		bRight.getBackground().setAlpha(0);
		bLeft.getBackground().setAlpha(0);
		bHome.getBackground().setAlpha(0);
	}

	private void setListener()
	{
		bUp.setOnClickListener(new upButtonListener());
		bDown.setOnClickListener(new downButtonListener());
		bLeft.setOnTouchListener(new leftButtonListener());
		bRight.setOnTouchListener(new rightButtonListener());
		bStop.setOnClickListener(new stopButtonListener());
		bHome.setOnClickListener(new homeButtonListener());
	}

	// create the panal of buttonButtonActivity
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_button);
		findItem();
		setListener();
	}

	private class upButtonListener implements OnClickListener
	{
		@Override
		public void onClick(View v)
		{
			fbstate=1;
			MainActivity.Go();
		}
	}
	private class downButtonListener implements OnClickListener
	{
		@Override
		public void onClick(View v)
		{
			fbstate=0;
			MainActivity.Back();
		}
	}

	private class leftButtonListener implements OnTouchListener
	{
		@Override
		public boolean onTouch(View v, MotionEvent event)
		{
			if(event.getAction() == MotionEvent.ACTION_DOWN)
			{
				MainActivity.TurnLeft();
			}
			else if(event.getAction() == MotionEvent.ACTION_UP)
			{
				if(fbstate==1)
					MainActivity.Go();
				else  MainActivity.Back();
			}
			return true;
		}
	}
	private class rightButtonListener implements OnTouchListener
	{
		@Override
		public boolean onTouch(View v, MotionEvent event)
		{
			if(event.getAction() == MotionEvent.ACTION_DOWN)
			{
				MainActivity.TurnRight();
			}
			else if(event.getAction() == MotionEvent.ACTION_UP)
			{
				if(fbstate==1)
					MainActivity.Go();
				else  MainActivity.Back();
			}
			return true;
		}
	}

	private class stopButtonListener implements OnClickListener
	{
		@Override
		public void onClick(View v)
		{
			MainActivity.Stop();
		}
	}

	private class homeButtonListener implements OnClickListener
	{
		@Override
		public void onClick(View v)
		{
			Intent homeIntent = new Intent();
			homeIntent.setClass(buttonButtonActivity.this, MainActivity.class);
			startActivity(homeIntent);
		}
	}
}
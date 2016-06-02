package org.example.mulcc;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Color;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
//import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;

import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.View.OnClickListener;

public class MainActivity extends Activity {

	private TextView text;
	private LinearLayout  list;

	private BluetoothAdapter bta=null;
	private BluetoothSocket bts=null;
	private static UUID uuid=UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	private static final String TAG = "MainActivity";

	private ImageButton bButton = null;
	private ImageButton bGravity = null;
	private ImageButton refresh = null;
	private boolean mConnectedFlag;
	private static BTsocketThread mBTsocketThread;
	private InstructionThread mItrThread;
	private static Queue<String> mItrQueue;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		findItem();
		setListener();
	}
	private void findItem()
	{
		bButton = (ImageButton) findViewById(R.id.btnButton);
		bGravity = (ImageButton) findViewById(R.id.btnGravity);
		//Set the background
		bButton.getBackground().setAlpha(0);  //set the transparency
		bGravity.getBackground().setAlpha(0);
		text = (TextView)findViewById(R.id.text);
		list = (LinearLayout)findViewById(R.id.list);
		refresh = (ImageButton)findViewById(R.id.refresh);
		text.setText("Created by Lingkun Kong.The author protects all copy rights.");
	}
	private void setListener()
	{
		//set onClickListener
		bButton.setOnClickListener(new buttonButtonListener());
		bGravity.setOnClickListener(new gravityButtonListener());
		mConnectedFlag=false;
		refresh.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				refreshBluetoothDevice(); //find the nearby bluetooth devices
			}
		});
	}
	@Override
	protected void onResume()
	{
		super.onResume();
		if(mConnectedFlag)
		{
			Go();
			Stop();
		}
	}
	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		if (bts != null)
		{
			try {
				//close the bluetooth socket
				bts.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		if (mBTsocketThread != null)
		{
			//mBTsocketThread.cancel();
			mBTsocketThread = null;

		}
		if (mItrThread != null)
			mItrThread.cancel();
	}

//------------------------------ bluetooth
	//Search Pair BluetoothDevices, and put them on the screen
	public void refreshBluetoothDevice() {
		bta = BluetoothAdapter.getDefaultAdapter();
		if (bta != null) {

			if (!bta.isEnabled()) {
				text.setText("BluetoothDevice is disabled, turn on it");
				return;
			}

			list.removeAllViews(); //Call this method to remove all child views from the ViewGroup.
			text.setText("BluetoothDevice Searching ... ");

			Set<BluetoothDevice> pairDevices= bta.getBondedDevices();
			for (int i=0;i<pairDevices.size();i++) {
				Button temp = new Button(this); // set new button for each device
				BluetoothDevice device = (BluetoothDevice)pairDevices.toArray()[i];
				temp.setText(device.getName());
				temp.setTextColor(Color.BLACK);
				list.addView(temp);
				temp.setOnClickListener(new BluetoothDeviceSelector(device));
			}
		} else {
			text.setText("BluetoothDevice connecting Failed");
		}
	}

	//Connect to appointed BluetoothDevice when you select the button
	//implements there means inherit
	class BluetoothDeviceSelector implements OnClickListener {
		BluetoothDevice device;
		BluetoothDeviceSelector(BluetoothDevice device) {
			this.device = device;
		}
		@Override
		public void onClick(View v) {
			try {
				if (bts != null)
					bts.close();
				bts = device.createRfcommSocketToServiceRecord(uuid);
				bta.cancelDiscovery();
				bts.connect();
				mConnectedFlag=true;

				if ((mConnectedFlag == true) && (mBTsocketThread == null))
				{
					mBTsocketThread = new BTsocketThread(bts);
					mBTsocketThread.start();
					Log.d(TAG,"BTsocketThread is started, already connected");
					mItrThread = new InstructionThread();
					mItrThread.start();
					Log.d(TAG,"InstructionThread is started()");
				}

				text.setText(device.getName() + "is now available");
				return;
			}
			catch (IOException e) {
				mConnectedFlag=false;
				mBTsocketThread=null;
				mItrThread=null;
				e.printStackTrace();
			}
			text.setText("Connecting failed");
		}
	}

	//Send information via BluetoothSocket
	public void sendInformation(String message) {
		if (mBTsocketThread == null) {
			text.setText("Bluetooth is offline");
			return;
		}
		try {
			System.out.println("++Send to socket++");
			byte[] information=message.getBytes();
			// write data in output stream
			mBTsocketThread.write(information);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	//Set Inst queque for sending
	public class InstructionThread extends Thread
	{
		private boolean run_flag = true;
		public InstructionThread()
		{
			mItrQueue = new LinkedList<String>();
		}
		public void run()
		{
			while (run_flag)
			{
				if (mItrQueue.size() > 0)
				{
					String tmp = mItrQueue.poll();
					//set the Inst queque for bluetooth
					sendInformation(tmp);
					try {	Thread.sleep(30);	}
					catch (Exception e)
					{	Log.d(TAG,"Set Inst exit");	}
				}
			}
		}
		public void cancel(){
			run_flag = false;
		}
	}
	public class BTsocketThread extends Thread
	{
		private final OutputStream mOutputStream;
		private final InputStream mInputStream;
		private final BluetoothSocket mSocket;

		// initialize the BTsocketThread
		public BTsocketThread(BluetoothSocket BluetoothSocket)
		{
			mSocket = BluetoothSocket;
			OutputStream outputStream = null;
			InputStream inputStream = null;
			System.out.println("++Socket on++");
			//infQueue= new LinkedList<byte[]>();
			try
			{
				outputStream = mSocket.getOutputStream();
				inputStream = mSocket.getInputStream();
				System.out.println("++I/O stream on++");
			}
			catch(Exception e)
			{   e.printStackTrace();
				System.out.println("++I/O stream off++");
			}
			mOutputStream = outputStream;
			mInputStream = inputStream;
			//System.out.println("++I/O stream on++");

			if ((mOutputStream == null )||(mInputStream == null) ) Log.d(TAG,"stream error");
			else Log.d(TAG,"stream all OK");
		}

		public void write(byte [] information) {
			// infQueue.offer(information);
			if (mSocket == null){
				System.out.println("++Socket off++");
				return;
			}
			try {
				mOutputStream.write(information);//send information via OutputStream.write
				System.out.println("++BTsocket Out ++");
			}
			catch (IOException e){
				System.out.println("++BTsocket Out Fail ++");
				e.printStackTrace();
			}
		}
	}
//--------------------------------------
	// send info
	public static void Go(){
		mItrQueue.offer("AAA");
		return;
	}
	public static void Back(){
		mItrQueue.offer("BBB");
		return;
	}
	public static void TurnLeft(){
		mItrQueue.offer("LLL");
		return;
	}
	public static void TurnRight(){
		mItrQueue.offer("RRR");
		return;
	}
	public static void Stop(){
		mItrQueue.offer("PPP");
		return;
	}

	private class buttonButtonListener implements OnClickListener
	{
		@Override
		public void onClick(View v)
		{
			Intent buttonIntent = new Intent();
			//switch to the buttonButtonActivity panal
			buttonIntent.setClass(MainActivity.this, buttonButtonActivity.class);
			startActivity(buttonIntent);
		}
	}
	private class gravityButtonListener implements OnClickListener
	{
		@Override
		public void onClick(View v)
		{
			Intent gravityIntent = new Intent();
			//switch to the gravityButtonActivity panal
			gravityIntent.setClass(MainActivity.this, gravityButtonActivity.class);
			startActivity(gravityIntent);
		}
	}
// set menu when click refresh
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}



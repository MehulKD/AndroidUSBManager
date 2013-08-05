package com.ruyipu.usbmanager;

import java.util.HashMap;
import java.util.Iterator;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity {
	public static final String NAME = MainActivity.class.getName();
	private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
	private UsbManager usbManager;
	private UsbDevice usbDevice;
	private String deviceName;

	private Button btnRefresh;
	private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (ACTION_USB_PERMISSION.equals(action)) {
				synchronized (this) {
					usbDevice = intent
							.getParcelableExtra(UsbManager.EXTRA_DEVICE);

					if (intent.getBooleanExtra(
							UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
						if (usbDevice != null) {

						}
					} else {
						Log.e(NAME, "permission denied for device" + usbDevice);
					}

				}
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);

		PendingIntent mPermissionIntent = PendingIntent.getBroadcast(this, 0,
				new Intent(ACTION_USB_PERMISSION), 0);
		IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);

		registerReceiver(mUsbReceiver, filter);

		btnRefresh = (Button) findViewById(R.id.btnRefesh);
				
		btnRefresh.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
				Toast.makeText(MainActivity.this, deviceList.size(), Toast.LENGTH_LONG).show();
			}
		});
		refresh();

		if (null != usbDevice) {
			usbManager.requestPermission(usbDevice, mPermissionIntent);
			if (!usbManager.hasPermission(usbDevice)) {
				Toast.makeText(this,
						"No permission to connect to " + usbDevice,
						Toast.LENGTH_LONG).show();
				return;
			}
			writeData("hello world");
		}
	}
	
	private static int TIMEOUT = 10;
	private boolean forceClaim = true;
	private void writeData(String data){
		new CommunicateThread(data.getBytes()).run();
	}

	private class CommunicateThread extends Thread {
		private byte[] bytes;
		private UsbDeviceConnection connection;
		private UsbEndpoint endpoint;
		public CommunicateThread(byte[] bytes){
			this.bytes = bytes;
			UsbInterface intf = usbDevice.getInterface(0);
			endpoint = intf.getEndpoint(0);
			connection = usbManager.openDevice(usbDevice);
			connection.claimInterface(intf, forceClaim);
		}

		@Override
		public void run() {
			super.run();
			connection.bulkTransfer(endpoint, bytes, bytes.length, TIMEOUT);
		}
	}

	private void refresh() {
		HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
		listDevices(deviceList);
		if (deviceList.containsKey(deviceName)) {
			usbDevice = deviceList.get(deviceName);
		}
	}

	private void listDevices(HashMap<String, UsbDevice> deviceList) {
		if (deviceList.size() == 0) {
			Log.d(NAME, "NO DIVICE");
			Toast.makeText(this, "No device", Toast.LENGTH_LONG).show();
		} else {
			Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
			while (deviceIterator.hasNext()) {
				UsbDevice usbDevice = (UsbDevice) deviceIterator.next();
				Log.d(NAME, usbDevice.getDeviceName());
				Toast.makeText(this, usbDevice.getDeviceName(), Toast.LENGTH_SHORT).show();
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
package com.fatcatlab.tanchess;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class BluetoothControlActivity extends Activity {

	private final String TAG = "BluetoothControlActivity";
	private BluetoothAdapter mBtAdapter;

	// Return Intent extra
	public static String EXTRA_DEVICE_ADDRESS = "device_address";

	private ArrayAdapter<String> mPairedDevicesArrayAdapter;
	private ArrayAdapter<String> mNewDevicesArrayAdapter;
	
	private Handler handler = new Handler();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// TODO Auto-generated method stub
		Log.d(TAG, "----Oncreate-----");
		
		
		setContentView(R.layout.main);

		Button scanButton = (Button) findViewById(R.id.button_scan);
		scanButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				mNewDevicesArrayAdapter.clear();
				doDiscovery();
			}
		});

		// Initialize array adapters. One for already paired devices and
		// one for newly discovered devices
		mPairedDevicesArrayAdapter = new ArrayAdapter<String>(this,
				R.layout.device_name);
		mNewDevicesArrayAdapter = new ArrayAdapter<String>(this,
				R.layout.device_name);

		// Find and set up the ListView for paired devices
		ListView pairedListView = (ListView) findViewById(R.id.paired_devices);
		pairedListView.setAdapter(mPairedDevicesArrayAdapter);
		pairedListView.setOnItemClickListener(mDeviceClickListener);

		// Find and set up the ListView for newly discovered devices
		ListView newDevicesListView = (ListView) findViewById(R.id.new_devices);
		newDevicesListView.setAdapter(mNewDevicesArrayAdapter);
		newDevicesListView.setOnItemClickListener(mDeviceClickListener);

		// Register for broadcasts when a device is discovered
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		this.registerReceiver(mReceiver, filter);

		// Register for broadcasts when discovery has finished
		filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		this.registerReceiver(mReceiver, filter);
		
		//Register for bound when a device is not bounded
		filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED); 
		this.registerReceiver(mReceiver, filter);
		

		// Get the local Bluetooth adapter
		mBtAdapter = BluetoothAdapter.getDefaultAdapter();

		
		
		if(!mBtAdapter.isEnabled()){
            //��缓涓�釜intent瀵硅薄锛��瀵硅薄�ㄤ����涓�釜Activity锛��绀虹��峰�������澶�
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(intent);
	    }
		
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        startActivity(intent);		
        
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				boolean shouldCheck = true;
				while( shouldCheck ){
					if(BluetoothService.getService().getState() == BluetoothService.STATE_CONNECTED)
					{
						shouldCheck = false;
						Intent itent = new Intent();
						itent.setClass(BluetoothControlActivity.this, StartActivity.class);
						StartActivity.Instance.startActivity(itent);
						StartActivity.SCENE_STATE = StartActivity.STATE_BTGAMESCENE;
					}
				}
			}
		}, 1000);
		
		Runnable task = new Runnable() {  
	        public void run() {  
	            // TODO Auto-generated method stub  
	            if ( mBtAdapter.isEnabled() == true) {  
	            	mPairedDevicesArrayAdapter.clear();
	                updateBoundedList();
	            }  
                handler.postDelayed(this, 500);  
	        }  
	    };  
	    
	    handler.postDelayed(task, 0);
		
	}
	
	private void updateBoundedList(){
		Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();
		// If there are paired devices, add each one to the ArrayAdapter
		if (pairedDevices.size() > 0) {
			for (BluetoothDevice device : pairedDevices) {
				findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);
				mPairedDevicesArrayAdapter.add(device.getName() + "\n"
						+ device.getAddress());
				Toast.makeText(BluetoothControlActivity.this, "这是什么，我这里是乱码", Toast.LENGTH_SHORT).show();
			}
		} else {
			String noDevices = getResources().getText(R.string.none_paired)
					.toString();
			mPairedDevicesArrayAdapter.add(noDevices);
		}
	}
	
	
	/**
	 * Start device discover with the BluetoothAdapter
	 */
	private void doDiscovery() {
		Log.d(TAG, "doDiscovery()");

		// Indicate scanning in the title
		setProgressBarIndeterminateVisibility(true);
		setTitle(R.string.scanning);

		// Turn on sub-title for new devices
		findViewById(R.id.title_new_devices).setVisibility(View.VISIBLE);

		// If we're already discovering, stop it
		if (mBtAdapter.isDiscovering()) {
			mBtAdapter.cancelDiscovery();
		}

		// Request discover from BluetoothAdapter
		mBtAdapter.startDiscovery();
	}

	// The on-click listener for all devices in the ListViews
	private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
		public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
			// Cancel discovery because it's costly and we're about to connect
			mBtAdapter.cancelDiscovery();

			// Get the device MAC address, which is the last 17 chars in the
			// View
			String info = ((TextView) v).getText().toString();
			String address = info.substring(info.length() - 17);

			BluetoothDevice device = mBtAdapter.getRemoteDevice(address);
			// Attempt to connect to the device
			if(device.getBondState() == BluetoothDevice.BOND_BONDED)
				BluetoothService.getService().connect(device);
			else if (device.getBondState() == BluetoothDevice.BOND_NONE) {
                try {
					BluetoothControlActivity.createBond(device.getClass(), device);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }

		}
	};

	// The BroadcastReceiver that listens for discovered devices and
	// changes the title when discovery is finished
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			// When discovery finds a device
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				// Get the BluetoothDevice object from the Intent
				BluetoothDevice device = intent
						.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				// If it's already paired, skip it, because it's been listed
				// already
				if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
					mNewDevicesArrayAdapter.add(device.getName() + "\n"
							+ device.getAddress());
				}
				// When discovery is finished, change the Activity title
			} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED
					.equals(action)) {
				setProgressBarIndeterminateVisibility(false);
				setTitle(R.string.select_device);
				if (mNewDevicesArrayAdapter.getCount() == 0) {
					String noDevices = getResources().getText(
							R.string.none_found).toString();
					mNewDevicesArrayAdapter.add(noDevices);
				}
			}else if(BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)){  
				BluetoothDevice device = intent
						.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                switch (device.getBondState()) {  
                case BluetoothDevice.BOND_BONDING:  
	                Toast.makeText(BluetoothControlActivity.this, "Bonding...", Toast.LENGTH_SHORT).show();
                    break;  
                case BluetoothDevice.BOND_BONDED:  
	                Toast.makeText(BluetoothControlActivity.this, "Bonding Finished...", Toast.LENGTH_SHORT).show();
	                mNewDevicesArrayAdapter.remove(device.getName()+"\n"+device.getAddress());
                    break;  
                case BluetoothDevice.BOND_NONE:  
	                Toast.makeText(BluetoothControlActivity.this, "Bonding Canceled...", Toast.LENGTH_SHORT).show();
                default:  
                    break;  
                }  
            }   
		}
	};

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		this.unregisterReceiver(mReceiver);  
		super.onDestroy();
	}
	
	static public boolean createBond(Class btClass,BluetoothDevice btDevice) throws Exception {    
	    Method createBondMethod = btClass.getMethod("createBond");    
	    Boolean returnValue = (Boolean) createBondMethod.invoke(btDevice);    
	    return returnValue.booleanValue();    
	}

}

package com.cnlius.ibeacon;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.radiusnetworks.ibeacon.IBeacon;
import com.radiusnetworks.ibeacon.IBeaconConsumer;
import com.radiusnetworks.ibeacon.IBeaconManager;
import com.radiusnetworks.ibeacon.MonitorNotifier;
import com.radiusnetworks.ibeacon.RangeNotifier;
import com.radiusnetworks.ibeacon.Region;
import com.tbruyelle.rxpermissions.RxPermissions;

import rx.functions.Action1;

public class MonitoringActivity extends Activity implements IBeaconConsumer {
	protected static final String TAG = "MonitoringActivity";

	private ListView list = null;
	private BeaconAdapter adapter = null;
	private ArrayList<IBeacon> arrayL = new ArrayList<IBeacon>();
	private LayoutInflater inflater;

	private BeaconServiceUtility beaconUtill = null;
	private IBeaconManager iBeaconManager = IBeaconManager.getInstanceForApplication(this);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_monitor);

//		RxPermissions.getInstance(this)
//				.request(Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION)
//				.subscribe(new Action1<Boolean>() {
//					@Override
//					public void call(Boolean aBoolean) {
//						if (aBoolean) {
//
//						}
//					}
//				});

		beaconUtill = new BeaconServiceUtility(this);
		list = (ListView) findViewById(R.id.list);
		adapter = new BeaconAdapter();
		list.setAdapter(adapter);
		inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onStart() {
		super.onStart();
		beaconUtill.onStart(iBeaconManager, this);
	}

	@Override
	protected void onStop() {
		beaconUtill.onStop(iBeaconManager, this);
		super.onStop();
	}

	@Override
	public void onIBeaconServiceConnect() {

		iBeaconManager.setRangeNotifier(new RangeNotifier() {
			@Override
			public void didRangeBeaconsInRegion(Collection<IBeacon> iBeacons, Region region) {

				arrayL.clear();
				arrayL.addAll((ArrayList<IBeacon>) iBeacons);
				adapter.notifyDataSetChanged();
			}

		});

		iBeaconManager.setMonitorNotifier(new MonitorNotifier() {
			@Override
			public void didEnterRegion(Region region) {
				Log.e("BeaconDetactorService", "didEnterRegion");
				// logStatus("I just saw an iBeacon for the first time!");
			}

			@Override
			public void didExitRegion(Region region) {
				Log.e("BeaconDetactorService", "didExitRegion");
				// logStatus("I no longer see an iBeacon");
			}

			@Override
			public void didDetermineStateForRegion(int state, Region region) {
				Log.e("BeaconDetactorService", "didDetermineStateForRegion");
				// logStatus("I have just switched from seeing/not seeing iBeacons: " + state);
			}

		});

		try {
			iBeaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
		} catch (RemoteException e) {
			e.printStackTrace();
		}
//liy
//		try {
//			iBeaconManager.startMonitoringBeaconsInRegion(new Region("myMonitoringUniqueId", null, null, null));
//		} catch (RemoteException e) {
//			e.printStackTrace();
//		}
	}

	private class BeaconAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			if (arrayL != null && arrayL.size() > 0)
				return arrayL.size();
			else
				return 0;
		}

		@Override
		public IBeacon getItem(int arg0) {
			return arrayL.get(arg0);
		}

		@Override
		public long getItemId(int arg0) {
			return arg0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			try {
				ViewHolder holder;

				if (convertView != null) {
					holder = (ViewHolder) convertView.getTag();
				} else {
					holder = new ViewHolder(convertView = inflater.inflate(R.layout.tupple_monitoring, null));
				}
				if (arrayL.get(position).getProximityUuid() != null)
					holder.beacon_uuid.setText("UUID: " + arrayL.get(position).getProximityUuid());

				holder.beacon_major.setText("Major: " + arrayL.get(position).getMajor());

				holder.beacon_minor.setText(", Minor: " + arrayL.get(position).getMinor());

				holder.beacon_proximity.setText("Proximity: " + arrayL.get(position).getProximity());

				holder.beacon_rssi.setText(", Rssi: " + arrayL.get(position).getRssi());

				holder.beacon_txpower.setText(", TxPower: " + arrayL.get(position).getTxPower());

				holder.beacon_range.setText("Accuracy: " + arrayL.get(position).getAccuracy()); 
				
				holder.beacon_address.setText("Address: " + arrayL.get(position).getAddress());//liy
				
			} catch (Exception e) {
				e.printStackTrace();
			}

			return convertView;
		}

		private class ViewHolder {
			private TextView beacon_uuid;
			private TextView beacon_major;
			private TextView beacon_minor;
			private TextView beacon_proximity;
			private TextView beacon_rssi;
			private TextView beacon_txpower;
			private TextView beacon_range;
			private TextView beacon_address;
			
			public ViewHolder(View view) {
				beacon_uuid = (TextView) view.findViewById(R.id.BEACON_uuid);
				beacon_major = (TextView) view.findViewById(R.id.BEACON_major);
				beacon_minor = (TextView) view.findViewById(R.id.BEACON_minor);
				beacon_proximity = (TextView) view.findViewById(R.id.BEACON_proximity);
				beacon_rssi = (TextView) view.findViewById(R.id.BEACON_rssi);
				beacon_txpower = (TextView) view.findViewById(R.id.BEACON_txpower);
				beacon_range = (TextView) view.findViewById(R.id.BEACON_range);
				beacon_address = (TextView) view.findViewById(R.id.BEACON_address);
				view.setTag(this);
			}
		}

	}


	private void requestGet(){
		try {
			URL url = new URL("http://freegeoip.net/json/");
			HttpURLConnection httpUrlConnection = (HttpURLConnection) url.openConnection();
			httpUrlConnection.setRequestMethod("GET");
			httpUrlConnection.setReadTimeout(5000);
			httpUrlConnection.setConnectTimeout(5000);

			if (httpUrlConnection.getResponseCode() == 200) {
				InputStream is = httpUrlConnection.getInputStream();
				ByteArrayOutputStream os = new ByteArrayOutputStream();
				int len = 0;
				byte buffer[] = new byte[1024];
				while ((len = is.read(buffer)) != -1) {
					os.write(buffer, 0, len);
				}
				is.close();
				os.close();
//				os.toString("utf-8");
				String result = new String(os.toByteArray());
				Log.e("RESULT","链接成功");
			} else {
				Log.e("RESULT","链接失败");
			}
		}catch (Exception e){
			e.printStackTrace();
		}
	}

	/**
	 * POST请求获取数据
	 */
	public static String requestPost(String path,String post){
		URL url = null;
		try {
			url = new URL(path);
			HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
			httpURLConnection.setRequestMethod("POST");// 提交模式
			// conn.setConnectTimeout(10000);//连接超时 单位毫秒
			// conn.setReadTimeout(2000);//读取超时 单位毫秒
			// 发送POST请求必须设置如下两行
			httpURLConnection.setDoOutput(true);
			httpURLConnection.setDoInput(true);
			// 获取URLConnection对象对应的输出流
			PrintWriter printWriter = new PrintWriter(httpURLConnection.getOutputStream());
			// 发送请求参数
			printWriter.write(post);//post的参数 xx=xx&yy=yy
			// flush输出流的缓冲
			printWriter.flush();
			//开始获取数据
			BufferedInputStream bis = new BufferedInputStream(httpURLConnection.getInputStream());
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			int len;
			byte[] arr = new byte[1024];
			while((len=bis.read(arr))!= -1){
				bos.write(arr,0,len);
				bos.flush();
			}
			bos.close();
			return bos.toString("utf-8");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
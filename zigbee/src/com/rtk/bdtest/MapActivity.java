package com.rtk.bdtest;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.GroundOverlayOptions;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.BaiduMap.OnMarkerClickListener;
import com.baidu.mapapi.map.BaiduMap.OnMarkerDragListener;
import com.baidu.mapapi.map.InfoWindow.OnInfoWindowClickListener;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;

import com.baidu.mapapi.map.offline.MKOLSearchRecord;
import com.baidu.mapapi.map.offline.MKOLUpdateElement;
import com.baidu.mapapi.map.offline.MKOfflineMap;
import com.baidu.mapapi.map.offline.MKOfflineMapListener;
import com.baidu.mapapi.utils.CoordinateConverter;
import com.baidu.mapapi.utils.CoordinateConverter.CoordType;
import com.rtk.bdtest.db.SmsHelper;
import com.rtk.bdtest.util.Device;
import com.rtk.bdtest.util.GpsCorrect;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MapActivity extends Fragment implements MKOfflineMapListener {

	private MapView mMapView;
	private static BaiduMap mBaiduMap;
	private static Marker mMarkerA;
	private static Marker mMarkerB;
	private static Marker mMarkerC;
	private static Marker mMarkerD;
	private static Marker mMarkerSelf;
	private InfoWindow mInfoWindow;
	private MKOfflineMap mOffline = null;
	private final static String Tag = "MapActivity";
	private static final int MSG_UPDATE_SELF_GPS = 0;
	private static final int MSG_UPDATE_OTHER_GPS = 1;
	private static final int MSG_UPDATE_SMS = 2;
	private static int count = 0;
	private static LatLng jingwei2;
	private static String smsdata;
	private static String addrtmp;
	private static String Idtmp;
	private static String typetmp;
	// 默认南京经纬度
	private static double[] jingwei = {  0,0 };
	public static ArrayList<Device> gpsdevices;

	static BitmapDescriptor bdA = BitmapDescriptorFactory
			.fromResource(R.drawable.icon_marka);
	static BitmapDescriptor bdB = BitmapDescriptorFactory
			.fromResource(R.drawable.icon_markb);
	private static BitmapDescriptor bdC = BitmapDescriptorFactory
			.fromResource(R.drawable.icon_markc);
	static BitmapDescriptor bdD = BitmapDescriptorFactory
			.fromResource(R.drawable.icon_markd);
	static BitmapDescriptor bd = BitmapDescriptorFactory
			.fromResource(R.drawable.icon_gcoding);
	BitmapDescriptor bdGround = BitmapDescriptorFactory
			.fromResource(R.drawable.ground_overlay);

	Runnable gpsselfRunnable = new Runnable() {
		public void run() {
			BitmapDescriptor bdC = BitmapDescriptorFactory
					.fromResource(R.drawable.icon_marka);
			// 将GPS设备采集的原始GPS坐标转换成百度坐标
			CoordinateConverter converter = new CoordinateConverter();
			converter.from(CoordType.GPS);
			LatLng jingwei= new LatLng(39.33, 116.400244);
			double[] jingweitest2 = {118.48446487,32.031519864};
			jingweitest2[1] = (jingweitest2[1]%1)*100/60 + (int)jingweitest2[1]/1;
			jingweitest2[0] =  (jingweitest2[0]%1)*100/60 + (int)jingweitest2[0]/1;
			LatLng sourceLatLng = new LatLng(jingweitest2[1], jingweitest2[0]);
			// sourceLatLng待转换坐标
			converter.coord(sourceLatLng);
			LatLng desLatLng = converter.convert();
			OverlayOptions selfgps = new MarkerOptions().position(desLatLng)
					.icon(bdC).perspective(false).anchor(0.5f, 0.5f).rotate(30)
					.zIndex(7);
			mMarkerSelf = (Marker) (mBaiduMap.addOverlay(selfgps));
			MapStatusUpdate status = MapStatusUpdateFactory.newLatLng(jingwei);

		}
	};
	
	public Runnable markeFlashRunnable = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			Message smsMessage = new Message();
			mMarkerSelf.setVisible(true);
			smsMessage.what = MSG_UPDATE_SMS;
			//gpsMessage.obj = jingwei;
			gpsHandler.sendMessage(smsMessage);
		}
		
	};

	Handler gpsHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
			case MSG_UPDATE_SMS:
				//闪烁图标
				mMarkerSelf.setVisible(false);
				gpsHandler.postDelayed(markeFlashRunnable, 2000);
				break;
			case MSG_UPDATE_SELF_GPS:
				BitmapDescriptor bdC = BitmapDescriptorFactory
						.fromResource(R.drawable.icon_marka);
				//经纬度换算
				// 将GPS设备采集的原始GPS坐标转换成百度坐标
				CoordinateConverter converter = new CoordinateConverter();
				converter.from(CoordType.GPS);
				//jingweitest2[1] = (jingweitest2[1]%1)*100/60 + (int)jingweitest2[1]/1;
				//jingweitest2[0] =  (jingweitest2[0]%1)*100/60 + (int)jingweitest2[0]/1;
				jingwei[1] = jingwei[1]%1 *100/60 +(int) jingwei[1]/1;
				jingwei[0] = jingwei[0]%1 *100/60 + (int)jingwei[0]/1;
				LatLng sourceLatLng = new LatLng(jingwei[1], jingwei[0]);
				Log.i(Tag,"The convert gps lat is " + jingwei[1] +" the lng is " + jingwei[0]);
				// sourceLatLng待转换坐标
				converter.coord(sourceLatLng);
				LatLng desLatLng = converter.convert();
				Log.i(Tag,"The destination convert gps lat is " + desLatLng.latitude +" the lng is " + desLatLng.longitude);
				//LatLng jingwei2 = new LatLng(jingwei[1], jingwei[0]);
				OverlayOptions selfgps = new MarkerOptions().position(desLatLng)
						.icon(bdC).perspective(false).anchor(0.5f, 0.5f)
						.rotate(30).zIndex(7);
				mMarkerSelf = (Marker) (mBaiduMap.addOverlay(selfgps));
				MapStatusUpdate status = MapStatusUpdateFactory
						.newLatLng(jingwei2);
				break;
			}

			super.handleMessage(msg);
		}

	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.activity_overlay, container, false);
	}
	
	private BroadcastReceiver receiverSms = new BroadcastReceiver() {

		@Override
		public void onReceive(Context arg0, Intent smsIntent) {
			// TODO Auto-generated method stub
			if (smsIntent.getAction().equals("ACTION_ZIGBEE_SMS")) {
/*				for (int i = 0; i<gpsdevices.size(); i++) {
					if (gpsdevices.get(i).deviceName!=null) {
						if(gpsdevices.get(i).deviceName.contains("本机")) {
							gpsdevices.get(i).gpsMarker = mMarkerSelf;
						}
					}
				}*/
				if (mMarkerSelf!=null) {
					smsdata = smsIntent.getExtras().getString("zigbee_sms");
				    addrtmp  = smsIntent.getExtras().getString("smsSourAddr");
					Idtmp = smsIntent.getExtras().getString("smsSourId");
					typetmp = smsIntent.getExtras().getString("smsType");
					Message smsMessage = new Message();
					smsMessage.what = MSG_UPDATE_SMS;
					//gpsMessage.obj = jingwei;
					gpsHandler.sendMessage(smsMessage);
				}
				
			}
		}
	
	};

	private BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context arg0, Intent gpsIntent) {
			// 收到gps intent以后，发给gps刷新handler，ui显示
			Log.i(Tag, "the action is !!!!" + gpsIntent.getAction());
			if (gpsIntent.getAction().equals("ACTION_UPDATE_SELF_GPS")) {
				String id = ""; // 得到gps所属的id
				Double longitude = gpsIntent.getExtras().getDouble("longitude");
				Double latitude = gpsIntent.getExtras().getDouble("latitude");
				//经纬度换算
				longitude = longitude%1*100/60+(int)(longitude/1);
				latitude = latitude%1*100/60+ (int)(latitude/1);
				Log.i(Tag, "update self gps info the longitude is " + longitude
						+ "the latitude is " + latitude);

				FragmentManager rightfm = getActivity()
						.getSupportFragmentManager();
				Fragment lfm = rightfm.findFragmentById(R.id.list_container);
				if(((FragmentList2) lfm).devicesB.size()>0) {
					gpsdevices = ((FragmentList2) lfm).devicesB;				
				} 
				//GpsCorrect.transform(latitude, longitude, jingwei);
				//Log.i(Tag, "update self gps  jiupian  info the longitude is " + jingwei[0]
				//		+ "the latitude is " + jingwei[1]);
				//jingwei[0] = longitude;
				//jingwei[1] = latitude;
				BitmapDescriptor bdC = BitmapDescriptorFactory
						.fromResource(R.drawable.icon_marka);
				jingwei2 = new LatLng(latitude, longitude);
				Log.i(Tag,"The convert gps lat is " + jingwei2.latitude +" the lng is " + jingwei2.longitude);
				// 将GPS设备采集的原始GPS坐标转换成百度坐标
				CoordinateConverter converter = new CoordinateConverter();
				converter.from(CoordType.GPS);
				// sourceLatLng待转换坐标
				converter.coord(jingwei2);

				LatLng desLatLng = converter.convert();
				Log.i(Tag,"The destination convert gps lat is " + desLatLng.latitude +" the lng is " + desLatLng.longitude);
				if(mMarkerSelf==null) {
				OverlayOptions selfgps = new MarkerOptions().position(desLatLng)
						.icon(bdC).perspective(false).anchor(0.5f, 0.5f)
						.rotate(30).zIndex(7);
				mMarkerSelf = (Marker) (mBaiduMap.addOverlay(selfgps));
				MapStatus mMapStatus = new MapStatus.Builder().target(desLatLng).zoom(12).build();
				MapStatusUpdate status = MapStatusUpdateFactory.newMapStatus(mMapStatus);
				mBaiduMap.setMapStatus(status);
				for (int i = 0; i<gpsdevices.size(); i++) {
					if (gpsdevices.get(i).deviceName!=null) {
						if(i==0) {
							gpsdevices.get(i).gpsMarker = mMarkerSelf;
						}
					}
				}
				//		.newLatLng(jingwei2);
				} else {
					//mMarkerSelf.setPosition(jingwei2);
					Log.i(Tag, " new position!");
					LatLng temp = mMarkerSelf.getPosition();
					LatLng llNew = new LatLng(latitude,longitude);
					CoordinateConverter converter2 = new CoordinateConverter();
					converter2.from(CoordType.GPS);
					// sourceLatLng待转换坐标
					converter2.coord(llNew);

					LatLng desLatLng2 = converter2.convert();
					mMarkerSelf.setPosition(desLatLng2);
					for (int i = 0; i<gpsdevices.size(); i++) {
						if (gpsdevices.get(i).deviceName!=null) {
							if(gpsdevices.get(i).deviceName.contains("本机")) {
								gpsdevices.get(i).gpsMarker = mMarkerSelf;
							}
						}
					}
				}
				//Message gpsMessage = new Message();
				//gpsMessage.what = MSG_UPDATE_SELF_GPS;
				//gpsMessage.obj = jingwei;
				//gpsHandler.sendMessage(gpsMessage);

			} else if (gpsIntent.getAction().equals("ACTION_UPDATE_GPS_INFO")) {
				String jingweidu = gpsIntent.getExtras().getString("gps");
				String deviceAddress = jingweidu.substring(0, 4);
				String gps = jingweidu.substring(4,jingweidu.length());
				Log.i(Tag, " gps address is " +deviceAddress + "gps info is " +gps);
				FragmentManager rightfm = getActivity()
						.getSupportFragmentManager();
				Fragment lfm = rightfm.findFragmentById(R.id.list_container);
				if (lfm instanceof FragmentList2) {
					// 得到B和C设备列表
					if(((FragmentList2) lfm).devicesB.size()>0) {
					gpsdevices = ((FragmentList2) lfm).devicesB;
					for (int i = 0; i < gpsdevices.size(); i++) {
						if(gpsdevices.get(i).deviceAddress!=null ) {
						if (gpsdevices.get(i).deviceAddress
								.equals(deviceAddress)) {
							gpsdevices.get(i).deviceAddress = deviceAddress;
							String longitude = gps.split(",")[2];
							String latitude = gps.split(",")[0];
							gpsdevices.get(i).jingdu = Float
									.parseFloat(longitude) * 0.01f;
							gpsdevices.get(i).weidu = Float
									.parseFloat(latitude) * 0.01f;
							LatLng jw = new LatLng(gpsdevices.get(i).jingdu,
									gpsdevices.get(i).weidu);
							if (gpsdevices.get(i).gpsMarker != null) {
								gpsdevices.get(i).gpsMarker.setPosition(jw);
							} else {
								OverlayOptions ooA = new MarkerOptions()
										.position(jw).icon(bdA).zIndex(9)
										.draggable(true);
								Marker markertmp;
								markertmp = (Marker) (mBaiduMap.addOverlay(ooA));
								gpsdevices.get(i).gpsMarker = mMarkerA;
							}
						}
						} else {
							// 设备没在设备列表里
						}
					}
					}
				}
				// 刷新所有marker点的gps经纬度信息

			}
		}

	};

	public void importFromSDCard(View view) {
		int num = mOffline.importOfflineData();
		String msg = "";
		if (num == 0) {
			msg = "没有导入离线包，这可能是离线包放置位置不正确，或离线包已经导入过";
		} else {
			msg = String.format("成功导入 %d 个离线包，可以在下载管理查看", num);
		}
		Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mMapView = (MapView) getActivity().findViewById(R.id.bmapView);

		mOffline = new MKOfflineMap();
		mOffline.init(this);
		importFromSDCard(mMapView);

		mBaiduMap = mMapView.getMap();
		//MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(14.0f);
		//mBaiduMap.setMapStatus(msu);

		initOverlay();

		mBaiduMap.setOnMarkerClickListener(new OnMarkerClickListener() {
			public boolean onMarkerClick(final Marker marker) {
				Button button = new Button(getActivity());
				button.setBackgroundResource(R.drawable.popup);
				OnInfoWindowClickListener listener = null;
				FragmentManager rightfm = getActivity()
						.getSupportFragmentManager();
				Fragment lfm = rightfm.findFragmentById(R.id.list_container);
				if(((FragmentList2) lfm).devicesB.size()>0) {
					gpsdevices = ((FragmentList2) lfm).devicesB;				
				} 
				for (int i = 0; i <gpsdevices.size(); i ++) {
					if((gpsdevices.get(i).gpsMarker!=null)&& (marker==gpsdevices.get(i).gpsMarker)) {
						if (i == 0) {
							if (gpsdevices.get(i).unread) {
								button.setText("来自" + Idtmp + "短信内容: "
										+ smsdata);
								gpsHandler.removeCallbacks(markeFlashRunnable);
								gpsdevices.get(i).unread = false;
							} else {
								button.setText("无未读短信");
							}
						} else {
							    final String name2 = gpsdevices.get(i).deviceName;
							    final String addrtmp  = gpsdevices.get(i).deviceAddress;
							    final String idtmp = gpsdevices.get(i).deviceID;
                                button.setText("发送短信");
                                button.setOnClickListener(new OnClickListener(){
									@Override
									public void onClick(View arg0) {
										final EditText mInput2 = new EditText(getActivity());
										mInput2.setMaxLines(4);
										AlertDialog dialog2 = new AlertDialog.Builder(getActivity())
										.setTitle("给" + name2 + "发送短信息:")
										.setView(mInput2)
										.setPositiveButton("发送",
												new DialogInterface.OnClickListener() {
											private SmsHelper smsHelper;

													@Override
													public void onClick(
															DialogInterface dialog,
															int which) {
														String sms = mInput2.getText()
																.toString();
														String destAddr =addrtmp ;
														String destId = idtmp ;
														MainActivity.instance.sendSMS(sms,destAddr,destId);
														Date tmpDate = new Date();
														SimpleDateFormat formatt = new SimpleDateFormat("yyyy年MM月dd日HH时mm分ss秒");
														String xx = formatt.format(tmpDate);
														smsHelper = new SmsHelper(getActivity());
														smsHelper.insert(name2, xx, sms, "true");
													}
												}).setNegativeButton(R.string.cancel, null)
										.create();
									}                 	
                                });
						}
						listener = new OnInfoWindowClickListener() {
							public void onInfoWindowClick() {
								mBaiduMap.hideInfoWindow();
							}
						};
						LatLng ll = marker.getPosition();
						mInfoWindow = new InfoWindow(BitmapDescriptorFactory
								.fromView(button), ll, -47, listener);
						mBaiduMap.showInfoWindow(mInfoWindow);
					}
				}
				return true;
			}
		});
	}

	public void initOverlay() {
		CoordinateConverter converter = new CoordinateConverter();
		converter.from(CoordType.GPS);
		LatLng sourceLatLng = new LatLng(32.05253311, 118.80744145);
		// sourceLatLng待转换坐标
		converter.coord(sourceLatLng);
		LatLng desLatLng = converter.convert();
		OverlayOptions ooC = new MarkerOptions().position(desLatLng).icon(bdC)
				.perspective(false).anchor(0.5f, 0.5f).rotate(30).zIndex(7);
		mMarkerC = (Marker) (mBaiduMap.addOverlay(ooC));
		// add marker overlay
		//LatLng testA = new LatLng(32.01807045, 118.48776303);
		//OverlayOptions ooA = new MarkerOptions().position(testA).icon(bdA)
		//		.zIndex(9).draggable(true);
		//mMarkerA = (Marker) (mBaiduMap.addOverlay(ooA));
		/*LatLng llA = new LatLng(39.963175, 116.400244);
		LatLng llB = new LatLng(39.942821, 116.369199);
		LatLng llC = new LatLng(39.939723, 116.425541);
		LatLng llD = new LatLng(39.906965, 116.401394);
		

		OverlayOptions ooA = new MarkerOptions().position(llA).icon(bdA)
				.zIndex(9).draggable(true);
		mMarkerA = (Marker) (mBaiduMap.addOverlay(ooA));
		OverlayOptions ooB = new MarkerOptions().position(llB).icon(bdB)
				.zIndex(5);
		mMarkerB = (Marker) (mBaiduMap.addOverlay(ooB));
		OverlayOptions ooC = new MarkerOptions().position(llC).icon(bdC)
				.perspective(false).anchor(0.5f, 0.5f).rotate(30).zIndex(7);
		mMarkerC = (Marker) (mBaiduMap.addOverlay(ooC));
		OverlayOptions ooD = new MarkerOptions().position(llD).icon(bdD)
				.perspective(false).zIndex(7);
		mMarkerD = (Marker) (mBaiduMap.addOverlay(ooD));*/

	}

	/**
	 * �������Overlay
	 * 
	 * @param view
	 */
	public void clearOverlay(View view) {
		mBaiduMap.clear();
	}

	/**
	 * �������Overlay
	 * 
	 * @param view
	 */
	public void resetOverlay(View view) {
		clearOverlay(null);
		initOverlay();
	}

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		mMapView.onPause();
		//gpsHandler.removeCallbacks(gpsselfRunnable);
		mMarkerSelf = null;
		getActivity().unregisterReceiver(receiver);
		getActivity().unregisterReceiver(receiverSms);
		super.onPause();
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		mMapView.onResume();
		super.onResume();
		IntentFilter filter = new IntentFilter(
				"com.rtk.bdtest.service.BDService.broadcast");
		filter.addAction("ACTION_UPDATE_SELF_GPS");
		filter.addAction("ACTION_UPDATE_GPS_INFO");
		IntentFilter filterSms = new IntentFilter(
				"com.rtk.bdtest.service.ZigbeeService.broadcastMap");
		filterSms.addAction("ACTION_ZIGBEE_SMS");
		getActivity().registerReceiver(receiver, filter);
		getActivity().registerReceiver(receiverSms, filterSms);
		//gpsHandler.postDelayed(gpsselfRunnable, 1000);
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		mMapView.onDestroy();
		super.onDestroy();
		/*
		 * bdA.recycle(); bdB.recycle(); bdC.recycle(); bdD.recycle();
		 * bd.recycle(); bdGround.recycle();
		 */
	}

	@Override
	public void onDestroyView() {
		// TODO Auto-generated method stub
		super.onDestroyView();
	}

	@Override
	public void onGetOfflineMapState(int arg0, int arg1) {
		// TODO Auto-generated method stub
		Log.i(Tag, "the arg0 is " + arg0 + "the arg1 is " + arg1);

	}
}

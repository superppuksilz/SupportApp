package com.example.ex07_09;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;

import android.app.*;
import android.content.*;
import android.database.*;
import android.database.sqlite.*;
import android.hardware.*;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.*;
import android.provider.*;
import android.telephony.*;
import android.util.*;
import android.view.*;
import android.widget.*;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

public class MainActivity extends Activity implements SensorEventListener {

	private Weather w = new Weather();
	String weatherString = "";

	Socket socket;
	InputStream socket_in;
	OutputStream socket_out;
	String data = null;
	private boolean iscon = false;

	// For getting gps info
	private GpsInfo gps;
	String latiude;
	String longitude;

	// For handling contact ↓↓↓↓↓↓↓↓↓↓↓↓
	Button listButton;
	EditText editName;
	EditText editAddress;

	// For SQLITE ↓↓↓↓↓↓↓↓↓↓↓↓
	ListView listView;
	MySQLiteHandler handler;
	Cursor c;
	SimpleCursorAdapter adapter;

	// For Send SMS ↓↓↓↓↓↓↓↓↓↓↓↓
	Context mContext;
	EditText smsNumber, smsTextContext;

	// For handling senosr ↓↓↓↓↓↓↓↓↓↓↓↓
	SensorData sensor = new SensorData();
	private SensorManager SensorMan;
	private Sensor Gyro;
	private Sensor Acc;

	Button connect;
	/**
	 * ATTENTION: This was auto-generated to implement the App Indexing API.
	 * See https://g.co/AppIndexing/AndroidStudio for more information.
	 */
	private GoogleApiClient client;



	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);



		// For handling senosr ↓↓↓↓↓↓↓↓↓↓↓↓
		SensorMan = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		Gyro = SensorMan.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
		Acc = SensorMan.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

		checkEmergency();

		// For Send SMS ↓↓↓↓↓↓↓↓↓↓↓↓
		mContext = this;

		// For SQLITE ↓↓↓↓↓↓↓↓↓↓↓↓
		listView = (ListView) findViewById(R.id.listView1);
		handler = MySQLiteHandler.open(getApplicationContext());
		c = handler.selectAll();

		adapter = new SimpleCursorAdapter(getApplicationContext(), R.layout.list_row, c,
				new String[]{"_id", "name", "address"}, new int[]{R.id.txtId, R.id.txtName, R.id.txtAddress},
				CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);

		listView.setAdapter(adapter);

		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

				TextView tv = (TextView) v.findViewById(R.id.txtId);

				Intent intent = new Intent(getApplicationContext(), DetailActivity.class);
				intent.putExtra("_id", tv.getText().toString());
				startActivity(intent);

			}
		});
		connect = (Button) findViewById(R.id.btnConnect);
		connect.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				new Thread() {
					public void run() {
						try {
							socket = new Socket("192.168.43.61", 9001);
							if(socket.isConnected()){
								iscon = true;
								socket_out = socket.getOutputStream();
								socket_in = socket.getInputStream();

								w.setURL();

								Bundle bun = new Bundle();
								bun.putString("data", w.getLine());
								Message msg = han.obtainMessage();
								msg.setData(bun);
								han.sendMessage(msg);
							}



							while (iscon) {
								int bytesToRead = socket_in.available();
								if (bytesToRead != 0) {
									byte[] bytes = new byte[10];
									socket_in.read(bytes, 0, bytesToRead);
									data = new String(bytes, 0, bytesToRead);
									if (data.equals("emer")) {

										sendSMS();
									}
								}

							}

						} catch (UnknownHostException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					}
				}.start();



			}
		});
		// ATTENTION: This was auto-generated to implement the App Indexing API.
		// See https://g.co/AppIndexing/AndroidStudio for more information.
		client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();

		//onGps();



	}


	Handler han = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			Bundle bun = msg.getData();
			String str = bun.getString("data");
			weatherString = 'w' + str;

			try {
				socket_out.write(weatherString.getBytes());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	};



	// Send Message Button is here ↓↓↓↓↓↓↓↓↓↓↓↓↓
	public void sendSMS() {
		Cursor temp;
		temp = handler.selectAll();

		while (temp.moveToNext()) {

			int _id = temp.getInt(temp.getColumnIndex("_id"));
			String name = temp.getString(temp.getColumnIndex("name"));
			String address = temp.getString(temp.getColumnIndex("address"));

			String smsText = "응급메시지가 수신되었습니다." + "http://maps.google.com/?q=" + latiude + "," + longitude;
			System.out.println(smsText + " " + latiude);

			if (address.length() > 0 && smsText.length() > 0) {
				sendSMS(address, smsText);
			} else {
				Toast.makeText(this, "모두 입력해 주세요", Toast.LENGTH_SHORT).show();
			}
		}
	}

	public void sendSMS(String smsNumber, String smsText) {
		PendingIntent sentIntent = PendingIntent.getBroadcast(this, 0, new Intent("SMS_SENT_ACTION"), 0);
		PendingIntent deliveredIntent = PendingIntent.getBroadcast(this, 0, new Intent("SMS_DELIVERED_ACTION"), 0);

		/**
		 * SMS가 발송될때 실행 When the SMS massage has been sent
		 */
		registerReceiver(new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				switch (getResultCode()) {
					case Activity.RESULT_OK:
						// 전송 성공
						Toast.makeText(mContext, "전송 완료", Toast.LENGTH_SHORT).show();
						break;
					case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
						// 전송 실패
						Toast.makeText(mContext, "전송 실패", Toast.LENGTH_SHORT).show();
						break;
					case SmsManager.RESULT_ERROR_NO_SERVICE:
						// 서비스 지역 아님
						Toast.makeText(mContext, "서비스 지역이 아닙니다", Toast.LENGTH_SHORT).show();
						break;
					case SmsManager.RESULT_ERROR_RADIO_OFF:
						// 무선 꺼짐
						Toast.makeText(mContext, "무선(Radio)가 꺼져있습니다", Toast.LENGTH_SHORT).show();
						break;
					case SmsManager.RESULT_ERROR_NULL_PDU:
						// PDU 실패
						Toast.makeText(mContext, "PDU Null", Toast.LENGTH_SHORT).show();
						break;
				}
			}
		}, new IntentFilter("SMS_SENT_ACTION"));

		/**
		 * SMS가 도착했을때 실행 When the SMS massage has been delivered
		 */
		registerReceiver(new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				switch (getResultCode()) {
					case Activity.RESULT_OK:
						// 도착 완료
						Toast.makeText(mContext, "SMS 도착 완료", Toast.LENGTH_SHORT).show();
						break;
					case Activity.RESULT_CANCELED:
						// 도착 안됨
						Toast.makeText(mContext, "SMS 도착 실패", Toast.LENGTH_SHORT).show();
						break;
				}
			}
		}, new IntentFilter("SMS_DELIVERED_ACTION"));

		SmsManager mSmsManager = SmsManager.getDefault();
		mSmsManager.sendTextMessage(smsNumber, null, smsText, sentIntent, deliveredIntent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.mymenu, menu);
		return true;
	}

	// Seosor is here ↓↓↓↓↓↓↓↓↓↓↓↓↓
	public void onAccuracyChanged(Sensor s, int accuracy) {

	}

	public void onSensorChanged(SensorEvent se) {
		Sensor s = se.sensor;
		if (s.getType() == Sensor.TYPE_GYROSCOPE) {
			sensor.setGyroX(Math.round(se.values[0] * 1000));
			sensor.setGyroY(Math.round(se.values[1] * 1000));
			sensor.setGyroZ(Math.round(se.values[2] * 1000));

			// System.out.println("Gyro :" + gyroX + " " + gyroY + " " + gyroZ);
		}
		if (s.getType() == Sensor.TYPE_ACCELEROMETER) {
			sensor.setAccX((int) se.values[0]);
			sensor.setAccX((int) se.values[1]);
			sensor.setAccX((int) se.values[2]);

			// System.out.println("Acc :" +accX + " " + accY + " " + accZ);
			// SystemClock.sleep(100);
		}
	}

	// Seosor is here ↓↓↓↓↓↓↓↓↓↓↓↓↓
	private TimerTask second;
	int timer_sec;
	int count;

	public void checkEmergency() {
		second = new TimerTask() {

			@Override
			public void run() {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (sensor.checkEmergency2()) {

							try {
								if(iscon) {
									socket_out.write("11".getBytes());
									getGPS();
								}
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							final AlertDialog.Builder dlg = new AlertDialog.Builder(MainActivity.this);
							dlg.setTitle("Emergency");
							dlg.setMessage("Emergency Detected");
							dlg.setPositiveButton("OK", new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									dialog.dismiss();
								}
							});
							dlg.show();
						}
					}
				});
			}
		};
		Timer timer = new Timer();
		timer.schedule(second, 0, 400);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		// 추가내용을 다시 가져온 후 ListView에 적용
		c.requery();
		adapter.notifyDataSetChanged();
		SensorMan.registerListener(this, Gyro, SensorManager.SENSOR_DELAY_FASTEST);
		SensorMan.registerListener(this, Acc, SensorManager.SENSOR_DELAY_FASTEST);
	}

	// Getting GPS Info is here ↓↓↓↓↓↓↓↓↓↓↓↓↓
	public void getGPS() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {

				gps = new GpsInfo(MainActivity.this);

				if (gps.isGetLocation()) {

					double lati = gps.getLatitude();
					double longi = gps.getLongitude();

					latiude = new String(Double.toString(lati));
					longitude = new String( Double.toString(longi));


					System.out.println(latiude + longitude);

				} else {
					// GPS 를 사용할수 없으므로
					gps.showSettingsAlert();
				}
			}
		});

	}

	// 메뉴
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {

			case R.id.addMenu:

				LayoutInflater inf = getLayoutInflater();
				View convertView = inf.inflate(R.layout.contactadd, null);

				editName = (EditText) convertView.findViewById(R.id.editName);
				editAddress = (EditText) convertView.findViewById(R.id.editAddress);
				listButton = (Button) convertView.findViewById(R.id.listButton);

				AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
				builder.setTitle("연락처 등록");
				builder.setIcon(android.R.drawable.stat_sys_warning);
				builder.setView(convertView);

				listButton.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent intent = new Intent(Intent.ACTION_PICK);
						intent.setData(ContactsContract.CommonDataKinds.Phone.CONTENT_URI);// (1)
						// ~
						// (2)
						// 연락처
						// 선택
						startActivityForResult(intent, 0);// 결과물 리턴받기위한 함수
					}
				});

				builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

						String name = editName.getText().toString();
						String address = editAddress.getText().toString();

						handler.insert(name, address);

						// 추가내용을 다시 가져온 후 ListView에 적용
						c.requery();
						adapter.notifyDataSetChanged();
					}
				});
				builder.setNegativeButton("취소", null);
				builder.show();

				break;

			case R.id.addFromAdress:
				Log.i("MyTag", "전화번호부 메뉴 선택");
				break;

			case R.id.preferMenu:
				Log.i("MyTag", "환경설정 메뉴선택");
				break;
		}
		return super.onOptionsItemSelected(item);

	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			Cursor cursor = getContentResolver().query(data.getData(),
					new String[]{ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
							ContactsContract.CommonDataKinds.Phone.NUMBER},
					null, null, null);
			cursor.moveToFirst();
			String addname = cursor.getString(0); // 0은 이름.
			String addnumber = cursor.getString(1); // 1은 번호.
			editName.setText(addname);
			editAddress.setText(addnumber);
			cursor.close();
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onStart() {
		super.onStart();

		// ATTENTION: This was auto-generated to implement the App Indexing API.
		// See https://g.co/AppIndexing/AndroidStudio for more information.
		client.connect();
		Action viewAction = Action.newAction(
				Action.TYPE_VIEW, // TODO: choose an action type.
				"Main Page", // TODO: Define a title for the content shown.
				// TODO: If you have web page content that matches this app activity's content,
				// make sure this auto-generated web page URL is correct.
				// Otherwise, set the URL to null.
				Uri.parse("http://host/path"),
				// TODO: Make sure this auto-generated app deep link URI is correct.
				Uri.parse("android-app://com.example.ex07_09/http/host/path")
		);
		AppIndex.AppIndexApi.start(client, viewAction);
	}

	@Override
	public void onStop() {
		super.onStop();

		// ATTENTION: This was auto-generated to implement the App Indexing API.
		// See https://g.co/AppIndexing/AndroidStudio for more information.
		Action viewAction = Action.newAction(
				Action.TYPE_VIEW, // TODO: choose an action type.
				"Main Page", // TODO: Define a title for the content shown.
				// TODO: If you have web page content that matches this app activity's content,
				// make sure this auto-generated web page URL is correct.
				// Otherwise, set the URL to null.
				Uri.parse("http://host/path"),
				// TODO: Make sure this auto-generated app deep link URI is correct.
				Uri.parse("android-app://com.example.ex07_09/http/host/path")
		);
		AppIndex.AppIndexApi.end(client, viewAction);
		client.disconnect();
	}




}
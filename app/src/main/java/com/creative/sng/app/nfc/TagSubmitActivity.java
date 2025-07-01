package com.creative.sng.app.nfc;

import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.NfcF;
import android.os.Bundle;
import android.os.Parcelable;
import androidx.appcompat.app.AppCompatActivity;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.creative.sng.app.R;
import com.creative.sng.app.fragment.FragMenuActivity;
import com.creative.sng.app.nfc.set.NdefMessageParser;
import com.creative.sng.app.nfc.set.ParsedRecord;
import com.creative.sng.app.nfc.set.TextRecord;
import com.creative.sng.app.nfc.set.UriRecord;
import com.creative.sng.app.retrofit.Datas;
import com.creative.sng.app.retrofit.RetrofitService;
import com.creative.sng.app.util.UtilClass;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TagSubmitActivity extends AppCompatActivity {
	private final String TAG = "TagSubmitActivity";
	private ProgressDialog pDlalog = null;

	TextView readResult;

	private NfcAdapter mAdapter;
	private PendingIntent mPendingIntent;
	private IntentFilter[] mFilters;
	private String[][] mTechLists;

	private RetrofitService service;

	public static final int TYPE_TEXT = 1;
	public static final int TYPE_URI = 2;

	//태그관련
	private String tag_value="";
	private String equip_no;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_read);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);//화면 자동 꺼짐 방지.

		service = RetrofitService.rest_api.create(RetrofitService.class);
		readResult = (TextView) findViewById(R.id.readResult);

		// NFC 관련 객체 생성
		mAdapter = NfcAdapter.getDefaultAdapter(this);

		Intent targetIntent = new Intent(this, TagSubmitActivity.class);
		targetIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		mPendingIntent = PendingIntent.getActivity(this, 0, targetIntent, 0);

		IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
		try {
			ndef.addDataType("*/*");
		} catch (MalformedMimeTypeException e) {
			throw new RuntimeException("fail", e);
		}

		mFilters = new IntentFilter[] { ndef, };

		mTechLists = new String[][] { new String[] { NfcF.class.getName() } };

		Intent passedIntent = getIntent();
		if (passedIntent != null) {
			String action = passedIntent.getAction();
			if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)) {
				// 태그 데이터가 들어 있는 인텐트가 전달 되었을 때 태그 정보 처리
				processTag(passedIntent);
			}
		}

		//테스트 코드
//		tag_value= "PSA3-4";
//		async_progress_dialog();
	}

	public void async_progress_dialog(){
		pDlalog = new ProgressDialog(this);
		UtilClass.showProcessingDialog(pDlalog);
		Call<Datas> call = service.listData("Check","tagDataInfo", tag_value);
		call.enqueue(new Callback<Datas>() {
			@Override
			public void onResponse(Call<Datas> call, Response<Datas> response) {
				UtilClass.logD(TAG, "response="+response);
				if (response.isSuccessful()) {
					UtilClass.logD(TAG, "isSuccessful="+response.body().toString());
					String status= response.body().getStatus();
					Toast.makeText(getApplicationContext(), "Scan success", Toast.LENGTH_SHORT).show();
					try {
						if(response.body().getCount()==0){
							Toast.makeText(getApplicationContext(), "해당 태그에 대한 데이터가 없습니다.", Toast.LENGTH_SHORT).show();
						}else{
							equip_no= response.body().getList().get(0).get("EQUIP_NO").toString();
							String use_part1= response.body().getList().get(0).get("USE_PART1").toString();
							String use_part2= response.body().getList().get(0).get("USE_PART2").toString();

							if(equip_no.equals("")){
								Toast.makeText(getApplicationContext(), "해당 태그에 대한 데이터가 없습니다.", Toast.LENGTH_SHORT).show();
							}else{
								UtilClass.logD(TAG, "TAG 모드");
								Intent intent = new Intent(getBaseContext(),FragMenuActivity.class);
								intent.putExtra("title", "NFC점검관리_INSERT");
								intent.putExtra("TAG_ID",tag_value);
								intent.putExtra("equip_no",equip_no);
								intent.putExtra("use_part1",use_part1);
								intent.putExtra("use_part2",use_part2);
								intent.putExtra("mode", "insert");
								startActivity(intent);
								finish();
							}

						}

					} catch ( Exception e ) {
						e.printStackTrace();
						Toast.makeText(getApplicationContext(), "에러코드 NFC 1", Toast.LENGTH_SHORT).show();
					}
				}else{
					Toast.makeText(getApplicationContext(), "response isFailed", Toast.LENGTH_SHORT).show();
				}
				if(pDlalog!=null) pDlalog.dismiss();
			}

			@Override
			public void onFailure(Call<Datas> call, Throwable t) {
				if(pDlalog!=null) pDlalog.dismiss();
				UtilClass.logD(TAG, "onFailure="+call.toString()+", "+t);
				Toast.makeText(getApplicationContext(), "onFailure NFC 1", Toast.LENGTH_SHORT).show();
			}
		});
	}


	/************************************
	 * 여기서부턴 NFC 관련 메소드
	 ************************************/
	public void onResume() {
		super.onResume();

		if (mAdapter != null) {
			mAdapter.enableForegroundDispatch(this, mPendingIntent, mFilters, mTechLists);
		}
	}

	public void onPause() {
		super.onPause();

		if (mAdapter != null) {
			mAdapter.disableForegroundDispatch(this);
		}
	}

	// NFC 태그 스캔시 호출되는 메소드
	public void onNewIntent(Intent passedIntent) {
		UtilClass.logD(TAG, "onNewIntent=" + passedIntent);
		// NFC 태그
		Tag tag = passedIntent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
		if (tag != null) {
			byte[] tagId = tag.getId();
			readResult.append("태그 ID : " + toHexString(tagId) + "\n"); // TextView에 태그 ID 덧붙임

		}

		if (passedIntent != null) {
			processTag(passedIntent); // processTag 메소드 호출
		}
	}

	// NFC 태그 ID를 리턴하는 메소드 (String 타입으로 변환)
	public static final String CHARS = "0123456789ABCDEF";
	public static String toHexString(byte[] data) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < data.length; ++i) {
			sb.append(CHARS.charAt((data[i] >> 4) & 0x0F)).append(
					CHARS.charAt(data[i] & 0x0F));
		}
		return sb.toString();
	}

	// onNewIntent 메소드 수행 후 호출되는 메소드
	private void processTag(Intent passedIntent) {
		Parcelable[] rawMsgs = passedIntent
				.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
		if (rawMsgs == null) {
			return;
		}

		// 참고! rawMsgs.length : 스캔한 태그 개수
		NdefMessage[] msgs;
		if (rawMsgs != null) {
			msgs = new NdefMessage[rawMsgs.length];
			for (int i = 0; i < rawMsgs.length; i++) {
				msgs[i] = (NdefMessage) rawMsgs[i];
				showTag(msgs[i]); // showTag 메소드 호출
			}
		}

		//태그값 넘기기
		async_progress_dialog();

	}

	// NFC 태그 정보를 읽어들이는 메소드
	private int showTag(NdefMessage mMessage) {
		List<ParsedRecord> records = NdefMessageParser.parse(mMessage);
		final int size = records.size();
		for (int i = 0; i < size; i++) {
			ParsedRecord record = records.get(i);

			int recordType = record.getType();
			String recordStr = ""; // NFC 태그로부터 읽어들인 텍스트 값
			if (recordType == ParsedRecord.TYPE_TEXT) {
				recordStr = "TEXT : " + ((TextRecord) record).getText();
			} else if (recordType == ParsedRecord.TYPE_URI) {
				recordStr = "URI : " + ((UriRecord) record).getUri().toString();
			}
			tag_value = ((TextRecord) record).getText();
			readResult.append(recordStr + "\n"); // 읽어들인 텍스트 값을 TextView에 덧붙임
		}

		return size;
	}
}

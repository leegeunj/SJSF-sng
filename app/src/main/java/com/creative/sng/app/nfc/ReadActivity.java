package com.creative.sng.app.nfc;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.NfcF;
import android.os.Bundle;
import android.os.Parcelable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import com.creative.sng.app.R;
import com.creative.sng.app.nfc.set.NdefMessageParser;
import com.creative.sng.app.nfc.set.ParsedRecord;
import com.creative.sng.app.nfc.set.TextRecord;
import com.creative.sng.app.nfc.set.UriRecord;

import java.util.List;

public class ReadActivity extends Activity {

	TextView readResult;

	private NfcAdapter mAdapter;
	private PendingIntent mPendingIntent;
	private IntentFilter[] mFilters;
	private String[][] mTechLists;

	public static final int TYPE_TEXT = 1;
	public static final int TYPE_URI = 2;

	//태그값 저장
	String tag_value="";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_read);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);//화면 자동 꺼짐 방지.

		readResult = (TextView) findViewById(R.id.readResult);

		// NFC 관련 객체 생성
		mAdapter = NfcAdapter.getDefaultAdapter(this);
		Intent targetIntent = new Intent(this, ReadActivity.class);
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
				processTag(passedIntent);
			}
		}

		//테스트 코드
//		passedIntent.putExtra("TAG_ID","75");
//		this.setResult(RESULT_OK,passedIntent);
//		finish();
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
								 Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_read, container,
					false);
			return rootView;
		}
	}

	/************************************
	 * 여기서부턴 NFC 관련 메소드
	 ************************************/
	public void onResume() {
		super.onResume();

		if (mAdapter != null) {
			mAdapter.enableForegroundDispatch(this, mPendingIntent, mFilters,
					mTechLists);
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
		// NFC 태그
		Tag tag = passedIntent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
		if (tag != null) {
			byte[] tagId = tag.getId();
			readResult.setText("");
			readResult.append("태그 ID : " + toHexString(tagId) + "\n"); // TextView에 태그 ID 덧붙임

		}

		if (passedIntent != null) {
			processTag(passedIntent); // processTag 메소드 호출
		}
	}

	// NFC 태그 ID를 리턴하는 메소드
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
		Toast.makeText(getApplicationContext(), "Scan success", Toast.LENGTH_SHORT).show();

		NdefMessage[] msgs;
		if (rawMsgs != null) {
			msgs = new NdefMessage[rawMsgs.length];
			for (int i = 0; i < rawMsgs.length; i++) {
				msgs[i] = (NdefMessage) rawMsgs[i];
				showTag(msgs[i]); // showTag 메소드 호출
			}
		}
	}

	private class WishWebViewClient extends WebViewClient {
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url){
			view.loadUrl(url);

			return true;
		}
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

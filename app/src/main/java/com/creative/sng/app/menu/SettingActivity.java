package com.creative.sng.app.menu;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import androidx.preference.Preference;
import android.preference.PreferenceManager;
import android.provider.Settings;
import androidx.preference.PreferenceFragmentCompat;
import android.text.TextUtils;
import android.widget.Toast;

import com.creative.sng.app.R;
import com.creative.sng.app.nfc.ReadActivity;
import com.creative.sng.app.nfc.TagSubmitActivity;
import com.creative.sng.app.nfc.WriteActivity;
import com.creative.sng.app.util.UtilClass;

import butterknife.ButterKnife;

/**
 * Created by GS on 2015-11-09.
 */
public class SettingActivity extends PreferenceFragmentCompat implements Preference.OnPreferenceClickListener{

    private final String TAG= this.getClass().getSimpleName();
    private NfcAdapter mAdapter;

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.app_setting);
        ButterKnife.bind(getActivity());
//        Preference app_ver = findPreference("keyAppVersion");
//        app_ver.setTitle(MainActivity.getAppVersion(getActivity()));

        Preference nfc_read = findPreference("nfc_read");
        Preference nfc_write = findPreference("nfc_write");
        Preference nfc_setting = findPreference("nfc_setting");

        nfc_read.setOnPreferenceClickListener(this);
        nfc_write.setOnPreferenceClickListener(this);
        nfc_setting.setOnPreferenceClickListener(this);

        //setOnPreferenceChange(findPreference("autoUpdate_ringtone"));

//        boolean test=isServiceRunningCheck();
//        Log.d("isServiceRunningCheck=", test + "");

    }



    //서비스 돌아가는지 확인
    public boolean isServiceRunningCheck() {
        ActivityManager manager = (ActivityManager) getActivity().getSystemService(Activity.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if ("com.pro1.gs.callme_info.CallingService".equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public boolean nfcCheck(){
        boolean isCheck;
        mAdapter = NfcAdapter.getDefaultAdapter(getActivity());
        if (mAdapter == null) {
            Toast.makeText(getActivity(), "NFC를 지원하지 않는 단말기입니다.", Toast.LENGTH_SHORT).show();
            isCheck = false;
        }else{
            if(mAdapter.isEnabled()){
                isCheck = true;
            }else{
                isCheck = false;
            }
        }
        UtilClass.logD(TAG, "mAdapter= "+mAdapter+", isCheck="+isCheck);
        return isCheck;
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        switch(preference.getKey()) {
            case "nfc_submit":
//                Intent Subintent = new Intent(getActivity(), TagSubmitActivity.class);
//                startActivity(Subintent);
                if(nfcCheck()){
                    Intent subintent = new Intent(getActivity(), TagSubmitActivity.class);
                    startActivity(subintent);
                }else{
                    if(mAdapter != null) Toast.makeText(getActivity(), "환경설정에서 NFC 를 활성화 해주세요. ", Toast.LENGTH_SHORT).show();
                }
                break;
            case "nfc_read":
                if(nfcCheck()){
                    Intent intent = new Intent(getActivity(), ReadActivity.class);
                    startActivity(intent);
                }else{
                    if(mAdapter != null) Toast.makeText(getActivity(), "환경설정에서 NFC 를 활성화 해주세요. ", Toast.LENGTH_SHORT).show();
                }
                break;
            case "nfc_write":
                if(nfcCheck()){
                    Intent intent2 = new Intent(getActivity(), WriteActivity.class);
                    startActivity(intent2);
                }else{
                    if(mAdapter != null) Toast.makeText(getActivity(), "환경설정에서 NFC 를 활성화 해주세요. ", Toast.LENGTH_SHORT).show();
                }
                break;
            case "nfc_setting":
                mAdapter = NfcAdapter.getDefaultAdapter(getActivity());

                if (mAdapter == null) {
                    Toast.makeText(getActivity(), "NFC를 지원하지 않는 단말기입니다.", Toast.LENGTH_SHORT).show();
                }else{
                    // NFC 환경설정 화면 호출
                    Intent intent3 = new Intent(Settings.ACTION_NFC_SETTINGS );
                    startActivity(intent3);
                }

                break;
        }
        return false;
    }

    private void setOnPreferenceChange(Preference mPreference) {
        mPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);

        onPreferenceChangeListener.onPreferenceChange(
                mPreference,
                PreferenceManager.getDefaultSharedPreferences(
                        mPreference.getContext()).getString(mPreference.getKey(), ""));
    }


    private androidx.preference.Preference.OnPreferenceChangeListener onPreferenceChangeListener =
            new androidx.preference.Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            String stringValue = newValue.toString();

            if (preference instanceof androidx.preference.EditTextPreference) {
                preference.setSummary(stringValue);
            } else if (preference instanceof androidx.preference.ListPreference) {
                /**
                 * ListPreference의 경우 stringValue가 entryValues이기 때문에 바로 Summary를
                 * 적용하지 못한다 따라서 설정한 entries에서 String을 로딩하여 적용한다
                 */

                androidx.preference.ListPreference listPreference = (androidx.preference.ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                preference
                        .setSummary(index >= 0 ? listPreference.getEntries()[index]
                                : null);

            } else if (preference instanceof androidx.preference.ListPreference) {
                /**
                 * RingtonePreference의 경우 stringValue가
                 * content://media/internal/audio/media의 형식이기 때문에
                 * RingtoneManager을 사용하여 Summary를 적용한다
                 *
                 * 무음일경우 ""이다
                 */

                if (TextUtils.isEmpty(stringValue)) {
                    // Empty values correspond to 'silent' (no ringtone).
                    preference.setSummary("무음으로 설정됨");

                } else {
                    Ringtone ringtone = RingtoneManager.getRingtone(
                            preference.getContext(), Uri.parse(stringValue));

                    if (ringtone == null) {
                        // Clear the summary if there was a lookup error.
                        preference.setSummary(null);

                    } else {
                        String name = ringtone
                                .getTitle(preference.getContext());
                        preference.setSummary(name);
                    }
                }
            }

            return true;
        }

    };

}

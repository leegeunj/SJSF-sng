package com.creative.sng.app.equip;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.creative.sng.app.R;
import com.creative.sng.app.fragment.FragMenuActivity;
import com.creative.sng.app.menu.MainFragment;
import com.creative.sng.app.util.KeyValueArrayAdapter;
import com.creative.sng.app.util.UtilClass;

import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class EquipPopupActivity extends Activity {
    private static final String TAG = "EquipPopupActivity";
    private String url = MainFragment.ipAddress+MainFragment.contextPath+"/rest/Equip/equipGubunList/gubun=";
    private String info_url = MainFragment.ipAddress+MainFragment.contextPath+"/rest/Equip/equipInfoList";

    private String[] gubunKeyList;
    private String[] gubunValueList;
    private String[] equipKeyList;
    private String[] equipValueList;
    String selectGubunKey="";
    String selectEquipKey="";
    String gubun;

    @BindView(R.id.spinner1) Spinner spn_gubun;
    @BindView(R.id.spinner2) Spinner spn_equip;
    @BindView(R.id.textView1) TextView _user_name;

    private AQuery aq = new AQuery( this );

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.equip_popup);
        ButterKnife.bind(this);
        this.setFinishOnTouchOutside(false);

        _user_name.setText(MainFragment.loginName);

        String gubunCheck = getIntent().getStringExtra("gubun");
        if(gubunCheck.equals("PSM대상설비점검")){
            gubun="P";
        }else{
            gubun="1";
        }
        getGubunData();

        spn_gubun.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                KeyValueArrayAdapter adapter = (KeyValueArrayAdapter) parent.getAdapter();
                selectGubunKey= adapter.getEntryValue(position);
//                UtilClass.logD("LOG", "KEY : " + adapter.getEntryValue(position));
//                UtilClass.logD("LOG", "VALUE : " + adapter.getEntry(position));
                async_progress_dialog("getEquipData");
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        spn_equip.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                KeyValueArrayAdapter adapter = (KeyValueArrayAdapter) parent.getAdapter();
                selectEquipKey= adapter.getEntryValue(position);
                UtilClass.logD("LOG", "KEY : " + adapter.getEntryValue(position));
                UtilClass.logD("LOG", "VALUE : " + adapter.getEntry(position));
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

    }//onCreate

    public void async_progress_dialog(String callback){
        ProgressDialog dialog = ProgressDialog.show(EquipPopupActivity.this, "", "Loading...", true, false);
        dialog.setInverseBackgroundForced(false);

        if(callback.equals("getEquipData")){
            aq.ajax( info_url+"/equip_cd="+selectGubunKey, JSONObject.class, new AjaxCallback<JSONObject>() {
                @Override
                public void callback(String url, JSONObject object, AjaxStatus status ) {
                    if( object != null) {
                        try {
                            equipKeyList= new String[object.getJSONArray("datas").length()];
                            equipValueList= new String[object.getJSONArray("datas").length()];
                            for(int i=0; i<object.getJSONArray("datas").length();i++){
                                equipKeyList[i]= object.getJSONArray("datas").getJSONObject(i).get("equip_cd").toString().trim();
                                equipValueList[i]= object.getJSONArray("datas").getJSONObject(i).get("equip_cd").toString().trim()
                                    +"  "+object.getJSONArray("datas").getJSONObject(i).get("equip_nm").toString().trim();
                            }

                            KeyValueArrayAdapter adapter = new KeyValueArrayAdapter(EquipPopupActivity.this, android.R.layout.simple_spinner_dropdown_item);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            adapter.setEntries(equipValueList);
                            adapter.setEntryValues(equipKeyList);

                            spn_equip.setPrompt("설비");
                            spn_equip.setAdapter(adapter);
                        } catch ( Exception e ) {
                            Toast.makeText(getApplicationContext(), "에러코드 Equip 2", Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        UtilClass.logD(TAG,"Data is Null");
                        Toast.makeText(getApplicationContext(),"데이터가 없습니다.", Toast.LENGTH_SHORT).show();
                    }
                }
            } );
        }
        aq.progress(dialog).ajax(info_url, JSONObject.class, this, callback);
    }

    public void getGubunData() {
        aq.ajax( url+gubun, JSONObject.class, new AjaxCallback<JSONObject>() {
            @Override
            public void callback(String url, JSONObject object, AjaxStatus status ) {
                if( object != null) {
                    try {
                        gubunKeyList= new String[object.getJSONArray("datas").length()];
                        gubunValueList= new String[object.getJSONArray("datas").length()];
                        for(int i=0; i<object.getJSONArray("datas").length();i++){
                            gubunKeyList[i]= object.getJSONArray("datas").getJSONObject(i).get("equip_cd").toString();
                            gubunValueList[i]= object.getJSONArray("datas").getJSONObject(i).get("equip_nm").toString();
                        }

                        KeyValueArrayAdapter adapter = new KeyValueArrayAdapter(EquipPopupActivity.this, android.R.layout.simple_spinner_dropdown_item);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        adapter.setEntries(gubunValueList);
                        adapter.setEntryValues(gubunKeyList);

                        spn_gubun.setPrompt("구분");
                        spn_gubun.setAdapter(adapter);
                    } catch ( Exception e ) {
                        Toast.makeText(getApplicationContext(), "에러코드 Equip 1", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    UtilClass.logD(TAG,"Data is Null");
                    Toast.makeText(getApplicationContext(),"데이터가 없습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        } );

    }

    @OnClick(R.id.textView3)
    public void closePopup() {
        finish();
    }

    @OnClick(R.id.textView2)
    public void nextDataInfo() {
        if(gubunKeyList.length==0){
            Toast.makeText(getApplicationContext(), "선택 항목이 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(getBaseContext(),FragMenuActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("title", "설비점검리스트");
        intent.putExtra("selectEquipKey", selectEquipKey);
        intent.putExtra("gubun", gubun);
        startActivity(intent);
    }
}

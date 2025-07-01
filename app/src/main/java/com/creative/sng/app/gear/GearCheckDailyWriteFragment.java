package com.creative.sng.app.gear;


import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.NfcF;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.Settings;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.creative.sng.app.R;
import com.creative.sng.app.adaptor.GearAdapter;
import com.creative.sng.app.fragment.FragMenuActivity;
import com.creative.sng.app.menu.MainActivity;
import com.creative.sng.app.menu.MainFragment;
import com.creative.sng.app.nfc.set.NdefMessageParser;
import com.creative.sng.app.nfc.set.ParsedRecord;
import com.creative.sng.app.nfc.set.TextRecord;
import com.creative.sng.app.nfc.set.UriRecord;
import com.creative.sng.app.retrofit.Datas;
import com.creative.sng.app.retrofit.RetrofitService;
import com.creative.sng.app.util.KeyValueArrayAdapter;
import com.creative.sng.app.util.UtilClass;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GearCheckDailyWriteFragment extends Fragment {
    private static final String TAG = "GearCheckDailyWriteFragment";
    public static final String STATE_NO = "1";
    public static final String STATE_YES = "2";
    public static final String STATE_ALL = "All";

    private ProgressDialog pDlalog = null;
    private RetrofitService service;
    private GearAdapter mAdapter;

    private String mode="";
    private String idx="";
    private String dataSabun;
    private String gear_key;
    private String param1;
    private String param2;
    private String param3;
    private String emp_cd1;
    private String emp_nm1;
    private String emp_cd2;
    private String lvl_st1, lvl_st2;
    private String qParams;
    private boolean isCheckPerson=false;
    private String stateString="";
    private String title, pcType;
    private String authLev="0";
    private String stateValue="2";

    @BindView(R.id.top_title) TextView textView;
    @BindView(R.id.textView1) TextView gear_cd;
    @BindView(R.id.textView2) TextView input_date;
    @BindView(R.id.textView3) TextView check_person_nm;
    @BindView(R.id.spinner1)  Spinner  yn_second;
    @BindView(R.id.listView1) ListView listView;
    @BindView(R.id.emp1_identy) TextView emp1_identy;
    @BindView(R.id.txt_approval) TextView textApproval;
    @BindView(R.id.linear9)  LinearLayout yesCheckBtn;
    @BindView(R.id.linear5)  LinearLayout noCheckBtn;
    @BindView(R.id.layoutNoCheck) LinearLayout noCheckGroup;
    @BindView(R.id.linear6)  LinearLayout saveBtn;
    @BindView(R.id.linear7)  LinearLayout cancelBtn;
    @BindView(R.id.noCheckEdit)  EditText noCheckContext;


    private AQuery aq;
    private String selectedPostionKey;  //스피너 선택된 키값
    private int selectedPostion=0;    //스피너 선택된 Row 값

    private String[] gubunKeyList;
    private String[] gubunValueList;
    private ArrayList<HashMap<String,Object>> arrayList;
    private FragmentManager fm;

//    NFC
    private NfcAdapter nfcAdapter;
    private PendingIntent mPendingIntent;
    private IntentFilter[] mFilters;
    private String[][] mTechLists;


    //  태깅 후 넘어온 데이타
    private String pendingIntent;
    private String target;
    private String tagValue;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        title= getArguments().getString("title");
        pcType= getArguments().getString("pc_type");

        setHasOptionsMenu(true);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.gear_check_daily_write, container, false);
        ButterKnife.bind(this, view);
        service= RetrofitService.rest_api.create(RetrofitService.class);
        aq = new AQuery( getActivity() );
//      넘어온값들 변수에 넣기
        mode = getArguments().getString("mode");
        dataSabun = MainFragment.loginSabun;
        gear_key = getArguments().getString("gear_key");
        param1 = getArguments().getString("gear_cd");
        param2 = getArguments().getString("input_date");
        param3 = getArguments().getString("check_person");
        emp_cd1 = getArguments().getString("emp_cd1");
        emp_nm1 = getArguments().getString("emp_nm1");
        emp_cd2 = getArguments().getString("emp_cd2");
        lvl_st1 = getArguments().getString("lvl1_app_tp");
        lvl_st2 = getArguments().getString("lvl2_app_tp");
        authLev = getArguments().getString("authLev");
        qParams = param1;

        UtilClass.logD("Bundle1: ", "mode: "+mode+", dataSabun: "+dataSabun+", gear_key: "+gear_key+", param1: "+param1+", param2: "+param2+", param3: "+param3);
        UtilClass.logD("Bundle2: ", "emp_cd1: "+emp_cd1+", emp_cd2: "+emp_cd2+", emp_nm1: "+emp_nm1+", lvl_st1: "+lvl_st1+", lvl_st2: "+lvl_st2+", qparams: "+qParams);
        textView.setText(title);

        gear_cd.setText(param1);
        input_date.setText(param2);
        check_person_nm.setText(param3);

//        2차승인자에게만 저장 버튼으로 저장하게 처리
//        authLev 1 : 1차승인자/ 2: 2차승인자 / 3: 운영자
        if(authLev.equals("1")){
            yesCheckBtn.setVisibility(View.GONE);
            equipApprovalData("2");
            textApproval.setText("2차승인");
            stateString= STATE_YES;
            selectedPostionKey=emp_cd2;
        } else if (authLev.equals("2")) {
            yesCheckBtn.setVisibility(View.VISIBLE);
            yn_second.setVisibility(View.GONE);
            textApproval.setText("1차확인");
            emp1_identy.setText(emp_nm1);
            emp1_identy.setVisibility(View.VISIBLE);
            stateString= STATE_ALL;
            selectedPostionKey="";
            isCheckPerson=true;
        } else {
            stateString= STATE_NO;
        }

//      승인자 세팅
//        1차승인이 안되어있으면...점검에서 1차승인자 지정
//        if(emp_cd1.equals("")) {
//            equipApprovalData("1");
//            stateString= STATE_NO;
//        2차승인이 안되어 있으면 2차승인자 콤보 넣고 txt2차승인자로 변경
//        } else if (emp_cd2.equals("")) {
//            equipApprovalData("2");
//            textApproval.setText("2차승인");
//            stateString= STATE_YES;
//            selectedPostionKey=emp_cd2;
//        2차승인자이 들어갔으면 1차승인자 명으로 나오기
//        } else {
//            yn_second.setVisibility(View.GONE);
//            textApproval.setText("1차확인");
//            emp1_identy.setText(emp_nm1);
//            emp1_identy.setVisibility(View.VISIBLE);
//            stateString= STATE_ALL;
//            selectedPostionKey="";
//            isCheckPerson=true;
//        }

        yn_second.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                KeyValueArrayAdapter adapter = (KeyValueArrayAdapter) parent.getAdapter();
                selectedPostionKey = adapter.getEntryValue(position);

                UtilClass.logD("LOG", "KEY : " + adapter.getEntryValue(position));
                UtilClass.logD("LOG", "VALUE : " + adapter.getEntry(position));
                isCheckPerson = true;
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

//      점검항목가져오기
        async_progress_dialog();

//NFC   화면 자동꺼짐 방지
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        nfcAdapter = NfcAdapter.getDefaultAdapter(getActivity());
        if(nfcAdapter!=null){
            if(!nfcAdapter.isEnabled()){
                alertDialogNfc();
            }
        }

        Intent targetIntent = new Intent(getActivity(), FragMenuActivity.class);
        targetIntent.putExtra("title", "점검일지승인");
        targetIntent.putExtra("pendingIntent", title);
        targetIntent.putExtra("target", "Check");
        targetIntent.putExtra("targetPoint", selectedPostionKey);
        targetIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        mPendingIntent = PendingIntent.getActivity(getActivity(), 0, targetIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        try {
            ndef.addDataType("*/*");
        } catch (IntentFilter.MalformedMimeTypeException e) {
            throw new RuntimeException("fail", e);
        }

        mFilters = new IntentFilter[] { ndef, };
        mTechLists = new String[][] { new String[] { NfcF.class.getName() } };



        return view;
    }
    //NFC TAG 내용을 여기서 점검하고 차량번호와 다르면 오류
    public void tagToast(String tagv){
        UtilClass.logD(TAG, "response vvvvv="+tagv);
        if(!tagv.equals("")){
            if(tagv.equals(param1)){
                if (selectedPostionKey.equals("0")) {
                    Toast.makeText(getActivity(), "2차승인자를 지정하십시요.",Toast.LENGTH_LONG).show();
                    return;
                } else postData();
            } else{
                Toast.makeText(getActivity(),"현재 차량번호 와 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }

//    NFC 꺼져있으면 폰 설정 NFC로 이동 및 Message 띄우기
    private void alertDialogNfc() {
        final android.app.AlertDialog.Builder alertDlg = new android.app.AlertDialog.Builder(getActivity());
        alertDlg.setTitle("알림");
        alertDlg.setMessage("NFC 기능이 꺼져 있습니다.");

        alertDlg.setPositiveButton("설정 하기", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(nfcAdapter == null){
                    Toast.makeText(getActivity(), "NFC를 지원하지 않는 단말기 입니다.", Toast.LENGTH_SHORT).show();
                }else{
                    Intent intent3 = new Intent(Settings.ACTION_NFC_SETTINGS);
                    startActivity(intent3);
                }
            }
        });

        alertDlg.setNegativeButton("닫기", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDlg.show();
    }

    private void async_progress_dialog() {
        final ProgressDialog pDlalog = new ProgressDialog(getActivity());
        UtilClass.showProcessingDialog(pDlalog);

        Call<Datas> call = service.listDataQ("Gear","gearCheckDailyItemList",gear_key, param2, qParams);
        call.enqueue(new Callback<Datas>() {
            @Override
            public void onResponse(Call<Datas> call, Response<Datas> response) {
                UtilClass.logD(TAG, "response="+response);
                if (response.isSuccessful()) {
                    UtilClass.logD(TAG, "isSuccessful="+response.body().toString());
                    String status= response.body().getStatus();
                    try {
                        if(response.body().getCount()==0){
                            Toast.makeText(getActivity(), "데이터가 없습니다.", Toast.LENGTH_SHORT).show();
                        }
                        arrayList = new ArrayList<>();
                        arrayList.clear();
                        for(int i=0; i<response.body().getList().size();i++){
                            UtilClass.dataNullCheckZero(response.body().getList().get(i));

                            HashMap<String,Object> hashMap = new HashMap<>();
                            hashMap.put("key",response.body().getList().get(i).get("check_cd"));
                            hashMap.put("data1",response.body().getList().get(i).get("check_nm"));
                            hashMap.put("data2",response.body().getList().get(i).get("check_result_nm"));
                            hashMap.put("data3",response.body().getList().get(i).get("check_content"));
                            hashMap.put("data4",response.body().getList().get(i).get("check_result"));
                            hashMap.put("ord",response.body().getList().get(i).get("ord"));
//                            hashMap.put("emp_cd2",response.body().getList().get(i).get("emp_cd2"));
                            arrayList.add(hashMap);
                        }

                        mAdapter = new GearAdapter(getActivity(), arrayList, "GearCheckDailyItem");
                        listView.setAdapter(mAdapter);
                    } catch ( Exception e ) {
                        e.printStackTrace();
                        Toast.makeText(getActivity(), "에러코드 Running 1", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(getActivity(), "response isFailed", Toast.LENGTH_SHORT).show();
                }
                if(pDlalog!=null) pDlalog.dismiss();
            }

            @Override
            public void onFailure(Call<Datas> call, Throwable t) {
                if(pDlalog!=null) pDlalog.dismiss();
                UtilClass.logD(TAG, "onFailure="+call.toString()+", "+t);
                Toast.makeText(getActivity(), "onFailure Running",Toast.LENGTH_SHORT).show();
            }
        });
    }


    //승인자.
    public void equipApprovalData(String app_lvl) {
        String url = MainFragment.ipAddress+MainFragment.contextPath+"/rest/Gear/equipApprovalList?app_level="+app_lvl;
        Log.d("승인자1주소: ", url);

        aq.ajax( url, JSONObject.class, new AjaxCallback<JSONObject>() {
            @Override
            public void callback(String url, JSONObject object, AjaxStatus status ) {
                if( object != null) {
                    try {
                        gubunKeyList= new String[object.getJSONArray("datas").length()+1];
                        gubunValueList= new String[object.getJSONArray("datas").length()+1];
                        gubunKeyList[0]="0";
                        gubunValueList[0]="선택하세요";
                        for(int i=0; i<object.getJSONArray("datas").length();i++){
                            gubunKeyList[i+1]= object.getJSONArray("datas").getJSONObject(i).get("emp_cd").toString();
                            if(gubunKeyList[i+1].equals(selectedPostionKey))  selectedPostion= i+1;
                            gubunValueList[i+1]= object.getJSONArray("datas").getJSONObject(i).get("user_nm").toString();
                        }
                        KeyValueArrayAdapter adapter = new KeyValueArrayAdapter(getActivity(), android.R.layout.simple_spinner_dropdown_item);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        adapter.setEntryValues(gubunKeyList);
                        adapter.setEntries(gubunValueList);

                        yn_second.setPrompt("구분");
                        yn_second.setAdapter(adapter);
                        yn_second.setSelection(selectedPostion);
                    } catch ( Exception e ) {

                    }
                }else{
                    UtilClass.logD(TAG,"Data is Null");
                    Toast.makeText(getActivity(),"데이터가 없습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        } );
    }

    @OnClick(R.id.top_home)
    public void goHome() {
        UtilClass.goHome(getActivity());
    }

//    저장버튼
    @OnClick(R.id.textButton1)
    public void alertDialogSave(){
        if(MainFragment.loginSabun.equals(dataSabun)){
            if(!isCheckPerson){
                Toast.makeText(getActivity(),"승인자를 선택하십시요.", Toast.LENGTH_SHORT).show();
            }else {
                alertDialog("S");
            }
        }else{
            Toast.makeText(getActivity(),"작성자만 가능합니다.", Toast.LENGTH_SHORT).show();
        }
    }
//    190514 반려 버튼 클릭시
    @OnClick(R.id.noCheckBtn)
    public void noCheckBtnClick() {
        noCheckGroup.setVisibility(View.VISIBLE);
    }
//    취소버튼
    @OnClick(R.id.cancelBtn)
    public void cancelBtnClick() {
        noCheckGroup.setVisibility(View.GONE);
    }
//    반려저장버튼
    @OnClick(R.id.saveBtn)
    public void saveBtnClick() {
        if(stateString.equals(STATE_YES)||stateString.equals(STATE_ALL)){
            stateValue="3";
        }
        alertDialog("S");
    }
//    2차 승인버튼
    @OnClick(R.id.yesCheckBtn)
    public void yesCheckBtnClick() {
        alertDialog("S");
    }

    public void alertDialog(final String gubun) {
        final AlertDialog.Builder alertDlg = new AlertDialog.Builder(getActivity());
        alertDlg.setTitle("알림");
        if (gubun.equals("S")) {
            alertDlg.setMessage("작성하시겠습니까?");
        } else {
            alertDlg.setMessage("삭제하시겠습니까?");
        }
        // '예' 버튼이 클릭되면
        alertDlg.setPositiveButton("예", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (gubun.equals("S")) {
                    postData();
                }/*else if(gubun.equals("D")){
                    deleteData();
                }else{
                    groupPostData();
                }*/
            }
        });
        // '아니오' 버튼이 클릭되면
        alertDlg.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();  // AlertDialog를 닫는다.
            }
        });
        alertDlg.show();
    }

    //작성,수정
    public void postData() {
        String checkDaily_key = gear_key;

        if (selectedPostionKey.equals("0")&&stateValue.equals("2")) {
            Toast.makeText(getActivity(), "2차승인자를 지정하십시요.",Toast.LENGTH_LONG).show();
            return;
        }

        if (stateValue.equals("3")&&noCheckContext.getText().toString().length()<1) {
            Toast.makeText(getActivity(), "반려시 전달내용은 필수 입니다.", Toast.LENGTH_LONG).show();
            return;
        }

        Map<String, Object> map = new HashMap();
        map.put("writer_sabun", MainFragment.loginSabun);
        map.put("writer_name", MainFragment.loginName);
        map.put("gear_key", checkDaily_key);
        map.put("state_what", stateString);
        map.put("emp_cd", selectedPostionKey);
        map.put("nocheck_context", noCheckContext.getText().toString());
        map.put("state_value", stateValue);

        UtilClass.logD(TAG, "map="+map);

        pDlalog = new ProgressDialog(getActivity());
        UtilClass.showProcessingDialog(pDlalog);

        Call<Datas> call= null;
        if(mode.equals("insert")){
            call = service.insertData("Gear","gearCheckDailyWrite", map);
        }else{
            call = service.updateData("Gear","gearCheckDailyModify/"+gear_key , map);
            map.put("idx",gear_key);
        }

        call.enqueue(new Callback<Datas>() {
            @Override
            public void onResponse(Call<Datas> call, Response<Datas> response) {
                UtilClass.logD(TAG, "response="+response);
                if (response.isSuccessful()) {
                    UtilClass.logD(TAG, "isSuccessful="+response.body().toString());
                    handleResponse(response);
                }else{
                    Toast.makeText(getActivity(), "작업에 실패하였습니다.",Toast.LENGTH_LONG).show();
                }
                if(pDlalog!=null) pDlalog.dismiss();
            }
            @Override
            public void onFailure(Call<Datas> call, Throwable t) {
                if(pDlalog!=null) pDlalog.dismiss();
                UtilClass.logD(TAG, "onFailure="+call.toString()+", "+t);
                Toast.makeText(getActivity(), "handleResponse GearCheckDailyWrite",Toast.LENGTH_LONG).show();
            }
        });
    }

    //작성 완료
    public void handleResponse(Response<Datas> response) {
        UtilClass.logD(TAG,"response="+response);
        try {
            String status= response.body().getStatus();
            if(status.equals("success")){
//            20190517 1차 반려 / 2차 승인, 반려 시 뒤로가기 하면서 리플레쉬
                if((authLev.equals("1")&&stateValue.equals("3"))||(authLev.equals("2")&&stateValue.equals("2"))||(authLev.equals("2")&&stateValue.equals("3")) ) {
                    getActivity().onBackPressed();
                }
            }else {
                Toast.makeText(getActivity(), "저장 실패 했습니다.",Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /************************************
     * 여기서부턴 NFC 관련 메소드
     ************************************/
    public void onResume() {
        super.onResume();
        if (nfcAdapter != null) {
            UtilClass.logD(TAG,"여기오니1="+gear_key);
            UtilClass.logD(TAG,"여기오니2="+mPendingIntent);
            UtilClass.logD(TAG,"여기오니3="+mFilters);
            UtilClass.logD(TAG,"여기오니4="+mTechLists);
            UtilClass.logD(TAG,"여기오니5="+selectedPostionKey);
            nfcAdapter.enableForegroundDispatch(getActivity(), mPendingIntent, mFilters, mTechLists);
        }
    }

    public void onPause() {
        super.onPause();
        if (nfcAdapter != null) {
            nfcAdapter.disableForegroundDispatch(getActivity());
        }
    }

}

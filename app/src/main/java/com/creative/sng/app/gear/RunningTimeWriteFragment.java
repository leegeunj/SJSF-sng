package com.creative.sng.app.gear;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.creative.sng.app.R;
import com.creative.sng.app.adaptor.BaseAdapter;
import com.creative.sng.app.menu.MainFragment;
import com.creative.sng.app.retrofit.Datas;
import com.creative.sng.app.retrofit.RetrofitService;
import com.creative.sng.app.util.KeyValueArrayAdapter;
import com.creative.sng.app.util.UtilClass;

import net.jcip.annotations.NotThreadSafe;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RunningTimeWriteFragment extends Fragment {
    private static final String TAG = "RunningTimeWriteFragment";
    private RetrofitService service;
    private static final String INSERT_URL = MainFragment.ipAddress+MainFragment.contextPath+"/rest/Gear/runningTimeInsert";
    private static final String MODIFY_URL = MainFragment.ipAddress+MainFragment.contextPath+"/rest/Gear/runningTimeModify";

    @BindView(R.id.top_title) TextView textTitle;
    @BindView(R.id.textView1) TextView tv_data1;
    @BindView(R.id.textView2) TextView tv_data2;
    @BindView(R.id.textView3) TextView tv_data3;
    @BindView(R.id.textView5) TextView tv_data5;
    @BindView(R.id.editText1) EditText et_data1;
    @BindView(R.id.editText2) EditText et_data2;
    @BindView(R.id.editText3) EditText et_data3;
    @BindView(R.id.spinner1) Spinner spn1;
    @BindView(R.id.spinner2) Spinner spn2;

    private String selectedPostionKey;  //스피너 선택된 키값
    private int selectedPostion=0;    //스피너 선택된 Row 값

    private String selectedPostion2Key;  //스피너2 선택된 키값
    private int selectedPostion2=0;    //스피너2 선택된 Row 값

    private String groupCode;
    private String resultTime;
    private String calcTime;

    private String url = MainFragment.ipAddress+MainFragment.contextPath+"/rest/Gear/workPeramPlaceCodeList";
    private String selectGubunKey="";
    private String[] gubunKeyList;
    private String[] gubunValueList;

    //검색 다이얼로그
    private String dialogGubun="";
    private Dialog mDialog = null;
    private Spinner search_spi;
    private String search_gubun;	//검색 구분
    private EditText et_search;
    private ListView listView;
    private ArrayList<HashMap<String,String>> arrayList;
    private BaseAdapter mAdapter;
    private Button btn_search;
    private TextView btn_cancel;
    private String selectGearKey="";
    private String selectSabunKey="";
    private String mode;
    private String idx;

    private String s_key1;
    private String s_key2;
    private String s_key3;

    private AQuery aq;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.running_time, container, false);
        ButterKnife.bind(this, view);
        service= RetrofitService.rest_api.create(RetrofitService.class);
        aq = new AQuery( getActivity() );

        mode= getArguments().getString("mode");
        view.findViewById(R.id.top_save).setVisibility(View.VISIBLE);

        if(mode.equals("insert")){
            view.findViewById(R.id.linear2).setVisibility(View.GONE);
            textTitle.setText("가동시간 등록");

            selectSabunKey= MainFragment.loginSabun;
            tv_data3.setText(MainFragment.loginName);

            tv_data1.setText(UtilClass.getCurrentDate(1, "."));

            spinner1onLoad();
            getworkPeramPlaceCodeData();

        }else{
            textTitle.setText("가동시간 수정");
            idx= getArguments().getString("idx");
            async_progress_dialog();

        }

        view.findViewById(R.id.top_save).setVisibility(View.VISIBLE);

        spn2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                KeyValueArrayAdapter adapter = (KeyValueArrayAdapter) parent.getAdapter();
                selectGubunKey= adapter.getEntryValue(position);

                UtilClass.logD("LOG", "KEY : " + adapter.getEntryValue(position));
                UtilClass.logD("LOG", "VALUE : " + adapter.getEntry(position));
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        et_data2.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override

            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus) {
                    if(et_data1.getText().toString()==null||et_data1.getText().toString().length()==0){
                        Toast.makeText(getActivity(), "작업전 값을 입력하세요.",Toast.LENGTH_LONG).show();
                        return;
                    }
                    if(et_data2.getText().toString()==null||et_data2.getText().toString().length()==0){
                        Toast.makeText(getActivity(), "작업후 값을 입력하세요.",Toast.LENGTH_LONG).show();
                        return;
                    }
                    double prevNum= Double.valueOf(et_data1.getText().toString());
                    double afterNum= Double.valueOf(et_data2.getText().toString());
                    if(afterNum < prevNum){
                        Toast.makeText(getActivity(), "작업전 값이 더 큽니다.",Toast.LENGTH_LONG).show();
                        return;
                    }else{
                        try {
                            double result= afterNum-prevNum;
                            resultTime = String.format("%.2f", result);
                            int point= resultTime.indexOf(".");
                            String subResult= resultTime.substring(point+1);
                            if(subResult.equals("0")){
                                resultTime= resultTime.substring(0,point);
                            }
                            tv_data5.setText(resultTime);

                            //C012 계산
                            point= resultTime.indexOf(".");
                            subResult= resultTime.substring(0,point);
                            String subResult2= resultTime.substring(point+1);
                            float c012= (float) (Integer.parseInt(subResult2)*0.6);

                            calcTime= UtilClass.addZero(Integer.parseInt(subResult))+UtilClass.addZero((int) c012);

                        }catch (StringIndexOutOfBoundsException e){
                            e.printStackTrace();
                        }
                    }
                }
            }
        });

        et_data2.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView view, int keyCode, KeyEvent event) {

                return true;
            }

        });

        return view;
    }//onCreateView

    public void spinner1onLoad(){
        ArrayAdapter adapter = ArrayAdapter.createFromResource(getActivity(), R.array.work_group_list, android.R.layout.simple_spinner_dropdown_item);
//		spn1.setPrompt("구분을 선택하세요.");
        spn1.setAdapter(adapter);
        spn1.setSelection(selectedPostion);
        spn1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                groupCode= parent.getItemAtPosition(position).toString().substring(0,1);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    public void async_progress_dialog(){
        RetrofitService service = RetrofitService.rest_api.create(RetrofitService.class);

        final ProgressDialog pDlalog = new ProgressDialog(getActivity());
        UtilClass.showProcessingDialog(pDlalog);

        Call<Datas> call = service.listData("Gear","runningTimeDetail", idx);
        call.enqueue(new Callback<Datas>() {
            @Override
            public void onResponse(Call<Datas> call, Response<Datas> response) {
                UtilClass.logD(TAG, "response="+response);
                if (response.isSuccessful()) {
                    UtilClass.logD(TAG, "isSuccessful="+response.body().toString());
                    String status= response.body().getStatus();
                    try {
                        UtilClass.dataNullCheckZero(response.body().getList().get(0));

                        selectSabunKey= response.body().getList().get(0).get("sabun_no").trim();

                        if(MainFragment.loginSabun.equals(selectSabunKey)){

                        }else{
                            et_data1.setFocusableInTouchMode(false);
                            et_data2.setFocusableInTouchMode(false);
                            et_data3.setFocusableInTouchMode(false);
                            getActivity().findViewById(R.id.linear1).setVisibility(View.GONE);
                            getActivity().findViewById(R.id.linear2).setVisibility(View.GONE);
                        }
                        selectedPostionKey =  response.body().getList().get(0).get("work_group").toString();
                        selectedPostion2Key = response.body().getList().get(0).get("locater").toString();
                        selectGearKey = response.body().getList().get(0).get("gear_cd").toString();

                        tv_data1.setText(response.body().getList().get(0).get("input_date").toString());
                        tv_data2.setText(response.body().getList().get(0).get("gear_gnm").toString());
                        tv_data3.setText(response.body().getList().get(0).get("work_user").toString());

                        et_data1.setText(response.body().getList().get(0).get("tachomt_before").toString());
                        et_data2.setText(response.body().getList().get(0).get("tachomt_after").toString());
                        tv_data5.setText(response.body().getList().get(0).get("running_time").toString());
                        et_data3.setText(response.body().getList().get(0).get("etc").toString());

                        resultTime= response.body().getList().get(0).get("running_time").toString();
                        calcTime= response.body().getList().get(0).get("calc_time").toString();

                        s_key1= response.body().getList().get(0).get("input_date").toString();
                        s_key2= response.body().getList().get(0).get("gear_cd").toString();
                        s_key3= response.body().getList().get(0).get("work_group").toString();


                        String[] workGroupList= {"A", "B", "C", "D", "S"};
                        for(int i=0; i<workGroupList.length;i++){
                            if(workGroupList[i].equals(selectedPostionKey))  selectedPostion= i;
                        }
                        spinner1onLoad();

                        getworkPeramPlaceCodeData();

                    } catch ( Exception e ) {
                        e.printStackTrace();
                        Toast.makeText(getActivity(), "에러코드 Running 2", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(getActivity(), "onFailure Running 1",Toast.LENGTH_SHORT).show();
            }
        });

    }

    @OnClick(R.id.top_home)
    public void goHome() {
        UtilClass.goHome(getActivity());
    }

    public void async_progress_dialog(String callback){
        ProgressDialog dialog = ProgressDialog.show(getActivity(), "", "Loading...", true, true);
        dialog.setInverseBackgroundForced(false);

        String url = null;
        if(callback.equals("getGearData")){
            url= MainFragment.ipAddress+MainFragment.contextPath+"/rest/Gear/getGearUserData/gubun="+search_gubun+"/param="+et_search.getText()+"/G";
        }else{
            url= MainFragment.ipAddress+MainFragment.contextPath+"/rest/Gear/getGearUserData/gubun="+search_gubun+"/param="+et_search.getText()+"/U";
        }
        aq.progress(dialog).ajax(url, JSONObject.class, this, callback);

    }

    //장비 조회 데이터
    public void getGearData(String url, JSONObject object, AjaxStatus status) {
//        UtilClass.logD(TAG, "object= "+object);
        if( object != null) {
            try {
                arrayList = new ArrayList<>();
                arrayList.clear();
                for(int i=0; i<object.getJSONArray("datas").length();i++){
                    HashMap<String,String> hashMap = new HashMap<>();
                    hashMap.put("data1",object.getJSONArray("datas").getJSONObject(i).get("gear_gnm").toString());
                    hashMap.put("data2",object.getJSONArray("datas").getJSONObject(i).get("gear_nm").toString().trim());
                    hashMap.put("gear_cd",object.getJSONArray("datas").getJSONObject(i).get("gear_cd").toString().trim());
                    arrayList.add(hashMap);
                }
                mAdapter = new BaseAdapter(getActivity(), arrayList);
                listView.setAdapter(mAdapter);
            } catch ( Exception e ) {
                Toast.makeText(getActivity(), "에러코드 Running 1", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }else{
            UtilClass.logD(TAG,"Data is Null");
            Toast.makeText(getActivity(), "데이터가 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    //유저 조회 데이터
    public void getUserData(String url, JSONObject object, AjaxStatus status) {
        if( object != null) {
            try {
                arrayList = new ArrayList<>();
                arrayList.clear();
                for(int i=0; i<object.getJSONArray("datas").length();i++){
                    HashMap<String,String> hashMap = new HashMap<>();
                    hashMap.put("sabun_no",object.getJSONArray("datas").getJSONObject(i).get("user_no").toString());
                    hashMap.put("data1",object.getJSONArray("datas").getJSONObject(i).get("user_sosok").toString());
                    hashMap.put("data2",object.getJSONArray("datas").getJSONObject(i).get("user_nm").toString().trim());
                    arrayList.add(hashMap);
                }
                mAdapter = new BaseAdapter(getActivity(), arrayList);
                listView.setAdapter(mAdapter);
            } catch ( Exception e ) {
                Toast.makeText(getActivity(), "에러코드 Running 2", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }else{
            UtilClass.logD(TAG,"Data is Null");
            Toast.makeText(getActivity(), "데이터가 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    //현장
    public void getworkPeramPlaceCodeData() {
        aq.ajax( url, JSONObject.class, new AjaxCallback<JSONObject>() {
            @Override
            public void callback(String url, JSONObject object, AjaxStatus status ) {
                if( object != null) {
                    try {
                        gubunKeyList= new String[object.getJSONArray("datas").length()];
                        gubunValueList= new String[object.getJSONArray("datas").length()];
                        for(int i=0; i<object.getJSONArray("datas").length();i++){
                            gubunKeyList[i]= object.getJSONArray("datas").getJSONObject(i).get("C001").toString();
                            if(gubunKeyList[i].equals(selectedPostion2Key))  selectedPostion2= i;
                            gubunValueList[i]= object.getJSONArray("datas").getJSONObject(i).get("C002").toString();
                        }
                        KeyValueArrayAdapter adapter = new KeyValueArrayAdapter(getActivity(), android.R.layout.simple_spinner_dropdown_item);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        adapter.setEntryValues(gubunKeyList);
                        adapter.setEntries(gubunValueList);

                        spn2.setPrompt("구분");
                        spn2.setAdapter(adapter);
                        spn2.setSelection(selectedPostion2);
                    } catch ( Exception e ) {

                    }
                }else{
                    UtilClass.logD(TAG,"Data is Null");
                    Toast.makeText(getActivity(),"데이터가 없습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        } );

    }

    //다이얼로그
    private void gearSearchDialog() {
        final View linear = View.inflate(getActivity(), R.layout.search_dialog_list, null);
        mDialog = new Dialog(getActivity());
        if(dialogGubun.equals("G")) mDialog.setTitle("장비 검색");
        if(dialogGubun.equals("U")) mDialog.setTitle("유저 검색");
        search_spi= (Spinner) linear.findViewById(R.id.search_spi);
        et_search= (EditText) linear.findViewById(R.id.et_search);
        listView= (ListView) linear.findViewById(R.id.listView1);

        // Spinner 생성
        int layout=0;
        if(dialogGubun.equals("G")) layout= R.array.gear_list;
        if(dialogGubun.equals("U")) layout= R.array.user_list;
        ArrayAdapter adapter = ArrayAdapter.createFromResource(getActivity(), layout, android.R.layout.simple_spinner_dropdown_item);
//		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        search_spi.setPrompt("선택하세요.");
        search_spi.setAdapter(adapter);
        search_spi.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//				et_search.setText("position : " + position + parent.getItemAtPosition(position));
//				search_spi.getSelectedItem().toString();
                if(dialogGubun.equals("G")){
                    if(position==0){
                        search_gubun="code";
                    }else if(position==1){
                        search_gubun="name";
                    }else{
                        search_gubun="";
                    }
                }else{
                    if(position==0){
                        search_gubun="name";
                    }else if(position==1){
                        search_gubun="code";
                    }else{
                        search_gubun="";
                    }
                }

            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        mDialog.setContentView(linear);

        // Back키 눌렀을 경우 Dialog Cancle 여부 설정
        mDialog.setCancelable(true);

        // Dialog 생성시 배경화면 어둡게 하지 않기
//		mDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        // Dialog 밖을 터치 했을 경우 Dialog 사라지게 하기
        mDialog.setCanceledOnTouchOutside(true);

        btn_search = (Button) linear.findViewById(R.id.button1);
        btn_cancel = (TextView) linear.findViewById(R.id.textButton1);

        btn_search.setOnClickListener(button_click_listener);
        btn_cancel.setOnClickListener(button_click_listener);
        listView.setOnItemClickListener(new ListViewItemClickListener());

        // Dialog Cancle시 Event 받기
        mDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                dismissDialog();
            }
        });

        // Dialog Show시 Event 받기
        mDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {

            }
        });

        // Dialog Dismiss시 Event 받기
        mDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if(mDialog != null && mDialog.isShowing()) {
                    mDialog.dismiss();
                }
            }
        });

        mDialog.show();
    }

    private void dismissDialog() {
        if(mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }
    }

    private View.OnClickListener button_click_listener = new View.OnClickListener() {
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.button1:
                    InputMethodManager imm= (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(et_search.getWindowToken(), 0);

                    if(dialogGubun.equals("G")) async_progress_dialog("getGearData");
                    if(dialogGubun.equals("U")) async_progress_dialog("getUserData");
                    break;

                case R.id.textButton1:
                    dismissDialog();
                    break;
            }
        }
    };

    //검색창 ListView의 item을 클릭했을 때.
    private class ListViewItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            HashMap<String,String> hashMap = new HashMap<>();
            hashMap = arrayList.get(position);
            ArrayList<String> arr = new ArrayList<>();
            for (Iterator iter = hashMap.entrySet().iterator(); iter.hasNext();) {
                Map.Entry entry = (Map.Entry) iter.next();
                arr.add((String) entry.getValue());
            }
            if(dialogGubun.equals("G")){
                selectGearKey= arrayList.get(position).get("gear_cd").toString();
                tv_data2.setText(arrayList.get(position).get("gear_cd").toString().trim());

                tacoData(arrayList.get(position).get("gear_cd").toString().trim());

            }else{
                selectSabunKey= arrayList.get(position).get("sabun_no").toString();
                tv_data3.setText(arrayList.get(position).get("data2").toString().trim());
            }

            dismissDialog();
        }
    }

    //해당 장비 마지막 타코메타 값
    public void tacoData(String gear_cd) {
        final ProgressDialog pDlalog = new ProgressDialog(getActivity());
        UtilClass.showProcessingDialog(pDlalog);

        Call<Datas> call = service.listData("Gear","tacoData", gear_cd);

        call.enqueue(new Callback<Datas>() {
            @Override
            public void onResponse(Call<Datas> call, Response<Datas> response) {
                if (response.isSuccessful()) {
                    UtilClass.logD(TAG, "isSuccessful="+response.body().toString());
                    et_data1.setText(UtilClass.numericZeroCheck(response.body().getList().get(0).get("t_after")));
                }else{
                    Toast.makeText(getActivity(), "작업에 실패하였습니다.",Toast.LENGTH_LONG).show();
                }
                if(pDlalog!=null) pDlalog.dismiss();
            }

            @Override
            public void onFailure(Call<Datas> call, Throwable t) {
                if(pDlalog!=null) pDlalog.dismiss();
                UtilClass.logD(TAG, "onFailure="+call.toString()+", "+t);
                Toast.makeText(getActivity(), "handleResponse TacoData",Toast.LENGTH_LONG).show();
            }
        });
    }

    @OnClick({R.id.textButton1, R.id.top_save})
    public void alertDialogSave(){
        alertDialog("S");

    }

    @OnClick({R.id.textButton2})
    public void alertDialogDelete(){
        if(MainFragment.loginSabun.equals(selectSabunKey)){
            alertDialog("D");
        }else{
            Toast.makeText(getActivity(),"작성자만 가능합니다.", Toast.LENGTH_SHORT).show();
        }
    }

    public void alertDialog(final String gubun){
        final AlertDialog.Builder alertDlg = new AlertDialog.Builder(getActivity());
        alertDlg.setTitle("알림");
        if(gubun.equals("S")){
            alertDlg.setMessage("작성하시겠습니까?");
        }else{
            alertDlg.setMessage("삭제하시겠습니까?");
        }
        // '예' 버튼이 클릭되면
        alertDlg.setPositiveButton("예", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(gubun.equals("S")){
                    postData();
                }else{
                    deleteData();
                }
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

    //삭제
    public void deleteData() {
        final ProgressDialog pDlalog = new ProgressDialog(getActivity());
        UtilClass.showProcessingDialog(pDlalog);

        Call<Datas> call = service.deleteData("Gear","runningTimeDelete", s_key1, s_key2, s_key3);

        call.enqueue(new Callback<Datas>() {
            @Override
            public void onResponse(Call<Datas> call, Response<Datas> response) {
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
                Toast.makeText(getActivity(), "handleResponse NoticeBoard",Toast.LENGTH_LONG).show();
            }
        });
    }

    //작성,수정
    public void postData() {
        String select_gear = tv_data2.getText().toString();
        String result = tv_data5.getText().toString();

        if (select_gear.equals("") || select_gear.length()==0) {
            Toast.makeText(getActivity(), "장비를 선택하세요.",Toast.LENGTH_LONG).show();
            return;
        }
        if (result.equals("") || result.length()==0) {
            Toast.makeText(getActivity(), "가동시간을 계산하세요.",Toast.LENGTH_LONG).show();
            return;
        }

        WebServiceTask wst= null;
        if(mode.equals("insert")){
            wst = new WebServiceTask(WebServiceTask.POST_TASK, getActivity(), "Loading...");
        }else{
            wst = new WebServiceTask(WebServiceTask.PUT_TASK, getActivity(), "Loading...");
            wst.addNameValuePair("s_key1",s_key1);
            wst.addNameValuePair("s_key2",s_key2);
            wst.addNameValuePair("s_key3",s_key3);
        }

        wst.addNameValuePair("writer_sabun",MainFragment.loginSabun);
        wst.addNameValuePair("writer_name",MainFragment.loginName);
        wst.addNameValuePair("running_date",tv_data1.getText().toString());
        wst.addNameValuePair("gear_cd",selectGearKey);
        wst.addNameValuePair("work_group",groupCode);
        wst.addNameValuePair("work_user",selectSabunKey);
        wst.addNameValuePair("work_place",selectGubunKey);
        wst.addNameValuePair("running_time",resultTime);
        wst.addNameValuePair("calc_time",calcTime);
        wst.addNameValuePair("tachomt_before",et_data1.getText().toString());
        wst.addNameValuePair("tachomt_after",et_data2.getText().toString());
        wst.addNameValuePair("running_etc",et_data3.getText().toString());

        // the passed String is the URL we will POST to
        if(mode.equals("insert")){
            wst.execute(new String[] { INSERT_URL });
        }else{
            wst.execute(new String[] { MODIFY_URL });
        }



    }

    //작성 완료
    public void handleResponse(Response<Datas> response) {
        try {
            String status= response.body().getStatus();
            if(status.equals("success")) getActivity().onBackPressed();

            if(status.equals("successOnPush")){
                String pushSend= response.body().getList().get(0).get("pushSend").toString();

                if(pushSend.equals("success")){
                    Toast.makeText(getActivity(), "푸시데이터가 전송 되었습니다.",Toast.LENGTH_LONG).show();
                }else if(pushSend.equals("empty")){
                    Toast.makeText(getActivity(), "푸시데이터를 받는 사용자가 없습니다.",Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(getActivity(), "푸시 전송이 실패하였습니다.",Toast.LENGTH_LONG).show();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "작업에 실패하였습니다.", Toast.LENGTH_SHORT).show();
        }

    }

    //작성 완료
    public void handleResponse(String response) {
        UtilClass.logD(TAG,"response="+response);

        try {
            JSONObject jso = new JSONObject(response);
            String status= jso.get("status").toString();

            if(status.equals("success")){
                getActivity().onBackPressed();

            }else if(status.equals("check")){
                Toast.makeText(getActivity(), "중복된 데이터가 존재합니다.",Toast.LENGTH_LONG).show();

            }else{
                Toast.makeText(getActivity(), "저장에 실패하였습니다.",Toast.LENGTH_LONG).show();
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "handleResponse Running",Toast.LENGTH_LONG).show();
        }

    }

    private void hideKeyboard() {
        InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(
                getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    private class WebServiceTask extends AsyncTask<String, Integer, String> {

        public static final int POST_TASK = 1;
        public static final int GET_TASK = 2;
        public static final int PUT_TASK = 3;
        public static final int DELETE_TASK = 4;

        private static final String TAG = "WebServiceTask";

        // connection timeout, in milliseconds (waiting to connect)
        private static final int CONN_TIMEOUT = 3000;

        // socket timeout, in milliseconds (waiting for data)
        private static final int SOCKET_TIMEOUT = 5000;

        private int taskType = GET_TASK;
        private Context mContext = null;
        private String processMessage = "Processing...";

        private ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();

        private ProgressDialog pDlg = null;

        public WebServiceTask(int taskType, Context mContext, String processMessage) {

            this.taskType = taskType;
            this.mContext = mContext;
            this.processMessage = processMessage;
        }

        public void addNameValuePair(String name, String value) {
            params.add(new BasicNameValuePair(name, value));
        }

        private void showProgressDialog() {
            pDlg = new ProgressDialog(mContext);
            pDlg.setMessage(processMessage);
            pDlg.setProgressDrawable(mContext.getWallpaper());
            pDlg.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            pDlg.setCancelable(false);
            pDlg.show();
        }

        @Override
        protected void onPreExecute() {
            hideKeyboard();
            showProgressDialog();
        }

        protected String doInBackground(String... urls) {
            String url = urls[0];
            String result = "";
            HttpResponse response = doResponse(url);

            if (response == null) {
                return result;
            } else {
                try {
                    result = inputStreamToString(response.getEntity().getContent());

                } catch (IllegalStateException e) {
                    Log.e(TAG, e.getLocalizedMessage(), e);
                } catch (IOException e) {
                    Log.e(TAG, e.getLocalizedMessage(), e);
                }
            }
            return result;
        }

        @Override
        protected void onPostExecute(String response) {
            handleResponse(response);
            pDlg.dismiss();
        }

        // Establish connection and socket (data retrieval) timeouts
        private HttpParams getHttpParams() {
            HttpParams htpp = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(htpp, CONN_TIMEOUT);
            HttpConnectionParams.setSoTimeout(htpp, SOCKET_TIMEOUT);

            return htpp;
        }

        @NotThreadSafe
        class HttpDeleteWithBody extends HttpEntityEnclosingRequestBase {
            public static final String METHOD_NAME = "DELETE";

            public String getMethod() {
                return METHOD_NAME;
            }
            public HttpDeleteWithBody(final String uri) {
                super();
                setURI(URI.create(uri));
            }
            public HttpDeleteWithBody(final URI uri) {
                super();
                setURI(uri);
            }
            public HttpDeleteWithBody() {
                super();
            }
        }

        private HttpResponse doResponse(String url) {
            HttpClient httpclient = new DefaultHttpClient(getHttpParams());
            HttpResponse response = null;

            try {
                switch (taskType) {

                    case POST_TASK:
                        HttpPost httppost = new HttpPost(url);
                        // Add parameters
                        httppost.setEntity(new UrlEncodedFormEntity(params,"UTF-8"));
                        response= httpclient.execute(httppost);
                        break;
                    case GET_TASK:
                        HttpGet httpget = new HttpGet(url);
                        response= httpclient.execute(httpget);
                        break;
                    case PUT_TASK:
                        HttpPut httpput = new HttpPut(url);
                        // Add parameters
                        httpput.setEntity(new UrlEncodedFormEntity(params,"UTF-8"));
                        response= httpclient.execute(httpput);
                        break;
                    case DELETE_TASK:
                        HttpDeleteWithBody httpdel = new HttpDeleteWithBody(url);
                        // Add parameters
                        httpdel.setEntity(new UrlEncodedFormEntity(params,"UTF-8"));
                        response= httpclient.execute(httpdel);
                        break;
                }
            } catch (Exception e) {
                Log.e(TAG, e.getLocalizedMessage(), e);
            }

            return response;
        }

        private String inputStreamToString(InputStream is) {

            String line = "";
            StringBuilder total = new StringBuilder();

            // Wrap a BufferedReader around the InputStream
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));

            try {
                // Read response until the end
                while ((line = rd.readLine()) != null) {
                    total.append(line);
                }
            } catch (IOException e) {
                Log.e(TAG, e.getLocalizedMessage(), e);
            }

            // Return full string
            return total.toString();
        }

    }

    @OnClick(R.id.button1)
    public void getGearSearch() {
        dialogGubun= "G";
        gearSearchDialog();
    }

    @OnClick(R.id.button2)
    public void getUserSearch() {
        dialogGubun= "U";
        gearSearchDialog();
    }

    //날짜설정
    @OnClick(R.id.date_button)
    public void getDateDialog() {
        getDialog("D");
    }


    public void getDialog(String gubun) {
        int year, month, day, hour, minute;

        GregorianCalendar calendar = new GregorianCalendar();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day= calendar.get(Calendar.DAY_OF_MONTH);
        hour = calendar.get(Calendar.HOUR_OF_DAY);
        minute = calendar.get(Calendar.MINUTE);

        DatePickerDialog dialog;
        dialog = new DatePickerDialog(getActivity(), date_listener, year, month, day);

        dialog.show();
    }

    private DatePickerDialog.OnDateSetListener date_listener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            Toast.makeText(getActivity(), year + "년" + (monthOfYear+1) + "월" + dayOfMonth +"일", Toast.LENGTH_SHORT).show();
            String month= UtilClass.addZero(monthOfYear+1);
            String day= UtilClass.addZero(dayOfMonth);

            tv_data1.setText(year+"."+month+"."+day);
        }
    };

}

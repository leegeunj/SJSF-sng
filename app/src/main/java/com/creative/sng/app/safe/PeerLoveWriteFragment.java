package com.creative.sng.app.safe;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
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

import org.json.JSONObject;

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

public class PeerLoveWriteFragment extends Fragment {
    private static final String TAG = "PeerLoveWriteFragment";
    private RetrofitService service;

    private String mode="";
    private String idx="";
    private String dataSabun;

    @BindView(R.id.top_title) TextView textTitle;
    @BindView(R.id.textView1) TextView tv_userName;
    @BindView(R.id.textView2) TextView tv_userSosok;
    @BindView(R.id.textView3) TextView tv_date;
    @BindView(R.id.textView4) TextView tv_writerName;

    @BindView(R.id.editText1) EditText et_memo;
    @BindView(R.id.spinner1) Spinner spn;
    private String selectedPostionKey;  //스피너 선택된 키값
    private int selectedPostion=0;    //스피너 선택된 Row 값

    private String url = MainFragment.ipAddress+MainFragment.contextPath+"/rest/Safe/peerLoveCodeList";
    private String selectGubunKey="";
    private String[] gubunKeyList;
    private String[] gubunValueList;

    //수정 데이터
    private String modifyDate;

    //검색 다이얼로그
    private Dialog mDialog = null;
    private Spinner search_spi;
    private String search_gubun;	//검색 구분
    private EditText et_search;
    private ListView listView;
    private ArrayList<HashMap<String,String>> arrayList;
    private BaseAdapter mAdapter;
    private Button btn_search;
    private TextView btn_cancel;
    private String selectSabunKey="";

    private AQuery aq;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.friends_write, container, false);
        ButterKnife.bind(this, view);
        service= RetrofitService.rest_api.create(RetrofitService.class);
        aq = new AQuery( getActivity() );

        mode= getArguments().getString("mode");
        if(mode==null) mode="";

        if(mode.equals("insert")){
            dataSabun= MainFragment.loginSabun;
            view.findViewById(R.id.linear2).setVisibility(View.GONE);
            textTitle.setText("동료사랑카드 작성");
            tv_date.setText(UtilClass.getCurrentDate(1, "."));
            tv_writerName.setText(MainFragment.loginName);
            getPeerLoveCodeData();
        }else{
            textTitle.setText("동료사랑카드 수정");
            idx= getArguments().getString("peer_key");
            async_progress_dialog();
        }
        view.findViewById(R.id.top_save).setVisibility(View.VISIBLE);

        spn.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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

        return view;
    }//onCreateView

    public void async_progress_dialog(){
        RetrofitService service = RetrofitService.rest_api.create(RetrofitService.class);

        final ProgressDialog pDlalog = new ProgressDialog(getActivity());
        UtilClass.showProcessingDialog(pDlalog);

        Call<Datas> call = service.listData("Safe","peerLoveCardDetail", idx);
        call.enqueue(new Callback<Datas>() {
            @Override
            public void onResponse(Call<Datas> call, Response<Datas> response) {
                UtilClass.logD(TAG, "response="+response);
                if (response.isSuccessful()) {
                    UtilClass.logD(TAG, "isSuccessful="+response.body().toString());
                    String status= response.body().getStatus();
                    try {
                        dataSabun= response.body().getList().get(0).get("input_id");
                        if(MainFragment.loginSabun.equals(dataSabun)){
                        }else{
                            et_memo.setFocusableInTouchMode(false);
                            getActivity().findViewById(R.id.linear1).setVisibility(View.GONE);
                            getActivity().findViewById(R.id.linear2).setVisibility(View.GONE);
                        }
                        selectedPostionKey= response.body().getList().get(0).get("unact_cd");
                        selectSabunKey= response.body().getList().get(0).get("peer_id");

                        tv_date.setText(response.body().getList().get(0).get("peer_date"));
                        tv_userName.setText(response.body().getList().get(0).get("peer_nm"));
                        tv_userSosok.setText(response.body().getList().get(0).get("peer_sosok"));
                        et_memo.setText(response.body().getList().get(0).get("peer_etc"));
                        tv_writerName.setText(response.body().getList().get(0).get("input_nm"));

                        modifyDate= tv_date.getText().toString();

                        getPeerLoveCodeData();

                    } catch ( Exception e ) {
                        e.printStackTrace();
                        Toast.makeText(getActivity(), "에러코드 Peer 2", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(getActivity(), "onFailure Peer",Toast.LENGTH_SHORT).show();
            }
        });

    }


    public void getUserData(){
        RetrofitService service = RetrofitService.rest_api.create(RetrofitService.class);

        final ProgressDialog pDlalog = new ProgressDialog(getActivity());
        UtilClass.showProcessingDialog(pDlalog);

        Call<Datas> call = service.listData("Login","searchUserData", "gubun="+search_gubun, "param="+et_search.getText().toString());
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
                            HashMap<String,String> hashMap = new HashMap<>();
                            hashMap.put("data1",response.body().getList().get(i).get("user_no"));
                            hashMap.put("data2",response.body().getList().get(i).get("user_nm"));
                            hashMap.put("user_sosok",response.body().getList().get(i).get("user_sosok"));
                            arrayList.add(hashMap);
                        }

                        mAdapter = new BaseAdapter(getActivity(), arrayList);
                        listView.setAdapter(mAdapter);
                    } catch ( Exception e ) {
                        e.printStackTrace();
                        Toast.makeText(getActivity(), "에러코드 Peer 1", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(getActivity(), "onFailure Peer",Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void getPeerLoveCodeData() {
        aq.ajax( url, JSONObject.class, new AjaxCallback<JSONObject>() {
            @Override
            public void callback(String url, JSONObject object, AjaxStatus status ) {
                if( object != null) {
                    try {
                        gubunKeyList= new String[object.getJSONArray("datas").length()];
                        gubunValueList= new String[object.getJSONArray("datas").length()];
                        for(int i=0; i<object.getJSONArray("datas").length();i++){
                            gubunKeyList[i]= object.getJSONArray("datas").getJSONObject(i).get("unact_cd").toString();
                            if(gubunKeyList[i].equals(selectedPostionKey))  selectedPostion= i;
                            gubunValueList[i]= object.getJSONArray("datas").getJSONObject(i).get("unact_nm").toString();
                        }
                        KeyValueArrayAdapter adapter = new KeyValueArrayAdapter(getActivity(), android.R.layout.simple_spinner_dropdown_item);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        adapter.setEntryValues(gubunKeyList);
                        adapter.setEntries(gubunValueList);

                        spn.setPrompt("구분");
                        spn.setAdapter(adapter);
                        spn.setSelection(selectedPostion);
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
    private void userSearchDialog() {
        final View linear = View.inflate(getActivity(), R.layout.search_dialog_list, null);
        mDialog = new Dialog(getActivity());
        mDialog.setTitle("직원 검색");
        search_spi= (Spinner) linear.findViewById(R.id.search_spi);
        et_search= (EditText) linear.findViewById(R.id.et_search);
        listView= (ListView) linear.findViewById(R.id.listView1);

        // Spinner 생성
        ArrayAdapter adapter = ArrayAdapter.createFromResource(getActivity(), R.array.user_list, android.R.layout.simple_spinner_dropdown_item);
//		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        search_spi.setPrompt("선택하세요.");
        search_spi.setAdapter(adapter);
        search_spi.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//				et_search.setText("position : " + position + parent.getItemAtPosition(position));
//				search_spi.getSelectedItem().toString();
                if(position==0){
                    search_gubun="user_nm";
                }else if(position==1){
                    search_gubun="sabun_no";
                }else{
                    search_gubun="";
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

                    getUserData();
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
//            UtilClass.logD(TAG, "?="+arr);
             tv_userSosok.setText(arrayList.get(position).get("user_sosok").toString().trim());
            selectSabunKey= arrayList.get(position).get("data1").toString();
            tv_userName.setText(arrayList.get(position).get("data2").toString().trim());
            dismissDialog();
        }
    }

    @OnClick({R.id.textButton1, R.id.top_save})
    public void alertDialogSave(){
        if(MainFragment.loginSabun.equals(dataSabun)){
            alertDialog("S");
        }else{
            Toast.makeText(getActivity(),"작성자만 가능합니다.", Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick({R.id.textButton2})
    public void alertDialogDelete(){
        if(MainFragment.loginSabun.equals(dataSabun)){
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

        Call<Datas> call = service.deleteData("Safe","peerLoveCardDelete", idx);

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
                Toast.makeText(getActivity(), "handleResponse Peer",Toast.LENGTH_LONG).show();
            }
        });
    }

    //작성,수정
    public void postData() {
        if (tv_userName.equals("") || tv_userName.length()==0) {
            Toast.makeText(getActivity(), "받는사람을 입력하세요.",Toast.LENGTH_LONG).show();
            return;
        }
        if (et_memo.equals("") || et_memo.length()==0) {
            Toast.makeText(getActivity(), "빈칸을 채워주세요.",Toast.LENGTH_LONG).show();
            return;
        }

        Map<String, Object> map = new HashMap();
        map.put("writer_sabun", MainFragment.loginSabun.trim());
        map.put("writer_name", MainFragment.loginName.trim());
        map.put("user_name",tv_userName.getText());
        map.put("peer_date",tv_date.getText());
        map.put("unact_cd",selectGubunKey.trim());
        map.put("peer_etc",et_memo.getText());
        map.put("peer_per",selectSabunKey.trim());

        final ProgressDialog pDlalog = new ProgressDialog(getActivity());
        UtilClass.showProcessingDialog(pDlalog);

        Call<Datas> call= null;
        if(mode.equals("insert")){
            call = service.insertData("Safe","peerLoveCardWrite", map);
        }else{
            call = service.updateData("Safe","peerLoveCardModify/"+idx, map);
            map.put("idx",idx);
        }

        call.enqueue(new Callback<Datas>() {
            @Override
            public void onResponse(Call<Datas> call, Response<Datas> response) {
                UtilClass.logD(TAG, "response="+response);
                if (response.isSuccessful()) {
                    UtilClass.logD(TAG, "isSuccessful="+response.body().toString());
                    try {
                        handleResponse(response);
                    } catch ( Exception e ) {
                        e.printStackTrace();
                        Toast.makeText(getActivity(), "에러코드 Peer 1", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(getActivity(), "onFailure Peer",Toast.LENGTH_SHORT).show();
            }
        });

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
                    String pendingPath= response.body().getList().get(0).get("pendingPath").toString();
                    String pendingPathKey= response.body().getList().get(0).get("resultKey").toString();
                    MainFragment.pendingPath= pendingPath;
                    MainFragment.pendingPathKey= pendingPathKey;
                }else if(pushSend.equals("empty")){
                    Toast.makeText(getActivity(), "푸시데이터를 받는 사용자가 없습니다.",Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(getActivity(), "푸시 전송이 실패하였습니다.",Toast.LENGTH_LONG).show();
                }
                getActivity().onBackPressed();
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "작업에 실패하였습니다.", Toast.LENGTH_SHORT).show();
        }

    }

    @OnClick(R.id.button1)
    public void getUserSearch() {
        userSearchDialog();
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
        if(mode.equals("insert")){
            dialog = new DatePickerDialog(getActivity(), date_listener, year, month, day);

        }else{
            String[] dateArr = modifyDate.split("[.]");
            dialog = new DatePickerDialog(getActivity(), date_listener, Integer.parseInt(dateArr[0]),
                    Integer.parseInt(dateArr[1])-1, Integer.parseInt(dateArr[2]));

        }
        dialog.show();
    }

    private DatePickerDialog.OnDateSetListener date_listener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            Toast.makeText(getActivity(), year + "년" + (monthOfYear+1) + "월" + dayOfMonth +"일", Toast.LENGTH_SHORT).show();
            String month= UtilClass.addZero(monthOfYear+1);
            String day= UtilClass.addZero(dayOfMonth);

            tv_date.setText(year+"."+month+"."+day);
        }
    };

    @OnClick(R.id.top_home)
    public void goHome() {
        UtilClass.goHome(getActivity());
    }

}

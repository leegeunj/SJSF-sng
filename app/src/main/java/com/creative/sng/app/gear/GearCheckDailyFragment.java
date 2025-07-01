package com.creative.sng.app.gear;


import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.creative.sng.app.R;
import com.creative.sng.app.adaptor.GearCheckDailyAdapter;
import com.creative.sng.app.menu.MainActivity;
import com.creative.sng.app.menu.MainFragment;
import com.creative.sng.app.retrofit.Datas;
import com.creative.sng.app.retrofit.RetrofitService;
import com.creative.sng.app.safe.ReciprocityWriteFragment;
import com.creative.sng.app.util.UtilClass;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.zip.Inflater;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GearCheckDailyFragment extends Fragment {
    private static final String TAG = "GearCheckDailyFragment";
    private RetrofitService service;
    private String title;

    private ArrayList<HashMap<String,Object>> arrayList;
    private GearCheckDailyAdapter mAdapter;

    @BindView(R.id.listView1) ListView listView;
    @BindView(R.id.top_title) TextView textTitle;
    @BindView(R.id.textButton1) TextView tv_button1;
    @BindView(R.id.textButton2) TextView tv_button2;

    private boolean isSdate=false;
    private String authLev="0";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.gear_check_daily_list, container, false);
        ButterKnife.bind(this,view);
        service = RetrofitService.rest_api.create(RetrofitService.class);
        title= getArguments().getString("title");
        textTitle.setText(title);

        tv_button1.setText(UtilClass.getCurrentDate(4, "."));
        tv_button2.setText(UtilClass.getCurrentDate(1, "."));
        Log.d("loginSabun: ",MainFragment.loginSabun);
        equipApprovalUser();
        async_progress_dialog();

        listView.setOnItemClickListener(new ListViewItemClickListener());

        return view;
    }

    private void equipApprovalUser() {
        Call<Datas> call = service.listData("Gear","equipApprovalUser","loginSabun="+MainFragment.loginSabun);
        call.enqueue(new Callback<Datas>() {
            @Override
            public void onResponse(Call<Datas> call, Response<Datas> response) {
                UtilClass.logD(TAG, "response="+response);
                if (response.isSuccessful()) {
                    UtilClass.logD(TAG, "isSuccessful="+response.body().toString());
                    String status= response.body().getStatus();
                    try {
                        if(response.body().getCount()!=0){
                            authLev = response.body().getList().get(0).get("app_level");
                        }
                    } catch ( Exception e ) {
                        e.printStackTrace();
                        Toast.makeText(getActivity(), "에러코드 Running 1", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(getActivity(), "response isFailed", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<Datas> call, Throwable t) {
                UtilClass.logD(TAG, "onFailure="+call.toString()+", "+t);
                Toast.makeText(getActivity(), "onFailure Running",Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void async_progress_dialog() {
        final ProgressDialog pDlalog = new ProgressDialog(getActivity());
        UtilClass.showProcessingDialog(pDlalog);

        Call<Datas> call = service.listDataQ("Gear","gearCheckDailyList",tv_button1.getText().toString(), tv_button2.getText().toString(), MainFragment.loginSabun);
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
                            hashMap.put("key",response.body().getList().get(i).get("gear_key"));
                            hashMap.put("data1",response.body().getList().get(i).get("check_date"));
                            hashMap.put("data2",response.body().getList().get(i).get("check_person_nm"));
                            hashMap.put("data3",response.body().getList().get(i).get("gear_nm"));
                            hashMap.put("data4",response.body().getList().get(i).get("check_state"));
                            hashMap.put("data5",response.body().getList().get(i).get("no_content"));
                            hashMap.put("emp_cd1",response.body().getList().get(i).get("emp_cd1"));
                            hashMap.put("emp_nm1",response.body().getList().get(i).get("emp_nm1"));
                            hashMap.put("emp_cd2",response.body().getList().get(i).get("emp_cd2"));
                            hashMap.put("lvl1_app_tp",response.body().getList().get(i).get("lvl1_app_tp"));
                            hashMap.put("lvl2_app_tp",response.body().getList().get(i).get("lvl2_app_tp"));

                            arrayList.add(hashMap);
                        }

                        mAdapter = new GearCheckDailyAdapter(getActivity(), arrayList, "GearCheckDaily");
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

    private class ListViewItemClickListener implements AdapterView.OnItemClickListener{
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if(authLev=="0"){
                Toast.makeText(getActivity(), "권한이 없습니다.",Toast.LENGTH_SHORT).show();
            }else {
                if(arrayList.get(position).get("lvl1_app_tp").toString().equals("3")){
                    Toast.makeText(getActivity(), "1차반려 처리된 항목입니다.",Toast.LENGTH_SHORT).show();
                } else {
                    Fragment frag = new GearCheckDailyWriteFragment();
                    Bundle bundle = new Bundle();

                    FragmentManager fm = getFragmentManager();
                    FragmentTransaction fragmentTransaction = fm.beginTransaction();
//            fragmentTransaction.hide(frag);
                    fragmentTransaction.add(R.id.fragmentReplace, frag);

                    bundle.putString("title", "점검일지승인상세");
                    String key = arrayList.get(position).get("key").toString();
                    int idx = arrayList.get(position).get("data3").toString().indexOf(" ");
                    String gear_cd = arrayList.get(position).get("data3").toString().substring(0, idx);
                    String input_date = arrayList.get(position).get("data1").toString();
                    String check_person = arrayList.get(position).get("data2").toString();
/*            Log.d(TAG,gear_cd);
            Log.d(TAG,input_date);
            Log.d(TAG,key);
            Log.d(TAG,check_person);
            Log.d(TAG,arrayList.get(position).get("emp_cd1").toString()); */
                    bundle.putString("gear_key", key);
                    bundle.putString("gear_cd", gear_cd);
                    bundle.putString("input_date", input_date);
                    bundle.putString("check_person", check_person);
                    bundle.putString("emp_cd1", arrayList.get(position).get("emp_cd1").toString());
                    bundle.putString("emp_nm1", arrayList.get(position).get("emp_nm1").toString());
                    bundle.putString("emp_cd2", arrayList.get(position).get("emp_cd2").toString());
                    bundle.putString("lvl1_app_tp", arrayList.get(position).get("lvl1_app_tp").toString());
                    bundle.putString("lvl2_app_tp", arrayList.get(position).get("lvl2_app_tp").toString());
                    bundle.putString("mode", "update");
                    bundle.putString("authLev", authLev);

                    frag.setArguments(bundle);
                    fragmentTransaction.addToBackStack("점검일지승인상세");
//            fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                    fragmentTransaction.commit();
                }
            }
        }
    }


    //해당 검색값 데이터 조회
    @OnClick(R.id.imageView1)
    public void onSearchColumn() {
        async_progress_dialog();

        //검색하면 키보드 내리기
//        InputMethodManager imm= (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
//        imm.hideSoftInputFromWindow(et_search.getWindowToken(), 0);
//
//        if(et_search.getText().toString().length()==0){
//            Toast.makeText(getActivity(), "장비명을 입력하세요.", Toast.LENGTH_SHORT).show();
//        }else{
//            async_progress_dialog();
//        }

    }

    //날짜설정
    @OnClick(R.id.textButton1)
    public void getDateDialog() {
        getDialog("SD");
        isSdate=true;
    }
    @OnClick(R.id.textButton2)
    public void getDateDialog2() {
        getDialog("ED");
        isSdate=false;
    }

    public void getDialog(String gubun) {
        TextView textView;
        if(gubun.equals("SD")){
            textView= tv_button1;
        }else{
            textView= tv_button2;
        }
        DatePickerDialog dialog = new DatePickerDialog(getActivity(), date_listener, UtilClass.dateAndTimeChoiceList(textView, "D").get(0)
                , UtilClass.dateAndTimeChoiceList(textView, "D").get(1)-1, UtilClass.dateAndTimeChoiceList(textView, "D").get(2));
        dialog.show();

    }


    private DatePickerDialog.OnDateSetListener date_listener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            String month= UtilClass.addZero(monthOfYear+1);
            String day= UtilClass.addZero(dayOfMonth);
            String date= year+"."+month+"."+day;

            if(isSdate){
                tv_button1.setText(date);
            }else{
                tv_button2.setText(date);
            }
            async_progress_dialog();

        }
    };
}

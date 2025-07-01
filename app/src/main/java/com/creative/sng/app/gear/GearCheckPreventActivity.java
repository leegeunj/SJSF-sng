package com.creative.sng.app.gear;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.creative.sng.app.R;
import com.creative.sng.app.adaptor.GearPreventAdapter;
import com.creative.sng.app.menu.MainFragment;
import com.creative.sng.app.retrofit.Datas;
import com.creative.sng.app.retrofit.RetrofitService;
import com.creative.sng.app.util.BackPressCloseSystem;
import com.creative.sng.app.util.UtilClass;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GearCheckPreventActivity extends AppCompatActivity {
    private static final String TAG = "GearCheckPreventActivity";
    private static final String FLTB = " - 필터 : ";
    private static final String OILB = " - 오일 : ";
    private static final String RTS = " hour ";

    private RetrofitService service;
    private String title;
    private BackPressCloseSystem backPressCloseSystem;

    private ArrayList<HashMap<String,Object>> arrayList;
    private GearPreventAdapter mAdapter;
    @BindView(R.id.listView1)   ListView listView;
    @BindView(R.id.top_title)   TextView textTitle;
    @BindView(R.id.textButton1) TextView tv_button1;
    @BindView(R.id.textButton2) TextView tv_button2;

    private String selectGearKey;

    private boolean isSdate=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gear_check_prevent);
        ButterKnife.bind(this);
        service = RetrofitService.rest_api.create(RetrofitService.class);
        textTitle.setText("예방점검조회");
        backPressCloseSystem = new BackPressCloseSystem(this);
        tv_button1.setText(UtilClass.getCurrentDate(2, "."));
        tv_button2.setText(UtilClass.getCurrentDate(1, "."));
        selectGearKey = getIntent().getStringExtra("selectGearKey");
        Log.d("loginSabun: ", MainFragment.loginSabun);

        async_progress_dialog();
    }

    private void async_progress_dialog() {
        final ProgressDialog pDlalog = new ProgressDialog(this);
        UtilClass.showProcessingDialog(pDlalog);

        Call<Datas> call = service.listDataQ("Gear","gearCheckPreventList",tv_button1.getText().toString(), tv_button2.getText().toString(), selectGearKey);
        call.enqueue(new Callback<Datas>() {
            @Override
            public void onResponse(Call<Datas> call, Response<Datas> response) {
                UtilClass.logD(TAG, "response="+response);
                if (response.isSuccessful()) {
                    UtilClass.logD(TAG, "isSuccessful="+response.body().toString());
                    String status= response.body().getStatus();
                    try {
                        if(response.body().getCount()==0){
                            Toast.makeText(getApplicationContext(), "데이터가 없습니다.", Toast.LENGTH_SHORT).show();
                        }
                        arrayList = new ArrayList<>();
                        arrayList.clear();
                        for(int i=0; i<response.body().getList().size();i++){
                            UtilClass.dataNullCheckZero(response.body().getList().get(i));

                            HashMap<String,Object> hashMap = new HashMap<>();
                            hashMap.put("data1",response.body().getList().get(i).get("pre_nm"));
                            hashMap.put("data2",response.body().getList().get(i).get("pre_date"));
                            hashMap.put("data3",response.body().getList().get(i).get("input_nm"));
                            hashMap.put("data4",response.body().getList().get(i).get("filter_date")+" / "+response.body().getList().get(i).get("filter_time"));
                            hashMap.put("data5",response.body().getList().get(i).get("pre_nm"));
//                            hashMap.put("data4",FLTB+response.body().getList().get(i).get("filter_date")+" / "+response.body().getList().get(i).get("filter_time")+RTS);
//                            hashMap.put("data5",OILB+response.body().getList().get(i).get("oil_date")+" / "+response.body().getList().get(i).get("oil_time")+RTS);
                            hashMap.put("data6",response.body().getList().get(i).get("bigo"));

                            arrayList.add(hashMap);
                        }

                        mAdapter = new GearPreventAdapter(getApplicationContext(), arrayList, "GearCheckPreventActivity");
                        listView.setAdapter(mAdapter);
                    } catch ( Exception e ) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), "에러코드 Running 1", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(getApplicationContext(), "onFailure Running",Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void getDialog(String gubun) {
        TextView textView;
        if(gubun.equals("SD")){
            textView= tv_button1;
        }else{
            textView= tv_button2;
        }
        DatePickerDialog dialog = new DatePickerDialog(GearCheckPreventActivity.this, date_listener, UtilClass.dateAndTimeChoiceList(textView, "D").get(0)
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

    @OnClick(R.id.imageView1)
    public void searchDate(){
        async_progress_dialog();
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @OnClick(R.id.top_home)
    public void goHome(){
        finish();
    }
}

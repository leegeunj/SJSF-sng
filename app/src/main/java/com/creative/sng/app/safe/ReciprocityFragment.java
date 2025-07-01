package com.creative.sng.app.safe;

import android.app.DatePickerDialog;
import android.widget.DatePicker;
import android.app.ProgressDialog;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxStatus;
import com.creative.sng.app.R;
import com.creative.sng.app.adaptor.PeerAdapter;
import com.creative.sng.app.menu.MainFragment;
import com.creative.sng.app.util.UtilClass;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ReciprocityFragment extends Fragment {
    private static final String TAG = "ReciprocityFragment";
    private String url = MainFragment.ipAddress+ MainFragment.contextPath+"/rest/Safe/reciprocityList";

    private ArrayList<HashMap<String,Object>> penaltyArray;
    private PeerAdapter mAdapter;
    @BindView(R.id.listView1) ListView listView;
    @BindView(R.id.top_title) TextView textTitle;
    @BindView(R.id.textButton1) TextView tv_button1;
    @BindView(R.id.textButton2) TextView tv_button2;

    private AQuery aq;
    private boolean isSdate=false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.free_list, container, false);
        ButterKnife.bind(this, view);
        aq = new AQuery(getActivity());

        textTitle.setText(getArguments().getString("title"));
        view.findViewById(R.id.top_write).setVisibility(View.VISIBLE);

        tv_button1.setText(UtilClass.getCurrentDate(2, "."));
        tv_button2.setText(UtilClass.getCurrentDate(1, "."));


        async_progress_dialog("getBoardInfo");

        listView.setOnItemClickListener(new ListViewItemClickListener());

        return view;
    }//onCreateView

    public void async_progress_dialog(String callback){
        ProgressDialog dialog = ProgressDialog.show(getActivity(), "", "Loading...", true, false);
        dialog.setInverseBackgroundForced(false);
        String fullUrl = url+"/"+tv_button1.getText().toString()+"/"+tv_button2.getText().toString();
        aq.progress(dialog).ajax(fullUrl, JSONObject.class, this, callback);
    }

    public void getBoardInfo(String url, JSONObject object, AjaxStatus status) throws JSONException {
//        Log.d(TAG, "object= "+object);

        if(!object.get("count").equals(0)) {
            try {
                penaltyArray = new ArrayList<>();
                penaltyArray.clear();
                for(int i=0; i<object.getJSONArray("datas").length();i++){
                    HashMap<String,Object> hashMap = new HashMap<>();
                    hashMap.put("key",object.getJSONArray("datas").getJSONObject(i).get("peer_key").toString());
                    hashMap.put("data1",object.getJSONArray("datas").getJSONObject(i).get("peer_date").toString());
                    hashMap.put("data2",object.getJSONArray("datas").getJSONObject(i).get("peer_nm").toString().trim());
                    hashMap.put("data3",object.getJSONArray("datas").getJSONObject(i).get("peer_etc").toString());
                    hashMap.put("data4",object.getJSONArray("datas").getJSONObject(i).get("input_nm").toString().trim());
                    penaltyArray.add(hashMap);
                }

                mAdapter = new PeerAdapter(getActivity(), penaltyArray);
                listView.setAdapter(mAdapter);
            } catch ( Exception e ) {
                e.printStackTrace();
                Toast.makeText(getActivity(), "에러코드 Peer 1", Toast.LENGTH_SHORT).show();
            }
        }else{
            Log.d(TAG,"Data is Null");
            Toast.makeText(getActivity(), "데이터가 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.top_home)
    public void goHome() {
        UtilClass.goHome(getActivity());
    }

    @OnClick(R.id.top_write)
    public void getWriteBoard() {
        Fragment frag = new ReciprocityWriteFragment();
        Bundle bundle = new Bundle();

        bundle.putString("mode","insert");
        Log.d(TAG,"bundle="+bundle);
        frag.setArguments(bundle);

        FragmentManager fm = getFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        fragmentTransaction.replace(R.id.fragmentReplace, frag);
        fragmentTransaction.addToBackStack("자율상호주의작성");
        fragmentTransaction.commit();
    }

    //ListView의 item (상세)
    private class ListViewItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Fragment frag = null;
            Bundle bundle = new Bundle();

            FragmentManager fm = getFragmentManager();
            FragmentTransaction fragmentTransaction = fm.beginTransaction();
            fragmentTransaction.replace(R.id.fragmentReplace, frag = new ReciprocityWriteFragment());
            bundle.putString("title","자율상호주의상세");
            String key= penaltyArray.get(position).get("key").toString();
            bundle.putString("peer_key", key);
            bundle.putString("mode", "update");

            frag.setArguments(bundle);
            fragmentTransaction.addToBackStack("자율상호주의상세");
            fragmentTransaction.commit();
        }
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

    @OnClick(R.id.imageView1)
    public void searchDate(){
        async_progress_dialog("getBoardInfo");
    }

    public void getDialog(String gubun) {
        TextView textView;
        if(gubun.equals("SD")){
            textView= tv_button1;
        }else{
            textView= tv_button2;
        }
        DatePickerDialog dialog = new DatePickerDialog(getActivity(), date_listener, UtilClass.dateAndTimeChoiceList(textView, "D").get(0),
        UtilClass.dateAndTimeChoiceList(textView, "D").get(1)-1, UtilClass.dateAndTimeChoiceList(textView, "D").get(2));

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
//            async_progress_dialog();

        }
    };
}

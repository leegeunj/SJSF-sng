package com.creative.sng.app.board;

import android.app.ProgressDialog;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.creative.sng.app.R;
import com.creative.sng.app.adaptor.BoardAdapter;
import com.creative.sng.app.databinding.BasicListBinding; // ✅ ViewBinding import
import com.creative.sng.app.retrofit.Datas;
import com.creative.sng.app.retrofit.RetrofitService;
import com.creative.sng.app.util.UtilClass;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NoticeBoardFragment extends Fragment {
    private static final String TAG = "NoticeBoardFragment";
    private ProgressDialog pDlalog = null;
    private ArrayList<HashMap<String,Object>> arrayList;
    private BoardAdapter mAdapter;

    // ✅ 기존 ButterKnife 변수명 유지
    private ListView listView;
    private TextView textTitle;
    private BasicListBinding binding;  // ✅ ViewBinding 객체
/*    @BindView(R.id.listView1) ListView listView;
    @BindView(R.id.top_title) TextView textTitle; */

   /* @Override
    *//*public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.basic_list, container, false);
        ButterKnife.bind(this, view);*/

   @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = BasicListBinding.inflate(inflater, container, false);
        View view = binding.getRoot();  // ✅ 루트 뷰 설정

       // 포함된 inc_top.xml 뷰 바인딩 수동 처리
       View topLayoutView = view.findViewById(R.id.layout_top);
       com.creative.sng.app.databinding.IncTopBinding topBinding = com.creative.sng.app.databinding.IncTopBinding.bind(topLayoutView);

       // 기존 변수명 유지
       listView = binding.listView1;
       textTitle = binding.layoutTop.topTitle;

       // 텍스트 설정 및 버튼 표시
       textTitle.setText(getArguments().getString("title"));
       topBinding.topWrite.setVisibility(View.VISIBLE);

        /*textTitle.setText(getArguments().getString("title"));
        view.findViewById(R.id.top_write).setVisibility(View.VISIBLE);
*/
        async_progress_dialog();

       listView.setOnItemClickListener(new ListViewItemClickListener());
      /*  listView.setOnItemClickListener(new ListViewItemClickListener());*/

       // ButterKnife @OnClick 대체 방식
       topBinding.topHome.setOnClickListener(v -> goHome());
       topBinding.topWrite.setOnClickListener(v -> getWriteBoard());


       return view;
    }//onCreateView

    public void async_progress_dialog(){
        RetrofitService service = RetrofitService.rest_api.create(RetrofitService.class);

        pDlalog = new ProgressDialog(getActivity());
        UtilClass.showProcessingDialog(pDlalog);

        Call<Datas> call = service.listData("Board","noticeBoardList");
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
                            hashMap.put("key",response.body().getList().get(i).get("push_key"));
                            hashMap.put("data1",response.body().getList().get(i).get("push_title").toString());
                            hashMap.put("data2",response.body().getList().get(i).get("target_nm").toString().trim());
                            hashMap.put("data3",response.body().getList().get(i).get("push_text").toString());
                            hashMap.put("data4",response.body().getList().get(i).get("push_date").toString());
                            hashMap.put("data5",response.body().getList().get(i).get("push_target").toString());
                            arrayList.add(hashMap);
                        }

                        mAdapter = new BoardAdapter(getActivity(), arrayList, "NoticeBoard");
                        listView.setAdapter(mAdapter);
                    } catch ( Exception e ) {
                        e.printStackTrace();
                        Toast.makeText(getActivity(), "에러코드 NoticeBoard 1", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(getActivity(), "onFailure Board",Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void goHome() {
        UtilClass.goHome(getActivity());
    }

    public void getWriteBoard() {
        Fragment frag = new NoticeBoardWriteFragment();
        Bundle bundle = new Bundle();

        bundle.putString("mode","insert");
        frag.setArguments(bundle);

        FragmentManager fm = getFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        fragmentTransaction.replace(R.id.fragmentReplace, frag);
        fragmentTransaction.addToBackStack("공지사항작성");
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
            fragmentTransaction.replace(R.id.fragmentReplace, frag = new NoticeBoardWriteFragment());
            bundle.putString("title","공지사항상세");
            String key= arrayList.get(position).get("key").toString();
            String push_target= arrayList.get(position).get("data5").toString();
            bundle.putString("push_key", key);
            bundle.putString("push_target", push_target);
            bundle.putString("mode", "update");

            frag.setArguments(bundle);
            fragmentTransaction.addToBackStack("공지사항상세");
            fragmentTransaction.commit();
        }
    }

}

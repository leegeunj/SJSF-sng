package com.creative.sng.app.fragment;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Parcelable;
import com.google.android.material.navigation.NavigationView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.creative.sng.app.R;
import com.creative.sng.app.board.NoticeBoardFragment;
import com.creative.sng.app.equip.EquipMainFragment;
import com.creative.sng.app.equip.EquipPopupActivity;
import com.creative.sng.app.gear.CheckApprovalFragment;
import com.creative.sng.app.gear.GearCheckDailyFragment;
import com.creative.sng.app.gear.GearCheckDailyWriteFragment;
import com.creative.sng.app.gear.GearMainFragment;
import com.creative.sng.app.gear.GearPopupActivity;
import com.creative.sng.app.gear.RunningTimeFragment;
import com.creative.sng.app.gear.RunningTimeWriteFragment;
import com.creative.sng.app.menu.LoginActivity;
import com.creative.sng.app.menu.MainFragment;
import com.creative.sng.app.menu.SettingActivity;
import com.creative.sng.app.nfc.set.NdefMessageParser;
import com.creative.sng.app.nfc.set.ParsedRecord;
import com.creative.sng.app.nfc.set.TextRecord;
import com.creative.sng.app.nfc.set.UriRecord;
import com.creative.sng.app.retrofit.Datas;
import com.creative.sng.app.retrofit.RetrofitService;
import com.creative.sng.app.safe.DangerMainFragment;
import com.creative.sng.app.safe.DangerPopupActivity;
import com.creative.sng.app.safe.PeerLoveFragment;
import com.creative.sng.app.safe.PeerLoveWriteFragment;
import com.creative.sng.app.safe.ReciprocityFragment;
import com.creative.sng.app.safe.WorkPerambulateHistoryFragment;
import com.creative.sng.app.safe.WorkPerambulateMainFragment;
import com.creative.sng.app.safe.WorkPerambulatePopupActivity;
import com.creative.sng.app.util.BackPressCloseSystem;
import com.creative.sng.app.util.SettingPreference;
import com.creative.sng.app.util.UtilClass;
import com.creative.sng.app.work.WorkStandardFragment;

import java.lang.annotation.Target;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragMenuActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "FragMenuActivity";
    private RetrofitService service;
    private SettingPreference pref = new SettingPreference("loginData",this);
    private String title;

//  태깅 후 넘어온 데이타
    private String pendingIntent;
    private String target;
    private String tagValue;
    private String targetPoint;
    private String pcType;


    private String sabun_no;
    private String user_nm;
    private String user_sosok;

    private DrawerLayout drawer;
    private FragmentManager fm;

    private BackPressCloseSystem backPressCloseSystem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment);
        backPressCloseSystem = new BackPressCloseSystem(this);
        loadLoginData();
        title= getIntent().getStringExtra("title");
        UtilClass.logD(TAG,"onCreate title="+title);
        UtilClass.logD(TAG,"onCreate pendingIntent="+pendingIntent);

        onMenuInfo(title);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.drawer_open, R.string.drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

    }//onCreate

    private void loadLoginData() {
        sabun_no = pref.getValue("sabun_no","");
        user_nm= pref.getValue("user_nm","");
        user_sosok= pref.getValue("user_sosok","");
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            int fragmentStackCount = fm.getBackStackEntryCount();
            String tag=fm.getBackStackEntryAt(fm.getBackStackEntryCount() - 1).getName();
            UtilClass.logD(TAG, "count="+fragmentStackCount+", tag="+tag);
            UtilClass.logD(TAG, "onBack="+title);
            if(tag.equals("메인")){
                backPressCloseSystem.onBackPressed();
            }else if(fragmentStackCount!=1&&(tag.equals(title+"작성")||tag.equals(title+"상세")||tag.equals(title+"이력")||tag.equals("장비점검이력")||tag.equals("장비점검작성"))){
                if(title.equals("점검일지승인")){
                    onMenuInfo(title);
                } else {
                    super.onBackPressed();
                }
            }else{
                UtilClass.logD(TAG, "피니쉬");
                Fragment fragment = new MainFragment();
                Bundle bundle = new Bundle();
                bundle.putString("mode", "back");
                onFragment(fragment,bundle,"메인");
            }

        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        title= intent.getStringExtra("title");
        UtilClass.logD(TAG,"onNewIntent title="+title);
        onMenuInfo(title);
        pendingIntent= intent.getStringExtra("pendingIntent");
        target= intent.getStringExtra("target");
        UtilClass.logD(TAG, "pending="+ pendingIntent);
        targetPoint = intent.getStringExtra("targetPoint");
        UtilClass.logD(TAG, "targetPoint="+ targetPoint);

        if(pendingIntent!=null){
//            if(target.equals("Check")) pcType= intent.getStringExtra("pc_type");

            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            if(tag != null){
                byte[] tagId = tag.getId();
            }
            processTag(intent);

            UtilClass.logD(TAG, "onNewIntent if tagValue="+ tagValue);
//            ((GearCheckDailyWriteFragment) getSupportFragmentManager().findFragmentByTag(GCDFRAG)).tagToast(tagValue);
            GearCheckDailyWriteFragment gcdw = (GearCheckDailyWriteFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentReplace);
            gcdw.tagToast(tagValue);
//            nfcTaggingData();
        } else {
            title = intent.getStringExtra("title");
            UtilClass.logD(TAG, "onNewIntent else="+ pendingIntent);
//            onMenuInfo(title);
        }
        UtilClass.logD(TAG, "onNewIntent title=" + title);
    }


    //NFC TAG
    // onNewIntent 메소드 수행 후 호출되는 메소드
    private void processTag(Intent passedIntent) {
        Parcelable[] rawMsgs = passedIntent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
        if (rawMsgs == null) {
            return;
        }

        // 참고! rawMsgs.length : 스캔한 태그 개수
        NdefMessage[] msgs;
        if (rawMsgs != null) {
            msgs = new NdefMessage[rawMsgs.length];
            for (int i = 0; i < rawMsgs.length; i++) {
                msgs[i] = (NdefMessage) rawMsgs[i];
                showTag(msgs[i]); // showTag 메소드 호출
            }
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
            tagValue = ((TextRecord) record).getText();
//            Toast.makeText(getApplicationContext(), "Scan success\nTAG : "+tagValue, Toast.LENGTH_SHORT).show();
        }

        return size;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
        BusProvider.getInstance().post(ActivityResultEvent.create(requestCode, resultCode, data));
    }

    public void onMenuInfo(String title){
        Fragment frag = null;
        Bundle bundle = new Bundle();

        fm = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        if(title.equals("메인")){
            fragmentTransaction.replace(R.id.fragmentReplace, frag = new MainFragment());
            String mode= getIntent().getStringExtra("mode");
            if(mode.equals("login")){
                bundle.putString("mode", "first");
            }else{
                bundle.putString("mode", "back");
            }

        }else if(title.equals("공지사항")){
            fragmentTransaction.replace(R.id.fragmentReplace, frag = new NoticeBoardFragment());

        }else if(title.equals("장비관리")){
            String selectGearKey= getIntent().getStringExtra("selectGearKey");

            fragmentTransaction.replace(R.id.fragmentReplace, frag = new GearMainFragment());
            bundle.putString("selectGearKey",selectGearKey);
            bundle.putString("url", MainFragment.ipAddress+MainFragment.contextPath+"/Gear/gearList.do?gear_cd="+selectGearKey);

        }else if(title.equals("가동시간")){
            fragmentTransaction.replace(R.id.fragmentReplace, frag = new RunningTimeFragment());
        //2019.01.23 점검일지승인 추가.
        }else if(title.equals("점검일지승인")){
            fragmentTransaction.replace(R.id.fragmentReplace, frag = new GearCheckDailyFragment());
        //2019.01.31 점검승인 -> 정비의뢰승인으로 변경
        }else if(title.equals("정비의뢰승인")){
            fragmentTransaction.replace(R.id.fragmentReplace, frag = new CheckApprovalFragment());
            bundle.putString("url", MainFragment.ipAddress+MainFragment.contextPath+"/Gear/checkApproval.do?loginSabun="+MainFragment.loginSabun
                    +"&part1_cd="+MainFragment.part1_cd+"&part2_cd="+MainFragment.part2_cd);
        }else if(title.equals("위험기계사용점검")){
            String url= getIntent().getStringExtra("url");
            String selectDaeClassKey= getIntent().getStringExtra("selectDaeClassKey");
            String selectJungClassKey= getIntent().getStringExtra("selectJungClassKey");

            fragmentTransaction.replace(R.id.fragmentReplace, frag = new DangerMainFragment());
            bundle.putString("selectDaeClassKey",selectDaeClassKey);
            bundle.putString("selectJungClassKey",selectJungClassKey);
            bundle.putString("url", MainFragment.ipAddress+ MainFragment.contextPath+"/Safe/safe_write.do?large_cd="+selectDaeClassKey+"&mid_cd="+selectJungClassKey+"&sabun_no="+MainFragment.loginSabun);

        }else if(title.equals("위험기계사용점검이력팝업")||title.equals("위험기계사용점검이력")){
            String selectDaeClassKey= getIntent().getStringExtra("selectDaeClassKey");
            String selectJungClassKey= getIntent().getStringExtra("selectJungClassKey");

            fragmentTransaction.replace(R.id.fragmentReplace, frag = new DangerMainFragment());
            bundle.putString("title","위험기계사용점검이력");
            bundle.putString("selectDaeClassKey",selectDaeClassKey);
            bundle.putString("selectJungClassKey",selectJungClassKey);
            bundle.putString("url", MainFragment.ipAddress+ MainFragment.contextPath+"/Safe/safe_check_history.do?large_cd="+selectDaeClassKey+"&mid_cd="+selectJungClassKey+"&sabun_no="+MainFragment.loginSabun);

        }else if(title.equals("설비점검리스트")){
            String gubun= getIntent().getStringExtra("gubun");
            String selectEquipKey= getIntent().getStringExtra("selectEquipKey");

            fragmentTransaction.replace(R.id.fragmentReplace, frag = new EquipMainFragment());
            bundle.putString("selectEquipKey",selectEquipKey);
            bundle.putString("url", MainFragment.ipAddress+MainFragment.contextPath+"/Equip/equipList.do?gubun="+gubun+"&equip_cd="+selectEquipKey);

        }else if(title.equals("작업장순회점검")){
            String selectDaeClassKey= getIntent().getStringExtra("selectDaeClassKey");
            String selectJungClassKey= getIntent().getStringExtra("selectJungClassKey");

            fragmentTransaction.replace(R.id.fragmentReplace, frag = new WorkPerambulateMainFragment());
            bundle.putString("selectDaeClassKey",selectDaeClassKey);
            bundle.putString("selectJungClassKey",selectJungClassKey);
            bundle.putString("mode","insert");
            bundle.putString("url", MainFragment.ipAddress+ MainFragment.contextPath+"/Safe/workPeram_write.do?large_cd="+selectDaeClassKey+"&mid_cd="+selectJungClassKey+"&sabun_no="+ sabun_no);

        }else if(title.equals("작업장순회점검이력팝업")||title.equals("작업장순회점검이력")){
            String selectDaeClassKey= getIntent().getStringExtra("selectDaeClassKey");
            String selectJungClassKey= getIntent().getStringExtra("selectJungClassKey");

            fragmentTransaction.replace(R.id.fragmentReplace, frag = new WorkPerambulateHistoryFragment());
            bundle.putString("title","작업장순회점검이력");
            bundle.putString("selectDaeClassKey",selectDaeClassKey);
            bundle.putString("selectJungClassKey",selectJungClassKey);
            bundle.putString("url", MainFragment.ipAddress+ MainFragment.contextPath+"/Safe/workPeram_check_history.do?large_cd="+selectDaeClassKey+"&mid_cd="+selectJungClassKey
                    +"&sabun_no="+ sabun_no+"&j_pos="+ MainFragment.jPos);

        }else if(title.equals("자율상호주의")){
            fragmentTransaction.replace(R.id.fragmentReplace, frag = new ReciprocityFragment());

        }else if(title.equals("사람찾기")){
            fragmentTransaction.replace(R.id.fragmentReplace, frag = new PersonnelFragment());

        }else if(title.equals("무재해현황판")){
            fragmentTransaction.replace(R.id.fragmentReplace, frag = new WebFragment());
            bundle.putString("url", MainFragment.ipAddress+ MainFragment.contextPath+"/Common/accident_view.do");

        }else if(title.equals("작업기준")){
            fragmentTransaction.replace(R.id.fragmentReplace, frag = new WorkStandardFragment());

        }else if(title.equals("동료사랑카드")){
            fragmentTransaction.replace(R.id.fragmentReplace, frag = new PeerLoveFragment());

        }else if(title.equals("동료사랑카드상세")){
            fragmentTransaction.replace(R.id.fragmentReplace, frag = new PeerLoveWriteFragment());
            bundle.putString("peer_key", MainFragment.pendingPathKey);
            bundle.putString("mode", "update");

        }else{
            return;
        }
        fragmentTransaction.addToBackStack(title);

        bundle.putString("title",title);
        bundle.putString("sabun_no", sabun_no);
        bundle.putString("user_nm",user_nm);

        frag.setArguments(bundle);
        fragmentTransaction.commit();
    }

    public void onFragment(Fragment fragment, Bundle bundle, String title){
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();

        fragmentTransaction.replace(R.id.fragmentReplace, fragment);
        fragmentTransaction.addToBackStack(title);

        fragment.setArguments(bundle);
        fragmentTransaction.commit();
    }


    private void nfcTaggingData() {       //NFC 테스트
//        pendingIntent= "일상점검";
//        tagValue= "E1200";
//        target= "Check";
//        pcType= "N";

        final ProgressDialog pDlalog = new ProgressDialog(this);
        UtilClass.showProcessingDialog(pDlalog);
        /*Call<Datas> call = service.listDataQuery("Check","tagDataInfo", tagValue, pcType);
        call.enqueue(new Callback<Datas>() {
            @Override
            public void onResponse(Call<Datas> call, Response<Datas> response) {
                UtilClass.logD(TAG, "response="+response);
                if (response.isSuccessful()) {
                    UtilClass.logD(TAG, "isSuccessful="+response.body().toString());
                    String status= response.body().getStatus();
                    try {
                        if(response.body().getCount()==0){
                            Toast.makeText(getApplicationContext(), "해당 태그에 대한 데이터가 없습니다.", Toast.LENGTH_SHORT).show();

                        }else{
                            if(status.equals("groupY")){    //그룹 태그
                                eGroup_no= response.body().getList().get(0).get("EGROUP_NO").toString();
                                equip_no="0";
                            }else{
                                eGroup_no= response.body().getList().get(0).get("EGROUP_NO").toString();
                                equip_no= response.body().getList().get(0).get("EQUIP_NO").toString();
                            }

                            if(target.equals("Check")){
                                Intent intent = new Intent(getBaseContext(),FragMenuActivity.class);
                                intent.putExtra("title", pendingIntent+"작성");
                                intent.putExtra("TAG_ID",tagValue);
                                intent.putExtra("eGroup_no",eGroup_no);
                                intent.putExtra("equip_no",equip_no);
                                intent.putExtra("pc_type",pcType);
                                intent.putExtra("groupYN",status);
                                intent.putExtra("mode", "insert");
                                startActivity(intent);
                                finish();

                            }else{  //설비정보
                                if(equip_no=="0"){   //그룹태그
                                    title= pendingIntent;
                                }else{
                                    title= pendingIntent+"상세";
                                }
                                onMenuInfo(title);
                            }

                        }

                    } catch ( Exception e ) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), "에러코드 NFC 1", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(getApplicationContext(), "onFailure NFC 1", Toast.LENGTH_SHORT).show();
            }
        });
        */
    }

    public void alertDialog(final String gubun){
        final AlertDialog.Builder alertDlg = new AlertDialog.Builder(FragMenuActivity.this);
        alertDlg.setTitle("알림");
        if(gubun.equals("S")){
            alertDlg.setMessage("작성하시겠습니까?");
        }else if(gubun.equals("D")){
            alertDlg.setMessage("삭제하시겠습니까?");
        }else{
            alertDlg.setMessage("로그아웃 하시겠습니까?");
        }
        // '예' 버튼이 클릭되면
        alertDlg.setPositiveButton("예", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(gubun.equals("S")){
                }else if(gubun.equals("D")){
                }else{
                    Intent logIntent = new Intent(getBaseContext(), LoginActivity.class);
                    logIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(logIntent);
                }
            }
        });
        // '아니오' 버튼이 클릭되면
        alertDlg.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDlg.show();
    }



    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        Intent intent=null;

        if (id == R.id.nav_common||id == R.id.nav_safe||id == R.id.nav_mate) {
            return false;
        }else if (id == R.id.nav_common1) {
            intent = new Intent(getApplicationContext(),FragMenuActivity.class);
            intent.putExtra("title", "공지사항");

        }else if (id == R.id.nav_common2) {
            intent = new Intent(getApplicationContext(),FragMenuActivity.class);
            intent.putExtra("title", "사람찾기");

        }else if (id == R.id.nav_common3) {
            intent = new Intent(getApplicationContext(),FragMenuActivity.class);
            intent.putExtra("title", "무재해현황판");

        } else if (id == R.id.nav_mate1) {
            intent = new Intent(getApplicationContext(),GearPopupActivity.class);
            intent.putExtra("title", "장비관리");
            startActivity(intent);
            drawer.closeDrawer(GravityCompat.START);
            return false;

        } else if (id == R.id.nav_mate2) {
            intent = new Intent(getApplicationContext(),FragMenuActivity.class);
            intent.putExtra("title", "점검일지승인");
        } else if (id == R.id.nav_mate3) {
            intent = new Intent(getApplicationContext(),FragMenuActivity.class);
            intent.putExtra("title", "정비의뢰승인");

        } else if (id == R.id.nav_safe1) {
            intent = new Intent(getApplicationContext(),FragMenuActivity.class);
            intent.putExtra("title", "작업기준");

        } else if (id == R.id.nav_safe2) {
            intent = new Intent(getApplicationContext(),DangerPopupActivity.class);
            intent.putExtra("title", "위험기계사용점검");
            startActivity(intent);
            drawer.closeDrawer(GravityCompat.START);
            return false;

        } else if (id == R.id.nav_safe3) {
            intent = new Intent(getApplicationContext(),EquipPopupActivity.class);
            intent.putExtra("gubun", "PSM대상설비점검");
            startActivity(intent);
            drawer.closeDrawer(GravityCompat.START);
            return false;

        } else if (id == R.id.nav_safe4) {
            intent = new Intent(getApplicationContext(),FragMenuActivity.class);
            intent.putExtra("title", "동료사랑카드");

        } else if (id == R.id.nav_safe5) {
            intent = new Intent(getApplicationContext(),FragMenuActivity.class);
            intent.putExtra("title", "자율상호주의");

        } else if (id == R.id.nav_safe6) {
            intent = new Intent(getApplicationContext(),WorkPerambulatePopupActivity.class);
            intent.putExtra("title", "작업장순회점검");
            startActivity(intent);
            drawer.closeDrawer(GravityCompat.START);
            return false;

        } else if (id == R.id.nav_log_out) {
            alertDialog("L");
            return false;
        }else{

        }
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);

        drawer.closeDrawer(GravityCompat.START);

        return true;
    }

}

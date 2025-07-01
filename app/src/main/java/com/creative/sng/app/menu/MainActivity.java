package com.creative.sng.app.menu;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.creative.sng.app.R;
import com.creative.sng.app.fragment.FragMenuActivity;
import com.creative.sng.app.gear.GearPopupActivity;
import com.creative.sng.app.util.BackPressCloseSystem;
import com.creative.sng.app.util.SettingPreference;
import com.creative.sng.app.util.UtilClass;
// import com.google.firebase.iid.FirebaseInstanceId;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.normal.TedPermission;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

//    adb shell dumpsys activity activities | findstr "Run"
    private static final String TAG = "MainActivity";
//    public static String ipAddress= "http://192.168.0.9:8585";
//    public static String ipAddress= "http://119.202.60.107:8585";
    public static String ipAddress = "http:59.24.91.15:8585"; // -> 최종 SNG IP 주소 수정
    public static String contextPath= "/sjsf_sng";

    //FCM 관련
    private String INSERT_URL=MainActivity.ipAddress+MainActivity.contextPath+"/rest/Board/fcmTokenData";
    private String token=null;
    private String phone_num=null;
    public static boolean onAppCheck= false;
    public static String pendingPath= "";
    public static String pendingPathKey= "";

    private BackPressCloseSystem backPressCloseSystem;
    private PermissionListener permissionlistener;

    private SettingPreference pref = new SettingPreference("loginData",this);
    public static String loginSabun;
    public static String loginName;
    public static String jPos;
    public static String part1_cd;
    public static String part2_cd;
    public static String latestAppVer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        ButterKnife.bind(this);
        backPressCloseSystem = new BackPressCloseSystem(this);

        loginSabun = pref.getValue("sabun_no","").trim();
        loginName = pref.getValue("user_nm","").trim();
        jPos = pref.getValue("j_pos","").trim();
        part1_cd = pref.getValue("part1_cd","");
        part2_cd = pref.getValue("part2_cd","");
        latestAppVer = pref.getValue("latest_app_ver","");

        String currentAppVer= getAppVersion(this);
        UtilClass.logD(TAG, "currentAppVer="+currentAppVer+", latestAppVer="+latestAppVer);

        if(!currentAppVer.equals(latestAppVer)){
            //최신버전이 아닐때
//            Snackbar.make(getView(), "현재 앱의 버전보다 높은 최신 버전이 있습니다.", Snackbar.LENGTH_INDEFINITE).setAction("다운받기", new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    new Thread() {
//                        public void run() {
//                            download_WebLink();
//                        }
//                    }.start();
//                }
//            }).show();

        }else{

        }

//        token = FirebaseInstanceId.getInstance().getToken();
//        UtilClass.logD(TAG, "Refreshed token: " + token);


//        // 전화번로를 얻기 위한 권한 요청 처리 -> 현재는 사용하지 않음
//        TedPermission.create()
//                .setPermissionListener(new PermissionListener() {
//            @Override
//            public void onPermissionGranted() {
////                Toast.makeText(getApplicationContext(), "권한 허가", Toast.LENGTH_SHORT).show();
//                phone_num= getPhoneNumber();
//                postData();
//            }
//
//            @Override
//            public void onPermissionDenied(ArrayList<String> deniedPermissions) {
//                Toast.makeText(getApplicationContext(), "권한 거부 목록\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();
//                phone_num="";
//                postData();
//            }
//        })
//                .setRationaleMessage("전화번호 정보를 가져오기 위해선 권한이 필요합니다.")
//                .setDeniedMessage("권한을 확인하세요.\n\n [설정] > [애플리케이션] [해당앱] > [권한]")
//                .setGotoSettingButtonText("권한확인")
//                .setPermissions(Manifest.permission.CALL_PHONE)
//                .check();

        onAppCheck= true;

    }

    public static String getAppVersion(Context context) {
        // application version
        String versionName = "";
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            versionName = info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            UtilClass.logD(TAG, "getAppVersion Error");
        }

        return versionName;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        backPressCloseSystem.onBackPressed();
    }

    @OnClick(R.id.imageView1)
    public void getMenu1() {
        Intent intent = new Intent(getBaseContext(),FragMenuActivity.class);
        intent.putExtra("title", "공지사항");
        startActivity(intent);
    }

    @OnClick(R.id.imageView2)
    public void getMenu2() {
        Intent intent = new Intent(getBaseContext(),GearPopupActivity.class);
        intent.putExtra("gubun", "장비관리");
        startActivity(intent);
    }

    @OnClick(R.id.imageView3)
    public void getMenu3() {
        Toast.makeText(getApplicationContext(), "비활성화 메뉴입니다.",Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.imageView4)
    public void getMenu4() {
        Intent intent = new Intent(getBaseContext(),FragMenuActivity.class);
        intent.putExtra("title", "점검승인");
        startActivity(intent);
    }

    @OnClick(R.id.imageView5)
    public void getMenu5() {
        Toast.makeText(getApplicationContext(), "비활성화 메뉴입니다.",Toast.LENGTH_SHORT).show();

    }

    @OnClick(R.id.imageView6)
    public void getMenu6() {
        Toast.makeText(getApplicationContext(), "비활성화 메뉴입니다.",Toast.LENGTH_SHORT).show();

    }

    @OnClick(R.id.imageView7)
    public void getMenu7() {
        Toast.makeText(getApplicationContext(), "비활성화 메뉴입니다.",Toast.LENGTH_SHORT).show();
//        Intent intent = new Intent(MainActivity.this,DangerPopupActivity.class);
//        intent.putExtra("title", "안전점검");
//        startActivity(intent);
    }

    @OnClick(R.id.imageView8)
    public void getMenu8() {
        Toast.makeText(getApplicationContext(), "비활성화 메뉴입니다.",Toast.LENGTH_SHORT).show();

    }

    //푸시 데이터 전송
    public void postData() {
        String sabun_no= pref.getValue("sabun_no","");
        String android_id = "" + android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);

        WebServiceTask wst= new WebServiceTask(WebServiceTask.POST_TASK, MainActivity.this, "Loading...");

        wst.addNameValuePair("Token",token);
        wst.addNameValuePair("phone_num",phone_num);
        wst.addNameValuePair("sabun_no",sabun_no);
        wst.addNameValuePair("and_id",android_id);

        // the passed String is the URL we will POST to
        wst.execute(new String[] { INSERT_URL });

    }

    //작성 완료
    public void handleResponse(String response) {
        UtilClass.logD(TAG,"response="+response);

        try {
            JSONObject jso = new JSONObject(response);
            String status= jso.get("status").toString();

            if(status.equals("success")){

            }else{
                Toast.makeText(getApplicationContext(), "토큰 생성에 실패하였습니다.",Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Log.e(TAG, e.getLocalizedMessage(), e);
            Toast.makeText(getApplicationContext(), "handleResponse Main",Toast.LENGTH_SHORT).show();
        }

    }

    private class WebServiceTask extends AsyncTask<String, Integer, String> {

        public static final int POST_TASK = 1;

        private static final String TAG = "WebServiceTask";

        // connection timeout, in milliseconds (waiting to connect)
        private static final int CONN_TIMEOUT = 3000;

        // socket timeout, in milliseconds (waiting for data)
        private static final int SOCKET_TIMEOUT = 5000;

        private int taskType = POST_TASK;
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

//    // 단말기 핸드폰번호 얻어오기 -> 현재 안쓰는 코드임
//    public String getPhoneNumber() {
//        String num = null;
//        try {
//            TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
//            num = tm.getLine1Number();
//            if(num.startsWith("+82")){
//                num = num.replace("+82", "0");
//            }
//        } catch (Exception e) {
//            Toast.makeText(getApplicationContext(), "오류가 발생 하였습니다!", Toast.LENGTH_SHORT).show();
//            e.printStackTrace();
//        }
//
//        return num;
//    }
}

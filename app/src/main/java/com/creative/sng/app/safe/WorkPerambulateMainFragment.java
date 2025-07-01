package com.creative.sng.app.safe;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.creative.sng.app.R;
import com.creative.sng.app.fragment.ActivityResultEvent;
import com.creative.sng.app.fragment.BusProvider;
import com.creative.sng.app.menu.MainFragment;
import com.creative.sng.app.util.UtilClass;
import com.gun0912.tedpermission.PermissionListener;
// import com.gun0912.tedpermission.normal.TedPermission;
import com.squareup.otto.Subscribe;

import java.io.File;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class WorkPerambulateMainFragment extends Fragment {

    private static final String TAG = "WorkPerambulateMainFragment";
    private static String UPLOAD_URL = MainFragment.ipAddress+ MainFragment.contextPath+"/Safe/fileUpload.do";
    private String url;
    private String sabun_no;
    private String mode;
    private String selectDaeClassKey;
    private String selectJungClassKey;

    @BindView(R.id.top_title) TextView textTitle;
    @BindView(R.id.webView1) WebView webView;
    @BindView(R.id.textView1) TextView bottomText1;
    @BindView(R.id.textView2) TextView bottomText2;
    private ProgressDialog dialog;

    //이미지,앨범 선택 업로드 관련
    private static final int PICK_FROM_CAMERA = 1;
    private static final int PICK_FROM_ALBUM = 2;
    private static final int CROP_FROM_IMAGE = 3;
    final int RESULT_OK=-1;
    private String filePath;
    private String file_seq;
    private String fileName;

    private String mCurrentPhotoPath;
    private Uri photoURI, albumURI = null;
    private Boolean album =false;

    private PermissionListener permissionlistener;

    public WorkPerambulateMainFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.safe_main, container, false);
        ButterKnife.bind(this, view);
        dialog = new ProgressDialog(getActivity());
        textTitle.setText(getArguments().getString("title"));

        sabun_no = MainFragment.loginSabun;
        url= getArguments().getString("url");
        selectDaeClassKey= getArguments().getString("selectDaeClassKey");
        selectJungClassKey= getArguments().getString("selectJungClassKey");
        mode= getArguments().getString("mode");

        UtilClass.logD(TAG, mode);
        if(mode.equals("insert")){

        }else{

        }

        final Context myApp = getActivity();
        //자바스크립트 Alert,confirm 사용
        webView.setWebChromeClient(new WebChromeClient() {
            ProgressBar pb = (ProgressBar)view.findViewById(R.id.progressBar1);

            @Override
            public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
                new AlertDialog.Builder(myApp)
                        .setTitle("경고")
                        .setMessage(message)
                        .setPositiveButton(android.R.string.ok,
                                new AlertDialog.OnClickListener() {

                                    public void onClick(DialogInterface dialog, int which) {
                                        result.confirm();
                                    }
                                })
                        .setCancelable(false)
                        .create()
                        .show();
                return true;
            }
            @Override
            public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
                // TODO Auto-generated method stub
                //return super.onJsConfirm(view, url, message, result);
                new AlertDialog.Builder(view.getContext())
                        .setTitle("알림")
                        .setMessage(message)
                        .setPositiveButton("네",
                                new AlertDialog.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        result.confirm();
                                    }
                                })
                        .setNegativeButton("아니오",
                                new AlertDialog.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        result.cancel();
                                    }
                                })
                        .setCancelable(false)
                        .create()
                        .show();
                return true;
            }

            public void onProgressChanged(WebView webView, int paramInt) {
                this.pb.setProgress(paramInt);
                if (paramInt == 100)
                {
                    this.pb.setVisibility(View.GONE);
                    return;
                }
                this.pb.setVisibility(View.VISIBLE);
            }
        });//setWebChromeClient 재정의

        WebSettings wSetting = webView.getSettings();
        webView.setWebViewClient(new WebViewClient()); // 이걸 안해주면 새창이 뜸
        webView.setWebViewClient(new MyWebViewClient());
        wSetting.setJavaScriptEnabled(true);      // 웹뷰에서 자바 스크립트 사용
        webView.setVerticalScrollBarEnabled(false);
        webView.setHorizontalScrollBarEnabled(false);

        webView.addJavascriptInterface(new AndroidBridge(), "android");
        webView.loadUrl(url);

        return view;
    }

    private class AndroidBridge {
        @JavascriptInterface
        public void imgChoice(final String paramString){
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    file_seq = paramString;
                    imagesChoice();
                }
            });
        }
        @JavascriptInterface
        public void imgView(final String paramString){
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(getActivity(), SafeImgViewActivity.class);
                    intent.putExtra("large_cd",selectDaeClassKey);
                    intent.putExtra("mid_cd",selectJungClassKey);
                    intent.putExtra("chk_cd",paramString);
                    startActivity(intent);
                }
            });
        }
        @JavascriptInterface
        public void beforeSend(){
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    UtilClass.showProgressDialog(dialog);
                }
            });
        }
        @JavascriptInterface
        public void saveAfterResult(){
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    UtilClass.closeProgressDialog(dialog);
                    Toast.makeText(getActivity(), "저장 되었습니다.", Toast.LENGTH_SHORT).show();
                    webView.loadUrl(url);
                }
            });
        }
    }

    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url){
//            Log.d("shouldOveride","웹뷰클릭 됨="+url);
            view.loadUrl(url);

            return true;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);

            UtilClass.showProgressDialog(dialog);
        }
        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            try {
                UtilClass.closeProgressDialog(dialog);
            } catch (Exception e) {
                // TODO: handle exception
            }

        }

        @Override
        public void onReceivedError(WebView view, int errorCode,String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
            Log.d("onReceivedError", "errorCode=" + errorCode);
            switch(errorCode) {
                case ERROR_AUTHENTICATION:              // 서버에서 사용자 인증 실패
                case ERROR_BAD_URL:                     // 잘못된 URL
                case ERROR_CONNECT:                     // 서버로 연결 실패
                case ERROR_FAILED_SSL_HANDSHAKE:     	// SSL handshake 수행 실패
                case ERROR_FILE:                        // 일반 파일 오류
                case ERROR_FILE_NOT_FOUND:              // 파일을 찾을 수 없습니다
                case ERROR_HOST_LOOKUP:            		// 서버 또는 프록시 호스트 이름 조회 실패
                case ERROR_IO:                          // 서버에서 읽거나 서버로 쓰기 실패
                case ERROR_PROXY_AUTHENTICATION:    	// 프록시에서 사용자 인증 실패
                case ERROR_REDIRECT_LOOP:               // 너무 많은 리디렉션
                case ERROR_TIMEOUT:                     // 연결 시간 초과
                case ERROR_TOO_MANY_REQUESTS:           // 페이지 로드중 너무 많은 요청 발생
                case ERROR_UNKNOWN:                     // 일반 오류
                case ERROR_UNSUPPORTED_AUTH_SCHEME:  	// 지원되지 않는 인증 체계
                case ERROR_UNSUPPORTED_SCHEME:			// URI가 지원되지 않는 방식

                    AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                    builder.setTitle("Error");
                    builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
//                            Fragment fm = getFragmentManager().findFragmentByTag("dfdf");
                            getActivity().finish();
                        }
                    });
                    builder.setMessage("네트워크 상태가 원활하지 않습니다. 잠시 후 다시 시도해 주세요.");
                    builder.show();

                    break;
            }
        }
    }//MyWebViewClient


//    public void imageUpload(){
//        permissionlistener = new PermissionListener() {
//            @Override
//            public void onPermissionGranted() {
////                Toast.makeText(getApplicationContext(), "권한 허가", Toast.LENGTH_SHORT).show();
//                imagesChoice();
//            }
//
//            @Override
//            public void onPermissionDenied(ArrayList<String> deniedPermissions) {
//                Toast.makeText(getActivity(), "권한 거부 목록\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();
//
//            }
//        };
//        new TedPermission(getActivity())
//                .setPermissionListener(permissionlistener)
//                .setRationaleMessage("앨범/촬영 이미지 업로드를 위해선 권한이 필요합니다.")
//                .setDeniedMessage("권한을 확인하세요.\n\n [설정] > [애플리케이션] [해당앱] > [권한]")
//                .setGotoSettingButtonText("권한확인")
//                .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
//                .check();
//    }

    private void imagesChoice(){
        AlertDialog.Builder alertDlg = new AlertDialog.Builder(getActivity());
        alertDlg.setTitle("선택하세요.")
                .setCancelable(true);

        alertDlg.setPositiveButton("앨범", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int paramInt) {
                getPhotoFromGallery();
            }
        });
        alertDlg.setNegativeButton("촬영", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int paramInt) {
                getPhotoFromCamera();
            }
        });

//        alertDlg.setMessage("?");
        alertDlg.show();
    }

    private void getPhotoFromCamera() { // 카메라 촬영 후 이미지 가져오기
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if(getActivity() != null) {
            File photoFile = createImageFile();      //사진 찍은 후 임시파일 저장

            if(photoFile != null){
                photoURI = Uri.fromFile(photoFile); //임시 파일의 위치,경로 가져옴
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI); //임시 파일 위치에 저장
                getActivity().startActivityForResult(intent, PICK_FROM_CAMERA);
            }
        }
    }

    private void getPhotoFromGallery() { // 갤러리에서 이미지 가져오기
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        getActivity().startActivityForResult(Intent.createChooser(intent, "Select file to upload "), PICK_FROM_ALBUM);
    }

    private File createImageFile() {
        String imageFileName = "tmp_" + String.valueOf(System.currentTimeMillis()) + ".jpg";
        File storageDir = new File(Environment.getExternalStorageDirectory()+"/File/", imageFileName);
        mCurrentPhotoPath = storageDir.getAbsolutePath();
        return storageDir;
    }

    private void cropImage() {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(photoURI, "image/*");

//        intent.putExtra("outputX", 200);
//        intent.putExtra("outputY", 200);
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("scale", true);

        if(album == false) {
            intent.putExtra("output",photoURI);
            UtilClass.logD(TAG,"photoURI="+photoURI);
        }else{
            intent.putExtra("output",albumURI);
            UtilClass.logD(TAG,"albumURI="+albumURI);
        }
        getActivity().startActivityForResult(intent, CROP_FROM_IMAGE);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        BusProvider.getInstance().register(this);
    }

    @Override
    public void onDestroyView() {
        BusProvider.getInstance().unregister(this);
        super.onDestroyView();

    }

    /**
     * Fragment에서 startactivityForresult실행시 fragment에 들어오지 않는 문제
     *
     * @param activityResultEvent
     */
    @Subscribe
    public void onActivityResultEvent(ActivityResultEvent activityResultEvent){
        onActivityResult(activityResultEvent.getRequestCode(), activityResultEvent.getResultCode(), activityResultEvent.getData());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        if (resultCode != RESULT_OK) return;

        switch (requestCode){
            case  PICK_FROM_ALBUM:{
                album = true;
                File albumFile  = createImageFile();
                if(albumFile != null){
                    albumURI = Uri.fromFile(albumFile);
                }
                photoURI = data.getData();  //앨범이미지 경로
            }

            case PICK_FROM_CAMERA:{
                cropImage();
                break;
            }

            case CROP_FROM_IMAGE:{
                Bitmap photo = BitmapFactory.decodeFile(photoURI.getPath());    //크롭된 이미지
                Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);  //동기화

                if(album == false) {
                    intent.setData(photoURI);
                    filePath= photoURI.getPath();
                }else{
                    album= false;
                    intent.setData(albumURI);
                    filePath= albumURI.getPath();
                }
                UtilClass.logD(TAG,"filePath="+filePath);
                int lastIndexOf = filePath.lastIndexOf("/");
                fileName= filePath.substring(lastIndexOf+1, filePath.length());

                getActivity().sendBroadcast(intent);

                dialog = ProgressDialog.show(getActivity(), "Uploading", "Please wait...", true);
//                new ImageUploadTask().execute();

                break;
            }
        }
    }

//    class ImageUploadTask extends AsyncTask<Void, Void, String> {
//        @Override
//        protected void onPreExecute() {
//        }
//
//        @Override
//        protected String doInBackground(Void... unsued) {
//            try {
//                MultipartEntityBuilder builder = MultipartEntityBuilder.create().setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
////                builder.addTextBody("STRING_KEY", "STRING_VALUE", ContentType.create("Multipart/related", "UTF-8"));
//                builder.addPart("fileData", new FileBody(new File(filePath)));
//
//                //전송
//                HttpClient httpClient = AndroidHttpClient.newInstance("Android");
//                HttpPost httpPost = new HttpPost(UPLOAD_URL);
//                httpPost.setHeader("Accept-Charset","UTF-8");
//                httpPost.setHeader("ENCTYPE","multipart/form-data");
//
//                httpPost.setEntity(builder.build());
//                httpClient.execute(httpPost);
//
//                return filePath;
//            } catch (Exception e) {
//                if (dialog.isShowing())
//                    dialog.dismiss();
//                Toast.makeText(getActivity(), "업로드에 실패 하였습니다.", Toast.LENGTH_LONG).show();
//                Log.e(e.getClass().getName(), e.getMessage(), e);
//                return null;
//            }
//
//        }
//
//        @Override
//        protected void onProgressUpdate(Void... unsued) {
//
//        }
//
//        @Override
//        protected void onPostExecute(String sResponse) {
//            try {
//                if (dialog.isShowing()) dialog.dismiss();
//                UtilClass.logD(TAG, "sResponse="+sResponse);
//                if(sResponse!=null){
//                    Toast.makeText(getActivity(), "업로드 완료", Toast.LENGTH_SHORT).show();
//                    webView.loadUrl("javascript: setUploadedImg('" + file_seq + "','" + fileName + "');");
//
//                }else{
//                    Toast.makeText(getActivity(), "업로드에 실패 하였습니다.", Toast.LENGTH_SHORT).show();
//                }
//
//            } catch (Exception e) {
//                Toast.makeText(getActivity(), "업로드에 실패 하였습니다.", Toast.LENGTH_LONG).show();
//                Log.e(e.getClass().getName(), e.getMessage(), e);
//            }
//        }
//    }

    public void onFragment(Fragment fragment, Bundle bundle, String title){
        FragmentManager fm = getFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        fragmentTransaction.replace(R.id.fragmentReplace, fragment);
        fragmentTransaction.addToBackStack(title);

        fragment.setArguments(bundle);
        fragmentTransaction.commit();
    }

    @OnClick(R.id.top_home)
    public void goHome() {
        UtilClass.goHome(getActivity());
    }

    @OnClick(R.id.textView1)
    public void goMenu1() {
        webView.loadUrl("javascript:fn_safe_chk_result('" + mode + "');");
    }

    @OnClick(R.id.textView2)
    public void goMenu2() {
        Fragment fragment = new WorkPerambulateHistoryFragment();
        Bundle bundle = new Bundle();
        onFragment(fragment, bundle, "안전관리이력");

        bundle.putString("title","안전관리이력");
        bundle.putString("selectDaeClassKey",selectDaeClassKey);
        bundle.putString("selectJungClassKey",selectJungClassKey);
        bundle.putString("url", MainFragment.ipAddress+ MainFragment.contextPath+"/Safe/safe_check_history.do?large_cd="+selectDaeClassKey+"&mid_cd="
                +selectJungClassKey+"&sabun_no="+ sabun_no+"&j_pos="+ MainFragment.jPos);

    }

}

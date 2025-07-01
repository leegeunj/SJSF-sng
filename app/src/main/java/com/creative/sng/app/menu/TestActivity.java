package com.creative.sng.app.menu;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.creative.sng.app.R;
import com.creative.sng.app.util.BackPressCloseSystem;

import butterknife.ButterKnife;

public class TestActivity extends AppCompatActivity {

    private BackPressCloseSystem backPressCloseSystem;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        ButterKnife.bind(this);
        backPressCloseSystem = new BackPressCloseSystem(this);


    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        backPressCloseSystem.onBackPressed();
    }

}

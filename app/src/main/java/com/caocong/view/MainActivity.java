package com.caocong.view;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    CircleProgressView circleProgressView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_main);
        circleProgressView = (CircleProgressView) findViewById(R.id.circle);
        circleProgressView.setProgress(81);
        circleProgressView.setOnObtainProgressListener(new CircleProgressView.OnObtainProgressListener() {
            @Override
            public int onObtainProgress() {
                return getAvailMemPercent();
            }
        });
    }


    //获取可用内存大小
    private int getAvailMemPercent() {
        // 获取android当前可用内存大小
        ActivityManager am = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(mi);

        return (int) (mi.availMem * 1.00f / mi.totalMem * 100);
    }

}

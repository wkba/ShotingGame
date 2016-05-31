package com.example.exp006.shootinggame;
import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

//シューティングゲーム
public class MainActivity extends Activity {
    //アクティビティ起動時に呼ばれる

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        getWindow().clearFlags(
                WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(new com.example.exp006.shootinggame.ShootingView(this));
    }
}
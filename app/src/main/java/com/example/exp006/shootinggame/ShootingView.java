package com.example.exp006.shootinggame;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

//シューティングゲーム
public class ShootingView extends SurfaceView
        implements SurfaceHolder.Callback, Runnable {
    //シーン定数(1)
    private final static int
            S_TITLE    = 0,//タイトル
            S_PLAY     = 1,//プレイ
            S_GAMEOVER = 2;//ゲームオーバー

    //画面サイズ定数
    private final static int
            W = 480,//画面幅
            H = 800;//画面高さ

    //システム
    private SurfaceHolder holder;        //サーフェイスホルダー
    private com.example.exp006.shootinggame.Graphics g;                  //グラフィック
    private Thread   thread;             //スレッド
    private Bitmap[] bmp = new Bitmap[9];//ビットマップ
    private int      init = S_TITLE;     //初期化(1)
    private int      scene = S_TITLE;    //シーン(1)
    private int      score;              //スコア
    private int      tick;               //時間経過
    private long     gameoverTime = 20;       //ゲームオーバー時間
    private long     now = 0;
    private String TAG ="test";

    //宇宙船
    private int shipX;          //宇宙船X
    private int shipY = 600;    //宇宙船Y
    private int shipToX = shipX;//宇宙船移動先X

    //爆発クラス(4)
    private class Bom {
        int x;
        int y;
        int life;

        //コンストラクタ
        private Bom(int x, int y) {
            this.x = x;
            this.y = y;
            this.life = 3;
        }
    }

    //隕石・弾・爆発
    private List<Point> meteos = new ArrayList<Point>();//隕石
    private List<Point> shots  = new ArrayList<Point>();//弾
    private List<Bom> boms     = new ArrayList<Bom>();//爆発

    //コンストラクタ
    public ShootingView(Activity activity) {
        super(activity);

        //ビットマップの読み込み
        for (int i = 0; i < 9; i++) {
            bmp[i] = readBitmap(activity, "sht"+i);
        }

        //サーフェイスホルダーの生成
        holder = getHolder();
        holder.addCallback(this);

        //画面サイズの指定
        Display display = activity.getWindowManager().getDefaultDisplay();
        Point p = new Point();
        display.getSize(p);
        int dh = W*p.y/p.x;

        //グラフィックスの生成
        g = new com.example.exp006.shootinggame.Graphics(W, dh, holder);
        g.setOrigin(0, (dh-H)/2);
    }

    //サーフェイス生成時に呼ばれる
    public void surfaceCreated(SurfaceHolder holder) {
        thread = new Thread(this);
        thread.start();
    }

    //サーフェイス終了時に呼ばれる
    public void surfaceDestroyed(SurfaceHolder holder) {
        thread = null;
    }

    //サーフェイス変更時に呼ばれる
    public void surfaceChanged(SurfaceHolder holder,
                               int format, int w, int h) {
    }

    //スレッドの処理
    public void run() {
        while(thread != null) {
            //初期化
            if (init >= 0) {
                scene = init;
                //タイトル
                if (scene == S_TITLE) {
                    shipX = W/2;
                    shots.clear();
                    meteos.clear();
                    boms.clear();
                    tick = 0;
                    score = 0;
                    now = 0;
                }
                //ゲームオーバー
                else if (scene == S_GAMEOVER) {
                    gameoverTime = 20;
                    now = 0;
                }
                init = -1;
            }

            //プレイ時の処理
            if (scene == S_PLAY) {
                //ゲームオーバー
                if (gameoverTime == now) {
                    init = S_GAMEOVER;
                    now = 0;
                }else{
                    now += 1;
                }
                //Log.v("time", String.valueOf(System.currentTimeMillis()));
                //隕石の出現(2)
                tick++;
                if (tick > 10) {
                    tick = 0;
                    meteos.add(new Point(rand(W), -50));
                }

                //隕石の移動(2)
                for (int i = meteos.size()-1; i >= 0; i--) {
                    Point pos = meteos.get(i);
                    pos.y += 5;

                    //ゲームオーバー
                    if (pos.y > H) {
                        init = S_GAMEOVER;
                    }
                }

                //弾の移動(3)
                for (int i = shots.size()-1; i >= 0; i--) {
                    Point pos0 = shots.get(i);
                    pos0.y -= 10;

                    //削除
                    if (pos0.y < -100) {
                        shots.remove(i);
                    }
                    //衝突
                    else {
                        for (int j = meteos.size() - 1; j >= 0; j--) {
                            Point pos1 = meteos.get(j);
                            if (Math.abs(pos0.x - pos1.x) < 50 &&
                                    Math.abs(pos0.y - pos1.y) < 50) {
                                //爆発の追加(4)
                                boms.add(new Bom(pos1.x, pos1.y));
                                shots.remove(i);
                                meteos.remove(j);
                                score += 1;
                                break;
                            }
                        }
                    }
                }

                //爆発の遷移(4)
                for (int i = boms.size()-1; i >= 0; i--) {
                    Bom bom = boms.get(i);
                    bom.life--;
                    if (bom.life < 0) {
                        boms.remove(i);
                    }
                }

                //宇宙船の移動(5)
                if (Math.abs(shipX - shipToX) < 10) {
                    shipX = shipToX;
                } else if (shipX < shipToX) {
                    shipX += 10;
                } else if (shipX > shipToX) {
                    shipX -= 10;
                }
            }

            //背景の描画
            g.lock();
            g.drawBitmap(bmp[0], 0, 0);
            if (scene == S_GAMEOVER) {
                //g.drawBitmap(bmp[3], 0, H-190);
            } else {
                //g.drawBitmap(bmp[2], 0, H-190);
            }

            //宇宙船の描画
            g.drawBitmap(bmp[6], shipX-48, shipY-50);

            //隕石の描画
            for (int i = meteos.size()-1; i >= 0; i--) {
                Point pos = meteos.get(i);
                g.drawBitmap(bmp[5], pos.x-43, pos.y-45);
            }

            //弾の描画
            for (int i = shots.size()-1; i >= 0; i--) {
                Point pos = shots.get(i);
                g.drawBitmap(bmp[7], pos.x-10, pos.y-18);
            }

            //爆発の描画
            for (int i = boms.size()-1; i >= 0; i--) {
                Bom bom = boms.get(i);
                g.drawBitmap(bmp[1], bom.x-57, bom.y-57);
            }

            //メッセージの描画
            if (scene == S_TITLE) {
                g.drawBitmap(bmp[8], (W-400)/2, 150);
            } else  if (scene == S_GAMEOVER) {
                g.drawBitmap(bmp[4], (W-400)/2, 150);
            }

            //スコア、経過時間の描画
            g.setColor(Color.WHITE);
            g.setTextSize(30);
            g.drawText("BANANA: " + num2str(score, 6) + "  Time: " + (gameoverTime - now)/10,
                    10, 80 + g.getOriginY() - (int) g.getFontMetrics().ascent);
            g.unlock();

            //スリープ
            try {
                Thread.sleep(30);
            } catch (Exception e) {
            }
        }
    }

    //タッチ時に呼ばれる
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int touchX = (int)(event.getX()*W/getWidth());
        int touchAction = event.getAction();
        if (touchAction == MotionEvent.ACTION_DOWN) {
            //タイトル
            if (scene == S_TITLE) {
                init = S_PLAY;
            }
            //プレイ
            else if (scene == S_PLAY) {
                //弾の追加(3)
                shots.add(new Point(shipX, shipY-50));

                //宇宙船の移動(5)
                shipToX = touchX;
            }
            //ゲームオーバー
            else if (scene == S_GAMEOVER) {
                //ゲームオーバー後1秒以上
                if (gameoverTime+1000 < System.currentTimeMillis()) {
                    init = S_TITLE;
                }
            }
        } else if (touchAction == MotionEvent.ACTION_MOVE) {
            //プレイ
            if (scene == S_PLAY) {
                //宇宙船の移動
                shipToX = touchX;
            }
        }
        return true;
    }

    //乱数の取得
    private static Random rand = new Random();
    private static int rand(int num) {
        return (rand.nextInt()>>>1)%num;
    }

    //数値→文字列
    private static String num2str(int num, int len) {
        String str = ""+num;
        while(str.length() < len) str = "0"+str;
        return str;
    }

    //数値→タイム（--:--）
    private static String num2time(int num) {
        return num/360 + ":" + num%360;
    }
    //ビットマップの読み込み
    private static Bitmap readBitmap(Context context, String name) {
        int resID = context.getResources().getIdentifier(
                name, "drawable", context.getPackageName());
        return BitmapFactory.decodeResource(
                context.getResources(), resID);
    }
}
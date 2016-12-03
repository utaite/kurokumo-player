package com.yuyu.kurokumoplayer.main;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.yuyu.kurokumoplayer.R;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

// 포그라운드에서 계속 노래가 돌아가는 서비스
public class MusicService extends Service {
    // Music Activity와의 변수 공유를 위해 static으로 설정
    public static int FOREGROUND_SERVICE = 6888;
    public static MediaPlayer mediaPlayer;
    public static String fileURL, file, zipFile, musicFile;
    public static int id;
    public static int resNum;
    public static int rightNum;
    public static int nextValue;
    public static int repeat;
    public static int shuffle;
    public static int notifi;
    public static int isResume;
    public static boolean isZip, isLyrics, isMove, isNext, isContext;
    public static LinkedHashMap<Integer, Integer> rightM = new LinkedHashMap<>();
    public static Toast mToast;
    public static MusicDB musicDB;
    public static Cursor res;
    public static final int[] imgs2 = {R.drawable.interviewer_2, R.drawable.kimino_2, R.drawable.amano_2, R.drawable.lostone_2, R.drawable.losttime_2, R.drawable.bakawa_2, R.drawable.seisou_2,
            R.drawable.koshi_2, R.drawable.okacha_2, R.drawable.outer_2, R.drawable.seki_2, R.drawable.donut_2, R.drawable.sekai_2, R.drawable.unravel_2, R.drawable.higai_2,
            R.drawable.youkai_2, R.drawable.gishi_2, R.drawable.ringo_2, R.drawable.hibikase_2, R.drawable.regret_2, R.drawable.undead_2, R.drawable.ame_2, R.drawable.cutter_2,
            R.drawable.ima_2, R.drawable.for_2, R.drawable.oni_2, R.drawable.atta_2, R.drawable.yomo_2, R.drawable.ai_2, R.drawable.ama_2, R.drawable.yoake_2, R.drawable.leave_2,
            R.drawable.asagao_2, R.drawable.meiri_2, R.drawable.conne_2, R.drawable.tsuyuake_2, R.drawable.doku_2, R.drawable.ashita_2, R.drawable.end_2, R.drawable.pride_2,
            R.drawable.daze_2, R.drawable.ao_2, R.drawable.luvo_2, R.drawable.mousou_2, R.drawable.uso_2, R.drawable.myr_2, R.drawable.haito_2, R.drawable.alittle_2, R.drawable.shounen_2,
            R.drawable.tatoeba_2, R.drawable.yuudachi_2, R.drawable.kokoro_2, R.drawable.amao_2, R.drawable.maru_2, R.drawable.moshimo_2, R.drawable.girls_2, R.drawable.rapun_2,
            R.drawable.donor_2, R.drawable.sorega_2};
    private Notification status;
    private RemoteViews views, bigViews;
    private NotificationManager nm;
    private long temp;

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    // Notification Bar와 Music Activity의 Button Event에 따른 처리를 Action 값에 따라 처리
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (mToast == null) {
            mToast = Toast.makeText(this, "null", Toast.LENGTH_SHORT);
        }
        if (System.currentTimeMillis() > temp + 1000) {
            temp = System.currentTimeMillis();
            if (intent.getAction().equals(String.valueOf(Action.STARTFOREGROUND_ACTION))) {
                if (intent != null) {
                    id = intent.getIntExtra("id", -1);
                    nextValue = intent.getIntExtra("nextValue", -1);
                }
                notifi = 0;
                repeat = getSharedPreferences("rep", MODE_PRIVATE).getInt("rep", -1);
                shuffle = getSharedPreferences("shuf", MODE_PRIVATE).getInt("shuf", 0);
                musicDB = new MusicDB(this);
                res = musicDB.getList();
                res.moveToFirst();
                resNum = 0;
                while (id != res.getInt(res.getColumnIndex("id"))) {
                    res.moveToNext();
                    ++resNum;
                    if (resNum == res.getCount()) {
                        resNum = 0;
                        break;
                    }
                }
                musicPrepare();
            } else if (intent.getAction().equals(String.valueOf(Action.PREV_ACTION))) {
                if (isContext) {
                    notifi = Action.NOTIFI_MOVE.getAction();
                }
                if (shuffle == Action.SHUFFLE_ON.getAction()) {
                    nextValue = Action.LEFT_SHUFFLE.getAction();
                } else {
                    nextValue = Action.LEFT_NO.getAction();
                }
                musicPrepare();
            } else if (intent.getAction().equals(String.valueOf(Action.PLAY_ACTION))) {
                if (mediaPlayer.isPlaying()) {
                    notifi = Action.NOTIFI_PLAY_OFF.getAction();
                    bigViews.setImageViewResource(R.id.status_bar_play,
                            R.drawable.apollo_holo_dark_play);
                    status.bigContentView = bigViews;
                    nm.notify(FOREGROUND_SERVICE, status);
                    mediaPlayer.pause();
                } else {
                    notifi = Action.NOTIFI_PLAY_ON.getAction();
                    bigViews.setImageViewResource(R.id.status_bar_play,
                            R.drawable.apollo_holo_dark_pause);
                    status.bigContentView = bigViews;
                    nm.notify(FOREGROUND_SERVICE, status);
                    mediaPlayer.start();
                }
            } else if (intent.getAction().equals(String.valueOf(Action.NEXT_ACTION))) {
                if (isContext) {
                    notifi = Action.NOTIFI_MOVE.getAction();
                }
                if (shuffle == Action.SHUFFLE_ON.getAction()) {
                    nextValue = Action.RIGHT_SHUFFLE.getAction();
                } else {
                    nextValue = Action.RIGHT_NO.getAction();
                }
                musicPrepare();
            } else if (intent.getAction().equals(
                    String.valueOf(Action.STOPFOREGROUND_ACTION))) {
                if (isContext) {
                    notifi = Action.NOTIFI_STOP.getAction();
                } else {
                    mediaPlayer.stop();
                    mediaPlayer.pause();
                    mediaPlayer.release();
                    stopService(new Intent(getApplicationContext(), MusicService.class));
                }
            }
        }
        return START_STICKY;
    }

    private void showNotification() {
        views = new RemoteViews(getPackageName(),
                R.layout.status_bar);
        bigViews = new RemoteViews(getPackageName(),
                R.layout.status_bar_expanded);

        views.setViewVisibility(R.id.status_bar_icon, View.VISIBLE);
        views.setViewVisibility(R.id.status_bar_album_art, View.GONE);
        bigViews.setImageViewBitmap(R.id.status_bar_album_art,
                setDefaultAlbumArt(this));

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, Splash.class).putExtra("intent", 0).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                , 0);

        Intent previousIntent = new Intent(this, MusicService.class);
        previousIntent.setAction(String.valueOf(Action.PREV_ACTION));
        PendingIntent ppreviousIntent = PendingIntent.getService(this, 0,
                previousIntent, 0);

        Intent playIntent = new Intent(this, MusicService.class);
        playIntent.setAction(String.valueOf(Action.PLAY_ACTION));
        PendingIntent pplayIntent = PendingIntent.getService(this, 0,
                playIntent, 0);

        Intent nextIntent = new Intent(this, MusicService.class);
        nextIntent.setAction(String.valueOf(Action.NEXT_ACTION));
        PendingIntent pnextIntent = PendingIntent.getService(this, 0,
                nextIntent, 0);

        Intent closeIntent = new Intent(this, MusicService.class);
        closeIntent.setAction(String.valueOf(Action.STOPFOREGROUND_ACTION));
        PendingIntent pcloseIntent = PendingIntent.getService(this, 0,
                closeIntent, 0);

        views.setOnClickPendingIntent(R.id.status_bar_play, pplayIntent);
        bigViews.setOnClickPendingIntent(R.id.status_bar_play, pplayIntent);

        views.setOnClickPendingIntent(R.id.status_bar_next, pnextIntent);
        bigViews.setOnClickPendingIntent(R.id.status_bar_next, pnextIntent);

        views.setOnClickPendingIntent(R.id.status_bar_prev, ppreviousIntent);
        bigViews.setOnClickPendingIntent(R.id.status_bar_prev, ppreviousIntent);

        views.setOnClickPendingIntent(R.id.status_bar_collapse, pcloseIntent);
        bigViews.setOnClickPendingIntent(R.id.status_bar_collapse, pcloseIntent);

        views.setImageViewResource(R.id.status_bar_play,
                R.drawable.apollo_holo_dark_pause);
        bigViews.setImageViewResource(R.id.status_bar_play,
                R.drawable.apollo_holo_dark_pause);

        views.setTextViewText(R.id.status_bar_track_name, res.getString(res.getColumnIndex("name1")));
        bigViews.setTextViewText(R.id.status_bar_track_name, res.getString(res.getColumnIndex("name1")));

        views.setTextViewText(R.id.status_bar_artist_name, res.getString(res.getColumnIndex("name2")));
        bigViews.setTextViewText(R.id.status_bar_artist_name, res.getString(res.getColumnIndex("name2")));

        status = new Notification.Builder(this).build();
        status.contentView = views;
        status.bigContentView = bigViews;
        status.flags = Notification.FLAG_ONGOING_EVENT;
        status.icon = R.drawable.ic_launcher;
        status.contentIntent = pendingIntent;
        startForeground(FOREGROUND_SERVICE, status);
    }

    public Bitmap setDefaultAlbumArt(Context context) {
        Bitmap bm = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        try {
            bm = BitmapFactory.decodeResource(context.getResources(),
                    MusicService.imgs2[MusicService.id], options);
        } catch (Exception e) {
        }
        return bm;
    }

    // Notification Bar와 Music Activity의 Button Event에 따른 처리를 Action 값에 따라 처리
    // 그 후 변수에 해당 노래의 정보를 대입
    public void musicPrepare() {
        isMove = false;
        if (nextValue != 0) {
            musicDB.setFavorite();
            res = musicDB.getList();
            res.moveToFirst();
            if (res.getCount() != 0) {
                for (int i = 0; i < resNum; i++) {
                    res.moveToNext();
                }
            }
            if (resNum > res.getCount()) {
                for (int i = 0; i < resNum - res.getCount(); i++) {
                    res.moveToPrevious();
                    resNum = -1;
                }
            }
            if (nextValue == Action.RIGHT_NO.getAction()) {
                if (resNum != res.getCount() - 1) {
                    res.moveToNext();
                    ++resNum;
                }
                id = res.getInt(res.getColumnIndex("id"));
            } else if (nextValue == Action.RIGHT_SHUFFLE.getAction()) {
                if (MusicDB.fileTrue.size() == 0) {
                    mediaPlayer.pause();
                    mediaPlayer.stop();
                    stopService(new Intent(getApplicationContext(), MusicService.class));
                    if (MainActivity.lang == MainActivity.LANG_KO) {
                        mToast.setText("파일을 찾을 수 없습니다.");
                    } else if (MainActivity.lang == MainActivity.LANG_JA) {
                        mToast.setText("ファイルがありません。");
                    } else if (MainActivity.lang == MainActivity.LANG_EN) {
                        mToast.setText("File can't be found.");
                    }
                    mToast.show();
                } else {
                    boolean isNull = false;
                    rightM.put(rightNum, resNum);
                    rightNum += 1;
                    while (!isNull) {
                        res.moveToFirst();
                        resNum = 0;
                        for (int i = 0; i < (int) (Math.random() * res.getCount()); i++) {
                            res.moveToNext();
                            ++resNum;
                        }
                        file = res.getString(res.getColumnIndex("file"));
                        for (String e : MusicDB.fileTrue) {
                            if (file.equals(e)) {
                                isNull = true;
                                break;
                            }
                        }
                    }
                }
                id = res.getInt(res.getColumnIndex("id"));
            } else if (nextValue == Action.LEFT_NO.getAction()) {
                if (resNum != 0) {
                    res.moveToPrevious();
                    --resNum;
                }
                id = res.getInt(res.getColumnIndex("id"));
            } else if (nextValue == Action.LEFT_SHUFFLE.getAction()) {
                rightNum -= 1;
                int leftC = 0;
                for (Map.Entry<Integer, Integer> e : rightM.entrySet()) {
                    if (e.getKey() == rightNum) {
                        leftC = e.getValue();
                        rightM.remove(rightNum);
                        break;
                    }
                }
                if (rightNum != -1) {
                    res.moveToFirst();
                    resNum = 0;
                    for (int i = 0; i < leftC; i++) {
                        res.moveToNext();
                        ++resNum;
                    }
                } else {
                    rightNum = 0;
                }
                id = res.getInt(res.getColumnIndex("id"));
            }
        }
        nextValue = 0;
        fileURL = "http://yuyu6888.tistory.com/attachment/cfile" + res.getString(res.getColumnIndex("down"));
        file = res.getString(res.getColumnIndex("file"));
        musicFile = Music.SAVE_PATH + "/" + file + ".mp3";
        zipFile = Music.SAVE_PATH + "/" + file + ".zip";
        if (file.equals("Kimi no Shiranai Monogatari") || file.equals("Lost Time Memory") || file.equals("Daze")) {
            isZip = true;
        } else {
            isZip = false;
        }
        musicPlay();
    }

    public void musicPlay() {
        mediaPlayer = new MediaPlayer();
        new Thread() {
            @Override
            public void run() {
                showNotification();
            }
        }.start();
        if (new File(musicFile).exists()) {
            try {
                mediaPlayer.setDataSource(musicFile);
            } catch (Exception e) {
            }
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                public void onPrepared(MediaPlayer mp) {
                    mediaPlayer.start();
                    isMove = true;
                    musicEnd();
                }
            });
            mediaPlayer.prepareAsync();
        } else {
            onDestroy();
        }
    }

    public void musicEnd() {
        // 노래 재생이 끝나면 repeat, shuffle 등 경우의 수에 따라 nextValue를 설정
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                isMove = false;
                isNext = true;
                if (repeat == Action.REPEAT_ALL.getAction()) {
                    if (shuffle == Action.SHUFFLE_ON.getAction()) {
                        nextValue = Action.RIGHT_SHUFFLE.getAction();
                    } else {
                        nextValue = Action.RIGHT_NO.getAction();
                    }
                } else if (repeat == Action.REPEAT_ONE.getAction()) {
                    MusicService.nextValue = 0;
                } else {
                    stopService(new Intent(getApplicationContext(), MusicService.class));
                }
                if (!isContext) {
                    isNext = false;
                    musicPrepare();
                }
            }
        });
    }
}
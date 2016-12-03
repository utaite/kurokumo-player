package com.yuyu.kurokumoplayer.main;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.yuyu.kurokumoplayer.R;

import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Music extends Activity implements View.OnClickListener {
    public static final String SAVE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/.KurokumoPlayer";
    private TextView musicName1, musicName2, musicTime1, musicTime2, lyrics;
    private DownloadThread dThread;
    private Thread pThread;
    private CheckTypesTask task;
    private Button downButton, deleteButton, repeatButton, shuffleButton;
    private ImageButton playButton, leftButton, rightButton;
    private SeekBar seekbar;
    private ImageView imgView, favoriteView;
    private long temp;
    private int repeatTemp;
    private boolean check;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.music);
        getActionBar().hide();
        musicName1 = (TextView) findViewById(R.id.name1);
        musicName2 = (TextView) findViewById(R.id.name2);
        musicTime1 = (TextView) findViewById(R.id.time1);
        musicTime2 = (TextView) findViewById(R.id.time2);
        lyrics = (TextView) findViewById(R.id.myImageViewText);
        lyrics.setMovementMethod(new ScrollingMovementMethod());
        lyrics.getParent().requestDisallowInterceptTouchEvent(true);
        imgView = (ImageView) findViewById(R.id.music_img);
        favoriteView = (ImageView) findViewById(R.id.favorite);
        favoriteView.setOnClickListener(this);
        downButton = (Button) findViewById(R.id.button_down);
        downButton.setOnClickListener(this);
        repeatButton = (Button) findViewById(R.id.button_repeat);
        repeatButton.setOnClickListener(this);
        deleteButton = (Button) findViewById(R.id.button_delete);
        deleteButton.setOnClickListener(this);
        shuffleButton = (Button) findViewById(R.id.button_shuffle);
        shuffleButton.setOnClickListener(this);
        leftButton = (ImageButton) findViewById(R.id.button_left);
        leftButton.setOnClickListener(this);
        rightButton = (ImageButton) findViewById(R.id.button_right);
        rightButton.setOnClickListener(this);
        playButton = (ImageButton) findViewById(R.id.button_play);
        seekbar = (SeekBar) findViewById(R.id.music_pro);
        task = new CheckTypesTask();
        MusicService.isContext = true;
        MusicService.isResume = Action.RESUME_FALSE.getAction();
    }

    // isContext는 현재 Music Activity가 실행되고 있는지 구별하는 변수
    // 즐겨찾기의 예비 OFF를 OFF 시킴
    public void onEnd() {
        MusicService.isContext = false;
        MusicService.musicDB.setFavorite();
        check = false;
    }

    @Override
    protected void onPause() {
        MusicService.isResume = Action.RESUME_NONE.getAction();
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        onEnd();
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        onEnd();
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (MusicService.isResume != Action.RESUME_FALSE.getAction() || getIntent().getIntExtra("intent", -1) == 0) {
            MusicService.isResume = Action.RESUME_TRUE.getAction();
        }
        check = true;
        if (getIntent().getIntExtra("id", -1) != -1 && MusicService.isResume == Action.RESUME_FALSE.getAction()) {
            Intent Service = new Intent(this, MusicService.class);
            stopService(Service);
            Service.putExtra("id", getIntent().getIntExtra("id", -1)).putExtra("nextValue", 0).setAction(String.valueOf(Action.STARTFOREGROUND_ACTION));
            Log.e("startService", String.valueOf(Action.STARTFOREGROUND_ACTION));
            startService(Service);
        }
        musicPrepare();
    }

    public void musicPrepare() {
        new Thread() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        downButton.setEnabled(false);
                        deleteButton.setEnabled(false);
                        shuffleButton.setEnabled(false);
                        repeatButton.setEnabled(false);
                    }
                });
                new Handler(Looper.getMainLooper()) {
                    @Override
                    public void handleMessage(Message msg) {
                        musicPlay();
                    }
                }.sendEmptyMessageDelayed(0, 300);
            }
        }.start();
    }

    // Music Activity에 해당 노래의 정보를 뿌려주는 기능
    public void musicPlay() {
        if (repeatTemp != 0) {
            MusicService.repeat = repeatTemp;
            getSharedPreferences("rep", MODE_PRIVATE).edit().putInt("rep", MusicService.repeat).apply();
        } else if (MusicService.res == null) {
            MusicService.mediaPlayer.pause();
            MusicService.mediaPlayer.stop();
            stopService(new Intent(getApplicationContext(), MusicService.class));
            finish();
        }
        musicName1.setText(MusicService.res.getString(MusicService.res.getColumnIndex("name1")));
        musicName2.setText(MusicService.res.getString(MusicService.res.getColumnIndex("name2")));
        musicName1.setSelected(true);
        musicName2.setSelected(true);
        lyrics.scrollTo(0, 0);
        if (MusicService.mediaPlayer.getCurrentPosition() > 0) {
            seekbar.setMax(MusicService.mediaPlayer.getDuration());
            seekbar.setProgress(MusicService.mediaPlayer.getCurrentPosition());
            if ((MusicService.mediaPlayer.getCurrentPosition() % 60000) / 1000 < 10) {
                musicTime2.setText(MusicService.mediaPlayer.getCurrentPosition() / 60000 + ":" + "0" + String.valueOf((MusicService.mediaPlayer.getCurrentPosition() % 60000) / 1000));
            } else {
                musicTime2.setText(MusicService.mediaPlayer.getCurrentPosition() / 60000 + ":" + (MusicService.mediaPlayer.getCurrentPosition() % 60000) / 1000);
            }
        } else {
            musicTime1.setText("0:00");
            musicTime2.setText("0:00");
        }
        musicThread();
        try {
            lyrics.setText(AudioFileIO.read(new File(MusicService.musicFile)).getTag().getFirst(FieldKey.LYRICS));
        } catch (Exception e) {
        }
        if (new File(MusicService.musicFile).exists()) {
            if (MusicService.res.getInt(MusicService.res.getColumnIndex("favorite")) == 0) {
                favoriteView.setImageResource(R.drawable.ic_favorite1);
            } else {
                favoriteView.setImageResource(R.drawable.ic_favorite2);
            }
            imgView.setImageResource(MusicService.imgs2[MusicService.id]);
            imgView.setScaleType(ImageView.ScaleType.FIT_XY);
            setLyrics(MusicService.isLyrics);
            lyrics.setVisibility(View.VISIBLE);
            if (MusicService.mediaPlayer.isPlaying()) {
                playButton.setImageResource(R.drawable.apollo_holo_dark_pause);
            } else {
                playButton.setImageResource(R.drawable.apollo_holo_dark_play);
            }
            downButton.setEnabled(false);
            deleteButton.setEnabled(true);
            playButton.setEnabled(true);
            shuffleButton.setEnabled(true);
            repeatButton.setEnabled(true);
            seekbar.setMax(MusicService.mediaPlayer.getDuration());
            if ((seekbar.getMax() % 60000) / 1000 < 10) {
                musicTime1.setText(seekbar.getMax() / 60000 + ":" + "0" + Integer.toString((seekbar.getMax() % 60000) / 1000));
            } else {
                musicTime1.setText(seekbar.getMax() / 60000 + ":" + (seekbar.getMax() % 60000) / 1000);
            }
            seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser) {
                        MusicService.mediaPlayer.seekTo(progress);
                        seekbar.setProgress(progress);
                    }
                    if ((progress % 60000) / 1000 < 10) {
                        musicTime2.setText(progress / 60000 + ":" + "0" + String.valueOf((progress % 60000) / 1000));
                    } else {
                        musicTime2.setText(progress / 60000 + ":" + (progress % 60000) / 1000);
                    }
                }
            });
            playButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    startService(new Intent(getApplication(), MusicService.class).setAction(String.valueOf(Action.PLAY_ACTION)));
                    if (MusicService.mediaPlayer.isPlaying()) {
                        playButton.setImageResource(R.drawable.apollo_holo_dark_play);
                    } else {
                        playButton.setImageResource(R.drawable.apollo_holo_dark_pause);
                    }
                }
            });
            lyrics.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    setLyrics(!MusicService.isLyrics);
                    return false;
                }
            });
        } else {
            notEnabled();
            lyrics.setVisibility(View.GONE);
            playButton = (ImageButton) findViewById(R.id.button_play);
            if (MainActivity.lang == MainActivity.LANG_KO) {
                MusicService.mToast.setText("파일을 찾을 수 없습니다.");
            } else if (MainActivity.lang == MainActivity.LANG_JA) {
                MusicService.mToast.setText("ファイルがありません。");
            } else if (MainActivity.lang == MainActivity.LANG_EN) {
                MusicService.mToast.setText("File can't be found.");
            }
            MusicService.mToast.show();
        }
    }

    @Override
    public void onClick(View view) {
        if (System.currentTimeMillis() > temp + 1000) {
            temp = System.currentTimeMillis();
            if (view.getId() == R.id.button_right) {
                if (MusicService.shuffle == Action.SHUFFLE_ON.getAction()) {
                    MusicService.nextValue = Action.RIGHT_SHUFFLE.getAction();
                } else {
                    MusicService.nextValue = Action.RIGHT_NO.getAction();
                }
                musicServiceStart();
            } else if (view.getId() == R.id.button_left) {
                if (MusicService.shuffle == Action.SHUFFLE_ON.getAction()) {
                    MusicService.nextValue = Action.LEFT_SHUFFLE.getAction();
                } else {
                    MusicService.nextValue = Action.LEFT_NO.getAction();
                }
                musicServiceStart();
            }
        }
        if (view.getId() == R.id.button_delete) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT);
            if (MainActivity.lang == MainActivity.LANG_KO) {
                dialog.setMessage("정말로 삭제하시겠습니까?").setCancelable(
                        false).setPositiveButton("네",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int btnId) {
                                dialog.dismiss();
                                dialog();
                            }
                        }).setNegativeButton("아니오",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
            } else if (MainActivity.lang == MainActivity.LANG_JA) {
                dialog.setMessage("本当に削除しますか?").setCancelable(
                        false).setPositiveButton("はい",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int btnId) {
                                dialog.dismiss();
                                dialog();
                            }
                        }).setNegativeButton("いいえ",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
            } else if (MainActivity.lang == MainActivity.LANG_EN) {
                dialog.setMessage("Do you really want to delete?").setCancelable(
                        false).setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int btnId) {
                                dialog.dismiss();
                                dialog();
                            }
                        }).setNegativeButton("No",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
            }
            dialog.create().show();
        } else if (view.getId() == R.id.button_repeat) {
            if (MusicService.repeat == Action.REPEAT_ALL.getAction()) {
                MusicService.repeat = Action.REPEAT_OFF.getAction();
                MusicService.mToast.setText("Repeat OFF");
                MusicService.mToast.show();
            } else if (MusicService.repeat == Action.REPEAT_ONE.getAction()) {
                MusicService.repeat = Action.REPEAT_ALL.getAction();
                MusicService.mToast.setText("Repeat ALL");
                MusicService.mToast.show();
            } else {
                MusicService.repeat = Action.REPEAT_ONE.getAction();
                MusicService.mToast.setText("Repeat ONE");
                MusicService.mToast.show();
            }
            getSharedPreferences("rep", MODE_PRIVATE).edit().putInt("rep", MusicService.repeat).apply();
        } else if (view.getId() == R.id.button_shuffle) {
            if (MusicService.shuffle == Action.SHUFFLE_ON.getAction()) {
                MusicService.shuffle = Action.SHUFFLE_OFF.getAction();
                MusicService.mToast.setText("Shuffle OFF");
                MusicService.mToast.show();
            } else {
                MusicService.shuffle = Action.SHUFFLE_ON.getAction();
                MusicService.mToast.setText("Shuffle ON");
                MusicService.mToast.show();
            }
            getSharedPreferences("shuf", MODE_PRIVATE).edit().putInt("shuf", MusicService.shuffle).apply();
        } else if (view.getId() == R.id.button_down) {
            File dir = new File(SAVE_PATH);
            if (!dir.exists()) {
                dir.mkdir();
            }
            ConnectivityManager manager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnected() || manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnected()) {
                stopService(new Intent(getApplicationContext(), MusicService.class));
                downButton.setEnabled(false);
                new File(MusicService.musicFile).delete();
                task.onPreExecute();
                if (MusicService.isZip) {
                    dThread = new DownloadThread(MusicService.fileURL, MusicService.zipFile);
                } else {
                    dThread = new DownloadThread(MusicService.fileURL, MusicService.musicFile);
                }
                dThread.start();
                MusicDB.fileTrue.add(MusicService.file);
            } else {
                if (MainActivity.lang == MainActivity.LANG_KO) {
                    MusicService.mToast.setText("인터넷 접속에 실패하였습니다.");
                } else if (MainActivity.lang == MainActivity.LANG_JA) {
                    MusicService.mToast.setText("ネットの繋がりが失敗しました。");
                } else if (MainActivity.lang == MainActivity.LANG_EN) {
                    MusicService.mToast.setText("Internet Connection Fail.");
                }
                MusicService.mToast.show();
            }
        } else if (view.getId() == R.id.favorite) {
            if (new File(MusicService.musicFile).exists()) {
                if (MusicService.res.getInt(MusicService.res.getColumnIndex("favorite")) == 1) {
                    favoriteView.setImageResource(R.drawable.ic_favorite1);
                    MusicService.musicDB.getFavorite(2, MusicService.id);
                } else {
                    favoriteView.setImageResource(R.drawable.ic_favorite2);
                    MusicService.musicDB.getFavorite(1, MusicService.id);
                }
                MusicService.res = MusicService.musicDB.getList();
                MusicService.res.moveToFirst();
                if (MusicService.res.getCount() == 1) {
                    MusicService.musicDB.setFavorite();
                    MusicService.res = MusicService.musicDB.getList();
                }
                if (MusicService.res.getCount() != 0) {
                    for (int i = 0; i < MusicService.resNum; i++) {
                        MusicService.res.moveToNext();
                    }
                } else {
                    MusicService.mediaPlayer.pause();
                    MusicService.mediaPlayer.stop();
                    stopService(new Intent(getApplicationContext(), MusicService.class));
                    if (MainActivity.lang == MainActivity.LANG_KO) {
                        MusicService.mToast.setText("파일을 찾을 수 없습니다.");
                    } else if (MainActivity.lang == MainActivity.LANG_JA) {
                        MusicService.mToast.setText("ファイルがありません。");
                    } else if (MainActivity.lang == MainActivity.LANG_EN) {
                        MusicService.mToast.setText("File can't be found.");
                    }
                    MusicService.mToast.show();
                    finish();
                }
            }
        }
    }

    // 가장 핵심적인 기능
    // 계속 돌아가는 Thread로 Activity가 꺼져도 돌고 있다가, Notification Bar의 Event를 처리하는 역할을 수행
    public void musicThread() {
        Runnable task = new Runnable() {
            public void run() {
                check = true;
                while (check && !Thread.currentThread().isInterrupted()) {
                    MusicService.isContext = true;
                    if (MusicService.notifi == Action.NOTIFI_MOVE.getAction()) {
                        MusicService.notifi = 0;
                        musicPrepare();
                        pThread.interrupt();
                    } else if (MusicService.notifi == Action.NOTIFI_STOP.getAction()) {
                        pThread.interrupt();
                        MusicService.notifi = 0;
                        MusicService.mediaPlayer.stop();
                        MusicService.mediaPlayer.pause();
                        MusicService.mediaPlayer.release();
                        stopService(new Intent(getApplicationContext(), MusicService.class));
                        finish();
                        break;
                    } else if (MusicService.notifi == Action.NOTIFI_PLAY_ON.getAction()) {
                        MusicService.notifi = 0;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                playButton.setImageResource(R.drawable.apollo_holo_dark_pause);
                            }
                        });
                    } else if (MusicService.notifi == Action.NOTIFI_PLAY_OFF.getAction()) {
                        MusicService.notifi = 0;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                playButton.setImageResource(R.drawable.apollo_holo_dark_play);
                            }
                        });
                    } else if (MusicService.isNext) {
                        if (MusicService.repeat == Action.REPEAT_ALL.getAction()) {
                            if (MusicService.shuffle == Action.SHUFFLE_ON.getAction()) {
                                MusicService.nextValue = Action.RIGHT_SHUFFLE.getAction();
                            } else {
                                MusicService.nextValue = Action.RIGHT_NO.getAction();
                            }
                            musicServiceStart();
                        } else if (MusicService.repeat == Action.REPEAT_ONE.getAction()) {
                            MusicService.nextValue = 0;
                            musicServiceStart();
                        } else {
                            stopService(new Intent(getApplicationContext(), MusicService.class));
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    playButton.setImageResource(R.drawable.button_play);
                                    playButton.setOnClickListener(new View.OnClickListener() {
                                        public void onClick(View view) {
                                            playButton.setImageResource(R.drawable.apollo_holo_dark_pause);
                                            musicServiceStart();
                                        }
                                    });
                                }
                            });
                        }
                        MusicService.isNext = false;
                        check = false;
                    } else if (!MusicService.isNext) {
                        while (MusicService.isMove && MusicService.mediaPlayer.isPlaying() && !Thread.currentThread().isInterrupted()) {
                            if (!check) {
                                pThread.interrupt();
                                break;
                            } else if (MusicService.notifi == Action.NOTIFI_MOVE.getAction()) {
                                MusicService.notifi = 0;
                                musicPrepare();
                                pThread.interrupt();
                            } else if (MusicService.notifi == Action.NOTIFI_STOP.getAction()) {
                                pThread.interrupt();
                                MusicService.notifi = 0;
                                MusicService.mediaPlayer.stop();
                                MusicService.mediaPlayer.pause();
                                MusicService.mediaPlayer.release();
                                stopService(new Intent(getApplicationContext(), MusicService.class));
                                finish();
                                break;
                            } else {
                                Log.e("musicThread", "1 second");
                                seekbar.setProgress(MusicService.mediaPlayer.getCurrentPosition());
                                try {
                                    for (int i = 0; i <= 100; i++) {
                                        if (MusicService.notifi == 0 && !Thread.currentThread().isInterrupted()) {
                                            Thread.sleep(10);
                                        } else {
                                            break;
                                        }
                                    }
                                } catch (Exception e) {
                                }
                            }
                        }
                    }
                }
            }
        };
        pThread = new Thread(task);
        pThread.start();
    }

    public void dialog() {
        pThread.interrupt();
        new File(MusicService.musicFile).delete();
        for (String e : MusicDB.fileTrue) {
            if (MusicService.file.equals(e)) {
                MusicDB.fileTrue.remove(MusicService.file);
                break;
            }
        }
        MusicService.musicDB.getFavorite(0, MusicService.id);
        notEnabled();
        lyrics.setTextColor(Color.TRANSPARENT);
        if (MainActivity.lang == MainActivity.LANG_KO) {
            MusicService.mToast.setText("성공적으로 삭제되었습니다.");
        } else if (MainActivity.lang == MainActivity.LANG_JA) {
            MusicService.mToast.setText("削除になりました。");
        } else if (MainActivity.lang == MainActivity.LANG_EN) {
            MusicService.mToast.setText("Successfully deleted.");
        }
        MusicService.mToast.show();
        seekbar.setMax(0);
        stopService(new Intent(getApplicationContext(), MusicService.class));
    }

    public void setLyrics(boolean isLyrics) {
        MusicService.isLyrics = isLyrics;
        Drawable alpha = ((ImageView) findViewById(R.id.music_img)).getDrawable();
        if (!isLyrics) {
            alpha.setAlpha(255);
            lyrics.setTextColor(Color.TRANSPARENT);
        } else {
            alpha.setAlpha(120);
            lyrics.setTextColor(Color.WHITE);
        }
    }

    public void notEnabled() {
        MusicService.mediaPlayer.seekTo(0);
        seekbar.setMax(0);
        seekbar.setProgress(0);
        MusicService.mediaPlayer.pause();
        MusicService.mediaPlayer.stop();
        imgView.setImageResource(android.R.color.transparent);
        favoriteView.setImageResource(android.R.color.transparent);
        musicTime1.setText("0:00");
        musicTime2.setText("0:00");
        downButton.setEnabled(true);
        deleteButton.setEnabled(false);
        playButton.setEnabled(false);
        shuffleButton.setEnabled(false);
        repeatButton.setEnabled(false);
    }

    public void musicServiceStart() {
        check = false;
        Intent Service = new Intent(this, MusicService.class);
        stopService(Service);
        Service.putExtra("id", MusicService.id).putExtra("nextValue", MusicService.nextValue).setAction(String.valueOf(Action.STARTFOREGROUND_ACTION));
        startService(Service);
        musicPrepare();
    }

    private class CheckTypesTask extends AsyncTask<Void, Void, Void> {

        ProgressDialog asyncDialog = new ProgressDialog(Music.this, AlertDialog.THEME_HOLO_LIGHT);

        @Override
        protected void onPreExecute() {
            asyncDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            if (MainActivity.lang == MainActivity.LANG_KO) {
                asyncDialog.setMessage("다운로드중입니다 ...");
            } else if (MainActivity.lang == MainActivity.LANG_JA) {
                asyncDialog.setMessage("ダウンロード中です 。。。");
            } else if (MainActivity.lang == MainActivity.LANG_EN) {
                asyncDialog.setMessage("Downloading ...");
            }
            asyncDialog.show();
            asyncDialog.setCancelable(false);
            asyncDialog.setCanceledOnTouchOutside(false);
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            asyncDialog.dismiss();
            super.onPostExecute(result);
        }
    }

    private class DownloadThread extends Thread {
        String ServerUrl;
        String LocalPath;

        DownloadThread(String ServerUrl, String LocalPath) {
            this.ServerUrl = ServerUrl;
            this.LocalPath = LocalPath;
        }

        @Override
        public void run() {
            BufferedInputStream bis = null;
            BufferedOutputStream bos = null;
            try {
                HttpURLConnection conn = (HttpURLConnection) new URL(ServerUrl).openConnection();
                byte[] tmpByte = new byte[conn.getContentLength()];
                bis = new BufferedInputStream(conn.getInputStream());
                bos = new BufferedOutputStream(new FileOutputStream(new File(LocalPath)));
                for (; ; ) {
                    int read = bis.read(tmpByte);
                    if (read <= 0) {
                        break;
                    }
                    bos.write(tmpByte, 0, read);
                }
                conn.disconnect();
            } catch (Exception e) {
            } finally {
                try {
                    bis.close();
                    bos.close();
                } catch (Exception e) {
                }
            }
            if (MusicService.isZip) {
                new Decompress(MusicService.zipFile, SAVE_PATH + "/").unzip();
                new File(MusicService.zipFile).delete();
            }
            zipper.sendEmptyMessageDelayed(0, 1);
        }
    }

    private class Decompress {
        private String zipFile;
        private String location;
        ZipInputStream zis;
        BufferedInputStream bis;

        public Decompress(String zipFile, String location) {
            this.zipFile = zipFile;
            this.location = location;
            dirChecker("");
        }

        public void unzip() {
            try {
                zis = new ZipInputStream(new FileInputStream(zipFile));
                ZipEntry ze;
                while ((ze = zis.getNextEntry()) != null) {
                    if (ze.isDirectory()) {
                        dirChecker(ze.getName());
                    } else {
                        bis = new BufferedInputStream(zis);
                        byte b[] = new byte[1024];
                        int n;
                        while ((n = bis.read(b, 0, 1024)) >= 0) {
                            new BufferedOutputStream(new FileOutputStream(location + ze.getName())).write(b, 0, n);
                        }
                        zis.closeEntry();
                    }
                }
            } catch (Exception e) {
            } finally {
                try {
                    bis.close();
                    zis.close();
                } catch (Exception e) {
                }
            }
        }

        public void dirChecker(String dir) {
            File f = new File(location + dir);
            if (!f.isDirectory()) {
                f.mkdirs();
            }
        }

    }

    Handler zipper = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            task.onPostExecute(null);
            repeatTemp = MusicService.repeat;
            getSharedPreferences("rep", MODE_PRIVATE).edit().putInt("rep", Action.REPEAT_ONE.getAction()).apply();
            Intent Service = new Intent(getApplicationContext(), MusicService.class);
            stopService(Service);
            Service.putExtra("id", MusicService.id).putExtra("nextValue", 0).setAction(String.valueOf(Action.STARTFOREGROUND_ACTION));
            startService(Service);
            check = true;
            musicPrepare();
        }
    };

}
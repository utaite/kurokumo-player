package com.yuyu.kurokumoplayer.main;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import com.yuyu.kurokumoplayer.R;
import com.yuyu.kurokumoplayer.adapter.DrawerAdapter;
import com.yuyu.kurokumoplayer.adapter.DrawerView;
import com.yuyu.kurokumoplayer.adapter.ExpandableAdapter;
import com.yuyu.kurokumoplayer.adapter.GroupView;
import com.yuyu.kurokumoplayer.fragment.WebViewFragment;

import java.util.ArrayList;

public class MainActivity extends Activity {
    public static int listValue, lang;
    public static final int LANG_KO = 0, LANG_JA = 1, LANG_EN = 2;
    private ExpandableListView myList;
    private ExpandableAdapter listAdapter;
    private ArrayList<GroupView> groupList = new ArrayList<>();
    private MusicDB mydb;
    private SearchView searchView;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private long backpress, timer;
    private Fragment fragment;
    private int timerValue;
    private boolean timerCheck;
    private Thread pThread;

    private class DrawerItemClickListener implements ListView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    private void selectItem(int position) {
        Bundle bundle = new Bundle();
        switch (position) {
            case 0:
                myList.setVisibility(View.VISIBLE);
                break;
            case 1:
                myList.setVisibility(View.GONE);
                fragment = new WebViewFragment();
                bundle.putString("url", "https://twitter.com/kurokumo_01");
                bundle.putBoolean("check", true);
                fragment.setArguments(bundle);
                break;
            case 2:
                if (MusicService.mediaPlayer != null) {
                    MusicService.mediaPlayer.stop();
                    MusicService.mediaPlayer.pause();
                }
                myList.setVisibility(View.GONE);
                stopService(new Intent(getApplicationContext(), MusicService.class));
                fragment = new WebViewFragment();
                bundle.putString("url", "http://www.nicovideo.jp/mylist/30546505");
                bundle.putBoolean("check", false);
                fragment.setArguments(bundle);
                break;
            case 3:
                myList.setVisibility(View.GONE);
                fragment = new WebViewFragment();
                bundle.putString("url", "https://www.instagram.com/___kurokumo");
                bundle.putBoolean("check", true);
                fragment.setArguments(bundle);
                break;
            default:
                break;
        }
        if (fragment != null) {
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
            mDrawerList.setItemChecked(position, true);
            mDrawerList.setSelection(position);
        }
        mDrawerLayout.closeDrawer(mDrawerList);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 단 한 번만 호출
        if (Loading.last != 2) {
            Loading.last = 2;
            startActivity(new Intent(getApplicationContext(), Loading.class));
        }
        mydb = new MusicDB(this);
        getActionBar().setBackgroundDrawable(new ColorDrawable(0xFF000000));
        getActionBar().setTitle("");
        getActionBar().setDisplayShowHomeEnabled(false);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
        DrawerView[] drawerItem = new DrawerView[4];
        drawerItem[0] = new DrawerView(R.drawable.ic_music, "Music");
        drawerItem[1] = new DrawerView(R.drawable.ic_twitter, "Twitter");
        drawerItem[2] = new DrawerView(R.drawable.ic_nico, "Niconico");
        drawerItem[3] = new DrawerView(R.drawable.ic_instagram, "Instagram");
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        DrawerAdapter adapter = new DrawerAdapter(this, R.layout.listview_item_row, drawerItem);
        mDrawerList.setAdapter(adapter);
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
        mDrawerList.setItemChecked(0, true);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(
                this,
                mDrawerLayout,
                R.string.drawer_open,
                R.string.drawer_close) {
            public void onDrawerClosed(View view) {
                invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                mDrawerList.bringToFront();
                mDrawerLayout.requestLayout();
                invalidateOptionsMenu();
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        listValue = getSharedPreferences("list", MODE_PRIVATE).getInt("list", 0);
        timerValue = getSharedPreferences("timer", MODE_PRIVATE).getInt("timer", 0);
        lang = getSharedPreferences("lang", MODE_PRIVATE).getInt("lang", 2);
        myList = (ExpandableListView) findViewById(R.id.expandableListView1);
        groupList = mydb.getAllMusics();
        listAdapter = new ExpandableAdapter(this, groupList);
        myList.setAdapter(listAdapter);
        myList.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            int lastClickedPosition = 0;

            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                Boolean isExpand = (!myList.isGroupExpanded(groupPosition));
                myList.collapseGroup(lastClickedPosition);
                if (isExpand) {
                    myList.expandGroup(groupPosition);
                }
                lastClickedPosition = groupPosition;
                return true;
                // Child가 펼쳐지는 Group을 1개로 고정
            }
        });
        myList.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, final int groupPosition, int childPosition,
                                        long id) {
                GroupView group = (GroupView) v.getTag();
                // Group Class의 정보를 가져올 수 있게 선언
                if (childPosition == 0) {
                    // 첫번째 Child가 Touch 되었을 경우 Group Class에서 DB의 값을 가져온 후, NicoView Class로 값을 넣어 Intent
                    stopService(new Intent(getApplicationContext(), MusicService.class));
                    Cursor res = mydb.getData(group.getId());
                    res.moveToFirst();
                    Intent intent = new Intent(getApplicationContext(), NicoView.class);
                    intent.putExtra("nico", "http://www.nicovideo.jp/watch/sm" + res.getString(res.getColumnIndex("watch")));
                    startActivity(intent);
                    // Web 실행
                } else {
                    // 두번째 Child가 Touch 되었을 경우 Group Class에서 DB의 값을 가져온 후, Music Acitivity로 값을 넣어 Intent
                    Intent intent = new Intent(getApplicationContext(), Music.class);
                    intent.putExtra("id", group.getId());
                    startActivity(intent);
                }
                return false;
            }
        });
        if (getIntent().getIntExtra("intent", -1) == 0) {
            startActivity(new Intent(getApplicationContext(), Music.class).putExtra("intent", getIntent().getIntExtra("intent", -1)));
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Music Activity 내에서 변화가 생겨도 바로 적용
        if (MusicService.mToast == null) {
            MusicService.mToast = Toast.makeText(this, "null", Toast.LENGTH_SHORT);
        }
        groupList = mydb.getAllMusics();
        listAdapter = new ExpandableAdapter(this, groupList);
        myList.setAdapter(listAdapter);
        getActionBar().show();
        // ActionBar의 SearchView를 항상 사용
        if (searchView != null) {
            searchView.clearFocus();
            listAdapter.filterData(searchView.getQuery().toString());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.searchButton));
        searchView.setQueryHint(" Music Search");
        searchView.setIconifiedByDefault(false);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                // 검색 기능 사용 및 검색 시 자동 그룹 닫기
                listAdapter.filterData(searchView.getQuery().toString());
                for (int i = 0; i < ExpandableAdapter.close; i++) {
                    myList.collapseGroup(i);
                }
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (lang == LANG_KO) {
            menu.findItem(R.id.menu_order).setTitle("정렬 설정");
            menu.findItem(R.id.menu_timer).setTitle("종료 타이머 설정");
            menu.findItem(R.id.menu_lang).setTitle("언어 설정");
        } else if (lang == LANG_JA) {
            menu.findItem(R.id.menu_order).setTitle("ソート設定");
            menu.findItem(R.id.menu_timer).setTitle("終了タイマ設定");
            menu.findItem(R.id.menu_lang).setTitle("言語設定");
        } else if (lang == LANG_EN) {
            menu.findItem(R.id.menu_order).setTitle("Sort Setting");
            menu.findItem(R.id.menu_timer).setTitle("End Timer Setting");
            menu.findItem(R.id.menu_lang).setTitle("Langugae Setting");
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_order) {
            menuSort();
        } else if (id == R.id.menu_timer) {
            menuEndTimer();
        } else if (id == R.id.menu_lang) {
            menuLang();
        } else if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Back Key와 Fragment Back Key의 Event 처리
        if ((keyCode == KeyEvent.KEYCODE_BACK) && WebViewFragment.webView != null) {
            if ((keyCode == KeyEvent.KEYCODE_BACK) && WebViewFragment.webView.canGoBack()) {
                WebViewFragment.webView.goBack();
                return true;
            }
        } else if ((keyCode == KeyEvent.KEYCODE_BACK) && System.currentTimeMillis() > backpress + 2000) {
            backpress = System.currentTimeMillis();
            if (lang == LANG_KO) {
                MusicService.mToast.setText("\'뒤로\'버튼을 한번 더 누르시면 종료됩니다.");
            } else if (lang == LANG_JA) {
                MusicService.mToast.setText("\'Back\' ボタンをもう一度押しと終了になります。");
            } else if (lang == LANG_EN) {
                MusicService.mToast.setText("Press \'Back\' again to exit.");
            }
            MusicService.mToast.show();
            return true;
        } else if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    public void menuSort() {
        // 정렬 텍스트를 배열화
        String items[] = null;
        AlertDialog.Builder ab = new AlertDialog.Builder(MainActivity.this);
        if (MainActivity.lang == MainActivity.LANG_KO) {
            items = new String[] {"최근 업로드순", "노래 이름순", "조회순", "즐겨찾기순"};
            ab.setTitle("정렬 설정");
        } else if (MainActivity.lang == MainActivity.LANG_JA) {
            items = new String[] {"最近アップロード", "歌の名前", "照会", "マイーリスト"};
            ab.setTitle("ソート設定");
        } else if (MainActivity.lang == MainActivity.LANG_EN) {
            items = new String[] {"Newest Upload", "Song Name", "Most View", "My List"};
            ab.setTitle("Sort Setting");
        }
        ab.setSingleChoiceItems(items, listValue,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int num) {
                        // listValue 값이 설정되면 DBHelper에서 값에 맞는 정렬을 사용
                        // 그 후 Activity를 재시작(Loading Activity는 실행되지 않음)
                        // listValue 값은 Preference를 통해 저장됨
                        MusicService.rightNum = 0;
                        MusicService.rightM.clear();
                        listValue = num;
                        getSharedPreferences("list", MODE_PRIVATE).edit().putInt("list", listValue).apply();
                        dialog.dismiss();
                        startActivity(new Intent(MainActivity.this, MainActivity.class));
                        finish();
                    }
                });
        ab.show();
    }

    public void menuEndTimer() {
        String items[] = null;
        AlertDialog.Builder ab = new AlertDialog.Builder(MainActivity.this);
        if (MainActivity.lang == MainActivity.LANG_KO) {
            items = new String[] {"10분 후", "30분 후", "1시간 후", "예약 취소"};
            ab.setTitle("종료 타이머 설정");
        } else if (MainActivity.lang == MainActivity.LANG_JA) {
            items = new String[] {"10分後", "30分後", "1時間後", "要約キャンセル"};
            ab.setTitle("終了タイマ設定");
        } else if (MainActivity.lang == MainActivity.LANG_EN) {
            items = new String[] {"10 Minutes", "30 Minutes", "1 Hours", "Canceled"};
            ab.setTitle("End Timer Setting");
        }
        ab.setSingleChoiceItems(items, timerValue,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int num) {
                        Runnable task = new Runnable() {
                            public void run() {
                                while (!Thread.currentThread().isInterrupted()) {
                                    if (timer <= System.currentTimeMillis()) {
                                        pThread.interrupt();
                                    }
                                }
                                if (timerCheck) {
                                    finishAffinity();
                                    stopService(new Intent(getApplicationContext(), MusicService.class));
                                    android.os.Process.killProcess(android.os.Process.myPid());
                                }
                            }
                        };
                        pThread = new Thread(task);
                        pThread.interrupt();
                        timerValue = num;
                        getSharedPreferences("timer", MODE_PRIVATE).edit().putInt("timer", timerValue).apply();
                        switch (timerValue) {
                            case 0:
                                timerCheck = true;
                                timer = System.currentTimeMillis() + 600000;
                                if (lang == LANG_KO) {
                                    MusicService.mToast.setText("10분 후에 음악이 자동으로 종료됩니다.");
                                } else if (lang == LANG_JA) {
                                    MusicService.mToast.setText("10分後で音楽が自動に終了になります。");
                                } else if (lang == LANG_EN) {
                                    MusicService.mToast.setText("After 10 minutes, The music shutdown.");
                                }
                                break;
                            case 1:
                                timerCheck = true;
                                timer = System.currentTimeMillis() + 1800000;
                                if (lang == LANG_KO) {
                                    MusicService.mToast.setText("30분 후에 음악이 자동으로 종료됩니다.");
                                } else if (lang == LANG_JA) {
                                    MusicService.mToast.setText("30分後で音楽が自動に終了になります。");
                                } else if (lang == LANG_EN) {
                                    MusicService.mToast.setText("After 30 minutes, The music shutdown.");
                                }
                                break;
                            case 2:
                                timerCheck = true;
                                timer = System.currentTimeMillis() + 3600000;
                                if (lang == LANG_KO) {
                                    MusicService.mToast.setText("1시간 후에 음악이 자동으로 종료됩니다.");
                                } else if (lang == LANG_JA) {
                                    MusicService.mToast.setText("１時間後で音楽が自動に終了になります。");
                                } else if (lang == LANG_EN) {
                                    MusicService.mToast.setText("After 1 hours, The music shutdown.");
                                }
                                break;
                            case 3:
                                timerCheck = false;
                                if (lang == LANG_KO) {
                                    MusicService.mToast.setText("음악 종료가 취소되었습니다.");
                                } else if (lang == LANG_JA) {
                                    MusicService.mToast.setText("終了がキャンセルになります。");
                                } else if (lang == LANG_EN) {
                                    MusicService.mToast.setText("The music shutdown is canceled.");
                                }
                                break;
                            default:
                                break;
                        }
                        MusicService.mToast.show();
                        if (timerCheck) {
                            pThread.start();
                        }
                        dialog.dismiss();
                    }
                });
        ab.show();
    }

    public void menuLang() {
        String items[] = null;
        AlertDialog.Builder ab = new AlertDialog.Builder(MainActivity.this);
        if (MainActivity.lang == MainActivity.LANG_KO) {
            items = new String[] {"한국어", "일본어", "영어"};
            ab.setTitle("언어 설정");
        } else if (MainActivity.lang == MainActivity.LANG_JA) {
            items = new String[] {"韓国語", "日本語", "英語"};
            ab.setTitle("言語設定");
        } else if (MainActivity.lang == MainActivity.LANG_EN) {
            items = new String[] {"Korean", "Japanese", "English"};
            ab.setTitle("Langugae Setting");
        }
        ab.setSingleChoiceItems(items, lang,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int num) {
                        lang = num;
                        getSharedPreferences("lang", MODE_PRIVATE).edit().putInt("lang", lang).apply();
                        switch (lang) {
                            case LANG_KO:
                                lang = LANG_KO;
                                break;
                            case LANG_JA:
                                lang = LANG_JA;
                                break;
                            case LANG_EN:
                                lang = LANG_EN;
                                break;
                            default:
                                break;
                        }
                        if (lang == LANG_KO) {
                            MusicService.mToast.setText("한국어로 설정되었습니다.");
                        } else if (lang == LANG_JA) {
                            MusicService.mToast.setText("日本語に設定しました。");
                        } else if (lang == LANG_EN) {
                            MusicService.mToast.setText("It has been set to English.");
                        }
                        MusicService.mToast.show();
                        dialog.dismiss();
                    }
                });
        ab.show();
    }
}
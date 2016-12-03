package com.yuyu.kurokumoplayer.main;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.yuyu.kurokumoplayer.R;
import com.yuyu.kurokumoplayer.adapter.ChildView;
import com.yuyu.kurokumoplayer.adapter.GroupView;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;

public class MusicDB extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "1Music.db";
    public static final int DATABASE_VERSION = 1;
    public static HashSet<String> fileTrue = new HashSet<>();
    public static int finish;

    public MusicDB(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTable(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS musics");
    }

    // 현재 동영상의 조회수를 Parsing하여 DB의 값을 수정하는 기능
    public Cursor getView(SQLiteDatabase db, int i1, int i2) {
        String str;
        Cursor res = db.rawQuery("SELECT * FROM musics ORDER BY id", null);
        res.moveToFirst();
        for (int i = 0; i < i1; i++) {
            res.moveToNext();
        }
        InputStream is = null;
        BufferedReader br = null;
        for (int i = i1; i <= i2; i++) {
            try {
                is = new URL("http://ext.nicovideo.jp/api/getthumbinfo/sm" + res.getString(res.getColumnIndex("watch"))).openStream();
                br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                while ((str = br.readLine()) != null) {
                    if (str.contains("view")) {
                        db.execSQL("UPDATE musics SET view = " + str.substring(18, str.indexOf("<", 18)) + " where id = " + i);
                    }
                }
            } catch (Exception e) {
                db.execSQL("DROP TABLE IF EXISTS musics");
                android.os.Process.killProcess(android.os.Process.myPid());
            } finally {
                try {
                    is.close();
                    br.close();
                } catch(Exception e) {
                }
            }
            res.moveToNext();
        }
        return res;
    }

    public Cursor getData(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("SELECT * FROM musics WHERE id=" + id, null);
        return res;
    }

    public Cursor getList() {
        // musics란 Table이 있는지 확인한 후, 없으면 Table 생성
        // -> Table 첫 Loading시 Network가 끊기면 Table이 사라지고 팅기게 구현해놓았기 때문
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name ='musics'", null);
        if (res.getCount() == 0) {
            createTable(db);
        }
        // Main Activity의 listValue 값을 switch 문으로 나누어 어떤 정렬을 사용할 것인지 구별
        switch (MainActivity.listValue) {
            case 0:
                res = db.rawQuery("SELECT * FROM musics ORDER BY id DESC", null);
                break;
            case 1:
                res = db.rawQuery("SELECT * FROM musics ORDER BY file", null);
                break;
            case 2:
                res = db.rawQuery("SELECT * FROM musics ORDER BY view DESC", null);
                break;
            case 3:
                res = db.rawQuery("SELECT * FROM musics WHERE favorite in (1, 2) ORDER BY file", null);
                break;
            default:
                break;
        }
        return res;
    }

    public void getFavorite(int value, int id) {
        // id를 인자값으로 받아 해당 id와 일치하는 노래의 즐겨찾기를 ON/OFF 시킴
        // 0은 OFF, 1은 ON, 2는 예비 OFF
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("UPDATE musics SET favorite = " + value +  " where id = " + id);
    }

    public void setFavorite() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("UPDATE musics SET favorite = 0 where favorite = 2");
    }

    public ArrayList getAllMusics() {
        ArrayList<GroupView> groupList = new ArrayList<>();
        String name3 = "ニコニコ動画";
        String name4 = "(Niconico)";
        String name5 = "再生";
        String name6 = "(Play)";
        int[] imgs1 = {R.drawable.interviewer_1, R.drawable.kimino_1, R.drawable.amano_1, R.drawable.lostone_1, R.drawable.losttime_1, R.drawable.bakawa_1, R.drawable.seisou_1,
                R.drawable.koshi_1, R.drawable.okacha_1, R.drawable.outer_1, R.drawable.seki_1, R.drawable.donut_1, R.drawable.sekai_1, R.drawable.unravel_1, R.drawable.higai_1,
                R.drawable.youkai_1, R.drawable.gishi_1, R.drawable.ringo_1, R.drawable.hibikase_1, R.drawable.regret_1, R.drawable.undead_1, R.drawable.ame_1, R.drawable.cutter_1,
                R.drawable.ima_1, R.drawable.for_1, R.drawable.oni_1, R.drawable.atta_1, R.drawable.yomo_1, R.drawable.ai_1, R.drawable.ama_1, R.drawable.yoake_1, R.drawable.leave_1,
                R.drawable.asagao_1, R.drawable.meiri_1, R.drawable.conne_1, R.drawable.tsuyuake_1, R.drawable.doku_1, R.drawable.ashita_1, R.drawable.end_1, R.drawable.pride_1,
                R.drawable.daze_1, R.drawable.ao_1, R.drawable.luvo_1, R.drawable.mousou_1, R.drawable.uso_1, R.drawable.myr_1, R.drawable.haito_1, R.drawable.alittle_1, R.drawable.shounen_1,
                R.drawable.tatoeba_1, R.drawable.yuudachi_1, R.drawable.kokoro_1, R.drawable.amao_1, R.drawable.maru_1, R.drawable.moshimo_1, R.drawable.girls_1, R.drawable.rapun_1,
                R.drawable.donor_1, R.drawable.sorega_1};
        // Expandablelistview Setting 중 해당 File이 있으면 true를 'fileTrue' 라는 HashSet에 대입
        // -> Music Activity에서 Shuffle을 돌릴 때, true 값인 노래가 나올 때 까지 Cursor가 Random으로 돌기 때문
        Cursor res = getList();
        res.moveToFirst();
        for (int i = 0; i < res.getCount(); i++) {
            if (new File(Music.SAVE_PATH + "/" + res.getString(res.getColumnIndex("file")) + ".mp3").exists()) {
                fileTrue.add(res.getString(res.getColumnIndex("file")));
            }
            ArrayList<ChildView> child = new ArrayList<>();
            child.add(new ChildView(name3, name4));
            child.add(new ChildView(name5, name6));
            groupList.add(new GroupView(res.getInt(res.getColumnIndex("id")), imgs1[res.getInt(res.getColumnIndex("id"))], res.getString(res.getColumnIndex("name1")), res.getString(res.getColumnIndex("name2")), child));
            res.moveToNext();
        }
        return groupList;
    }

    private void createTable(final SQLiteDatabase db) {
        // id = 업로드 순서, name1 = 일본어 이름, name2 = 영어 및 한국어 이름, file = 파일명, watch = 동영상 링크, down = 다운로드 링크, view = 동영상 조회수, favorite = 즐겨찾기 ON/OFF
        db.execSQL("CREATE TABLE musics " +
                "(id INTEGER PRIMARY KEY, " +
                "name1 TEXT, " +
                "name2 TEXT, " +
                "file TEXT, " +
                "watch TEXT," +
                "down TEXT, " +
                "view INTEGER, " +
                "favorite INTERGER)"
        );
        db.execSQL("INSERT INTO musics (id, name1, name2, file, watch, down) VALUES " +
                "(0, 'インタビュア', '(Interviewer - 인터뷰어)', 'Interviewer', '17999838', '21.uf@2327065057BF01642346C1')," +
                "(1, '君の知らない物語', '(Kimi no Shiranai Monogatari - 네가 모르는 이야기)', 'Kimi no Shiranai Monogatari', '18841018', '10.uf@273B0A5057BF02C9113D0F')," +
                "(2, '天ノ弱', '(Ama no Jaku - 천성의 약함)', 'Ama no Jaku', '19934014', '29.uf@263E684757BF0361109F6B')," +
                "(3, 'ロストワンの号哭', '(Lost One no Goukoku - 로스트 원의 호곡)', 'Lost One no Goukoku', '20289162', '25.uf@235CD94C57BF051E304806')," +
                "(4, 'ロスタイムメモリー', '(Lost Time Memory - 로스타임 메모리)', 'Lost Time Memory', '21429361', '22.uf@2168A14D57BF054911DEC2')," +
                "(5, '馬鹿はアノマリーに憧れる', '(Baka wa Anomaly ni Akogareru - 바보는 아노마리를 동경한다)', 'Baka wa Anomaly ni Akogareru', '21545375', '30.uf@25260B4957BF084B043BDA')," +
                "(6, '聖槍爆裂ボーイ', '(Seisoubakuretsu Boy - 성창폭렬 보이)', 'Seisoubakuretsu Boy', '21778350', '24.uf@257C794E57BF08F0229CF9')," +
                "(7, '虎視眈々', '(Koshitantan - 호시탐탐)', 'Koshitantan', '22226734', '1.uf@215C314757BF09A60C507B')," +
                "(8, 'おこちゃま戦争', '(Okochama Sensou - 어린이 전쟁)', 'Okochama Sensou', '22371249', '24.uf@244EDE5057BF0A561E247B')," +
                "(9, 'アウターサイエンス', '(Outer Science - 아우터 사이언스)', 'Outer Science', '22433672', '7.uf@23241E4E57BF0ACB06ADF3')," +
                "(10, '赤心性：カマトト荒療治', '(Sekishinsei: Kamatoto Araryouzi - 적심성: 새침데기 치료)', 'Sekishinsei Kamatoto Araryouzi', '22835656', '5.uf@275DE15057BF0BBA144F0A')," +
                "(11, 'ドーナツホール', '(Donut Hole - 도넛 홀)', 'Donut Hole', '23257026', '6.uf@2316794C57BF0C2D1436AC')," +
                "(12, '世界は恋に落ちている', '(Sekai wa Koi ni Ochiteiru - 세계는 사랑에 빠져있어)', 'Sekai wa Koi ni Ochiteiru', '24176925', '23.uf@2449944857BF0C7D180D02')," +
                "(13, 'Unravel', '', 'Unravel', '24277186', '9.uf@24241E4E57BF0CCA0DFBD0')," +
                "(14, '被害妄想携帯女子(笑)', '(Higai Mousou Keitai Joshi(Wara) - 피해 망상 휴대 여자(웃음))', 'Higai Mousou Keitai Joshi', '24288214', '3.uf@2257075057BF0DCA22617F')," +
                "(15, 'ようかい体操第一', '(Youkai Taisou Daiichi - 요괴 체조 첫번째)', 'Youkai Taisou Daiichi', '24307474', '2.uf@2476794C57BF0F413B10E6')," +
                "(16, '疑心暗鬼', '(Gishinanki - 의심암귀)', 'Gishinanki', '24358206', '24.uf@267ED94A57BF0F962DBB5D')," +
                "(17, '林檎売りの泡沫少女', '(Ringo Uri no Utakata Shoujo - 사과팔이 물거품 소녀)', 'Ringo Uri no Utakata Shoujo', '24464111', '7.uf@230FB73657BF0FF61DEE9C')," +
                "(18, 'ヒビカセ', '(Hibikase - 울려퍼져라)', 'Hibikase', '24586945', '25.uf@22232C3357BF104A2E9F3C')," +
                "(19, 'Regret', '', 'Regret', '24644837', '1.uf@2709DB3A57BF109219D521')," +
                "(20, 'アンデッドエネミー', '(Undead Enemy - 언데드 에네미)', 'Undead Enemy', '23846691', '30.uf@253EA44957BF11540BDAE6')," +
                "(21, '雨き声残響', '(Amekigoe Zankyou - 빗소리 잔향)', 'Amekigoe Zankyou', '24841778', '24.uf@27022F4457BF88690A970A')," +
                "(22, 'カッターナイフ', '(Cutter Knife - 커터 나이프)', 'Cutter Knife', '24911050', '28.uf@2217DE4357BFC81B214402')," +
                "(23, '今好きになる', '(Ima Suki ni Naru - 지금 좋아하게 돼)', 'Ima Suki ni Naru', '25024924', '9.uf@260B604357BFC8602F3BFA')," +
                "(24, 'For You', '', 'For You', '25066596', '27.uf@2158E14257BFC98B2E29C1')," +
                "(25, '鬼 KYOKAN', '(Oni KYOKAN - 귀신 KYOKAN)', 'Oni KYOKAN', '25147792', '28.uf@2710EF4657BFCA912FB4ED')," +
                "(26, 'あったかいんだからぁ', '(Attakain Dakara - 따뜻하니까)', 'Attakain Dakara', '25349688', '24.uf@2747924357BFCACF0B9D0F')," +
                "(27, '夜もすがら君想ふ', '(Yomosugara Kimi Omofu - 밤새도록 널 생각해)', 'Yomosugara Kimi Omofu', '25418784', '29.uf@2457344357BFCB7F01A92F')," +
                "(28, 'アイのシナリオ', '(Ai no Scenario - 사랑의 시나리오)', 'Ai no Scenario', '25438939', '25.uf@2502FE4457BFCBB91BCAB4')," +
                "(29, '雨音ノイズ', '(Amaoto Noise - 빗소리 노이즈)', 'Amaoto Noise', '25535474', '3.uf@2729993D57BFCC3B1CB4EE')," +
                "(30, '夜明けと蛍', '(Yoake to Hotaru - 새벽과 반딧불이)', 'Yoake to Hotaru', '25656348', '22.uf@2469F83F57BFCC6C117968')," +
                "(31, 'Leave', '', 'Leave', '25879853', '27.uf@2448854357BFCCBF1AE91A')," +
                "(32, 'アサガオの散る頃に', '(Asagao no Chiru Koro ni - 나팔꽃 질 무렵에)', 'Asagao no Chiru Koro ni', '26085843', '28.uf@240F414257BFCE55288813')," +
                "(33, 'メリュー', '(Mairieux - 메류)', 'Mairieux', '26265326', '10.uf@227D1C3E57BFCEA22F41AD')," +
                "(34, 'Connecting', '', 'Connecting', '26349261', '30.uf@2308A44D57BFDBEB1A31CC')," +
                "(35, '梅雨明けの', '(Tsuyuake no - 장마가 끝난 후의)', 'Tsuyuake no', '26477554', '27.uf@2112254B57BFDC6C354D02')," +
                "(36, '毒占欲', '(Dokusenyoku - 독점욕)', 'Dokusenyoku', '26599776', '27.uf@232C934957BFDCA1219B29')," +
                "(37, '明日世界が滅ぶなら', '(Ashita Sekai ga Horobunara - 내일 세계가 멸망한다면)', 'Ashita Sekai ga Horobu nara', '26669563', '1.uf@2440774857BFDCDE317A15')," +
                "(38, 'end tree', '', 'End Tree', '26744607', '26.uf@233AF24857DD1087064F0A')," +
                "(39, 'プライド革命', '(Pride Kakumei - 프라이드 혁명)', 'Pride Kakumei', '26904732', '9.uf@252A364B57BFDD482423B7')," +
                "(40, 'daze', '', 'Daze', '26933575', '24.uf@272ED95057DD0FC92F8EFD')," +
                "(41, '青', '(Ao - 청)', 'Ao', '27077345', '22.uf@240C3A4F57BFDE3829D03B')," +
                "(42, 'LUVORATORRRRRY!', '', 'LUVORATORRRRRY', '27219907', '23.uf@2472514057BFDE8E223CB6')," +
                "(43, '妄想疾患ガール', '(Mousou Shikkan Girl - 망상 질환 걸)', 'Mousou Shikkan Girl', '27325250', '26.uf@2773684057BFDECA23BE64')," +
                "(44, 'うそつき', '(Usotsuki - 거짓말쟁이)', 'Usotsuki', '27421061', '26.uf@224DE24357BFDEF71F9581')," +
                "(45, 'わたしのアール', '(Watashi no R - 나의 R)', 'Watashi no R', '27631776', '30.uf@270BD54257BFDF582B4BA8')," +
                "(46, '廃都アトリエスタにて', '(Haito Atoriesta Nite - 폐도시 아틀리에스타에서)', 'Haito Atoriesta Nite', '27909159', '30.uf@2564144B57BFE18706FD86')," +
                "(47, 'A Little Pain', '', 'A Little Pain', '28077040', '24.uf@221F324757BFE1B90AC759')," +
                "(48, '少年と魔法のメドレー', '(Shounen to Mahou no Medley - 소년과 마법의 메들리)', 'Shounen to Mahou no Medley', '28104452', '4.uf@234FD44E57BFE46923C287')," +
                "(49, '例えば今此処に置かれた花に', '(Tatoeba Ima Koko ni Okareta Hana ni - 예를들어 지금 여기에 놓여진 꽃에)', 'Tatoeba Ima Koko ni Okareta Hana ni', '28155601', '30.uf@210DED4857BFE4AB1C3C23')," +
                "(50, '夕立のりぼん', '(Yuudachi no Ribbon - 소나기의 리본)', 'Yuudachi no Ribbon', '28395018', '5.uf@241D3F4857BFE4D812430B')," +
                "(51, '心とかいう名前の未発見の臓器の機能についての考察', '(Kokoro Toka Iu Namae no Mihakken no Zouki no Kinou ni Tsuite no Kousatsu - 마음이라는 이름의 미발견의 장기의 기능에 대한 고찰)', 'Kokoro Toka Iu Namae no Mihakken no Zouki no Kinou ni Tsuite no Kousatsu', '28478997', '28.uf@214A464D57BFE521158CDB')," +
                "(52, '雨音ペトリコール', '(Amaoto Petrichor - 빗소리 페트리코)', 'Amaoto Petrichor', '28761490', '8.uf@2220935057BFE5521A7D8D')," +
                "(53, '丸の内サディスティック', '(Marunouchi Sadistic - 마루노우치 새디스틱)', 'Marunouchi Sadistic', '28877702', '22.uf@255E094458080EB10EC1E5')," +
                "(54, 'もしも一人残されて界が嘘じゃないなら', '(Moshimo Hitori Nokosarete, Sekai ga Uso ja nai Nara - 만일 혼자 남겨진 세계가 거짓이 아니라면)', 'Moshimo Hitori Nokosarete, Sekai ga Uso ja nai Nara', '29049325', '3.uf@22274F4757BFE5B91C1A20')," +
                "(55, 'ガールズトーク', '(Girls Talk - 걸즈 토크)', 'Girls Talk', '29216441', '24.uf@2704114957BFE69D0ED8F8')," +
                "(56, 'ラプンツェル', '(Rapunzel - 라푼젤)', 'Rapunzel', '29280551', '30.uf@234CBD4A57BFE70E21A6BE')," +
                "(57, 'ドナーソング', '(Donor Song - 도너 송)', 'Donor Song', '29568654', '4.uf@2439554C57E0F0151AE664')," +
                "(58, 'それがあなたの幸せとしても', '(Sore ga Anata no Shiawase Toshitemo - 그것이 당신의 행복이라 할지라도)', 'Sore ga Anata no Shiawase Toshitemo', '29620980', '30.uf@2644C44657E0F5AE087766')");

        new Thread() {
            public void run() {
                getView(db, 0, 15);
                ++finish;
            }
        }.start();
        new Thread() {
            public void run() {
                getView(db, 16, 30);
                ++finish;
            }
        }.start();
        new Thread() {
            public void run() {
                getView(db, 31, 45);
                ++finish;
            }
        }.start();
        new Thread() {
            public void run() {
                getView(db, 46, 58);
                ++finish;
            }
        }.start();
    }
}
Kurokumo Player [개인 프로젝트]
=
#### 우타이테 '쿠로쿠모'의 노래를 다운로드 및 감상할 수 있는 어플리케이션입니다.

- 다운로드 : https://github.com/Utaite/kurokumo-player/blob/master/readme/apk/com.yuyu.kurokumoplayer.apk


<img width="1000" height="70" src="/readme/image/kurokumo-player-ps.png"/>

<div>
<img width="140" height="250" src="/readme/image/kurokumo-player-1.png"/>
<img width="140" height="250" src="/readme/image/kurokumo-player-2.png"/>
<img width="140" height="250" src="/readme/image/kurokumo-player-3.png"/>
<img width="140" height="250" src="/readme/image/kurokumo-player-4.png"/>
<img width="140" height="250" src="/readme/image/kurokumo-player-5.png"/>
<img width="140" height="250" src="/readme/image/kurokumo-player-6.png"/>

</div>


# 1. 개발환경

- #### 개발 기간 : 2015.11 ~ 2016.10

- #### 개발 언어 : JAVA

- #### IDE : Android Studio

- #### 라이브러리 : JAudiotagger

- #### 사용 기술 : SQLite


# 2. 개발요약


- #### 1. 우타이테 ‘쿠로쿠모’의 노래를 다운로드 및 감상할 수 있는 어플리케이션입니다.

- #### 2. 안드로이드 내장 DB인 SQLite를 사용하여 데이터를 관리하였고, 노래 검색, 노래 즐겨찾기, 노래 재생(셔플 및 반복), 상단 바를 이용한 노래 컨트롤 등이 지원됩니다.

- #### 3. mp3 파일의 가사정보를 읽어오기 위해 JAudiotagger 라이브러리를 사용하였습니다.


# 3. 상세내용


## 3.1 메인화면

- #### 1. SQLite의 데이터를 ExpandableListView 를 사용하여 표시했고, marquee 옵션을 적용했습니다.
- #### 2. 노래를 클릭하면 웹페이지의 동영상으로 감상할 것인지, 다운로드 받아 감상할 것인지 선택할 수 있습니다.
- #### 3. 한글 및 영어로 제목을 검색할 수 있습니다.
- #### 4. 노래를 최근 업로드순, 노래 이름순, 조회순(동영상 링크에서 파싱), 즐겨찾기 순으로 정렬할 수 있습니다.
- #### 5. 시간에 따라 종료 타이머를 설정할 수 있습니다.
- #### 6. 언어를 한국어, 일본어, 영어로 설정할 수 있습니다.
- #### 7. 해당 우타이테의 트위터, 인스타그램, 니코니코동화 사이트를 네비게이션 드로워(프래그먼트 처리)로 확인할 수 있습니다.
- #### 8. 메인화면이 표시되기 전, 3초간 스플래시 화면이 등장합니다.

## 3.2 노래화면

- #### 1. 다운로드, 삭제, 정지, 재생, 다음 노래, 이전 노래, 셔플 선택(ON / OFF), 반복 선택(ALL, ONE, OFF)을 지원합니다.
- #### 2. 노래 재생 시 상단 바에 표시되며, 정지, 재생, 다음 노래, 이전 노래로 컨트롤 할 수 있습니다.
- #### 3. 앨범 우측 하단의 별표 표시로 즐겨찾기를 ON / OFF 할 수 있습니다.
- #### 4. 쓰레드를 이용해 1초마다 시크바에 현재 위치가 표시되며, 프로그레스를 컨트롤하여 시간을 조절할 수 있습니다.


# 4. 기타사항


- #### 1. 구글 플레이 스토어에서 다운로드 수 약 7천을 기록했지만, 추후 저작권 문제로 배포가 중지되었습니다.

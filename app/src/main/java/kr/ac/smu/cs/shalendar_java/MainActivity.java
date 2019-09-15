package kr.ac.smu.cs.shalendar_java;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.Future;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.CalendarMode;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;

import static kr.ac.smu.cs.shalendar_java.CodeNumber.PICK_IMAGE_REQUEST;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

//    ArrayList<MainPlanItem> mainRecyclerList;

    private TextView textViewTitle;
    private TextView selectedDate;
    private Button buttonToBoard;
    private Button buttonToRegisterPlan;


    //UserToken
    private String userToken;

    private String imageURL;

    private ImageView imageView;

    //static
    public static int cid;
    public static String calName;

    //7-17
    boolean isPageOpen = false;

    Animation translateUpAnim;
    Animation translateDownAnim;

    LinearLayout main_animation;
    LinearLayout calendarLinear;
    FrameLayout calendarFrame;
    RelativeLayout calendarRelative;

    //js
    private Context mContext = MainActivity.this;

    private ViewGroup mainLayout;   //사이드 나왔을때 클릭방지할 영역
    private ViewGroup viewLayout;   //전체 감싸는 영역
    private ViewGroup sideLayout;   //사이드바만 감싸는 영역
    private ViewGroup calendarLayout; //달력레이아웃 부분
    private ImageButton mainanidownarrow;  //메인화면에서 애니메이션 닫는 이미지버튼

    private Boolean isMenuShow = false;
    private Boolean isExitFlag = false;

    //materialCalendar
    String time, kcal, menu;
    private final OneDayDecorator oneDayDecorator = new OneDayDecorator();
    MaterialCalendarView materialCalendarView;
    Cursor cursor;


    //통신
    NetWorkUrl url = new NetWorkUrl();

    //서버로 받은 것.
    ArrayList<ScheduleData> schedList = new ArrayList<>();

    //달력에 표시할 dot을 가지는 즉, 서버로 부터 응답받은 날짜들
    //date에는 startDate와 endDate가 다를 경우, 사이의 date들도 가진다.
    ArrayList<CalendarDay> dates = new ArrayList<>();

    //dates ArrayList에서 시작 날짜 == 끝 날짜  시작 날짜 != 끝날짜 경계 index
    public int boundary_index;

    //달력에 점 찍을 data들
    private HashMap<CalendarDay, Integer> map = new HashMap<>();

    //startDate와 endDate를 String에서 CalendarDay형으로 바꾼다.
    //이외의 schedList의 각 data들은 동일. 즉 나머지는 그냥 복사.
    private ArrayList<CalendarScheduleData> csdList = new ArrayList<>();


    RecyclerView mainRecyclerView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        SharedPreferences pref = getSharedPreferences("pref_USERTOKEN", MODE_PRIVATE);
        userToken = pref.getString("userToken", "NO_TOKEN");
        Log.i("Main화면::넘겨받은 토큰", userToken);

        //갤러리 접근 허용 체크.
        checkPermissions();

        textViewTitle = (TextView) findViewById(R.id.calendarNameTextView);
        buttonToBoard = (Button) findViewById(R.id.main_toBoard_button);
        selectedDate = (TextView) findViewById(R.id.TextView1);
        buttonToRegisterPlan = (Button) findViewById(R.id.main_ToRegister_button);

        //07-17
        main_animation = findViewById(R.id.main_anipage);
        calendarFrame = findViewById(R.id.calendarFrame);
        calendarRelative = findViewById(R.id.main_relative);

        //애니메이션
        mainanidownarrow=findViewById(R.id.main_downarrow);

        translateUpAnim = AnimationUtils.loadAnimation(this, R.anim.maintranslate_up);
        translateDownAnim = AnimationUtils.loadAnimation(this, R.anim.maintranslate_down);

        MainActivity.SlidingPageAnimationListner animListener = new MainActivity.SlidingPageAnimationListner();
        translateUpAnim.setAnimationListener(animListener);
        translateDownAnim.setAnimationListener(animListener);

        //리사이클러 부분

//        mainRecyclerList=new ArrayList<>();


        mainRecyclerView = (RecyclerView) findViewById(R.id.mainRecyclerview);
//        mainRecyclerView.setHasFixedSize(true);
//        MainPlanAdapter m_adapter = new MainPlanAdapter(mainRecyclerList,this);
//        mainRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayout.VERTICAL, false));
//        mainRecyclerView.setAdapter(m_adapter);

        //JS
        init();

        //사이드바 설정
        addSideView();


        if (cid == 0) {
            Toast.makeText(getApplicationContext(), "달력을 먼저 선택하세요~", Toast.LENGTH_LONG).show();
            textViewTitle.setText("달력이름");
            //setContentView(R.layout.defualt_activity_main);
        }

        else {
            textViewTitle.setText(MainActivity.calName);
            //materialCalendar뷰 초기화
            initCalendarView();
            //서버로 부터 해당 달력의 일정을 가져온다.
            showAllSche();
            //서버로 부터 받은 모든 일정을 달력에 표시
            setCalendarView();
        }

        /*
          MainActivity에서 BoardActivity로 넘어간다.
        */
        buttonToBoard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cid == 0) {
                    Toast.makeText(getApplicationContext(), "달력을 먼저 선택하세요~", Toast.LENGTH_LONG).show();
                } else {
                    Intent intent = new Intent(getApplicationContext(), BoardActivity.class);
                    startActivityForResult(intent, CodeNumber.TO_BOARD_ACTIVITY);
                }
            }
        });



        /*
          MainActivity에서 RegisterPlanActivity로 넘어간다.
        */
        buttonToRegisterPlan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (cid == 0) {
                    Toast.makeText(getApplicationContext(), "달력을 먼저 선택하세요", Toast.LENGTH_LONG).show();
                } else {
                    Intent intent = new Intent(getApplicationContext(), RegisterPlanActivity.class);
                    startActivityForResult(intent, CodeNumber.TO_REGISTERPLAN_ACTIVITY);
                }
            }
        });

    }


    public void initCalendarView() {
        materialCalendarView = (MaterialCalendarView) findViewById(R.id.calendarView);
        materialCalendarView.setArrowColor(Color.parseColor("#ff6067"));
        materialCalendarView.state().edit()
                .setFirstDayOfWeek(Calendar.SUNDAY)
                .setMinimumDate(CalendarDay.from(2017, 0, 1))
                .setMaximumDate(CalendarDay.from(2020, 11, 31))
                .setCalendarDisplayMode(CalendarMode.MONTHS)
                .commit();

        materialCalendarView.addDecorators(
                new SundayDecorator(),
                new SaturdayDecorator(),
                oneDayDecorator);
    }


    public void setCalendarView() {

        for (int i = 0; i < schedList.size(); i++) {
            if (schedList.get(i).startDate.equals(schedList.get(i).endDate)) {
                try {
                    String sameDate = schedList.get(i).startDate;
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M-d");
                    Date start = sdf.parse(sameDate);

                    CalendarDay day = CalendarDay.from(start);
                    dates.add(day);


                    CalendarScheduleData csd = new CalendarScheduleData();
                    csd.sche_title = schedList.get(i).sche_title;
                    csd.cid = schedList.get(i).cid;
                    csd.sid = schedList.get(i).sid;
                    csd.sche_content = schedList.get(i).sche_content;
                    csd.startDate = day;
                    csd.startTime = schedList.get(i).startTime;
                    csd.middleDate = day;
                    csd.endDate = day;
                    csd.endTime = schedList.get(i).endTime;
                    csd.area = schedList.get(i).area;
                    csd.numberofComment = schedList.get(i).numberofComment;

                    csdList.add(csd);

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }

        //startDate == Enddate와 startDate != EndDate 경계 index값
        boundary_index = dates.size();
        Log.i("start와 end == 마지막index", Integer.toString(boundary_index));

        for (int i = 0; i < schedList.size(); i++) {

            if (!(schedList.get(i).startDate.equals(schedList.get(i).endDate))) {

                try {
                    String s_date = schedList.get(i).startDate;
                    String e_date = schedList.get(i).endDate;

                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M-d");
                    Date start = sdf.parse(s_date);
                    Date end = sdf.parse(e_date);


                    Date currentDate = start;
                    while (currentDate.compareTo(end) <= 0) {
                        CalendarDay day = CalendarDay.from(currentDate);
                        dates.add(day);

                        /////////////////////////////////////////////////
                        CalendarScheduleData csd = new CalendarScheduleData();
                        csd.sche_title = schedList.get(i).sche_title;
                        csd.cid = schedList.get(i).cid;
                        csd.sid = schedList.get(i).sid;
                        csd.sche_content = schedList.get(i).sche_content;
                        csd.startDate = CalendarDay.from(start);
                        csd.startTime = schedList.get(i).startTime;
                        csd.middleDate = day;
                        csd.endDate = CalendarDay.from(end);
                        csd.endTime = schedList.get(i).endTime;
                        csd.area = schedList.get(i).area;
                        csd.numberofComment = schedList.get(i).numberofComment;

                        csdList.add(csd);
                        //////////////////////////////////////////////////

                        Calendar c = Calendar.getInstance();
                        c.setTime(currentDate);
                        c.add(Calendar.DAY_OF_MONTH, 1);
                        currentDate = c.getTime();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }


        Log.i("들어있는 date", dates.toString());

        Log.i("csdList Size()", Integer.toString(csdList.size()));

        for (int i = 0; i < csdList.size(); i++) {
            Log.i("scheTitle", csdList.get(i).sche_title);
            Log.i("startDate", csdList.get(i).startDate.toString());
            Log.i("middleDate", csdList.get(i).middleDate.toString());
            Log.i("endDate", csdList.get(i).endDate.toString());
            Log.i("sid", Integer.toString(csdList.get(i).sid));
            Log.i("cid", Integer.toString(csdList.get(i).cid));
        }


        new ApiSimulator().executeOnExecutor(Executors.newSingleThreadExecutor());


        //////////////////////////////////////////////////////////////////////////////////////////////////////////
        materialCalendarView.setOnDateChangedListener(new OnDateSelectedListener() {

            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
                int Year = date.getYear();
                int Month = date.getMonth() + 1;
                int Day = date.getDay();

                Log.i("Year test", Year + "");
                Log.i("Month test", Month + "");
                Log.i("Day test", Day + "");


                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                Date formatDate = date.getDate();
                String selectionDate = format.format(formatDate);

                Calendar c = Calendar.getInstance();
                c.setTime(formatDate);
                int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);

                String day = Global.getDayOfWeek(dayOfWeek);


                Log.i("shot_Day test", selectionDate + "" + day);
//                materialCalendarView.clearSelection();


                insertData(date);


                ///////애니메이션 구현

                if (isPageOpen) {
                    //calendarRelative.setClickable(false);
//                    main_animation.setVisibility(View.INVISIBLE);
//                    Toast.makeText(getApplicationContext(), "열림", Toast.LENGTH_SHORT).show();
//                    main_animation.startAnimation(translateDownAnim);

                }
                    /*
                    calendarLayout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            main_animation.startAnimation(translateDownAnim);
                        }
                    });*/


                else {
                    main_animation.setVisibility(View.VISIBLE);
                    main_animation.startAnimation(translateUpAnim);
                }


                mainanidownarrow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                         main_animation.setVisibility(View.INVISIBLE);
                    Toast.makeText(getApplicationContext(), "열림", Toast.LENGTH_SHORT).show();
                    main_animation.startAnimation(translateDownAnim);
                    }
                });



                Toast.makeText(getApplicationContext(), selectionDate, Toast.LENGTH_SHORT).show();

                //TextView에 삽입
                selectedDate.setText(selectionDate);
                materialCalendarView.clearSelection();

            }
        });
    }


    public void insertData(CalendarDay date) {

        ArrayList<MainPlanItem> mainRecyclerList = new ArrayList<>();
        mainRecyclerView.setHasFixedSize(true);
        MainPlanAdapter m_adapter = new MainPlanAdapter(mainRecyclerList, this);
        mainRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayout.VERTICAL, false));
        mainRecyclerView.setAdapter(m_adapter);

        Log.i("insertDate()csdListSize", Integer.toString(csdList.size()));

        ArrayList<CalendarScheduleData> shotDayList = new ArrayList<>();
        int animationItemCount = 0;
        for (int i = 0; i < csdList.size(); i++) {
            if (date.equals(csdList.get(i).middleDate)) {
                CalendarScheduleData csd = new CalendarScheduleData();

                csd.sche_title = csdList.get(i).sche_title;
                csd.startDate = csdList.get(i).startDate;
                csd.middleDate = csdList.get(i).middleDate;
                csd.startTime = csdList.get(i).startTime;
                csd.endDate = csdList.get(i).endDate;
                csd.endTime = csdList.get(i).endTime;
                csd.sid = csdList.get(i).sid;
                shotDayList.add(csd);
                animationItemCount++;
            }
        }

        for (int i = 0; i < animationItemCount; i++) {
            MainPlanItem mitem = new MainPlanItem();

            String startDate = calendarDayToStringFormat(shotDayList.get(i).startDate);
            String endDate = calendarDayToStringFormat(shotDayList.get(i).endDate);

            String d_day = getDday(shotDayList.get(i).middleDate, shotDayList.get(i).endDate);

            mitem.setMainPlanname(shotDayList.get(i).sche_title);


            if (startDate.equals(endDate)) {
                mitem.setMainPlantime(startDate);
                mitem.setMainPlanDday("");
            } else {
                mitem.setMainPlantime(startDate + " ~ " + endDate);
                mitem.setMainPlanDday(d_day);
            }
            mitem.setSid(shotDayList.get(i).sid);

            ArrayList<MainPlanTeamIteam> mtItem = new ArrayList<>();
            for (int j = 0; j < 6; j++) {
                mtItem.add(new MainPlanTeamIteam(R.drawable.face));
            }

            mitem.setTeamPicList(mtItem);
            mainRecyclerList.add(mitem);
        }
    }


    //일정이 여러날에 걸친 경우
    //D-day보여준다.
    public String getDday(CalendarDay currentDay, CalendarDay endDay) {

        int count = 0;

        while (currentDay.isBefore(endDay)) {
            Calendar c = Calendar.getInstance();
            c.setTime(currentDay.getDate());
            c.add(Calendar.DAY_OF_MONTH, 1);
            currentDay = CalendarDay.from(c.getTime());
            count++;
        }

        if (count == 0) {
            return "D - day";
        } else
            return "D - " + Integer.toString(count);
    }


    public String calendarDayToStringFormat(CalendarDay date) {

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date formatDate = date.getDate();
        String parseDate = format.format(formatDate);

        Calendar c = Calendar.getInstance();
        c.setTime(formatDate);
        int dayofWeek = c.get(Calendar.DAY_OF_WEEK);

        String day = Global.getDayOfWeek(dayofWeek);
        String resultDate = parseDate + day;

        return resultDate;
    }


    //7.17 추가부분
    private class SlidingPageAnimationListner implements Animation.AnimationListener {
        @Override
        public void onAnimationStart(Animation animation) {
            if (isPageOpen) {
                main_animation.setVisibility(View.INVISIBLE);
                isPageOpen = false;
            } else {

                isPageOpen = true;
            }
        }

        @Override
        public void onAnimationEnd(Animation animation) {

        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    }


    private class ApiSimulator extends AsyncTask<Void, Void, List<CalendarDay>> {

        @Override
        protected List<CalendarDay> doInBackground(@NonNull Void... voids) {

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


            //dates ArrayList에 사용자 지정 날짜들이 들어있다. 2019-7-8형식.
            Log.d("들어있는 Date", dates.toString());
            return dates;
        }


        @Override
        protected void onPostExecute(@NonNull List<CalendarDay> calendarDays) {
            super.onPostExecute(calendarDays);

            if (isFinishing()) {
                return;
            }

            ArrayList<CalendarDay> tempDateList = (ArrayList<CalendarDay>) calendarDays;
//            HashMap<CalendarDay, Integer> map = new HashMap<>();

            for (int i = 0; i < tempDateList.size(); i++) {
                CalendarDay i_date = tempDateList.get(i);
                int duplicate_count = 1;

                for (int j = i + 1; j < tempDateList.size(); j++) {
                    CalendarDay j_date = tempDateList.get(j);
                    if (i_date.equals(j_date))
                        duplicate_count++;
                }

                if (!map.containsKey(i_date))
                    map.put(i_date, duplicate_count);

                Log.i(i_date.toString(), " :: " + Integer.toString(duplicate_count));
            }


            EventDecorator event, event2, event3;

            for (CalendarDay key : map.keySet()) {
                Log.i("MAP", "key : " + key.toString() + "Value : " + map.get(key));

                ArrayList<CalendarDay> dateList = new ArrayList<>();
                int duplicateDateCount = map.get(key);

                if (duplicateDateCount == 1) {
                    dateList.add(key);
                    event = new EventDecorator(1, Color.parseColor("#ff6067"), dateList, MainActivity.this);
                    materialCalendarView.addDecorator(event);
                } else if (duplicateDateCount == 2) {
                    dateList.add(key);
                    event = new EventDecorator(1, Color.parseColor("#ff6067"), dateList, MainActivity.this);
                    event2 = new EventDecorator(2, Color.parseColor("#f8c930"), dateList, MainActivity.this);
                    materialCalendarView.addDecorator(event);
                    materialCalendarView.addDecorator(event2);
                } else if (duplicateDateCount >= 3) {
                    dateList.add(key);
                    event = new EventDecorator(1, Color.parseColor("#ff6067"), dateList, MainActivity.this);
                    event2 = new EventDecorator(2, Color.parseColor("#f8c930"), dateList, MainActivity.this);
                    event3 = new EventDecorator(3, Color.parseColor("#cdcdcd"), dateList, MainActivity.this);
                    materialCalendarView.addDecorator(event);
                    materialCalendarView.addDecorator(event2);
                    materialCalendarView.addDecorator(event3);
                } else {
                    Toast.makeText(getApplicationContext(), "No Event", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private void init() {

        findViewById(R.id.btn_menu).setOnClickListener(this);
        findViewById(R.id.btn_search).setOnClickListener(this);

        mainLayout = findViewById(R.id.id_main);
        viewLayout = findViewById(R.id.fl_silde);
        sideLayout = findViewById(R.id.view_sildebar);
        calendarLayout = findViewById(R.id.calendarFrame);
    }

    private void addSideView() {

        final Sidebar sidebar = new Sidebar(mContext);
        sideLayout.addView(sidebar);
        //sidebar.setUserID(userName);

        viewLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        sidebar.setEventListener(new Sidebar.EventListener() {

            @Override
            public void btnCancel() {
                closeMenu();
            }

            @Override
            public void btnLevel1() {
                Intent intent2 = new Intent(getApplicationContext(), NoticeActivity.class);
                startActivityForResult(intent2, CodeNumber.TO_NOTICE_ACTIVITY);
            }

            @Override
            public void btnLevel2() {
                Intent intent2 = new Intent(getApplicationContext(), SettingActivity.class);
                startActivityForResult(intent2, CodeNumber.TO_SETTING_ACTIVITY);
            }

            @Override
            public void btnLevel3() {
                Intent intent2 = new Intent(getApplicationContext(), CreateCalendarActivity.class);
                //캘린더 생성 코드
                intent2.putExtra("where", 88888);
                startActivityForResult(intent2, CodeNumber.TO_CREATE_CALENDAR_ACTIVITY);
            }

            @Override
            public void btnInvited() {
                Intent intent2 = new Intent(getApplicationContext(), WaitInvite.class);
                startActivityForResult(intent2, CodeNumber.TO_WAITINVITEACTIVITY);
            }

            @Override
            public void image_profile() {
                getPictureFromGallery();
            }

        });
    }

    private void getPictureFromGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/jpg");
        try {
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }


    /*
     폰에서 사진을 지정하면 해당 사진 주소를 가져온다.
     */
    private String getPathFromURI(Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        Log.d("여기까지", "ㅇ5");
        CursorLoader loader = new CursorLoader(getApplicationContext(), contentUri, proj, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        Log.d("여기까지", "ㅇ6");

        return cursor.getString(column_index);
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {
            switch (requestCode) {
                //사진등록
                case PICK_IMAGE_REQUEST:
                    if (resultCode == RESULT_OK) {
                        imageURL = getPathFromURI(data.getData());
                        Log.d("사진 경로", imageURL);
                        imageView = findViewById(R.id.image_profile);
                        //Request to Server.
                        setUserProfileImage_Server(imageURL);

                        SharedPreferences pref = getSharedPreferences("pref_USERTOKEN", MODE_PRIVATE);
                        SharedPreferences.Editor editor = pref.edit();
                        editor.putString("img_url", imageURL);
                        editor.commit();
                    }
            }

        } catch (Exception e) {
            Toast.makeText(this, "오류가 있습니다.", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }


    /*
     사진 지정 후 서버와 통신 하는 메소드.
     서버에서 success받으면
     imageView에 set한다.
     */
    public void setUserProfileImage_Server(final String imageUrl) {

        Log.i("프로필 변경", userToken);
        File file = new File(imageUrl);

        final ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setMessage("프로필 사진 등록 중 입니다~");
        progressDialog.setProgressStyle(progressDialog.STYLE_SPINNER);
        progressDialog.show();

        Ion.with(this)
                .load("POST", url.getServerUrl() + "/imageChange")
                //.setHeader("Content-Type", "application/json")
                .setHeader("Authorization", userToken)
                .progressDialog(progressDialog)
                .setMultipartFile("file", file)
                //응답
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {

                        if (e != null) {
                            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                        } else {
                            String message = result.get("message").getAsString();

                            if (message.equals("image change success")) {
                                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();

                                imageView.setBackground(new ShapeDrawable(new OvalShape()));
                                if(Build.VERSION.SDK_INT >= 21) {
                                    imageView.setClipToOutline(true);
                                }

                                Ion.with(imageView)
                                        .centerCrop()
                                        .resize(250, 250)
                                        .load(imageUrl);

                                progressDialog.dismiss();
                            } else {
                                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                });
    }


    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    },
                    1052);
        }
    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1052: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted.
                } else {
                    // Permission denied - Show a message to inform the user that this app only works
                    // with these permissions granted
                }
                return;
            }
        }
    }


    public void closeMenu() {

        isMenuShow = false;
        Animation slide = AnimationUtils.loadAnimation(mContext, R.anim.siderbar_hidden);
        sideLayout.startAnimation(slide);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                viewLayout.setVisibility(View.GONE);
                viewLayout.setEnabled(false);
                mainLayout.setEnabled(true);
            }
        }, 450);
    }

    public void showMenu() {

        isMenuShow = true;
        Animation slide = AnimationUtils.loadAnimation(this, R.anim.sidebar_show);
        sideLayout.startAnimation(slide);
        viewLayout.setVisibility(View.VISIBLE);
        viewLayout.setEnabled(true);
        mainLayout.setEnabled(false);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_menu:
                showMenu();
                break;
            case R.id.btn_search:
                Intent intent2 = new Intent(getApplicationContext(), SearchPlanActivity.class);
                startActivity(intent2);
                break;
        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            onBackPressed();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        if (isMenuShow) {
            closeMenu();
        } else {

            if (isExitFlag) {
                finish();
            } else {

                isExitFlag = true;
                Toast.makeText(this, "뒤로가기를 한번더 누르시면 앱이 종료됩니다.", Toast.LENGTH_SHORT).show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        isExitFlag = false;
                    }
                }, 2000);
            }
        }
    }


//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode,resultCode,data);
//
//        if(requestCode == CodeNumber.TO_REGISTERPLAN_ACTIVITY) {
//            if(resultCode == RESULT_OK) {
//                //String strfirstDate = data.getStringExtra("start");
//                //String strlastDate = data.getStringExtra("last");
//                //map.put(strfirstDate,strlastDate);
//               // Log.d("시작날짜 key", strfirstDate);
//                //Log.d("종료날짜 value", map.get(strfirstDate));
//                new ApiSimulator(map).executeOnExecutor(Executors.newSingleThreadExecutor());
//            }
//        }
//    }


    public void showAllSche() {

        Ion.getDefault(this).configure().setLogging("ion-sample", Log.DEBUG);
        Ion.getDefault(this).getConscryptMiddleware().enable(false);

        final ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMessage("잠시만 기다려주세요. 해당 일정을 가져오는 중 입니다~");
        progressDialog.show();

        JsonObject json = new JsonObject();


        //////////////////////////////////////////////////////////////////////
        json.addProperty("cid", cid);
        //////////////////////////////////////////////////////////////////////


        Future ion = Ion.with(getApplicationContext())
                .load("POST", url.getServerUrl() + "/showAllSche")
                .setHeader("Content-Type", "application/json")
                .setJsonObjectBody(json)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {


                        if (e != null) {
                            Toast.makeText(getApplicationContext(), "Server Connection Error", Toast.LENGTH_LONG).show();
                        } else {
                            progressDialog.dismiss();

                            String message = result.get("message").getAsString();
                            if (message.equals("success")) {
                                JsonArray data = result.get("data").getAsJsonArray();
                                //scheduleData = new ScheduleData[data.size()];
                                //schedList = new ArrayList<>();

                                Log.i("들어있는 일정 개수", Integer.toString(data.size()));

                                String[] dateTime;
                                for (int i = 0; i < data.size(); i++) {
                                    JsonObject sched_data = data.get(i).getAsJsonObject();

                                    ScheduleData scheduleData = new ScheduleData();

                                    scheduleData.sche_title = sched_data.get("title").getAsString();
                                    scheduleData.cid = sched_data.get("cid").getAsInt();
                                    scheduleData.sid = sched_data.get("sid").getAsInt();
                                    scheduleData.sche_content = sched_data.get("sContent").getAsString();

                                    String startDateTime = sched_data.get("startDate").getAsString();
                                    dateTime = parseDateAndTime(startDateTime);
                                    scheduleData.startDate = dateTime[0];
                                    scheduleData.startTime = dateTime[1];

                                    String endDateTime = sched_data.get("endDate").getAsString();
                                    dateTime = parseDateAndTime(endDateTime);
                                    scheduleData.endDate = dateTime[0];
                                    scheduleData.endTime = dateTime[1];

                                    scheduleData.area = sched_data.get("area").getAsString();
                                    scheduleData.numberofComment = sched_data.get("numOfComments").getAsInt();

                                    schedList.add(scheduleData);

                                }

                            } else {
                                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                            }
                        }
                    }

                    public String[] parseDateAndTime(String date) {

                        String[] parseData = date.split(" ");
                        String[] result = new String[parseData.length];

                        for (int i = 0; i < parseData.length; i++) {
                            Log.i("들어있는 값", parseData[i]);
                        }
                        result[0] = parseData[0].trim();
                        result[1] = parseData[1].substring(0, 5).trim();

                        return result;
                    }
                });

        try {
            ion.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
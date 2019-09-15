package kr.ac.smu.cs.shalendar_java;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;

import android.widget.EditText;

import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.Future;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import android.os.Handler;

import static kr.ac.smu.cs.shalendar_java.CodeNumber.PICK_IMAGE_REQUEST;

/*
  일정 상세보기 Activity
  app bar의 메뉴에서 '일정 수정', '일정 삭제' 선택시
  UpdatePlan, DeletePlanActivity로 각각 넘어간다.
 */
public class PlanDetailActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener{

    SwipeRefreshLayout mSwipeRefreshLayout;//새로고침

    //UserToken
    private String userToken;

    private TextView textViewTitle;
    private Button buttonToUpdate;
    private Button buttonToDelete;
    private ImageButton replySend;
    private EditText replyInput;
    private String replyInputString;

    //js
    private Context mContext = PlanDetailActivity.this;


    //이미지 절대 경로
    private String imageURL;

    //통신 위한 url
    private NetWorkUrl url = new NetWorkUrl();


    final PlandetailAdapter plandetailAdapter = new PlandetailAdapter();

    ImageButton backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //댓글생성, 수정 구분을 위한 Flag
        final int buttonFlag = 0;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan_detail);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefresh);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        textViewTitle = (TextView) findViewById(R.id.shareCalName);
        replySend = (ImageButton) findViewById(R.id.replysend_button);
        replyInput = (EditText) findViewById(R.id.replyInput);

        textViewTitle.setText(MainActivity.calName);

        //리스트뷰
        final ListView plandetail_Listview = (ListView) findViewById(R.id.planDetail_listView);

        //헤더 삽입
        View header = getLayoutInflater().inflate(R.layout.activity_plandetailreply_header, null, false);
        plandetail_Listview.addHeaderView(header);

        final TextView schedTitle = findViewById(R.id.planDetail_title_textView);
        final TextView userName = findViewById(R.id.plandetail_header_profile_name);
        final TextView location = findViewById(R.id.plandetail_header_location);
        final TextView aboutSched = findViewById(R.id.plandetail_header_aboutSchedule);
        final TextView startToEndTime = findViewById(R.id.plandetail_header_startToEndTime);


        Intent intent = getIntent();
        intent.getStringExtra("title");

        schedTitle.append(intent.getStringExtra("schedTitle"));
        userName.setText(intent.getStringExtra("userName"));
        location.setText(intent.getStringExtra("area"));
        aboutSched.setText(intent.getStringExtra("aboutSched"));
        startToEndTime.setText(intent.getStringExtra("startToEnd"));


        //SharedPreference에 저장된 userToken가져오기.
        SharedPreferences pref = getSharedPreferences("pref_USERTOKEN", MODE_PRIVATE);
        //값이 없으면 default로 0
        userToken = pref.getString("userToken", "NO_TOKEN");
        Log.i("넘겨받은 토큰", userToken);


        //댓글 받아오는 통시 시작
        //서버와 통신. 헤더 부분의 정보를 서버응답으로 부터 온 정보들을 파싱하여 set한다.
        Ion.getDefault(this).configure().setLogging("ion-sample", Log.DEBUG);
        Ion.getDefault(this).getConscryptMiddleware().enable(false);

        JsonObject json = new JsonObject();
        json.addProperty("cid", MainActivity.cid);
        json.addProperty("sid", Global.getSid());


        //댓글읽기 코드
        onRefresh();

        plandetail_Listview.setAdapter(plandetailAdapter);
        plandetailAdapter.notifyDataSetChanged();

        //전송버튼 누를시
        replySend.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                //그냥 댓글 생성
                if (buttonFlag == 0) {

                    replyInputString = replyInput.getText().toString();
                    if (replyInputString.length() == 0) {
                        Toast.makeText(getApplicationContext(), "내용을 작성해주세요!", Toast.LENGTH_LONG).show();
                    } else {
                        //댓글 등록 통신 보내기!
                        Toast.makeText(getApplicationContext(), replyInputString, Toast.LENGTH_LONG).show();

                        //통신 준비 --> ION
                        Ion.getDefault(getApplicationContext()).configure().setLogging("ion-sample", Log.DEBUG);
                        Ion.getDefault(getApplicationContext()).getConscryptMiddleware().enable(false);

                        final JsonObject json = new JsonObject();

                        json.addProperty("cid", MainActivity.cid);
                        json.addProperty("sid", Global.getSid());
                        json.addProperty("comments", replyInputString);

                        Ion.with(getApplicationContext())
                                .load("POST", url.getServerUrl() + "/createComments")
                                .setHeader("Content-Type", "application/json")
                                .setHeader("Authorization", userToken)
                                .setJsonObjectBody(json)
                                .asJsonObject() //응답
                                .setCallback(new FutureCallback<JsonObject>() {
                                    @Override
                                    public void onCompleted(Exception e, JsonObject result) {
                                        //응답 받을 변수
                                        String userName, schedTitle, aboutSched, schedLocation;
                                        String startDate, startTime, endDate, endTime, startToEnd;

                                        if (e != null) {
                                            Toast.makeText(getApplicationContext(), "Server Connection Error!", Toast.LENGTH_LONG).show();
                                        } else {

                                            String message = result.get("message").getAsString();
                                            //서버로 부터 응답 메세지가 success이면...

                                            if (message.equals("success")) {
                                                //서버 응답 오면 로딩 창 해제
                                                Toast.makeText(getApplicationContext(), "등록성공", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(getApplicationContext(), "등록실패", Toast.LENGTH_SHORT).show();
                                            }

                                        }
                                    }
                                });

                        //댓글읽기 코드
                        plandetailAdapter.items.clear();
                        Toast.makeText(getApplicationContext(), "받아오기 시작", Toast.LENGTH_SHORT).show();


                        Ion.with(getApplicationContext())
                                .load("POST", url.getServerUrl() + "/readComments")
                                .setHeader("Content-Type", "application/json")
                                .setHeader("Authorization", userToken)
                                .setJsonObjectBody(json)
                                .asJsonObject()
                                .setCallback(new FutureCallback<JsonObject>() {
                                    @Override
                                    public void onCompleted(Exception e, JsonObject result) {

                                        String comments, id, rdate;
                                        int commentNum;

                                        if (e != null) {
                                            Toast.makeText(getApplicationContext(), "Server Connection Error!", Toast.LENGTH_LONG).show();
                                        } else {
                                            //응답 형식이 { "data":{"id":"jacob456@hanmail.net", "cid":1, "sid":10, "title":"korea"}, "message":"success"}
                                            //data: 다음에 나오는 것들도 JsonObject형식.
                                            //따라서 data를 JsonObject로 받고, 다시 이 data를 이용하여(어찌보면 JsonObject안에 또다른 JsonObject가 있는 것이다.
                                            //JSONArray가 아님. 얘는 [,]로 묶여 있어야 함.

                                            String message = result.get("message").getAsString();
                                            //서버로 부터 응답 메세지가 success이면...

                                            if (message.equals("success")) {
                                                //서버 응답 오면 로딩 창 해제
                                                // progressDialog.dismiss();

                                                //data: {} 에서 {}안에 있는 것들도 JsonObject

                                                JsonArray data = result.getAsJsonArray("data");

                                                for (int i = 0; i < data.size(); i++) {
                                                    JsonObject jsonArr1 = data.get(i).getAsJsonObject();
                                                    commentNum = jsonArr1.get("commentNum").getAsInt();
                                                    comments = jsonArr1.get("comments").getAsString();
                                                    id = jsonArr1.get("id").getAsString();
                                                    rdate = jsonArr1.get("rdate").getAsString();
                                                    plandetailAdapter.addItem(new PlandetailItem(id, rdate, comments, commentNum));
                                                    plandetailAdapter.notifyDataSetChanged();
                                                }

                                                //Log.i("result",data.get("id").getAsString());
                                            } else {

                                                Toast.makeText(getApplicationContext(), "해당 일정이 없습니다.", Toast.LENGTH_LONG).show();
                                            }

                                        }
                                    }
                                });

                        //replyInput.clearFocus();
                        //replyInput.setText("");

                        Toast.makeText(getApplicationContext(), "FLAG값"+buttonFlag, Toast.LENGTH_LONG).show();
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY,0);

                    }

                }
            }

        });


        //길게 눌렀을 때
        plandetail_Listview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener()

        {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view,
                                           final int position, long id) {
                Toast.makeText(getApplicationContext(), plandetailAdapter.items.get(position - 1).getReply_name(), Toast.LENGTH_SHORT).show();

                //플래그
                final int buttonFlag = 1;

                AlertDialog.Builder dialog = new AlertDialog.Builder(view.getContext());
                dialog.setTitle("댓글 수정/삭제");
                dialog.setMessage("댓글 수정, 삭제하십니까?")
                        .setPositiveButton("수정", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                replyInput.requestFocus();
                                //키보드 올라오는 코드
                                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);


                                replySend.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                        replyInputString = replyInput.getText().toString();

                                        //통신 준비 --> ION
                                        Ion.getDefault(getApplicationContext()).configure().setLogging("ion-sample", Log.DEBUG);
                                        Ion.getDefault(getApplicationContext()).getConscryptMiddleware().enable(false);

                                        final JsonObject json = new JsonObject();

                                        json.addProperty("cid", MainActivity.cid);
                                        json.addProperty("sid", Global.getSid());

                                        //수정될 내용
                                        json.addProperty("comments", replyInputString);
                                        json.addProperty("commentNum", plandetailAdapter.items.get(position - 1).getCommentNum());


                                        Future ion = Ion.with(getApplicationContext())
                                                .load("POST", url.getServerUrl() + "/updateComments")
                                                .setHeader("Content-Type", "application/json")
                                                .setHeader("Authorization", userToken)
                                                .setJsonObjectBody(json)
                                                .asJsonObject() //응답
                                                .setCallback(new FutureCallback<JsonObject>() {
                                                    @Override
                                                    public void onCompleted(Exception e, JsonObject result) {
                                                        //응답 받을 변수

                                                        if (e != null) {
                                                            Toast.makeText(getApplicationContext(), "Server Connection Error!", Toast.LENGTH_LONG).show();
                                                        } else {

                                                            String message = result.get("message").getAsString();
                                                            //서버로 부터 응답 메세지가 success이면...

                                                            if (message.equals("success")) {
                                                                //서버 응답 오면 로딩 창 해제
                                                                Toast.makeText(getApplicationContext(), "수정성공", Toast.LENGTH_SHORT).show();
                                                            } else {
                                                                Toast.makeText(getApplicationContext(), "수정실패", Toast.LENGTH_SHORT).show();
                                                            }

                                                        }

                                                    }

                                                });
//                                        try {
//                                            ion.get();
//                                        } catch (Exception e) {
//                                            e.printStackTrace();
//                                        }

                                        replyInput.clearFocus();
                                        replyInput.setText("");

                                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY,0);

                                        plandetailAdapter.items.clear();
                                        onRefresh();
                                    }
                                });


                                dialog.cancel();
                            }
                        })

                        .setNegativeButton("삭제", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                //통신 준비 --> ION
                                Ion.getDefault(getApplicationContext()).configure().setLogging("ion-sample", Log.DEBUG);
                                Ion.getDefault(getApplicationContext()).getConscryptMiddleware().enable(false);

                                final JsonObject json = new JsonObject();

                                json.addProperty("cid", MainActivity.cid);
                                json.addProperty("sid", Global.getSid());
                                json.addProperty("commentNum", plandetailAdapter.items.get(position - 1).getCommentNum());

                                Ion.with(getApplicationContext())
                                        .load("POST", url.getServerUrl() + "/deleteComments")
                                        .setHeader("Content-Type", "application/json")
                                        .setHeader("Authorization", userToken)
                                        .setJsonObjectBody(json)
                                        .asJsonObject() //응답
                                        .setCallback(new FutureCallback<JsonObject>() {
                                            @Override
                                            public void onCompleted(Exception e, JsonObject result) {
                                                //응답 받을 변수

                                                String userName, schedTitle, aboutSched, schedLocation;
                                                String startDate, startTime, endDate, endTime, startToEnd;

                                                if (e != null) {
                                                    Toast.makeText(getApplicationContext(), "Server Connection Error!", Toast.LENGTH_LONG).show();
                                                } else {

                                                    String message = result.get("message").getAsString();
                                                    //서버로 부터 응답 메세지가 success이면...

                                                    if (message.equals("success")) {
                                                        //서버 응답 오면 로딩 창 해제
                                                        Toast.makeText(getApplicationContext(), "삭제성공", Toast.LENGTH_SHORT).show();
                                                    } else {
                                                        Toast.makeText(getApplicationContext(), "삭제실패", Toast.LENGTH_SHORT).show();
                                                    }

                                                }
                                            }
                                        });

                                //댓글읽어오기 코드
                                //plandetailAdapter.items.clear();
                                Toast.makeText(getApplicationContext(), "받아오기 시작", Toast.LENGTH_SHORT).show();

                                plandetailAdapter.items.clear();
                                onRefresh();

                                dialog.cancel();
                            }
                        });

                plandetailAdapter.notifyDataSetChanged();
                AlertDialog alertDialog = dialog.create();
                alertDialog.show();
                return false;
            }
        });

        backButton = findViewById(R.id.btn_back);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @Override
    public void onRefresh() {
        Ion.getDefault(this).configure().setLogging("ion-sample", Log.DEBUG);
        Ion.getDefault(this).getConscryptMiddleware().enable(false);

        mSwipeRefreshLayout.setRefreshing(true);
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                plandetailAdapter.notifyDataSetChanged();

                //댓글 받아오는 통시 시작
                //서버와 통신. 헤더 부분의 정보를 서버응답으로 부터 온 정보들을 파싱하여 set한다.

                JsonObject json = new JsonObject();
                json.addProperty("cid", MainActivity.cid);
                json.addProperty("sid", Global.getSid());

                //댓글읽기 코드
                Ion.with(getApplicationContext())
                        .load("POST", url.getServerUrl() + "/readComments")
                        .setHeader("Content-Type", "application/json")
                        .setHeader("Authorization", userToken)
                        .setJsonObjectBody(json)
                        .asJsonObject()
                        .setCallback(new FutureCallback<JsonObject>() {
                            @Override
                            public void onCompleted(Exception e, JsonObject result) {

                                String comments, id, rdate;
                                int commentNum;

                                if (e != null) {
                                    Toast.makeText(getApplicationContext(), "Server Connection Error!", Toast.LENGTH_LONG).show();
                                } else {
                                    //응답 형식이 { "data":{"id":"jacob456@hanmail.net", "cid":1, "sid":10, "title":"korea"}, "message":"success"}
                                    //data: 다음에 나오는 것들도 JsonObject형식.
                                    //따라서 data를 JsonObject로 받고, 다시 이 data를 이용하여(어찌보면 JsonObject안에 또다른 JsonObject가 있는 것이다.
                                    //JSONArray가 아님. 얘는 [,]로 묶여 있어야 함.

                                    String message = result.get("message").getAsString();
                                    //서버로 부터 응답 메세지가 success이면...

                                    if (message.equals("success")) {
                                        //서버 응답 오면 로딩 창 해제
                                        // progressDialog.dismiss();

                                        //data: {} 에서 {}안에 있는 것들도 JsonObject

                                        JsonArray data = result.getAsJsonArray("data");

                                        for (int i = 0; i < data.size(); i++) {
                                            JsonObject jsonArr1 = data.get(i).getAsJsonObject();
                                            commentNum = jsonArr1.get("commentNum").getAsInt();
                                            comments = jsonArr1.get("comments").getAsString();
                                            id = jsonArr1.get("id").getAsString();
                                            rdate = jsonArr1.get("rdate").getAsString();
                                            plandetailAdapter.addItem(new PlandetailItem(id, rdate, comments, commentNum));
                                            plandetailAdapter.notifyDataSetChanged();
                                        }

                                        //Log.i("result",data.get("id").getAsString());
                                    } else {

                                        Toast.makeText(getApplicationContext(), "해당 일정이 없습니다.", Toast.LENGTH_LONG).show();
                                    }

                                }
                            }
                        });
                mSwipeRefreshLayout.setRefreshing(false);
            }
        }, 1000);
    }
}
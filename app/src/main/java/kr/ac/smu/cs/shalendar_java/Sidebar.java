package kr.ac.smu.cs.shalendar_java;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;
import static kr.ac.smu.cs.shalendar_java.CodeNumber.PICK_IMAGE_REQUEST;

public class Sidebar extends LinearLayout implements View.OnClickListener {

    //
    ArrayList<SidebarItem> calendarRecyclerList;

    public EventListener listener;

    //사용자 TOken값
    private String userToken;

    //서버 통신
    NetWorkUrl url = new NetWorkUrl();

    //
    private SidebarAdapter s_adapter;

    //
    public static String userProfile_Name;
    public static String userProfile_ID;
    public static String userProfile_ImgURL;


    public void setEventListener(EventListener l) {
        listener = l;
    }

    public interface EventListener {
        void btnCancel();

        void btnLevel1();

        void btnLevel2();

        void btnLevel3();

        void btnInvited();

        void image_profile();
    }


    public Sidebar(Context context) {
        this(context, null);
        init();
    }

    public Sidebar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.activity_sidebar, this, true);
        findViewById(R.id.btn_cancel).setOnClickListener(this);
        findViewById(R.id.btn_info).setOnClickListener(this);
        findViewById(R.id.btn_setting).setOnClickListener(this);
        findViewById(R.id.btn_add_calender).setOnClickListener(this);
        findViewById(R.id.btn_invite).setOnClickListener(this);
        findViewById(R.id.image_profile).setOnClickListener(this);
//
//        imageView = findViewById(R.id.image_profile);
//        imageView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                getPictureFromGallery();
//            }
//        });


        //사용자 ID 프로필 set
        TextView userName = findViewById(R.id.userName_textView);
        TextView userID = findViewById(R.id.userID_textView);
        ImageView imageView = findViewById(R.id.image_profile);

//        imageView.setBackground(new ShapeDrawable(new OvalShape()));
//        if(Build.VERSION.SDK_INT >= 21) {
//            imageView.setClipToOutline(true);
//        }



        SharedPreferences pref = getContext().getSharedPreferences("pref_USERTOKEN", MODE_PRIVATE);
        userProfile_Name = pref.getString("userName", "DEFAULT :: USER");
        userProfile_ID = pref.getString("userEmail", "DEFAULT :: MIND@");
        userProfile_ImgURL = pref.getString("img_url", "DEFAULT :: profile_IMAGE");


        userName.setText(userProfile_Name);
        userID.setText(userProfile_ID);


        Log.i("사용자 프로필 이미지??", userProfile_ImgURL);

        if (!(userProfile_ImgURL.equals("DEFAULT :: profile_IMAGE"))) {

            imageView.setBackground(new ShapeDrawable(new OvalShape()));
            if(Build.VERSION.SDK_INT >= 21) {
                imageView.setClipToOutline(true);
            }

            Ion.with(imageView)
                    .centerCrop()
                    .resize(250, 250)
                    .load(userProfile_ImgURL);
        } else {
            Toast.makeText(getContext(), "프로필 사진을 정해보세요~", Toast.LENGTH_SHORT).show();
            Ion.with(imageView)
                    .centerCrop()
                    .placeholder(R.drawable.profile_default)
                    .resize(250, 250);
        }





        //리사이클러
        calendarRecyclerList = new ArrayList<>();

        insertData();

        RecyclerView sidebarRecyclerView = (RecyclerView) findViewById(R.id.sideBarRecyclerView);
        sidebarRecyclerView.setHasFixedSize(true);
        s_adapter = new SidebarAdapter(sidebarRecyclerView.getContext(), calendarRecyclerList);
        sidebarRecyclerView.setLayoutManager(new LinearLayoutManager(sidebarRecyclerView.getContext(), LinearLayout.VERTICAL, false));
        sidebarRecyclerView.setAdapter(s_adapter);


        //진권 추가
        s_adapter.notifyDataSetChanged();

    }


    //서버에서 응답받은 것 파싱해서 여기에서 추가하면 된다.
    public void insertData() {

        //header로 보낼 token값 가져오기.
        SharedPreferences pref = getContext().getSharedPreferences("pref_USERTOKEN", MODE_PRIVATE);
        userToken = pref.getString("userToken", "NO_TOKEN");
        Log.i("Sharepref에 저장된 토큰", userToken);

        //통신 준비 --> ION
        Ion.getDefault(getContext()).configure().setLogging("ion-sample", Log.DEBUG);
        Ion.getDefault(getContext()).getConscryptMiddleware().enable(false);

        //여기서는 요청시 보내는 data가 없음.
        JsonObject json = new JsonObject();
        json.addProperty("", "");

        Ion.with(getContext())
                .load("POST", url.getServerUrl() + "/readAllCal")
                .setHeader("Content-Type", "application/json")
                .setHeader("Authorization", userToken)
                .setJsonObjectBody(json)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {

                        if (e != null) {
                            Toast.makeText(getContext(), "Sever Connection Error", Toast.LENGTH_LONG).show();
                        } else {
                            String message = result.get("message").getAsString();
                            Toast.makeText(getContext(), "/readAllcal::" + message, Toast.LENGTH_LONG).show();
                            parseDataFromServer(message, result);
                        }
                    }
                });
    }

    //서버 응답 처리
    public void parseDataFromServer(String message, JsonObject result) {
        if (message.equals("success")) {
            //달력정보를 JSONArray로 받는다.
            JsonArray data = result.get("data").getAsJsonArray();

            //cid값 가지는 배열
            int[] cid_Array = new int[data.size()];

            Log.i("공유달력 개수 ", Integer.toString(data.size()));


            for (int i = 0; i < data.size(); i++) { //공유달력 개수
                JsonObject innerData = data.get(i).getAsJsonObject();

                //int cid = innerData.get("cid").getAsInt();
                cid_Array[i] = innerData.get("cid").getAsInt();

                //해당 달력 내 사용자들 정보를 JSONArray로 받는다.
                JsonArray data2 = result.get("data2").getAsJsonArray();;
                JsonArray innerData2 = data2.get(i).getAsJsonArray();

                Log.i("전달 받은 cid", Integer.toString(cid_Array[i]));
                Log.i("공유달력 내부의 사용자 명수 ", Integer.toString(innerData2.size()));
//                Log.i("해당 달력에 있는 사용자이름", innerData2.get(i).getAsJsonObject().get("userName").getAsString());

                SidebarItem sitem = new SidebarItem();

                //sitem.setSenderName(userProfile_Name);
                //sitem.setSenderImg(userProfile_ImgURL);
                sitem.setCalendarName(innerData.get("calName").getAsString());
                sitem.setCalendarImage(innerData.get("img_url").getAsString());
                sitem.setCalendarContent(innerData.get("calContent").getAsString());
                sitem.setCalendar_ID(cid_Array[i]);

//                String calName = innerData.get("calName").getAsString();
//                String cal_img = innerData.get("img_url").getAsString();
//                s_adapter.addItem(new SidebarItem(calName, cal_img));


                ArrayList<SidebarTeamItem> stItem = new ArrayList<>();

                for (int j = 0; j < innerData2.size(); j++) { //공유달력 내 사용자들 명수

                    Log.i("해당 달력에 있는 사용자이름", innerData2.get(j).getAsJsonObject().get("id").getAsString());
                    String imageURL;

                    if (innerData2.get(j).getAsJsonObject().get("img_url").isJsonNull())
                        imageURL = "DEFAULT :: profile_IMAGE";
                    else
                        imageURL = innerData2.get(j).getAsJsonObject().get("img_url").getAsString();

                    stItem.add(new SidebarTeamItem(imageURL));
                }

                sitem.setTeamImageList(stItem);
                calendarRecyclerList.add(sitem);
            }
        } else {
            Toast.makeText(getContext(), "/readAllCal 현재 달력 없음", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_cancel:
                listener.btnCancel();
                break;
            case R.id.btn_info:
                listener.btnLevel1();
                break;
            case R.id.btn_setting:
                listener.btnLevel2();
                break;
            case R.id.btn_add_calender:
                listener.btnLevel3();
                break;
            case R.id.btn_invite:
                listener.btnInvited();
                break;
            case R.id.image_profile:
                listener.image_profile();
            default:
                break;
        }
    }
}

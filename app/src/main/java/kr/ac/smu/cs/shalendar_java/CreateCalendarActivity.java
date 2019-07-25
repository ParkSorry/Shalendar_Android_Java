package kr.ac.smu.cs.shalendar_java;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import java.io.File;

import static kr.ac.smu.cs.shalendar_java.CodeNumber.PICK_IMAGE_REQUEST;

/*
    캘린더 생성하는 Activity
*/
public class CreateCalendarActivity extends AppCompatActivity {

    private EditText calendarName;
    private EditText aboutCalendar;
    private Button registerCal;
    private CreateCalendarActivityDialog dialog;
    private ImageView imageView;

    //UserToken
    private String userToken;

    //통신 위한 url가져오기
    NetWorkUrl url = new NetWorkUrl();

    //이미지 절대 경로
    private String imageURL;

    //서버에 보낼 data
    private String calName;
    private String aboutCal;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_calendar);


//        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            checkPermissions();
//        }



        Ion.getDefault(this).configure().setLogging("ion-sample", Log.DEBUG);
        Ion.getDefault(this).getConscryptMiddleware().enable(false);

        SharedPreferences pref = getSharedPreferences("pref_USERTOKEN", MODE_PRIVATE);
        //값이 없으면 default로 0
        userToken = pref.getString("userToken", "NO_TOKEN");
        Log.i("Sharepref에 저장된 토큰", userToken);

        imageView = findViewById(R.id.imageView_createCal);
        calendarName = findViewById(R.id.calTitle_EditText_createCal);
        aboutCalendar = findViewById(R.id.aboutCal_EditText_createCal);

        //갤러리에서 사진 가져오기 위한 ImageView리스너 구현
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                getPictureFromGallery();
            }
        });



        registerCal = (Button) findViewById(R.id.register_complete);
        registerCal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                calName = calendarName.getText().toString().trim();
                aboutCal = aboutCalendar.getText().toString().trim();

                //File file = new File(imageURL);
                //서버 통신.

                JsonObject json = new JsonObject();

                //RequestBody 설정 JSONObjedt를 보내기 위한 준비
                //json에 담는다.
                json.addProperty("calName", calName);
                json.addProperty("calContent", aboutCal);
                json.addProperty("img_url", "");

                Ion.with(getApplicationContext())
                        .load("POST", url.getServerUrl() + "/createCal")
                        //요청 헤더 지정
                        .setHeader("Content-Type","application/json;charset=UTF-8")
                        .setHeader("Authorization", userToken)
                        .setJsonObjectBody(json)

                        //.setMultipartParameter("img_url", imageURL)

                        //응답은 JsonObject로 받겠다.
                        .asJsonObject()
                        .setCallback(new FutureCallback<JsonObject>() {
                            @Override
                            public void onCompleted(Exception e, JsonObject result) {

                                if(e != null) { //서버 연결 오류
                                    Toast.makeText(getApplicationContext(), "SEver Error", Toast.LENGTH_LONG).show();
                                }

                                else {// 서버 연결 성공 시
                                    Toast.makeText(getApplicationContext(), result.get("message").getAsString(), Toast.LENGTH_LONG).show();
                                    Dialog();
                                }
                            }
                        });
                //Dialog();
            }
        });
    }


    public void Dialog() {
        dialog = new CreateCalendarActivityDialog(CreateCalendarActivity.this,
                leftListener); // 왼쪽 버튼 이벤트
        // 오른쪽 버튼 이벤트

        //요청 이 다이어로그를 종료할 수 있게 지정함
        dialog.setCancelable(true);
        dialog.getWindow().setGravity(Gravity.CENTER);
        dialog.show();
    }

    //다이얼로그 클릭이벤트
    private View.OnClickListener leftListener = new View.OnClickListener() {
        public void onClick(View v) {
            Toast.makeText(CreateCalendarActivity.this, "버튼을 클릭하였습니다.", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        }
    };


    //로컬 폰의 갤러리에서 사진 선택
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
        String[] proj = { MediaStore.Images.Media.DATA };
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
                    Log.d("여기까지", "ㅇ3");
                    if (resultCode == RESULT_OK) {
                        Log.d("여기까지", "ㅇ4");
                        imageURL = getPathFromURI(data.getData());
                        Log.d("사진 경로", imageURL);
                        imageView.setImageURI(data.getData());
                    }

                    //주소받아오기

            }
        }catch (Exception e) {
            Toast.makeText(this, "오류가 있습니다.", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }


    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED||
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
}




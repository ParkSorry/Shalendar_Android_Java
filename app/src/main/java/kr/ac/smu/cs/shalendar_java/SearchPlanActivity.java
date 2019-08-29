package kr.ac.smu.cs.shalendar_java;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

public class SearchPlanActivity extends AppCompatActivity {

    //검색어 저장 변수.
    private String keyword;

    //사용자 token
    private String userToken;

    //서버 연동
    NetWorkUrl url = new NetWorkUrl();


    ImageButton backButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_plan);

        final EditText searchText = findViewById(R.id.search_Keyword);
        ImageView searchSchedule = findViewById(R.id.search_Schedule);


        SharedPreferences pref = getSharedPreferences("pref_USERTOKEN", MODE_PRIVATE);
        userToken = pref.getString("userToken", "NO_TOKEN");
        Log.i("Sharepref에 저장된 토큰", userToken);


        //통신 준비 --> ION
        Ion.getDefault(this).configure().setLogging("ion-sample", Log.DEBUG);
        Ion.getDefault(this).getConscryptMiddleware().enable(false);

        searchSchedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                keyword = searchText.getText().toString().trim();
                Log.i("검색어", keyword);
                if(keyword.equals(""))
                    Toast.makeText(getApplicationContext(), "검색어를 입력해주세요~", Toast.LENGTH_LONG).show();
                else {

                    final ProgressDialog progressDialog = new ProgressDialog(SearchPlanActivity.this);
                    progressDialog.setMessage("검색 중입니다~");
                    progressDialog.setProgressStyle(progressDialog.STYLE_SPINNER);
                    progressDialog.show();

                    JsonObject json = new JsonObject();
                    json.addProperty("title", keyword);

                    Ion.with(getApplicationContext())
                            .load("POST", url.getServerUrl() + "/searchSche")
                            .progressDialog(progressDialog)
                            .setHeader("Content-Type","application/json")
                            .setHeader("Authorization", userToken)
                            .setJsonObjectBody(json)
                            .asJsonObject()
                            .setCallback(new FutureCallback<JsonObject>() {

                                @Override
                                public void onCompleted(Exception e, JsonObject result) {

                                    if(e != null) {
                                        Toast.makeText(getApplicationContext(), "Sever Connection Error", Toast.LENGTH_LONG).show();
                                    }

                                    else {
                                        progressDialog.dismiss();
                                        String message = result.get("message").getAsString();
                                        parseResponse(message, result);
                                    }
                                }
                            });
                }
            }
        });



        backButton = findViewById(R.id.btn_back);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

//        Toolbar toolbar = (Toolbar) findViewById(R.id.search_toolbar);
//        setSupportActionBar(toolbar);

    }

    /*
     서버로 부터 온 응답 파싱하는 부분.
     */
    public void parseResponse(String message, JsonObject result) {

        if(message.equals("success")) {

            JsonArray data = result.get("data").getAsJsonArray();
            Log.i("JSONArray길이", Integer.toString(data.size()));

            for(int i = 0; i<data.size(); i++) {
                JsonObject data2 = data.get(i).getAsJsonObject();
                String title = data2.get("title").getAsString();
                int sid = data2.get("sid").getAsInt();

                if(title.contains(keyword))
                {
                    Log.i("파싱한 title", title);
                    Log.i("파싱한 sid", Integer.toString(sid));
                }
            }
        }

        else {
            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
        }
    }
}

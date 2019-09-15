package kr.ac.smu.cs.shalendar_java;


import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.menu.MenuView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;


public class BoarderAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<BoardPlanItem> boardList = new ArrayList<>();
    private Context context;
    private final int TYPE_HEADER = 0;
    private final int TYPE_ITEM = 1;
    private final int TYPE_FOOTER = 2;
    private BoardHeaderAdapter h_adapter;
    int sharedPeoplenum;
    JsonArray shareUserData;
    JsonObject calendarData;
    String calName;
    String calContent;
    ImageView boardHeaderImage;
    TextView boardHeadertitle;
    TextView boardHeaderContent;

    public BoarderAdapter(int sharedPeoplenum, JsonArray shareUserData, JsonObject calendarData) {
        this.sharedPeoplenum = sharedPeoplenum;
        this.shareUserData = shareUserData;
        this.calendarData = calendarData;
    }

    //사람을 받아옴


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        context = parent.getContext();
        RecyclerView.ViewHolder holder;
        View view;

        if (viewType == TYPE_HEADER) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_boardheader, parent, false);
            //추가
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context, LinearLayout.HORIZONTAL, false);
            RecyclerView h_recyclerView = view.findViewById(R.id.header_recycler);
            h_recyclerView.setLayoutManager(linearLayoutManager);

            //보더헤더 이름과 설명 설정하는 부분
            boardHeaderImage = view.findViewById(R.id.boardHeaderImage);
            boardHeadertitle = (TextView) view.findViewById(R.id.boardHeadertitle);
            boardHeaderContent = (TextView) view.findViewById(R.id.boardHeaderContent);

            Ion.with(boardHeaderImage)
                    .centerCrop()
                    .resize(350, 250)
                    .load(calendarData.get("img_url").getAsString());


            calName = calendarData.get("calName").getAsString();
            calContent = calendarData.get("calContent").getAsString();
            Toast.makeText(context, calContent, Toast.LENGTH_SHORT).show();

            boardHeadertitle.setText(calName);
            boardHeaderContent.setText(calContent);

            h_adapter = new BoardHeaderAdapter();
            h_recyclerView.setAdapter(h_adapter);

            Log.i("넘어온 명수", Integer.toString(sharedPeoplenum));

            for (int i = 0; i < sharedPeoplenum; i++) {
                JsonObject jsonArr = shareUserData.get(i).getAsJsonObject();
                //id = jsonArr.get("id").getAsString();
                String userName = jsonArr.get("userName").getAsString();
                String userImgURL;

                if (jsonArr.get("img_url").isJsonNull())
                    userImgURL = "DEFAULT :: profile_IMAGE";
                else
                    userImgURL = jsonArr.get("img_url").getAsString();

                h_adapter.addItem(new BoardTeamItem(userName, userImgURL));
            }

            Log.i("누가 먼저 실행되는 거임??11", Integer.toString(sharedPeoplenum));
            h_adapter.notifyDataSetChanged();
            holder = new HeaderViewHolder(view);


        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.boardplan_item, parent, false);
            holder = new ItemViewHolder(view);
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {
            HeaderViewHolder headerViewHolder = (HeaderViewHolder) holder;
        } else {
            ItemViewHolder itemViewHolder = (ItemViewHolder) holder;
            itemViewHolder.onBind(boardList.get(position - 1), position);
        }
    }

    @Override
    public int getItemCount() {
        return boardList.size() + 1;
    }

    void addItem(BoardPlanItem data) {
        boardList.add(data);
    }

    class ItemViewHolder extends RecyclerView.ViewHolder {

        //서버 통신
        private NetWorkUrl url = new NetWorkUrl();

        private TextView planname_text;
        private TextView plandate_text;
        private TextView planlocation_text;
        private TextView planreply_text;
        private BoardPlanItem data;
        private int position;

        public ItemViewHolder(@NonNull final View itemView) {
            super(itemView);

            plandate_text = itemView.findViewById(R.id.dateandtime);
            planname_text = itemView.findViewById(R.id.planname);
            planlocation_text = itemView.findViewById(R.id.location);
            planreply_text = itemView.findViewById(R.id.replynum);


            //통신 준비
            Ion.getDefault(itemView.getContext()).configure().setLogging("ion-sample", Log.DEBUG);
            Ion.getDefault(itemView.getContext()).getConscryptMiddleware().enable(false);

            /*
              해당 item클릭시 서버에서 정보 받고
              이 정보들을 intent로 저장 시킨 후
              PlanDetail로 남긴다.
             */

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Global.setSid(boardList.get(getAdapterPosition() - 1).getSid());
                    Toast.makeText(v.getContext(), boardList.get(getAdapterPosition() - 1).getPlanname() + "이거 수정한다잉", Toast.LENGTH_SHORT).show();

                    AlertDialog.Builder dialog = new AlertDialog.Builder(v.getContext());
                    dialog.setTitle("일정 수정/삭제");

                    dialog.setMessage("일정 수정, 삭제하십니까?")
                            .setPositiveButton("수정", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent(context, UpdatePlanActivity.class);

                                    getScheduleFromServer(Global.getSid());
//                                    intent.putExtra("sid_update", Global.getSid());
//                                    intent.putExtra("scheTitle", boardList.get(getAdapterPosition()-1).getPlanname());
//                                    intent.putExtra("startDate", boardList.get(getAdapterPosition()-1).dateandtime);
//                                    intent.putExtra("startTime", boardList.get(getAdapterPosition()-1).getPlanname());
//                                    intent.putExtra("endDate", boardList.get(getAdapterPosition()-1).getPlanname());
//                                    intent.putExtra("endTime", boardList.get(getAdapterPosition()-1).getPlanname());
//                                    intent.putExtra("aboutSche", boardList.get(getAdapterPosition()-1).ge);
//                                    intent.putExtra("location", boardList.get(getAdapterPosition()-1).getLocation());
                                    context.startActivity(intent);
                                    dialog.cancel();
                                }
                            })

                            .setNegativeButton("삭제", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    //서버와 통신.
                                    Ion.getDefault(itemView.getContext()).configure().setLogging("ion-sample", Log.DEBUG);
                                    Ion.getDefault(itemView.getContext()).getConscryptMiddleware().enable(false);

                                    JsonObject json = new JsonObject();

                                    json.addProperty("sid", Global.getSid());
                                    Log.d("ttt", "가져온 sid는 " + Global.getSid());
                                    Ion.with(itemView.getContext())
                                            .load("POST", url.getServerUrl() + "/deleteSche")
                                            .setHeader("Content-Type", "application/json")
                                            .setJsonObjectBody(json)
                                            .asJsonObject()
                                            .setCallback(new FutureCallback<JsonObject>() {
                                                @Override
                                                public void onCompleted(Exception e, JsonObject result) {

                                                    if (e != null) {
                                                        Toast.makeText(itemView.getContext(), "Server Connection Error!", Toast.LENGTH_LONG).show();
                                                    } else {

                                                        String message = result.get("message").getAsString();
                                                        //서버로 부터 응답 메세지가 success이면...

                                                        if (message.equals("success")) {
                                                            //서버 응답 오면 로딩 창 해제
                                                            Toast.makeText(itemView.getContext(), "일정 삭제 성공", Toast.LENGTH_LONG).show();

                                                        } else {
                                                            Toast.makeText(itemView.getContext(), "일정 삭제 실패", Toast.LENGTH_LONG).show();
                                                        }

                                                    }
                                                }
                                            });

                                    dialog.cancel();
                                }
                            });

                    AlertDialog alertDialog = dialog.create();
                    alertDialog.show();

                    return false;
                }
            });

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Toast.makeText(v.getContext(), boardList.get(getAdapterPosition() - 1).getPlanname(), Toast.LENGTH_SHORT).show();


                    //sid오류
                    Global.setSid(boardList.get(getAdapterPosition() - 1).getSid());
                    Log.d("어댑터sid", "sid는" + Global.getSid());

                    getScheduleFromServer(Global.getSid());
//                    JsonObject json = new JsonObject();
//
//                    json.addProperty("sid", Global.getSid());
//
//                    final ProgressDialog progressDialog = new ProgressDialog(itemView.getContext());
//                    progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
//                    progressDialog.setMessage("해당 댓글로 이동중~" + boardList.get(getAdapterPosition() - 1).getSid());
//                    progressDialog.show();
//
//
//                    Ion.with(itemView.getContext())
//                            .load("POST", url.getServerUrl() + "/showSche")
//                            .setHeader("Content-Type", "application/json")
//                            .setJsonObjectBody(json)
//                            .asJsonObject() //응답
//                            .setCallback(new FutureCallback<JsonObject>() {
//                                @Override
//                                public void onCompleted(Exception e, JsonObject result) {
//                                    //응답 받을 변수
//                                    String userName, schedTitle, aboutSched, schedLocation;
//                                    String startDate, startTime, endDate, endTime, startToEnd;
//
//                                    if (e != null) {
//                                        Toast.makeText(itemView.getContext(), "Server Connection Error!", Toast.LENGTH_LONG).show();
//                                    } else {
//                                        //응답 형식이 { "data":{"id":"jacob456@hanmail.net", "cid":1, "sid":10, "title":"korea"}, "message":"success"}
//                                        //data: 다음에 나오는 것들도 JsonObject형식.
//                                        //따라서 data를 JsonObject로 받고, 다시 이 data를 이용하여(어찌보면 JsonObject안에 또다른 JsonObject가 있는 것이다.
//                                        //JSONArray가 아님. 얘는 [,]로 묶여 있어야 함.
//
//                                        String message = result.get("message").getAsString();
//                                        //서버로 부터 응답 메세지가 success이면...
//
//                                        if (message.equals("success")) {
//                                            //서버 응답 오면 로딩 창 해제
//                                            progressDialog.dismiss();
//
//                                            //data: {} 에서 {}안에 있는 것들도 JsonObject
//                                            JsonObject data = result.get("data").getAsJsonObject();
//
//                                            userName = data.get("id").getAsString();
//                                            schedTitle = data.get("title").getAsString();
//                                            aboutSched = data.get("sContent").getAsString();
//                                            schedLocation = data.get("area").getAsString();
//                                            startDate = data.get("startDate").getAsString();
//                                            //startTime = data.get("startTime").getAsString();
//                                            endDate = data.get("endDate").getAsString();
//                                            //endTime = data.get("endTime").getAsString();
//
//                                            //뒤에 0.000 잘라내기
//                                            startDate = startDate.substring(0, 16);
//                                            endDate = endDate.substring(0, 16);
//                                            startToEnd = startDate + " ~ " + endDate;
//
//                                            Intent intent = new Intent(context, PlanDetailActivity.class);
//                                            intent.putExtra("userName", userName);
//                                            intent.putExtra("schedTitle", schedTitle);
//                                            intent.putExtra("aboutSched", aboutSched);
//                                            intent.putExtra("area", schedLocation);
//                                            intent.putExtra("startToEnd", startToEnd);
//
//                                            context.startActivity(intent);
//
//                                            Log.i("result", data.get("id").getAsString());
//                                        } else {
//
//                                            Toast.makeText(itemView.getContext(), "해당 일정이 없습니다.", Toast.LENGTH_LONG).show();
//                                        }
//
//                                    }
//                                }
//                            });

                }
            });

        }


        public void onBind(BoardPlanItem data, int position) {
            this.data = data;
            this.position = position;

            plandate_text.setText(data.getDateandtime());
            planname_text.setText(data.getPlanname());
            planlocation_text.setText(data.getLocation());
            planreply_text.setText(data.getReplynum());
        }


        public void getScheduleFromServer(int sid) {

            JsonObject json = new JsonObject();

            json.addProperty("sid", sid);

            final ProgressDialog progressDialog = new ProgressDialog(itemView.getContext());
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setMessage("해당 댓글로 이동중~" + boardList.get(getAdapterPosition() - 1).getSid());
            progressDialog.show();


            Ion.with(itemView.getContext())
                    .load("POST", url.getServerUrl() + "/showSche")
                    .setHeader("Content-Type", "application/json")
                    .setJsonObjectBody(json)
                    .asJsonObject() //응답
                    .setCallback(new FutureCallback<JsonObject>() {
                        @Override
                        public void onCompleted(Exception e, JsonObject result) {
                            //응답 받을 변수
                            String userName, schedTitle, aboutSched, schedLocation;
                            String startDate, startTime, endDate, endTime, startToEnd;

                            if (e != null) {
                                Toast.makeText(itemView.getContext(), "Server Connection Error!", Toast.LENGTH_LONG).show();
                            } else {
                                //응답 형식이 { "data":{"id":"jacob456@hanmail.net", "cid":1, "sid":10, "title":"korea"}, "message":"success"}
                                //data: 다음에 나오는 것들도 JsonObject형식.
                                //따라서 data를 JsonObject로 받고, 다시 이 data를 이용하여(어찌보면 JsonObject안에 또다른 JsonObject가 있는 것이다.
                                //JSONArray가 아님. 얘는 [,]로 묶여 있어야 함.

                                String message = result.get("message").getAsString();
                                //서버로 부터 응답 메세지가 success이면...

                                if (message.equals("success")) {
                                    //서버 응답 오면 로딩 창 해제
                                    progressDialog.dismiss();

                                    //data: {} 에서 {}안에 있는 것들도 JsonObject
                                    JsonObject data = result.get("data").getAsJsonObject();

                                    userName = data.get("id").getAsString();
                                    schedTitle = data.get("title").getAsString();
                                    aboutSched = data.get("sContent").getAsString();
                                    schedLocation = data.get("area").getAsString();
                                    startDate = data.get("startDate").getAsString();
                                    //startTime = data.get("startTime").getAsString();
                                    endDate = data.get("endDate").getAsString();
                                    //endTime = data.get("endTime").getAsString();

                                    //뒤에 0.000 잘라내기
                                    startDate = startDate.substring(0, 16);
                                    endDate = endDate.substring(0, 16);
                                    startToEnd = startDate + " ~ " + endDate;

                                    Intent intent = new Intent(context, PlanDetailActivity.class);
                                    intent.putExtra("userName", userName);
                                    intent.putExtra("schedTitle", schedTitle);
                                    intent.putExtra("aboutSched", aboutSched);
                                    intent.putExtra("area", schedLocation);
                                    intent.putExtra("startToEnd", startToEnd);

                                    context.startActivity(intent);

                                    Log.i("result", data.get("id").getAsString());
                                } else {

                                    Toast.makeText(itemView.getContext(), "해당 일정이 없습니다.", Toast.LENGTH_LONG).show();
                                }

                            }
                        }
                    });

        }

    }

    class HeaderViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        HeaderViewHolder(View headerView) {
            super(headerView);
        }

        @Override
        public void onClick(View v) {

        }
    }

    class FooterViewHolder extends RecyclerView.ViewHolder {

        FooterViewHolder(View footerView) {
            super(footerView);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return position;
//        if (position == 0)
//            return TYPE_HEADER;
//        else if (position == boardList.size() + 1)
//               return TYPE_FOOTER;
//        else
//            return TYPE_ITEM;
    }

}
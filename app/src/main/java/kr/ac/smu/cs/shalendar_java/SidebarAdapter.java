package kr.ac.smu.cs.shalendar_java;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;

public class SidebarAdapter extends RecyclerView.Adapter<SidebarAdapter.ItemRowHolder> {

    private Context context;
    private Context mContext;
    private ArrayList<SidebarItem> calendarList;


    public SidebarAdapter(Context mContext, ArrayList<SidebarItem> calendarList) {
        this.mContext = mContext;
        this.calendarList = calendarList;
    }


    @Override
    public ItemRowHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.sidebaritem, null);
//        SidebarAdapter.ItemRowHolder mh = new SidebarAdapter.ItemRowHolder(v);
//        return mh;

        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.sidebaritem, parent, false);

        return new ItemRowHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ItemRowHolder holder, int position) {
//        String calendarName = calendarList.get(position).getCalendarName();
//        String calendarImage = calendarList.get(position).getCalendarImage();
        ArrayList teampicList = calendarList.get(position).getTeamImageList();
//
//        holder.calendarSidebarName.setText(calendarName);
//        holder.calendarSidebarImage.setImageURI(calendarImage);

//        Ion.with(holder.calendarSidebarImage)
//                .centerCrop()
//                .placeholder(R.drawable.face)
//                .load(calendarImage);

        SidebarItem item = calendarList.get(position);
        holder.setItem(item);

        SidebarTeamAdapter st_adapter = new SidebarTeamAdapter(teampicList, mContext);

        holder.sideteam_recyclerView.setHasFixedSize(true);
        holder.sideteam_recyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayout.HORIZONTAL, false));
        holder.sideteam_recyclerView.setAdapter(st_adapter);

    }


    @Override
    public int getItemCount() {
        return (null != calendarList ? calendarList.size() : 0);
    }

    public void addItem(SidebarItem item) {
        calendarList.add(item);
    }

    public void setItems(ArrayList<SidebarItem> items) {
        this.calendarList = items;
    }

    public SidebarItem getItem(int position) {
        return calendarList.get(position);
    }

    public void setItem(int position, SidebarItem item) {
        calendarList.set(position, item);
    }


    public class ItemRowHolder extends RecyclerView.ViewHolder {


        TextView calendarSidebarName;
        ImageView calendarSidebarImage;
        RecyclerView sideteam_recyclerView;
        ImageView toInviteMember;

        int calendar_ID;
        String calendarName;
        String calendarContent;
//        String senderName;
//        String senderImg;

        NetWorkUrl url = new NetWorkUrl();


        public ItemRowHolder(final View itemView) {
            super(itemView);

            this.sideteam_recyclerView = (RecyclerView) itemView.findViewById(R.id.sideBarTeamRecycler);
            this.calendarSidebarName = (TextView) itemView.findViewById(R.id.calendarSidebarName);
            this.calendarSidebarImage = (ImageView) itemView.findViewById(R.id.calendarSidebarImage);
            this.toInviteMember = itemView.findViewById(R.id.sidebarList_invite_member);


//            멤버 초대화면으로 이동.
            toInviteMember.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    MainActivity.cid = calendar_ID;
                    if (MainActivity.cid == 0) {
                        Toast.makeText(itemView.getContext(), "달력을 먼저 선택해주세요", Toast.LENGTH_SHORT).show();
                    } else {
                        if (getAdapterPosition() == 0) {
//                        calendarSidebarName.setText("개인 달력");
                            Toast.makeText(itemView.getContext(), "해당 달력은 개인 달력 입니다.", Toast.LENGTH_LONG).show();
                        } else {
                            Intent intent = new Intent(itemView.getContext(), InviteActivity.class);
                            intent.putExtra("cid", calendar_ID);
                            intent.putExtra("calName", calendarName);
                            itemView.getContext().startActivity(intent);
                        }
                    }
                }
            });


            itemView.setOnClickListener(new View.OnClickListener() {
                ///////////////////////////
                /*
                사이드 바  달력 리스트에서 해당 달력 클릭 시
                cid와 달력 이름등의 정보를 가지고
                MainActivity로 돌아간다.
                */
                @Override
                public void onClick(View v) {
                    Toast.makeText(v.getContext(), calendarSidebarName.getText() + Integer.toString(calendar_ID), Toast.LENGTH_SHORT).show();
                    MainActivity.cid = calendar_ID;
                    MainActivity.calName = calendarName;
                    Intent intent = new Intent(itemView.getContext(), MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    itemView.getContext().startActivity(intent);
                }
            });


            itemView.setOnLongClickListener(new View.OnLongClickListener()

            {
                @Override
                public boolean onLongClick(View v) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(v.getContext());
                    dialog.setTitle("달력 수정/삭제");
                    dialog.setMessage("달력 수정, 삭제하십니까?")
                            .setPositiveButton("수정", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //여기 캘린더 수정화면으로 바꿔야함
                                    MainActivity.cid = calendar_ID;
                                    Intent intent = new Intent(context, CreateCalendarActivity.class);
                                    //99999 캘린더 수정 코드
                                    intent.putExtra("where", 99999);
                                    intent.putExtra("cid", MainActivity.cid);
                                    intent.putExtra("calImage", getItem(getAdapterPosition()).getCalendarImage());
                                    intent.putExtra("calName", calendarName);
                                    intent.putExtra("aboutCal",calendarContent);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    context.startActivity(intent);
                                    dialog.cancel();
                                }
                            })

                            .setNegativeButton("삭제", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    deleteCalendar(calendar_ID);
                                    Intent intent = new Intent(context, MainActivity.class);
                                    MainActivity.cid = 0;
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    context.startActivity(intent);
                                    dialog.cancel();
                                }
                            });

                    AlertDialog alertDialog = dialog.create();
                    alertDialog.show();
                    return false;
                }
            });
        }


//        public void setSenderName(String senderName) {
//            this.senderName = senderName;
//        }


//        public void setSenderImg(String senderImg) {
//            this.senderImg = senderImg;
//        }

        public void setCid(int calendar_ID) {
            this.calendar_ID = calendar_ID;
        }

        public void setCalName(String calName) {
            this.calendarName = calName;
        }

        public void setCalContent(String calContent) {
            this.calendarContent = calContent;
        }

        public void setItem(SidebarItem item) {
            calendarSidebarName.setText(item.getCalendarName());
            setCid(item.getCalendar_ID());
            setCalName(item.getCalendarName());
            setCalContent(item.getCalendarContent());

            Ion.with(calendarSidebarImage)
                    .centerCrop()
                    .placeholder(R.drawable.face)
                    .load(item.getCalendarImage());
        }

        public void deleteCalendar(int calendar_ID) {

            SharedPreferences pref = context.getSharedPreferences("pref_USERTOKEN", MODE_PRIVATE);
            String userToken = pref.getString("userToken", "NO_TOKEN");
            Log.i("C::Sharepref에 저장된 토큰", userToken);


            final ProgressDialog progressDialog = new ProgressDialog(context);
            progressDialog.setMessage("공유 달력을 삭제 중 입니다~");
            progressDialog.setProgressStyle(progressDialog.STYLE_SPINNER);
            progressDialog.show();

            JsonObject json = new JsonObject();
            json.addProperty("cid", calendar_ID);
            Log.i("선택된 달력 cid", Integer.toString(calendar_ID));

            Ion.with(context.getApplicationContext())
                    .load("POST", url.getServerUrl() + "/deleteCal")
                    .setHeader("Content-Type", "application/json")
                    .setHeader("Authorization", userToken)
                    .setJsonObjectBody(json)
                    .asJsonObject()
                    .setCallback(new FutureCallback<JsonObject>() {
                        @Override
                        public void onCompleted(Exception e, JsonObject result) {

                            if (e != null) {
                                Log.i("/DeleteCal", e.getMessage());
                                Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
                            } else {
                                String message = result.get("message").getAsString();
                                progressDialog.dismiss();
                                if (message.equals("success")) {
                                    Toast.makeText(context, "삭제 " + message, Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                                }
                            }
                        }
                    });
        }
    }

}

package kr.ac.smu.cs.shalendar_java;

import android.net.Uri;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class SidebarItem {

    String calendarImage;
    String calendarName;
    String calendarContent;
    //cid추가
    int calendar_ID;
//    String senderName;
//    String senderImg;

    ArrayList<SidebarTeamItem> teamImageList;

//    public SidebarItem(String calendarImage, String calendarName) {
//        this.calendarImage = calendarImage;
//        this.calendarName = calendarName;
//    }

    public SidebarItem() {

    }

    public String getCalendarImage() {

        return calendarImage;
    }

    public void setCalendarImage(String calendarImage) {


        this.calendarImage = calendarImage;
    }

    public String getCalendarName() {

        return calendarName;
    }

//    public void setSenderName(String senderName) {
//        this.senderName = senderName;
//    }
//
//    public String getSenderName() {
//        return this.senderName;
//    }
//
//    public void setSenderImg(String senderImg) {
//        this.senderImg = senderImg;
//    }
//
//    public String getSenderImg() {
//        return this.senderImg;
//    }

    public void setCalendarName(String calendarName) {
        this.calendarName = calendarName;
    }

    public void setCalendar_ID(int cid) { this.calendar_ID = cid; }

    public int getCalendar_ID() { return this.calendar_ID; }

    public void setCalendarContent(String calendarContent) {
        this.calendarContent = calendarContent;
    }

    public String getCalendarContent() {
        return this.calendarContent;
    }

    public ArrayList<SidebarTeamItem> getTeamImageList() {
        return teamImageList;
    }

    public void setTeamImageList(ArrayList<SidebarTeamItem> teamImageList) {
        this.teamImageList = teamImageList;
    }
}

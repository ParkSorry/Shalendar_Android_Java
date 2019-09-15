package kr.ac.smu.cs.shalendar_java;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;

public class PlandetailAdapter extends BaseAdapter {

    ArrayList<PlandetailItem> items = new ArrayList<PlandetailItem>();

    public void clearItem() {
        items.clear();
    }

    public void addItem(PlandetailItem item) {
        items.add(item);
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        PlandetailItemView view = null;
        if (convertView == null) {
            view = new PlandetailItemView(parent.getContext());
        } else {
            view = (PlandetailItemView) convertView;
        }

        PlandetailItem item = items.get(position);
        view.setReply_profile_name(item.reply_name);
        view.setReply_date(item.reply_date);
        view.setReply_content(item.reply_content);

        return view;
    }
}

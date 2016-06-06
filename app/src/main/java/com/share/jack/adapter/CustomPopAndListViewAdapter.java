package com.share.jack.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


import com.share.jack.swingtravel.R;

import java.util.List;

/**
 * Author:Created by JackCheng
 * Email:17764576259@163.com
 * Time:2016/1/21 12:49
 * Copyright:1.0
 */
public class CustomPopAndListViewAdapter extends BaseAdapter {

    private List<String> mList;
    private Context mContext;
    private LayoutInflater mInflater;

    public CustomPopAndListViewAdapter(Context mContext, List<String> mList) {
        this.mContext = mContext;
        this.mList = mList;
        mInflater = LayoutInflater.from(mContext);
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.item_pop_list, null);
            holder.tvText = (TextView) convertView.findViewById(R.id.ipl_tv_text);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.tvText.setText(mList.get(position).toString());
        return convertView;
    }

    class ViewHolder {
        TextView tvText;
    }
}

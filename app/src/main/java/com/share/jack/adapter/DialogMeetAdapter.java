package com.share.jack.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.share.jack.bean.DialogMeetBean;
import com.share.jack.swingtravel.R;

import java.util.List;

/**
 * Author:Created by JackCheng
 * Email:17764576259@163.com
 * Time:2016/2/19 09:10
 * Copyright:1.0
 */
public class DialogMeetAdapter extends BaseAdapter {

    private List<DialogMeetBean> mList;
    private Context mContext;
    private LayoutInflater mInflater;

    public DialogMeetAdapter(Context mContext, List<DialogMeetBean> mList) {
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
            convertView = mInflater.inflate(R.layout.item_dialog_meet, null);
            holder.tvContent = (TextView) convertView.findViewById(R.id.idm_tv_content);
            holder.tvNum = (TextView) convertView.findViewById(R.id.idm_tv_num);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.tvContent.setText(mList.get(position).mContent);
        holder.tvNum.setText(mList.get(position).mNum);
        return convertView;
    }

    class ViewHolder {
        TextView tvContent;
        TextView tvNum;
    }

}

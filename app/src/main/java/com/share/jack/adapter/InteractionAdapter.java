package com.share.jack.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.share.jack.bean.InteractionBean;
import com.share.jack.swingtravel.R;
import com.share.jack.utils.GlideCircleTransform;

import java.util.List;

/**
 * Author:Created by JackCheng
 * Email:17764576259@163.com
 * Time:2016/2/11 20:13
 * Copyright:1.0
 */
public class InteractionAdapter extends BaseAdapter {

    private List<InteractionBean> mList;
    private Context mContext;
    private LayoutInflater mInflater;

    public InteractionAdapter(Context mContext, List<InteractionBean> mList) {
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
            convertView = mInflater.inflate(R.layout.item_interaction, null);
            holder.rivHead = (ImageView) convertView.findViewById(R.id.ii_riv_head);
            holder.tvNickname = (TextView) convertView.findViewById(R.id.ii_tv_nickname);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        String url = mList.get(position).imgHeadUrl;
        Glide.with(mContext).load(url).placeholder(R.mipmap.ic_launcher)
                .error(R.mipmap.ic_launcher).transform(new GlideCircleTransform(mContext))
                .into(holder.rivHead);
        holder.tvNickname.setText(mList.get(position).nickName);
        return convertView;
    }

    class ViewHolder {
        ImageView rivHead;
        TextView tvNickname;
    }
}

package com.share.jack.adapter;

import android.content.Context;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.TextAppearanceSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.share.jack.bean.WantGoBean;
import com.share.jack.swingtravel.R;
import com.share.jack.utils.GlideCircleTransform;

import java.util.List;

/**
 * Author:Created by JackCheng
 * Email:17764576259@163.com
 * Time:2016/1/26 20:22
 * Copyright:1.0
 */
public class WantGoAdapter extends BaseAdapter {

    private List<WantGoBean> mList;
    private Context mContext;
    private LayoutInflater mInflater;

    public WantGoAdapter(Context mContext, List<WantGoBean> mList) {
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

    public void addNewsItem(WantGoBean wantGoBean) {
        mList.add(wantGoBean);
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
            convertView = mInflater.inflate(R.layout.item_want_go_list, null);
            holder.rivImageHead = (ImageView) convertView.findViewById(R.id.iwgl_riv_image_head);
            holder.tvTime = (TextView) convertView.findViewById(R.id.iwgl_tv_time);
            holder.tvNickName = (TextView) convertView.findViewById(R.id.iwgl_tv_nickname);
            holder.ivImage = (ImageView) convertView.findViewById(R.id.iwgl_iv_image);
            holder.tvTitle = (TextView) convertView.findViewById(R.id.iwgl_tv_title);
            holder.tvContent = (TextView) convertView.findViewById(R.id.iwgl_tv_content);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        String url = mList.get(position).mImgUrl;
        //placeholder是未完成下载时所显示的，error是下载出错的时候所显示的
        Glide.with(mContext).load(url).placeholder(R.mipmap.ic_launcher)
                .error(R.mipmap.ic_launcher).transform(new GlideCircleTransform(mContext))
                .into(holder.rivImageHead);
        holder.tvTime.setText(mList.get(position).mTime);
        holder.tvNickName.setText(mList.get(position).mNickName);

        Glide.with(mContext).load(url).placeholder(R.mipmap.ic_launcher)
                .error(R.mipmap.ic_launcher)
                .centerCrop().into(holder.ivImage);
        holder.tvTitle.setText(mList.get(position).mTitle);
        holder.tvContent.setText(mList.get(position).mContent);
        return convertView;
    }

    class ViewHolder {
        ImageView rivImageHead;
        TextView tvNickName;
        TextView tvTime;
        ImageView ivImage;
        TextView tvTitle;
        TextView tvContent;
    }
}

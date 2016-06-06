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
import com.share.jack.bean.BeenGoneBean;
import com.share.jack.demoutils.ImageUtils;
import com.share.jack.swingtravel.R;
import com.share.jack.utils.GlideCircleTransform;
import com.share.jack.widget.RoundImageView;

import java.util.List;

/**
 * Author:Created by JackCheng
 * Email:17764576259@163.com
 * Time:2016/1/26 20:22
 * Copyright:1.0
 */
public class BeenGoneAdapter extends BaseAdapter {

    private List<BeenGoneBean> mList;
    private Context mContext;
    private LayoutInflater mInflater;

    public BeenGoneAdapter(Context mContext, List<BeenGoneBean> mList) {
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

    public void addNewsItem(BeenGoneBean beenGoneBean) {
        mList.add(beenGoneBean);
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
            convertView = mInflater.inflate(R.layout.item_been_gone_list, null);
            holder.rivImageHead = (RoundImageView) convertView.findViewById(R.id.ibgl_riv_image_head);
            holder.tvTime = (TextView) convertView.findViewById(R.id.ibgl_tv_time);
            holder.tvNickName = (TextView) convertView.findViewById(R.id.ibgl_tv_nickname);
            holder.tvContentAit = (TextView) convertView.findViewById(R.id.ibgl_tv_content_ait);
            holder.ivImage = (ImageView) convertView.findViewById(R.id.ibgl_iv_image);
            holder.tvTitle = (TextView) convertView.findViewById(R.id.ibgl_tv_title);
            holder.tvContent = (TextView) convertView.findViewById(R.id.ibgl_tv_content);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.rivImageHead.setImageBitmap(ImageUtils.stringtoBitmap(mList.get(position).mImgHeadUrl));
        holder.tvTime.setText(mList.get(position).mTime);
        holder.tvNickName.setText(mList.get(position).mNickName);
        String strContent = "@" + mList.get(position).mContentAit.toString();
        int pos = strContent.indexOf("：");
        Log.d("BeenGoneAdapter---->", "pos=" + pos);
        SpannableString styledText = new SpannableString(strContent);
        //设置昵称和评论的字体不一致
        styledText.setSpan(new TextAppearanceSpan(mContext, R.style.style_name)
                , 0, pos, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        styledText.setSpan(new TextAppearanceSpan(mContext, R.style.style_content)
                , pos + 1, strContent.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        holder.tvContentAit.setText(styledText, TextView.BufferType.SPANNABLE);

        Glide.with(mContext).load(mList.get(position).mImgUrl).placeholder(R.mipmap.ic_launcher)
                .error(R.mipmap.ic_launcher)
                .centerCrop().into(holder.ivImage);
        holder.tvTitle.setText(mList.get(position).mTitle);
        holder.tvContent.setText(mList.get(position).mContent);
        return convertView;
    }

    class ViewHolder {
        RoundImageView rivImageHead;
        TextView tvNickName;
        TextView tvContentAit;
        TextView tvTime;
        ImageView ivImage;
        TextView tvTitle;
        TextView tvContent;
    }
}

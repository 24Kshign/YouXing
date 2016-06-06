package com.share.jack.adapter;

import android.content.Context;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.TextAppearanceSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.share.jack.bean.FragmentFoodBean;
import com.share.jack.swingtravel.R;
import com.share.jack.utils.GlideCircleTransform;

import java.util.List;

/**
 * Author:Created by JackCheng
 * Email:17764576259@163.com
 * Time:2016/1/24 13:46
 * Copyright:1.0
 */
public class FragmentFoodAdapter extends BaseAdapter {

    private List<FragmentFoodBean> mList;
    private LayoutInflater mInflater;
    private Context mContext;

    public FragmentFoodAdapter(Context mContext, List<FragmentFoodBean> mList) {
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
            convertView = mInflater.inflate(R.layout.item_food_list, null);
            holder.ivImage = (ImageView) convertView.findViewById(R.id.ifl_iv_image);
            holder.tvContent = (TextView) convertView.findViewById(R.id.ifl_tv_content);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        String url = mList.get(position).mImageUrl;
        //placeholder是未完成下载时所显示的，error是下载出错的时候所显示的

        Glide.with(mContext).load(url).placeholder(R.mipmap.ic_launcher)
                .error(R.mipmap.ic_launcher)
                .centerCrop().into(holder.ivImage);
        String strContent = mList.get(position).mNickName + mList.get(position).mContent;
        int pos = strContent.indexOf("：");
        SpannableString styledText = new SpannableString(strContent);
        //设置昵称和评论的字体不一致
        styledText.setSpan(new TextAppearanceSpan(mContext, R.style.style_name)
                , 0, pos, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        styledText.setSpan(new TextAppearanceSpan(mContext, R.style.style_content)
                , pos + 1, strContent.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        holder.tvContent.setText(styledText, TextView.BufferType.SPANNABLE);
        return convertView;
    }

    class ViewHolder {
        ImageView ivImage;
        TextView tvContent;
    }
}

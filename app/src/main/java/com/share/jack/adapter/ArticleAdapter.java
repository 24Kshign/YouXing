package com.share.jack.adapter;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.share.jack.bean.ArticleBean;
import com.share.jack.bean.CommentBean;
import com.share.jack.demoutils.ImageUtils;
import com.share.jack.swingtravel.R;
import com.share.jack.utils.GlideCircleTransform;
import com.share.jack.utils.YXConstant;
import com.share.jack.widget.MyNoScrollListView;
import com.share.jack.widget.MyScrollView;
import com.share.jack.widget.RoundImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * Author:Created by JackCheng
 * Email:17764576259@163.com
 * Time:2016/1/28 15:21
 * Copyright:1.0
 */
public class ArticleAdapter extends BaseAdapter {

    private List<ArticleBean> mList;
    private Context mContext;
    private LayoutInflater mInflater;
    private Handler mHandler;

    public ArticleAdapter(Context mContext, List<ArticleBean> mList, Handler mHandler) {
        this.mContext = mContext;
        this.mList = mList;
        this.mHandler = mHandler;
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
            convertView = mInflater.inflate(R.layout.item_article_list, null);
            holder.rivHead = (RoundImageView) convertView.findViewById(R.id.ial_riv_head);
            holder.ivHead = (ImageView) convertView.findViewById(R.id.ial_iv_head);
            holder.tvNickName = (TextView) convertView.findViewById(R.id.ial_tv_nickname);
            holder.tvContent = (TextView) convertView.findViewById(R.id.ial_tv_content);
            holder.tvTime = (TextView) convertView.findViewById(R.id.ial_tv_time);
            holder.tvReply = (TextView) convertView.findViewById(R.id.ial_tv_reply);
            holder.listview = (MyNoScrollListView) convertView.findViewById(R.id.ial_listview);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (mList.get(position).isMe.equals("1")) {    //是自己评论的
            holder.ivHead.setVisibility(View.GONE);
            holder.rivHead.setVisibility(View.VISIBLE);
            holder.rivHead.setImageBitmap(ImageUtils.stringtoBitmap(mList.get(position).mHeadImgUrl));
        } else {
            holder.ivHead.setVisibility(View.VISIBLE);
            holder.rivHead.setVisibility(View.GONE);
            //placeholder是未完成下载时所显示的，error是下载出错的时候所显示的
            Glide.with(mContext).load(mList.get(position).mHeadImgUrl).placeholder(R.mipmap.ic_launcher)
                    .error(R.mipmap.ic_launcher).transform(new GlideCircleTransform(mContext))
                    .into(holder.ivHead);
        }

        holder.tvNickName.setText(mList.get(position).mNickName);
        holder.tvTime.setText(mList.get(position).mTime);
        SpannableString styledText = YXConstant.getSpannableString(mList.get(position).mContent, mContext);
        holder.tvContent.setText(styledText, TextView.BufferType.SPANNABLE);
        TextviewClickListener tcl = new TextviewClickListener(position);
        holder.tvReply.setOnClickListener(tcl);
        CommentAdapter caAdapter = new CommentAdapter(mContext, mList.get(position).mCommentList);
        holder.listview.setAdapter(caAdapter);

        return convertView;
    }

    class ViewHolder {
        RoundImageView rivHead;
        ImageView ivHead;
        TextView tvNickName;
        TextView tvTime;
        TextView tvContent;
        TextView tvReply;
        MyNoScrollListView listview;
    }

    /**
     * 获取回复评论
     */
    public void getReplyComment(CommentBean bean, int position) {
        List<CommentBean> rList = mList.get(position).mCommentList;
        rList.add(rList.size(), bean);
    }


    //自定义回复事件点击监听器
    private final class TextviewClickListener implements View.OnClickListener {
        private int position;

        public TextviewClickListener(int position) {
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.ial_tv_reply:
                    Message msg = mHandler.obtainMessage();
                    msg.what = 1011;
                    msg.obj = position;
                    mHandler.sendMessage(msg);
                    break;
            }
        }
    }
}

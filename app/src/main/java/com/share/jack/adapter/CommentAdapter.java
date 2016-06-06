package com.share.jack.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.text.style.TextAppearanceSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.share.jack.bean.ArticleBean;
import com.share.jack.bean.CommentBean;
import com.share.jack.swingtravel.R;
import com.share.jack.utils.Expression;
import com.share.jack.utils.YXConstant;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Author:Created by JackCheng
 * Email:17764576259@163.com
 * Time:2016/1/28 15:21
 * Copyright:1.0
 */
public class CommentAdapter extends BaseAdapter {

    private List<CommentBean> mList;
    private Context mContext;
    private LayoutInflater mInflater;

    public CommentAdapter(Context mContext, List<CommentBean> mList) {
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
            convertView = mInflater.inflate(R.layout.item_comment, null);
            holder.tvComment = (TextView) convertView.findViewById(R.id.ic_comment);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        String strCommentNick = mList.get(position).commentNickname;
        String strReplyNick = mList.get(position).replyNickname;
        String strComment = strReplyNick + "回复" + strCommentNick + "：" + mList.get(position).mComment;

        int pos1 = strReplyNick.length();
        int pos2 = strCommentNick.length() + strReplyNick.length() + 2;   //分号的位置
        SpannableString styledText = YXConstant.getSpannableString(strComment, mContext);

        //设置昵称和评论的字体不一致
        styledText.setSpan(new TextAppearanceSpan(mContext, R.style.style_name)
                , 0, pos1, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        styledText.setSpan(new TextAppearanceSpan(mContext, R.style.style_content)
                , pos1 + 1, pos1 + 2, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        styledText.setSpan(new TextAppearanceSpan(mContext, R.style.style_name)
                , pos1 + 2, pos2, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        styledText.setSpan(new TextAppearanceSpan(mContext, R.style.style_content)
                , pos2 + 1, strComment.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);

        holder.tvComment.setText(styledText, TextView.BufferType.SPANNABLE);

        return convertView;
    }

    class ViewHolder {
        TextView tvComment;
    }
}

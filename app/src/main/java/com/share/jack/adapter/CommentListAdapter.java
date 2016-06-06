package com.share.jack.adapter;

import android.content.Context;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.share.jack.bean.CommentListBean;
import com.share.jack.swingtravel.R;
import com.share.jack.utils.GlideCircleTransform;

import java.util.List;

/**
 * Created by 程 on 2016/4/10.
 */
public class CommentListAdapter extends BaseAdapter {

    private List<CommentListBean> mList;
    private Context mContext;
    private LayoutInflater mInflater;

    public CommentListAdapter(Context mContext, List<CommentListBean> mList) {
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
            convertView = mInflater.inflate(R.layout.item_comment_list, null);
            holder.ivHead = (ImageView) convertView.findViewById(R.id.icl_iv_head);
            holder.ivMainImage = (ImageView) convertView.findViewById(R.id.icl_iv_image);
            holder.tvNickname = (TextView) convertView.findViewById(R.id.icl_tv_nickname);
            holder.tvTime = (TextView) convertView.findViewById(R.id.icl_tv_time);
            holder.tvReply = (TextView) convertView.findViewById(R.id.icl_tv_reply);
            holder.tvArticleTitle = (TextView) convertView.findViewById(R.id.icl_tv_title);
            holder.tvArticleAuthor = (TextView) convertView.findViewById(R.id.icl_tv_author);
            holder.tvArticleContent = (TextView) convertView.findViewById(R.id.icl_tv_content);
            holder.tvReplyContent = (TextView) convertView.findViewById(R.id.icl_tv_reply_content);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        Glide.with(mContext).load(mList.get(position).mImageHeadUrl).placeholder(R.mipmap.ic_launcher)
                .error(R.mipmap.ic_launcher).transform(new GlideCircleTransform(mContext))
                .into(holder.ivHead);
        Glide.with(mContext).load(mList.get(position).mImageUrl).placeholder(R.mipmap.ic_launcher)
                .error(R.mipmap.ic_launcher)
                .centerCrop().into(holder.ivMainImage);
        holder.tvNickname.setText(mList.get(position).mNickname);
        holder.tvTime.setText(mList.get(position).mTime);
        holder.tvReplyContent.setText(mList.get(position).mReplyContent);
        holder.tvArticleTitle.setText(mList.get(position).mArticleTitle);
        holder.tvArticleAuthor.setText(mList.get(position).mArticleAuthor);
        holder.tvArticleContent.setText(mList.get(position).mArticleContent);
        return convertView;
    }

    class ViewHolder {
        ImageView ivHead;
        ImageView ivMainImage;
        TextView tvNickname;
        TextView tvReplyContent;
        TextView tvTime;
        TextView tvArticleTitle;
        TextView tvArticleAuthor;
        TextView tvArticleContent;
        TextView tvReply;
    }
}

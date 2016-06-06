package com.share.jack.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.easemob.chat.EMConversation;
import com.easemob.chat.EMGroup;
import com.easemob.chat.EMGroupManager;
import com.easemob.chat.EMMessage;
import com.easemob.chat.ImageMessageBody;
import com.easemob.chat.TextMessageBody;
import com.easemob.util.DateUtils;
import com.easemob.util.EMLog;
import com.share.jack.bean.UsersBean;
import com.share.jack.controller.HXSDKHelper;
import com.share.jack.demoutils.SmileUtils;
import com.share.jack.demoutils.UserUtils;
import com.share.jack.domain.RobotUser;
import com.share.jack.model.DemoHXSDKHelper;
import com.share.jack.swingtravel.R;
import com.share.jack.utils.GlideCircleTransform;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Author:Created by JackCheng
 * Email:17764576259@163.com
 * Time:2016/2/1 20:54
 * Copyright:1.0
 */
public class ContactListAdapter extends ArrayAdapter<EMConversation> {

    private static final String TAG = "ContactListAdapter";
    private LayoutInflater inflater;
    private List<EMConversation> conversationList;
    private List<EMConversation> copyConversationList;
    private ConversationFilter conversationFilter;
    private List<UsersBean> mUserList;
    private Context mContext;
    private boolean notiyfyByFilter;

    public ContactListAdapter(Context mContext, int resource, List<EMConversation> objects, List<UsersBean> mUserList) {
        super(mContext, resource, objects);
        this.conversationList = objects;
        this.mUserList = mUserList;
        copyConversationList = new ArrayList<EMConversation>();
        this.mContext = mContext;
        copyConversationList.addAll(objects);
        inflater = LayoutInflater.from(mContext);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_contact_list, parent, false);
        }
        ViewHolder holder = (ViewHolder) convertView.getTag();
        if (holder == null) {
            holder = new ViewHolder();
            holder.name = (TextView) convertView.findViewById(R.id.name);
            holder.unreadLabel = (TextView) convertView.findViewById(R.id.unread_msg_number);
            holder.message = (TextView) convertView.findViewById(R.id.message);
            holder.time = (TextView) convertView.findViewById(R.id.time);
            holder.avatar = (ImageView) convertView.findViewById(R.id.avatar);
            holder.msgState = convertView.findViewById(R.id.msg_state);
            holder.list_item_layout = (RelativeLayout) convertView.findViewById(R.id.list_item_layout);
            convertView.setTag(holder);
        }
        // 获取与此用户/群组的会话
        EMConversation conversation = getItem(position);
        // 获取用户username或者群组groupid
        if (mUserList.size() > 0) {
            holder.name.setText(mUserList.get(position).userNick);
            Glide.with(mContext).load(mUserList.get(position).userHead).placeholder(R.mipmap.default_avatar)
                    .error(R.mipmap.ic_launcher)
                    .transform(new GlideCircleTransform(mContext)).into(holder.avatar);
        } else {
            String username = conversation.getUserName();
            holder.name.setText("YX_" + username.substring(5));
            UserUtils.setUserAvatar(getContext(), username, holder.avatar);
        }

        if (conversation.getUnreadMsgCount() > 0) {
            // 显示与此用户的消息未读数
            holder.unreadLabel.setText(String.valueOf(conversation.getUnreadMsgCount()));
            holder.unreadLabel.setVisibility(View.VISIBLE);
        } else {
            holder.unreadLabel.setVisibility(View.INVISIBLE);
        }

        if (conversation.getMsgCount() != 0) {
            // 把最后一条消息的内容作为item的message内容
            EMMessage lastMessage = conversation.getLastMessage();
            holder.message.setText(SmileUtils.getSmiledText(getContext(), getMessageDigest(lastMessage, (this.getContext()))),
                    TextView.BufferType.SPANNABLE);
            holder.time.setText(DateUtils.getTimestampString(new Date(lastMessage.getMsgTime())));
            if (lastMessage.direct == EMMessage.Direct.SEND && lastMessage.status == EMMessage.Status.FAIL) {
                holder.msgState.setVisibility(View.VISIBLE);
            } else {
                holder.msgState.setVisibility(View.GONE);
            }
        }
        return convertView;
    }

    /**
     * 根据消息内容和消息类型获取消息内容提示
     *
     * @param message
     * @param context
     * @return
     */
    private String getMessageDigest(EMMessage message, Context context) {
        String digest = "";
        switch (message.getType()) {
            case IMAGE: // 图片消息
                ImageMessageBody imageBody = (ImageMessageBody) message.getBody();
                digest = getStrng(context, R.string.picture) + imageBody.getFileName();
                break;
            case VOICE:// 语音消息
                digest = getStrng(context, R.string.voice);
                break;
            case TXT: // 文本消息
                TextMessageBody txtBody = (TextMessageBody) message.getBody();
                digest = txtBody.getMessage();
                break;
            default:
                EMLog.e(TAG, "unknow type");
                return "";
        }
        return digest;
    }

    private static class ViewHolder {
        TextView name;      //和谁的聊天记录
        TextView unreadLabel;       //消息未读数
        TextView message;       //最后一条消息的内容
        TextView time;      //最后一条消息的时间
        ImageView avatar;      //用户头像
        View msgState;      //最后一条消息的发送状态
        RelativeLayout list_item_layout;        //整个list中每一行总布局
    }

    String getStrng(Context context, int resId) {
        return context.getResources().getString(resId);
    }

    @Override
    public Filter getFilter() {
        if (conversationFilter == null) {
            conversationFilter = new ConversationFilter(conversationList);
        }
        return conversationFilter;
    }

    private class ConversationFilter extends Filter {
        List<EMConversation> mOriginalValues = null;

        public ConversationFilter(List<EMConversation> mList) {
            mOriginalValues = mList;
        }

        @Override
        protected FilterResults performFiltering(CharSequence prefix) {
            FilterResults results = new FilterResults();

            if (mOriginalValues == null) {
                mOriginalValues = new ArrayList<EMConversation>();
            }
            if (prefix == null || prefix.length() == 0) {
                results.values = copyConversationList;
                results.count = copyConversationList.size();
            } else {
                String prefixString = prefix.toString();
                final int count = mOriginalValues.size();
                final ArrayList<EMConversation> newValues = new ArrayList<EMConversation>();

                for (int i = 0; i < count; i++) {
                    final EMConversation value = mOriginalValues.get(i);
                    String username = value.getUserName();
                    EMGroup group = EMGroupManager.getInstance().getGroup(username);
                    if (group != null) {
                        username = group.getGroupName();
                    }
                    // First match against the whole ,non-splitted value
                    if (username.startsWith(prefixString)) {
                        newValues.add(value);
                    } else {
                        final String[] words = username.split(" ");
                        final int wordCount = words.length;
                        // Start at index 0, in case valueText starts with space(s)
                        for (int k = 0; k < wordCount; k++) {
                            if (words[k].startsWith(prefixString)) {
                                newValues.add(value);
                                break;
                            }
                        }
                    }
                }
                results.values = newValues;
                results.count = newValues.size();
            }
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            conversationList.clear();
            conversationList.addAll((List<EMConversation>) results.values);
            if (results.count > 0) {
                notiyfyByFilter = true;
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }

        }
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        if (!notiyfyByFilter) {
            copyConversationList.clear();
            copyConversationList.addAll(conversationList);
            notiyfyByFilter = false;
        }
    }
}

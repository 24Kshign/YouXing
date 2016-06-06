package com.share.jack.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Spannable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TextView.BufferType;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.easemob.EMCallBack;
import com.easemob.EMError;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMConversation;
import com.easemob.chat.EMMessage;
import com.easemob.chat.EMMessage.ChatType;
import com.easemob.chat.EMMessage.Direct;
import com.easemob.chat.EMMessage.Type;
import com.easemob.chat.FileMessageBody;
import com.easemob.chat.ImageMessageBody;
import com.easemob.chat.TextMessageBody;
import com.easemob.chat.VoiceMessageBody;
import com.easemob.util.DateUtils;
import com.easemob.util.EMLog;
import com.share.jack.bean.PersonalBean;
import com.share.jack.demoutils.ImageCache;
import com.share.jack.demoutils.ImageUtils;
import com.share.jack.demoutils.SmileUtils;
import com.share.jack.demoutils.UserUtils;
import com.share.jack.swingtravel.AlertDialog;
import com.share.jack.swingtravel.ChatActivity;
import com.share.jack.swingtravel.ContextMenu;
import com.share.jack.swingtravel.MySelfAndOtherActivity;
import com.share.jack.swingtravel.R;
import com.share.jack.swingtravel.ShowBigImage;
import com.share.jack.task.LoadImageTask;
import com.share.jack.utils.GlideCircleTransform;
import com.share.jack.utils.YXConstant;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class MessageAdapter extends BaseAdapter {

    private final static String TAG = "msg";

    private static final int MESSAGE_TYPE_RECV_TXT = 0;
    private static final int MESSAGE_TYPE_SENT_TXT = 1;
    private static final int MESSAGE_TYPE_SENT_IMAGE = 2;
    private static final int MESSAGE_TYPE_RECV_IMAGE = 5;
    private static final int MESSAGE_TYPE_SENT_VOICE = 6;
    private static final int MESSAGE_TYPE_RECV_VOICE = 7;

    public static final String IMAGE_DIR = "chat/image/";

    private String username;
    private LayoutInflater inflater;
    private Activity activity;

    private static final int HANDLER_MESSAGE_REFRESH_LIST = 0;
    private static final int HANDLER_MESSAGE_SELECT_LAST = 1;
    private static final int HANDLER_MESSAGE_SEEK_TO = 2;

    // reference to conversation object in chatsdk
    private EMConversation conversation;
    EMMessage[] messages = null;

    private Context context;

    private Map<String, Timer> timers = new Hashtable<String, Timer>();

    private PersonalBean personalBean;

    public MessageAdapter(Context context, String username, PersonalBean personalBean) {
        this.username = username;
        this.context = context;
        inflater = LayoutInflater.from(context);
        activity = (Activity) context;
        this.personalBean = personalBean;
        this.conversation = EMChatManager.getInstance().getConversation(username);
    }

    Handler handler = new Handler() {
        private void refreshList() {
            // UI线程不能直接使用conversation.getAllMessages()
            // 否则在UI刷新过程中，如果收到新的消息，会导致并发问题
            messages = (EMMessage[]) conversation.getAllMessages().toArray(new EMMessage[conversation.getAllMessages().size()]);
            for (int i = 0; i < messages.length; i++) {
                // getMessage will set message as read status
                conversation.getMessage(i);
            }
            notifyDataSetChanged();
        }

        @Override
        public void handleMessage(android.os.Message message) {
            switch (message.what) {
                case HANDLER_MESSAGE_REFRESH_LIST:
                    refreshList();
                    break;
                case HANDLER_MESSAGE_SELECT_LAST:
                    if (activity instanceof ChatActivity) {
                        ListView listView = ((ChatActivity) activity).getListView();
                        if (messages.length > 0) {
                            listView.setSelection(messages.length - 1);
                        }
                    }
                    break;
                case HANDLER_MESSAGE_SEEK_TO:
                    int position = message.arg1;
                    if (activity instanceof ChatActivity) {
                        ListView listView = ((ChatActivity) activity).getListView();
                        listView.setSelection(position);
                    }
                    break;
                default:
                    break;
            }
        }
    };


    /**
     * 获取item数
     */
    public int getCount() {
        return messages == null ? 0 : messages.length;
    }

    /**
     * 刷新页面
     */
    public void refresh() {
        if (handler.hasMessages(HANDLER_MESSAGE_REFRESH_LIST)) {
            return;
        }
        android.os.Message msg = handler.obtainMessage(HANDLER_MESSAGE_REFRESH_LIST);
        handler.sendMessage(msg);
    }

    /**
     * 刷新页面, 选择最后一个
     */
    public void refreshSelectLast() {
        handler.sendMessage(handler.obtainMessage(HANDLER_MESSAGE_REFRESH_LIST));
        handler.sendMessage(handler.obtainMessage(HANDLER_MESSAGE_SELECT_LAST));
    }

    /**
     * 刷新页面, 选择Position
     */
    public void refreshSeekTo(int position) {
        handler.sendMessage(handler.obtainMessage(HANDLER_MESSAGE_REFRESH_LIST));
        android.os.Message msg = handler.obtainMessage(HANDLER_MESSAGE_SEEK_TO);
        msg.arg1 = position;
        handler.sendMessage(msg);
    }

    public EMMessage getItem(int position) {
        if (messages != null && position < messages.length) {
            return messages[position];
        }
        return null;
    }

    public long getItemId(int position) {
        return position;
    }

    /**
     * 获取item类型数
     */
    public int getViewTypeCount() {
        return 18;
    }

    /**
     * 获取item类型
     */
    public int getItemViewType(int position) {
        EMMessage message = getItem(position);
        if (message == null) {
            return -1;
        }
        if (message.getType() == Type.TXT) {
            return message.direct == Direct.RECEIVE ? MESSAGE_TYPE_RECV_TXT : MESSAGE_TYPE_SENT_TXT;
        }
        if (message.getType() == Type.IMAGE) {
            return message.direct == Direct.RECEIVE ? MESSAGE_TYPE_RECV_IMAGE : MESSAGE_TYPE_SENT_IMAGE;
        }
        if (message.getType() == Type.VOICE) {
            return message.direct == Direct.RECEIVE ? MESSAGE_TYPE_RECV_VOICE : MESSAGE_TYPE_SENT_VOICE;
        }
        return -1;// invalid
    }

    private View createViewByMessage(EMMessage message, int position) {
        switch (message.getType()) {
            case IMAGE:
                return message.direct == Direct.RECEIVE ? inflater.inflate(R.layout.row_received_picture, null) : inflater.inflate(
                        R.layout.row_sent_picture, null);
            case VOICE:
                return message.direct == Direct.RECEIVE ? inflater.inflate(R.layout.row_received_voice, null) : inflater.inflate(
                        R.layout.row_sent_voice, null);
            default:
                return message.direct == Direct.RECEIVE ? inflater.inflate(R.layout.row_received_message,
                        null) : inflater.inflate(R.layout.row_sent_message, null);
        }
    }

    @SuppressLint("NewApi")
    public View getView(final int position, View convertView, ViewGroup parent) {
        final EMMessage message = getItem(position);
        ChatType chatType = message.getChatType();
        final ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = createViewByMessage(message, position);
            if (message.getType() == Type.IMAGE) {
                try {
                    holder.iv = ((ImageView) convertView.findViewById(R.id.iv_sendPicture));
                    holder.iv_avatar = (ImageView) convertView.findViewById(R.id.iv_userhead);
                    holder.tv = (TextView) convertView.findViewById(R.id.percentage);
                    holder.pb = (ProgressBar) convertView.findViewById(R.id.progressBar);
                    holder.staus_iv = (ImageView) convertView.findViewById(R.id.msg_status);
                    holder.tv_usernick = (TextView) convertView.findViewById(R.id.tv_userid);
                } catch (Exception e) {
                }

            } else if (message.getType() == Type.TXT) {
                try {
                    holder.pb = (ProgressBar) convertView.findViewById(R.id.pb_sending);
                    holder.staus_iv = (ImageView) convertView.findViewById(R.id.msg_status);
                    holder.iv_avatar = (ImageView) convertView.findViewById(R.id.iv_userhead);
                    // 这里是文字内容
                    holder.tv = (TextView) convertView.findViewById(R.id.tv_chatcontent);
                    holder.tv_usernick = (TextView) convertView.findViewById(R.id.tv_userid);
                } catch (Exception e) {
                }

            } else if (message.getType() == Type.VOICE) {
                try {
                    holder.iv = ((ImageView) convertView.findViewById(R.id.iv_voice));
                    holder.iv_avatar = (ImageView) convertView.findViewById(R.id.iv_userhead);
                    holder.tv = (TextView) convertView.findViewById(R.id.tv_length);
                    holder.pb = (ProgressBar) convertView.findViewById(R.id.pb_sending);
                    holder.staus_iv = (ImageView) convertView.findViewById(R.id.msg_status);
                    holder.tv_usernick = (TextView) convertView.findViewById(R.id.tv_userid);
                    holder.iv_read_status = (ImageView) convertView.findViewById(R.id.iv_unread_voice);
                } catch (Exception e) {
                }
            }
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        if (message.direct == Direct.SEND) {
            UserUtils.setCurrentUserNick(holder.tv_usernick);
        }
        // 如果是发送的消息并且不是群聊消息，显示已读textview
        if (!(chatType == ChatType.GroupChat || chatType == ChatType.ChatRoom) && message.direct == Direct.SEND) {
            holder.tv_ack = (TextView) convertView.findViewById(R.id.tv_ack);
            holder.tv_delivered = (TextView) convertView.findViewById(R.id.tv_delivered);
            if (holder.tv_ack != null) {
                if (message.isAcked) {
                    if (holder.tv_delivered != null) {
                        holder.tv_delivered.setVisibility(View.INVISIBLE);
                    }
                    holder.tv_ack.setVisibility(View.VISIBLE);
                } else {
                    holder.tv_ack.setVisibility(View.INVISIBLE);
                    // check and display msg delivered ack status
                    if (holder.tv_delivered != null) {
                        if (message.isDelivered) {
                            holder.tv_delivered.setVisibility(View.VISIBLE);
                        } else {
                            holder.tv_delivered.setVisibility(View.INVISIBLE);
                        }
                    }
                }
            }
        }

        //设置用户头像
        setUserAvatar(message, holder.iv_avatar);
        switch (message.getType()) {
            // 根据消息type显示item
            case IMAGE: // 图片
                handleImageMessage(message, holder, position, convertView);
                break;
            case TXT: // 文本
                handleTextMessage(message, holder, position);
                break;
            case VOICE: // 语音
                handleVoiceMessage(message, holder, position, convertView);
                break;
        }
        if (message.direct == Direct.SEND) {
            View statusView = convertView.findViewById(R.id.msg_status);
            // 重发按钮点击事件
            statusView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {

                    // 显示重发消息的自定义alertdialog
                    Intent intent = new Intent(activity, AlertDialog.class);
                    intent.putExtra("msg", activity.getString(R.string.confirm_resend));
                    intent.putExtra("title", activity.getString(R.string.resend));
                    intent.putExtra("cancel", true);
                    intent.putExtra("position", position);
                    if (message.getType() == Type.TXT)
                        activity.startActivityForResult(intent, ChatActivity.REQUEST_CODE_TEXT);
                    else if (message.getType() == Type.VOICE)
                        activity.startActivityForResult(intent, ChatActivity.REQUEST_CODE_VOICE);
                    else if (message.getType() == Type.IMAGE)
                        activity.startActivityForResult(intent, ChatActivity.REQUEST_CODE_PICTURE);
                    else if (message.getType() == Type.LOCATION)
                        activity.startActivityForResult(intent, ChatActivity.REQUEST_CODE_LOCATION);
                    else if (message.getType() == Type.FILE)
                        activity.startActivityForResult(intent, ChatActivity.REQUEST_CODE_FILE);
                    else if (message.getType() == Type.VIDEO)
                        activity.startActivityForResult(intent, ChatActivity.REQUEST_CODE_VIDEO);

                }
            });

        }
        TextView timestamp = (TextView) convertView.findViewById(R.id.timestamp);
        if (position == 0) {
            SimpleDateFormat sdformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//24小时制
            timestamp.setText(sdformat.format(new Date(message.getMsgTime())));
            timestamp.setVisibility(View.VISIBLE);
        } else {
            // 两条消息时间离得如果稍长，显示时间
            EMMessage prevMessage = getItem(position - 1);
            if (prevMessage != null && DateUtils.isCloseEnough(message.getMsgTime(), prevMessage.getMsgTime())) {
                timestamp.setVisibility(View.GONE);
            } else {
                SimpleDateFormat sdformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//24小时制
                timestamp.setText(sdformat.format(new Date(message.getMsgTime())));
                timestamp.setVisibility(View.VISIBLE);
            }
        }
        return convertView;
    }


    /**
     * 显示用户头像
     *
     * @param message
     * @param imageView
     */
    private void setUserAvatar(final EMMessage message, ImageView imageView) {
        if (message.direct == Direct.SEND) {
            //显示自己头像
            UserUtils.setCurrentUserAvatar(context, imageView);
        } else {
            if (personalBean != null) {
                Glide.with(context).load(personalBean.getUserHead()).asBitmap()
                        .placeholder(R.mipmap.default_avatar)
                        .transform(new GlideCircleTransform(context))
                        .into(imageView);
            } else {
                UserUtils.setUserAvatar(context, message.getFrom(), imageView);
            }
        }
        if (message.direct == Direct.SEND) {
            imageView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    //显示自己信息
                    intent.setClass(context, MySelfAndOtherActivity.class);
                    intent.putExtra("username", message.getFrom());
                    Log.d(TAG, "message1=" + message.getFrom());
                    context.startActivity(intent);
                }
            });
        } else {
            imageView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    //显示别人信息
                    intent.setClass(context, MySelfAndOtherActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putParcelable(YXConstant.PAR_USER_KEY, personalBean);
                    intent.putExtra("username", message.getFrom());
                    intent.putExtras(bundle);
                    context.startActivity(intent);
                }
            });
        }
    }

    /**
     * 文本消息
     *
     * @param message
     * @param holder
     * @param position
     */
    private void handleTextMessage(EMMessage message, ViewHolder holder, final int position) {
        TextMessageBody txtBody = (TextMessageBody) message.getBody();
        Spannable span = SmileUtils.getSmiledText(context, txtBody.getMessage());
        // 设置内容
        holder.tv.setText(span, BufferType.SPANNABLE);
        // 设置长按事件监听
        holder.tv.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                activity.startActivityForResult(
                        (new Intent(activity, ContextMenu.class)).putExtra("position", position).putExtra("type",
                                Type.TXT.ordinal()), ChatActivity.REQUEST_CODE_CONTEXT_MENU);
                return true;
            }
        });

        if (message.direct == Direct.SEND) {
            switch (message.status) {
                case SUCCESS: // 发送成功
                    holder.pb.setVisibility(View.GONE);
                    holder.staus_iv.setVisibility(View.GONE);
                    break;
                case FAIL: // 发送失败
                    holder.pb.setVisibility(View.GONE);
                    holder.staus_iv.setVisibility(View.VISIBLE);
                    break;
                case INPROGRESS: // 发送中
                    holder.pb.setVisibility(View.VISIBLE);
                    holder.staus_iv.setVisibility(View.GONE);
                    break;
                default:
                    // 发送消息
                    sendMsgInBackground(message, holder);
            }
        }
    }

    /**
     * 图片消息
     *
     * @param message
     * @param holder
     * @param position
     * @param convertView
     */
    private void handleImageMessage(final EMMessage message, final ViewHolder holder, final int position, View convertView) {
        holder.pb.setTag(position);
        holder.iv.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                activity.startActivityForResult(
                        (new Intent(activity, ContextMenu.class)).putExtra("position", position).putExtra("type",
                                Type.IMAGE.ordinal()), ChatActivity.REQUEST_CODE_CONTEXT_MENU);
                return true;
            }
        });

        // 接收方向的消息
        if (message.direct == Direct.RECEIVE) {
            // "it is receive msg";
            if (message.status == EMMessage.Status.INPROGRESS) {
                // "!!!! back receive";
                holder.iv.setImageResource(R.mipmap.default_image);
                showDownloadImageProgress(message, holder);
                // downloadImage(message, holder);
            } else {
                // "!!!! not back receive, show image directly");
                holder.pb.setVisibility(View.GONE);
                holder.tv.setVisibility(View.GONE);
                holder.iv.setImageResource(R.mipmap.default_image);
                ImageMessageBody imgBody = (ImageMessageBody) message.getBody();
                if (imgBody.getLocalUrl() != null) {
                    // String filePath = imgBody.getLocalUrl();
                    String remotePath = imgBody.getRemoteUrl();
                    String filePath = ImageUtils.getImagePath(remotePath);
                    String thumbRemoteUrl = imgBody.getThumbnailUrl();
                    if (TextUtils.isEmpty(thumbRemoteUrl) && !TextUtils.isEmpty(remotePath)) {
                        thumbRemoteUrl = remotePath;
                    }
                    String thumbnailPath = ImageUtils.getThumbnailImagePath(thumbRemoteUrl);
                    showImageView(thumbnailPath, holder.iv, filePath, imgBody.getRemoteUrl(), message);
                }
            }
            return;
        }

        // 发送的消息
        // process send message
        // send pic, show the pic directly
        ImageMessageBody imgBody = (ImageMessageBody) message.getBody();
        String filePath = imgBody.getLocalUrl();
        if (filePath != null && new File(filePath).exists()) {
            showImageView(ImageUtils.getThumbnailImagePath(filePath), holder.iv, filePath, null, message);
        } else {
            showImageView(ImageUtils.getThumbnailImagePath(filePath), holder.iv, filePath, IMAGE_DIR, message);
        }

        switch (message.status) {
            case SUCCESS:
                holder.pb.setVisibility(View.GONE);
                holder.tv.setVisibility(View.GONE);
                holder.staus_iv.setVisibility(View.GONE);
                break;
            case FAIL:
                holder.pb.setVisibility(View.GONE);
                holder.tv.setVisibility(View.GONE);
                holder.staus_iv.setVisibility(View.VISIBLE);
                break;
            case INPROGRESS:
                holder.staus_iv.setVisibility(View.GONE);
                holder.pb.setVisibility(View.VISIBLE);
                holder.tv.setVisibility(View.VISIBLE);
                if (timers.containsKey(message.getMsgId()))
                    return;
                // set a timer
                final Timer timer = new Timer();
                timers.put(message.getMsgId(), timer);
                timer.schedule(new TimerTask() {

                    @Override
                    public void run() {
                        activity.runOnUiThread(new Runnable() {
                            public void run() {
                                holder.pb.setVisibility(View.VISIBLE);
                                holder.tv.setVisibility(View.VISIBLE);
                                holder.tv.setText(message.progress + "%");
                                if (message.status == EMMessage.Status.SUCCESS) {
                                    holder.pb.setVisibility(View.GONE);
                                    holder.tv.setVisibility(View.GONE);
                                    // message.setSendingStatus(Message.SENDING_STATUS_SUCCESS);
                                    timer.cancel();
                                } else if (message.status == EMMessage.Status.FAIL) {
                                    holder.pb.setVisibility(View.GONE);
                                    holder.tv.setVisibility(View.GONE);
                                    // message.setSendingStatus(Message.SENDING_STATUS_FAIL);
                                    // message.setProgress(0);
                                    holder.staus_iv.setVisibility(View.VISIBLE);

                                    Toast.makeText(activity,
                                            activity.getString(R.string.send_fail) + activity.getString(R.string.connect_failuer_toast), Toast.LENGTH_SHORT)
                                            .show();
                                    timer.cancel();
                                }

                            }
                        });

                    }
                }, 0, 500);
                break;
            default:
                sendPictureMessage(message, holder);
        }
    }

    /**
     * 语音消息
     *
     * @param message
     * @param holder
     * @param position
     * @param convertView
     */
    private void handleVoiceMessage(final EMMessage message, final ViewHolder holder, final int position, View convertView) {
        VoiceMessageBody voiceBody = (VoiceMessageBody) message.getBody();
        int len = voiceBody.getLength();
        if (len > 0) {
            holder.tv.setText(voiceBody.getLength() + "\"");
            holder.tv.setVisibility(View.VISIBLE);
        } else {
            holder.tv.setVisibility(View.INVISIBLE);
        }
        holder.iv.setOnClickListener(new VoicePlayClickListener(message, holder.iv, holder.iv_read_status, this, activity, username));
        holder.iv.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                activity.startActivityForResult(
                        (new Intent(activity, ContextMenu.class)).putExtra("position", position).putExtra("type",
                                Type.VOICE.ordinal()), ChatActivity.REQUEST_CODE_CONTEXT_MENU);
                return true;
            }
        });
        if (((ChatActivity) activity).playMsgId != null
                && ((ChatActivity) activity).playMsgId.equals(message
                .getMsgId()) && VoicePlayClickListener.isPlaying) {
            AnimationDrawable voiceAnimation;
            if (message.direct == Direct.RECEIVE) {
                holder.iv.setImageResource(R.anim.voice_from_icon);
            } else {
                holder.iv.setImageResource(R.anim.voice_to_icon);
            }
            voiceAnimation = (AnimationDrawable) holder.iv.getDrawable();
            voiceAnimation.start();
        } else {
            if (message.direct == Direct.RECEIVE) {
                holder.iv.setImageResource(R.mipmap.chatfrom_voice_playing);
            } else {
                holder.iv.setImageResource(R.mipmap.chatto_voice_playing);
            }
        }
        if (message.direct == Direct.RECEIVE) {
            if (message.isListened()) {
                // 隐藏语音未听标志
                holder.iv_read_status.setVisibility(View.INVISIBLE);
            } else {
                holder.iv_read_status.setVisibility(View.VISIBLE);
            }
            EMLog.d(TAG, "it is receive msg");
            if (message.status == EMMessage.Status.INPROGRESS) {
                holder.pb.setVisibility(View.VISIBLE);
                EMLog.d(TAG, "!!!! back receive");
                ((FileMessageBody) message.getBody()).setDownloadCallback(new EMCallBack() {

                    @Override
                    public void onSuccess() {
                        activity.runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                holder.pb.setVisibility(View.INVISIBLE);
                                notifyDataSetChanged();
                            }
                        });

                    }

                    @Override
                    public void onProgress(int progress, String status) {
                    }

                    @Override
                    public void onError(int code, String message) {
                        activity.runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                holder.pb.setVisibility(View.INVISIBLE);
                            }
                        });

                    }
                });

            } else {
                holder.pb.setVisibility(View.INVISIBLE);

            }
            return;
        }

        // until here, deal with send voice msg
        switch (message.status) {
            case SUCCESS:
                holder.pb.setVisibility(View.GONE);
                holder.staus_iv.setVisibility(View.GONE);
                break;
            case FAIL:
                holder.pb.setVisibility(View.GONE);
                holder.staus_iv.setVisibility(View.VISIBLE);
                break;
            case INPROGRESS:
                holder.pb.setVisibility(View.VISIBLE);
                holder.staus_iv.setVisibility(View.GONE);
                break;
            default:
                sendMsgInBackground(message, holder);
        }
    }

    /**
     * 发送消息
     *
     * @param message
     * @param holder
     */
    public void sendMsgInBackground(final EMMessage message, final ViewHolder holder) {
        holder.staus_iv.setVisibility(View.GONE);
        holder.pb.setVisibility(View.VISIBLE);

        final long start = System.currentTimeMillis();
        // 调用sdk发送异步发送方法
        EMChatManager.getInstance().sendMessage(message, new EMCallBack() {

            @Override
            public void onSuccess() {

                updateSendedView(message, holder);
            }

            @Override
            public void onError(int code, String error) {

                updateSendedView(message, holder);
            }

            @Override
            public void onProgress(int progress, String status) {
            }

        });

    }

    /*
     * chat sdk will automatic download thumbnail image for the image message we
     * need to register callback show the download progress
     */
    private void showDownloadImageProgress(final EMMessage message, final ViewHolder holder) {
        EMLog.d(TAG, "!!! show download image progress");
        // final ImageMessageBody msgbody = (ImageMessageBody)
        // message.getBody();
        final FileMessageBody msgbody = (FileMessageBody) message.getBody();
        if (holder.pb != null)
            holder.pb.setVisibility(View.VISIBLE);
        if (holder.tv != null)
            holder.tv.setVisibility(View.VISIBLE);

        msgbody.setDownloadCallback(new EMCallBack() {

            @Override
            public void onSuccess() {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // message.setBackReceive(false);
                        if (message.getType() == Type.IMAGE) {
                            holder.pb.setVisibility(View.GONE);
                            holder.tv.setVisibility(View.GONE);
                        }
                        notifyDataSetChanged();
                    }
                });
            }

            @Override
            public void onError(int code, String message) {

            }

            @Override
            public void onProgress(final int progress, String status) {
                if (message.getType() == Type.IMAGE) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            holder.tv.setText(progress + "%");

                        }
                    });
                }

            }

        });
    }

    /*
     * send message with new sdk
     */
    private void sendPictureMessage(final EMMessage message, final ViewHolder holder) {
        try {
            String to = message.getTo();

            // before send, update ui
            holder.staus_iv.setVisibility(View.GONE);
            holder.pb.setVisibility(View.VISIBLE);
            holder.tv.setVisibility(View.VISIBLE);
            holder.tv.setText("0%");

            final long start = System.currentTimeMillis();
            // if (chatType == ChatActivity.CHATTYPE_SINGLE) {
            EMChatManager.getInstance().sendMessage(message, new EMCallBack() {

                @Override
                public void onSuccess() {
                    Log.d(TAG, "send image message successfully");
                    activity.runOnUiThread(new Runnable() {
                        public void run() {
                            holder.pb.setVisibility(View.GONE);
                            holder.tv.setVisibility(View.GONE);
                        }
                    });
                }

                @Override
                public void onError(int code, String error) {

                    activity.runOnUiThread(new Runnable() {
                        public void run() {
                            holder.pb.setVisibility(View.GONE);
                            holder.tv.setVisibility(View.GONE);
                            // message.setSendingStatus(Message.SENDING_STATUS_FAIL);
                            holder.staus_iv.setVisibility(View.VISIBLE);
                            Toast.makeText(activity,
                                    activity.getString(R.string.send_fail) + activity.getString(R.string.connect_failuer_toast), Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onProgress(final int progress, String status) {
                    activity.runOnUiThread(new Runnable() {
                        public void run() {
                            holder.tv.setText(progress + "%");
                        }
                    });
                }

            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 更新ui上消息发送状态
     *
     * @param message
     * @param holder
     */
    private void updateSendedView(final EMMessage message, final ViewHolder holder) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // send success
                if (message.getType() == Type.VIDEO) {
                    holder.tv.setVisibility(View.GONE);
                }
                EMLog.d(TAG, "message status : " + message.status);
                if (message.status == EMMessage.Status.SUCCESS) {
                    // if (message.getType() == EMMessage.Type.FILE) {
                    // holder.pb.setVisibility(View.INVISIBLE);
                    // holder.staus_iv.setVisibility(View.INVISIBLE);
                    // } else {
                    // holder.pb.setVisibility(View.GONE);
                    // holder.staus_iv.setVisibility(View.GONE);
                    // }

                } else if (message.status == EMMessage.Status.FAIL) {
                    // if (message.getType() == EMMessage.Type.FILE) {
                    // holder.pb.setVisibility(View.INVISIBLE);
                    // } else {
                    // holder.pb.setVisibility(View.GONE);
                    // }
                    // holder.staus_iv.setVisibility(View.VISIBLE);

                    if (message.getError() == EMError.MESSAGE_SEND_INVALID_CONTENT) {
                        Toast.makeText(activity, activity.getString(R.string.send_fail) + activity.getString(R.string.error_send_invalid_content), Toast.LENGTH_SHORT)
                                .show();
                    } else if (message.getError() == EMError.MESSAGE_SEND_NOT_IN_THE_GROUP) {
                        Toast.makeText(activity, activity.getString(R.string.send_fail) + activity.getString(R.string.error_send_not_in_the_group), Toast.LENGTH_SHORT)
                                .show();
                    } else {
                        Toast.makeText(activity, activity.getString(R.string.send_fail) + activity.getString(R.string.connect_failuer_toast), Toast.LENGTH_SHORT)
                                .show();
                    }
                }

                notifyDataSetChanged();
            }
        });
    }

    /**
     * load image into image view
     *
     * @param thumbernailPath
     * @param iv
     * @return the image exists or not
     */
    private boolean showImageView(final String thumbernailPath, final ImageView iv, final String localFullSizePath, String remoteDir,
                                  final EMMessage message) {
        // String imagename =
        // localFullSizePath.substring(localFullSizePath.lastIndexOf("/") + 1,
        // localFullSizePath.length());
        // final String remote = remoteDir != null ? remoteDir+imagename :
        // imagename;
        final String remote = remoteDir;
        EMLog.d("###", "local = " + localFullSizePath + " remote: " + remote);
        // first check if the thumbnail image already loaded into cache
        Bitmap bitmap = ImageCache.getInstance().get(thumbernailPath);
        if (bitmap != null) {
            // thumbnail image is already loaded, reuse the drawable
            iv.setImageBitmap(bitmap);
            iv.setClickable(true);
            iv.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    EMLog.d(TAG, "image view on click");
                    Intent intent = new Intent(activity, ShowBigImage.class);
                    File file = new File(localFullSizePath);
                    if (file.exists()) {
                        Uri uri = Uri.fromFile(file);
                        intent.putExtra("uri", uri);
                        EMLog.d(TAG, "here need to check why download everytime");
                    } else {
                        // The local full size pic does not exist yet.
                        // ShowBigImage needs to download it from the server
                        // first
                        // intent.putExtra("", message.get);
                        ImageMessageBody body = (ImageMessageBody) message.getBody();
                        intent.putExtra("secret", body.getSecret());
                        intent.putExtra("remotepath", remote);
                    }
                    if (message != null && message.direct == Direct.RECEIVE && !message.isAcked
                            && message.getChatType() != ChatType.GroupChat && message.getChatType() != ChatType.ChatRoom) {
                        try {
                            EMChatManager.getInstance().ackMessageRead(message.getFrom(), message.getMsgId());
                            message.isAcked = true;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    activity.startActivity(intent);
                }
            });
            return true;
        } else {
            new LoadImageTask().execute(thumbernailPath, localFullSizePath, remote, message.getChatType(), iv, activity, message);
            return true;
        }

    }

    public static class ViewHolder {
        ImageView iv;
        TextView tv;
        ProgressBar pb;
        ImageView staus_iv;
        ImageView iv_avatar;
        TextView tv_usernick;
        ImageView iv_read_status;
        // 显示已读回执状态
        TextView tv_ack;
        // 显示送达回执状态
        TextView tv_delivered;
    }
}
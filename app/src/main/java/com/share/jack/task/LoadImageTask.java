package com.share.jack.task;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ImageView;

import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMMessage;
import com.easemob.chat.EMMessage.ChatType;
import com.easemob.util.ImageUtils;
import com.share.jack.demoutils.CommonUtils;
import com.share.jack.demoutils.ImageCache;
import com.share.jack.swingtravel.ShowBigImage;

import java.io.File;

public class LoadImageTask extends AsyncTask<Object, Void, Bitmap> {
	private ImageView iv = null;
	String localFullSizePath = null;
	String thumbnailPath = null;
	String remotePath = null;
	EMMessage message = null;
	ChatType chatType;
	Activity activity;

	@Override
	protected Bitmap doInBackground(Object... args) {
		thumbnailPath = (String) args[0];
		localFullSizePath = (String) args[1];
		remotePath = (String) args[2];
		chatType = (ChatType) args[3];
		iv = (ImageView) args[4];
		// if(args[2] != null) {
		activity = (Activity) args[5];
		// }
		message = (EMMessage) args[6];
		File file = new File(thumbnailPath);
		if (file.exists()) {
			return ImageUtils.decodeScaleImage(thumbnailPath, 160, 160);
		} else {
			if (message.direct == EMMessage.Direct.SEND) {
				return ImageUtils.decodeScaleImage(localFullSizePath, 160, 160);
			} else {
				return null;
			}
		}
		

	}

	protected void onPostExecute(Bitmap image) {
		if (image != null) {
			iv.setImageBitmap(image);
			ImageCache.getInstance().put(thumbnailPath, image);
			iv.setClickable(true);
			iv.setTag(thumbnailPath);
			iv.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (thumbnailPath != null) {
						Intent intent = new Intent(activity, ShowBigImage.class);
						File file = new File(localFullSizePath);
						if (file.exists()) {
							Uri uri = Uri.fromFile(file);
							intent.putExtra("uri", uri);
						} else {
							// The local full size pic does not exist yet.
							// ShowBigImage needs to download it from the server
							// first
							intent.putExtra("remotepath", remotePath);
						}
						if (message.getChatType() != ChatType.Chat) {
							// delete the image from server after download
						}
						if (message != null && message.direct == EMMessage.Direct.RECEIVE && !message.isAcked && message.getChatType() != ChatType.GroupChat && message.getChatType() != ChatType.ChatRoom) {
							message.isAcked = true;
							try {
								// 看了大图后发个已读回执给对方
								EMChatManager.getInstance().ackMessageRead(message.getFrom(), message.getMsgId());
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						activity.startActivity(intent);
					}
				}
			});
		} else {
			if (message.status == EMMessage.Status.FAIL) {
				if (CommonUtils.isNetWorkConnected(activity)) {
					new Thread(new Runnable() {
						@Override
						public void run() {
							EMChatManager.getInstance().asyncFetchMessage(message);
						}
					}).start();
				}
			}
		}
	}
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
	}
}

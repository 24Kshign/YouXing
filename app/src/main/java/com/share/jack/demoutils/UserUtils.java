package com.share.jack.demoutils;

import android.content.Context;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.share.jack.controller.HXSDKHelper;
import com.share.jack.domain.User;
import com.share.jack.model.DemoHXSDKHelper;
import com.share.jack.swingtravel.R;
import com.share.jack.utils.GlideCircleTransform;

public class UserUtils {
    /**
     * 根据username获取相应user，由于demo没有真实的用户数据，这里给的模拟的数据；
     *
     * @return
     */
    public static User getUserInfo(String username) {
        User user = ((DemoHXSDKHelper) HXSDKHelper.getInstance()).getContactList().get(username);
        if (user == null) {
            user = new User(username);
        }
        if (user != null) {
            //demo没有这些数据，临时填充
            if (TextUtils.isEmpty(user.getNick()))
                user.setNick(username);
        }
        return user;
    }

    /**
     * 设置用户头像
     *
     * @param username
     */
    public static void setUserAvatar(Context context, String username, ImageView imageView) {
        RequestManager glideRequest;
        glideRequest = Glide.with(context);
        User user = getUserInfo(username);
        if (user != null && user.getAvatar() != null) {
            Glide.with(context).load(user.getAvatar()).asBitmap()
                    .placeholder(R.mipmap.default_avatar)
                    .transform(new GlideCircleTransform(context))
                    .into(imageView);
        } else {
            Glide.with(context).load(R.mipmap.default_avatar).asBitmap()
                    .transform(new GlideCircleTransform(context))
                    .into(imageView);
        }
    }

    /**
     * 设置当前用户头像
     */
    public static void setCurrentUserAvatar(Context context, ImageView imageView) {
        RequestManager glideRequest;
        glideRequest = Glide.with(context);
        User user = ((DemoHXSDKHelper) HXSDKHelper.getInstance()).getUserProfileManager().getCurrentUserInfo();
        if (user != null && user.getAvatar() != null) {
            Glide.with(context).load(user.getAvatar()).asBitmap()
                    .placeholder(R.mipmap.default_avatar)
                    .transform(new GlideCircleTransform(context))
                    .into(imageView);
        } else {
            Glide.with(context).load(R.mipmap.default_avatar).asBitmap()
                    .transform(new GlideCircleTransform(context))
                    .into(imageView);
        }
    }

    /**
     * 设置用户昵称
     */
    public static void setUserNick(String username, TextView textView) {
        User user = getUserInfo(username);
        if (user != null) {
            textView.setText(user.getNick());
        } else {
            textView.setText(username);
        }
    }

    /**
     * 设置当前用户昵称
     */
    public static void setCurrentUserNick(TextView textView) {
        User user = ((DemoHXSDKHelper) HXSDKHelper.getInstance()).getUserProfileManager().getCurrentUserInfo();
        if (textView != null) {
            textView.setText(user.getNick());
        }
    }

    /**
     * 设置当前用户昵称
     */
    public static void setCurrentUserNick(EditText editText) {
        User user = ((DemoHXSDKHelper) HXSDKHelper.getInstance()).getUserProfileManager().getCurrentUserInfo();
        if (editText != null) {
            editText.setText(user.getNick());
        }
    }

    /**
     * 保存或更新某个用户
     */
    public static void saveUserInfo(User newUser) {
        if (newUser == null || newUser.getUsername() == null) {
            return;
        }
        ((DemoHXSDKHelper) HXSDKHelper.getInstance()).saveContact(newUser);
    }

}

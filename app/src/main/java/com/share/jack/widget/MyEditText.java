package com.share.jack.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.EditText;

/**
 * Author:Created by JackCheng
 * Email:17764576259@163.com
 * Time:2016/2/16 12:30
 * Copyright:1.0
 */
public class MyEditText extends EditText {


    public MyEditText(Context context) {
        super(context);
    }

    public MyEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void insertDrawable(EditText editText, Bitmap bm, int screenWidth, String path) {
        final SpannableString ss = new SpannableString(path);
        //得到drawable对象，即所要插入的图片
        Drawable d = new BitmapDrawable(bm);
        Log.d("Edittext---->", "picWidth=" + d.getIntrinsicWidth());
        if (screenWidth < d.getIntrinsicWidth()) {
            d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
        } else {
            d.setBounds((screenWidth - d.getIntrinsicWidth()) / 2, 0
                    , d.getIntrinsicWidth() + (screenWidth - d.getIntrinsicWidth()) / 2, d.getIntrinsicHeight());
        }
        //用这个drawable对象代替字符串easy
        ImageSpan span = new ImageSpan(d, ImageSpan.ALIGN_BASELINE);
        //包括0但是不包括path.length()即：4。[0,4)。实值得注意的是当我们复制这个图片的时候，际是复制了"easy"这个字符串。
        ss.setSpan(span, 0, path.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        if (!editText.getText().toString().equals("")) {
            append("\n");
        }
        append(ss);
        append("\n");
    }
}

package com.share.jack.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * Author:Created by JackCheng
 * Email:17764576259@163.com
 * Time:2016/1/28 15:21
 * Copyright:1.0
 */
public class ArticleBean {

    public String mHeadImgUrl;   //评论人头像
    public String mCommnetId;    //评论人Id
    public String mNickName;    //评论人昵称
    public String mTime;   //评论时间
    public String mContent; //评论内容
    public String isMe;     //是不是自己
    public List<CommentBean> mCommentList = new ArrayList<CommentBean>();

}

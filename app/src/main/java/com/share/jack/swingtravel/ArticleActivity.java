package com.share.jack.swingtravel;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.loopj.android.http.RequestParams;
import com.share.jack.adapter.ArticleAdapter;
import com.share.jack.adapter.EmotionGridViewAdapter;
import com.share.jack.adapter.EmotionPagerAdapter;
import com.share.jack.bean.ArticleBean;
import com.share.jack.bean.CommentBean;
import com.share.jack.bean.ShopBean;
import com.share.jack.http.NetCallBack;
import com.share.jack.http.RequestUtils;
import com.share.jack.utils.Expression;
import com.share.jack.utils.GlideCircleTransform;
import com.share.jack.utils.YXConstant;
import com.share.jack.widget.MyNoScrollListView;
import com.share.jack.widget.MyProgressDialog;
import com.share.jack.widget.MyScrollView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Author:Created by JackCheng
 * Email:17764576259@163.com
 * Time:2016/1/28 14:55
 * Copyright:1.0
 */

public class ArticleActivity extends BaseActivity implements View.OnClickListener
        , MyScrollView.ScrollListener {

    private static final String TAG = "";
    private static final int FINISH_GET_IMAGE = 0x110;
    private static final int REPLY_MESSEGE = 1011;

    @Bind(R.id.ltb_rv_left)
    RelativeLayout mRvLeft;
    @Bind(R.id.ltb_tv_left)
    TextView mTvLeft;
    @Bind(R.id.ltb_tv_title)
    TextView mTvTitle;
    @Bind(R.id.ltb_btn_right)
    Button mBtnRight;

    @Bind(R.id.aa_iv_head)
    ImageView mIvHead;
    @Bind(R.id.aa_iv_main_pic)
    ImageView mIvMainImage;
    @Bind(R.id.aa_tv_nickname)
    TextView mTvNickname;
    @Bind(R.id.aa_tv_time)
    TextView mTvTime;
    @Bind(R.id.aa_tv_read_num)
    TextView mTvReadNum;
    @Bind(R.id.aa_tv_title)
    TextView mTvArticleTitle;
    @Bind(R.id.aa_tv_content)
    TextView mTvContent;
    @Bind(R.id.aa_tv_location)
    TextView mTvLocation;


    @Bind(R.id.aa_listview)
    MyNoScrollListView mListView;
    @Bind(R.id.aa_rv_praise)
    RelativeLayout mRvPraise;
    @Bind(R.id.aa_rv_comment)
    RelativeLayout mRvComment;
    @Bind(R.id.aa_lv_bottom)
    LinearLayout mLvBottom;
    @Bind(R.id.aa_tv_praise_num)
    TextView mTvPraiseNum;
    @Bind(R.id.aa_tv_comment_num)
    TextView mTvCommentNum;
    @Bind(R.id.aa_rv_want_go)
    RelativeLayout mRvWantGo;
    @Bind(R.id.aa_rv_been_gone)
    RelativeLayout mRvBeenGone;
    @Bind(R.id.aa_sv_scroll)
    MyScrollView mScroll;
    @Bind(R.id.aa_lv_comment)
    LinearLayout mLvComment;
    @Bind(R.id.lc_rv_content)
    RelativeLayout mRvCommentFrame;
    @Bind(R.id.aa_btn_send)
    Button mBtnSend;
    @Bind(R.id.aa_btn_emotion)
    Button mBtnEmotion;
    @Bind(R.id.aa_et_comment)
    EditText mEtComment;
    @Bind(R.id.lc_push_emoj_viewpager)
    ViewPager emojPager;
    @Bind(R.id.aa_rv_main)
    RelativeLayout mRvMain;

    private boolean isFirst = true;    //首次进入
    private int flagUpOrDown = 2;

    private List<ArticleBean> mDatas = new ArrayList<ArticleBean>();
    private ArticleAdapter mAdapter;

    private int position;                //记录回复评论的索引
    private boolean isReply;            //是否是回复
    private String strComment;      //评论的内容
    InputMethodManager inputMethodManager;
    private boolean isOpen = false;      //判断表情是否弹出
    private ArrayList<GridView> mGridViews;
    private ShopBean shopBean = null;
    private SharedPreferences sp = null;
    private boolean isZan = false;
    private MyProgressDialog mDialog;
    private List<Bitmap> mBitmaps = new ArrayList<>();
    private String[] mPicUrls;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == REPLY_MESSEGE) {
                if (sp.getString(YXConstant.USER_TOKEN, "false").equals("false")) {   //未登陆
                    startActivity(new Intent(ArticleActivity.this, LoginActivity.class).putExtra("isLoging", "jack"));
                    return;
                }
                isReply = true;
                position = (Integer) msg.obj;
                showMyEdit();
            } else if (msg.what == FINISH_GET_IMAGE) {
                hideProgressDialog();
                mRvMain.setVisibility(View.VISIBLE);
                initeView();
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        setContentView(R.layout.activity_article);
        ButterKnife.bind(this);
        sp = getSharedPreferences(YXConstant.USER, MODE_PRIVATE);
        shopBean = getIntent().getParcelableExtra(YXConstant.PAR_KEY);
        if (!shopBean.getOtherImage().equals("")) {
            showProgressDialog("加载中....");
            mPicUrls = shopBean.getOtherImage().split(",");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < mPicUrls.length; i++) {
                        mBitmaps.add(returnBitMap(mPicUrls[i]));
                    }
                    Message msg = mHandler.obtainMessage();
                    msg.what = FINISH_GET_IMAGE;
                    mHandler.sendMessage(msg);
                }
            }).start();
        } else {
            mRvMain.setVisibility(View.VISIBLE);
            initeView();
        }
        updateReadNum();
    }

    private void updateReadNum() {
        String url = "http://115.28.101.140/youxing/Home/Recomment/updataReadNum";
        RequestParams params = new RequestParams();
        params.put("Recomment_Id", shopBean.getRecommentId());
        params.put("Read_Num", shopBean.getReadNum());
        RequestUtils.ClientPost(url, params, new NetCallBack() {
            @Override
            public void onMySuccess(byte[] response) {
                try {
                    JSONObject jsonObject = new JSONObject(new String(response));
                    if (jsonObject.getString("result").equals("success")) {
                        Log.d(TAG, jsonObject.getJSONObject("response").getString("message"));
                    } else {
                        Log.d(TAG, jsonObject.getJSONObject("response").getString("message"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onMyFailure(byte[] response, Throwable throwable) {

            }
        });
    }

    /**
     * @param url
     * @return 根据url获取图片的bitmap
     */
    public Bitmap returnBitMap(String url) {
        URL myFileUrl = null;
        Bitmap bitmap = null;
        try {
            myFileUrl = new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        try {
            HttpURLConnection conn = (HttpURLConnection) myFileUrl.openConnection();
            conn.setDoInput(true);
            conn.connect();
            InputStream is = conn.getInputStream();
            bitmap = BitmapFactory.decodeStream(is);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    private void initeView() {
        mRvLeft.setVisibility(View.VISIBLE);
        mBtnRight.setVisibility(View.VISIBLE);
        mTvTitle.setVisibility(View.GONE);
        mTvLeft.setText(getResources().getString(R.string.back));
        mBtnRight.setBackgroundResource(R.mipmap.aa_iv_share);
        mRvPraise.setOnClickListener(this);
        mRvComment.setOnClickListener(this);
        mLvComment.setVisibility(View.VISIBLE);
        mBtnRight.setOnClickListener(this);
        mRvLeft.setOnClickListener(this);
        mRvWantGo.setOnClickListener(this);
        mRvBeenGone.setOnClickListener(this);
        mListView.setFocusable(false);
        mScroll.smoothScrollBy(0, 10);
        mScroll.setScrollListener(this);
        asyncHttpPost();    //获取文章评论列表

        if (mAdapter == null) {
            mListView.setAdapter(null);
        }

        if (!sp.getString(YXConstant.USER_TOKEN, "false").equals("false")) {    //登陆过
            isWantGo();      //判断是否想去
            isBeenGone();    //判断是否去过
            isPraised();     //判断用户是否点赞
        }

        if (shopBean.getUserHead() != null) {
            Glide.with(this).load(shopBean.getUserHead()).placeholder(R.mipmap.default_avatar)
                    .error(R.mipmap.ic_launcher)
                    .transform(new GlideCircleTransform(this)).into(mIvHead);
        } else {
            Glide.with(this).load(R.mipmap.default_avatar)
                    .transform(new GlideCircleTransform(this)).into(mIvHead);
        }
        if (shopBean.getMainImage() != null) {
            Glide.with(this).load(shopBean.getMainImage()).placeholder(R.mipmap.ic_launcher)
                    .error(R.mipmap.ic_launcher)
                    .centerCrop().into(mIvMainImage);
        } else {
            Glide.with(this).load(R.mipmap.ic_launcher).centerCrop().into(mIvMainImage);
        }
        setContent();
    }

    private void setContent() {
        mTvNickname.setText(shopBean.getNickName());
        mTvTime.setText(shopBean.getTime());
        mTvArticleTitle.setText(shopBean.getTitle());
        mTvReadNum.setText(shopBean.getReadNum());
        mTvLocation.setText(shopBean.getLocation());

        if (shopBean.getCommentNum().equals("")) {
            mTvCommentNum.setVisibility(View.GONE);
        } else {
            mTvCommentNum.setVisibility(View.VISIBLE);
            mTvCommentNum.setText(shopBean.getCommentNum());
        }
        if (shopBean.getPraiseNum().equals("")) {
            mTvPraiseNum.setVisibility(View.GONE);
        } else {
            mTvPraiseNum.setVisibility(View.VISIBLE);
            mTvPraiseNum.setText(shopBean.getPraiseNum());
        }

        if (shopBean.getOtherImage().equals("")) {
            mTvContent.setText(shopBean.getContent());
        } else {
            SpannableString spanString = new SpannableString(shopBean.getContent());
            int cnt = 0;
            for (int i = 0; i < shopBean.getContent().length(); i++) {
                if (shopBean.getContent().charAt(i) == '★') {
                    Drawable d = new BitmapDrawable(mBitmaps.get(cnt++));
                    if (getScreenWidth() < d.getIntrinsicWidth()) {
                        d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
                    } else {
                        d.setBounds((getScreenWidth() - d.getIntrinsicWidth()) / 2, 0
                                , d.getIntrinsicWidth() + (getScreenWidth() - d.getIntrinsicWidth()) / 2, d.getIntrinsicHeight());
                    }
                    ImageSpan span = new ImageSpan(d, ImageSpan.ALIGN_BASELINE);
                    if (i == shopBean.getContent().length() - 1) {
                        spanString.setSpan(span, i - 1, i, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                    }
                    spanString.setSpan(span, i, i + 1, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                }
            }
            mTvContent.append(spanString);
        }
    }

    private void showProgressDialog(String messege) {
        if (null == mDialog)
            mDialog = MyProgressDialog.createProgrssDialog(this);
        if (null != mDialog) {
            mDialog.setMessege(messege);
            mDialog.show();
        }
    }

    private void hideProgressDialog() {
        if (null != mDialog) {
            mDialog.dismiss();
            mDialog = null;
        }
    }

    private List<ArticleBean> getJsonDatas(String result) {
        List<ArticleBean> list = new ArrayList<>();
        ArticleBean bean;
        try {
            JSONObject jsonObject = new JSONObject(result);
            if (jsonObject.getString("result").equals("success")) {
                JSONArray jsonArray = jsonObject.getJSONObject("response").getJSONArray("data");
                for (int i = 0; i < jsonArray.length(); i++) {
                    jsonObject = jsonArray.getJSONObject(i);
                    bean = new ArticleBean();
                    bean.mHeadImgUrl = jsonObject.getString("Author_Head");
                    bean.mNickName = jsonObject.getString("Author_Nick");
                    bean.mContent = jsonObject.getString("Comment");
                    bean.mCommnetId = jsonObject.getString("Comment_Id");
                    bean.mTime = jsonObject.getString("Time");
                    bean.isMe = "0";
                    bean.mCommentList = getReplyData(jsonObject.getString("Comment_Id"));
                    list.add(bean);
                }
            } else {
                showToast("获取用户评论失败" + jsonObject.getJSONObject("response").getString("message"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }

    private void asyncHttpPost() {
        String url = "http://115.28.101.140/youxing/Home/Comment/getArticleMainCommentList";
        RequestParams params = new RequestParams();
        params.put("Recomment_Id", shopBean.getRecommentId());
        Log.e(TAG, "recommentId=" + shopBean.getRecommentId());
        RequestUtils.ClientGet(url, params, new NetCallBack() {
            @Override
            public void onMySuccess(byte[] response) {
                mDatas = getJsonDatas(new String(response));
                mAdapter = new ArticleAdapter(ArticleActivity.this, mDatas, mHandler);
                mListView.setAdapter(mAdapter);
            }

            @Override
            public void onMyFailure(byte[] response, Throwable throwable) {
                Log.d(TAG, getString(R.string.fail_to_link_server) + new String() + "\n" + throwable.toString());
                showToast(getString(R.string.fail_to_link_server) + new String() + "\n" + throwable.toString());
            }
        });
    }

    private void isPraised() {
        String url = "http://115.28.101.140/youxing/Home/Recomment/isZan";
        RequestParams params = new RequestParams();
        params.put("User_ID", String.valueOf(sp.getInt(YXConstant.USER_ID, 0)));
        params.put("Token", sp.getString(YXConstant.USER_TOKEN, ""));
        params.put("Recomment_Id", shopBean.getRecommentId());
        RequestUtils.ClientPost(url, params, new NetCallBack() {
            @Override
            public void onMySuccess(byte[] response) {
                try {
                    JSONObject jsonObject = new JSONObject(new String(response));
                    if (jsonObject.getString("result").equals("success")) {
                        if (jsonObject.getJSONObject("response").getString("message").equals("YES")) {
                            isZan = true;
                            mRvPraise.setBackgroundColor(ContextCompat.getColor(ArticleActivity.this
                                    , R.color.btnLoginGreenDisable));
                        } else {
                            isZan = false;
                            mRvPraise.setBackgroundColor(ContextCompat.getColor(ArticleActivity.this
                                    , R.color.btnLoginGreenNormal));
                        }
                    } else {
                        isZan = false;
                        mRvPraise.setBackgroundColor(ContextCompat.getColor(ArticleActivity.this
                                , R.color.btnLoginGreenNormal));
                        Log.d(TAG, jsonObject.getJSONObject("response").getString("message"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onMyFailure(byte[] response, Throwable throwable) {
                isZan = false;
                mRvPraise.setBackgroundColor(ContextCompat.getColor(ArticleActivity.this
                        , R.color.btnLoginGreenNormal));
                Log.d(TAG, getString(R.string.fail_to_link_server) + new String() + "\n" + throwable.toString());
                showToast(getString(R.string.fail_to_link_server) + new String() + "\n" + throwable.toString());
            }
        });
    }

    private void isWantGo() {
        String url = "http://115.28.101.140/youxing/Home/Recomment/isWantGo";
        RequestParams params = new RequestParams();
        params.put("User_ID", String.valueOf(sp.getInt(YXConstant.USER_ID, 0)));
        params.put("Token", sp.getString(YXConstant.USER_TOKEN, ""));
        params.put("Recomment_Id", shopBean.getRecommentId());
        RequestUtils.ClientPost(url, params, new NetCallBack() {
            @Override
            public void onMySuccess(byte[] response) {
                try {
                    JSONObject jsonObject = new JSONObject(new String(response));
                    if (jsonObject.getString("result").equals("success")) {
                        if (jsonObject.getJSONObject("response").getString("message").equals("YES")) {
                            mRvWantGo.setEnabled(false);
                        } else {
                            mRvWantGo.setEnabled(true);
                        }
                    } else {
                        mRvWantGo.setEnabled(true);
                        Log.d(TAG, jsonObject.getJSONObject("response").getString("message"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onMyFailure(byte[] response, Throwable throwable) {
                mRvWantGo.setEnabled(true);
                Log.d(TAG, getString(R.string.fail_to_link_server) + new String() + "\n" + throwable.toString());
                showToast(getString(R.string.fail_to_link_server) + new String() + "\n" + throwable.toString());
            }
        });
    }

    private void isBeenGone() {
        String url = "http://115.28.101.140/youxing/Home/Recomment/isBeen";
        RequestParams params = new RequestParams();
        params.put("User_ID", String.valueOf(sp.getInt(YXConstant.USER_ID, 0)));
        params.put("Token", sp.getString(YXConstant.USER_TOKEN, ""));
        params.put("Recomment_Id", shopBean.getRecommentId());
        RequestUtils.ClientPost(url, params, new NetCallBack() {
            @Override
            public void onMySuccess(byte[] response) {
                try {
                    JSONObject jsonObject = new JSONObject(new String(response));
                    if (jsonObject.getString("result").equals("success")) {
                        if (jsonObject.getJSONObject("response").getString("message").equals("YES")) {
                            mRvBeenGone.setEnabled(false);
                        } else {
                            mRvBeenGone.setEnabled(true);
                        }
                    } else {
                        mRvBeenGone.setEnabled(true);
                        Log.d(TAG, jsonObject.getJSONObject("response").getString("message"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onMyFailure(byte[] response, Throwable throwable) {
                mRvBeenGone.setEnabled(true);
                Log.d(TAG, getString(R.string.fail_to_link_server) + new String() + "\n" + throwable.toString());
                showToast(getString(R.string.fail_to_link_server) + new String() + "\n" + throwable.toString());
            }
        });
    }

    private void showMyEdit() {
        mLvBottom.setVisibility(View.VISIBLE);
        mLvComment.setVisibility(View.GONE);
        mRvCommentFrame.setVisibility(View.VISIBLE);
        showKeyboard(mEtComment);
        initEmojGridview();
        mBtnSend.setOnClickListener(this);
        mBtnEmotion.setOnClickListener(this);
        emojPager.setOnClickListener(this);
        mEtComment.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (isOpen) {
                    showKeyboard(mEtComment);
                    isOpen = false;
                    showEmotion(isOpen);
                }
                return false;
            }
        });
    }

    private void showEmotion(boolean isOpen) {
        if (isOpen) {
            hideKeyboard();
            emojPager.setVisibility(View.VISIBLE);
            mBtnEmotion.setBackgroundResource(R.mipmap.ac_btn_express_pressed);
            initEmotionUp();
        } else {
            showKeyboard(mEtComment);
            emojPager.setVisibility(View.GONE);
            mBtnEmotion.setBackgroundResource(R.mipmap.ac_btn_express_normal);
        }
    }

    //初始化表情
    private void initEmotionUp() {
        Log.e(TAG, "======initEmotionUp=========");
        emojPager.setAdapter(new EmotionPagerAdapter(this, mGridViews));
        emojPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void initEmojGridview() {
        mGridViews = new ArrayList<GridView>();
        LayoutInflater inflater = LayoutInflater.from(this);
        mGridViews.clear();
        for (int i = 0; i < 6; i++) {
            final int j = i;
            GridView gridView = (GridView) inflater.inflate(R.layout.lxw_emoj_gridview, null, false);
            gridView.setAdapter(new EmotionGridViewAdapter(this, i));
            mGridViews.add(gridView);
            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if (position > 0 && (position % 20 == 0) || (j == 5 && position == 5)) {
                        int selectionStart = mEtComment.getSelectionStart();
                        String str = mEtComment.getText().toString();
                        String strTemp = str.substring(0, selectionStart);
                        if (!TextUtils.isEmpty(str)) {
                            int i = strTemp.lastIndexOf("]");
                            if (i == strTemp.length() - 1) {
                                int j = strTemp.lastIndexOf("[");
                                mEtComment.getEditableText().delete(j, selectionStart);
                            } else {
                                mEtComment.getEditableText().delete(strTemp.length() - 1, selectionStart);
                            }
                        }
                    } else {
                        Log.e(TAG, "=====onItemClick===" + position);
                        String str = Expression.emojName[position + j * 20];
                        SpannableString spannableString = new SpannableString(str);
                        Log.e(TAG, "====Expression.getIdAsName(str)===" + Expression.getIdAsName(str));
                        Drawable drawable = ArticleActivity.this.getResources().getDrawable(Expression.getIdAsName(str));
                        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
                        ImageSpan imageSpan = new ImageSpan(drawable, ImageSpan.ALIGN_BASELINE);
                        spannableString.setSpan(imageSpan, 0, str.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        int cuors = mEtComment.getSelectionStart();
                        mEtComment.getText().insert(cuors, spannableString);
                    }
                }
            });
        }
    }

    //显示输入法
    public void showKeyboard(EditText editText) {
        if (editText != null) {
            //设置可获得焦点
            editText.setFocusable(true);
            editText.setFocusableInTouchMode(true);
            //请求获得焦点
            editText.requestFocus();
            //调用系统输入法
            InputMethodManager inputManager = (InputMethodManager) editText
                    .getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.showSoftInput(editText, 0);
        }
    }

    /**
     * 获取回复列表数据
     */
    private List<CommentBean> getReplyData(String commentId) {
        String url = "http://115.28.101.140/youxing/Home/Comment/getArticleChildCommentList";
        RequestParams params = new RequestParams();
        params.put("Comment_Id", commentId);
        params.put("Recomment_Id", shopBean.getRecommentId());
        final List<CommentBean> commentList = new ArrayList<CommentBean>();
        RequestUtils.ClientGet(url, params, new NetCallBack() {
            @Override
            public void onMySuccess(byte[] response) {
                CommentBean bean;
                try {
                    JSONObject jsonObject = new JSONObject(new String(response));
                    if (jsonObject.getString("result").equals("success")) {
                        JSONArray jsonArray = jsonObject.getJSONObject("response").getJSONArray("data");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            jsonObject = jsonArray.getJSONObject(i);
                            bean = new CommentBean();
                            bean.commentId = jsonObject.getString("Author_Id");
                            bean.commentNickname = jsonObject.getString("Author_Nick");
                            bean.replyId = jsonObject.getString("Other_Id");
                            bean.replyNickname = jsonObject.getString("Other_Nick");
                            commentList.add(bean);
                        }
                    } else {
                        Log.d(TAG, jsonObject.getJSONObject("response").getString("message"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onMyFailure(byte[] response, Throwable throwable) {
                Log.d(TAG, getString(R.string.fail_to_link_server) + new String() + "\n" + throwable.toString());
                showToast(getString(R.string.fail_to_link_server) + new String() + "\n" + throwable.toString());
            }
        });
        return commentList;
    }

    /**
     * 发表评论
     */
    private void publishComment() {
        String url = "http://115.28.101.140/youxing/Home/Comment/setComment";
        RequestParams params = new RequestParams();
        Log.d(TAG, "userId=" + String.valueOf(sp.getInt(YXConstant.USER_ID, 0)));
        params.put("User_ID", String.valueOf(sp.getInt(YXConstant.USER_ID, 0)));
        params.put("Token", sp.getString(YXConstant.USER_TOKEN, ""));
        params.put("Comment_Set_User_Id", String.valueOf(sp.getInt(YXConstant.USER_ID, 0)));
        params.put("Comment_Get_User_Id", shopBean.getUserId());
        params.put("Comment_Content", strComment);
        if (!shopBean.getCommentNum().equals("")) {
            params.put("Comment_Id", shopBean.getCommentNum());
        } else {
            params.put("Comment_Id", "0");
        }
        params.put("Reply_Id", "0");
        params.put("Recomment_Id", shopBean.getRecommentId());
        RequestUtils.ClientPost(url, params, new NetCallBack() {
            @Override
            public void onMySuccess(byte[] response) {
                ArticleBean bean = new ArticleBean();
                try {
                    JSONObject jsonObject = new JSONObject(new String(response));
                    if (jsonObject.getString("result").equals("success")) {
                        bean.mHeadImgUrl = sp.getString(YXConstant.USER_HEAD, "");
                        bean.mNickName = sp.getString(YXConstant.USER_NICKNAME, "");
                        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
                        bean.mTime = format.format(new Date(System.currentTimeMillis()));
                        bean.mContent = strComment;
                        bean.mCommnetId = String.valueOf(sp.getInt(YXConstant.USER_ID, 0));
                        bean.isMe = "1";

                        if (shopBean.getCommentNum().equals("")) {
                            mTvCommentNum.setText("1");
                        } else {
                            mTvCommentNum.setText(String.valueOf(Integer.valueOf(mTvCommentNum.getText().toString()) + 1));
                        }

                        mDatas.add(bean);
                        mAdapter.notifyDataSetChanged();
                    } else {
                        showToast("评论失败，请稍后再试" + jsonObject.getJSONObject("response")
                                .getString("message"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onMyFailure(byte[] response, Throwable throwable) {
                Log.d(TAG, getString(R.string.fail_to_link_server) + new String() + "\n" + throwable.toString());
                showToast(getString(R.string.fail_to_link_server) + new String() + "\n" + throwable.toString());
            }
        });
    }

    /**
     * 回复评论
     */
    private void replyComment() {
        String url = "http://115.28.101.140/youxing/Home/Comment/setComment";
        RequestParams params = new RequestParams();
        params.put("User_ID", String.valueOf(sp.getInt(YXConstant.USER_ID, 0)));
        params.put("Token", sp.getString(YXConstant.USER_TOKEN, ""));
        params.put("Comment_Set_User_Id", String.valueOf(sp.getInt(YXConstant.USER_ID, 0)));
        params.put("Comment_Get_User_Id", mDatas.get(position).mCommnetId);
        params.put("Comment_Content", strComment);
        if (!shopBean.getCommentNum().equals("")) {
            params.put("Comment_Id", shopBean.getCommentNum());
        } else {
            params.put("Comment_Id", "0");
        }
        params.put("Reply_Id", "1");
        params.put("Recomment_Id", shopBean.getRecommentId());
        RequestUtils.ClientPost(url, params, new NetCallBack() {
            @Override
            public void onMySuccess(byte[] response) {
                CommentBean bean = new CommentBean();
                try {
                    JSONObject jsonObject = new JSONObject(new String(response));
                    if (jsonObject.getString("result").equals("success")) {
                        bean.commentNickname = (mDatas.get(position).mNickName);
                        bean.replyNickname = sp.getString(YXConstant.USER_NICKNAME, "");
                        bean.mComment = strComment;
                        if (shopBean.getCommentNum().equals("")) {
                            mTvCommentNum.setText("1");
                        } else {
                            mTvCommentNum.setText(String.valueOf(Integer.valueOf(mTvCommentNum.getText().toString()) + 1));
                        }
                        mAdapter.getReplyComment(bean, position);
                        mAdapter.notifyDataSetChanged();
                    } else {
                        showToast("评论失败，请稍后再试" + jsonObject.getJSONObject("response")
                                .getString("message"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onMyFailure(byte[] response, Throwable throwable) {
                Log.d(TAG, getString(R.string.fail_to_link_server) + new String() + "\n" + throwable.toString());
                showToast(getString(R.string.fail_to_link_server) + new String() + "\n" + throwable.toString());
            }
        });
    }

    /**
     * 判断对话框中是否输入内容
     */
    private boolean isEditEmply() {
        strComment = mEtComment.getText().toString().trim();
        if (strComment.equals("")) {
            Toast.makeText(getApplicationContext(), "评论不能为空", Toast.LENGTH_SHORT).show();
            return false;
        }
        mEtComment.setText("");
        return true;
    }


    @Override
    public void onClick(View v) {
        strComment = mEtComment.getText().toString();
        switch (v.getId()) {
            case R.id.ltb_rv_left:
                finish();
                break;
            case R.id.ltb_btn_right:
                if (sp.getString(YXConstant.USER_TOKEN, "false").equals("false")) {   //未登陆
                    startActivity(new Intent(this, LoginActivity.class).putExtra("isLoging", "jack"));
                    return;
                }
                showToast("分享成功");
                break;
            case R.id.aa_btn_send:
                if (sp.getString(YXConstant.USER_TOKEN, "false").equals("false")) {   //未登陆
                    startActivity(new Intent(this, LoginActivity.class).putExtra("isLoging", "jack"));
                    return;
                }
                if (strComment.equals("")) {
                    showToast("请输入内容");
                    return;
                }
                hideKeyboard();
                if (isEditEmply()) {        //判断用户是否输入内容
                    if (isReply) {
                        replyComment();
                    } else {
                        publishComment();
                    }
                    mLvBottom.setVisibility(View.GONE);
                    emojPager.setVisibility(View.GONE);
                    mBtnEmotion.setBackgroundResource(R.mipmap.ac_btn_express_normal);
                    showToast(strComment);
                }
                break;
            case R.id.aa_rv_want_go:
                if (sp.getString(YXConstant.USER_TOKEN, "false").equals("false")) {   //未登陆
                    startActivity(new Intent(this, LoginActivity.class).putExtra("isLoging", "jack"));
                    return;
                }
                addWantGoList();
                break;
            case R.id.aa_rv_been_gone:
                if (sp.getString(YXConstant.USER_TOKEN, "false").equals("false")) {   //未登陆
                    startActivity(new Intent(this, LoginActivity.class).putExtra("isLoging", "jack"));
                    return;
                }
                addBeenGoneList();
                break;
            case R.id.aa_rv_praise:
                if (sp.getString(YXConstant.USER_TOKEN, "false").equals("false")) {   //未登陆
                    startActivity(new Intent(this, LoginActivity.class).putExtra("isLoging", "jack"));
                    return;
                }
                if (isZan) {         //已经点赞了，再点一次就取消点赞
                    deleteZan();
                } else {          //还未点赞，再点一次记录点赞
                    addZan();
                }
                break;
            case R.id.aa_rv_comment:
                if (sp.getString(YXConstant.USER_TOKEN, "false").equals("false")) {   //未登陆
                    startActivity(new Intent(this, LoginActivity.class).putExtra("isLoging", "jack"));
                    return;
                }
                isReply = false;
                showMyEdit();
                break;
            case R.id.aa_btn_emotion:
                mBtnEmotion.setBackgroundResource(R.mipmap.ac_btn_express_pressed);
                Log.e(TAG, "=============>emotion");
                if (isOpen) {
                    isOpen = false;
                } else {
                    isOpen = true;
                }
                showEmotion(isOpen);
                break;

        }
    }

    private void deleteZan() {
        String url = "http://115.28.101.140/youxing/Home/Recomment/deleteZan";
        RequestParams params = new RequestParams();
        params.put("User_ID", String.valueOf(sp.getInt(YXConstant.USER_ID, 0)));
        params.put("Token", sp.getString(YXConstant.USER_TOKEN, ""));
        params.put("Recomment_Id", shopBean.getRecommentId());
        RequestUtils.ClientPost(url, params, new NetCallBack() {
            @Override
            public void onMySuccess(byte[] response) {
                try {
                    JSONObject jsonObject = new JSONObject(new String(response));
                    if (jsonObject.getString("result").equals("success")) {
                        showToast("取消点赞成功");
                        mRvPraise.setBackgroundResource(R.drawable.al_btn_bg);
                        if (mTvPraiseNum.getText().toString().equals("1")) {
                            mTvPraiseNum.setVisibility(View.GONE);
                        } else {
                            mTvPraiseNum.setText(String.valueOf(Integer.valueOf(mTvPraiseNum.getText().toString()) - 1));
                        }
                        isZan = false;
                    } else {
                        showToast("取消点赞失败" + jsonObject.getJSONObject("response").getString("message"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onMyFailure(byte[] response, Throwable throwable) {
                Log.d(TAG, getString(R.string.fail_to_link_server) + new String() + "\n" + throwable.toString());
                showToast(getString(R.string.fail_to_link_server) + new String() + "\n" + throwable.toString());
            }
        });
    }

    private void addZan() {
        String url = "http://115.28.101.140/youxing/Home/Recomment/addZan";
        RequestParams params = new RequestParams();
        params.put("User_ID", String.valueOf(sp.getInt(YXConstant.USER_ID, 0)));
        params.put("Token", sp.getString(YXConstant.USER_TOKEN, ""));
        params.put("Recomment_Id", shopBean.getRecommentId());
        RequestUtils.ClientPost(url, params, new NetCallBack() {
            @Override
            public void onMySuccess(byte[] response) {
                try {
                    JSONObject jsonObject = new JSONObject(new String(response));
                    if (jsonObject.getString("result").equals("success")) {
                        showToast("点赞成功");
                        mRvPraise.setBackgroundColor(ContextCompat.getColor(ArticleActivity.this
                                , R.color.btnLoginGreenDisable));
                        if (mTvPraiseNum.getVisibility() == View.GONE) {
                            mTvPraiseNum.setVisibility(View.VISIBLE);
                            mTvPraiseNum.setText("1");
                        } else {
                            mTvPraiseNum.setText(String.valueOf(Integer.valueOf(mTvPraiseNum.getText().toString()) + 1));
                        }
                        isZan = true;
                    } else {
                        showToast("点赞失败" + jsonObject.getJSONObject("response").getString("message"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onMyFailure(byte[] response, Throwable throwable) {
                Log.d(TAG, getString(R.string.fail_to_link_server) + new String() + "\n" + throwable.toString());
                showToast(getString(R.string.fail_to_link_server) + new String() + "\n" + throwable.toString());
            }
        });
    }

    private void addWantGoList() {
        String url = "http://115.28.101.140/youxing/Home/Recomment/addWantGo";
        RequestParams params = new RequestParams();
        params.put("User_ID", String.valueOf(sp.getInt(YXConstant.USER_ID, 0)));
        Log.d(TAG, "user_id=" + sp.getInt(YXConstant.USER_ID, 0));
        params.put("Token", sp.getString(YXConstant.USER_TOKEN, ""));
        Log.d(TAG, "token=" + sp.getString(YXConstant.USER_TOKEN, ""));
        params.put("Recomment_Id", shopBean.getRecommentId());
        Log.d(TAG, "recommentId=" + shopBean.getRecommentId());
        RequestUtils.ClientPost(url, params, new NetCallBack() {
            @Override
            public void onMySuccess(byte[] response) {
                try {
                    JSONObject jsonObject = new JSONObject(new String(response));
                    if (jsonObject.getString("result").equals("success")) {
                        showToast("加入想去成功");
                        mRvWantGo.setEnabled(false);
                    } else {
                        showToast("加入想去失败" + jsonObject.getJSONObject("response").getString("message"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onMyFailure(byte[] response, Throwable throwable) {
                Log.d(TAG, getString(R.string.fail_to_link_server) + new String() + "\n" + throwable.toString());
                showToast(getString(R.string.fail_to_link_server) + new String() + "\n" + throwable.toString());
            }
        });
    }

    private void addBeenGoneList() {
        String url = "http://115.28.101.140/youxing/Home/Recomment/addBeen";
        RequestParams params = new RequestParams();
        params.put("User_ID", String.valueOf(sp.getInt(YXConstant.USER_ID, 0)));
        Log.d(TAG, "user_id=" + sp.getInt(YXConstant.USER_ID, 0));
        params.put("Token", sp.getString(YXConstant.USER_TOKEN, ""));
        Log.d(TAG, "token=" + sp.getString(YXConstant.USER_TOKEN, ""));
        params.put("Recomment_Id", shopBean.getRecommentId());
        Log.d(TAG, "recommentId=" + shopBean.getRecommentId());
        params.put("Been_Content", shopBean.getContent());
        RequestUtils.ClientPost(url, params, new NetCallBack() {
            @Override
            public void onMySuccess(byte[] response) {
                try {
                    JSONObject jsonObject = new JSONObject(new String(response));
                    if (jsonObject.getString("result").equals("success")) {
                        showToast("加入去过成功");
                        mRvBeenGone.setEnabled(false);
                    } else {
                        showToast("加入去过失败" + jsonObject.getJSONObject("response").getString("message"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onMyFailure(byte[] response, Throwable throwable) {
                Log.d(TAG, getString(R.string.fail_to_link_server) + new String() + "\n" + throwable.toString());
                showToast(getString(R.string.fail_to_link_server) + new String() + "\n" + throwable.toString());
            }
        });

    }

    //触摸屏事件
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (ArticleActivity.this.getCurrentFocus() != null && ArticleActivity.this.getCurrentFocus()
                        .getWindowToken() != null) {
                    mLvBottom.setVisibility(View.GONE);
                    inputMethodManager.hideSoftInputFromWindow(ArticleActivity.this.getCurrentFocus()
                            .getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    /**
     * 覆盖手机返回键
     */
    @Override
    public void onBackPressed() {
        if (mRvCommentFrame.getVisibility() == View.VISIBLE || emojPager.getVisibility() == View.VISIBLE) {
            mRvCommentFrame.setVisibility(View.GONE);
            emojPager.setVisibility(View.GONE);
            mBtnEmotion.setBackgroundResource(R.mipmap.ac_btn_express_normal);
        } else {
            super.onBackPressed();
        }
        cancelToast();
    }

    @Override
    public void myScrollview(int scroll) {
        hideKeyboard();
        switch (scroll) {
            case MyScrollView.SCROLL_UP:
                flagUpOrDown = 0;
                if (isFirst) {
                    isFirst = false;
                    mLvBottom.setVisibility(View.GONE);
                    mRvCommentFrame.setVisibility(View.GONE);
                    emojPager.setVisibility(View.GONE);
                    mBtnEmotion.setBackgroundResource(R.mipmap.ac_btn_express_normal);
                    mLvComment.setVisibility(View.GONE);
                    return;
                }
                mLvBottom.setVisibility(View.GONE);
                mRvCommentFrame.setVisibility(View.GONE);
                emojPager.setVisibility(View.GONE);
                mBtnEmotion.setBackgroundResource(R.mipmap.ac_btn_express_normal);
                mLvComment.setVisibility(View.GONE);
                Log.d(TAG, "上滑");
                break;
            case MyScrollView.SCROLL_DOWN:
                flagUpOrDown = 1;
                mLvBottom.setVisibility(View.VISIBLE);
                mRvCommentFrame.setVisibility(View.GONE);
                mLvComment.setVisibility(View.VISIBLE);
                mBtnEmotion.setBackgroundResource(R.mipmap.ac_btn_express_normal);
                emojPager.setVisibility(View.GONE);
                Log.d(TAG, "下滑");
                break;
            case MyScrollView.SCROLL_STOP:
                if (flagUpOrDown == 0) {
                    mLvBottom.setVisibility(View.GONE);
                    mRvCommentFrame.setVisibility(View.GONE);
                    emojPager.setVisibility(View.GONE);
                    mBtnEmotion.setBackgroundResource(R.mipmap.ac_btn_express_normal);
                    mLvComment.setVisibility(View.GONE);
                    Log.d(TAG, "上滑停止");
                } else if (flagUpOrDown == 1) {
                    mLvBottom.setVisibility(View.VISIBLE);
                    mRvCommentFrame.setVisibility(View.GONE);
                    mBtnEmotion.setBackgroundResource(R.mipmap.ac_btn_express_normal);
                    emojPager.setVisibility(View.GONE);
                    mLvComment.setVisibility(View.VISIBLE);
                    Log.d(TAG, "下滑停止");
                }
                break;
        }
    }
}

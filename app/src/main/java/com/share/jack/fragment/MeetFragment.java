package com.share.jack.fragment;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.Image;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.RequestParams;
import com.share.jack.adapter.DialogMeetAdapter;
import com.share.jack.bean.DialogMeetBean;
import com.share.jack.bean.FoodOrRestBean;
import com.share.jack.bean.LocationBean;
import com.share.jack.bean.MeetBean;
import com.share.jack.http.NetCallBack;
import com.share.jack.http.RequestUtils;
import com.share.jack.swingtravel.LoginActivity;
import com.share.jack.swingtravel.MySelfAndOtherActivity;
import com.share.jack.swingtravel.R;
import com.share.jack.utils.DisplayUtil;
import com.share.jack.utils.GlideCircleTransform;
import com.share.jack.utils.YXApplication;
import com.share.jack.utils.YXConstant;
import com.share.jack.widget.MyCustomPopAndListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Author:Created by JackCheng
 * Email:17764576259@163.com
 * Time:2016/1/23 12:23
 * Copyright:1.0
 */
public class MeetFragment extends Fragment implements LocationSource, AMapLocationListener
        , AMap.OnMarkerClickListener, View.OnClickListener {

    public static final String TAG = "MeetFragment";

    private View mView;
    private MapView mMapView;
    private AMap mAMap;
    private UiSettings mSetting;    //对地图UI的设置
    private RelativeLayout mRvGo;
    private Button mBtnMore;
    private Button mBtnGo;
    private TextView mTvAll;
    private TextView mTvFood;
    private TextView mTvRest;
    private TextView mTvFootPrint;
    private RelativeLayout mRvAll;
    private RelativeLayout mRvFood;
    private RelativeLayout mRvRest;
    private RelativeLayout mRvFootPrint;
    private OnLocationChangedListener mListener;
    private AMapLocationClient mlocationClient;
    private AMapLocationClientOption mLocationOption;
    private SharedPreferences sp = null;

    private static final float POP_WIDTH = 180.0f;
    private static final float POP_HEIGHT = 220.0f;
    private MyCustomPopAndListView myCustomPopAndListView = null;

    private List<FoodOrRestBean> mFoodList = new ArrayList<>();
    private List<FoodOrRestBean> mRestList = new ArrayList<>();
    private List<MeetBean> mMeetList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_meet, container, false);
        sp = getActivity().getSharedPreferences(YXConstant.USER, getActivity().MODE_PRIVATE);
        initeView();
        mMapView.onCreate(savedInstanceState);// 必须要写，在onCreat方法中给aMap对象赋值
        if (mAMap == null) {
            mAMap = mMapView.getMap();
            mSetting = mAMap.getUiSettings();      //获取UI的设置
            setMap();   //设置地图
            addFoodMarks();
            addRestMarks();
            if (!sp.getString(YXConstant.USER_TOKEN, "false").equals("false")) {
                addMeetMarks();
            }
        }
        return mView;
    }

    private void setMap() {
        mSetting.setZoomControlsEnabled(false);       //设置地图默认的缩放按钮是否显示，在这里设置不显示
        // 设置定位监听
        mAMap.setLocationSource(this);
        // 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
        mAMap.setMyLocationEnabled(true);
        mAMap.setOnMarkerClickListener(this);// 设置点击marker事件监听器
    }

    private void initeView() {
        mMapView = (MapView) mView.findViewById(R.id.fm_mv_map);
        mRvGo = (RelativeLayout) mView.findViewById(R.id.fm_rv_go);
        mBtnMore = (Button) mView.findViewById(R.id.fm_btn_more);
        mBtnGo = (Button) mView.findViewById(R.id.fm_btn_go);
        mTvAll = (TextView) mView.findViewById(R.id.imtb_tv_all);
        mTvFood = (TextView) mView.findViewById(R.id.imtb_tv_food);
        mTvRest = (TextView) mView.findViewById(R.id.imtb_tv_rest);
        mTvFootPrint = (TextView) mView.findViewById(R.id.imtb_tv_footprint);
        mRvAll = (RelativeLayout) mView.findViewById(R.id.imtb_rv_all);
        mRvFood = (RelativeLayout) mView.findViewById(R.id.imtb_rv_food);
        mRvRest = (RelativeLayout) mView.findViewById(R.id.imtb_rv_rest);
        mRvFootPrint = (RelativeLayout) mView.findViewById(R.id.imtb_rv_footprint);

        mBtnMore.setOnClickListener(this);
        mBtnGo.setOnClickListener(this);
        mRvAll.setOnClickListener(this);
        mRvFood.setOnClickListener(this);
        mRvRest.setOnClickListener(this);
        mRvFootPrint.setOnClickListener(this);

        asyncHttpPost();
    }

    private void asyncHttpPost() {
        String url = "http://115.28.101.140/youxing/Home/Meet/getMapList";
        RequestParams params = new RequestParams();
        params.put("User_ID", String.valueOf(sp.getInt(YXConstant.USER_ID, 0)));
        Log.d(TAG, "userId=" + sp.getInt(YXConstant.USER_ID, 0));
        params.put("Token", sp.getString(YXConstant.USER_TOKEN, ""));
        Log.d(TAG, "token=" + sp.getString(YXConstant.USER_TOKEN, ""));
//        params.put("Coordinate", sp.getString(YXConstant.USER_CITY_ID, ""));
        params.put("Coordinate", "0571");   //测试用
        Log.d(TAG, "coordinate=" + sp.getString(YXConstant.USER_CITY_ID, ""));
        RequestUtils.ClientPost(url, params, new NetCallBack() {
            @Override
            public void onMySuccess(byte[] response) {
                try {
                    JSONObject jsonObject = new JSONObject(new String(response));
                    if (jsonObject.getString("result").equals("success")) {
                        Gson gson = new Gson();
                        Type listFoodType = new TypeToken<List<FoodOrRestBean>>() {
                        }.getType();
                        Type listRestType = new TypeToken<List<FoodOrRestBean>>() {
                        }.getType();
                        Type listMeetType = new TypeToken<List<MeetBean>>() {
                        }.getType();
                        JSONArray jsonFoodArray = jsonObject.getJSONObject("response")
                                .getJSONObject("data").getJSONArray("food");
                        Log.d(TAG, "foodJsom=" + jsonFoodArray.toString());
                        JSONArray jsonRestArray = jsonObject.getJSONObject("response")
                                .getJSONObject("data").getJSONArray("rest");
                        Log.d(TAG, "foodJsom=" + jsonRestArray.toString());
                        JSONArray jsonMeetArray = jsonObject.getJSONObject("response")
                                .getJSONObject("data").getJSONArray("meet");
                        Log.d(TAG, "foodJsom=" + jsonMeetArray.toString());
                        mFoodList = getListFromJson(jsonFoodArray, listFoodType);
                        mRestList = getListFromJson(jsonRestArray, listRestType);
                        mMeetList = gson.fromJson(jsonMeetArray.toString(), listMeetType);
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
                YXConstant.showToast(getActivity(), getString(R.string.fail_to_link_server) + new String() + "\n" + throwable.toString());
            }
        });
    }

    private List<FoodOrRestBean> getListFromJson(JSONArray jsonArray, Type listType) {
        List<FoodOrRestBean> list = new ArrayList<>();
        Gson gson = new Gson();
        list = gson.fromJson(jsonArray.toString(), listType);
        return list;
    }

    //在地图上添加标记
    private void addFoodMarks() {
        Marker marker = mAMap.addMarker(new MarkerOptions()
                .position(YXConstant.BEIJING)
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.fm_iv_food_marker))
                .draggable(true));
        marker.setObject("notMeet");
        marker.showInfoWindow();
    }

    private void addRestMarks() {
        Marker marker = mAMap.addMarker(new MarkerOptions()
                .position(YXConstant.XIAN)
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.fm_iv_rest_marker))
                .draggable(true));
        marker.setObject("notMeet");
        marker.showInfoWindow();
    }

    private void addMeetMarks() {
        Marker marker = mAMap.addMarker(new MarkerOptions()
                .position(YXConstant.HANGZHOU)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                .draggable(true));
        marker.setObject("meet");
        marker.showInfoWindow();
    }

    //必须要重写——帮助UI储存状态
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }

    //必须要重写——暂停
    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    //必须要重写——重新启动
    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    //必须要重写——销毁
    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    /**
     * 定位成功后回调函数
     *
     * @param aMapLocation
     */
    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (mListener != null && aMapLocation != null) {
            if (aMapLocation.getErrorCode() == 0) {
                Log.d("MeetFragment--->", "经度为=" + aMapLocation.getLongitude() + "\n"
                        + "纬度为" + aMapLocation.getLatitude());
                LocationBean bean = new LocationBean();
                bean.longitude = String.valueOf(aMapLocation.getLongitude());
                bean.latitude = String.valueOf(aMapLocation.getLatitude());
                YXApplication.getInstance().mList.add(bean);
                mlocationClient.stopLocation();
            } else {
                YXConstant.showToast(getActivity(), "定位失败");
                mlocationClient.stopLocation();
                Log.d("MeetFragment---->", "定位失败," + aMapLocation.getErrorCode() + ":" + aMapLocation.getErrorInfo());
            }
        }
    }

    //激活定位
    @Override
    public void activate(OnLocationChangedListener listener) {
        mListener = listener;
        if (mlocationClient == null) {
            mlocationClient = new AMapLocationClient(getActivity());
            mLocationOption = new AMapLocationClientOption();
            //设置定位监听
            mlocationClient.setLocationListener(this);
            //设置为高精度定位模式
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            //设置定位参数
            mlocationClient.setLocationOption(mLocationOption);
            // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
            // 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用stopLocation()方法来取消定位请求
            // 在定位结束后，在合适的生命周期调用onDestroy()方法
            // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
            mlocationClient.startLocation();
        }
    }

    //停止定位
    @Override
    public void deactivate() {
        mListener = null;
        if (mlocationClient != null) {
            mlocationClient.stopLocation();     //停止定位
            mlocationClient.onDestroy();     //销毁定位
        }
        mlocationClient = null;
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if (marker.getObject().equals("notMeet")) {
            mRvGo.setVisibility(View.VISIBLE);
        } else {
            MeetBean bean = new MeetBean();
            bean.userPhone = "18857119910";
            bean.userNick = "空灵11111111";
            bean.time = "2016.1.1";
            bean.userHead = "http://c.hiphotos.baidu.com/zhidao/wh%3D450%2C600/sign=d93bc9d38b1001e94e691c0b8d3e57da/279759ee3d6d55fbc80cf61c6b224f4a21a4ddc6.jpg";
            showMeetPop(bean);
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fm_btn_more:
                if (myCustomPopAndListView == null) {
                    //自定义的单击事件
                    MyCustomPopOnItemClickListener popOnItemClickListener = new MyCustomPopOnItemClickListener();
                    myCustomPopAndListView = new MyCustomPopAndListView(getActivity(), popOnItemClickListener
                            , DisplayUtil.dip2px(getActivity(), POP_WIDTH), DisplayUtil.dip2px(getActivity(), POP_HEIGHT));
                    //监听窗口的焦点事件，点击窗口外面则取消显示
                    myCustomPopAndListView.getContentView().setOnFocusChangeListener(new View.OnFocusChangeListener() {
                        @Override
                        public void onFocusChange(View v, boolean hasFocus) {
                            if (!hasFocus) {
                                myCustomPopAndListView.dismiss();
                            }
                        }
                    });
                }
                //设置默认获取焦点
                myCustomPopAndListView.setFocusable(true);
                //以某个控件的x和y的向左偏移量位置开始显示窗口
                myCustomPopAndListView.showAsDropDown(mBtnMore, 20, 0);
                //如果窗口存在，则更新
                myCustomPopAndListView.update();
                break;
            case R.id.fm_btn_go:
                mRvGo.setVisibility(View.GONE);
                YXConstant.showToast(getActivity(), "Go Successful");
                break;
            case R.id.imtb_rv_all:
                mRvGo.setVisibility(View.GONE);
                mRvAll.setBackgroundResource(R.drawable.fm_rv_all_bg_pressed);
                mTvAll.setTextColor(Color.WHITE);
                mTvFood.setTextColor(Color.BLACK);
                mTvRest.setTextColor(Color.BLACK);
                mTvFootPrint.setTextColor(Color.BLACK);
                if (mAMap != null) {
                    mAMap.clear();
                    addFoodMarks();
                    addRestMarks();
                    if (!sp.getString(YXConstant.USER_TOKEN, "false").equals("false")) {
                        addMeetMarks();
                    }
                }
                break;
            case R.id.imtb_rv_food:
                mRvGo.setVisibility(View.GONE);
                mRvAll.setBackgroundResource(R.drawable.fm_rv_all_bg_normal);
                mTvAll.setTextColor(Color.BLACK);
                mTvFood.setTextColor(getResources().getColor(R.color.btnLoginGreenNormal));
                mTvRest.setTextColor(Color.BLACK);
                mTvFootPrint.setTextColor(Color.BLACK);
                if (mAMap != null) {
                    mAMap.clear();
                    addFoodMarks();
                }
                break;
            case R.id.imtb_rv_rest:
                mRvGo.setVisibility(View.GONE);
                mRvAll.setBackgroundResource(R.drawable.fm_rv_all_bg_normal);
                mTvAll.setTextColor(Color.BLACK);
                mTvFood.setTextColor(Color.BLACK);
                mTvRest.setTextColor(getResources().getColor(R.color.btnLoginGreenNormal));
                mTvFootPrint.setTextColor(Color.BLACK);
                if (mAMap != null) {
                    mAMap.clear();
                    addRestMarks();
                }
                break;
            case R.id.imtb_rv_footprint:
                mRvGo.setVisibility(View.GONE);
                mRvAll.setBackgroundResource(R.drawable.fm_rv_all_bg_normal);
                mTvAll.setTextColor(Color.BLACK);
                mTvFood.setTextColor(Color.BLACK);
                mTvRest.setTextColor(Color.BLACK);
                mTvFootPrint.setTextColor(getResources().getColor(R.color.btnLoginGreenNormal));
                if (sp.getString(YXConstant.USER_TOKEN, "false").equals("false")) {   //没有登陆过
                    startActivity(new Intent(getActivity(), LoginActivity.class).putExtra("isLoging", "jack"));
                } else {
                    if (mAMap != null) {
                        mAMap.clear();
                        addMeetMarks();
                    }
                }
                break;

        }
    }

    private void showMeetPop(final MeetBean bean) {
        final AlertDialog dlg = new AlertDialog.Builder(getActivity()).create();
        dlg.show();
        WindowManager windowManager = getActivity().getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        WindowManager.LayoutParams lp = dlg.getWindow().getAttributes();
        lp.width = (int) (display.getWidth()); //设置宽度
        dlg.getWindow().setAttributes(lp);
        Window window = dlg.getWindow();
        window.setContentView(R.layout.dialog_meet);
        Button btnClose = (Button) window.findViewById(R.id.dm_btn_close);
        ListView listView = (ListView) window.findViewById(R.id.dm_listview);
        TextView tvData = (TextView) window.findViewById(R.id.dm_tv_data);
        TextView tvNickname = (TextView) window.findViewById(R.id.dm_tv_nickname);
        TextView tvTime = (TextView) window.findViewById(R.id.dm_tv_time);
        ImageView ivHead = (ImageView) window.findViewById(R.id.dm_riv_head);

        tvNickname.setText(bean.userNick);
        tvTime.setText(bean.time);
        Glide.with(this).load(bean.userHead).placeholder(R.mipmap.default_avatar)
                .error(R.mipmap.ic_launcher)
                .transform(new GlideCircleTransform(getActivity())).into(ivHead);

        List<DialogMeetBean> mDates = getDatas(bean.userNick, bean.time);
        DialogMeetAdapter adapter = new DialogMeetAdapter(getActivity(), mDates);
        listView.setAdapter(adapter);
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dlg.cancel();
            }
        });
        tvData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), MySelfAndOtherActivity.class).putExtra("username", bean.userPhone));
                dlg.cancel();
            }
        });
    }

    private List<DialogMeetBean> getDatas(String userName, String time) {
        List<DialogMeetBean> list = new ArrayList<DialogMeetBean>();
        DialogMeetBean bean = new DialogMeetBean();
        bean.mContent = time + "，在这个街头，你与" + userName + "擦肩而过，在这个地点你们相遇了";
        bean.mNum = "1";
        list.add(bean);
        return list;
    }

    class MyCustomPopOnItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            YXConstant.showToast(getActivity(), "您点击了第" + position + "个pop菜单");
            mRvGo.setVisibility(View.VISIBLE);
            myCustomPopAndListView.dismiss();
        }
    }
}

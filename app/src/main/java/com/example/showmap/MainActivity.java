package com.example.showmap;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.MapsInitializer;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.animation.Animation;
import com.amap.api.maps.model.animation.ScaleAnimation;
import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.AMapNaviView;
import com.amap.api.navi.model.NaviLatLng;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.ServiceSettings;
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.RideRouteResult;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.WalkPath;
import com.amap.api.services.route.WalkRouteResult;

import java.util.List;

public class MainActivity extends Activity implements LocationSource, AMapLocationListener,AMap.InfoWindowAdapter,AMap.OnMarkerClickListener,
        AMap.OnMapClickListener,RouteSearch.OnRouteSearchListener,NaviController.CallBack {
    MapView mMapView = null;
    AMap aMap = null;
    //定位相关参数
    LocationSource.OnLocationChangedListener mListener;
    AMapLocationClient mlocationClient;
    AMapLocationClientOption mLocationOption;
    boolean continuelocation = true;    //每次更新位置都会回到小原点，所有要在必要时关闭

    //Marker
    View infoWindow = null;
    TextView tx_pos;
    Button bt_route;
    Button bt_guide;
    Button bt_remove;
    Marker target = null;
    RouteSearch routeSearch = null;

    //导航
    NaviController naviController = null;
    AMapNaviView naviView = null;

    //初始化
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //权限
        RequestPermission();
        //创建地图
        mMapView = (MapView) findViewById(R.id.map);
        mMapView.onCreate(savedInstanceState);
        aMap = mMapView.getMap();
        //导航控件初始化
        naviView = findViewById(R.id.naviView);
        naviView.onCreate(savedInstanceState);
        AMapNavi mapNavi = null;
        try {
            mapNavi = AMapNavi.getInstance(getApplicationContext());
        } catch (com.amap.api.maps.AMapException e) {
            e.printStackTrace();
        }
        naviController = new NaviController(naviView, mapNavi, this);

        //初始化定位
        InitLocation();
        //初始化标志
        InitMarker();
    }

    public void RequestPermission(){
        Context context = getApplicationContext();
        //基础权限
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.CHANGE_WIFI_STATE,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE

        }, 0);
        //高德权限
        MapsInitializer.updatePrivacyShow(context, true, true);
        MapsInitializer.updatePrivacyAgree(context, true);

        ServiceSettings.updatePrivacyShow(context,true,true);
        ServiceSettings.updatePrivacyAgree(context,true);
    }

    public void InitLocation() {
        MyLocationStyle myLocationStyle = new MyLocationStyle();
        //设置连续定位模式下的定位间隔，只在连续定位模式下生效，单次定位模式下不会生效。单位为毫秒。
        myLocationStyle.interval(2000);
        //定位蓝点展现模式，默认是LOCATION_TYPE_LOCATION_ROTATE
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE);
        //设置是否显示定位小蓝点，用于满足只想使用定位，不想使用定位小蓝点的场景，设置false以后图面上不再有定位蓝点的概念，但是会持续回调位置信息。
        myLocationStyle.showMyLocation(true);
        // 设置定位监听
        aMap.setLocationSource(this);
        //设置定位蓝点的Style
        aMap.setMyLocationStyle(myLocationStyle);
        //设置为true表示启动显示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false。
        aMap.setMyLocationEnabled(true);
        // 设置地图模式，aMap是地图控制器对象。1.MAP_TYPE_NAVI:导航地图 2.MAP_TYPE_NIGHT:夜景地图 3.MAP_TYPE_NORMAL:白昼地图（即普通地图） 4.MAP_TYPE_SATELLITE:卫星图
        aMap.setMapType(AMap.MAP_TYPE_NORMAL);
        //设置UI
        UiSettings ui = aMap.getUiSettings();
        //定位按钮
        ui.setMyLocationButtonEnabled(true);
        //控制比例尺控件
        ui.setScaleControlsEnabled(true);
        //指南针
        ui.setCompassEnabled(true);
    }

    public void InitMarker() {
        //实现标志点击后的窗口
        Context context = getApplicationContext();
        aMap.setInfoWindowAdapter(this);
        //点击地图生成标志(Marker)并显示窗口
        aMap.addOnMapClickListener(this);
        //点击Marker生成窗口
        aMap.addOnMarkerClickListener(this);
    }


    //开始定位的回调
    @Override
    public void activate(LocationSource.OnLocationChangedListener listener) {
        mListener = listener;
        if (mlocationClient == null) {
            //初始化定位
            try {
                mlocationClient = new AMapLocationClient(this);
            } catch (Exception e) {
                e.printStackTrace();
            }
            //初始化定位参数
            mLocationOption = new AMapLocationClientOption();
            //设置为高精度定位模式
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            //mLocationOption.setInterval(2000);
            //设置定位回调监听
            mlocationClient.setLocationListener(this);
            //设置定位参数
            mlocationClient.setLocationOption(mLocationOption);
            // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
            // 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用stopLocation()方法来取消定位请求
            // 在定位结束后，在合适的生命周期调用onDestroy()方法
            // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
            mlocationClient.startLocation();//启动定位
        }
    }

    //停止定位的回调
    @Override
    public void deactivate() {
        mListener = null;
        if (mlocationClient != null) {
            mlocationClient.stopLocation();
            mlocationClient.onDestroy();
        }
        mlocationClient = null;
    }

    //位置变化的回调
    @Override
    public void onLocationChanged(AMapLocation amapLocation) {
        if (mListener != null && amapLocation != null) {
            if (amapLocation.getErrorCode() == 0 && continuelocation) {
                mListener.onLocationChanged(amapLocation);// 显示系统小蓝点
                continuelocation = false;
            } else {
                String errText = "定位失败," + amapLocation.getErrorCode() + ": " + amapLocation.getErrorInfo();
                Log.e("AmapErr", errText);
            }
        }
    }

    //销毁
    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，销毁地图
        mMapView.onDestroy();
        //销毁定位对象
        if (mlocationClient != null) {
            mlocationClient.onDestroy();
            mlocationClient = null;
        }
        //销毁导航对象
        if(naviController!=null){
            naviController.onDestroy();
            naviController = null;
        }
    }

    //重启
    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView.onResume ()，重新绘制加载地图
        mMapView.onResume();
        if(naviController!=null) { naviController.onResume(); }
    }

    //暂停
    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView.onPause ()，暂停地图的绘制
        mMapView.onPause();
        if(naviController!=null) { naviController.onPause(); }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，保存地图当前的状态
        mMapView.onSaveInstanceState(outState);
    }

    //判断是否是有效Marker
    boolean IsValidTarget(){
        return  target!=null && !target.isRemoved() && target.getPosition()!=null;
    }

    //自定义Marker窗口
    @Override   //info window 是所有marker共用的，包括 空marker 和 没有position的非空marker
    public View getInfoWindow(Marker marker) {
        if(infoWindow == null) {
            infoWindow = LayoutInflater.from(getApplicationContext()).inflate(
                    R.layout.marker_info_window, null);
            tx_pos = infoWindow.findViewById(R.id.tx_pos);
            bt_route = infoWindow.findViewById(R.id.bt_route);
            bt_guide = infoWindow.findViewById(R.id.bt_guide);
            bt_remove = infoWindow.findViewById(R.id.bt_remove);
            try {
                routeSearch = new RouteSearch(getApplicationContext());
            } catch (AMapException e) {
                e.printStackTrace();
            }
            routeSearch.setRouteSearchListener(this);

            bt_route.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(!IsValidTarget()) { return; }
                    LatLonPoint startLatLonPoint = new LatLonPoint(aMap.getMyLocation().getLatitude(), aMap.getMyLocation().getLongitude());
                    LatLonPoint endLatLonPoint = new LatLonPoint(target.getPosition().latitude, target.getPosition().longitude);
                    RouteSearch.FromAndTo fromAndTo = new RouteSearch.FromAndTo(startLatLonPoint, endLatLonPoint);
                    RouteSearch.WalkRouteQuery query = new RouteSearch.WalkRouteQuery(fromAndTo, RouteSearch.WALK_DEFAULT);
                    routeSearch.calculateWalkRouteAsyn(query);
                }
            });

            bt_guide.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(!IsValidTarget()) { return; }
                    LatLng targetpos = target.getPosition();
                    Location start = aMap.getMyLocation();
                    naviController.StartNavi(new NaviLatLng(start.getLatitude(),start.getLongitude()), new NaviLatLng(targetpos.latitude,targetpos.longitude));
                }
            });

            bt_remove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(target!=null && !target.isRemoved()){
                        target.remove();
                        target = null;
                    }
                }
            });
        }
        target = marker;
        render(marker);
        return infoWindow;
    }

    //渲染自定义窗口（主要更新位置等）
    public void render(Marker marker){
        if(!IsValidTarget()) { return; }
        //String.format 有问题，不能用！ 保留3位小数
        LatLng pos = marker.getPosition();
        float lat = (int)(pos.latitude*1000) / 1000f;
        float lgn = (int)(pos.longitude*1000) / 1000f;
        tx_pos.setText("lat/lgn: \n("+lat+","+lgn+")");
    }

    //用了InfoWindow，而InfoContens用不上了
    @Override
    public View getInfoContents(Marker marker) { return null; }

    //地图点击
    @Override
    public void onMapClick(LatLng latLng) {
        Marker marker = aMap.addMarker(new MarkerOptions().position(latLng));
//                MarkerOptions options = new MarkerOptions();
//                options.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
//                        .decodeResource(getResources(),R.drawable.xxxx)));
//                // 将Marker设置为贴地显示，可以双指下拉地图查看效果
//                options.setFlat(true);//设置marker平贴地图效果
//                marker.setMarkerOptions(options);
        long time = 50L;
        float fat_w = 2f;   // h = 1f/w;
        float skin_w = 0.7f;
        Animation scale1 = new ScaleAnimation(fat_w,skin_w,1f/fat_w, 1/skin_w);
        scale1.setDuration(time);
        scale1.setInterpolator(new DecelerateInterpolator());
        scale1.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart() { }
            @Override
            public void onAnimationEnd() {
                Animation scale2 = new ScaleAnimation(skin_w,1,1f/skin_w,1);
                scale2.setDuration(time);
                scale2.setInterpolator(new DecelerateInterpolator());
                scale2.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart() {}
                    @Override
                    public void onAnimationEnd() {
                        Animation scale3 = new ScaleAnimation(1,1,1,1);
                        scale3.setDuration(1L);
                        marker.setAnimation(scale3);
                        marker.startAnimation();
                        marker.showInfoWindow();
                    }
                });
                marker.setAnimation(scale2);
                marker.startAnimation();
            }
        });
        marker.setAnimation(scale1);
        marker.startAnimation();
    }

    //Marker点击显示自定义窗口
    @Override
    public boolean onMarkerClick(Marker marker) {
        marker.showInfoWindow();
        return true;
    }

    //路线搜索回调
    @Override
    public void onBusRouteSearched(BusRouteResult busRouteResult, int i) {

    }

    @Override
    public void onDriveRouteSearched(DriveRouteResult driveRouteResult, int i) {

    }

    @Override
    public void onWalkRouteSearched(WalkRouteResult walkRouteResult, int i) {
        aMap.clear();
        if(i != AMapException.CODE_AMAP_SUCCESS || walkRouteResult==null) { return; }
        List<WalkPath> paths = walkRouteResult.getPaths();
        if(paths==null || paths.isEmpty()) { return; }

        final WalkPath walkPath = paths.get(0);
        if (walkPath == null) {
            return;
        }
        WalkRouteOverlay overlay = new WalkRouteOverlay(
                getApplicationContext(), aMap, walkPath,
                walkRouteResult.getStartPos(),
                walkRouteResult.getTargetPos());
        overlay.removeFromMap();
        overlay.addToMap();
        overlay.zoomToSpan();
    }

    @Override
    public void onRideRouteSearched(RideRouteResult rideRouteResult, int i) {

    }


    //导航回调
    @Override
    public void onNaviStart() {
        mMapView.setVisibility(View.INVISIBLE);
        naviView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onNaviCancel() {
        mMapView.setVisibility(View.VISIBLE);
        naviView.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onNaviBackClick() {
        mMapView.setVisibility(View.VISIBLE);
        naviView.setVisibility(View.INVISIBLE);
    }
}
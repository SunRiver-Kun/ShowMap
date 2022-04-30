package com.example.showmap;

import android.util.Log;

import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.AMapNaviListener;
import com.amap.api.navi.AMapNaviView;
import com.amap.api.navi.AMapNaviViewListener;
import com.amap.api.navi.enums.NaviType;
import com.amap.api.navi.model.AMapCalcRouteResult;
import com.amap.api.navi.model.AMapLaneInfo;
import com.amap.api.navi.model.AMapModelCross;
import com.amap.api.navi.model.AMapNaviCameraInfo;
import com.amap.api.navi.model.AMapNaviCross;
import com.amap.api.navi.model.AMapNaviLocation;
import com.amap.api.navi.model.AMapNaviRouteNotifyData;
import com.amap.api.navi.model.AMapNaviTrafficFacilityInfo;
import com.amap.api.navi.model.AMapServiceAreaInfo;
import com.amap.api.navi.model.AimLessModeCongestionInfo;
import com.amap.api.navi.model.AimLessModeStat;
import com.amap.api.navi.model.NaviInfo;
import com.amap.api.navi.model.NaviLatLng;

import java.util.ArrayList;
import java.util.List;

public class NaviController implements AMapNaviViewListener, AMapNaviListener {
    public interface CallBack{
        void onNaviStart();
        void onNaviCancel();
        void onNaviBackClick();
    }

    public AMapNaviView aMapNaviView;
    AMapNavi aMapNavi;

    boolean isinitsuccess = false;
    boolean isnaving = false;

    CallBack cb;

    NaviController(AMapNaviView naviView, AMapNavi mapNavi ,CallBack cb){
        this.aMapNaviView = naviView;
        this.aMapNavi = mapNavi;
        this.cb = cb;
        aMapNaviView.setAMapNaviViewListener(this);
        //添加监听回调，用于处理算路成功
        aMapNavi.addAMapNaviListener(this);
        //加入导航语音
        aMapNavi.setUseInnerVoice(true);
    }


    //==========================对外接口===========================

    public void StartNavi(NaviLatLng start, NaviLatLng end){
        if(!isinitsuccess) { Log.e("Navi","初始化失败"); return; }
        if (isnaving){
            CancelNavi();
        }
        isnaving = true;
        //aMapNavi.calculateWalkRoute(start, end);
        List<NaviLatLng> sList = new ArrayList<NaviLatLng>();
        List<NaviLatLng> eList = new ArrayList<NaviLatLng>();
        sList.add(start);
        eList.add(end);
        aMapNavi.calculateDriveRoute(sList, eList, 0);
    }

    public void CancelNavi(){
        aMapNavi.stopNavi();
        isnaving = false;
    }

    public void onResume() {
        aMapNaviView.onResume();
    }

    public void onPause() {
        aMapNaviView.onPause();
    }

    public void onDestroy() {
        aMapNavi.stopNavi();
        aMapNavi.destroy();
        aMapNaviView.onDestroy();
    }



    //=======================下面的不要直接调用=================================
    //初始化成功
    @Override
    public void onInitNaviSuccess() {
        isinitsuccess = true;
    }

    //导航被取消
    @Override
    public void onNaviCancel() {
        isnaving = false;
        aMapNavi.stopNavi();
        cb.onNaviCancel();
    }

    //导航回退按钮
    @Override
    public boolean onNaviBackClick() {
        isnaving = false;
        aMapNavi.stopNavi();
        cb.onNaviBackClick();
        return true;
    }

    //成功计算路线，开始导航
    @Override
    public void onCalculateRouteSuccess(int[] ints) {

        cb.onNaviStart();
        aMapNavi.startNavi(NaviType.EMULATOR);
    }

    //下面的都是空函数接口
    @Override
    public void onNaviMapMode(int i) {

    }

    @Override
    public void onNaviTurnClick() {

    }

    @Override
    public void onNextRoadClick() {

    }

    @Override
    public void onScanViewButtonClick() {

    }

    @Override
    public void onLockMap(boolean b) {

    }

    @Override
    public void onNaviViewLoaded() {

    }

    @Override
    public void onMapTypeChanged(int i) {

    }

    @Override
    public void onNaviViewShowMode(int i) {

    }

    @Override
    public void onInitNaviFailure() {

    }

    @Override
    public void onStartNavi(int i) {

    }

    @Override
    public void onTrafficStatusUpdate() {

    }

    @Override
    public void onLocationChange(AMapNaviLocation aMapNaviLocation) {

    }

    @Override
    public void onGetNavigationText(int i, String s) {

    }

    @Override
    public void onGetNavigationText(String s) {

    }

    @Override
    public void onEndEmulatorNavi() {

    }

    @Override
    public void onArriveDestination() {

    }

    @Override
    public void onCalculateRouteFailure(int i) {

    }

    @Override
    public void onReCalculateRouteForYaw() {

    }

    @Override
    public void onReCalculateRouteForTrafficJam() {

    }

    @Override
    public void onArrivedWayPoint(int i) {

    }

    @Override
    public void onGpsOpenStatus(boolean b) {

    }

    @Override
    public void onNaviInfoUpdate(NaviInfo naviInfo) {

    }

    @Override
    public void updateCameraInfo(AMapNaviCameraInfo[] aMapNaviCameraInfos) {

    }

    @Override
    public void updateIntervalCameraInfo(AMapNaviCameraInfo aMapNaviCameraInfo, AMapNaviCameraInfo aMapNaviCameraInfo1, int i) {

    }

    @Override
    public void onServiceAreaUpdate(AMapServiceAreaInfo[] aMapServiceAreaInfos) {

    }

    @Override
    public void showCross(AMapNaviCross aMapNaviCross) {

    }

    @Override
    public void hideCross() {

    }

    @Override
    public void showModeCross(AMapModelCross aMapModelCross) {

    }

    @Override
    public void hideModeCross() {

    }

    @Override
    public void showLaneInfo(AMapLaneInfo[] aMapLaneInfos, byte[] bytes, byte[] bytes1) {

    }

    @Override
    public void showLaneInfo(AMapLaneInfo aMapLaneInfo) {

    }

    @Override
    public void hideLaneInfo() {

    }

    @Override
    public void notifyParallelRoad(int i) {

    }

    @Override
    public void OnUpdateTrafficFacility(AMapNaviTrafficFacilityInfo[] aMapNaviTrafficFacilityInfos) {

    }

    @Override
    public void OnUpdateTrafficFacility(AMapNaviTrafficFacilityInfo aMapNaviTrafficFacilityInfo) {

    }


    @Override
    public void updateAimlessModeStatistics(AimLessModeStat aimLessModeStat) {

    }

    @Override
    public void updateAimlessModeCongestionInfo(AimLessModeCongestionInfo aimLessModeCongestionInfo) {

    }

    @Override
    public void onPlayRing(int i) {

    }

    @Override
    public void onCalculateRouteSuccess(AMapCalcRouteResult aMapCalcRouteResult) {

    }

    @Override
    public void onCalculateRouteFailure(AMapCalcRouteResult aMapCalcRouteResult) {

    }

    @Override
    public void onNaviRouteNotify(AMapNaviRouteNotifyData aMapNaviRouteNotifyData) {

    }

    @Override
    public void onGpsSignalWeak(boolean b) {

    }

    @Override
    public void onNaviSetting() {

    }
}

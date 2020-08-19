package com.maple.rtc.videocall1v1;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.maple.rtc.BMediaKit;
import com.maple.rtc.ConstantApp;
import com.maple.rtc.Constants;
import com.maple.rtc.IBMediaEventHandler;
import com.maple.rtc.R;
import com.maple.rtc.video.BMVideoCanvas;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class VideoChatViewActivity extends AppCompatActivity {
    private static final String TAG = "VideoChatViewActivity";

    private static final String LOG_TAG = VideoChatViewActivity.class.getSimpleName();

    private static final int PERMISSION_REQ_ID_RECORD_AUDIO = 22;
    private static final int PERMISSION_REQ_ID_CAMERA = PERMISSION_REQ_ID_RECORD_AUDIO + 1;

    private boolean isRemoteView = false;

    private String ownUserId = "";

    private String channelName;
    private Map<String, SurfaceView> surfaceViewMap;

    private BMediaKit mBMediaKit;// Tutorial Step 1
    private final IBMediaEventHandler mRtcEventHandler = new IBMediaEventHandler() { // Tutorial Step 1
        @Override
        public void onJoinChannelSuccess(final String channelId, final String uid){
            Log.d(TAG, "onJoinChannelSuccess" + channelId + uid);
            ownUserId = uid;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    joinChannelSuccess(uid);
                }
            });

        }

        @Override
        public void onJoinChannelFailed(int reason){
            Log.d(TAG, "onJoinChannelFailed, reason:" + reason);
        }

        @Override
        public void onConnectionLost(){
            Log.d(TAG, "onConnectionLost" );
        }

        @Override
        public void onLeaveChannel(int reason){

            finish();
            Log.d(TAG, "onLeaveChannel, reason:" + reason);
        }

        @Override
        public void onForceKickOutChannel(int reason){
            Log.e(TAG, "onForceKickOutChannel, reason:" + reason);
        }

        @Override
        public void onFirstRemoteVideoFrameSizeChanged(final String uid, int width, int height){

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    displayRemoteVideo(uid);
                }
            });

        }
        @Override
        public void onAudioVolumeIndication(String volumeInfo, int totalVolume){

        }

        @Override
        public void onUserJoinedNotice(final List<String> uids) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Iterator<String> uit = uids.iterator();
                    while (uit.hasNext()){
                        setupRemoteVideo(uit.next());
                    }
                }
            });
        }

        @Override
        public void onUserOfflineNotice(final List<String> uids) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Iterator<String> uit = uids.iterator();
                    while (uit.hasNext()){
                        removeRemoteVideo(uit.next());
                    }
                }
            });
        }
        @Override
        public void onWarning(int warn){
            Log.w(TAG, "onWarning " + warn);
        }

        @Override
        public void onError(int err) {
            super.onError(err);
            Log.e(TAG, "onError " + err);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_chat_view);
        Intent i = getIntent();
        channelName = i.getStringExtra(ConstantApp.ACTION_KEY_CHANNEL_NAME);

        surfaceViewMap = new HashMap<String, SurfaceView>();

        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO, PERMISSION_REQ_ID_RECORD_AUDIO) && checkSelfPermission(Manifest.permission.CAMERA, PERMISSION_REQ_ID_CAMERA)) {
            initMapleEngineAndJoinChannel();
        }
    }

    private void initMapleEngineAndJoinChannel() {
        initializeMapleEngine();     // Tutorial Step 1
        setupVideoProfile();         // Tutorial Step 2
        joinChannel();               // Tutorial Step 3
    }

    public boolean checkSelfPermission(String permission, int requestCode) {
        Log.i(LOG_TAG, "checkSelfPermission " + permission + " " + requestCode);
        if (ContextCompat.checkSelfPermission(this,
                permission)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{permission},
                    requestCode);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        Log.i(LOG_TAG, "onRequestPermissionsResult " + grantResults[0] + " " + requestCode);

        switch (requestCode) {
            case PERMISSION_REQ_ID_RECORD_AUDIO: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkSelfPermission(Manifest.permission.CAMERA, PERMISSION_REQ_ID_CAMERA);
                } else {
                    showLongToast("No permission for " + Manifest.permission.RECORD_AUDIO);
                    finish();
                }
                break;
            }
            case PERMISSION_REQ_ID_CAMERA: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initMapleEngineAndJoinChannel();
                } else {
                    showLongToast("No permission for " + Manifest.permission.CAMERA);
                    finish();
                }
                break;
            }
        }
    }

    public final void showLongToast(final String msg) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        surfaceViewMap.clear();
        leaveChannel();
        mBMediaKit = null;
    }

    // Tutorial Step 10
    public void onLocalVideoMuteClicked(View view) {
        ImageView iv = (ImageView) view;
        if (iv.isSelected()) {
            iv.setSelected(false);
            iv.clearColorFilter();
        } else {
            iv.setSelected(true);
            iv.setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.MULTIPLY);
        }
        //开关发送本地视频
        mBMediaKit.muteLocalVideoStream(iv.isSelected());

        FrameLayout container = (FrameLayout) findViewById(R.id.local_video_view_container);
        SurfaceView surfaceView = (SurfaceView) container.getChildAt(0);
        surfaceView.setZOrderMediaOverlay(!iv.isSelected());
        surfaceView.setVisibility(iv.isSelected() ? View.GONE : View.VISIBLE);
    }

    // Tutorial Step 9
    public void onLocalAudioMuteClicked(View view) {
        ImageView iv = (ImageView) view;
        if (iv.isSelected()) {
            iv.setSelected(false);
            iv.clearColorFilter();
        } else {
            iv.setSelected(true);
            iv.setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.MULTIPLY);
        }
        //开关发送本地音频
        mBMediaKit.muteLocalAudioStream(iv.isSelected());
    }

    // Tutorial Step 8
    public void onSwitchCameraClicked(View view) {
        //切换前后摄像头
        isRemoteView = !isRemoteView;
        mBMediaKit.switchCamera();
    }

    // Tutorial Step 6
    public void onEncCallClicked(View view) {
        leaveChannel();
    }

    // Tutorial Step 1
    private void initializeMapleEngine() {
        try {
            mBMediaKit = BMediaKit.create(getBaseContext(), getString(R.string.maple_app_id), mRtcEventHandler);
        } catch (Exception e) {
            Log.e(LOG_TAG, Log.getStackTraceString(e));

            throw new RuntimeException("NEED TO check rtc sdk init fatal error\n" + Log.getStackTraceString(e));
        }
    }

    // Tutorial Step 2
    private void setupVideoProfile() {
        //设置参数
        mBMediaKit.setChannelProfile(Constants.CHANNEL_PROFILE_VOICE_VIDEO);
        mBMediaKit.setMediaProfile(Constants.AUDIO_PROFILE_VOICE_STANDARD,Constants.VIDEO_PROFILE_480P);
        mBMediaKit.setVideoConferenceProfile(Constants.VIDEO_CONFERENCE_PROFILE_NORMAL);
    }

    private void setupLocalVideo(String uid) {
        FrameLayout container = (FrameLayout) findViewById(R.id.local_video_view_container);
        //创建本地视频
        SurfaceView surfaceView = mBMediaKit.createRenderView();
        if(surfaceView!=null) {
            surfaceView.setZOrderMediaOverlay(true);
            container.addView(surfaceView);
            //设置本地视频view
        	mBMediaKit.setupLocalVideo(new BMVideoCanvas(surfaceView, BMVideoCanvas.RENDER_MODE_HIDDEN, uid));
        }
    }

    // Tutorial Step 4
    private void joinChannelSuccess(String uid){
        setupLocalVideo(uid);
        //开始本地预览
        mBMediaKit.startPreview();
        //发送本地视频
        mBMediaKit.muteLocalVideoStream(false);
    }

    // Tutorial Step 3
    private void joinChannel() {
        //加入channel
        mBMediaKit.joinChannel(channelName); // if you do not specify the uid, we will generate the uid for you
    }

    // Tutorial Step 5
    private void setupRemoteVideo(String uid) {
        if(ownUserId.length() > 0 && ownUserId.equals(uid)){
            return;
        }
        //创建渲染view
        SurfaceView surfaceView = mBMediaKit.createRenderView();

        surfaceViewMap.put(uid,surfaceView);
        //设置远端渲染view
        mBMediaKit.setupRemoteVideo(new BMVideoCanvas(surfaceView, BMVideoCanvas.RENDER_MODE_HIDDEN, uid));
        //开始接受远端视频
        int ret =  mBMediaKit.muteRemoteVideoStream(false, uid);

    }

    private void removeRemoteVideo(String uid) {
        if(ownUserId.length() > 0  && ownUserId.equals(uid)){
            return;
        }
        //停止接受远端视频
        int ret =  mBMediaKit.muteRemoteVideoStream(true, uid);
        //解绑远端视频view
        mBMediaKit.setupRemoteVideo(new BMVideoCanvas(null, BMVideoCanvas.RENDER_MODE_HIDDEN, uid));

        FrameLayout container = (FrameLayout) findViewById(R.id.remote_video_view_container);

        SurfaceView mSurfaceView = surfaceViewMap.get(uid);
        //删除远端视频view
        if(container.findViewWithTag(uid) == mSurfaceView)
            container.removeView(mSurfaceView);

        View tipMsg = findViewById(R.id.quick_tips_when_use_maple_sdk); // optional UI
        tipMsg.setVisibility(View.VISIBLE);
        surfaceViewMap.remove(uid) ;
    }

    private void displayRemoteVideo(String uid) {

        FrameLayout container = (FrameLayout) findViewById(R.id.remote_video_view_container);
        SurfaceView mSurfaceView = surfaceViewMap.get(uid);
        container.addView(mSurfaceView);

        mSurfaceView.setTag(uid); // for mark purpose
        View tipMsg = findViewById(R.id.quick_tips_when_use_maple_sdk); // optional UI
        tipMsg.setVisibility(View.GONE);
    }

    // Tutorial Step 6
    private void leaveChannel() {
        mBMediaKit.leaveChannel();
    }

    @Override
    public void onBackPressed(){
        mBMediaKit.leaveChannel();
        super.onBackPressed();
    }


}

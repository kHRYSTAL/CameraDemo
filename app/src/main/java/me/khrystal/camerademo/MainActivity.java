package me.khrystal.camerademo;

import android.content.Intent;
import android.hardware.Camera;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, SurfaceHolder.Callback, Camera.ShutterCallback, Camera.PictureCallback {

    SurfaceView sfcPreview;
    Button btnWrite;
    Button btnShutter;
    Button btnFlash;

    /** 睡眠时间 */
    protected static final long SLEEP_TIME = 50;
    /** 初始化相机 */
    protected static final int MSG_ID_CAMERA_START = 0x01;
    /** 释放相机资源 */
    protected static final int MSG_ID_CAMERA_STOP = 0x2;

    boolean mIsActivityTeady = false;
    boolean mIsSurfaceTeady = false;
    SurfaceHolder mHolder;

    Camera.Parameters mParameters;
    Camera mCamera;
    /** 支持的闪光灯模式 */
    List<String> mSupportFlash;
    int mIndexFlash;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        uiInit();
    }

    private void uiInit() {
        btnWrite = (Button)findViewById(R.id.btn_write);
        btnFlash = (Button)findViewById(R.id.btn_flash);
        btnShutter = (Button)findViewById(R.id.btn_shutter);
        sfcPreview = (SurfaceView)findViewById(R.id.sfc_preview);
        btnWrite.setOnClickListener(this);
        btnShutter.setOnClickListener(this);
        btnFlash.setOnClickListener(this);
        sfcPreview.getHolder().addCallback(this);
        sfcPreview.setOnClickListener(this);
        mHolder = sfcPreview.getHolder();
    }

    /**
     * 相机为SurfaceView 要跟随生命周期
     */
    @Override
    protected void onStart() {
        super.onStart();
        mIsActivityTeady = true;
        mHandler.sendEmptyMessage(MSG_ID_CAMERA_START);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mIsActivityTeady = false;
        mHandler.sendEmptyMessage(MSG_ID_CAMERA_STOP);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_flash:
                int index = (mIndexFlash + 1) % mSupportFlash.size();
                String mode = mSupportFlash.get(index);
                // 将新的闪光灯模式设置到参数中
                mParameters.setFlashMode(mode);
                // 设置参数到照相机上
                mCamera.setParameters(mParameters);
                refreshFlashMode();
                break;
            case R.id.btn_shutter:
                /*
				 * shutter:快门回调，在按下快门的瞬间会调用此接口中的方法， 常用于播放自定义的快门
				 * raw：照片回调，将未经处理的最原始的照片数据信息传递给此接口
				 * postview：照片回调，将经过一定处理的照片数据信息传递给此接口 此处理一般是经过硬件处理，所以部分手机可能不支持。
				 * jpeq 照片回调 ：将经过较大压缩的照片数据传递给此接口。最常用的接口
				 */
                mCamera.takePicture(this, null, null, this);
                break;
            case R.id.btn_write:
                //TODO 跳转至其他页面
                Toast.makeText(this, "跳转至其他页面", Toast.LENGTH_SHORT).show();
                break;
            case R.id.sfc_preview:
                //调用autoFocus方法可以进行自动对焦
                //参数是个回调，用于确认对焦是否成功，如果无需知道对焦是否成功，设置为null
                mCamera.autoFocus(null);
                break;
            default:
                break;
        }

    }
    //  SurfaceView callback method start---->
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mIsSurfaceTeady = true;
        mHandler.sendEmptyMessage(MSG_ID_CAMERA_START);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mIsSurfaceTeady = false;
        mHandler.sendEmptyMessage(MSG_ID_CAMERA_STOP);
    }
//    SurfaceView callback method end --->
//    Camera implements method callback  start----->

    /**
     * @param data
     *            字节数组，包含照片的数据
     * @param camera
     *            刚进行拍照的相机
     * */
    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        FileOutputStream fos = null;
        // 用于创建临时文件
        try {
            File file = File.createTempFile("DCIM", ".tmp");
            fos = new FileOutputStream(file);
            fos.write(data);
//            TODO 页面跳转
            Intent intent = new Intent(this, BitmapTransformActivity.class);
            intent.putExtra("path", file.getAbsolutePath());
            startActivity(intent);
            finish();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fos != null)
                    fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 快门按下回调
     */
    @Override
    public void onShutter() {

    }
//    Camera implements method callback end------>

    private void startCamera() {
        // 如果相机在短时间内重复打开，先关闭一下就不会出错了；
        stopCamera();
        // 可以得到摄像头的个数；此方法的返回值-1是Open方法的最大值；
        // Camera.getNumberOfCameras();
        // 不带参数 打开默认摄像头；如果带int参数表示打开指定的摄像头
        // 一般0表示的后置摄像头，1表示前置后置摄像头
        mCamera = Camera.open();
        mCamera.setDisplayOrientation(90);
        mParameters = mCamera.getParameters();
        // getMaxZoom得到最大的比例
        mSupportFlash = mParameters.getSupportedFlashModes();
//adapteration
        List<Camera.Size> supportedPreviewSizes = mParameters.getSupportedPreviewSizes();
        Camera.Size previewSize = supportedPreviewSizes.get(getPictureSize(supportedPreviewSizes));
        mParameters.setPreviewSize(previewSize.width, previewSize.height);
        List<Camera.Size> supportedPictureSizes = mParameters.getSupportedPictureSizes();
        Camera.Size pictureSize = supportedPictureSizes.get(getPictureSize(supportedPictureSizes));
        mParameters.setPictureSize(pictureSize.width, pictureSize.height);
        mCamera.setParameters(mParameters);
//
        refreshFlashMode();
        try {
            mCamera.setPreviewDisplay(mHolder);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        // 开始捕获图像进行预览
        mCamera.startPreview();
    }

    /** 刷新闪光灯按钮上的图片状态 */
    private void refreshFlashMode() {
        // 得到当前的闪光灯状态
        String mode = mParameters.getFlashMode();
        // 得到当前闪光灯模式在列表中的下标；
        mIndexFlash = mSupportFlash.indexOf(mode);
        if ("off".equals(mode)) {
            Toast.makeText(this,"关闭闪关灯",Toast.LENGTH_SHORT).show();
        } else if ("on".equals(mode)) {
            Toast.makeText(this,"开启闪关灯",Toast.LENGTH_SHORT).show();

        } else if ("auto".equals(mode)) {
            Toast.makeText(this,"自动",Toast.LENGTH_SHORT).show();
        }
    }

    private void stopCamera() {
        if (mCamera != null) {
            // 停止预览
            mCamera.stopPreview();
            // 释放相机资源
            mCamera.release();
        }
        mCamera = null;
    }

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case MSG_ID_CAMERA_START: {
                    if (mIsActivityTeady && mIsSurfaceTeady) {
                        startCamera();
                    } else if (mIsActivityTeady || mIsSurfaceTeady) {
                        // 移除其他正在等待的消息，即同时只能有一个消息在等待；
                        removeMessages(MSG_ID_CAMERA_START);
                        sendEmptyMessageDelayed(MSG_ID_CAMERA_START, SLEEP_TIME);
                    }
                    break;
                }

                case MSG_ID_CAMERA_STOP: {
                    removeMessages(MSG_ID_CAMERA_START);
                    removeMessages(MSG_ID_CAMERA_STOP);
                    stopCamera();
                    break;
                }
                default:
                    break;
            }
        }

    };

    private int getPictureSize(List<Camera.Size> sizes) {
        WindowManager wm = this.getWindowManager();
        int screenWidth = wm.getDefaultDisplay().getWidth();
// 屏幕的宽度
        int index = -1;
        for (int i = 0; i < sizes.size(); i++) {
            if (Math.abs(screenWidth - sizes.get(i).width) == 0) {
                index = i;
                break;
            }
        }
// 当未找到与手机分辨率相等的数值,取列表中间的分辨率
        if (index == -1) {
            index = sizes.size() / 2;
        }
        return index;
    }

}

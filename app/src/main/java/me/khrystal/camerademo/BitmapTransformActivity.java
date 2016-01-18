package me.khrystal.camerademo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

/**
 * CameraActivity 获取图片后跳转至此Activity
 *
 * @FileName: me.shurufa.activities.BitmapTransformActivity.java
 * @author: kHRYSTAL
 * @email: 723526676@qq.com
 * @date: 2016-01-18 13:31
 */
public class BitmapTransformActivity extends AppCompatActivity {

    ImageView tramsformImg;

    protected String mPath;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tramsformImg = (ImageView)findViewById(R.id.img);
        mPath = getIntent().getStringExtra("path");
        Bitmap bmp = BitmapFactory.decodeFile(mPath);
        Matrix m = new Matrix();
        m.setRotate(90,(float) bmp.getWidth() / 2, (float) bmp.getHeight() / 2);
        bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), m, true);
        tramsformImg.setImageBitmap(bmp);
    }
}

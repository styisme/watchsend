package org.watchsend.uikit;

import org.watchsend.R;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.Window;
import android.widget.ImageView;
import android.widget.Toast;
import java.io.IOException;
import java.io.InputStream;
public class UikitImageViewerActivity extends Activity {
    public static final String EXTRA_IMAGE_NAME = "uikit_image_name";
    public static void show(Activity activity, String assetName) {
        android.content.Intent intent = new android.content.Intent(activity, UikitImageViewerActivity.class);
        intent.putExtra(EXTRA_IMAGE_NAME, assetName);
        activity.startActivity(intent);
    }
    private ImageView imageView;
    private Matrix matrix = new Matrix();
    private ScaleGestureDetector scaleDetector;
    private float lastX, lastY;
    
    @Override protected void attachBaseContext(Context newBase) { super.attachBaseContext(UikitUi.wrapScale(newBase)); }
    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(0xFF000000));
        
        imageView = new ImageView(this);
        imageView.setScaleType(ImageView.ScaleType.MATRIX);
        setContentView(imageView);
        org.watchsend.uikit.RtlSupport.apply(this);
        
        String assetName = getIntent().getStringExtra(EXTRA_IMAGE_NAME);
        try {
            Bitmap bmp;
            if (assetName != null && assetName.startsWith("/")) {
                // 传绝对路径时读本机文件（接收后直接在表上看图，不依赖第三方看图 App）
                bmp = BitmapFactory.decodeFile(assetName);
            } else {
                InputStream is = getAssets().open(assetName);
                bmp = BitmapFactory.decodeStream(is);
                is.close();
            }
            if (bmp != null) {
                imageView.setImageBitmap(bmp);
            } else {
                Toast.makeText(this, "Image load failed", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
        } catch (IOException e) {
            Toast.makeText(this, "File not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        scaleDetector = new ScaleGestureDetector(this, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                float scaleFactor = detector.getScaleFactor();
                matrix.postScale(scaleFactor, scaleFactor, detector.getFocusX(), detector.getFocusY());
                imageView.setImageMatrix(matrix);
                return true;
            }
        });
        
        imageView.post(new Runnable() {
            @Override
            public void run() {
                fitCenter();
            }
        });
    }
    
    private void fitCenter() {
        Drawable drawable = imageView.getDrawable();
        if (drawable == null) return;
        int dw = drawable.getIntrinsicWidth();
        int dh = drawable.getIntrinsicHeight();
        int vw = imageView.getWidth();
        int vh = imageView.getHeight();
        
        matrix.reset();
        float wRatio = (float) vw / dw;
        float hRatio = (float) vh / dh;
        float ratio = Math.min(wRatio, hRatio);
        matrix.postScale(ratio, ratio);
        
        float dx = (vw - dw * ratio) / 2f;
        float dy = (vh - dh * ratio) / 2f;
        matrix.postTranslate(dx, dy);
        imageView.setImageMatrix(matrix);
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleDetector.onTouchEvent(event);
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                lastX = event.getX();
                lastY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                if (event.getPointerCount() == 1) {
                    float dx = event.getX() - lastX;
                    float dy = event.getY() - lastY;
                    matrix.postTranslate(dx, dy);
                    imageView.setImageMatrix(matrix);
                    lastX = event.getX();
                    lastY = event.getY();
                }
                break;
        }
        return true;
    }
    
    @Override public boolean dispatchTouchEvent(android.view.MotionEvent ev) { if (UikitUi.handleSwipeBack(this, ev)) return true; return super.dispatchTouchEvent(ev); }

    @Override
    public void onBackPressed() {
        // 开启“软件内返回”时吞掉返回键，交给界面内横滑退出；关闭时交回系统
        if (org.watchsend.uikit.BackGate.handleBack(this)) return;
        super.onBackPressed();
    }
}

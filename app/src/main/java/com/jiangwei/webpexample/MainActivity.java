package com.jiangwei.webpexample;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.LayoutInflaterCompat;
import android.support.v4.view.LayoutInflaterFactory;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import me.everything.webp.WebPDecoder;

public class MainActivity extends AppCompatActivity {
    private ImageView iv2;
    private Button btn;

    private static final int[] android_attr = new int[] { android.R.attr.src };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            LayoutInflaterCompat.setFactory(LayoutInflater.from(this), new LayoutInflaterFactory() {
                @Override
                public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
                    AppCompatDelegate delegate = getDelegate();
                    View view = delegate.createView(parent, name, context, attrs);
                    if (view != null && view instanceof ImageView) {
                        ImageView iv = (ImageView) view;
                        TypedArray a = context.obtainStyledAttributes(attrs, android_attr);
                        // 取出src属性
                        int webpSourceResourceID = a.getResourceId(0, 0);
                        if (webpSourceResourceID == 0) {
                            return view;
                        }
                        // 获取webp的名字
                        TypedValue value = new TypedValue();
                        getResources().getValue(webpSourceResourceID, value, true);
                        // res/drawable/meizi.webp 取后13个
                        String resname = value.string.toString().substring(13, value.string.toString().length());
                        if (resname.endsWith(".webp")) {
                            final Bitmap webpBitmap = getBitmapFromResId(webpSourceResourceID);
                            if (webpBitmap != null) {
                                iv.setImageBitmap(webpBitmap);
                                Toast.makeText(MainActivity.this, "SDK:" + Build.VERSION.SDK_INT + "成功加载webp",
                                        Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            iv.setImageResource(webpSourceResourceID);
                            Toast.makeText(MainActivity.this, "SDK:" + Build.VERSION.SDK_INT + "成功加载普通图片",
                                    Toast.LENGTH_SHORT).show();
                        }
                        a.recycle();
                    }
                    return view;
                }
            });
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        iv2 = (ImageView) findViewById(R.id.iv2);
        btn = (Button) findViewById(R.id.btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    iv2.setImageBitmap(getBitmapFromResId(R.drawable.meizi));
                    Toast.makeText(MainActivity.this, "SDK:" + Build.VERSION.SDK_INT + "成功加载webp", Toast.LENGTH_SHORT)
                            .show();
                } else {
                    iv2.setImageResource(R.drawable.meizi);
                    Toast.makeText(MainActivity.this, "SDK:" + Build.VERSION.SDK_INT + "成功加载webp", Toast.LENGTH_SHORT)
                            .show();
                }
            }
        });
    }

    private Bitmap getBitmapFromResId(int webpSourceResourceID) {
        InputStream rawImageStream = getResources().openRawResource(webpSourceResourceID);
        byte[] data = streamToBytes(rawImageStream);
        return WebPDecoder.getInstance().decodeWebP(data);
    }

    private static byte[] streamToBytes(InputStream is) {
        ByteArrayOutputStream os = new ByteArrayOutputStream(1024);
        byte[] buffer = new byte[1024];
        int len;
        try {
            while ((len = is.read(buffer)) >= 0) {
                os.write(buffer, 0, len);
            }
        } catch (java.io.IOException e) {
        }
        return os.toByteArray();
    }
}

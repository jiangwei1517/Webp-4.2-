# Webp向4.2以下兼容

## 参考资料
* EverythingMe/webp-android   <https://github.com/EverythingMe/webp-android>
* WebP 探寻之路 <http://isux.tencent.com/introduction-of-webp.html>

### 优势
WebP 的优势体现在它具有更优的图像数据压缩算法，能带来更小的图片体积，而且拥有肉眼识别无差异的图像质量；同时具备了无损和有损的压缩模式、Alpha 透明以及动画的特性，在 JPEG 和 PNG 上的转化效果都非常优秀、稳定和统一。

科技博客 GigaOM 曾报道：YouTube 的视频略缩图采用 WebP 格式后，网页加载速度提升了 10%；谷歌的 Chrome 网上应用商店采用 WebP 格式图片后，每天可以节省几 TB 的带宽，页面平均加载时间大约减少 1/3；Google+ 移动应用采用 WebP 图片格式后，每天节省了 50TB 数据存储空间。

### 缺点：

* Android默认支持4.2系统以上的。
* 4.0-4.2之间不支持透明格式的图片
* 解码速度对比png要慢5倍

## 主要实现

通过ndk将webp-android这个项目编译成so文件，添加依赖，如果SDK版本小于17，调用WebPDecoder.getInstance().decodeWebP(data)；这个native方法。

在这里做了一个优化，对数据源的layout中的imageview进行截取，LayoutInflaterCompat.setFactory，将src属性获得到，直接赋值并返回view。不再利用webp-android这个项目中的方式读取src了。

传统的方式：

	<me.everything.webp.WebPImageView
	  android:layout_width="wrap_content"
	  android:layout_height="wrap_content"
	  webp:webp_src="@drawable/your_webp_image" />
	  
经过改造后的文件：

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
	                    System.out.println(name);
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

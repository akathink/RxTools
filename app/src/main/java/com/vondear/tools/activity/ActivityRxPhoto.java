package com.vondear.tools.activity;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.vondear.rxtools.RxBarUtils;
import com.vondear.rxtools.RxImageUtils;
import com.vondear.rxtools.RxPermissionsUtils;
import com.vondear.rxtools.RxPhotoUtils;
import com.vondear.rxtools.RxSPUtils;
import com.vondear.rxtools.activity.ActivityBase;
import com.vondear.rxtools.interfaces.onRequestListener;
import com.vondear.rxtools.view.dialog.RxDialog;
import com.vondear.rxtools.view.dialog.RxDialogSureCancle;
import com.vondear.tools.R;
import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.UCropActivity;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import jp.wasabeef.glide.transformations.CropCircleTransformation;

public class ActivityRxPhoto extends ActivityBase {

    @BindView(R.id.iv_avatar)
    ImageView ivAvatar;
    @BindView(R.id.iv_finish)
    ImageView ivFinish;
    @BindView(R.id.tv_title)
    TextView tvTitle;
    @BindView(R.id.ll_include_title)
    LinearLayout llIncludeTitle;
    @BindView(R.id.btn_exit)
    Button mBtnExit;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RxBarUtils.noTitle(this);
        setContentView(R.layout.activity_von_photo);
        ButterKnife.bind(this);
        initView();
    }

    protected void initView() {
        Resources r = mContext.getResources();
        resultUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://"
                + r.getResourcePackageName(R.drawable.elves_ball) + "/"
                + r.getResourceTypeName(R.drawable.elves_ball) + "/"
                + r.getResourceEntryName(R.drawable.elves_ball));

        ivFinish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        llIncludeTitle.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        ivAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initDialogOpenAvatar();
            }
        });
        ivAvatar.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                RxImageUtils.showBigImageView(mContext, resultUri);
                return false;
            }
        });
    }

    /**
     * 选择头像 弹窗
     */
    private void initDialogOpenAvatar() {
        final RxDialog dialog1 = new RxDialog(this);
        dialog1.getLayoutParams().gravity = Gravity.BOTTOM;
        View dialogView1 = LayoutInflater.from(this).inflate(
                R.layout.dialog_picker_pictrue, null);
        TextView tv_camera = (TextView) dialogView1
                .findViewById(R.id.tv_camera);
        TextView tv_file = (TextView) dialogView1
                .findViewById(R.id.tv_file);
        TextView tv_cancelid = (TextView) dialogView1
                .findViewById(R.id.tv_cancelid);
        tv_cancelid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                dialog1.cancel();
            }
        });
        tv_camera.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                //请求Camera权限
                RxPermissionsUtils.requestCamera(mContext, new onRequestListener() {
                    @Override
                    public void onRequestBefore() {

                    }

                    @Override
                    public void onRequestLater() {
                        RxPhotoUtils.openCameraImage(ActivityRxPhoto.this);
                        dialog1.cancel();
                    }
                });
            }
        });
        tv_file.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                RxPhotoUtils.openLocalImage(ActivityRxPhoto.this);
                dialog1.cancel();
            }
        });
        dialog1.setContentView(dialogView1);
        dialog1.show();
    }

    private Uri resultUri;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case RxPhotoUtils.GET_IMAGE_FROM_PHONE://选择相册之后的处理
                if (resultCode == RESULT_OK) {
//                    RxPhotoUtils.cropImage(ActivityUser.this, );// 裁剪图片
                    initUCrop(data.getData());
                }

                break;
            case RxPhotoUtils.GET_IMAGE_BY_CAMERA://选择照相机之后的处理
                if (resultCode == RESULT_OK) {
                   /* data.getExtras().get("data");*/
//                    RxPhotoUtils.cropImage(ActivityUser.this, RxPhotoUtils.imageUriFromCamera);// 裁剪图片
                    initUCrop(RxPhotoUtils.imageUriFromCamera);
                }

                break;
            case RxPhotoUtils.CROP_IMAGE://普通裁剪后的处理
                Glide.with(mContext).
                        load(RxPhotoUtils.cropImageUri).
                        diskCacheStrategy(DiskCacheStrategy.RESULT).
                        bitmapTransform(new CropCircleTransformation(mContext)).
                        thumbnail(0.5f).
                        placeholder(R.drawable.elves_ball).
                        priority(Priority.LOW).
                        error(R.drawable.elves_ball).
                        fallback(R.drawable.elves_ball).
                        dontAnimate().
                        into(ivAvatar);
//                RequestUpdateAvatar(new File(RxPhotoUtils.getRealFilePath(mContext, RxPhotoUtils.cropImageUri)));
                break;

            case UCrop.REQUEST_CROP://UCrop裁剪之后的处理
                if (resultCode == RESULT_OK) {
                    resultUri = UCrop.getOutput(data);
                    roadImageView(resultUri, ivAvatar);
                    RxSPUtils.putContent(mContext, "AVATAR", resultUri.toString());
                } else if (resultCode == UCrop.RESULT_ERROR) {
                    final Throwable cropError = UCrop.getError(data);
                }
                break;
            case UCrop.RESULT_ERROR://UCrop裁剪错误之后的处理
                final Throwable cropError = UCrop.getError(data);
                break;
            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    //从Uri中加载图片 并将其转化成File文件返回
    private File roadImageView(Uri uri, ImageView imageView) {
        Glide.with(mContext).
                load(uri).
                diskCacheStrategy(DiskCacheStrategy.RESULT).
                bitmapTransform(new CropCircleTransformation(mContext)).
                thumbnail(0.5f).
                placeholder(R.drawable.elves_ball).
                priority(Priority.LOW).
                error(R.drawable.elves_ball).
                fallback(R.drawable.elves_ball).
                dontAnimate().
                into(imageView);

        return (new File(RxPhotoUtils.getImageAbsolutePath(this, uri)));
    }

    private void initUCrop(Uri uri) {
        //Uri destinationUri = RxPhotoUtils.createImagePathUri(this);

        SimpleDateFormat timeFormatter = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA);
        long time = System.currentTimeMillis();
        String imageName = timeFormatter.format(new Date(time));

        Uri destinationUri = Uri.fromFile(new File(getCacheDir(), imageName + ".jpeg"));

        UCrop.Options options = new UCrop.Options();
        //设置裁剪图片可操作的手势
        options.setAllowedGestures(UCropActivity.SCALE, UCropActivity.ROTATE, UCropActivity.ALL);
        //设置隐藏底部容器，默认显示
        //options.setHideBottomControls(true);
        //设置toolbar颜色
        options.setToolbarColor(ActivityCompat.getColor(this, R.color.colorPrimary));
        //设置状态栏颜色
        options.setStatusBarColor(ActivityCompat.getColor(this, R.color.colorPrimaryDark));

        //开始设置
        //设置最大缩放比例
        options.setMaxScaleMultiplier(5);
        //设置图片在切换比例时的动画
        options.setImageToCropBoundsAnimDuration(666);
        //设置裁剪窗口是否为椭圆
//        options.setOvalDimmedLayer(true);
        //设置是否展示矩形裁剪框
//        options.setShowCropFrame(false);
        //设置裁剪框横竖线的宽度
//        options.setCropGridStrokeWidth(20);
        //设置裁剪框横竖线的颜色
//        options.setCropGridColor(Color.GREEN);
        //设置竖线的数量
//        options.setCropGridColumnCount(2);
        //设置横线的数量
//        options.setCropGridRowCount(1);

        UCrop.of(uri, destinationUri)
                .withAspectRatio(1, 1)
                .withMaxResultSize(1000, 1000)
                .withOptions(options)
                .start(this);
    }

    @OnClick(R.id.btn_exit)
    public void onClick() {
        final RxDialogSureCancle rxDialogSureCancle = new RxDialogSureCancle(this);
        rxDialogSureCancle.getTv_cancle().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rxDialogSureCancle.cancel();
            }
        });
        rxDialogSureCancle.getTv_sure().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        rxDialogSureCancle.show();
    }
}

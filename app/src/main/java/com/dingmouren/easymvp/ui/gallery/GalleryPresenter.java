package com.dingmouren.easymvp.ui.gallery;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.Element;
import android.support.v8.renderscript.RenderScript;
import android.support.v8.renderscript.ScriptIntrinsicBlur;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.ImageViewTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.dingmouren.easymvp.api.ApiManager;
import com.dingmouren.easymvp.bean.GirlPic;
import com.dingmouren.easymvp.helper.CardScaleHelper;
import com.dingmouren.easymvp.util.BlurBitmapUtils;
import com.dingmouren.easymvp.util.SnackbarUtils;
import com.dingmouren.easymvp.util.ViewSwitchUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import jp.wasabeef.glide.transformations.GrayscaleTransformation;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by dingmouren on 2016/12/3.
 */

public class GalleryPresenter extends GalleryContract.Presenter<GalleryContract.View> {

    public GalleryContract.View mGalleryView;
    public RecyclerView mRecycler;
    public GalleryAdapter mGalleryAdapter;
    public LinearLayoutManager mLinearLayoutManager;
    public ImageView mBlurImg;
    private CardScaleHelper mCardScaleHepler;

    private int page = 1;
    private boolean isLoadMore = false;
    private int mLastVisibleItem;
    public List<GirlPic> mList = new ArrayList<>();
    private int mLastPos = -1;
    private Runnable mBlurRunnable;

    public GalleryPresenter(GalleryContract.View view){
        this.mGalleryView = view;
        this.mRecycler = view.getRecyclerView();
        this.mGalleryAdapter = view.getGalleryAdapter();
        mLinearLayoutManager = view.getLayoutManager();
        mBlurImg = view.getBlurImageView();
        mCardScaleHepler = view.getCardScaleHelper();
    }

    //请求数据
    public void requestData(){
        if (isLoadMore) page = page + 1;
        ApiManager.getApiInstance().mApiService.getGirlPics(page)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(girlResult ->  displayData(girlResult.getResults()),this :: loadError);
    }

    /**
     * 没请求到数据
     * @param throwable
     */
    private void loadError(Throwable throwable){
        throwable.printStackTrace();
        SnackbarUtils.showSimpleSnackbar(mRecycler,"网络不见了~~");
    }

    /**
     * 展示相册图片
     * @param list
     */
    private void displayData(List<GirlPic> list){
        mGalleryView.setDataRefresh(true);
        if (list == null){
            return;
        }else {
            mList.addAll(list);
        }
        mGalleryAdapter.setList(mList);
        mGalleryAdapter.notifyDataSetChanged();
        mGalleryView.setDataRefresh(false);
    }

    /**
     * recyclerview的监听，加载到最后一个条目时，请求下一页数据
     */
    public void addScrollistener(){
        mRecycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
               if (newState == RecyclerView.SCROLL_STATE_IDLE){
                   //获取最后一个可见条目的角标
                   mLastVisibleItem = mLinearLayoutManager.findLastVisibleItemPosition();
                   //当显示本页最后一个条目时，加载下一页
                   if (mLastVisibleItem + 1 == mLinearLayoutManager.getItemCount()){
                       mGalleryView.setDataRefresh(true);
                       isLoadMore = true;
                       new Handler().postDelayed(()-> requestData(),1000);
                   }
               }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                mLastVisibleItem = mLinearLayoutManager.findLastVisibleItemPosition();
            }
        });
    }


}

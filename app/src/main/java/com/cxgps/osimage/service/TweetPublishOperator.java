package com.cxgps.osimage.service;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.SharedPreferencesCompat;
import android.text.TextUtils;


import com.cxgps.osimage.contract.TweetPublishContract;
import com.cxgps.osimage.fragment.TweetPublishFragment;
import com.cxgps.osimage.utils.CollectionUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by JuQiu
 * on 16/8/22.
 */
public class TweetPublishOperator implements TweetPublishContract.Operator {
    private final static String SHARE_FILE_NAME = TweetPublishFragment.class.getName();
    private final static String SHARE_VALUES_CONTENT = "content";
    private final static String SHARE_VALUES_IMAGES = "images";
    private final static String SHARE_VALUES_ABOUT = "about";
    private final static String DEFAULT_PRE = "default";
    private final static String SHARE_LOCAL_IMAGE = "share_image";
    private TweetPublishContract.View mView;
    private String mDefaultContent;
    private String[] mDefaultImages;

    private String mLocalImg;

    @Override
    public void setDataView(TweetPublishContract.View view, String defaultContent, String[] defaultImages, String localImg) {
        mView = view;
        mDefaultContent = defaultContent;
        mDefaultImages = defaultImages;

        mLocalImg = localImg;
    }

    @Override
    public void publish() {

    }


    @Override
    public void onBack() {
        saveXmlData();
        mView.finish();
    }

    @Override
    public void loadData() {
        if (isUseXmlCache()) {
            final Context context = mView.getContext();
            SharedPreferences sharedPreferences = context.getSharedPreferences(SHARE_FILE_NAME, Activity.MODE_PRIVATE);
            String content = sharedPreferences.getString(SHARE_VALUES_CONTENT, null);
            Set<String> set = sharedPreferences.getStringSet(SHARE_VALUES_IMAGES, null);
            if (content != null) {
                mView.setContent(content, false);
            }
            if (set != null && set.size() > 0) {
                mView.setImages(CollectionUtil.toArray(set, String.class));
            }
        } else {
            if (!TextUtils.isEmpty(mLocalImg)) {
                mView.setImages(new String[]{mLocalImg});
                return;
            }
            if (mDefaultImages != null && mDefaultImages.length > 0)
                mView.setImages(mDefaultImages);

            boolean haveAbout = false;

            if (!TextUtils.isEmpty(mDefaultContent))
                mView.setContent(mDefaultContent, !haveAbout);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        final String content = mView.getContent();
        final String[] paths = mView.getImages();
        if (content != null)
            outState.putString(SHARE_VALUES_CONTENT, content);
        if (paths != null && paths.length > 0)
            outState.putStringArray(SHARE_VALUES_IMAGES, paths);
        // save default
        if (mDefaultContent != null) {
            outState.putString(DEFAULT_PRE + SHARE_VALUES_CONTENT, mDefaultContent);
        }
        if (mDefaultImages != null && mDefaultImages.length > 0) {
            outState.putStringArray(DEFAULT_PRE + SHARE_VALUES_IMAGES, mDefaultImages);
        }

    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        String content = savedInstanceState.getString(SHARE_VALUES_CONTENT, null);
        String[] images = savedInstanceState.getStringArray(SHARE_VALUES_IMAGES);
        if (content != null) {
            mView.setContent(content, false);
        }
        if (images != null && images.length > 0) {
            mView.setImages(images);
        }
        // Read default
        mDefaultContent = savedInstanceState.getString(DEFAULT_PRE + SHARE_VALUES_CONTENT, null);
        mDefaultImages = savedInstanceState.getStringArray(DEFAULT_PRE + SHARE_VALUES_IMAGES);

    }

    private void clearAndFinish(Context context) {
        if (isUseXmlCache()) {
            SharedPreferences sharedPreferences = context.getSharedPreferences(SHARE_FILE_NAME, Activity.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(SHARE_VALUES_CONTENT, null);
            editor.putStringSet(SHARE_VALUES_IMAGES, null);
            SharedPreferencesCompat.EditorCompat.getInstance().apply(editor);
        }
        mView.finish();
    }


    private void saveXmlData() {
        if (isUseXmlCache()) {
            final Context context = mView.getContext();
            final String content = mView.getContent();
            final String[] paths = mView.getImages();
            SharedPreferences sharedPreferences = context.getSharedPreferences(SHARE_FILE_NAME, Activity.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(SHARE_VALUES_CONTENT, content);
            if (paths != null && paths.length > 0) {
                editor.putStringSet(SHARE_VALUES_IMAGES, CollectionUtil.toHashSet(paths));
            } else {
                editor.putStringSet(SHARE_VALUES_IMAGES, null);
            }
            SharedPreferencesCompat.EditorCompat.getInstance().apply(editor);
        }
    }

    private boolean isUseXmlCache() {
        if (!TextUtils.isEmpty(mLocalImg))
            return false;
        return TextUtils.isEmpty(mDefaultContent)
                && (mDefaultImages == null || mDefaultImages.length == 0);
    }
}

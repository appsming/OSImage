package com.cxgps.osimage.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Size;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;


import com.cxgps.osimage.BuildConfig;
import com.cxgps.osimage.R;
import com.cxgps.osimage.base.activities.BaseBackActivity;
import com.cxgps.osimage.contract.TweetPublishContract;
import com.cxgps.osimage.fragment.TweetPublishFragment;
import com.cxgps.osimage.service.TweetPublishService;
import com.cxgps.osimage.utils.CollectionUtil;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by JuQiu
 * on 16/8/22.
 */
public class TweetPublishActivity extends BaseBackActivity {
    private static final String TAG = "TweetPublishActivity";
    private TweetPublishContract.View mView;



    public static void show(Context context) {

        Intent intent = new Intent(context, TweetPublishActivity.class);
        context.startActivity(intent);
    }


    @Override
    protected int getContentView() {
        // hide the software
        // getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        return R.layout.activity_tweet_publish;
    }

    @SuppressWarnings({"MismatchedQueryAndUpdateOfCollection", "unchecked", "ResultOfMethodCallIgnored"})
    @Override
    protected void initWidget() {
        super.initWidget();
        setStatusBarDarkMode();
        getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        if (bundle == null) bundle = new Bundle();
        // Read other data
        readFastShareByOther(bundle, intent);

        TweetPublishFragment fragment = new TweetPublishFragment();
        // init the args bounds
        fragment.setArguments(bundle);
        FragmentTransaction trans = getSupportFragmentManager()
                .beginTransaction();
        trans.replace(R.id.activity_tweet_publish, fragment);
        trans.commit();
        mView = fragment;
    }

    /**
     * 读取快速分享到当前界面的内容
     *
     * @param intent 需要写入源
     */
    private void readFastShareByOther(Bundle bundle, Intent intent) {
        // Check
        if (intent == null)
            return;
        String type = intent.getType();
        if (TextUtils.isEmpty(type))
            return;

        //判断当前分享的内容是文本，还是图片
        if ("text/plain".equals(type)) {
            String text = intent.getStringExtra(Intent.EXTRA_TEXT);
            bundle.putString("defaultContent", text);
        } else if (type.startsWith("image/")) {
            ArrayList<String> uris = new ArrayList<>();
            Object obj = intent.getExtras().get(Intent.EXTRA_STREAM);
            if (obj instanceof Uri) {
                Uri uri = (Uri) obj;
                String decodePath = decodePath(uri);
                if (decodePath != null)
                    uris.add(decodePath);
            } else {
                try {
                    @SuppressWarnings("unchecked")
                    ArrayList<Uri> list = (ArrayList<Uri>) obj;
                    //大于9张图片的分享，直接只使用前9张
                    if (list != null && list.size() > 0) {
                        for (int i = 0, len = list.size(); i < len; i++) {
                            if (i > 9) {
                                break;
                            }
                            String decodePath = decodePath(list.get(i));
                            if (decodePath != null)
                                uris.add(decodePath);
                        }
                    }
                } catch (Exception e) {
                    if (BuildConfig.DEBUG)
                        e.printStackTrace();
                }
            }
            if (uris.size() > 0) {
                String[] paths = CollectionUtil.toArray(uris, String.class);
                bundle.putStringArray("defaultImages", paths);
            }
        }
    }

    /**
     * 通过uri当中的唯一id搜索本地相册图片，是否真的存在。然后返回真实的path路径
     *
     * @param uri rui
     * @return path
     */
    private String decodePath(Uri uri) {
        String decodePath = null;
        String uriPath = uri.toString();

        if (uriPath != null && uriPath.startsWith("content://")) {
            int id = 0;
            try {
                id = Integer.parseInt(uriPath.substring(uriPath.lastIndexOf("/") + 1, uriPath.length()));
            } catch (Exception e) {
                e.printStackTrace();
                return parseUri(uri);
            }

            Uri tempUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            String[] projection = {MediaStore.Images.Media.DATA};
            String selection = MediaStore.Images.Media._ID + "=?";
            String[] selectionArgs = {id + ""};

            Cursor cursor = getContentResolver().query(tempUri, projection, selection, selectionArgs, null);
            try {
                while (cursor != null && cursor.moveToNext()) {
                    String temp = cursor.getString(0);
                    File file = new File(temp);
                    if (file.exists()) {
                        decodePath = temp;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (cursor != null && !cursor.isClosed()) {
                    cursor.close();
                }
            }

        } else {
            return uriPath;
        }
        return decodePath;
    }

    private String parseUri(Uri uri) {
        String path = uri.getPath();
        Log.e("path", "path" + path);
        if (path != null) {
            File file = new File(path.replace("/raw/", ""));
            return file.exists() ? file.getPath() : "";
        }
        return "";
    }

    @Override
    protected void initData() {
        super.initData();
        // before the fragment show
        registerPublishStateReceiver();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        //暂不处理已在当前界面下的分享
    }

    @Override
    protected void onPause() {
        unRegisterPublishStateReceiver();
        super.onPause();
    }

    private void registerPublishStateReceiver() {
        if (mPublishStateReceiver != null)
            return;
        IntentFilter intentFilter = new IntentFilter(TweetPublishService.ACTION_RECEIVER_SEARCH_FAILED);
        BroadcastReceiver receiver = new SearchReceiver();
        registerReceiver(receiver, intentFilter);
        mPublishStateReceiver = receiver;

        // start search
        TweetPublishService.startActionSearchFailed(this);
    }

    private void unRegisterPublishStateReceiver() {
        final BroadcastReceiver receiver = mPublishStateReceiver;
        mPublishStateReceiver = null;
        if (receiver != null)
            unregisterReceiver(receiver);
    }

    private BroadcastReceiver mPublishStateReceiver;

    private class SearchReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (TweetPublishService.ACTION_RECEIVER_SEARCH_FAILED.equals(intent.getAction())) {
                String[] ids = intent.getStringArrayExtra(TweetPublishService.EXTRA_IDS);
                if (ids == null || ids.length == 0)
                    return;
               // TweetPublishQueueActivity.show(TweetPublishActivity.this, ids);
            }
        }
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        if (mView != null && mView.onBackPressed()) {
            mView.getOperator().onBack();
        }
    }
}

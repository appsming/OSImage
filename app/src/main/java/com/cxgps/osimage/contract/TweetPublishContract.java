package com.cxgps.osimage.contract;

import android.content.Context;
import android.os.Bundle;


/**
 * Created by JuQiu
 * on 16/7/14.
 */

public interface TweetPublishContract {
    interface Operator {
        void setDataView(View view, String defaultContent, String[] defaultImages,String localImg);

        void publish();

        void onBack();

        void loadData();

        void onSaveInstanceState(Bundle outState);

        void onRestoreInstanceState(Bundle savedInstanceState);
    }

    interface View {
        Context getContext();

        String getContent();

        void setContent(String content, boolean needSelectionEnd);

        boolean needCommit();

        String[] getImages();

        void setImages(String[] paths);

        void finish();

        Operator getOperator();

        boolean onBackPressed();
    }
}

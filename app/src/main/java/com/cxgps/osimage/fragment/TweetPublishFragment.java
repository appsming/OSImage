package com.cxgps.osimage.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.text.Editable;
import android.text.Spannable;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;


import com.cxgps.osimage.R;
import com.cxgps.osimage.adapter.TextWatcherAdapter;
import com.cxgps.osimage.base.activities.BaseBackActivity;
import com.cxgps.osimage.base.fragment.BaseFragment;
import com.cxgps.osimage.contract.TweetPublishContract;
import com.cxgps.osimage.service.TweetPublishOperator;
import com.cxgps.osimage.utils.OnKeyArrivedListenerAdapterV2;
import com.cxgps.osimage.widget.RichEditText;
import com.cxgps.osimage.widget.TweetPicturesPreviewer;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 发布动弹界面实现
 */
@SuppressWarnings("WeakerAccess")
public class TweetPublishFragment extends BaseFragment implements View.OnClickListener,
        TweetPublishContract.View {

    public static final int MAX_TEXT_LENGTH = 160;
    public static final int REQUEST_CODE_SELECT_FRIENDS = 0x0001;
    public static final int REQUEST_CODE_SELECT_TOPIC = 0x0002;

    @BindView(R.id.edit_content)
    RichEditText mEditContent;

    @BindView(R.id.recycler_images)
    TweetPicturesPreviewer mLayImages;

    @BindView(R.id.txt_indicator)
    TextView mIndicator;


    private TweetPublishContract.Operator mOperator;


    public TweetPublishFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        // init operator
        this.mOperator = new TweetPublishOperator();
        String defaultContent = null;
        String[] paths = null;

        Bundle bundle = getArguments();
        String localImg = null;
        if (bundle != null) {
            defaultContent = bundle.getString("defaultContent");
            paths = bundle.getStringArray("defaultImages");
            localImg = bundle.getString("imageUrl");
        }
       this.mOperator.setDataView(this, defaultContent, paths,localImg);

        super.onAttach(context);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_tweet_publish;
    }

    @SuppressLint("ClickableViewAccessibility")
    @SuppressWarnings("all")
    @Override
    protected void initWidget(View root) {
        super.initWidget(root);

        // set hide action

        mLayImages.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideAllKeyBoard();
                return false;
            }
        });

        // add text change listener
        mEditContent.addTextChangedListener(new TextWatcherAdapter() {
            @Override
            public void afterTextChanged(Editable s) {
                final int len = s.length();
                final int surplusLen = MAX_TEXT_LENGTH - len;
                // set the send icon state
                setSendIconStatus(len > 0 && surplusLen >= 0, s.toString());
                // checkShare the indicator state
                if (surplusLen > 10) {
                    // hide
                    if (mIndicator.getVisibility() != View.INVISIBLE) {
                        ViewCompat.animate(mIndicator)
                                .alpha(0)
                                .setDuration(200)
                                .withEndAction(new Runnable() {
                                    @Override
                                    public void run() {
                                        mIndicator.setVisibility(View.INVISIBLE);
                                    }
                                })
                                .start();
                    }
                } else {
                    // show
                    if (mIndicator.getVisibility() != View.VISIBLE) {
                        ViewCompat.animate(mIndicator)
                                .alpha(1f)
                                .setDuration(200)
                                .withStartAction(new Runnable() {
                                    @Override
                                    public void run() {
                                        mIndicator.setVisibility(View.VISIBLE);
                                    }
                                })
                                .start();
                    }

                    mIndicator.setText(String.valueOf(surplusLen));
                    //noinspection deprecation
                    mIndicator.setTextColor(surplusLen >= 0 ?
                            getResources().getColor(R.color.tweet_indicator_text_color) :
                            getResources().getColor(R.color.tweet_indicator_text_color_error));
                }
            }
        });

        // 设置键盘输入#或者@适合的监听器
        mEditContent.setOnKeyArrivedListener(new OnKeyArrivedListenerAdapterV2(this));
        mEditContent.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                return false;
            }
        });

        // Show keyboard
        showSoftKeyboard(mEditContent);
    }

    private void setSendIconStatus(boolean haveContent, String content) {
        if (haveContent) {
            content = content.trim();
            haveContent = !TextUtils.isEmpty(content);
        }

    }


    @Override
    protected void initData() {
        super.initData();
        mOperator.loadData();
    }

    // 用于拦截后续的点击事件
    private long mLastClickTime;

    @OnClick({R.id.iv_picture,
          R.id.txt_indicator,
           R.id.edit_content})
    @Override
    public void onClick(View v) {
        // 用来解决快速点击多个按钮弹出多个界面的情况
        long nowTime = System.currentTimeMillis();
        if ((nowTime - mLastClickTime) < 500)
            return;
        mLastClickTime = nowTime;

        try {
            switch (v.getId()) {
                case R.id.iv_picture:
                    hideAllKeyBoard();
                    mLayImages.onLoadMoreClick();
                    break;
                case R.id.txt_indicator:
                    handleClearContentClick();
                    break;

                case R.id.edit_content: {

                }
                break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleClearContentClick() {
        if (mIndicator.isSelected()) {
            mIndicator.setSelected(false);
            mEditContent.setText("");
        } else {
            mIndicator.setSelected(true);
            mIndicator.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mIndicator.setSelected(false);
                }
            }, 1000);
        }
    }





    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_SELECT_FRIENDS:
                    // Nun Do handleSelectFriendsResult(data);
                    break;
                case REQUEST_CODE_SELECT_TOPIC:
                    // Nun Do handleSelectTopicResult(data);
                    break;
            }
        }

        mEditContent.postDelayed(new Runnable() {
            @Override
            public void run() {
                showSoftKeyboard(mEditContent);
            }
        }, 200);
    }

    private void hideSoftKeyboard() {
        mEditContent.clearFocus();
        ((InputMethodManager) getActivity().getSystemService(
                Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(
                mEditContent.getWindowToken(), 0);
    }

    private void showSoftKeyboard(final EditText requestView) {
        if (requestView == null)
            return;
        requestView.requestFocus();
        ((InputMethodManager) getActivity().getSystemService(
                Context.INPUT_METHOD_SERVICE)).showSoftInput(requestView,
                InputMethodManager.SHOW_FORCED);
    }

    private void hideAllKeyBoard() {

        hideSoftKeyboard();
    }

    @Override
    public String getContent() {
        return mEditContent.getText().toString();
    }

    @Override
    public void setContent(String content, boolean needSelectionEnd) {

        //if (needSelectionEnd)
        mEditContent.setSelection(mEditContent.getText().length());
    }


    @Override
    public boolean needCommit() {
        return false;
    }

    @Override
    public String[] getImages() {
        return mLayImages.getPaths();
    }

    @Override
    public void setImages(String[] paths) {
        mLayImages.set(paths);
    }

    @Override
    public void finish() {
        // hide key board before finish
        hideAllKeyBoard();
        // finish
        Activity activity = getActivity();
        if (activity != null && activity instanceof BaseBackActivity) {
            ((BaseBackActivity) activity).onSupportNavigateUp();
        }
    }

    @Override
    public TweetPublishContract.Operator getOperator() {
        return mOperator;
    }

    @Override
    public boolean onBackPressed() {

        return true;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mOperator.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestartInstance(Bundle bundle) {
        super.onRestartInstance(bundle);
        if (bundle != null)
            mOperator.onRestoreInstanceState(bundle);
    }
}

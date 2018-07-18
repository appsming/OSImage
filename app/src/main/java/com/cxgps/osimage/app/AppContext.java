package com.cxgps.osimage.app;

public class AppContext extends BaseApplication {
    public static final int PAGE_SIZE = 20;// 默认分页大小
    private static AppContext instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

    }

    public static AppContext getInstance() {
        return instance;
    }
}

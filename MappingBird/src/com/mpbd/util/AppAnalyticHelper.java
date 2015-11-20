package com.mpbd.util;

import android.app.Activity;
import android.content.Context;

import com.flurry.android.FlurryAgent;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;

public class AppAnalyticHelper {

    
    public static final String CATEGORY_UI_ACTION = "category_ui_action";
    
    public static final String ACTION_BUTTON_PRESS = "action_button_press";
    public static final String ACTION_COLLECTION_LIST_ITEM_PRESS = "action_collection_list_item_press";
    public static final String ACTION_PLACE_LIST_ITEM_PRESS = "action_place_list_item_press";
    public static final String ACTION_IMAGE_SWIPE = "action_image_swipe";
    public static final String ACTION_IMAGE_CLICK = "action_image_click";
    
    public static final String LABEL_LIST_ITEM = "label_list_item";
    public static final String LABEL_LIST_ITEM_SETTING = "label_list_item_setting";
    public static final String LABEL_LIST_ITEM_HELP = "label_list_itme_help";
    
    public static final String LABEL_LIST_ITEM_SIGN_IN = "label_list_item_sign_in";
    public static final String LABEL_LIST_ITEM_SIGN_UP = "label_list_item_sign_up";
    public static final String LABEL_MAP_IN_PLACE_PAGE = "label_map_in_place_page";
    public static final String LABEL_SWIPE_IN_PLACE_PAGE = "label_swipe_in_place_page";
    
    public static final String LABEL_BUTTON_NAVIGATE = "label_button_navigate";
    public static final String LABEL_BUTTON_LOGOUT_OK = "label_logout_ok";
    public static final String LABEL_BUTTON_LOGOUT_CANCEL = "label_logout_cancel";
    public static final String LABEL_BUTTON_SHARE = "label_button_share";

    public static final long VALUE_OPERATION_SUCCESS = 1000;
    public static final long VALUE_OPERATION_FAIL = 2000;
    
    private AppAnalyticHelper(){
    }
    
    private static class LazyHolder {
        private static final AppAnalyticHelper INSTANCE = new AppAnalyticHelper();
    }
 
    public static AppAnalyticHelper getInstance() {
        return LazyHolder.INSTANCE;
    }
           
    public static void startSession(Activity activity){
        if (activity != null) {
            EasyTracker.getInstance(activity).activityStart(activity);
            FlurryAgent.onStartSession(activity);
        }
    }
    
    public static void endSession(Activity activity) {
        if (activity != null) {
            EasyTracker.getInstance(activity).activityStop(activity);
            FlurryAgent.onEndSession(activity);
        }
    }

    // Event category (required)
    // Event action (required)
    // Event label
    // Event value
    public static void sendEvent(Context ctx, String category, String acton, String label, long value){
        
        EasyTracker easyTracker = EasyTracker.getInstance(ctx);

        // DISPLAY在 GA時, 會顯示在 分類 CATEGORY, 動作EVENT (ACTION) 這兩個也可以自己定義
        easyTracker.send(MapBuilder.createEvent(category, acton, label, value).build());
    }
    
}

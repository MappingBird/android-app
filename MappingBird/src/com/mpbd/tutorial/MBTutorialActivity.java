package com.mpbd.tutorial;

import java.util.Stack;

import android.animation.Keyframe;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mpbd.widget.MappingBirdParallaxPager;
import com.mpbd.mappingbird.R;
import com.mpbd.mappingbird.util.AppAnalyticHelper;
import com.mpbd.mappingbird.util.MBUtil;
import com.viewpagerindicator.CirclePageIndicator;
import com.viewpagerindicator.PageIndicator;

public class MBTutorialActivity extends FragmentActivity {

    private final static String TAG = "MBTutorialActivity";
    private MappingBirdParallaxPager viewPager;
    private int mScreenHeight;
    private int mScreenWidth;
    private boolean mTransitionAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.mb_activity_layout_tutorial);

        viewPager = (MappingBirdParallaxPager) findViewById(R.id.tutoral_pager);
        mScreenHeight = MBUtil.getScreenHeight(MBTutorialActivity.this);
        mScreenWidth = MBUtil.getScreenWidth(MBTutorialActivity.this);
        mTransitionAnimation = false;

        MBViewPagerAdapter viewPagerAdapter = new MBViewPagerAdapter(MBTutorialActivity.this);
        viewPager.setAdapter(viewPagerAdapter);

        final View logoIconView = findViewById(R.id.tutoral_page_logo_icon);
        final RelativeLayout.LayoutParams logoIconParams = (RelativeLayout.LayoutParams) logoIconView.getLayoutParams();

        final View logoTextView = findViewById(R.id.tutoral_page_logo_text);
        final RelativeLayout.LayoutParams logoTextParams = (RelativeLayout.LayoutParams) logoTextView.getLayoutParams();

        final View sloganImageView = findViewById(R.id.tutoral_page_slogan_img);
        final RelativeLayout.LayoutParams sloganImageViewParams = (RelativeLayout.LayoutParams) sloganImageView.getLayoutParams();
        
        final View layoutBottomBtns = findViewById(R.id.layout_tutoral_bottom_btns);
        final FrameLayout.LayoutParams bottomBtnsParams = (FrameLayout.LayoutParams) layoutBottomBtns.getLayoutParams();

        
        final PageIndicator mIndicator = (CirclePageIndicator) findViewById(R.id.tutoral_pager_indicator);
        final RelativeLayout.LayoutParams indicatorParams = (RelativeLayout.LayoutParams) ((View) mIndicator).getLayoutParams();
        
        mIndicator.setViewPager(viewPager);
        mIndicator.setOnPageChangeListener(new OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                //Log.d(TAG, "onPageScrolled : " + String.format(" position : %d, positionOffset : %f, positionOffsetPixels : %d,", position, positionOffset, positionOffsetPixels));

                ((View) mIndicator).setVisibility(View.VISIBLE);

                float iconOrignaSize = 87f; //height
                float iconScaledSize = 37f; //height

                float textOrignaSize = 45f; //height
                float textScaledSize = 36f; //height

                int iconStartOffsetX = 87; // A_X
                int iconEndOffsetX = 244;  // B_X

                float iconStartOffsetY = 0.22f; // A_Y
                float iconEndOffsetY = 0.07f;  // B_Y

                float logoTextOriginTopMargin = 0.344f;
                float logoTextAfterTopMargin = 0.10f;

                if (position == 0) {
                    sloganImageView.setVisibility(View.INVISIBLE);

                    float iconScaleFactor = 1 - ((iconOrignaSize - iconScaledSize) / iconOrignaSize) * positionOffset;
                    float textScaleFactor = 1 - ((textOrignaSize - textScaledSize) / textOrignaSize) * positionOffset;

                    // set margin
                    logoIconParams.topMargin = (int) (mScreenHeight * (iconStartOffsetY + (iconEndOffsetY - iconStartOffsetY) * positionOffset));
                    logoIconParams.leftMargin = (int) (mScreenWidth - MBUtil.getPixelsFromDip(iconStartOffsetX) + MBUtil.getPixelsFromDip(iconStartOffsetX - iconEndOffsetX) * positionOffset) / 2;
                    logoIconView.setLayoutParams(logoIconParams);

                    logoTextParams.topMargin = (int) (mScreenHeight * (logoTextOriginTopMargin - (positionOffset * (logoTextOriginTopMargin - logoTextAfterTopMargin))));
                    logoTextParams.leftMargin = (int) (mScreenWidth - MBUtil.getPixelsFromDip(194) + MBUtil.getPixelsFromDip(39) * positionOffset) / 2;
                    logoTextView.setLayoutParams(logoTextParams);

                    // set scale
                    logoIconView.setScaleX(iconScaleFactor);
                    logoIconView.setScaleY(iconScaleFactor);

                    logoTextView.setScaleX(textScaleFactor);
                    logoTextView.setScaleY(textScaleFactor);


                } else if (position == 3) {
                    bottomBtnsParams.topMargin = (int) (positionOffset * 200);
                    layoutBottomBtns.setLayoutParams(bottomBtnsParams);

                    int rightMargin = (int) (positionOffset * 2000);

                    if (rightMargin > (mScreenWidth)) {
                        ((View) mIndicator).setVisibility(View.INVISIBLE);
                    }

                    indicatorParams.rightMargin = rightMargin;
                    ((View) mIndicator).setLayoutParams(indicatorParams);

                    float iconScaleFactor = iconScaledSize / iconOrignaSize + ((iconOrignaSize - iconScaledSize) / iconOrignaSize) * positionOffset;
                    float textScaleFactor = textScaledSize / textOrignaSize + ((textOrignaSize - textScaledSize) / textOrignaSize) * positionOffset;

                    // set margin
                    logoIconParams.topMargin = (int) (mScreenHeight * (iconEndOffsetY + (iconStartOffsetY - iconEndOffsetY) * positionOffset));
                    logoIconParams.leftMargin = (int) (mScreenWidth - MBUtil.getPixelsFromDip(iconEndOffsetX) + MBUtil.getPixelsFromDip(iconEndOffsetX - iconStartOffsetX) * positionOffset) / 2;
                    logoIconView.setLayoutParams(logoIconParams);


                    logoTextParams.topMargin = (int) (mScreenHeight * (logoTextAfterTopMargin + (positionOffset * (logoTextOriginTopMargin - logoTextAfterTopMargin))));
                    logoTextParams.leftMargin = (int) (mScreenWidth - MBUtil.getPixelsFromDip(155) - MBUtil.getPixelsFromDip(39) * (positionOffset)) / 2;
                    logoTextView.setLayoutParams(logoTextParams);

                    // set scale
                    logoIconView.setScaleX(iconScaleFactor);
                    logoIconView.setScaleY(iconScaleFactor);

                    logoTextView.setScaleX(textScaleFactor);
                    logoTextView.setScaleY(textScaleFactor);


                    // slogan
                    sloganImageView.setVisibility(View.VISIBLE);
                    sloganImageViewParams.topMargin = (int) (mScreenHeight * logoTextOriginTopMargin + MBUtil.getPixelsFromDip(39) + MBUtil.getPixelsFromDip(10) * positionOffset);
                    sloganImageViewParams.leftMargin = (int) (mScreenWidth - MBUtil.getPixelsFromDip(200)) / 2;
                    sloganImageView.setLayoutParams(sloganImageViewParams);
                    sloganImageView.setAlpha(positionOffset);

                } else if (position == 4) {
                    ((View) mIndicator).setVisibility(View.INVISIBLE);


                    float iconScaleFactor = iconScaledSize / iconOrignaSize + ((iconOrignaSize - iconScaledSize) / iconOrignaSize);
                    float textScaleFactor = textScaledSize / textOrignaSize + ((textOrignaSize - textScaledSize) / textOrignaSize);

                    // set margin
                    logoIconParams.topMargin = (int) (mScreenHeight * (iconEndOffsetY + (iconStartOffsetY - iconEndOffsetY)));
                    logoIconParams.leftMargin = (int) (mScreenWidth - MBUtil.getPixelsFromDip(iconEndOffsetX) + MBUtil.getPixelsFromDip(iconEndOffsetX - iconStartOffsetX)) / 2;
                    logoIconView.setLayoutParams(logoIconParams);


                    logoTextParams.topMargin = (int) (mScreenHeight * logoTextOriginTopMargin);
                    logoTextParams.leftMargin = (int) (mScreenWidth - MBUtil.getPixelsFromDip(155) - MBUtil.getPixelsFromDip(39)) / 2;
                    logoTextView.setLayoutParams(logoTextParams);

                    // set scale
                    logoIconView.setScaleX(iconScaleFactor);
                    logoIconView.setScaleY(iconScaleFactor);

                    logoTextView.setScaleX(textScaleFactor);
                    logoTextView.setScaleY(textScaleFactor);

                    // slogan
                    sloganImageView.setVisibility(View.VISIBLE);
                    sloganImageViewParams.topMargin = (int) (mScreenHeight * logoTextOriginTopMargin + MBUtil.getPixelsFromDip(45 + 4));
                    sloganImageViewParams.leftMargin = (int) (mScreenWidth - MBUtil.getPixelsFromDip(200)) / 2;
                    sloganImageView.setLayoutParams(sloganImageViewParams);


                } else {  // position == 1 || position == 2
                    sloganImageView.setVisibility(View.INVISIBLE);

                    float iconScaleFactor = 1 - ((iconOrignaSize - iconScaledSize) / iconOrignaSize) * 1;
                    float textScaleFactor = 1 - ((textOrignaSize - textScaledSize) / textOrignaSize) * 1;

                    // set margin
                    logoIconParams.topMargin = (int) (mScreenHeight * (iconStartOffsetY + (iconEndOffsetY - iconStartOffsetY)));
                    logoIconParams.leftMargin = (int) (mScreenWidth - MBUtil.getPixelsFromDip(iconStartOffsetX) + MBUtil.getPixelsFromDip(iconStartOffsetX - iconEndOffsetX)) / 2;
                    logoIconView.setLayoutParams(logoIconParams);

                    logoTextParams.topMargin = (int) (mScreenHeight * (logoTextOriginTopMargin - (1 * (logoTextOriginTopMargin - logoTextAfterTopMargin))));
                    logoTextParams.leftMargin = (int) (mScreenWidth - MBUtil.getPixelsFromDip(194) + MBUtil.getPixelsFromDip(39)) / 2;
                    logoTextView.setLayoutParams(logoTextParams);

                    // set scale
                    logoIconView.setScaleX(iconScaleFactor);
                    logoIconView.setScaleY(iconScaleFactor);

                    logoTextView.setScaleX(textScaleFactor);
                    logoTextView.setScaleY(textScaleFactor);

                }

            }

            @Override
            public void onPageSelected(int position) {
                // do nothing
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                // do nothing
            }
        });

        findViewById(R.id.tutoral_bottom_btn_log_in).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(MBTutorialActivity.this, com.mpbd.mappingbird.MBLoginActivity.class);
                MBTutorialActivity.this.startActivity(intent);
            }
        });

        findViewById(R.id.tutoral_bottom_btn_sign_up).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(MBTutorialActivity.this, com.mpbd.mappingbird.MBSignUpActivity.class);
                MBTutorialActivity.this.startActivity(intent);
            }
        });


        if(!mTransitionAnimation){
            doTranslationUpAnimation(layoutBottomBtns, (int) MBUtil.getPixelsFromDip(60), 350);
            doTranslationUpAnimation(logoIconView, (int) MBUtil.getPixelsFromDip(60), 350);
            doTranslationUpAnimation(logoTextView, (int) MBUtil.getPixelsFromDip(60), 350);

            doFadeInAnimation((View)mIndicator, 350);

            mTransitionAnimation = true;
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    protected void onStart() {
        super.onStart();
        AppAnalyticHelper.startSession(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        AppAnalyticHelper.endSession(this);
    }

    static class MBTutorPagerViewHolder {

        Context ctx;
        View rootView;

        // layout - welcome
        RelativeLayout layoutWelcome;
        TextView welcomeMsgText;
        TextView learnMoreText;

        // layout 2
        RelativeLayout layoutIntroPage;
        ImageView introImage;
        TextView introText;

        // layout 3 - create account
        RelativeLayout layoutCreatOrDoItLater;
        TextView tvLogIn;
        TextView tvSignUp;

        public MBTutorPagerViewHolder(View view, Context context) {
            rootView = view;
            ctx = context;

            int screenHeight = MBUtil.getScreenHeight(context);
            FrameLayout.LayoutParams params;


            // layout - welcome (Page 1)
            layoutWelcome = (RelativeLayout) view.findViewById(R.id.tutoral_welcome);
            welcomeMsgText = (TextView) view.findViewById(R.id.tutoral_welcome_message);
            learnMoreText = (TextView) view.findViewById(R.id.tutoral_welcome_swipe_to_learn_more);

            RelativeLayout.LayoutParams welcomeMsgTextParams = (RelativeLayout.LayoutParams)welcomeMsgText.getLayoutParams();
            welcomeMsgTextParams.topMargin = (int)(screenHeight * 0.47f);
            welcomeMsgText.setLayoutParams(welcomeMsgTextParams);

            RelativeLayout.LayoutParams learnMoreTextParams = (RelativeLayout.LayoutParams)learnMoreText.getLayoutParams();
            learnMoreTextParams.topMargin = (int)(screenHeight * 0.75f);
            learnMoreText.setLayoutParams(learnMoreTextParams);


            // layout 2 (Page 2 ~ 4)
            layoutIntroPage = (RelativeLayout) view.findViewById(R.id.layout_tutoral_page);
            introText = (TextView) view.findViewById(R.id.page_intro_title);
            introImage = (ImageView) view.findViewById(R.id.page_intro_image);

            params = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            params.gravity = Gravity.CENTER;
            params.height = (screenHeight / 100) * 66;
            layoutIntroPage.setLayoutParams(params);


            // layout 3 - create account (Page 5)
            layoutCreatOrDoItLater = (RelativeLayout) view.findViewById(R.id.tutoral_create_or_do_later);
            tvLogIn = (TextView) view.findViewById(R.id.tutoral_log_in);
            tvSignUp = (TextView) view.findViewById(R.id.tutoral_sign_up);

        }

    }

    public class MBViewPagerAdapter extends PagerAdapter {

        private final String TAG = MBViewPagerAdapter.class.getSimpleName();
        Context context;
        boolean isAnimated = false;

        int targetLayoutId = R.layout.mb_tutorial_page_layout;
        Stack<View> recycledViewsList = new Stack<View>();

        private MBTutorPagerViewHolder inflateOrRecycleView() {

            View rootView;
            MBTutorPagerViewHolder vh;
            if (recycledViewsList.isEmpty()) {
                rootView = LayoutInflater.from(MBTutorialActivity.this).inflate(targetLayoutId, null, false);

                Log.i(TAG, "create a new view : " + rootView.hashCode());
            } else {
                rootView = recycledViewsList.pop();

                Log.i(TAG, "Restored recycled view from cache " + rootView.hashCode());
            }

            vh = new MBTutorPagerViewHolder(rootView, context);
            rootView.setTag(vh);

            return vh;
        }

        public MBViewPagerAdapter(Context ctx) {
            context = ctx;
        }

        @Override
        public int getCount() {
            return 5;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {

            final MBTutorPagerViewHolder vh = inflateOrRecycleView();

            if (position == 0) {
                vh.layoutWelcome.setVisibility(View.VISIBLE);
                vh.layoutIntroPage.setVisibility(View.GONE);
                vh.layoutCreatOrDoItLater.setVisibility(View.GONE);

                if(!isAnimated){
                    doFadeInAnimation(vh.welcomeMsgText, 350);
                    doFadeInAnimation(vh.learnMoreText, 350);
                    isAnimated = true;
                }

            } else if (position == 4) {
                vh.layoutWelcome.setVisibility(View.GONE);
                vh.layoutIntroPage.setVisibility(View.GONE);
                vh.layoutCreatOrDoItLater.setVisibility(View.VISIBLE);

                vh.tvLogIn.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent();
                        intent.setClass(MBTutorialActivity.this, com.mpbd.mappingbird.MBLoginActivity.class);
                        MBTutorialActivity.this.startActivity(intent);
                    }
                });

                vh.tvSignUp.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent();
                        intent.setClass(MBTutorialActivity.this, com.mpbd.mappingbird.MBSignUpActivity.class);
                        MBTutorialActivity.this.startActivity(intent);
                    }
                });

            } else {
                vh.layoutWelcome.setVisibility(View.GONE);
                vh.layoutIntroPage.setVisibility(View.VISIBLE);
                vh.layoutCreatOrDoItLater.setVisibility(View.GONE);

                if (position == 1) {
                    vh.introText.setText(R.string.tutorial_page1_scenario_intro);
                    vh.introImage.setImageResource(R.drawable.oobe_splash_001);
                } else if (position == 2) {
                    vh.introText.setText(R.string.tutorial_page2_scenario_intro);
                    vh.introImage.setImageResource(R.drawable.oobe_splash_002);
                } else if (position == 3) {
                    vh.introText.setText(R.string.tutorial_page3_scenario_intro);
                    vh.introImage.setImageResource(R.drawable.oobe_splash_003);
                }
            }

            ((ViewPager) container).addView(vh.rootView);
            return vh.rootView;
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object view) {
            View recycledView = (View) view;

            MappingBirdParallaxPager pager = (MappingBirdParallaxPager) container;

            pager.removeView(recycledView);
            recycledViewsList.push(recycledView);

            Log.i(TAG, "Stored view in cache " + recycledView.hashCode());

        }

    }

    private void doTranslationUpAnimation(final View view, final int offset, final long durationInMill){

        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "translationY", offset, 0);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.setDuration(durationInMill);
        animator.start();
    }

    private void doTranslationUpAnimationEx(final View view, final int offset, final long durationInMill){

        Keyframe kf0 = Keyframe.ofFloat(0f / durationInMill, offset);
        Keyframe kf1 = Keyframe.ofFloat(100f / durationInMill, offset);
        Keyframe kf2 = Keyframe.ofFloat(durationInMill / durationInMill, 0);
        PropertyValuesHolder phv = PropertyValuesHolder.ofKeyframe("translationY", kf0, kf1, kf2);
        ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(view, phv);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.setDuration(durationInMill);
        animator.start();
    }


    private void doFadeInAnimation(final View view, final long durationInMill){

        ObjectAnimator loadingRotateAnim = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);
        loadingRotateAnim.setInterpolator(new LinearInterpolator());
        loadingRotateAnim.setDuration(durationInMill);
        loadingRotateAnim.start();
    }

}
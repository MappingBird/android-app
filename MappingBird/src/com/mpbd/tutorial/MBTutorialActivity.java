package com.mpbd.tutorial;

import java.util.Stack;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mappingbird.widget.MappingBirdParallaxPager;
import com.mpbd.mappingbird.R;
import com.mpbd.mappingbird.util.MBUtil;
import com.viewpagerindicator.CirclePageIndicator;
import com.viewpagerindicator.PageIndicator;

public class MBTutorialActivity extends FragmentActivity{

    MappingBirdParallaxPager viewPager;
    int mScreenHeight ;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.mb_activity_layout_tutorial);
		
		viewPager = (MappingBirdParallaxPager) findViewById(R.id.tutoral_pager);
		mScreenHeight = MBUtil.getScreenHeight(MBTutorialActivity.this);
		
		MBViewPagerAdapter viewPagerAdapter = new MBViewPagerAdapter(MBTutorialActivity.this);
		viewPager.setAdapter(viewPagerAdapter);
		
		PageIndicator mIndicator = (CirclePageIndicator)findViewById(R.id.tutoral_pager_indicator);
		mIndicator.setViewPager(viewPager);
	}


	
	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
	}
	
	
	static class MBTutorPagerViewHolder {
	    
	    Context ctx;
	    View rootView;
	    
        // layout - welcome
	    LinearLayout layoutWelcome;
        TextView tvSignIn;

        // layout 2
        RelativeLayout layoutIntroPage;
        ImageView introImage;
        TextView introText;

        // layout 3 - create account
        LinearLayout layoutCreatOrDoItLater;
        TextView tvDescription;
        TextView tvSignIn2;
        TextView tvLearnMore;
	    
	    public MBTutorPagerViewHolder(View view, Context context) {
            rootView = view;
            ctx = context;

            int screenHeight = MBUtil.getScreenHeight(context);
            
            // layout - welcome
            layoutWelcome = (LinearLayout) view.findViewById(R.id.tutoral_welcome);
            tvSignIn = (TextView) view.findViewById(R.id.tutoral_sign_in);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            params.gravity = Gravity.CENTER;
            params.height = (screenHeight / 100) * 66;
            layoutWelcome.setLayoutParams(params);
            
            // layout 2            
            layoutIntroPage = (RelativeLayout) view.findViewById(R.id.layout_tutoral_page);
            introText = (TextView) view.findViewById(R.id.page_intro_title);
            introImage = (ImageView) view.findViewById(R.id.page_intro_image);
            
            params = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            params.gravity = Gravity.CENTER;
            params.height = (screenHeight / 100) * 68;
            layoutIntroPage.setLayoutParams(params);
            
            // layout 3 - create account
            layoutCreatOrDoItLater = (LinearLayout) view.findViewById(R.id.tutoral_create_or_do_later);
            tvDescription = (TextView) view.findViewById(R.id.tutoral_create_descripion);
            tvSignIn2 = (TextView) view.findViewById(R.id.tutoral_sign_in_2);
            tvLearnMore = (TextView) view.findViewById(R.id.tutoral_learn_more);
            params = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            params.gravity = Gravity.CENTER;
            params.height = (screenHeight / 100) * 73;
            layoutCreatOrDoItLater.setLayoutParams(params);
	    }
	}
	
	
	public class MBViewPagerAdapter extends PagerAdapter {
	    
	    private final String TAG = MBViewPagerAdapter.class.getSimpleName();
	    Context context;
	    
	    int targetLayoutId = R.layout.mb_tutorial_page_layout; 
	    Stack<View> recycledViewsList = new Stack<View>();
	    
        private MBTutorPagerViewHolder inflateOrRecycleView() {

            View rootView;
            MBTutorPagerViewHolder vh;
            if(recycledViewsList.isEmpty()) {
                rootView = LayoutInflater.from(MBTutorialActivity.this).inflate(targetLayoutId, null, false);

                Log.i(TAG, "create a new view : " + rootView.hashCode());
            }
            else {
                rootView = recycledViewsList.pop();

                    Log.i(TAG, "Restored recycled view from cache " + rootView.hashCode());
            }

            vh = new MBTutorPagerViewHolder(rootView, context);
            rootView.setTag(vh);

            return vh;
        }	    
	    
	    
	    public MBViewPagerAdapter(Context ctx){
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
                
                
                vh.tvSignIn.setOnClickListener(new OnClickListener() {
                    
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent();
                        intent.setClass(MBTutorialActivity.this, com.mpbd.mappingbird.MappingBirdLoginActivity.class);
                        MBTutorialActivity.this.startActivity(intent);
                        
                    }
                });

            } else if (position == 4) {
                vh.layoutWelcome.setVisibility(View.GONE);
                vh.layoutIntroPage.setVisibility(View.GONE);
                vh.layoutCreatOrDoItLater.setVisibility(View.VISIBLE);
                
//                vh.tvDescription.setMovementMethod(LinkMovementMethod.getInstance());
//                vh.tvDescription.setText(Html
//                        .fromHtml(getString(R.string.tutorial_sign_up_descriptions)));
                
                vh.tvSignIn2.setOnClickListener(new OnClickListener() {
                    
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent();
                        intent.setClass(MBTutorialActivity.this, com.mpbd.mappingbird.MappingBirdLoginActivity.class);
                        MBTutorialActivity.this.startActivity(intent);                        
                    }
                });
                vh.tvLearnMore.setOnClickListener(new OnClickListener() {
                    
                    @Override
                    public void onClick(View v) {
                        String url = "http://www.mappingbird.com";
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse(url));
                        startActivity(i);
                    }
                });
                
            } else {
                vh.layoutWelcome.setVisibility(View.GONE);
                vh.layoutIntroPage.setVisibility(View.VISIBLE);
                vh.layoutCreatOrDoItLater.setVisibility(View.GONE);
                
                if(position == 1){
                    vh.introText.setText(R.string.tutorial_page1_scenario_intro);
                    vh.introImage.setImageResource(R.drawable.oobe_splash_001);
                }
                else if(position == 2){
                    vh.introText.setText(R.string.tutorial_page2_scenario_intro);
                    vh.introImage.setImageResource(R.drawable.oobe_splash_002);                    
                }
                else if(position == 3){
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
}
package com.mpbd.tutorial;

import java.util.Stack;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mappingbird.widget.MappingBirdParallaxPager;
import com.mpbd.mappingbird.R;
import com.viewpagerindicator.CirclePageIndicator;
import com.viewpagerindicator.PageIndicator;

public class MBTutorialActivity extends FragmentActivity{

    MappingBirdParallaxPager viewPager;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.mb_activity_layout_tutorial);
		
		viewPager = (MappingBirdParallaxPager) findViewById(R.id.tutoral_pager);
		
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
	    RelativeLayout layoutWelcome;
	    TextView tvSignIn;
	    
	    
	 // layout 2
	    LinearLayout layoutIntroPage;
	    ImageView introImage;
	    TextView introText;
	    TextView introText2;
	    
	 // layout 3
	    LinearLayout layoutCreatOrDoItLater;
	    TextView tvDescription;
	    TextView tvCreateAccount;
	    TextView tvDoLater;
	    
	    public MBTutorPagerViewHolder(View view, Context context) {
	           rootView = view;
	           ctx = context;
	           
	           layoutWelcome = (RelativeLayout) view.findViewById(R.id.tutoral_welcome);
	           tvSignIn = (TextView) view.findViewById(R.id.tutoral_sign_in);
	           
	           layoutIntroPage = (LinearLayout) view.findViewById(R.id.tutoral_page);
	           introText = (TextView) view.findViewById(R.id.page_intro_title);
	           introText2 = (TextView) view.findViewById(R.id.page_intro_title2);
	           introImage = (ImageView) view.findViewById(R.id.page_intro_image);
	           
	           layoutCreatOrDoItLater = (LinearLayout) view.findViewById(R.id.tutoral_create_or_do_later);
	           tvDescription = (TextView) view.findViewById(R.id.tutoral_create_descripion);
	           tvCreateAccount = (TextView) view.findViewById(R.id.tutoral_create_account);
	           tvDoLater = (TextView) view.findViewById(R.id.tutoral_do_it_later);
	           
	           
	            
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
                
                vh.tvDescription.setMovementMethod(LinkMovementMethod.getInstance());
                vh.tvDescription.setText(Html
                        .fromHtml(getString(R.string.tutorial_sign_up_descriptions)));
                
                vh.tvCreateAccount.setOnClickListener(new OnClickListener() {
                    
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent();
                        intent.setClass(MBTutorialActivity.this, com.mpbd.mappingbird.MappingBirdLoginActivity.class);
                        MBTutorialActivity.this.startActivity(intent);                        
                    }
                });
                vh.tvDoLater.setOnClickListener(new OnClickListener() {
                    
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent();
                        intent.setClass(MBTutorialActivity.this, com.mpbd.mappingbird.MappingBirdLoginActivity.class);
                        MBTutorialActivity.this.startActivity(intent);
                    }
                });
                
            } else {
                vh.layoutWelcome.setVisibility(View.GONE);
                vh.layoutIntroPage.setVisibility(View.VISIBLE);
                vh.layoutCreatOrDoItLater.setVisibility(View.GONE);
                
                if(position == 1){
                    vh.introText.setText(R.string.tutorial_page1_scenario_intro);
                    vh.introText2.setVisibility(View.GONE);
                    
                    //vh.introImage.setImageResource(resId);
                }
                else if(position == 2){
                    vh.introText.setText(R.string.tutorial_page2_scenario_intro_1);
                    vh.introText2.setVisibility(View.VISIBLE);
                    vh.introText2.setText(R.string.tutorial_page2_scenario_intro_2);

                    //vh.introImage.setImageResource(resId);                    
                }
                else if(position == 3){
                    vh.introText.setText(R.string.tutorial_page3_scenario_intro);
                    vh.introText2.setVisibility(View.GONE);

                    //vh.introImage.setImageResource(resId);                    
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
package com.mappingbird.saveplace;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.RelativeLayout;

import com.mappingbird.common.MappingBirdApplication;
import com.mpbd.mappingbird.R;
import com.mpbd.mappingbird.util.MBUtil;
import com.mpbd.mappingbird.util.Utils;

public class MBListLayoutAddPlaceLayout extends RelativeLayout {

	// Current Location state
	private static final int STATE_CL_DOWN = 0;
	private static final int STATE_CL_DOWN_ANIM = 1;
	private static final int STATE_CL_UP = 2;
	private static final int STATE_CL_UP_ANIM = 3;
	private int mCLState = STATE_CL_DOWN;
	// Title bar
	private int mTitleBarHeight = 0;
	// Current
	private int mCurrentInputFieldHeight = 0;
	private int mCurrentLocationLayoutHeight = 0;
	private View mCurrentLocationLayout;
	
	private View mInputLayout;
	
	private ValueAnimator mAnimator = null;
	public MBListLayoutAddPlaceLayout(Context context) {
		super(context);
	}

	public MBListLayoutAddPlaceLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public MBListLayoutAddPlaceLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		initLayout();
	}
	
	private void initLayout() {
		mCurrentLocationLayout = findViewById(R.id.pick_place_current_location_layout);
		
		mInputLayout = findViewById(R.id.pick_place_add_location_input_layout);
		mInputLayout.setOnClickListener(mInputClickListener);

		mTitleBarHeight = (int) getResources().getDimension(R.dimen.title_bar_height);

		mCurrentInputFieldHeight = (int) getResources().getDimension(R.dimen.pick_place_add_location_height);
		mCurrentLocationLayoutHeight = (int) getResources().getDimension(R.dimen.pick_place_add_location_layout_height);
	}

	private View.OnClickListener mInputClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			switch(v.getId()) {
			case R.id.pick_place_add_location_input_layout:
				if(mAnimator == null) {
					if(mCLState == STATE_CL_DOWN) {
						mAnimator = ValueAnimator.ofFloat(mCurrentLocationLayout.getY(), 
								getHeight() - mCurrentLocationLayoutHeight);
						mCLState = STATE_CL_UP_ANIM;
					} else {
						mAnimator = ValueAnimator.ofFloat(mCurrentLocationLayout.getY(), 
								getHeight() - mCurrentInputFieldHeight);
						mCLState = STATE_CL_DOWN_ANIM;						
					}
					mAnimator.setDuration(400);
					mAnimator.setInterpolator(new DecelerateInterpolator());
					mAnimator.addUpdateListener(mUpUpdateListener);
					mAnimator.addListener(mAnimatorListener);
					mAnimator.start();
				}
				break;
			}
		}
	};

	private ValueAnimator.AnimatorUpdateListener mUpUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
		@Override
		public void onAnimationUpdate(ValueAnimator animation) {
			float value = (Float)animation.getAnimatedValue();
			mCurrentLocationLayout.setY(value);
		}
	};

	private Animator.AnimatorListener mAnimatorListener = new Animator.AnimatorListener() {
		
		@Override
		public void onAnimationStart(Animator animation) {
		}
		
		@Override
		public void onAnimationRepeat(Animator animation) {
		}
		
		@Override
		public void onAnimationEnd(Animator animation) {
			mAnimator = null;
			if(mCLState == STATE_CL_DOWN_ANIM)
				mCLState = STATE_CL_DOWN;
			else if(mCLState == STATE_CL_UP_ANIM)
				mCLState = STATE_CL_UP;
		}
		
		@Override
		public void onAnimationCancel(Animator animation) {
		}
	}; 

	public void showCurrectLocationLayout() {
		if(mCurrentLocationLayout.getVisibility() != View.VISIBLE) {
			
			mCurrentLocationLayout.setVisibility(View.VISIBLE);
			mCurrentLocationLayout.setY(getHeight() - mCurrentInputFieldHeight);
		}
	}
}
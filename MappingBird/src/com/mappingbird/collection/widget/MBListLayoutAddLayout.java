package com.mappingbird.collection.widget;

import java.util.ArrayList;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hlrt.common.DeBug;
import com.mappingbird.collection.widget.MBProgressCircleLayout.ProgressListener;
import com.mappingbird.common.MappingBirdApplication;
import com.mappingbird.saveplace.MappingBirdPickPlaceActivity;
import com.mappingbird.saveplace.services.MBPlaceSubmitTask;
import com.mappingbird.saveplace.services.MBPlaceSubmitUtil;
import com.mpbd.mappingbird.R;
import com.mpbd.mappingbird.util.MBUtil;

public class MBListLayoutAddLayout extends RelativeLayout {
	
	private static final int MODE_CLOSE = 0;
	private static final int MODE_OPEN = 1;
	private static final int MODE_CLOSE_ANIM = 2;
	private static final int MODE_OPEN_ANIM = 3;
	private static final int MODE_PROGRESSING = 4;
	private static final int MODE_ERROR = 5;

	private int mMode = MODE_CLOSE;
	
	// Animation space
	private float mAnimSpace = 0.5f;
	private static final int ANIM_DURATION = 500;
	private static final int ANIM_MOVE_DURATION = 300;
	
	// 有沒有卡片
	private boolean mHasCard = false;
	// Add
	private MBProgressCircleLayout mAddItemLayout;
	private MBProgressCircleLayout mCurrentItemLayout;
	private TextView mAddItemText;
	private int mAddItemWidth = 0;
	private int mAddItemOpenPositionY = 0;
	private int mAddItemClosePositionY = 0;
	private int mAddItemPaddingTop = 0;
	private int mAddItemPaddingRight = 0;
	
	private int mSelectItemCenterClosePositionX = 0;
	private int mSelectItemCenterClosePositionY = 0;
	private int mSelectItemCenterOpenPositionX = 0;
	private int mSelectItemCenterOpenPositionY = 0;
	
	private int mCurrentPositionPositionX = 0;
	private int mCurrentPositionPositionY = 0;
	
	// select_scene
	private View mSelectScene;
	private int mSelectScenePositionX = 0;
	private int mSelectScenePositionY = 0;
	// select_restaurant
	private View mSelectRestaurant;
	private int mSelectRestaurantPositionX = 0;
	private int mSelectRestaurantPositionY = 0;
	// select_hotel
	private View mSelectHotel;
	private int mSelectHotelPositionX = 0;
	private int mSelectHotelPositionY = 0;
	// select_default
	private View mSelectDefault;
	private int mSelectDefaultPositionX = 0;
	private int mSelectDefaultPositionY = 0;
	// select_mall
	private View mSelectMall;
	private int mSelectMallPositionX = 0;
	private int mSelectMallPositionY = 0;
	// select_bar
	private View mSelectBar;
	private int mSelectBarPositionX = 0;
	private int mSelectBarPositionY = 0;
	
	private ArrayList<View> 	mItemViewList = new ArrayList<View>();
	private ArrayList<Integer> 	mItemViewXList = new ArrayList<Integer>();
	private ArrayList<Integer> 	mItemViewYList = new ArrayList<Integer>();
	
	private OnSelectKindLayoutListener mOnSelectKindLayoutListener = null;
	
	private ArrayList<TextView> mHintTextArrays = new ArrayList<TextView>();
	
	private int mLayoutWidth = 0;
	// 滑出滑入動畫需要知道行徑的距離
	private float mSlideAnimationDistance = 0;
	public MBListLayoutAddLayout(Context context) {
		super(context);
	}

	public MBListLayoutAddLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public MBListLayoutAddLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		
		mAddItemLayout = (MBProgressCircleLayout)findViewById(R.id.item_add_layout);
		mAddItemText = (TextView)findViewById(R.id.item_add);
		
		mCurrentItemLayout = (MBProgressCircleLayout)findViewById(R.id.item_current_position_layout);
		mCurrentItemLayout.setOnClickListener(mItemClickListener);
		
		mSelectScene 		= findViewById(R.id.select_scene);
		mSelectRestaurant 	= findViewById(R.id.select_restaurant);
		mSelectHotel 		= findViewById(R.id.select_hotel);
		mSelectDefault 		= findViewById(R.id.select_default);
		mSelectMall 		= findViewById(R.id.select_mall);
		mSelectBar 			= findViewById(R.id.select_bar);
		
		// setClick
		mAddItemLayout.setOnClickListener(mItemClickListener);
		mSelectScene.setOnClickListener(mItemClickListener);
		mSelectRestaurant.setOnClickListener(mItemClickListener);
		mSelectHotel.setOnClickListener(mItemClickListener);
		mSelectDefault.setOnClickListener(mItemClickListener);
		mSelectMall.setOnClickListener(mItemClickListener);
		mSelectBar.setOnClickListener(mItemClickListener);
//		initLayout();
	}
	
	public void init(boolean hasCard) {
		mHasCard = hasCard; 
		initLayout();
	}
	
	private void initLayout() {
		// Add position
		mLayoutWidth = MBUtil.getWindowWidth(getContext());
		int windowHeight = MBUtil.getWindowHeight(getContext());
		int titleBarHeight = (int)getContext().getResources().getDimension(R.dimen.title_bar_height);
		int add_margin_bottom = (int) getContext().getResources().getDimension(R.dimen.col_add_place_margin_bottom);
		int add_margin_bottom_open = (int) getResources().getDimension(R.dimen.col_add_place_margin_bottom_end);
		int add_margin_bottom_nocard = (int) getResources().getDimension(R.dimen.col_add_place_no_card_margin_bottom);
		int btn_btw_space = (int) getResources().getDimension(R.dimen.col_add_place_btn_bwt_space);
		
		mAddItemClosePositionY = mHasCard ? windowHeight - titleBarHeight - add_margin_bottom
				:  windowHeight - titleBarHeight - add_margin_bottom_nocard;
		mAddItemOpenPositionY = windowHeight - titleBarHeight - add_margin_bottom_open;
		setMarginInView(mAddItemLayout, 0, mAddItemClosePositionY, 0, 0);

		mCurrentPositionPositionX = mAddItemOpenPositionY;
		mCurrentPositionPositionY = mAddItemClosePositionY - btn_btw_space;
		setMarginInView(mCurrentItemLayout, 0, mCurrentPositionPositionY, 0, 0);
		
		mAddItemPaddingTop = (int) getContext().getResources().getDimension(R.dimen.col_add_place_padding_top);
		mAddItemPaddingRight = (int) getContext().getResources().getDimension(R.dimen.col_add_place_padding_right);

		int selectItemWidth = (int)getResources().getDimension(R.dimen.col_select_place_item_width);
						
		mSelectItemCenterOpenPositionX = mLayoutWidth - mAddItemPaddingRight - selectItemWidth;
		mSelectItemCenterOpenPositionY = mAddItemOpenPositionY + mAddItemPaddingTop;
		
		mSelectItemCenterClosePositionX = mSelectItemCenterOpenPositionX;
		mSelectItemCenterClosePositionY = mAddItemClosePositionY + mAddItemPaddingTop;
		
		int radius = (int) getResources().getDimension(R.dimen.col_select_place_item_radius);
		double angle = 0;
		mItemViewList.clear();
		mItemViewXList.clear();
		mItemViewYList.clear();
		//
		setMarginInView(mSelectScene, 0, 0, mAddItemPaddingRight, 0);
		mSelectScenePositionX = mSelectItemCenterOpenPositionX;
		mSelectScenePositionY = mSelectItemCenterOpenPositionY - radius;
		mSelectScene.setY(mSelectItemCenterClosePositionY);
		mItemViewList.add(mSelectScene);
		mItemViewXList.add(mSelectScenePositionX);
		mItemViewYList.add(mSelectScenePositionY);

		//
		angle = 54 * Math.PI / 180;
		setMarginInView(mSelectRestaurant, 0, 0, mAddItemPaddingRight, 0);
		mSelectRestaurantPositionX = mSelectItemCenterOpenPositionX + (int)(- radius * Math.cos(angle));
		mSelectRestaurantPositionY = mSelectItemCenterOpenPositionY - (int)(radius * Math.sin(angle));
		mSelectRestaurant.setY(mSelectItemCenterClosePositionY);
		mItemViewList.add(mSelectRestaurant);
		mItemViewXList.add(mSelectRestaurantPositionX);
		mItemViewYList.add(mSelectRestaurantPositionY);

		//
		angle = 18 * Math.PI / 180;
		setMarginInView(mSelectHotel, 0, 0, mAddItemPaddingRight, 0);
		mSelectHotelPositionX = mSelectItemCenterOpenPositionX + (int)(- radius * Math.cos(angle));
		mSelectHotelPositionY = mSelectItemCenterOpenPositionY - (int)(radius * Math.sin(angle));
		mSelectHotel.setY(mSelectItemCenterClosePositionY);
		mItemViewList.add(mSelectHotel);
		mItemViewXList.add(mSelectHotelPositionX);
		mItemViewYList.add(mSelectHotelPositionY);

		//
		angle = 18 * Math.PI / 180;
		setMarginInView(mSelectDefault, 0, 0, mAddItemPaddingRight, 0);
		mSelectDefaultPositionX = mSelectItemCenterOpenPositionX + (int)(- radius * Math.cos(angle));
		mSelectDefaultPositionY = mSelectItemCenterOpenPositionY + (int)(radius * Math.sin(angle));
		mSelectDefault.setY(mSelectItemCenterClosePositionY);
		mItemViewList.add(mSelectDefault);
		mItemViewXList.add(mSelectDefaultPositionX);
		mItemViewYList.add(mSelectDefaultPositionY);

		//
		setMarginInView(mSelectMall, 0, 0, mAddItemPaddingRight, 0);
		angle = 54 * Math.PI / 180;
		mSelectMallPositionX = mSelectItemCenterOpenPositionX + (int)(- radius * Math.cos(angle));
		mSelectMallPositionY = mSelectItemCenterOpenPositionY + (int)(radius * Math.sin(angle));
		mSelectMall.setY(mSelectItemCenterClosePositionY);
		mItemViewList.add(mSelectMall);
		mItemViewXList.add(mSelectMallPositionX);
		mItemViewYList.add(mSelectMallPositionY);

		//
		setMarginInView(mSelectBar, 0, 0, mAddItemPaddingRight, 0);
		mSelectBarPositionX = mSelectItemCenterOpenPositionX;
		mSelectBarPositionY = mSelectItemCenterOpenPositionY + radius;
		mSelectBar.setY(mSelectItemCenterClosePositionY);
		mItemViewList.add(mSelectBar);
		mItemViewXList.add(mSelectBarPositionX);
		mItemViewYList.add(mSelectBarPositionY);
		
		// Hint Text
		TextView textView;
		RelativeLayout.LayoutParams params;
		mHintTextArrays.clear();
		textView = (TextView)findViewById(R.id.item_text_scene);
		params = (RelativeLayout.LayoutParams)textView.getLayoutParams();
//		params.setMargins(0, 0, right, bottom);
	}
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		if(mAddItemWidth == 0) {
			mAddItemWidth = mAddItemLayout.getWidth();
		}
	}

	private void setMarginInView(View view, int left, int top, int right, int bottom) {
		RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) view.getLayoutParams();
		lp.setMargins(left, top, right, bottom);
		view.setLayoutParams(lp);
	}

	private OnClickListener mItemClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// 上傳中按下add button
			if(mMode == MODE_PROGRESSING && v.getId() == R.id.item_add_layout) {
				mOnSelectKindLayoutListener.onCancelUpload();
				return;
			}
			if(mMode != MODE_CLOSE
					&& mMode != MODE_OPEN)
				return;
			
			if(v.getId() == R.id.item_add_layout) {
				startAnimation(mMode);
			} else if(v.getId() == R.id.item_current_position_layout) {
				mOnSelectKindLayoutListener.onCurrentPosition();
			} else {
				if(mMode != MODE_OPEN)
					return;
				String type = MappingBirdPickPlaceActivity.TYPE_DEFAULT;
				switch(v.getId()) {
					case R.id.select_scene:
						type = MappingBirdPickPlaceActivity.TYPE_SCENE;
						break;
					case R.id.select_restaurant:
						type = MappingBirdPickPlaceActivity.TYPE_BAR;
						break;
					case R.id.select_hotel:
						type = MappingBirdPickPlaceActivity.TYPE_HOTEL;
						break;
					case R.id.select_default:
						type = MappingBirdPickPlaceActivity.TYPE_RESTURANT;
						break;
					case R.id.select_mall:
						type = MappingBirdPickPlaceActivity.TYPE_MALL;
						break;
					case R.id.select_bar:
						type = MappingBirdPickPlaceActivity.TYPE_DEFAULT;
						break;				
				}
				if(mOnSelectKindLayoutListener  != null)
					mOnSelectKindLayoutListener.onSelectKind(type);
			}
		}
	};
	
	public void closeImm() {
		mMode = MODE_PROGRESSING;
		ValueAnimator animator = ValueAnimator.ofFloat(0, 1.0f);
		animator.setDuration(0);
		animator.addUpdateListener(mCloseUpdateListener);
		animator.start();
		if(mOnSelectKindLayoutListener != null)
			mOnSelectKindLayoutListener.closeSelect();
	}

	private void startAnimation(int mode) {
		if(mode == MODE_CLOSE) {
			mMode = MODE_OPEN_ANIM;
			ValueAnimator animator = ValueAnimator.ofFloat(0, 1.0f);
			animator.setDuration(ANIM_MOVE_DURATION);
			animator.addUpdateListener(mUpUpdateListener);
			animator.setInterpolator(new DecelerateInterpolator());
			animator.addListener(mTranUpAnimatorListener);
			animator.start();
			if(mOnSelectKindLayoutListener != null)
				mOnSelectKindLayoutListener.openSelect();

		} else {
			mMode = MODE_CLOSE_ANIM;
			ValueAnimator animator = ValueAnimator.ofFloat(0, 1.0f);
			animator.setDuration(ANIM_DURATION);
			animator.addUpdateListener(mCloseUpdateListener);
			animator.setInterpolator(new DecelerateInterpolator());
			animator.addListener(mCloseAnimatorListener);
			animator.start();
			if(mOnSelectKindLayoutListener != null)
				mOnSelectKindLayoutListener.closeSelect();
		}
		mAddItemText.setText(R.string.iconfont_addplace);
	}
	
	public boolean isOpenSelect() {
		return mMode == MODE_OPEN;
	}

	public void closeSelector() {
		if(mMode == MODE_OPEN) {
			startAnimation(mMode);
		}
	}

	private AnimatorUpdateListener mUpUpdateListener = new AnimatorUpdateListener() {
		@Override
		public void onAnimationUpdate(ValueAnimator animation) {
			float value = ((Float) (animation.getAnimatedValue()))
                    .floatValue();
			mAddItemLayout.setY(mAddItemClosePositionY + value * (mAddItemOpenPositionY - mAddItemClosePositionY));
			mCurrentItemLayout.setX(mLayoutWidth - (1 - value) * (mCurrentItemLayout.getWidth()));
			View targeView = null;
			for(int i = 0; i < mItemViewList.size(); i++) {
				targeView = mItemViewList.get(i);
				targeView.setY(mSelectItemCenterClosePositionY + 
						(mSelectItemCenterOpenPositionY - mSelectItemCenterClosePositionY) * value);
			}
		}
	};

	private AnimatorUpdateListener mOpenUpdateListener = new AnimatorUpdateListener() {
		@Override
		public void onAnimationUpdate(ValueAnimator animation) {
			float value = ((Float) (animation.getAnimatedValue()))
                    .floatValue();
			float rate = 0;
			if(value < 0.1)
				mAddItemText.setRotation(-45 * value * 10);
			else {
				mAddItemText.setRotation(-45);
			}

			float colorValue = value * 1.5f;
			if(colorValue > 1)
				colorValue = 1;
			int color = Color.argb((int)(0xB2*colorValue), 0, 0, 0);
			setBackgroundColor(color);
			float startValue = 0;
			View targeView = null;
			for(int i = 0; i < mItemViewList.size(); i++) {
				targeView = mItemViewList.get(i);
				if(value > startValue && value < startValue + mAnimSpace) {
					rate = (value - startValue)/mAnimSpace;
					// y = (1 - b)x^2 + b*x , b = (e - d^2)/d(1-d)  (d,e) = (0.75, 1.2) b = 3.4
					// y = (1 - b)x^2 + b*x , b = (e - d^2)/d(1-d)  (d,e) = (0.75, 1.05) b = 2.6
					float rateN = -1.6f*rate*rate +2.6f * rate;
					targeView.setX(mSelectItemCenterOpenPositionX
							+ (mItemViewXList.get(i)- mSelectItemCenterOpenPositionX) * rateN);
					targeView.setY(mSelectItemCenterOpenPositionY
							+ (mItemViewYList.get(i) - mSelectItemCenterOpenPositionY) * rateN);
					targeView.setRotation(-180*(1 - rate));
				} else if(value >= startValue + mAnimSpace) {
					targeView.setX(mItemViewXList.get(i));
					targeView.setY(mItemViewYList.get(i));
					targeView.setRotation(0);
				}
				startValue += 0.08f;
			}
		}
	};
	
	private AnimatorUpdateListener mDownUpdateListener = new AnimatorUpdateListener() {
		@Override
		public void onAnimationUpdate(ValueAnimator animation) {
			float value = ((Float) (animation.getAnimatedValue()))
                    .floatValue();
			mAddItemLayout.setY(mAddItemOpenPositionY - value * (mAddItemOpenPositionY - mAddItemClosePositionY));
			mCurrentItemLayout.setX(mLayoutWidth - value * (mCurrentItemLayout.getWidth()));
			View targeView = null;
			for(int i = 0; i < mItemViewList.size(); i++) {
				targeView = mItemViewList.get(i);
				targeView.setY(mSelectItemCenterOpenPositionY - 
						(mSelectItemCenterOpenPositionY - mSelectItemCenterClosePositionY) * value);
			}
		}
	};

	private AnimatorUpdateListener mCloseUpdateListener = new AnimatorUpdateListener() {
		@Override
		public void onAnimationUpdate(ValueAnimator animation) {
			float value = ((Float) (animation.getAnimatedValue()))
                    .floatValue();
			float rate = 0;
			if(value < 0.1)
				mAddItemText.setRotation(-45 * (0.1f - value) * 10);
			else {
				mAddItemText.setRotation(0);
			}

			float colorValue = value * 1.5f;
			if(colorValue > 1)
				colorValue = 1;
			int color = Color.argb((int)(0xB2*(1 - value)), 0, 0, 0);
			setBackgroundColor(color);
			float startValue = 0;
			View targeView = null;
			for(int i = mItemViewList.size() - 1; i >= 0; i--) {
				targeView = mItemViewList.get(i);
				if(value > startValue && value < startValue + mAnimSpace) {
					rate = (value - startValue)/mAnimSpace;
					targeView.setX(mItemViewXList.get(i)
							- (mItemViewXList.get(i)- mSelectItemCenterOpenPositionX) * rate);
					targeView.setY(mItemViewYList.get(i)
							- (mItemViewYList.get(i) - mSelectItemCenterOpenPositionY) * rate);
					targeView.setRotation(-180*(rate));
				} else if(value >= startValue + mAnimSpace) {
					targeView.setX(mSelectItemCenterOpenPositionY);
					targeView.setY(mSelectItemCenterOpenPositionY);
					targeView.setRotation(0);
				}
				startValue += 0.08f;
			}

		}
	};

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if(mMode != MODE_CLOSE)
			return true;

		return super.onTouchEvent(event);
	}

	private AnimatorListener mTranUpAnimatorListener = new AnimatorListener() {
		
		@Override
		public void onAnimationStart(Animator animation) {
		}
		
		@Override
		public void onAnimationRepeat(Animator animation) {
		}
		
		@Override
		public void onAnimationEnd(Animator animation) {
			ValueAnimator animator = ValueAnimator.ofFloat(0, 1.0f);
			animator.setDuration(ANIM_DURATION);
			animator.addUpdateListener(mOpenUpdateListener);
			animator.setInterpolator(new DecelerateInterpolator());
			animator.addListener(mAnimatorListener);
			animator.start();
		}
		
		@Override
		public void onAnimationCancel(Animator animation) {
		}
	};
	
	private AnimatorListener mTranDownAnimatorListener = new AnimatorListener() {
		
		@Override
		public void onAnimationStart(Animator animation) {
		}
		
		@Override
		public void onAnimationRepeat(Animator animation) {
		}
		
		@Override
		public void onAnimationEnd(Animator animation) {
			if(mMode == MODE_CLOSE_ANIM) {
				mMode = MODE_CLOSE;
			}
			if(mMode == MODE_OPEN_ANIM) {
				mMode = MODE_OPEN;
			}
		}
		
		@Override
		public void onAnimationCancel(Animator animation) {
		}
	};

	private AnimatorListener mCloseAnimatorListener = new AnimatorListener() {
		
		@Override
		public void onAnimationStart(Animator animation) {
		}
		
		@Override
		public void onAnimationRepeat(Animator animation) {
		}
		
		@Override
		public void onAnimationEnd(Animator animation) {
			ValueAnimator animator = ValueAnimator.ofFloat(0, 1.0f);
			animator.setDuration(ANIM_MOVE_DURATION);
			animator.addUpdateListener(mDownUpdateListener);
			animator.setInterpolator(new DecelerateInterpolator());
			animator.addListener(mTranDownAnimatorListener);
			animator.start();
		}
		
		@Override
		public void onAnimationCancel(Animator animation) {
		}
	};

	private AnimatorListener mAnimatorListener = new AnimatorListener() {
		
		@Override
		public void onAnimationStart(Animator animation) {
		}
		
		@Override
		public void onAnimationRepeat(Animator animation) {
		}
		
		@Override
		public void onAnimationEnd(Animator animation) {
			if(mMode == MODE_CLOSE_ANIM) {
				mMode = MODE_CLOSE;
			}
			if(mMode == MODE_OPEN_ANIM) {
				mMode = MODE_OPEN;
			}
		}
		
		@Override
		public void onAnimationCancel(Animator animation) {
		}
	};
	
	public void setOnSelectKindLayoutListener(OnSelectKindLayoutListener listener) {
		mOnSelectKindLayoutListener = listener;
	}
	
	public interface OnSelectKindLayoutListener {
		public void onSelectKind(String type);
		public void openSelect();
		public void closeSelect();
		public void onProgressFinished();
		public void onCancelUpload();
		public void onCurrentPosition();
	}
	
	// Progress ---------------------------------
	public void setProgress(int state, int progress, int total) {
		if(DeBug.DEBUG)
			DeBug.d(MBPlaceSubmitUtil.ADD_TAG, "[MBListLayoutAddLayout] setProgress,state = "+state+
					", progress = "+progress+", total = "+total+", mMode = "+mMode);

		if(mMode != MODE_PROGRESSING) {
			closeImm();
		}
		
		if(state == MBPlaceSubmitTask.MSG_ADD_PLACE_FINISHED) {
			mMode = MODE_PROGRESSING;
			mAddItemLayout.setMode(MBProgressCircleLayout.MODE_PROGRESS);
			mAddItemLayout.setProgressListener(mProgressListener);
			mAddItemLayout.startProcress(progress, total);
			post(new Runnable() {
				@Override
				public void run() {
					mAddItemText.setText(R.string.iconfont_cloud_upload);
					mAddItemText.setTextColor(
							MappingBirdApplication.instance().getResources().getColor(R.color.graphic_blue));
					mAddItemLayout.setBackgroundResource(R.drawable.btn_white_circle);
				}
			});			
		} else if(state == MBPlaceSubmitTask.MSG_ADD_PLACE_FAILED ||
				state == MBPlaceSubmitTask.MSG_ADD_PLACE_IMAGE_UPLOAD_FAILED) {
			// 上傳失敗
			// 1. 清掉Progress狀態
			mAddItemLayout.cleanProgress();
			mAddItemLayout.setMode(MBProgressCircleLayout.MODE_NORMAL);
			// 2. 換圖
			post(new Runnable() {
				@Override
				public void run() {
					mAddItemText.setText(R.string.iconfont_refresh);
					mAddItemText.setTextColor(
							MappingBirdApplication.instance().getResources().getColor(R.color.white));
					mAddItemLayout.setBackgroundResource(R.drawable.btn_bule_circle);
				}
			});			
		} else {
			// 上傳中
			mMode = MODE_PROGRESSING;
			mAddItemLayout.setMode(MBProgressCircleLayout.MODE_PROGRESS);
			mAddItemLayout.setProgressListener(mProgressListener);
			mAddItemLayout.startProcress(progress, total);
			post(new Runnable() {
				@Override
				public void run() {
					mAddItemText.setText(R.string.iconfont_cloud_upload);
					mAddItemText.setTextColor(
							MappingBirdApplication.instance().getResources().getColor(R.color.graphic_blue));
					mAddItemLayout.setBackgroundResource(R.drawable.btn_white_circle);
				}
			});
		}
	}
	
	private ProgressListener mProgressListener = new ProgressListener() {
		@Override
		public void progressFinished() {
			mMode = MODE_CLOSE;
			if(mOnSelectKindLayoutListener != null)
				mOnSelectKindLayoutListener.onProgressFinished();
			post(new Runnable() {
				@Override
				public void run() {
					resetState();
				}
			});
		}
	};
	
	public void resetState() {
		mAddItemText.setText(R.string.iconfont_addplace);
		mAddItemText.setTextColor(
				MappingBirdApplication.instance().getResources().getColor(R.color.white));
		mAddItemLayout.setBackgroundResource(R.drawable.btn_bule_circle);
	}
	// 移出動畫
	public void setTranlatorX(float x) {
		DeBug.d("setTranlatorX = "+x);
		float ratio = x/mSlideAnimationDistance;
		setX((mAddItemWidth*ratio)*2); 
	}
	
	public void setSlidOutDistance(float dis) {
		mSlideAnimationDistance = dis;
	}
}
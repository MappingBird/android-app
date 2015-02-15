package com.mappingbird.collection.widget;

import java.util.ArrayList;

import android.animation.Animator.AnimatorListener;
import android.animation.Animator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import com.mappingbird.common.DeBug;
import com.mappingbird.saveplace.MappingBirdPickPlaceActivity;
import com.mpbd.mappingbird.R;
import com.mpbd.mappingbird.util.MBUtil;

public class MBListLayoutAddLayout extends RelativeLayout {
	
	private static final int MODE_CLOSE = 0;
	private static final int MODE_OPEN = 1;
	private static final int MODE_CLOSE_ANIM = 2;
	private static final int MODE_OPEN_ANIM = 3;
	
	private int mMode = MODE_CLOSE;
	
	// Animation space
	private float mAnimSpace = 0.3f;
	private int mAnimDuration = 900;
	
	// Add
	private View mAddItem;
	private int mAddItemPositionY = 0;
	private int mAddItemPaddingTop = 0;
	private int mAddItemPaddingRight = 0;
	
	private int mSelectItemCenterPositionX = 0;
	private int mSelectItemCenterPositionY = 0;
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
		mAddItem = findViewById(R.id.item_add);
		
		mSelectScene 		= findViewById(R.id.select_scene);
		mSelectRestaurant 	= findViewById(R.id.select_restaurant);
		mSelectHotel 		= findViewById(R.id.select_hotel);
		mSelectDefault 		= findViewById(R.id.select_default);
		mSelectMall 		= findViewById(R.id.select_mall);
		mSelectBar 			= findViewById(R.id.select_bar);
		
		// setClick
		mAddItem.setOnClickListener(mItemClickListener);
		mSelectScene.setOnClickListener(mItemClickListener);
		mSelectRestaurant.setOnClickListener(mItemClickListener);
		mSelectHotel.setOnClickListener(mItemClickListener);
		mSelectDefault.setOnClickListener(mItemClickListener);
		mSelectMall.setOnClickListener(mItemClickListener);
		mSelectBar.setOnClickListener(mItemClickListener);
		initLayout();
	}
	
	private void initLayout() {
		// Add position
		int windowWidth = MBUtil.getWindowWidth(getContext());
		int windowHeight = MBUtil.getWindowHeight(getContext());
		int titleBarHeight = (int)getContext().getResources().getDimension(R.dimen.title_bar_height);
		int add_margin_bottom = (int) getContext().getResources().getDimension(R.dimen.col_add_place_margin_bottom);
		mAddItemPositionY = windowHeight - titleBarHeight - add_margin_bottom;
		setMarginInView(mAddItem, 0, mAddItemPositionY, 0, 0);
		
		mAddItemPaddingTop = (int) getContext().getResources().getDimension(R.dimen.col_add_place_padding_top);
		mAddItemPaddingRight = (int) getContext().getResources().getDimension(R.dimen.col_add_place_padding_right);

		int selectItemWidth = (int)getResources().getDimension(R.dimen.col_select_place_item_width);
		
		mSelectItemCenterPositionX = windowWidth - mAddItemPaddingRight - selectItemWidth;
		mSelectItemCenterPositionY = mAddItemPositionY + mAddItemPaddingTop;
		
		int radius = (int) getResources().getDimension(R.dimen.col_select_place_item_radius);
		double angle = 0;
		mItemViewList.clear();
		mItemViewXList.clear();
		mItemViewYList.clear();
		//
		setMarginInView(mSelectScene, 0, 0, mAddItemPaddingRight, 0);
		mSelectScenePositionX = mSelectItemCenterPositionX;
		mSelectScenePositionY = mSelectItemCenterPositionY - radius;
//		mSelectScene.setX(mSelectItemCenterPositionX);
		mSelectScene.setY(mSelectItemCenterPositionY);
		mItemViewList.add(mSelectScene);
		mItemViewXList.add(mSelectScenePositionX);
		mItemViewYList.add(mSelectScenePositionY);

		//
		angle = 54 * Math.PI / 180;
		setMarginInView(mSelectRestaurant, 0, 0, mAddItemPaddingRight, 0);
		mSelectRestaurantPositionX = mSelectItemCenterPositionX + (int)(- radius * Math.cos(angle));
		mSelectRestaurantPositionY = mSelectItemCenterPositionY - (int)(radius * Math.sin(angle));
//		mSelectRestaurant.setX(mSelectItemCenterPositionX);
		mSelectRestaurant.setY(mSelectItemCenterPositionY);
		mItemViewList.add(mSelectRestaurant);
		mItemViewXList.add(mSelectRestaurantPositionX);
		mItemViewYList.add(mSelectRestaurantPositionY);

		//
		angle = 18 * Math.PI / 180;
		setMarginInView(mSelectHotel, 0, 0, mAddItemPaddingRight, 0);
		mSelectHotelPositionX = mSelectItemCenterPositionX + (int)(- radius * Math.cos(angle));
		mSelectHotelPositionY = mSelectItemCenterPositionY - (int)(radius * Math.sin(angle));
//		mSelectHotel.setX(mSelectItemCenterPositionX);
		mSelectHotel.setY(mSelectItemCenterPositionY);
		mItemViewList.add(mSelectHotel);
		mItemViewXList.add(mSelectHotelPositionX);
		mItemViewYList.add(mSelectHotelPositionY);

		//
		angle = 18 * Math.PI / 180;
		setMarginInView(mSelectDefault, 0, 0, mAddItemPaddingRight, 0);
		mSelectDefaultPositionX = mSelectItemCenterPositionX + (int)(- radius * Math.cos(angle));
		mSelectDefaultPositionY = mSelectItemCenterPositionY + (int)(radius * Math.sin(angle));
//		mSelectDefault.setX(mSelectItemCenterPositionX);
		mSelectDefault.setY(mSelectItemCenterPositionY);
		mItemViewList.add(mSelectDefault);
		mItemViewXList.add(mSelectDefaultPositionX);
		mItemViewYList.add(mSelectDefaultPositionY);

		//
		setMarginInView(mSelectMall, 0, 0, mAddItemPaddingRight, 0);
		angle = 54 * Math.PI / 180;
		mSelectMallPositionX = mSelectItemCenterPositionX + (int)(- radius * Math.cos(angle));
		mSelectMallPositionY = mSelectItemCenterPositionY + (int)(radius * Math.sin(angle));
//		mSelectMall.setX(mSelectItemCenterPositionX);
		mSelectMall.setY(mSelectItemCenterPositionY);
		mItemViewList.add(mSelectMall);
		mItemViewXList.add(mSelectMallPositionX);
		mItemViewYList.add(mSelectMallPositionY);

		//
		setMarginInView(mSelectBar, 0, 0, mAddItemPaddingRight, 0);
		mSelectBarPositionX = mSelectItemCenterPositionX;
		mSelectBarPositionY = mSelectItemCenterPositionY + radius;
//		mSelectBar.setX(mSelectItemCenterPositionX);
		mSelectBar.setY(mSelectItemCenterPositionY);
		mItemViewList.add(mSelectBar);
		mItemViewXList.add(mSelectBarPositionX);
		mItemViewYList.add(mSelectBarPositionY);
	}
	
	private void setMarginInView(View view, int left, int top, int right, int bottom) {
		RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) view.getLayoutParams();
		lp.setMargins(left, top, right, bottom);
		view.setLayoutParams(lp);
	}

	private OnClickListener mItemClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			if(mMode != MODE_CLOSE
					&& mMode != MODE_OPEN)
				return;
			
			if(v.getId() == R.id.item_add) {
				startAnimation(mMode);
			} else {
				if(mMode != MODE_OPEN)
					return;
				int type = MappingBirdPickPlaceActivity.TYPE_DEFAULT;
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
//				Intent intent = new Intent(getContext(), MappingBirdPickPlaceActivity.class);
//				intent.putExtra(MappingBirdPickPlaceActivity.EXTRA_COLLECTION_LIST, mCollectionList);
//				intent.putExtra(MappingBirdPickPlaceActivity.EXTRA_TYPE, type);
//				intent.putExtra(MappingBirdPickPlaceActivity.EXTRA_LAT, mLatitude);
//				intent.putExtra(MappingBirdPickPlaceActivity.EXTRA_LONG, mLongitude);
//				getContext().startActivity(intent);
			}
		}
	};
	
	private void startAnimation(int mode) {
		if(mode == MODE_CLOSE) {
			mMode = MODE_OPEN_ANIM;
			ValueAnimator animator = ValueAnimator.ofFloat(0, 1.0f);
			animator.setDuration(mAnimDuration);
			animator.addUpdateListener(mOpenUpdateListener);
			animator.addListener(mAnimatorListener);
			animator.start();
			if(mOnSelectKindLayoutListener != null)
				mOnSelectKindLayoutListener.openSelect();
		} else {
			mMode = MODE_CLOSE_ANIM;
			ValueAnimator animator = ValueAnimator.ofFloat(0, 1.0f);
			animator.setDuration(mAnimDuration);
			animator.addUpdateListener(mCloseUpdateListener);
			animator.addListener(mAnimatorListener);
			animator.start();
			if(mOnSelectKindLayoutListener != null)
				mOnSelectKindLayoutListener.closeSelect();
		}
	}
	
	private AnimatorUpdateListener mOpenUpdateListener = new AnimatorUpdateListener() {
		@Override
		public void onAnimationUpdate(ValueAnimator animation) {
			float value = ((Float) (animation.getAnimatedValue()))
                    .floatValue();
			float rate = 0;
			if(value < 0.1)
				mAddItem.setRotation(-45 * value * 10);
			else {
				mAddItem.setRotation(-45);
			}

			float startValue = 0;
			View targeView = null;
			for(int i = 0; i < mItemViewList.size(); i++) {
				targeView = mItemViewList.get(i);
				if(value > startValue && value < startValue + mAnimSpace) {
					rate = (value - startValue)/mAnimSpace;
					targeView.setX(mSelectItemCenterPositionX
							+ (mItemViewXList.get(i)- mSelectItemCenterPositionX) * rate);
					targeView.setY(mSelectItemCenterPositionY
							+ (mItemViewYList.get(i) - mSelectItemCenterPositionY) * rate);
					targeView.setRotation(-180*(1 - rate));
				} else if(value >= startValue + mAnimSpace) {
					targeView.setX(mItemViewXList.get(i));
					targeView.setY(mItemViewYList.get(i));
					targeView.setRotation(0);
				}
				startValue += 0.1f;
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
				mAddItem.setRotation(-45 * (0.1f - value) * 10);
			else {
				mAddItem.setRotation(0);
			}

			float startValue = 0;
			View targeView = null;
			for(int i = mItemViewList.size() - 1; i >= 0; i--) {
				targeView = mItemViewList.get(i);
				if(value > startValue && value < startValue + mAnimSpace) {
					rate = (value - startValue)/mAnimSpace;
					targeView.setX(mItemViewXList.get(i)
							- (mItemViewXList.get(i)- mSelectItemCenterPositionX) * rate);
					targeView.setY(mItemViewYList.get(i)
							- (mItemViewYList.get(i) - mSelectItemCenterPositionY) * rate);
					targeView.setRotation(-180*(rate));
				} else if(value >= startValue + mAnimSpace) {
					targeView.setX(mSelectItemCenterPositionX);
					targeView.setY(mSelectItemCenterPositionY);
					targeView.setRotation(0);
				}
				startValue += 0.1f;
			}

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
		public void onSelectKind(int type);
		public void openSelect();
		public void closeSelect();
	}
}
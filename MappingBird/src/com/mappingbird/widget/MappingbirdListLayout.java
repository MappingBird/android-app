package com.mappingbird.widget;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.mappingbird.MappingBirdItem;
import com.mappingbird.R;
import com.mappingbird.api.MBPointData;
import com.mappingbird.common.BitmapLoader;
import com.mappingbird.common.BitmapParameters;
import com.mappingbird.common.DeBug;
import com.mappingbird.common.Utils;

public class MappingbirdListLayout extends RelativeLayout {

	private static final int MODE_NORMAL 				= 0;

	private static final int MODE_ITEM_NORMAL 			= 10;
	private static final int MODE_ITEM_CHANGE_ITEM 		= 11;
	private static final int MODE_ITEM_DRAGING			= 12;
	private static final int MODE_ITEM_ANIMATION_UP 	= 14;
	private static final int MODE_ITEM_CHANGE_TO_LIST 	= 15;
	private static final int MODE_ITEM_ANIMATION_DOWN 	= 16;

	private static final int MODE_LIST_BOTTOM_NORMAL		= 20;
	private static final int MODE_LIST_BOTTOM_DRAGING_UP	= 21;
	private static final int MODE_LIST_BOTTOM_ANIM_UP		= 22;
	private static final int MODE_LIST_BOTTOM_ANIM_DOWN		= 23;
	private static final int MODE_LIST_TOP_NORMAL			= 24;
	private static final int MODE_LIST_TOP_DRAGING_DOWN		= 25;
	private static final int MODE_LIST_TOP_ANIM_DOWN		= 29;

	private static final float MAX_ALPHA = 0.9f;

	private int mTouchDownX = 0;
	private int mTouchDownY = 0;

	private int mMode = MODE_NORMAL;
	
	private int mCard0_Position = 300;
	private int mCard1_Position = 330;
	private int mCard2_Position = 360;
	
	private int mItemMaxHeight = 400;

	private MappingbirdListView mListView;
	private MappingbirdListLayoutCardView mCard1, mCard2; 
	private MappingbirdListLayoutCardView mCard0, mCardAnim;
	private ItemAdapter mItemAdapter;

	private MBPointData mCurrentPoint = null;
	
	// Item press
	private boolean isPressCardInItemMode = false;
	// Drag down
	private boolean mListViewTop = false;
	
	//Bitmap loader
	private BitmapLoader mBitmapLoader;

	// Location
	private LatLng mMyLocation = null;

	// Click card
	private CardClickListener mCardClickListener = null;
	
	private GestureDetector mGestureDetector = null;
	private boolean isInited = false;
	
	private boolean mTouchEventFling = false;
	private float mVelocityY = 0;
	
	private GradientDrawable mDrawable;
	public MappingbirdListLayout(Context context) {
		super(context);
	}

	public MappingbirdListLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public MappingbirdListLayout(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		mListView = (MappingbirdListView) findViewById(R.id.item_list);
		mListView.setOnItemClickListener(mListViewItemClickListener);
		mListView.setVisibility(View.GONE);
		mItemAdapter = new ItemAdapter(getContext());
		mListView.setAdapter(mItemAdapter);
		
		mCard0 = (MappingbirdListLayoutCardView)findViewById(R.id.item_back0);
		mCardAnim = (MappingbirdListLayoutCardView) findViewById(R.id.item_back_anim);
		mCard1 = (MappingbirdListLayoutCardView)findViewById(R.id.item_back1);
		mCard2 = (MappingbirdListLayoutCardView)findViewById(R.id.item_back2);
		
		mBitmapLoader = new BitmapLoader(getContext());
		
		mCard0_Position = (int)getResources().getDimension(R.dimen.list_layout_card0_position_height);
		mCard1_Position = (int)getResources().getDimension(R.dimen.list_layout_card1_position_height);
		mCard2_Position = (int)getResources().getDimension(R.dimen.list_layout_card2_position_height);
		
		mItemMaxHeight = (int) getResources().getDimension(R.dimen.place_item_card_max_position);
		isInited = false;
		mGestureDetector = new GestureDetector(getContext(), mGestureListener);
		
		mDrawable = new GradientDrawable(  
		          GradientDrawable.Orientation.BOTTOM_TOP, new int[] { 0x90000000, 0x10000000});
		
		mDrawable.setGradientType(GradientDrawable.LINEAR_GRADIENT);
		mCard0.setInfoLayoutBackground(mDrawable);
		mCardAnim.setInfoLayoutBackground(mDrawable);
	}

	private OnGestureListener mGestureListener = new OnGestureListener() {
		
		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			return false;
		}
		
		@Override
		public void onShowPress(MotionEvent e) {
		}
		
		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
				float distanceY) {
			return false;
		}
		
		@Override
		public void onLongPress(MotionEvent e) {
		}
		
		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			mTouchEventFling = true;
			mVelocityY = velocityY;
			return false;
		}
		
		@Override
		public boolean onDown(MotionEvent e) {
			mTouchEventFling = false;
			mVelocityY = 0;
			return false;
		}
	};
	// init +++++++
	private void init() {
		if(isInited)
			return;
		isInited = true;
		initItemPosition();
	}

	private void initItemPosition() {
		// 1
//		RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mCard1.getLayoutParams();
//		mCard1.setLayoutParams(lp);
		mCard1.setY(getHeight() - mCard1_Position);
		mCard1.setParentHeight(-1);
		// 2
//		lp = (RelativeLayout.LayoutParams) mCard2.getLayoutParams();
//		mCard2.setLayoutParams(lp);
		mCard2.setY(getHeight() - mCard2_Position);
		mCard2.setParentHeight(-1);
		// 0
		mCard0.setY(getHeight() - mCard0_Position);
		mCard0.setParentHeight(getHeight());
	}
	// init -------

	public void setMyLocation(LatLng location) {
		mMyLocation = location;
		if(mMyLocation != null) {
			mItemAdapter.countDistance();
		}
		if(mCurrentPoint != null)
			mCard0.setData(mMyLocation, mCurrentPoint);
	}

	// Change Mode ++++++++++++++
	private void switchMode(int mode) {
		DeBug.d("Test", "switchMode from ["+mMode+"] to ["+mode+"]");
		switch(mode) {
			case MODE_NORMAL:
				handleNormal();
				break;
			case MODE_ITEM_CHANGE_TO_LIST:
				mCard0.switchItemAnimation(mCardSwitchItemListener);
				break;
			case MODE_ITEM_NORMAL: {
				handleItemNormal();
				break;
			}
			case MODE_ITEM_DRAGING: {
				handleItemDragged();
				break;
			}
			case MODE_ITEM_ANIMATION_UP : {
				handleItemAnimationUp();
				break;
			}
			case MODE_ITEM_ANIMATION_DOWN : {
				handleItemAnimationDown();
				break;
			}
			case MODE_LIST_BOTTOM_NORMAL : {
				handleListDownNormal();
				break;
			}
			case MODE_LIST_BOTTOM_DRAGING_UP : {
				mMode = MODE_LIST_BOTTOM_DRAGING_UP;
				break;
			}
			case MODE_LIST_BOTTOM_ANIM_UP : {
				handleListBottomAimUp();
				break;
			}
			case MODE_LIST_BOTTOM_ANIM_DOWN: {
				handleListBottomAimDown();
				break;
			}
			case MODE_LIST_TOP_NORMAL : {
				mMode = MODE_LIST_TOP_NORMAL;
				break;
			}
			case MODE_LIST_TOP_DRAGING_DOWN : {
				mMode = MODE_LIST_TOP_DRAGING_DOWN;
				break;
			}
			case MODE_LIST_TOP_ANIM_DOWN : {
				handleListTopAimDown();
				break;
			}
			case MODE_ITEM_CHANGE_ITEM : {
				handleChangeItem();
				break;
			}
		}
	}

	private void handleNormal() {
		if(mCard0.getVisibility() == View.VISIBLE) {
			fakeViewAnimation(false);
			ObjectAnimator objectAnimatino = ObjectAnimator.ofFloat(mCard0, "y", mCard0.getY(),
					getHeight());
			objectAnimatino.setDuration(300);
			objectAnimatino.start();
		} else if(mListView.getVisibility() == View.VISIBLE) {
			ObjectAnimator objectAnimatino = ObjectAnimator.ofFloat(this, "ListViewMarginTop", mListView.getY(),
					getHeight());
			objectAnimatino.setDuration(300);
			objectAnimatino.start();			
		}
		mMode = MODE_NORMAL;
	}

	private void handleChangeItem() {
		ObjectAnimator objectAnimatino = ObjectAnimator.ofFloat(mCard1, "y", mCard1.getY(),
				getHeight());
		objectAnimatino.setDuration(200);
		objectAnimatino.addListener(mItemChangedCard1Listener);
		objectAnimatino.start();
		mMode = MODE_ITEM_CHANGE_ITEM;
	}

	private void handleListTopAimDown() {
		ObjectAnimator objectAnimatino = ObjectAnimator.ofFloat(this, "ListViewMarginTop", mListView.getY(),
				getHeight());
		objectAnimatino.setDuration(300);
		objectAnimatino.addListener(mListViewTopAnimDownListener);
		objectAnimatino.start();
		mMode = MODE_LIST_TOP_ANIM_DOWN;
	}

	private void handleListBottomAimDown() {
		ObjectAnimator objectAnimatino = ObjectAnimator.ofFloat(this, "ListViewMarginTop", mListView.getY(),
				getHeight() - mItemMaxHeight);
		objectAnimatino.setDuration(300);
		objectAnimatino.addListener(mListViewBottomAnimDownListener);
		objectAnimatino.start();
		mMode = MODE_LIST_BOTTOM_ANIM_DOWN;
	}

	private void handleListBottomAimUp() {
		ObjectAnimator objectAnimatino = ObjectAnimator.ofFloat(this, "ListViewMarginTop", mListView.getY(),
				0);
		objectAnimatino.setDuration(300);
		objectAnimatino.addListener(mListViewBottomAnimUpListener);
		objectAnimatino.start();
		mMode = MODE_LIST_BOTTOM_ANIM_UP;
	}

	private void handleListDownNormal() {
		mCard0.resetLayout();
		mCard0.setVisibility(View.GONE);
		mListView.setVisibility(View.VISIBLE);
		mListView.setParentHeight(getHeight());
		setListViewMarginTop(getHeight() - mItemMaxHeight);
		mMode = MODE_LIST_BOTTOM_NORMAL;
	}

	private void handleItemAnimationDown() {
		ObjectAnimator objectAnimatino = ObjectAnimator.ofFloat(mCard0, "y", mCard0.getY(),
				getHeight() - mCard0_Position);
		objectAnimatino.setDuration(300);
		objectAnimatino.addListener(mItemViewMoveDownListener);
		objectAnimatino.start();
		mMode = MODE_ITEM_ANIMATION_DOWN;
	}

	private void handleItemAnimationUp() {
		ObjectAnimator objectAnimatino = ObjectAnimator.ofFloat(mCard0, "y", mCard0.getY(),
				getHeight() - mItemMaxHeight);
		int dis = (int)(getHeight() - mItemMaxHeight - mCard0.getY());
		long time = 300;
		if(dis < mItemMaxHeight/3) {
			time = 150;
		}
		
		objectAnimatino.setDuration(time);
		objectAnimatino.addListener(mItemViewMoveUpListener);
		objectAnimatino.start();
		mMode = MODE_ITEM_ANIMATION_UP;
	}

	private void handleItemDragged() {
		switch(mMode) {
			case MODE_ITEM_NORMAL: {
				DeBug.e("Test", "fakeViewAnimation close");
				fakeViewAnimation(false);
				break;
			}
		}
		mMode = MODE_ITEM_DRAGING;
	}

	private void handleItemNormal() {
		switch(mMode) {
			case MODE_LIST_TOP_ANIM_DOWN :
			case MODE_NORMAL: {
				ObjectAnimator objectAnimatino = ObjectAnimator.ofFloat(mCard0, "y", getHeight(),
						getHeight() - mCard0_Position);
				objectAnimatino.setDuration(300);
				objectAnimatino.addListener(mItemViewMoveDownListener);
				objectAnimatino.start();
				mCard0.setVisibility(View.VISIBLE);

				fakeViewAnimation(true);
				break;
			}
		}
		mMode = MODE_ITEM_NORMAL;
	}
	// Change Mode ---------------

//	private void switchMode(int mode) {
//		switch(mode) {
//		case MODE_ITEM:
//			switch(mMode) {
//			case MODE_NONE:
//				Animation anim = AnimationUtils.loadAnimation(getContext(), R.anim.layout_scroll_from_bottom_to_up);
//				setListViewMarginTop(500);
//				mListView.setAnimation(anim);
//				mListView.setVisibility(View.VISIBLE);
//				fakeViewAnimation(true);
//				break;
//			case MODE_DRAG_UP:
//			case MODE_DRAG_DOWN:
//				fakeViewAnimation(true);
//				mMode = MODE_ITEM;
//				resetLayout();
//				break;
//			}
//			mMode = mode;
//			break;
//		case MODE_DRAG_UP:
//			if(mMode == MODE_ITEM) {
//				fakeViewAnimation(false);
//			}
//			mMode = MODE_DRAG_UP;
//			break;
//		case MODE_DRAG_DOWN:
//			mMode = MODE_DRAG_DOWN;
//			break;
//		case MODE_LIST:
//			mMode = MODE_LIST;
//			resetLayout();
//			break;
//		case MODE_NONE:
//			if(mMode ==MODE_ITEM) {
//				fakeViewAnimation(false);
//				Animation anim = AnimationUtils.loadAnimation(getContext(), R.anim.layout_scroll_from_up_to_bottom);
//				anim.setAnimationListener(mListViewScrollDownListener);
//				mListView.setAnimation(anim);
//				mListView.setVisibility(View.VISIBLE);
//			}else if(mMode ==MODE_LIST) {
//				Animation anim = AnimationUtils.loadAnimation(getContext(), R.anim.layout_scroll_from_up_to_bottom);
//				anim.setAnimationListener(mListViewScrollDownListener);
//				mListView.setAnimation(anim);
//				mListView.setVisibility(View.VISIBLE);				
//			}
//			mMode = MODE_NONE;
//			break;
//		}
//	}

	public void closeLayout() {
		switchMode(MODE_NORMAL);
	}

	public void setListViewMarginTop(float top) {
		if(top < 0)
			top = 0;
		mListView.setY(top);
		float rate = ((float)top)/ (getHeight() - mItemMaxHeight);
		if(rate > 1)
			rate = 1;
		int bgColor = Color.argb((int)(0xff*(MAX_ALPHA*(1-rate))), 0xff, 0xff, 0xff);
		setBackgroundColor(bgColor);
	}

	private void setItemMarginTop(int top) {
		int value = top >= 0 ? top : 0;
		if(value > (mItemMaxHeight - mCard0_Position)) {
			value = (mItemMaxHeight - mCard0_Position);
		}
		mCard0.setY(getHeight() - mCard0_Position - value);
	}

	private OnItemClickListener mListViewItemClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			if(mCardClickListener != null) {
				mCardClickListener.onClickCard(mItemAdapter.getSelectPoint(position));
			}
		}
	};

	// Touch ++++++++++
	private boolean isItemView(float x, float y) {
		if(mCard0.getVisibility() != View.VISIBLE)
			return false;

		if(mCard0.getY() < y)
			return true;

		return false;
	}

	private boolean isListView(float x, float y) {
		if(mListView.getVisibility() != View.VISIBLE)
			return false;

		if(mListView.getY() < y)
			return true;

		return false;
	}

	private boolean handleItemNormalTouchEvent(MotionEvent ev) {
		switch(ev.getAction()) {
			case MotionEvent.ACTION_DOWN: {
				if(isItemView(ev.getX(), ev.getY())) {
					isPressCardInItemMode = true;
				} else {
					isPressCardInItemMode = false;
				}
				mTouchDownX = (int)ev.getX();
				mTouchDownY = (int)ev.getY();
				if(isPressCardInItemMode)
					return true;
				break;
			}
			case MotionEvent.ACTION_MOVE: {
				if(isPressCardInItemMode) {
					int diffX = (int)(ev.getX() - mTouchDownX);
					int diffY = (int)(ev.getY() - mTouchDownY);
					if(diffX*diffX + diffY*diffY > 100) {
						switchMode(MODE_ITEM_DRAGING);
					}
					return true;
				}
				break;
			}
			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_OUTSIDE: {
				if(isPressCardInItemMode) {
					if(mCardClickListener != null)
						mCardClickListener.onClickCard(mItemAdapter.getSelectPoint());
					return true;
				}
				break;
			}
		}
		return false;
	}

	private boolean handleItemDraggedTouchEvent(MotionEvent ev) {
		switch(ev.getAction()) {
			case MotionEvent.ACTION_DOWN:
				break;
			case MotionEvent.ACTION_MOVE:
				setItemMarginTop((int)(mTouchDownY - ev.getY()));
				return true;
			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_OUTSIDE: {
				if(!mTouchEventFling || Math.abs(mVelocityY) < 500) {
					if(mCard0.getY() < (getHeight() - mItemMaxHeight/2 - mCard0_Position/2)) {
						switchMode(MODE_ITEM_ANIMATION_UP);
					} else {
						switchMode(MODE_ITEM_ANIMATION_DOWN);
					}
				} else {
					if(mVelocityY > 0) {
						switchMode(MODE_ITEM_ANIMATION_DOWN);
					} else {
						switchMode(MODE_ITEM_ANIMATION_UP);
					}
				}
				break;
			}
		}
		return true;
	}

	private boolean handleListDownNormal(MotionEvent ev) {
		switch(ev.getAction()) {
			case MotionEvent.ACTION_DOWN: {
				DeBug.d("Test", "handleListDownNormal ACTION_DOWN");
				if(isListView(ev.getX(), ev.getY())) {
					isPressCardInItemMode = true;
				} else {
					isPressCardInItemMode = false;
				}
				mTouchDownX = (int)ev.getX();
				mTouchDownY = (int)ev.getY();
				if(isPressCardInItemMode)
					return true;
				break;
			}
			case MotionEvent.ACTION_MOVE: {
				DeBug.d("Test", "handleListDownNormal ACTION_MOVE");
				if(isPressCardInItemMode) {
					int diffX = (int)(ev.getX() - mTouchDownX);
					int diffY = (int)(ev.getY() - mTouchDownY);
					if(diffX*diffX + diffY*diffY > 100) {
						switchMode(MODE_LIST_BOTTOM_DRAGING_UP);
					}
					return true;
				}
				break;
			}
			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_OUTSIDE: {
				if(isPressCardInItemMode) {
					if(mCardClickListener != null)
						mCardClickListener.onClickCard(mItemAdapter.getSelectPoint());
					return true;
				}
				break;
			}
		}
		return false;
	}

	private boolean handleListDragUpTouchEvent(MotionEvent ev) {
		DeBug.d("Test", "handleListDragUpTouchEvent , ");
		switch(ev.getAction()) {
			case MotionEvent.ACTION_DOWN:
				break;
			case MotionEvent.ACTION_MOVE:
				int move = getHeight() - mItemMaxHeight - (int)(mTouchDownY - ev.getY());
				if(move <= 0)
					move = 0;
				setListViewMarginTop(move);
				return true;
			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_OUTSIDE: {
				if(!mTouchEventFling || Math.abs(mVelocityY) < 500) {
					if(mListView.getY() < ((getHeight()-mItemMaxHeight)/2)) {
						switchMode(MODE_LIST_BOTTOM_ANIM_UP);
					} else if(mListView.getY() < ((getHeight()-mItemMaxHeight/2))){
						switchMode(MODE_LIST_BOTTOM_ANIM_DOWN);
					} else {
						switchMode(MODE_LIST_TOP_ANIM_DOWN);
					}
				} else {
					if(mVelocityY > 0) {
						switchMode(MODE_LIST_TOP_ANIM_DOWN);
					} else {
						switchMode(MODE_LIST_BOTTOM_ANIM_UP);
					}
				}
				break;
			}
		}
		return true;
	}

	private boolean handleListTopNormal(MotionEvent ev) {
		switch(ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			DeBug.v("Test", "handleListTopNormal ACTION_DOWN");
			mListViewTop = false;
			if(mListView.getFirstVisiblePosition() == 0) {
				View v = mListView.getChildAt(0);
				int top = (v == null) ? 0 : v.getTop();
				if(top == 0) {
					mListViewTop = true;
				}
			}
			mTouchDownX = (int)ev.getX();
			mTouchDownY = (int)ev.getY();						
			break;
		case MotionEvent.ACTION_MOVE:
			DeBug.v("Test", "handleListTopNormal ACTION_MOVE, mListViewTop = "+mListViewTop);
//			DeBug.v("ACTION_MOVE , mListViewTop = "+mListViewTop+", diff = "+(ev.getY() - mTouchDownY));
			if(mListViewTop && (ev.getY() - mTouchDownY) > 0) {
				DeBug.v("Test", "handleListTopNormal change to MODE_LIST_TOP_DRAGING_DOWN");
				switchMode(MODE_LIST_TOP_DRAGING_DOWN);
				return true;
			}
			break;
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_OUTSIDE:
			break;
		}
		return false;
	}

	private boolean handleListTopDragging(MotionEvent ev) {
		
		switch(ev.getAction()) {
		case MotionEvent.ACTION_MOVE:
			DeBug.d("Test", "handleListTopDragging ACTION_MOVE");
			int move = (int)(ev.getY() - mTouchDownY);
			if(move <= 0)
				move = 0;
			setListViewMarginTop((int)(ev.getY() - mTouchDownY));
			return true;
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_OUTSIDE:
			if(!mTouchEventFling || Math.abs(mVelocityY) < 500) {
				if(mListView.getY() < getHeight()/2) {
					switchMode(MODE_LIST_BOTTOM_ANIM_UP);
				} else {
					switchMode(MODE_LIST_TOP_ANIM_DOWN);
				}
			} else {
				if(mVelocityY > 0) {
					switchMode(MODE_LIST_TOP_ANIM_DOWN);
				} else {
					switchMode(MODE_LIST_BOTTOM_ANIM_UP);
				}
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
//		DeBug.i("Test", "dispatchTouchEvent, mMode = "+mMode);
		mGestureDetector.onTouchEvent(ev);
		switch(mMode) {
			case MODE_ITEM_CHANGE_ITEM : {
				return true;
			}
			case MODE_ITEM_NORMAL: {
				if(handleItemNormalTouchEvent(ev))
					return true;
				break;
			}
			case MODE_ITEM_DRAGING: {
				if(handleItemDraggedTouchEvent(ev))
					return true;
				break;
			}
			case MODE_LIST_BOTTOM_NORMAL : {
				if(handleListDownNormal(ev))
					return true;
				break;
			}
			case MODE_LIST_BOTTOM_DRAGING_UP : {
				if(handleListDragUpTouchEvent(ev))
					return true;
				break;
			}
			case MODE_LIST_TOP_NORMAL : {
				if(handleListTopNormal(ev))
					return true;
				break;				
			}
			case MODE_LIST_TOP_DRAGING_DOWN : {
				if(handleListTopDragging(ev))
					return true;
				break;
			}
		}
		/*
		if(mMode == MODE_ITEM) {
			switch(ev.getAction()) {
			case MotionEvent.ACTION_DOWN:
				if(isTouchListView((int)ev.getX(), (int)ev.getY())) {
					isPressCardInItemMode = true;
				} else {
					isPressCardInItemMode = false;
				}
				mTouchDownX = (int)ev.getX();
				mTouchDownY = (int)ev.getY();
				break;
			case MotionEvent.ACTION_MOVE:
				if(isPressCardInItemMode) {
					int diffX = (int)(ev.getX() - mTouchDownX);
					int diffY = (int)(ev.getY() - mTouchDownY);
					if(diffX*diffX + diffY*diffY > 100) {
						switchMode(MODE_DRAG_UP);
					}
					return true;					
				}
				break;
			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_OUTSIDE:
				if(isPressCardInItemMode) {
					if(mCardClickListener != null)
						mCardClickListener.onClickCard(mItemAdapter.getSelectPoint());
					return true;
				}
				break;
			}
		} else if(mMode == MODE_DRAG_UP) {
			switch(ev.getAction()) {
			case MotionEvent.ACTION_MOVE:
				setListViewMarginTop((int)(mTouchDownY - ev.getY()));
				return true;
//				break;
			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_OUTSIDE:
				if(Math.abs(mTouchDownY - ev.getY()) > getHeight()/2) {
					switchMode(MODE_LIST);
				} else {
					switchMode(MODE_ITEM);
				}
				return true;
//				break;
			}
		} else if(mMode == MODE_LIST) {
			switch(ev.getAction()) {
			case MotionEvent.ACTION_DOWN:
				mListViewTop = false;
				if(mListView.getFirstVisiblePosition() == 0) {
					View v = mListView.getChildAt(0);
					int top = (v == null) ? 0 : v.getTop();
					if(top == 0) {
						mListViewTop = true;
					}
				}
				mTouchDownX = (int)ev.getX();
				mTouchDownY = (int)ev.getY();						
				break;
			case MotionEvent.ACTION_MOVE:
				DeBug.v("ACTION_MOVE , mListViewTop = "+mListViewTop+", diff = "+(ev.getY() - mTouchDownY));
				if(mListViewTop && (ev.getY() - mTouchDownY) > 0) {
					
					switchMode(MODE_DRAG_DOWN);
					return true;
				}
				break;
			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_OUTSIDE:
				break;
			}
		} else if(mMode == MODE_DRAG_DOWN) {
			switch(ev.getAction()) {
			case MotionEvent.ACTION_MOVE:
				setListViewMarginTop((int)(ev.getY() - mTouchDownY));
				return true;
			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_OUTSIDE:
				if(Math.abs(ev.getY() - mTouchDownY) < getHeight()/2) {
					switchMode(MODE_LIST);
				} else {
					switchMode(MODE_ITEM);
				}
				return true;
			}			
		}
		*/
		return super.dispatchTouchEvent(ev);
	}

	// Animation +++++++++++++++
	private void fakeViewAnimation(boolean visiable) {
		if(visiable) {
			TranslateAnimation anim1 = new TranslateAnimation(0, 0, mCard1.getY(), 0);
			anim1.setInterpolator(new AccelerateInterpolator());
			anim1.setDuration(350);
			mCard1.setAnimation(anim1);
			mCard1.setVisibility(View.VISIBLE);
			TranslateAnimation anim2 = new TranslateAnimation(0, 0, mCard2.getY(), 0);
			anim2.setInterpolator(new AccelerateInterpolator());
			anim2.setDuration(400);
			mCard2.setAnimation(anim2);
			mCard2.setVisibility(View.VISIBLE);
			
		} else {
			TranslateAnimation anim1 = new TranslateAnimation(0, 0, 0, mCard1.getY());
			anim1.setAnimationListener(mFake1MoveDownListener);
			anim1.setInterpolator(new DecelerateInterpolator());
			anim1.setDuration(300);
			mCard1.startAnimation(anim1);
			mCard1.setVisibility(View.VISIBLE);
			TranslateAnimation anim2 = new TranslateAnimation(0, 0, 0, mCard1.getY());
			anim2.setAnimationListener(mFake2MoveDownListener);
			anim2.setInterpolator(new DecelerateInterpolator());
			anim2.setDuration(300);
			mCard2.startAnimation(anim2);
			mCard2.setVisibility(View.VISIBLE);						
		}
	}
	// Animation -----------------

	public void setPositionData(ArrayList<MBPointData> items) {
		mItemAdapter.setItem(items);
		init();
		if(mItemAdapter.getCount() > 0) {
			ListItem first = (ListItem)mItemAdapter.getItem(0);
			mItemAdapter.clickItem(first);
			mCurrentPoint = first.mPoint;
			mCard0.setData(mMyLocation, mCurrentPoint);
			switchMode(MODE_ITEM_NORMAL);
		} else {
			
		}
	}

	public void clickItem(MappingBirdItem item) {
		MBPointData point = mItemAdapter.clickItem(item);
		if(!mCurrentPoint.equals(point)) {
			mCurrentPoint = point;
			if(mMode == MODE_ITEM_NORMAL) {
				switchMode(MODE_ITEM_CHANGE_ITEM);
			} else {
				mCard0.setData(mMyLocation, point);
				switchMode(MODE_ITEM_NORMAL);
			}
		}
	}

	private AnimatorListener mItemChangedCard1Listener = new AnimatorListener() {
		@Override
		public void onAnimationCancel(Animator animation) {
		}

		@Override
		public void onAnimationEnd(Animator animation) {
			
			mCardAnim.setData(mMyLocation, mCard0.getPoint());
			mCardAnim.setVisibility(View.VISIBLE);
			mCardAnim.setY(getHeight() - mCard0_Position);
			float scale = mCard1.getWidth()*1.0f / mCard0.getWidth();
			mCard0.setData(mMyLocation, mCurrentPoint);
			mCard0.setY(getHeight());

			ObjectAnimator objectAnimatino = ObjectAnimator.ofFloat(mCardAnim, "y", getHeight() - mCard0_Position,
					getHeight() - mCard1_Position);
			objectAnimatino.setDuration(200);
			objectAnimatino.start();
			ObjectAnimator objectAnimatino2 = ObjectAnimator.ofFloat(mCardAnim, "ScaleX", 1.0f,
					scale);
			objectAnimatino2.setDuration(200);
			objectAnimatino2.start();

			ObjectAnimator objectAnimatino1 = ObjectAnimator.ofFloat(mCard0, "y", getHeight(),
					getHeight() - mCard0_Position);
			objectAnimatino1.setDuration(200);
			objectAnimatino.addListener(mItemChangedCard0UpListener);
			objectAnimatino1.start();
		}

		@Override
		public void onAnimationRepeat(Animator animation) {
		}

		@Override
		public void onAnimationStart(Animator animation) {
		}
	};

	private AnimatorListener mItemChangedCard0UpListener = new AnimatorListener() {
		@Override
		public void onAnimationCancel(Animator animation) {
		}

		@Override
		public void onAnimationEnd(Animator animation) {
			mCard1.cleanData();
			mCard1.setY(getHeight() - mCard1_Position);
			mCardAnim.cleanData();
			mCardAnim.setVisibility(View.GONE);
			switchMode(MODE_ITEM_NORMAL);
		}

		@Override
		public void onAnimationRepeat(Animator animation) {
		}

		@Override
		public void onAnimationStart(Animator animation) {
		}
	};

	private AnimatorListener mListViewAnimUpListener = new AnimatorListener() {
		@Override
		public void onAnimationCancel(Animator animation) {
		}

		@Override
		public void onAnimationEnd(Animator animation) {
			switchMode(MODE_LIST_BOTTOM_NORMAL);
		}

		@Override
		public void onAnimationRepeat(Animator animation) {
		}

		@Override
		public void onAnimationStart(Animator animation) {
		}
	};

	private AnimatorListener mListViewTopAnimDownListener = new AnimatorListener() {
		@Override
		public void onAnimationCancel(Animator animation) {
		}

		@Override
		public void onAnimationEnd(Animator animation) {
			switchMode(MODE_ITEM_NORMAL);
		}

		@Override
		public void onAnimationRepeat(Animator animation) {
		}

		@Override
		public void onAnimationStart(Animator animation) {
		}
	};

	private AnimatorListener mListViewBottomAnimUpListener = new AnimatorListener() {
		@Override
		public void onAnimationCancel(Animator animation) {
		}

		@Override
		public void onAnimationEnd(Animator animation) {
			switchMode(MODE_LIST_TOP_NORMAL);
		}

		@Override
		public void onAnimationRepeat(Animator animation) {
		}

		@Override
		public void onAnimationStart(Animator animation) {
		}
	};

	private AnimatorListener mListViewBottomAnimDownListener = new AnimatorListener() {
		@Override
		public void onAnimationCancel(Animator animation) {
		}

		@Override
		public void onAnimationEnd(Animator animation) {
			switchMode(MODE_LIST_BOTTOM_NORMAL);
		}

		@Override
		public void onAnimationRepeat(Animator animation) {
		}

		@Override
		public void onAnimationStart(Animator animation) {
		}
	};

	private AnimatorListener mCardSwitchItemListener = new AnimatorListener() {
		@Override
		public void onAnimationCancel(Animator animation) {
		}

		@Override
		public void onAnimationEnd(Animator animation) {
			switchMode(MODE_LIST_BOTTOM_NORMAL);
		}

		@Override
		public void onAnimationRepeat(Animator animation) {
		}

		@Override
		public void onAnimationStart(Animator animation) {
		}
	};

	private AnimatorListener mItemViewMoveUpListener = new AnimatorListener() {
		@Override
		public void onAnimationCancel(Animator animation) {
		}

		@Override
		public void onAnimationEnd(Animator animation) {
			switchMode(MODE_ITEM_CHANGE_TO_LIST);
		}

		@Override
		public void onAnimationRepeat(Animator animation) {
		}

		@Override
		public void onAnimationStart(Animator animation) {
		}
	};

	private AnimatorListener mItemViewMoveDownListener = new AnimatorListener() {
		@Override
		public void onAnimationCancel(Animator animation) {
		}

		@Override
		public void onAnimationEnd(Animator animation) {
			fakeViewAnimation(true);
			switchMode(MODE_ITEM_NORMAL);
		}

		@Override
		public void onAnimationRepeat(Animator animation) {
		}

		@Override
		public void onAnimationStart(Animator animation) {
		}
	};

	private AnimationListener mFake1MoveDownListener = new AnimationListener() {
		
		@Override
		public void onAnimationStart(Animation animation) {
		}
		
		@Override
		public void onAnimationRepeat(Animation animation) {
		}
		
		@Override
		public void onAnimationEnd(Animation animation) {
			mCard1.setVisibility(View.GONE);
		}
	};

	private AnimationListener mFake2MoveDownListener = new AnimationListener() {
		
		@Override
		public void onAnimationStart(Animation animation) {
		}
		
		@Override
		public void onAnimationRepeat(Animation animation) {
		}
		
		@Override
		public void onAnimationEnd(Animation animation) {
			mCard2.setVisibility(View.GONE);
		}
	};

	public void setCardClickListener(CardClickListener listener) {
		mCardClickListener = listener;
	}

	public interface CardClickListener {
		public void onClickCard(MBPointData point);
	}

	private class ItemAdapter extends BaseAdapter {

		private ArrayList<ListItem> mAllPoints = new ArrayList<ListItem>();
		private ArrayList<ListItem> mItems = new ArrayList<ListItem>();
		private ListItem mSelectPoint = null;
		private LayoutInflater mInflater;
		private Context mContext;

		public ItemAdapter(Context context) {
			mInflater = LayoutInflater.from(context);
			mContext = context;
		}

		public synchronized void setItem(ArrayList<MBPointData> items) {
			mAllPoints.clear();
			for(MBPointData item : items) {
				mAllPoints.add(new ListItem(item));
			}
			countDistance();
			mItems.clear();
			mItems.addAll(mAllPoints);
			notifyDataSetChanged();
		}

		public synchronized void countDistance() {
			if(mMyLocation == null)
				return;
			for(ListItem item : mAllPoints) {
				item.countDistance(mMyLocation);
			}
			Collections.sort(mAllPoints, new Comparator<ListItem>() {

				@Override
				public int compare(ListItem lhs, ListItem rhs) {
					return (int)(lhs.mDistance - rhs.mDistance);
				}
				
			});
		}

		public MBPointData getSelectPoint(int index) {
			if(index >= mItems.size())
				return null;
			return mItems.get(index).mPoint;
		}
		
		public MBPointData getSelectPoint() {
			if(mSelectPoint == null)
				return null;
			return mSelectPoint.mPoint;
		}

		public synchronized void clickItem(ListItem item) {
			mItems.clear();
			for(ListItem point : mAllPoints) {
				if(point.equals(item.mPoint.getLatLng())) {
					mSelectPoint = point;
					mItems.add(0, point);
				} else {
					mItems.add(point);
				}
			}
			notifyDataSetChanged();			
		}

		public synchronized MBPointData clickItem(MappingBirdItem item) {
			mItems.clear();
			MBPointData clickPoint = null;
			for(ListItem point : mAllPoints) {
				if(point.equals(item.mPosition)) {
					clickPoint = point.mPoint;
					DeBug.d("Click item : "+item.mTitle);
					mSelectPoint = point;
					mItems.add(0, point);
				} else {
					mItems.add(point);
				}
			}
			notifyDataSetChanged();
			return clickPoint;
		}

		@Override
		public int getCount() {
			return mItems.size();
		}

		@Override
		public Object getItem(int position) {
			return mItems.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if(convertView == null) {
				convertView = mInflater.inflate(R.layout.mappingbird_place_item, parent, false);
			}
			ListItem item = mItems.get(position);
			ImageView image = (ImageView) convertView.findViewById(R.id.card_icon);
			TextView title = (TextView) convertView.findViewById(R.id.card_title);
			TextView tag = (TextView) convertView.findViewById(R.id.card_subtitle);
			String imagePath = null;
			convertView.findViewById(R.id.card_info_layout).setBackground(mDrawable);
			if(item.mPoint.getImageDetails().size() > 0) {
//				imagePath = item.mPoint.getImageDetails().get(0).getThumbPath();
				if(TextUtils.isEmpty(imagePath))
					imagePath = item.mPoint.getImageDetails().get(0).getUrl();
				BitmapParameters params = BitmapParameters.getUrlBitmap(imagePath);
				mBitmapLoader.getBitmap(image, params);
			} else {
				image.setImageDrawable(null);
			}

			title.setText(item.mPoint.getTitle());
			if(item.mPoint.getTags().size() == 0)
				tag.setVisibility(View.GONE);
			else {
				tag.setVisibility(View.VISIBLE);
				tag.setText(item.mPoint.getTagsString());
			}

			TextView dis = (TextView) convertView.findViewById(R.id.card_distance);
			// dis 
			if(mMyLocation != null) {
				dis.setText(
						Utils.getDistanceString(
								item.mDistance));
			} else {
				dis.setText("");
			}
			
			TextView address = (TextView) convertView.findViewById(R.id.card_address);
			address.setText(item.mPoint.getLocation().getPlaceAddress());
			DeBug.i("Test", "height = "+convertView.getHeight());
			return convertView;
		}
	}

	private class ListItem {
		final MBPointData mPoint;
		float mDistance = 0;
		public ListItem(MBPointData point){
			mPoint = point;
		}

		public void countDistance(LatLng location) {
			mDistance = Utils.getDistance(mMyLocation.latitude,
					mMyLocation.longitude, 
					mPoint.getLocation().getLatitude(), 
					mPoint.getLocation().getLongitude());
		}

		public boolean equals(LatLng latlng) {
			return mPoint.getLatLng().equals(latlng);
		}
	}
}
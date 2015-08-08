package com.mappingbird.collection.widget;

import java.util.ArrayList;
import java.util.Comparator;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.mappingbird.api.MBPointData;
import com.mappingbird.collection.widget.MBListLayoutAddLayout.OnSelectKindLayoutListener;
import com.mappingbird.common.BitmapLoader;
import com.mappingbird.common.BitmapLoader.BitmapDownloadedListener;
import com.mappingbird.common.BitmapParameters;
import com.mappingbird.common.DeBug;
import com.mappingbird.common.DistanceObject;
import com.mappingbird.saveplace.MappingBirdPickPlaceActivity;
import com.mpbd.mappingbird.MappingBirdItem;
import com.mpbd.mappingbird.R;
import com.mpbd.mappingbird.util.MBUtil;
import com.mpbd.mappingbird.util.Utils;

public class MBCollectionListLayout extends RelativeLayout {
	private final static String TAG = "MB.Collection.List";
	
	private boolean isInited = false;
	// Touch
	private GestureDetector mGestureDetector = null;
	private boolean mTouchEventFling = false;
	private float mVelocityY = 0;

	// Card
	private MBListLayoutCardView mCard;
	private int mCardDefaultPositionY = 0;
	private float mCardMaxHeight = 0;

	private MBListLayoutChangeCardObject mChangeCardAnimBoj; 
	// Location
	private LatLng mMyLocation = null;
	
	// ListView
	private boolean mListViewTop = false;
	private ListView mListView;
	private ItemAdapter mItemAdapter;

	// Select kind
	private MBListLayoutAddLayout mAddLayout;

	//Bitmap loader
	private BitmapLoader mBitmapLoader;
	
	private float mDragY = 0;
	private float mStartY = 0, mEndY = 0;
	
	// 當只有兩個Item的時候. 不能拉到最上面
	private float mMostTopPosition = 0;
	private float mCardHeight = 0;

	private MBPointData mCurrentPoint = null;

	// TouchLock
	// 1. Change card animaiont
	private boolean lockTouchEvent = false;
	
	// Animator
	private static final int ANIMATOR_CARD = 300;
	private ValueAnimator mValueAnimator;

	private int mItemWidth = 0;
	public MBCollectionListLayout(Context context) {
		super(context);
	}

	public MBCollectionListLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public MBCollectionListLayout(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		mCard = (MBListLayoutCardView) findViewById(R.id.item_card);
		mChangeCardAnimBoj = new MBListLayoutChangeCardObject();
		
		mAddLayout = (MBListLayoutAddLayout) findViewById(R.id.item_add_layout);
		mAddLayout.setOnSelectKindLayoutListener(mOnSelectKindLayoutListener);

		mListView = (ListView) findViewById(R.id.item_list);
		mListView.setVisibility(View.INVISIBLE);
		mBitmapLoader = new BitmapLoader(getContext());
		mItemAdapter = new ItemAdapter(getContext());
		mListView.setAdapter(mItemAdapter);
		mListView.setOnItemClickListener(mListViewItemClickListener);
		
		mGestureDetector = new GestureDetector(getContext(), mGestureListener);
		
		mItemWidth = (int)(MBUtil.getWindowWidth(getContext()) 
				- getResources().getDimension(R.dimen.coll_list_item_card_padding_right)
				- getResources().getDimension(R.dimen.coll_list_item_card_padding_left)
				- getResources().getDimension(R.dimen.coll_list_item_title_margin_left)
				- getResources().getDimension(R.dimen.coll_list_item_title_margin_right));

	}

	public void closeLayout() {
	}

	public void setMyLocation(LatLng location) {
		mMyLocation = location;
	}

	private void init() {
		if(isInited)
			return;

		int marge_bottom_other = (int) getResources().getDimension(R.dimen.coll_card_marge_bottom_other);
		int card_position = marge_bottom_other * 2 + mCard.getHeight() - mCard.getPaddingBottom();
		mCardDefaultPositionY = getHeight() - card_position;
		mCard.setY(mCardDefaultPositionY);
		mCard.setParentHeight(getHeight());
		mCard.setCardPosition(card_position);
		mCardMaxHeight = getHeight() - ((int) getResources().getDimension(R.dimen.place_item_card_max_position));

		mCardHeight = (int) getResources().getDimension(R.dimen.place_item_card_max_position);
		// Add layout滑出動畫
		mAddLayout.setSlidOutDistance(mCardDefaultPositionY - mCardMaxHeight);
		int shadow_height = mCard.getPaddingBottom();
		mChangeCardAnimBoj.setPosition(getHeight(), 0, shadow_height, marge_bottom_other);
		isInited = true;
	}

	private void initDefaultBitmap() {
		if(mChangeCardAnimBoj.init(mCard)) {
			postInvalidate();
		}
	}

	public void setPositionData(ArrayList<MBPointData> items) {
		if(DeBug.DEBUG)
			DeBug.d(TAG, "setPositionData, size = "+items.size());
		mItemAdapter.setItem(items);
		// 檢查Y最高可以到多少
		
		init();
		initDefaultBitmap();
		if(mItemAdapter.getCount() > 0) {
			// 有資料
			int windowHeight = MBUtil.getWindowHeight(getContext());
			int titleBarHeight = (int)getContext().getResources().getDimension(R.dimen.title_bar_height);
			if(mItemAdapter.getCount() * mCardHeight >= (windowHeight - titleBarHeight)) {
				mMostTopPosition = 0;
			} else {
				mMostTopPosition = windowHeight - mItemAdapter.getCount() * mCardHeight - titleBarHeight;				
			}
			
			ListItem first = (ListItem)mItemAdapter.getItem(0);
			mItemAdapter.clickItem(first);
			mCurrentPoint = first.mPoint;
			mCard.setData(mMyLocation, first.mPoint);
			mCard.setVisibility(View.VISIBLE);
			if(MBUtil.mEnableAddFunction) {
				mAddLayout.init(true);
				mAddLayout.setVisibility(View.VISIBLE);
			}
		} else {
			// 無資料
			mCard.setVisibility(View.GONE);
			if(MBUtil.mEnableAddFunction) {
				mAddLayout.init(false);
				mAddLayout.setVisibility(View.VISIBLE);
			}			
		}
	}

	public void clickItem(MappingBirdItem item) {
		if(DeBug.DEBUG)
			DeBug.d(TAG, "clickItem,  item = "+item.mTitle);
		MBPointData point = mItemAdapter.clickItem(item);
		if(!mCurrentPoint.equals(point)) {
			mCurrentPoint = point;
			mChangeCardAnimBoj.prepareChangeCard(mCard);
			mCard.setVisibility(View.INVISIBLE);
			ObjectAnimator obj = ObjectAnimator.ofFloat(this, "SwitchAnimation", 0.0f, 1.0f);
			obj.addListener(mListener);
			obj.setInterpolator(new  DecelerateInterpolator());
			obj.setDuration(500);
			obj.start();
			mCard.setData(mMyLocation, point);
		}
	}

	public void setSwitchAnimation(float value) {
		mChangeCardAnimBoj.onChangedCardAnimation(value);
		postInvalidate();
	}

	private AnimatorListener mListener = new AnimatorListener() {

		@Override
		public void onAnimationCancel(Animator animation) {
			lockTouchEvent = false;
		}

		@Override
		public void onAnimationEnd(Animator animation) {
			mChangeCardAnimBoj.restoreCardPosition();
			mCard.setVisibility(View.VISIBLE);
			postInvalidate();
			lockTouchEvent = false;
		}

		@Override
		public void onAnimationRepeat(Animator animation) {
		}

		@Override
		public void onAnimationStart(Animator animation) {
			lockTouchEvent = true;
		}
	};

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

	private NewCardClickListener mNewCardClickListener = null;

	public void setCardClickListener(NewCardClickListener listener) {
		mNewCardClickListener = listener;
	}

	private OnItemClickListener mListViewItemClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			if(mNewCardClickListener != null) {
				mNewCardClickListener.onClickCard(mItemAdapter.getSelectPoint(position));
			}
		}
	};

	public interface NewCardClickListener {
		public void onClickCard(MBPointData point);
		public void onProgressFinished();
		public void onCancelUpload();
		public void onCurrentPosition();
	}

	private static final int MOVE_POSITION_ANIMATION = 300;

	private static final int MODE_SMALL_CARD = 10;
	private static final int MODE_DRAGE_SMALL_CARD = 11;
	private static final int MODE_ANIM_TO_SMALL_CARD = 12;
	private static final int MODE_MIDDLE = 20;
	private static final int MODE_MIDDLE_DRAGE = 21;
	private static final int MODE_MIDDLE_ANIM = 22;
	private static final int MODE_ALL = 30;
	private static final int MODE_ALL_DRAGE = 31;
	private static final int MODE_ALL_ANIM = 32;
	
	private static final float RATE_SMALL = 0.5f;
	private static final float RATE_MIDDLE = 0.5f;

	private int mMode = MODE_SMALL_CARD;
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		
		if(mItemAdapter.getCount() == 0)
			return super.dispatchTouchEvent(ev);

		if(lockTouchEvent)
			return true;

		mGestureDetector.onTouchEvent(ev);
//		DeBug.d("Test", "mMode = "+mMode);
		switch(mMode) {
			case MODE_SMALL_CARD:
				if(handleSmallCardTouchEvent(ev))
					return true;
				break;
			case MODE_DRAGE_SMALL_CARD:
				if(handleDraggedSmallCardTouchEvent(ev))
					return true;
				break;
			case MODE_MIDDLE:
				if(handleMiddleTouchEvent(ev))
					return true;
				break;
			case MODE_MIDDLE_DRAGE:
				if(handleDraggedMiddleTouchEvent(ev))
					return true;
				break;
			case MODE_ALL:
				if(handleAllEvent(ev))
					return true;
				break;
			case MODE_ALL_DRAGE:
				if(handleDraggedAllTouchEvent(ev))
					return true;
				break;
		}
		return super.dispatchTouchEvent(ev);
	}

	private boolean isPressCardInItemMode = false;
	private int mTouchDownX = 0;
	private int mTouchDownY = 0;
	
	private boolean handleSmallCardTouchEvent(MotionEvent ev) {
		switch(ev.getAction()) {
			case MotionEvent.ACTION_DOWN: {
				if(mCard.isTouchCard(ev.getX(), ev.getY())) {
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
						isPressCardInItemMode = false;
						mCard.perpareDragCardParameter();
						switchMode(MODE_DRAGE_SMALL_CARD);
					}
					return true;
				}
				break;
			}
			case MotionEvent.ACTION_UP: {
				if(isPressCardInItemMode) {
					isPressCardInItemMode = false;
					// Click event
					if(mNewCardClickListener != null)
						mNewCardClickListener.onClickCard(mItemAdapter.getSelectPoint());
					return true;
				}
				isPressCardInItemMode = false;
				break;
			}
			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_OUTSIDE:
				isPressCardInItemMode = false;
				break;
			}
		return false;
	}

	private boolean handleDraggedSmallCardTouchEvent(MotionEvent ev) {
		switch(ev.getAction()) {
			case MotionEvent.ACTION_DOWN:
				break;
			case MotionEvent.ACTION_MOVE:
				float touchY = mCardDefaultPositionY - (int)(mTouchDownY - ev.getY());
				// 下限
				if(touchY > mCardDefaultPositionY)
					touchY = mCardDefaultPositionY;
				// 上限
//				if(touchY < mCardMaxHeight)
//					touchY = mCardMaxHeight;
				// 行為
				setCardAndListViewY(touchY);
				return true;
			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_OUTSIDE: {
				checkTouchUp();
				break;
			}
		}
		return true;
	}

	private boolean handleMiddleTouchEvent(MotionEvent ev) {
		switch(ev.getAction()) {
			case MotionEvent.ACTION_DOWN: {
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
				if(isPressCardInItemMode) {
					int diffX = (int)(ev.getX() - mTouchDownX);
					int diffY = (int)(ev.getY() - mTouchDownY);
					if(diffX*diffX + diffY*diffY > 80) {
						isPressCardInItemMode = false;
						switchMode(MODE_MIDDLE_DRAGE);
					}
					return true;
				}
				break;
			}
			case MotionEvent.ACTION_UP: {
				isPressCardInItemMode = false;
				if(isPressCardInItemMode) {
					// Click event
					return true;
				}
				break;
			}
			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_OUTSIDE:
				isPressCardInItemMode = false;
				break;
			}
		return false;
	}

	private boolean handleDraggedMiddleTouchEvent(MotionEvent ev) {
		switch(ev.getAction()) {
			case MotionEvent.ACTION_DOWN:
				break;
			case MotionEvent.ACTION_MOVE:
				float touchY = mCardMaxHeight - (int)(mTouchDownY - ev.getY());
				// 下限
				if(touchY > mCardDefaultPositionY)
					touchY = mCardDefaultPositionY;
				// 上限
				if(touchY < 0)
					touchY = 0;
				// 行為
				setCardAndListViewY(touchY);
				return true;
			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_OUTSIDE: {
				checkTouchUp();
				break;
			}
		}
		return true;
	}

	private boolean handleAllEvent(MotionEvent ev) {
		switch(ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mListViewTop = false;
			mTouchDownX = (int)ev.getX();
			mTouchDownY = (int)ev.getY();						
			if(mListView.getFirstVisiblePosition() == 0) {
				View v = mListView.getChildAt(0);
				int top = (v == null) ? 0 : v.getTop();
				if(top == 0) {
					mListViewTop = true;
				}
			}
			break;
		case MotionEvent.ACTION_MOVE:
			if(mListViewTop && (ev.getY() - mTouchDownY) > -mMostTopPosition) {
				DeBug.v("Test", "handleListTopNormal change to MODE_LIST_TOP_DRAGING_DOWN");
				switchMode(MODE_ALL_DRAGE);
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

	private boolean handleDraggedAllTouchEvent(MotionEvent ev) {
		switch(ev.getAction()) {
			case MotionEvent.ACTION_DOWN:
				break;
			case MotionEvent.ACTION_MOVE:
				float touchY = (int)(mMostTopPosition + ev.getY() - mTouchDownY);
				// 下限
				if(touchY > mCardDefaultPositionY)
					touchY = mCardDefaultPositionY;
				// 上限
				if(touchY < 0)
					touchY = 0;
				// 行為
				setCardAndListViewY(touchY);
				return true;
			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_OUTSIDE: {
				checkTouchUp();
				break;
			}
		}
		return true;
	}

	private void checkTouchUp() {
		mStartY = mDragY;
		if(!mTouchEventFling || Math.abs(mVelocityY) < 2500) {
			if(mItemAdapter.getCount() > 1) {
				// 動量不足, 不是大力滑動
				if(mDragY < (mCardMaxHeight*RATE_MIDDLE)) {
					// 進入All
					switchMode(MODE_ALL_ANIM);
					mEndY = mMostTopPosition;
				} else if(mDragY > (mCardMaxHeight*RATE_MIDDLE) &&
						mDragY < (mCardDefaultPositionY - (mCardDefaultPositionY - mCardMaxHeight)*RATE_SMALL)) {
					// 回到Middle
					switchMode(MODE_MIDDLE_ANIM);
					mEndY = mCardMaxHeight;
				} else {
					// 回到Small
					switchMode(MODE_ANIM_TO_SMALL_CARD);
					mEndY = mCardDefaultPositionY;
				}
			} else {
				// 只有一張, 所以只有Middle
				if(mDragY < (mCardMaxHeight*RATE_MIDDLE) ||
						(mDragY > (mCardMaxHeight*RATE_MIDDLE) &&
						mDragY < (mCardDefaultPositionY - (mCardDefaultPositionY - mCardMaxHeight)*RATE_SMALL))) {
					// 回到Middle
					switchMode(MODE_MIDDLE_ANIM);
					mEndY = mCardMaxHeight;
				} else {
					// 回到Small
					switchMode(MODE_ANIM_TO_SMALL_CARD);
					mEndY = mCardDefaultPositionY;
				}
			}
		} else {
			// 大力滑動
			if(mItemAdapter.getCount() > 1) {
				if(mVelocityY > 0) {
					switchMode(MODE_ANIM_TO_SMALL_CARD);
					mEndY = mCardDefaultPositionY;
				} else {
					switchMode(MODE_ALL_ANIM);
					mEndY = mMostTopPosition;
				}
			} else {
				if(mVelocityY > 0) {
					switchMode(MODE_ANIM_TO_SMALL_CARD);
					mEndY = mCardDefaultPositionY;
				} else {
					switchMode(MODE_MIDDLE_ANIM);
					mEndY = mCardMaxHeight;
				}
			}
		}		
		ObjectAnimator obj = ObjectAnimator.ofFloat(this, "SwitchModeAnimation", 0.0f, 1.0f);
		obj.addListener(mSwitchModeAnimationListener);
		obj.setInterpolator(new DecelerateInterpolator());
		obj.setDuration(MOVE_POSITION_ANIMATION);
		obj.start();
	}

	private AnimatorListener mSwitchModeAnimationListener = new AnimatorListener() {

		@Override
		public void onAnimationCancel(Animator animation) {
			lockTouchEvent = false;
		}

		@Override
		public void onAnimationEnd(Animator animation) {
			switch (mMode) {
			case MODE_ALL_ANIM:
				switchMode(MODE_ALL);
				break;
			case MODE_MIDDLE_ANIM:
				switchMode(MODE_MIDDLE);
				break;
			case MODE_ANIM_TO_SMALL_CARD:
				switchMode(MODE_SMALL_CARD);
				break;
			}
			postInvalidate();
			lockTouchEvent = false;
		}

		@Override
		public void onAnimationRepeat(Animator animation) {
		}

		@Override
		public void onAnimationStart(Animator animation) {
			lockTouchEvent = true;
		}
	};

	public void setSwitchModeAnimation(float vaule) {
		float positionY = mStartY + (mEndY - mStartY)*vaule;
		setCardAndListViewY(positionY);
	}

	private void switchMode(int mode) {
		mMode = mode;
		switch(mMode) {
		case MODE_SMALL_CARD:
			mListView.setSelectionFromTop(0, 0);
			break;
		case MODE_MIDDLE:
			mCard.setVisibility(View.INVISIBLE);
			mChangeCardAnimBoj.setVisiable(false);
			mListView.setVisibility(View.VISIBLE);
			mListView.setY(mCardMaxHeight);
			break;
		case MODE_ALL:
			mCard.setVisibility(View.INVISIBLE);
			mChangeCardAnimBoj.setVisiable(false);
			break;
		}
	}

	private void setCardAndListViewY(float touchY) {
		mDragY = touchY;
		if(mDragY >= mCardMaxHeight) {
			if(mListView.getVisibility() == View.VISIBLE
					|| mCard.getVisibility() != View.VISIBLE) {
				mCard.setVisibility(View.VISIBLE);
				mChangeCardAnimBoj.setVisiable(true);
				mListView.setVisibility(View.INVISIBLE);
				setBackgroundColor(0x00000000);
			}
			mCard.setTranlatorY(mDragY);
			if(MBUtil.mEnableAddFunction)
				mAddLayout.setTranlatorX(mCardDefaultPositionY - mDragY);
		} else {
			if(mListView.getVisibility() != View.VISIBLE
					|| mCard.getVisibility() == View.VISIBLE) {
				mCard.setVisibility(View.INVISIBLE);
				mChangeCardAnimBoj.setVisiable(false);
				mListView.setVisibility(View.VISIBLE);
			}
			if(mDragY >= 0)
				mListView.setY(mDragY);
			else
				mListView.scrollTo(0, -(int)mDragY);
			float alpha = mDragY * 1.0f / (getHeight() -  mCardMaxHeight);
			if(alpha > 1)
				alpha = 1;
			else if(alpha < 0)
				alpha = 0;
			int bgColor = Color.argb((int)(0xff*(1 - alpha)), 0xE7, 0xE7, 0xE7);
			setBackgroundColor(bgColor);
		}
	}

	private boolean isListView(float x, float y) {
		if(mListView.getVisibility() != View.VISIBLE)
			return false;

		if(mListView.getY() < y)
			return true;

		return false;
	}

	private OnSelectKindLayoutListener mOnSelectKindLayoutListener = new OnSelectKindLayoutListener() {
		
		@Override
		public void openSelect() {
			int detailX = getHeight() - mCardDefaultPositionY;
			mValueAnimator = ValueAnimator.ofInt(0, detailX);
			mValueAnimator.setDuration(ANIMATOR_CARD);
			mValueAnimator.addUpdateListener(new CardObjectAnimator(CardObjectAnimator.ANIM_DOWN));
			mValueAnimator.start();
		}
		
		@Override
		public void onSelectKind(String type) {
			
			Intent intent = new Intent(getContext(), MappingBirdPickPlaceActivity.class);
			intent.putExtra(MappingBirdPickPlaceActivity.EXTRA_TYPE, type);
			intent.putExtra(MappingBirdPickPlaceActivity.EXTRA_LAT, mMyLocation.latitude);
			intent.putExtra(MappingBirdPickPlaceActivity.EXTRA_LONG, mMyLocation.longitude);
			getContext().startActivity(intent);
		}
		
		@Override
		public void closeSelect() {
			int detailX = getHeight() - mCardDefaultPositionY;
			mValueAnimator = ValueAnimator.ofInt(detailX, 0);
			mValueAnimator.setDuration(ANIMATOR_CARD);
			mValueAnimator.addUpdateListener(new CardObjectAnimator(CardObjectAnimator.ANIM_UP));
			mValueAnimator.start();
		}

		@Override
		public void onProgressFinished() {
			if(mNewCardClickListener != null)
				mNewCardClickListener.onProgressFinished();
		}

		@Override
		public void onCancelUpload() {
			if(mNewCardClickListener != null)
				mNewCardClickListener.onCancelUpload();
		}

		@Override
		public void onCurrentPosition() {
			if(mNewCardClickListener != null)
				mNewCardClickListener.onCurrentPosition();
		}
	};

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if(mItemAdapter.getCount() > 0)
			mChangeCardAnimBoj.draw(canvas);
	}

	// Animator Listener =============================
	private class CardObjectAnimator implements ValueAnimator.AnimatorUpdateListener {
		private static final int ANIM_UP = 0;
		private static final int ANIM_DOWN = 1;
		private int mAnim = ANIM_UP;
		
		public CardObjectAnimator(int anim) {
			if(mAnim == ANIM_DOWN) {
				mAnim = ANIM_DOWN;
			} else {
				mAnim = ANIM_UP;
			}
		}

		@Override
		public void onAnimationUpdate(ValueAnimator animation) {
			int y = (Integer)animation.getAnimatedValue();
			mChangeCardAnimBoj.setMoveY(y);
			mCard.setY(mCardDefaultPositionY + y);
			postInvalidate();
		}
	}
	
	// Key Event
	public boolean handlerKeyDown() {
		if(mAddLayout.isOpenSelect()) {
			mAddLayout.closeSelector();
			return true;
		}
		if(mMode == MODE_ALL) {
			// 全開模式遇到Back key則縮回去
			switchMode(MODE_ANIM_TO_SMALL_CARD);
			mEndY = mCardDefaultPositionY;
			ObjectAnimator obj = ObjectAnimator.ofFloat(this, "SwitchModeAnimation", 0.0f, 1.0f);
			obj.addListener(mSwitchModeAnimationListener);
			obj.setInterpolator(new  DecelerateInterpolator());
			obj.setDuration(MOVE_POSITION_ANIMATION);
			obj.start();
			return true;
		}
		return false;
	}

	private class ItemAdapter extends BaseAdapter {

		private ArrayList<ListItem> mAllPoints = new ArrayList<ListItem>();
		private ArrayList<ListItem> mItems = new ArrayList<ListItem>();
		private ListItem mSelectPoint = null;
		private LayoutInflater mInflater;
		public ItemAdapter(Context context) {
			mInflater = LayoutInflater.from(context);
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

			java.util.Collections.sort(mAllPoints, new Comparator<ListItem>() {

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
				ViewHost host = new ViewHost();
				host.mImageView = (ImageView) convertView.findViewById(R.id.card_icon);
				host.mMaskView = convertView.findViewById(R.id.card_mask);
				host.mTag = (TextView) convertView.findViewById(R.id.card_tag_list);
				GradientDrawable lightMaskDrawable;
				lightMaskDrawable = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
						new int[] { 0x00000000, 0x80000000 });
				lightMaskDrawable.setShape(GradientDrawable.RECTANGLE);
				host.mMaskView.setBackgroundDrawable(lightMaskDrawable);
				host.mDistance = (TextView) convertView.findViewById(R.id.card_distance);
				host.mUtil = (TextView) convertView.findViewById(R.id.card_unit);
				host.mAddress = (TextView) convertView.findViewById(R.id.card_address);
				host.mTitle = (TextView) convertView.findViewById(R.id.card_title_single);
				convertView.setTag(host);
			}

			final ViewHost host = (ViewHost) convertView.getTag();
			final ListItem item = mItems.get(position);
			
			String imagePath = null;
			host.mImageView.setImageResource(item.mPoint.getDefTypeResource());
			if(item.mPoint.getImageDetails().size() > 0) {
				if(TextUtils.isEmpty(imagePath))
					imagePath = item.mPoint.getImageDetails().get(0).getUrl();
				BitmapParameters params = BitmapParameters.getUrlBitmap(imagePath);
				params.mBitmapDownloaded = new BitmapDownloadedListener() {
					@Override
					public void onDownloadFaild(String url, ImageView icon,
							BitmapParameters params) {
					}
					
					@Override
					public void onDownloadComplete(String url, ImageView icon, Bitmap bmp,
							BitmapParameters params) {
					}
				};
				mBitmapLoader.getBitmap(host.mImageView, params, false);
			}
			
			host.mTitle.setText(item.mPoint.getTitle());
			int textSize = MBUtil.getTextSize(item.mPoint.getTitle(), 32, 20, mItemWidth);
			host.mTitle.setTextSize(TypedValue.COMPLEX_UNIT_DIP, textSize);

			if(item.mPoint.getTags().size() == 0) {
				host.mTag.setText("");
			} else {
				host.mTag.setText(item.mPoint.getTagsStringSpan(), TextView.BufferType.SPANNABLE);
			}
			
			if(mMyLocation != null) {
				DistanceObject disObject = Utils.getDistanceObject(
						item.mDistance);
				Utils.setDistanceToText(host.mDistance, disObject.mDistance);
				host.mUtil.setText(disObject.mUnit);
			} else {
				host.mDistance.setText("");
				host.mUtil.setText("");
			}

			host.mAddress.setText(item.mPoint.getLocation().getPlaceAddress());
			return convertView;
		}
	}

	private class ViewHost {
		public View mMaskView;
		public ImageView mImageView;
		public TextView mTag;
		public TextView mDistance;
		public TextView mUtil;
		public TextView mAddress;
		public TextView mTitle;
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
	
	// Progress
	public void setProgress(int state, int progress, int total) {
		mAddLayout.setProgress(state, progress, total);
	}

	// 回復到預設
	public void resetState() {
		
	}
}
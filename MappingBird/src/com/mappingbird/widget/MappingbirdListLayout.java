package com.mappingbird.widget;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.mappingbird.MappingBirdItem;
import com.mappingbird.R;
import com.mappingbird.api.Point;
import com.mappingbird.common.BitmapLoader;
import com.mappingbird.common.BitmapParameters;
import com.mappingbird.common.DeBug;
import com.mappingbird.common.Utils;

public class MappingbirdListLayout extends RelativeLayout {

	private static final int MODE_NONE = 4;
	private static final int MODE_ITEM = 0;
	private static final int MODE_DRAG_UP = 1;
	private static final int MODE_DRAG_DOWN = 3;
	private static final int MODE_LIST = 2;
	
	private int mTouchDownX = 0;
	private int mTouchDownY = 0;

	private int mMode = MODE_NONE;
	
	private int mDefaultHeight = 300;
	private int mFake1Height = 330;
	private int mFake2Height = 360;

	private ListView mListView;
	private View mItemfakeView1, mItemfakeView2;
	private ItemAdapter mItemAdapter;
	
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
		mListView = (ListView) findViewById(R.id.item_list);
		mListView.setOnItemClickListener(mListViewItemClickListener);
		mListView.setVisibility(View.GONE);
		mItemAdapter = new ItemAdapter(getContext());
		mListView.setAdapter(mItemAdapter);
		
		mItemfakeView1 = findViewById(R.id.item_back1);
		mItemfakeView2 = findViewById(R.id.item_back2);
		
		mBitmapLoader = new BitmapLoader(getContext());
		
		mDefaultHeight = (int)getResources().getDimension(R.dimen.place_show_item_card_height);
		mFake1Height = (int)getResources().getDimension(R.dimen.place_show_fake1_item_card_height);
		mFake2Height = (int)getResources().getDimension(R.dimen.place_show_fake2_item_card_height);
	}

	public void setMyLocation(LatLng location) {
		mMyLocation = location;
		if(mMyLocation != null) {
			mItemAdapter.countDistance();
		}
	}

	private void resetLayout() {
		switch(mMode) {
		case MODE_NONE:
			mListView.setVisibility(View.GONE);
			setListViewMarginTop(0);
			setFakeMarginTop();
			break;
		case MODE_ITEM:
			mListView.setVisibility(View.VISIBLE);
			setListViewMarginTop(0);
			break;
		case MODE_LIST:
			mListView.setVisibility(View.VISIBLE);
			setListViewMarginTop(0);
			break;
		}
	}

	private void switchMode(int mode) {
		switch(mode) {
		case MODE_ITEM:
			switch(mMode) {
			case MODE_NONE:
				Animation anim = AnimationUtils.loadAnimation(getContext(), R.anim.layout_scroll_from_bottom_to_up);
				setListViewMarginTop(0);
				mListView.setAnimation(anim);
				mListView.setVisibility(View.VISIBLE);
				fakeViewAnimation(true);
				break;
			case MODE_DRAG_UP:
			case MODE_DRAG_DOWN:
				fakeViewAnimation(true);
				mMode = MODE_ITEM;
				resetLayout();
				break;
			}
			mMode = mode;
			break;
		case MODE_DRAG_UP:
			if(mMode == MODE_ITEM) {
				fakeViewAnimation(false);
			}
			mMode = MODE_DRAG_UP;
			break;
		case MODE_DRAG_DOWN:
			mMode = MODE_DRAG_DOWN;
			break;
		case MODE_LIST:
			mMode = MODE_LIST;
			resetLayout();
			break;
		case MODE_NONE:
			if(mMode ==MODE_ITEM) {
				fakeViewAnimation(false);
				Animation anim = AnimationUtils.loadAnimation(getContext(), R.anim.layout_scroll_from_up_to_bottom);
				anim.setAnimationListener(mListViewScrollDownListener);
				mListView.setAnimation(anim);
				mListView.setVisibility(View.VISIBLE);
			}else if(mMode ==MODE_LIST) {
				Animation anim = AnimationUtils.loadAnimation(getContext(), R.anim.layout_scroll_from_up_to_bottom);
				anim.setAnimationListener(mListViewScrollDownListener);
				mListView.setAnimation(anim);
				mListView.setVisibility(View.VISIBLE);				
			}
			mMode = MODE_NONE;
			break;
		}
	}

	public void closeCardLayout() {
		switchMode(MODE_NONE);
	}

	private void setListViewMarginTop(int top) {
		if(mMode == MODE_NONE || mMode == MODE_ITEM || mMode == MODE_DRAG_UP) {
			int value = top >= 0 ? top : 0;
			RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mListView.getLayoutParams();
			lp.topMargin = getHeight() - mDefaultHeight - value;
			mListView.setLayoutParams(lp);
		} if(mMode == MODE_LIST || mMode == MODE_DRAG_DOWN) {
			int value = top >= 0 ? top : 0;
			RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mListView.getLayoutParams();
			lp.topMargin = value;
			mListView.setLayoutParams(lp);
		}
	}

	private void setFakeMarginTop() {
		RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mItemfakeView1.getLayoutParams();
		lp.topMargin = getHeight() - mFake1Height;
		lp.height = mFake1Height;
		mItemfakeView1.setLayoutParams(lp);
		lp = (RelativeLayout.LayoutParams) mItemfakeView2.getLayoutParams();
		lp.topMargin = getHeight() - mFake2Height;
		lp.height = mFake2Height;
		mItemfakeView2.setLayoutParams(lp);
	}

	private boolean isTouchListView(int x, int y) {
		if(mListView.getVisibility() != View.VISIBLE)
			return false;

		RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mListView.getLayoutParams();
		if(lp.topMargin < y) {
			return true;
		}
		
		return false;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		DeBug.v("ListLayout onTouchEvent");
		return super.onTouchEvent(event);
	}

	private OnItemClickListener mListViewItemClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			if(mCardClickListener != null) {
				mCardClickListener.onClickCard(mItemAdapter.getSelectPoint(position));
			}
		}
	};

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
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
		return super.dispatchTouchEvent(ev);
	}

	private void fakeViewAnimation(boolean visiable) {
		if(visiable) {
			Animation anim1 = AnimationUtils.loadAnimation(getContext(), R.anim.layout_fake_item_1_scroll_from_bottom_to_up);
			mItemfakeView1.setAnimation(anim1);
			mItemfakeView1.setVisibility(View.VISIBLE);
			Animation anim2 = AnimationUtils.loadAnimation(getContext(), R.anim.layout_fake_item_2_scroll_from_bottom_to_up);
			mItemfakeView2.setAnimation(anim2);
			mItemfakeView2.setVisibility(View.VISIBLE);			
		} else {
			Animation anim1 = AnimationUtils.loadAnimation(getContext(), R.anim.layout_fake_item_1_scroll_from_up_to_bottom);
			anim1.setAnimationListener(mFake1MoveDownListener);
			mItemfakeView1.setAnimation(anim1);
			mItemfakeView1.setVisibility(View.VISIBLE);
			Animation anim2 = AnimationUtils.loadAnimation(getContext(), R.anim.layout_fake_item_2_scroll_from_up_to_bottom);
			anim2.setAnimationListener(mFake2MoveDownListener);
			mItemfakeView2.setAnimation(anim2);
			mItemfakeView2.setVisibility(View.VISIBLE);						
		}
	}


	public void setPositionData(ArrayList<Point> items) {
		mItemAdapter.setItem(items);
		resetLayout();
	}

	public void clickItem(MappingBirdItem item) {
		mItemAdapter.clickItem(item);
		switchMode(MODE_ITEM);
	}

	private AnimationListener mListViewScrollDownListener = new AnimationListener() {
		
		@Override
		public void onAnimationStart(Animation animation) {
		}
		
		@Override
		public void onAnimationRepeat(Animation animation) {
		}
		
		@Override
		public void onAnimationEnd(Animation animation) {
			resetLayout();
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
			mItemfakeView1.setVisibility(View.GONE);
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
			mItemfakeView2.setVisibility(View.GONE);
		}
	};

	public void setCardClickListener(CardClickListener listener) {
		mCardClickListener = listener;
	}

	public interface CardClickListener {
		public void onClickCard(Point point);
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

		public synchronized void setItem(ArrayList<Point> items) {
			mAllPoints.clear();
			for(Point item : items) {
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

		public Point getSelectPoint(int index) {
			if(index >= mItems.size())
				return null;
			return mItems.get(index).mPoint;
		}
		
		public Point getSelectPoint() {
			if(mSelectPoint == null)
				return null;
			return mSelectPoint.mPoint;
		}

		public synchronized void clickItem(MappingBirdItem item) {
			mItems.clear();
			for(ListItem point : mAllPoints) {
				if(point.equals(item.mPosition)) {
					DeBug.d("Click item : "+item.mTitle);
					mSelectPoint = point;
					mItems.add(0, point);
				} else {
					mItems.add(point);
				}
			}
			notifyDataSetChanged();
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
				convertView = mInflater.inflate(R.layout.mappingbird_card, parent, false);
			}
			ListItem item = mItems.get(position);
			ImageView image = (ImageView) convertView.findViewById(R.id.card_icon);
			TextView title = (TextView) convertView.findViewById(R.id.card_title);
			String imagePath = null;
			if(item.mPoint.getImageDetails().size() > 0) {
				imagePath = item.mPoint.getImageDetails().get(0).getThumbPath();
				if(TextUtils.isEmpty(imagePath))
					imagePath = item.mPoint.getImageDetails().get(0).getUrl();
				BitmapParameters params = BitmapParameters.getUrlBitmap(imagePath);
				mBitmapLoader.getBitmap(image, params);
			} else {
				image.setImageDrawable(null);
			}

			title.setText(item.mPoint.getTitle());
			
			TextView dis = (TextView) convertView.findViewById(R.id.card_distance);
			// dis 
			if(mMyLocation != null) {
				dis.setText(
						Utils.getDistanceString(
								item.mDistance));
			} else {
				dis.setText("");
			}
			return convertView;
		}
	}

	private class ListItem {
		final Point mPoint;
		float mDistance = 0;
		public ListItem(Point point){
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
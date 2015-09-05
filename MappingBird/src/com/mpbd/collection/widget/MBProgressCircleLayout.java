package com.mpbd.collection.widget;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.mpbd.mappingbird.R;

public class MBProgressCircleLayout extends RelativeLayout {

	public static final int MODE_NORMAL = 0;
	public static final int MODE_PROGRESS = 1;
	private static final int ANGLE_TO_DURATION = 10;
	private RectF mOvalRect = null;
	private Paint mProgressCirclePaint;
	private float mRadio;
	private ValueAnimator mAnimator = null;
	private int mProcress = 0;
	private int mTotalProgress = 0;
	private float mCurrentAngle = 0;
	private float mTargeAngle = 0;
	private static final float ANGLE_START = -90;
	
	private int mMode = MODE_NORMAL;
	private ProgressListener mListener;
	
	public interface ProgressListener {
		public void progressFinished();
	}

	public MBProgressCircleLayout(Context context) {
		super(context);
	}

	public MBProgressCircleLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public MBProgressCircleLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		mProgressCirclePaint = new Paint();
		mProgressCirclePaint.setAntiAlias(true);
		mProgressCirclePaint.setColor(getResources().getColor(R.color.graphic_blue));
		mProgressCirclePaint.setStrokeWidth(getResources().getDimension(R.dimen.progress_circle_width));
		mProgressCirclePaint.setStyle(Style.STROKE);

		mRadio = getResources().getDimension(R.dimen.progress_circle_radio);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if(mMode == MODE_PROGRESS) {
			if(mOvalRect == null) {
				float centerX = getWidth()/2;
				float centerY = getHeight()/2;
				mOvalRect = new RectF(centerX - mRadio, centerY - mRadio, centerX + mRadio, centerY + mRadio);
			}
			canvas.drawArc(mOvalRect, mCurrentAngle + ANGLE_START, (360-mCurrentAngle), false, mProgressCirclePaint);
		}
	}

	public void setMode(int mode) {
		if(mode == MODE_PROGRESS) {
			mMode = MODE_PROGRESS;
		} else {
			mMode = MODE_NORMAL;
		}
	}

	/**
	 * 清除 progress的行為
	 */
	public void cleanProgress() {
		if(mAnimator != null) {
			mAnimator.cancel();
			mAnimator = null;
		}

		mProcress = 0;
		mTotalProgress = 0;
		mTargeAngle = 0;
		mCurrentAngle = 0;
		postInvalidate();
	}

	/**
	 * 啟動動畫
	 * @param progress
	 * @param total
	 */
	public void startProcress(int progress, int total) {
//		if(DeBug.DEBUG) {
//			DeBug.v(MBPlaceSubmitUtil.ADD_TAG, "[MBProgressCircleLayout] setProgress, progress = "+progress
//					+", total = "+total);
//			DeBug.v(MBPlaceSubmitUtil.ADD_TAG, "[MBProgressCircleLayout] setProgress, mProcress = "+mProcress
//					+", mTotalProgress = "+mTotalProgress);
//		}

		// 確認 progress小於Total
		if(progress > total) {
			// Error : progress不能大於 total
			return;
		}
		// 確認輸入的progress有比上次多.
		if(mProcress > progress) {
			// Error progress是遞增不會減少
			return;
		}
		
		float newTargetAngle = (int)(360 * (progress / (1.0f * total)));
		if(newTargetAngle < mTargeAngle) {
			// Error : 最後的Angle不會變小
			return;
		}
		// 當有動畫時先停止
		if(mAnimator != null) {
			mAnimator.cancel();
			mAnimator = null;
		}
		
		mProcress = progress;
		mTotalProgress = total;
		
		mTargeAngle = newTargetAngle;
		
		mAnimator = ValueAnimator.ofFloat(mCurrentAngle, mTargeAngle);
		mAnimator.setDuration((int)((mTargeAngle - mCurrentAngle)*ANGLE_TO_DURATION));
		mAnimator.addListener(mAnimatorListener);
		mAnimator.addUpdateListener(new AnimatorUpdateListener() {
			
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				float value = (Float) animation.getAnimatedValue();
				mCurrentAngle = value;
				postInvalidate();
			}
		});
		mAnimator.start();
	}

	private Animator.AnimatorListener mAnimatorListener = new AnimatorListener() {
		
		@Override
		public void onAnimationStart(Animator animation) {
		}
		
		@Override
		public void onAnimationRepeat(Animator animation) {
		}
		
		@Override
		public void onAnimationEnd(Animator animation) {
			if(mProcress == mTotalProgress && 
					mListener != null) {
				mListener.progressFinished();
				mMode = MODE_NORMAL;
				mProcress = 0;
				mTotalProgress = 0;
				mTargeAngle = 0;
				mCurrentAngle = 0;
			}
		}
		
		@Override
		public void onAnimationCancel(Animator animation) {
		}
	};

	public void setProgressListener(ProgressListener listener) {
		mListener = listener;
	}
}
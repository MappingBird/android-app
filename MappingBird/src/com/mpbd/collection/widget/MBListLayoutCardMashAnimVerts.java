package com.mpbd.collection.widget;

import android.graphics.Bitmap;


public class MBListLayoutCardMashAnimVerts {
	private Bitmap mBmp = null;
	public final static int MATRIX_COLUME = 7;
	public final static int MATRIX_ROW = 7;
	
	
	
	private final static float LEFT_PADDING = 0.08f;
	private final static float RIGHT_PADDING = 1 - LEFT_PADDING;
	
	public final static float RATE_HEIGHT = 0.9f;
	
	private final static float NEXT_LEFT_PADDING = 0.08f;
	private final static float NEXT_RATE_HEIGHT = 0.9f;
	private float[] mPreviousVerts = null;
	private float[] mCenterVerts = null;
	private float[] mNextVerts = null;

	public MBListLayoutCardMashAnimVerts() {
		buildVerts();
	}

	public boolean isInit() {
		return mPreviousVerts != null && mCenterVerts != null && mNextVerts != null;
	}

	private void buildVerts() {
		mPreviousVerts = new float[MATRIX_COLUME*MATRIX_ROW*2];
		mCenterVerts = new float[MATRIX_COLUME*MATRIX_ROW*2];
		mNextVerts = new float[MATRIX_COLUME*MATRIX_ROW*2];

		// 算值
		float left = 0;
		float right = 0;
		float yRate = 0;
		float next_left = 0;
		float next_right = 0;
		for(int j = 0; j < MATRIX_ROW ; j++) {
			yRate = (j / (float)(MATRIX_ROW-1));
			left = LEFT_PADDING * (1 - yRate);
			right = 1 - left;
			next_left = - NEXT_LEFT_PADDING * (1 - yRate);
			next_right = 1 - next_left;

			for(int i = 0 ; i < MATRIX_COLUME ; i++) {
				mPreviousVerts[(j*MATRIX_COLUME + i)*2] = (right - left) * (i/(float)(MATRIX_COLUME-1)) + left;
				mPreviousVerts[(j*MATRIX_COLUME + i)*2 + 1] = yRate * RATE_HEIGHT;
				
				mCenterVerts[(j*MATRIX_COLUME + i)*2] = (i/(float)(MATRIX_COLUME-1));
				mCenterVerts[(j*MATRIX_COLUME + i)*2 + 1] = yRate;

				mNextVerts[(j*MATRIX_COLUME + i)*2] = (next_right - next_left) * (i/(float)(MATRIX_COLUME-1)) + next_left;
				mNextVerts[(j*MATRIX_COLUME + i)*2 + 1] = yRate * NEXT_RATE_HEIGHT;
			}
		}
	}

	public float[] getPreviousVerts() {
		return mPreviousVerts;
	}

	public float[] getCenterVerts() {
		return mCenterVerts;
	}

	public float[] getNextVerts() {
		return mNextVerts;
	}
}
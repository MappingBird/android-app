package com.mpbd.mappingbird.common;

import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;



public class MBAnimation {

	public static AnimationSet getActionBtnShowAnimation() {
		AlphaAnimation alpha = new AlphaAnimation(0f, 1.0f);
		alpha.setDuration(150);
		
		ScaleAnimation scale1 = new ScaleAnimation(0.5f, 1.25f, 0.5f, 1.25f, 
				ScaleAnimation.RELATIVE_TO_SELF, 0.5f, ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
		scale1.setDuration(200);
		
		ScaleAnimation scale2 = new ScaleAnimation(1.0f, 0.8f, 1.0f, 0.8f, 
				ScaleAnimation.RELATIVE_TO_SELF, 0.5f, ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
		scale2.setDuration(200);
		scale2.setStartOffset(200);
		
		AnimationSet set = new AnimationSet(false);
		set.addAnimation(alpha);
		set.addAnimation(scale1);
		set.addAnimation(scale2);
		return set;
	}

	public static AnimationSet getActionBtnHideAnimation() {
		AlphaAnimation alpha = new AlphaAnimation(1.0f, 0f);
		alpha.setDuration(200);
		
		ScaleAnimation scale1 = new ScaleAnimation(1.0f, 0.4f, 1.0f, 0.4f, 
				ScaleAnimation.RELATIVE_TO_SELF, 0.5f, ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
		scale1.setDuration(300);
				
		AnimationSet set = new AnimationSet(false);
		set.addAnimation(alpha);
		set.addAnimation(scale1);
		return set;
	}
}

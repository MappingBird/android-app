package com.mpbd.mappingbird;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface.OnClickListener;
import android.graphics.drawable.AnimationDrawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.mappingbird.api.MappingBirdAPI;

public class MappingBirdDialog {

	public static Dialog createLoadingDialog(Context context, String message,
			boolean isLoading) {

		Dialog dialog = new Dialog(context, android.R.style.Theme_Translucent_NoTitleBar);
		dialog.setContentView(R.layout.mappingbird_dailog);

		TextView msg = (TextView) dialog.findViewById(R.id.message);
		View image = dialog.findViewById(R.id.image);

		if (message != null) {
			msg.setVisibility(View.VISIBLE);
			msg.setText(message);
		} else {
			msg.setVisibility(View.GONE);
		}

		image.setVisibility(View.VISIBLE);
//		image.setImageResource(R.anim.loading_animation_white);
//		AnimationDrawable animationDrawable = (AnimationDrawable) image
//				.getDrawable();
//
//		animationDrawable.start();

		return dialog;

	}

	public static Dialog createMessageDialog(Context context, String title,
			String message,
			String positiveText, OnClickListener positiveListener,
			String negativeText, OnClickListener negativeListener) {

		Builder builder = new AlertDialog.Builder(context);

		if (title != null)
			builder.setTitle(title);
		builder.setMessage(message);

		if (positiveText != null)
			builder.setPositiveButton(positiveText, positiveListener);
		if (negativeText != null)
			builder.setNegativeButton(negativeText, negativeListener);
		return builder.create();
	}
}

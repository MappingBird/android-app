package com.mappingbird;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface.OnClickListener;
import android.graphics.drawable.AnimationDrawable;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.mappingbird.api.MappingBirdAPI;

class MappingBirdDialog {
	private static Dialog mDialog = null;

	public static Dialog createMessageDialog(Context context, String message,
			boolean isLoading) {

		mDialog = new Dialog(context, R.style.LoadingDialog);
		mDialog.setContentView(R.layout.mappingbird_dailog);

		TextView msg = (TextView) mDialog.findViewById(R.id.message);
		ImageView image = (ImageView) mDialog.findViewById(R.id.image);

		if (message != null) {
			msg.setVisibility(View.VISIBLE);
			msg.setText(message);
		} else {
			msg.setVisibility(View.GONE);
		}

		if (isLoading) {
			image.setVisibility(View.VISIBLE);
			image.setBackgroundResource(R.anim.loading_animation_white);
			AnimationDrawable animationDrawable = (AnimationDrawable) image
					.getBackground();

			animationDrawable.start();
		} else {
			image.setVisibility(View.GONE);
		}

		return mDialog;

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

	public static String setError(int statusCode, Context context) {
		String error = "";

		if (statusCode == MappingBirdAPI.RESULT_INTERNAL_ERROR) {
			error = context.getResources().getString(R.string.internal_error);
		} else if (statusCode == MappingBirdAPI.RESULT_NETWORK_ERROR) {
			error = context.getResources().getString(R.string.network_error);
		} else {
			error = context.getResources().getString(R.string.unknow_error);
		}
		return error;
	}

}

package com.mpbd.mappingbird;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface.OnClickListener;
import android.graphics.drawable.AnimationDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.mappingbird.api.MappingBirdAPI;
import com.mappingbird.widget.MappingbirdSelectPleaceKindLayout;

public class MappingBirdDialog {

	public static Dialog createLoadingDialog(Context context, String message,
			boolean isLoading) {

		Dialog dialog = new Dialog(context, android.R.style.Theme_Translucent_NoTitleBar);
		dialog.setContentView(R.layout.mappingbird_dailog);

		TextView msg = (TextView) dialog.findViewById(R.id.message);
		ImageView image = (ImageView) dialog.findViewById(R.id.image);

		if (message != null) {
			msg.setVisibility(View.VISIBLE);
			msg.setText(message);
		} else {
			msg.setVisibility(View.GONE);
		}

		image.setVisibility(View.VISIBLE);
		image.setImageResource(R.anim.loading_animation_white);
		AnimationDrawable animationDrawable = (AnimationDrawable) image
				.getDrawable();

		animationDrawable.start();

		return dialog;

	}

	public static Dialog createSelectPlaceKindDialog(Context context, int width, int height,
			double latitude, double longitude, ArrayList<String> list) {

		Dialog dialog = new Dialog(context, android.R.style.Theme_Translucent_NoTitleBar);
		LayoutInflater inflater = LayoutInflater.from(context);
		MappingbirdSelectPleaceKindLayout layout = (MappingbirdSelectPleaceKindLayout)
				inflater.inflate(R.layout.dialog_select_place_kind_layout,
						null, false);
		layout.setCollection(list);
		layout.initView(width, height);
		layout.setLocation(latitude, longitude);
		dialog.setContentView(layout);
		layout.startAnimation();
		layout.setDialig(dialog);
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
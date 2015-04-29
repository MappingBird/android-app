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

	public static Dialog createLoadingDialog(Context context) {

		Dialog dialog = new Dialog(context, android.R.style.Theme_Translucent_NoTitleBar);
		dialog.setContentView(R.layout.mb_dailog_loading);
		View image = dialog.findViewById(R.id.image);
		image.setVisibility(View.VISIBLE);
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

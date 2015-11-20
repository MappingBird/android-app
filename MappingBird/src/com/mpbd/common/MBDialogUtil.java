package com.mpbd.common;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface.OnClickListener;
import android.view.View;

import com.mpbd.mappingbird.R;

public class MBDialogUtil {

	public static Dialog createLoadingDialog(Context context) {

		Dialog dialog = new Dialog(context, android.R.style.Theme_Translucent_NoTitleBar);
		dialog.setContentView(R.layout.mb_dailog_loading);
		View image = dialog.findViewById(R.id.image);
		image.setVisibility(View.VISIBLE);
		return dialog;

	}
}

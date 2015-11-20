package com.mpbd.common;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.mpbd.mappingbird.R;

public class MBToast {

	public static Toast getToast(Context context, String msg) {
		LayoutInflater inflater = LayoutInflater.from(context);
		View layout = inflater.inflate(R.layout.mb_toast_layout, null);
		TextView text = (TextView) layout.findViewById(R.id.toast_text);
		text.setText(msg);
		
		Toast toast = new Toast(context);
		toast.setView(layout);
		return toast;
	}
}

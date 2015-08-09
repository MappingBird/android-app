package com.mpbd.mappingbird.common;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface.OnDismissListener;
import android.content.DialogInterface.OnKeyListener;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mappingbird.common.MappingBirdApplication;
import com.mpbd.mappingbird.R;
import com.mpbd.mappingbird.util.MBUtil;


public class MBDialog {
	public Dialog mDialog = null;
	
	// UI
	private TextView mDialogTitle;
	private TextView mDialogDescription;
	private TextView mDialogNegativeBtn;
	private TextView mDialogPositiveBtn;
	private View mDialogTitlePaddingBottom;
	private View mDialogDivider;
	private FrameLayout mContextLayout;

	// Style
	public static final int BTN_STYLE_DEFAULT = 0;
	public static final int BTN_STYLE_BLUE = 1;
	public MBDialog(Context context) {
		
		initView(context);
	}
	
	private void initView(Context context) {
		View view = (LayoutInflater.from(context)).inflate(R.layout.mb_dialog, null);

		if(view != null) {
			mDialogTitle = (TextView) view.findViewById(R.id.dialog_title);
			mDialogTitlePaddingBottom = view.findViewById(R.id.dialog_title_padding_bottom);
			mDialogDescription = (TextView) view.findViewById(R.id.dialog_description);
			mDialogNegativeBtn = (TextView) view.findViewById(R.id.dialog_negative);
			mDialogPositiveBtn = (TextView) view.findViewById(R.id.dialog_positive);
			mDialogDivider = view.findViewById(R.id.dialog_divider);
			mContextLayout = (FrameLayout) view.findViewById(R.id.dialog_content_layout);
			mDialog = new Dialog(context, R.style.MBDialog);
			setDialogSize(MBUtil.getWindowWidth(context), 0);
			mDialog.setContentView(view);
		}
	}

	private void setDialogSize(int width, int height) {
		try {
		if(mDialog != null) {
			Window window = mDialog.getWindow();
			WindowManager.LayoutParams lp = window.getAttributes();
			lp.width = width;
			if(height > 0)
				lp.height = height;
			window.setAttributes(lp);
		}
		} catch(Exception e) {
			
		}
		if(mDialogTitle != null) {
			LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams)mDialogTitle.getLayoutParams();
			lp.width = width;
			mDialogTitle.setLayoutParams(lp);
		}
	}

	public void setTitle(String title) {
		if(mDialogTitle != null)
			mDialogTitle.setText(title);
	}

	public void setDescription(String description) {
		if(mDialogDescription != null) {
			mDialogTitlePaddingBottom.setVisibility(View.GONE);
			mDialogDescription.setVisibility(View.VISIBLE);
			mDialogDescription.setText(description);
		}
	}

	public void setDescription(SpannableString description) {
		if(mDialogDescription != null) {
			mDialogTitlePaddingBottom.setVisibility(View.GONE);
			mDialogDescription.setVisibility(View.VISIBLE);
			mDialogDescription.setText(description);
		}
	}

	public void setView(View view) {
		if(view != null) {
			mContextLayout.removeAllViews();
			mContextLayout.addView(view);
			mContextLayout.setVisibility(View.VISIBLE);
			mDialogDescription.setPadding(
					(int)MappingBirdApplication.instance().getResources().getDimension(R.dimen.dialog_padding_left),
					(int)MappingBirdApplication.instance().getResources().getDimension(R.dimen.dialog_description_padding_top),
					(int)MappingBirdApplication.instance().getResources().getDimension(R.dimen.dialog_padding_left),
					0);
		}
	}
	public void setNegativeBtn(String text, OnClickListener listener, int style) {
		if(mDialogNegativeBtn != null) {
			mDialogNegativeBtn.setVisibility(View.VISIBLE);
			mDialogNegativeBtn.setOnClickListener(listener);
			mDialogNegativeBtn.setText(text);
			mDialogPositiveBtn.setAllCaps(true);
			setBtnStyle(mDialogNegativeBtn, style);
			if(mDialogPositiveBtn.getVisibility() == View.VISIBLE) {
				mDialogDivider.setVisibility(View.VISIBLE);
			}
		}
	}

	public void setPositiveBtn(String text, OnClickListener listener, int style) {
		if(mDialogPositiveBtn != null) {
			mDialogPositiveBtn.setVisibility(View.VISIBLE);
			mDialogPositiveBtn.setOnClickListener(listener);
			mDialogPositiveBtn.setText(text);
			mDialogPositiveBtn.setAllCaps(true);
			setBtnStyle(mDialogPositiveBtn, style);
			if(mDialogNegativeBtn.getVisibility() == View.VISIBLE) {
				mDialogDivider.setVisibility(View.VISIBLE);
			}
		}
	}

	private void setBtnStyle(TextView btn, int style) {
		switch(style) {
			case BTN_STYLE_BLUE:
				btn.setTextColor(
						MappingBirdApplication.instance().getResources().getColor(R.color.font_deep_blue));
				btn.setTypeface(Typeface.DEFAULT_BOLD);
				break;
		}
	}
	public void show() {
		if(mDialog != null) {
			mDialog.show();
		}
	}

	public void dismiss() {
		if(mDialog != null)
			mDialog.dismiss();
		
		mDialog = null;
	}

	public boolean isShowing() {
		if(mDialog != null)
			return mDialog.isShowing();
		return false;
	}

	public void setCanceledOnTouchOutside(boolean cancel) {
		if(mDialog != null)
			mDialog.setCanceledOnTouchOutside(cancel);
	}

	public void setOnDismissListener(OnDismissListener listener) {
		if(mDialog != null)
			mDialog.setOnDismissListener(listener);
	}

	public void setOnKeyListener(OnKeyListener listener) {
		if(mDialog != null)
			mDialog.setOnKeyListener(listener);
	}
}

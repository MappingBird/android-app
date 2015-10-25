package com.mpbd.saveplace;

import java.util.ArrayList;

import android.graphics.Point;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;

import com.mappingbird.common.MappingBirdApplication;
import com.mappingbird.common.MappingBirdPref;
import com.mpbd.collection.widget.MBSpannBackground;
import com.mpbd.mappingbird.R;

public class MBSavePlaceUtil {
//	public static SpannableString parseTagString(String str) {
//		String[] sp = str.split(" ");
//		SpannableString ss = new SpannableString(str);
//		int index = 0;
//		for (String st : sp) {
//			ss.setSpan(new MBSpannBackground(
//					MappingBirdApplication.instance().getResources().getColor(R.color.tag_background)), index,
//					index + st.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//			index += st.length() + 1;
//		}
//		return ss;
//	}
//
	public static void saveTagArray(String[] tags) {
		String tag_array = MappingBirdPref.getIns().getTagArray();
		ArrayList<String> list = new ArrayList<String>();
		for(String tag : tags) {
			if(tag_array.indexOf(tag+",") >= 0) {
				// 表示已經有了. 跳過.
			} else {
				list.add(tag);
			}
		}
		
		// 加入 Tag array
		for(String tag : list) {
			tag_array+=tag+",";
		}
		MappingBirdPref.getIns().setTagArray(tag_array);
	}

	public static Spannable getTagsStringSpan(String[] tags, int lineSpaceing) {
		String tag = "";
		if(tags.length == 0) {
			SpannableString ss = new SpannableString("");
			return ss;
		}
		
		ArrayList<Point> positionList = new ArrayList<Point>();
		
		for(int i = 0; i < tags.length; i++) {
			if(!TextUtils.isEmpty(tags[i])) {
				Point position = new Point();
				position.x = tag.length();
				tag += tags[i];
				position.y = tag.length();
				positionList.add(position);
				if(i < tags.length-1)
					tag += " ";
			}
		}

		SpannableString ss = new SpannableString(tag);
		if(positionList.size() > 0) {
			for(Point position : positionList)
				ss.setSpan(new MBSpannBackground(
						MappingBirdApplication.instance().getResources().getColor(R.color.tag_background), lineSpaceing),
						position.x, position.y, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		}

		return ss;
	}

}

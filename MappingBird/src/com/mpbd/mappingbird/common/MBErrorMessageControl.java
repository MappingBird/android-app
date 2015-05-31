package com.mpbd.mappingbird.common;

import android.content.Context;

import com.mappingbird.api.MappingBirdAPI;
import com.mpbd.mappingbird.R;

public class MBErrorMessageControl {

	public static String getErrorTitle(int statusCode, Context context) {
		String error = "";

		if (statusCode == MappingBirdAPI.RESULT_INTERNAL_ERROR) {
			error = context.getResources().getString(R.string.error_internal_title);
		} else if (statusCode == MappingBirdAPI.RESULT_NETWORK_ERROR) {
			error = context.getResources().getString(R.string.error_network_title);
	    } else if (statusCode == MappingBirdAPI.RESULT_SIGN_UP_ERROR_PASSWORD_NOT_MATCH) {
            error = context.getResources().getString(R.string.error_normal_title);
		} else {
			error = context.getResources().getString(R.string.error_unknow_title);
		}
		return error;
	}

	public static String getErrorMessage(int statusCode, Context context) {
		String error = "";

		if (statusCode == MappingBirdAPI.RESULT_INTERNAL_ERROR) {
			error = context.getResources().getString(R.string.error_internal_message);
		} else if (statusCode == MappingBirdAPI.RESULT_NETWORK_ERROR) {
			error = context.getResources().getString(R.string.error_network_message);
		} else if (statusCode == MappingBirdAPI.RESULT_LOGIN_ACCOUNT_ERROR) {
			error = context.getResources().getString(R.string.error_login_fail_accout);
		} else if (statusCode == MappingBirdAPI.RESULT_LOGIN_NETWORK_ERROR) {
			error = context.getResources().getString(R.string.error_login_fail_network);
        } else if (statusCode == MappingBirdAPI.RESULT_SIGN_UP_ERROR_PASSWORD_NOT_MATCH) {
            error = context.getResources().getString(R.string.error_sign_up_fail_password_not_match_message);			
		} else {
			error = context.getResources().getString(R.string.error_unknow_message);
		}
		return error;
	}

}

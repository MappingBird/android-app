package com.mappingbird.api;

import java.io.Serializable;

public class User implements Serializable{
	private static final String TAG = User.class.getName();
	private String mEmail = null;
	private String mToken = null;
	private long mId = -1;

	User(long id, String email, String token) {
		mEmail = email;
		mToken = token;
		mId = id;
	}
	
	public String getEmail() {
		return mEmail;
	}
	
	public String getToken() {
		return mToken;
	}
	
	public long getId() {
		return mId;
	}
}

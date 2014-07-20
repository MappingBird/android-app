package com.mappingbird.api;

import java.io.Serializable;

public class User implements Serializable{
	private String mEmail = null;
	private String mToken = null;

	User(int id, String email, String token) {
		mEmail = email;
		mToken = token;
	}
	
	public String getEmail() {
		return mEmail;
	}
}

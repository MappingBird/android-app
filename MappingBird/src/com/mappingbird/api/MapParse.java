package com.mappingbird.api;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

class MapParse {

	private static final String TAG = MapParse.class.getName();

	static JSONObject writeAccount(String email, String password)
			throws JSONException {
		JSONObject obj = new JSONObject();
		obj.put("email", email);
		obj.put("password", password);
		obj.put("token", 1);
		Log.i(TAG, "[login json] =" + obj.toString());
		return obj;
	}

	static User parseAccountResult(Context context, String rsp)
			throws JSONException {
		JSONObject obj = new JSONObject(rsp);
		String email = null;
		long id = -1;
		String token = null;
		User user = null;
		String error = null;

		if (!obj.has("error")) {

			token = obj.optString("token");

			JSONObject userobj = obj.getJSONObject("user");
			email = userobj.optString("email");
			id = userobj.optLong("id");
			user = new User(id, email, token);
			UserPrefs pref = new UserPrefs(context);
			pref.setUser(user);
		} else {
			error = obj.getString("error");
			Log.i(TAG, "[error] =" + error);
		}
		return user;
	}

	public static Collections parseCollectionsResult(Context mContext,
			String rsp) throws JSONException {

		Collections collections = new Collections();
		collections.clear();
		JSONObject obj = new JSONObject(rsp);
		long newestCollectionId = obj
				.optLong("most_recent_modified_collection");
		JSONArray array = obj.getJSONArray("collections");
		for (int i = 0; i < array.length(); i++) {
			Log.i(TAG, "[collection json] length=" + array.length() + ":" + i);
			JSONObject arrayobj = array.getJSONObject(i);
			long id = arrayobj.optLong("id");
			Log.i(TAG, "[collection json] id=" + id);
			String name = arrayobj.optString("name");
			Log.i(TAG, "[collection json] name=" + name);
			long userId = arrayobj.optLong("user");
			Log.i(TAG, "[collection json] userId=" + userId);
			String createTime = arrayobj.optString("create_time");
			Log.i(TAG, "[collection json] createTime=" + createTime);
			String updateTime = arrayobj.optString("update_time");
			Log.i(TAG, "[collection json] updateTime=" + updateTime);
			boolean isNewest = (newestCollectionId == id ? true : false);
			Log.i(TAG, "[collection json] isNewest=" + isNewest);
			ArrayList<Integer> points = new ArrayList<Integer>();
			points.clear();
			JSONArray pointarray = arrayobj.getJSONArray("points");
			for (int j = 0; j < pointarray.length(); j++) {
				int pid = (Integer) pointarray.get(j);
				Log.i(TAG, "[collection json] pid=" + pid);
				points.add(pid);
			}
			collections.add(new Collection(id, userId, name, createTime,
					updateTime, isNewest, points));
		}

		return collections;
	}

	public static Point parsePointsResult(Context mContext, String rsp)
			throws JSONException {

		JSONObject obj = new JSONObject(rsp);

		long pid = obj.optLong("id");
		Log.i(TAG, "[point json] pid=" + pid);

		String title = obj.optString("title");
		Log.i(TAG, "[point json] title=" + title);

		String url = obj.optString("url");
		Log.i(TAG, "[point json] url=" + url);

		String description = obj.optString("description");
		Log.i(TAG, "[point json] description=" + description);

		String place_name = obj.optString("place_name");
		Log.i(TAG, "[point json] place_name=" + place_name);

		String place_address = obj.optString("place_address");
		Log.i(TAG, "[point json] place_address=" + place_address);

		String place_phone = obj.optString("place_phone");
		Log.i(TAG, "[point json] place_phone=" + place_phone);

		String coordinates = obj.optString("coordinates");
		Log.i(TAG, "[point json] coordinates=" + coordinates);

		String type = obj.optString("type");
		Log.i(TAG, "[point json] type=" + type);

		long collectionId = obj.optLong("collection");
		Log.i(TAG, "[point json] collectionId=" + collectionId);

		String pcreateTime = obj.optString("create_time");
		Log.i(TAG, "[point json] createTime=" + pcreateTime);

		String pupdateTime = obj.optString("update_time");
		Log.i(TAG, "[point json] updateTime=" + pupdateTime);

		JSONObject location = obj.getJSONObject("location");

		long lId = location.optLong("id");
		Log.i(TAG, "[location json] lId=" + lId);

		String placename = location.optString("place_name");
		Log.i(TAG, "[location json] place_name=" + placename);

		String placeaddress = location.optString("place_address");
		Log.i(TAG, "[location json] place_address=" + placeaddress);

		String placephone = location.optString("place_phone");
		Log.i(TAG, "[location json] place_phone=" + placephone);

		String lcoordinates = location.optString("coordinates");
		Log.i(TAG, "[location json] coordinates=" + lcoordinates);

		String category = location.optString("category");
		Log.i(TAG, "[location json] category=" + category);

		String lcreateTime = location.optString("create_time");
		Log.i(TAG, "[location json] createTime=" + lcreateTime);

		String lupdateTime = location.optString("update_time");
		Log.i(TAG, "[location json] updateTime=" + lupdateTime);

		ArrayList<ImageDetail> images = new ArrayList<ImageDetail>();
		images.clear();
		JSONArray imagearray = obj.getJSONArray("images");
		for (int j = 0; j < imagearray.length(); j++) {
			JSONObject imagearrayobj = imagearray.getJSONObject(j);

			long iId = imagearrayobj.optLong("id");
			Log.i(TAG, "[image json] tId=" + iId);

			String iUrl = imagearrayobj.optString("url");
			Log.i(TAG, "[image json] Url=" + iUrl);//

			String thumb_path = imagearrayobj.optString("thumb_path");
			Log.i(TAG, "[image json] thumb_path=" + thumb_path);

			long iPointId = imagearrayobj.optLong("point");
			Log.i(TAG, "[image json] PointId=" + iPointId);

			String ct = imagearrayobj.optString("create_time");
			Log.i(TAG, "[image json] create_time=" + ct);

			String ut = imagearrayobj.optString("update_time");
			Log.i(TAG, "[image json] update_time=" + ut);
			images.add(new ImageDetail(iId, thumb_path, iUrl, ct, ut, iPointId));
		}

		ArrayList<Tag> tags = new ArrayList<Tag>();
		tags.clear();
		JSONArray tagarray = obj.getJSONArray("tags");
		for (int k = 0; k < tagarray.length(); k++) {
			JSONObject tagarrayobj = tagarray.getJSONObject(k);

			long tId = tagarrayobj.optLong("id");
			Log.i(TAG, "[tag json] tId=" + tId);

			String tName = tagarrayobj.optString("name");
			Log.i(TAG, "[tag json] name=" + tName);
			tags.add(new Tag(tId, tName));
		}

		return new Point(pid, title, url, type, description, place_address,
				place_phone, place_name, coordinates, images, tags,
				collectionId, new Location(lId, placeaddress, placephone,
						placename, lcoordinates, category, lcreateTime,
						lupdateTime), pcreateTime, pupdateTime);
	}

	public static Collection parseCollectionInfoResult(Context mContext,
			String rsp) throws JSONException {
		JSONObject obj = new JSONObject(rsp);
		long cid = obj.optLong("id");
		Log.i(TAG, "[collection info json] cid=" + cid);

		String name = obj.optString("name");
		Log.i(TAG, "[collection info json] name=" + name);

		long userId = obj.optLong("user");
		Log.i(TAG, "[collection info json] userId=" + userId);

		JSONArray array = obj.getJSONArray("points");
		ArrayList<Point> points = new ArrayList<Point>();
		points.clear();
		for (int i = 0; i < array.length(); i++) {
			Log.i(TAG, "[collection info json] length=" + array.length() + ":"
					+ i);
			JSONObject arrayobj = array.getJSONObject(i);
			long pid = arrayobj.optLong("id");
			Log.i(TAG, "[cp info json] pid=" + pid);

			String title = arrayobj.optString("title");
			Log.i(TAG, "[cp info json] title=" + title);

			String coordinates = arrayobj.optString("coordinates");
			Log.i(TAG, "[cp info json] coordinates=" + coordinates);

			String type = arrayobj.optString("type");
			Log.i(TAG, "[cp info json] title=" + type);

			ArrayList<ImageDetail> images = new ArrayList<ImageDetail>();
			images.clear();
			JSONArray imagearray = arrayobj.getJSONArray("images");

			for (int j = 0; j < imagearray.length(); j++) {
				JSONObject imagearrayobj = imagearray.getJSONObject(j);

				long iId = imagearrayobj.optLong("id");
				Log.i(TAG, "[ci json] tId=" + iId);

				String iUrl = imagearrayobj.optString("url");
				Log.i(TAG, "[ci json] Url=" + iUrl);//

				String thumb_path = imagearrayobj.optString("thumb_path");
				Log.i(TAG, "[ci json] thumb_path=" + thumb_path);
				images.add(new ImageDetail(iId, thumb_path, iUrl));
			}

			JSONObject location = arrayobj.getJSONObject("location");

			long lId = location.optLong("id");
			Log.i(TAG, "[cl json] lId=" + lId);

			String lcoordinates = location.optString("coordinates");
			Log.i(TAG, "[cl json] coordinates=" + lcoordinates);

			points.add(new Point(pid, title, coordinates, type, images,
					new Location(lId, lcoordinates)));
		}

		return new Collection(cid, userId, name, points);
	}

}

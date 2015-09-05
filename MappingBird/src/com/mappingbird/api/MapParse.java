package com.mappingbird.api;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.text.TextUtils;
import android.util.Base64;

import com.mappingbird.common.DeBug;
import com.mappingbird.common.MBLocation;

class MapParse {

	private static final String TAG = MapParse.class.getName();

	static JSONObject writeAccount(String email, String password)
			throws JSONException {
		JSONObject obj = new JSONObject();
		obj.put("email", email);
		obj.put("password", password);
		obj.put("token", 1);
		DeBug.i(TAG, "[login json] =" + obj.toString());
		return obj;
	}

	static JSONObject writeCollection(String name, long userId)
			throws JSONException {
		JSONObject obj = new JSONObject();
		obj.put("name", name);
		obj.put("user", userId);
		DeBug.i(TAG, "[collection json] =" + obj.toString());
		return obj;
	}

	static JSONObject writeImage(String placeId, byte[] image)
			throws JSONException {
		JSONObject obj = new JSONObject();
		obj.put("point", placeId);
		obj.put("media", Base64.encodeToString(image, Base64.NO_WRAP));
		DeBug.i(TAG, "[image json] =" + obj.toString());
		return obj;
	}

	static JSONObject writeImagePath(String placeId, String path)
			throws JSONException {
		JSONObject obj = new JSONObject();
		try {
			obj.put("point", placeId);
			obj.put("thumb_path", "");
			obj.put("url", URLEncoder.encode(path, "utf-8"));			
			DeBug.i(TAG, "[image path json] =" + obj.toString());
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return obj;
	}

	static JSONObject writePlace(String title, String tags,
			String url, String description, String placeName,
			String placeAddress, String placePhone, String lat, String lng,
			String type, long collectionId) throws JSONException {
		JSONObject obj = new JSONObject();
		if (title != null)
			obj.put("title", title);
		if (tags != null) {
			obj.put("tags", tags);
		}
		if (url != null)
			obj.put("url", url);
		if (description != null)
			obj.put("description", description);
		if (placeName != null)
			obj.put("place_name", placeName);
		if (placeAddress != null)
			obj.put("place_address", placeAddress);
		if (placePhone != null)
			obj.put("place_phone", placePhone);
		if (type != null)
			obj.put("type", type);
		obj.put("collection", collectionId);
		String coordinates = lat + "," + lng;
		obj.put("coordinates", coordinates);
		DeBug.i(TAG, "[place json] =" + obj.toString());
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
			if(TextUtils.isEmpty(token))
				return null;

			JSONObject userobj = obj.getJSONObject("user");
			email = userobj.optString("email");
			id = userobj.optLong("id");
			user = new User(id, email, token);
			UserPrefs pref = new UserPrefs(context);
			pref.setUser(user);
		} else {
			error = obj.getString("error");
			DeBug.i(TAG, "[error] =" + error);
		}
		return user;
	}

	public static MBCollectionList parseCollectionsResult(String rsp)
			throws JSONException {

		MBCollectionList collections = new MBCollectionList();
		collections.clear();
		JSONObject obj = new JSONObject(rsp);
		long newestCollectionId = obj
				.optLong("most_recent_modified_collection");
		JSONArray array = obj.getJSONArray("collections");
		for (int i = 0; i < array.length(); i++) {
			DeBug.i(TAG, "[collection json] length=" + array.length() + ":" + i);
			JSONObject arrayobj = array.getJSONObject(i);
			long id = arrayobj.optLong("id");
			DeBug.i(TAG, "[collection json] id=" + id);
			String name = arrayobj.optString("name");
			DeBug.i(TAG, "[collection json] name=" + name);
			long userId = arrayobj.optLong("user");
			DeBug.i(TAG, "[collection json] userId=" + userId);
			String createTime = arrayobj.optString("create_time");
			DeBug.i(TAG, "[collection json] createTime=" + createTime);
			String updateTime = arrayobj.optString("update_time");
			DeBug.i(TAG, "[collection json] updateTime=" + updateTime);
			boolean isNewest = (newestCollectionId == id ? true : false);
			DeBug.i(TAG, "[collection json] isNewest=" + isNewest);
			ArrayList<Integer> points = new ArrayList<Integer>();
			points.clear();
			JSONArray pointarray = arrayobj.getJSONArray("points");
			for (int j = 0; j < pointarray.length(); j++) {
				int pid = (Integer) pointarray.get(j);
				DeBug.i(TAG, "[collection json] pid=" + pid);
				points.add(pid);
			}
			collections.add(new MBCollectionItem(id, userId, name, createTime,
					updateTime, isNewest, points));
		}

		return collections;
	}

	public static MBPointData parseAddPointsResult(String rsp) 
			throws JSONException {
		JSONObject obj = new JSONObject(rsp);

		long pid = obj.optLong("id");
		DeBug.i(TAG, "[add point json] pid=" + pid);

		String title = obj.optString("title");
		DeBug.i(TAG, "[add point json] title=" + title);

		String url = obj.optString("url");
		DeBug.i(TAG, "[add point json] url=" + url);

		String description = obj.optString("description");
		DeBug.i(TAG, "[add point json] description=" + description);
		DeBug.i(TAG, "[add point json] description index =" + description.indexOf("\n"));

		String place_name = obj.optString("place_name");
		DeBug.i(TAG, "[add point json] place_name=" + place_name);

		String place_address = obj.optString("place_address");
		DeBug.i(TAG, "[add point json] place_address=" + place_address);

		String place_phone = obj.optString("place_phone");
		DeBug.i(TAG, "[add point json] place_phone=" + place_phone);

		String coordinates = obj.optString("coordinates");
		DeBug.i(TAG, "[add point json] coordinates=" + coordinates);

		String type = obj.optString("type");
		DeBug.i(TAG, "[add point json] type=" + type);

		long collectionId = obj.optLong("collection");
		DeBug.i(TAG, "[add point json] collectionId=" + collectionId);

		String pcreateTime = obj.optString("create_time");
		DeBug.i(TAG, "[add point json] createTime=" + pcreateTime);

		String pupdateTime = obj.optString("update_time");
		DeBug.i(TAG, "[add point json] updateTime=" + pupdateTime);

		return new MBPointData(pid, title, url, type, description,
				place_address, place_phone, place_name, coordinates, null,
				null, collectionId, null, pcreateTime, pupdateTime);
	}

	public static MBPointData parsePointsResult(String rsp)
			throws JSONException {

		JSONObject obj = new JSONObject(rsp);

		long pid = obj.optLong("id");
		DeBug.i(TAG, "[point json] pid=" + pid);

		String title = obj.optString("title");
		DeBug.i(TAG, "[point json] title=" + title);

		String url = obj.optString("url");
		DeBug.i(TAG, "[point json] url=" + url);

		String description = obj.optString("description");
		DeBug.i(TAG, "[point json] description=" + description);
		DeBug.i(TAG, "[point json] description index =" + description.indexOf("\n"));

		String place_name = obj.optString("place_name");
		DeBug.i(TAG, "[point json] place_name=" + place_name);

		String place_address = obj.optString("place_address");
		DeBug.i(TAG, "[point json] place_address=" + place_address);

		String place_phone = obj.optString("place_phone");
		DeBug.i(TAG, "[point json] place_phone=" + place_phone);

		String coordinates = obj.optString("coordinates");
		DeBug.i(TAG, "[point json] coordinates=" + coordinates);

		String type = obj.optString("type");
		DeBug.i(TAG, "[point json] type=" + type);

		long collectionId = obj.optLong("collection");
		DeBug.i(TAG, "[point json] collectionId=" + collectionId);

		String pcreateTime = obj.optString("create_time");
		DeBug.i(TAG, "[point json] createTime=" + pcreateTime);

		String pupdateTime = obj.optString("update_time");
		DeBug.i(TAG, "[point json] updateTime=" + pupdateTime);

		JSONObject location = obj.getJSONObject("location");

		long lId = location.optLong("id");
		DeBug.i(TAG, "[location json] lId=" + lId);

		String placename = location.optString("place_name");
		DeBug.i(TAG, "[location json] place_name=" + placename);

		String placeaddress = location.optString("place_address");
		DeBug.i(TAG, "[location json] place_address=" + placeaddress);

		String placephone = location.optString("place_phone");
		DeBug.i(TAG, "[location json] place_phone=" + placephone);

		String lcoordinates = location.optString("coordinates");
		DeBug.i(TAG, "[location json] coordinates=" + lcoordinates);

		String category = location.optString("category");
		DeBug.i(TAG, "[location json] category=" + category);

		String lcreateTime = location.optString("create_time");
		DeBug.i(TAG, "[location json] createTime=" + lcreateTime);

		String lupdateTime = location.optString("update_time");
		DeBug.i(TAG, "[location json] updateTime=" + lupdateTime);

		ArrayList<ImageDetail> images = new ArrayList<ImageDetail>();
		images.clear();
		JSONArray imagearray = obj.getJSONArray("images");
		for (int j = 0; j < imagearray.length(); j++) {
			JSONObject imagearrayobj = imagearray.getJSONObject(j);

			long iId = imagearrayobj.optLong("id");
			DeBug.i(TAG, "[image json] tId=" + iId);

			String iUrl = imagearrayobj.optString("url");
			DeBug.i(TAG, "[image json] Url=" + iUrl);//

			String thumb_path = imagearrayobj.optString("thumb_path");
			DeBug.i(TAG, "[image json] thumb_path=" + thumb_path);

			long iPointId = imagearrayobj.optLong("point");
			DeBug.i(TAG, "[image json] PointId=" + iPointId);

			String ct = imagearrayobj.optString("create_time");
			DeBug.i(TAG, "[image json] create_time=" + ct);

			String ut = imagearrayobj.optString("update_time");
			DeBug.i(TAG, "[image json] update_time=" + ut);
			images.add(new ImageDetail(iId, thumb_path, iUrl, ct, ut, iPointId));
		}

		ArrayList<Tag> tags = new ArrayList<Tag>();
		tags.clear();
		JSONArray tagarray = obj.getJSONArray("tags");
		for (int k = 0; k < tagarray.length(); k++) {
			JSONObject tagarrayobj = tagarray.getJSONObject(k);

			long tId = tagarrayobj.optLong("id");
			DeBug.i(TAG, "[tag json] tId=" + tId);

			String tName = tagarrayobj.optString("name");
			DeBug.i(TAG, "[tag json] name=" + tName);
			tags.add(new Tag(tId, tName));
		}

		return new MBPointData(pid, title, url, type, description,
				place_address, place_phone, place_name, coordinates, images,
				tags, collectionId, new MBLocation(lId, placeaddress, placephone,
						placename, lcoordinates, category, lcreateTime,
						lupdateTime), pcreateTime, pupdateTime);
	}

	public static MBCollectionItem parseCollectionInfoResult(String rsp)
			throws JSONException {
		JSONObject obj = new JSONObject(rsp);
		long cid = obj.optLong("id");
		DeBug.i(TAG, "[collection info json] cid=" + cid);

		String name = obj.optString("name");
		DeBug.i(TAG, "[collection info json] name=" + name);

		long userId = obj.optLong("user");
		DeBug.i(TAG, "[collection info json] userId=" + userId);

		JSONArray array = obj.getJSONArray("points");
		ArrayList<MBPointData> points = new ArrayList<MBPointData>();
		points.clear();
		for (int i = 0; i < array.length(); i++) {
			DeBug.i(TAG, "[collection info json] length=" + array.length()
					+ ":" + i);
			JSONObject arrayobj = array.getJSONObject(i);
			long pid = arrayobj.optLong("id");
			DeBug.i(TAG, "[cp info json] pid=" + pid);

			String title = arrayobj.optString("title");
			DeBug.i(TAG, "[cp info json] title=" + title);

			String coordinates = arrayobj.optString("coordinates");
			DeBug.i(TAG, "[cp info json] coordinates=" + coordinates);

			String type = arrayobj.optString("type");
			DeBug.i(TAG, "[cp info json] title=" + type);

			ArrayList<ImageDetail> images = new ArrayList<ImageDetail>();
			images.clear();
			JSONArray imagearray = arrayobj.getJSONArray("images");

			for (int j = 0; j < imagearray.length(); j++) {
				JSONObject imagearrayobj = imagearray.getJSONObject(j);

				long iId = imagearrayobj.optLong("id");
				DeBug.i(TAG, "[ci json] iId=" + iId);

				String iUrl = imagearrayobj.optString("url");
				DeBug.i(TAG, "[ci json] Url=" + iUrl);//

				String thumb_path = imagearrayobj.optString("thumb_path");
				DeBug.i(TAG, "[ci json] thumb_path=" + thumb_path);
				images.add(new ImageDetail(iId, thumb_path, iUrl));
			}

			JSONObject location = arrayobj.getJSONObject("location");

			long lId = location.optLong("id");
			DeBug.i(TAG, "[cl json] lId=" + lId);

			String lcoordinates = location.optString("coordinates");
			DeBug.i(TAG, "[cl json] coordinates=" + lcoordinates);

			String address = location.optString("place_address");
			DeBug.i(TAG, "[cl json] address=" + address);

			ArrayList<Tag> tags = new ArrayList<Tag>();
			tags.clear();

			JSONArray tagarray = arrayobj.getJSONArray("tags");
			for (int j = 0; j < tagarray.length(); j++) {
				JSONObject tagobj = tagarray.getJSONObject(j);
				long tId = tagobj.optLong("id");
				DeBug.i(TAG, "[tag json] tId=" + tId);

				String tName = tagobj.optString("name");
				DeBug.i(TAG, "[tag json] name =" + tName);
				tags.add(new Tag(tId, tName));
			}

			points.add(new MBPointData(pid, title, coordinates, type, images,
					tags, new MBLocation(lId, lcoordinates, address)));
		}
		return new MBCollectionItem(cid, userId, name, points);
	}

	public static long parseErrorResult(String rsp) throws JSONException {
		JSONObject obj = new JSONObject(rsp);
		JSONObject meta = obj.getJSONObject("meta");
		long code = meta.optLong("code");
		DeBug.i(TAG, "[meta json] code=" + code);

		String errorType = meta.optString("errorType");
		DeBug.i(TAG, "[meta json] errorType=" + errorType);

		String errorDetail = meta.optString("errorDetail");
		DeBug.i(TAG, "[meta json] errorDetail=" + errorDetail);
		return code;
	}

	public static VenueCollection parseSearchResult(String rsp)
			throws JSONException {
		JSONObject obj = new JSONObject(rsp);
		JSONObject meta = obj.getJSONObject("meta");
		long code = meta.optLong("code");
		DeBug.i(TAG, "[meta json] code=" + code);

		// String errorType = meta.optString("errorType");
		// DeBug.i(TAG, "[meta json] errorType=" + errorType);
		//
		// String errorDetail = meta.optString("errorDetail");
		// DeBug.i(TAG, "[meta json] errorDetail=" + errorDetail);

		VenueCollection collection = new VenueCollection();
		collection.clear();
		if (code == 200) {
			JSONObject response = obj.getJSONObject("response");
			if (response != null) {
				JSONArray venues = response.getJSONArray("venues");
				if (venues != null) {
					for (int i = 0; i < venues.length(); i++) {
						JSONObject venue = venues.getJSONObject(i);
						String vname = null;
						String phone = null;
						String address = null;
						double lat = 0;
						double lng = 0;
						long distance = 0;
						String url = null;
						ArrayList<String> formattedA = new ArrayList<String>();
						formattedA.clear();
						
						vname = venue.optString("name");
						DeBug.i(TAG, "[venue json] vname=" + vname);

						JSONObject contact = venue.getJSONObject("contact");
						if (contact != null) {
							phone = contact.optString("phone");
							DeBug.i(TAG, "[venue json] phone=" + phone);
							String formattedPhone = contact
									.optString("formattedPhone");
							DeBug.i(TAG, "[venue json] formattedPhone="
									+ formattedPhone);
						}

						JSONObject location = venue.getJSONObject("location");
						if (location != null) {
							address = location.optString("address");
							DeBug.i(TAG, "[venue json] address=" + address);

							lat = location.optDouble("lat");
							DeBug.i(TAG, "[venue json] lat=" + lat);

							lng = location.optDouble("lng");
							DeBug.i(TAG, "[venue json] lng=" + lng);

							distance = location.optLong("distance");
							DeBug.i(TAG, "[venue json] distance=" + distance);

							long postalCode = location.optLong("postalCode");
							DeBug.i(TAG, "[venue json] postalCode="
									+ postalCode);

							String cc = location.optString("cc");
							DeBug.i(TAG, "[venue json] cc=" + cc);

							String city = location.optString("city");
							DeBug.i(TAG, "[venue json] city=" + city);

							String state = location.optString("state");
							DeBug.i(TAG, "[venue json] state=" + state);

							String country = location.optString("country");
							DeBug.i(TAG, "[venue json] country=" + country);

							JSONArray formattedAddress = location
									.getJSONArray("formattedAddress");

							if (formattedAddress != null) {
								for (int j = 0; j < formattedAddress.length(); j++) {
									String a = formattedAddress.getString(j);
									DeBug.i(TAG, "[venue json] a=" + a);
									formattedA.add(a);
								}
							}
						}

						url = venue.optString("url");
						DeBug.i(TAG, "[venue json] url=" + url);
						DeBug.i(TAG,
								"=========================================================");
						collection.add(new Venue(vname, phone, address, lat,
								lng, url, formattedA, distance));
					}
				}
			}
		}
		collection.sort();
		return collection;
	}

	public static VenueCollection parseExploreResult(String rsp)
			throws JSONException {
		JSONObject obj = new JSONObject(rsp);
		JSONObject meta = obj.getJSONObject("meta");
		long code = meta.optLong("code");
		DeBug.i(TAG, "[meta json] code=" + code);

		// String errorType = meta.optString("errorType");
		// DeBug.i(TAG, "[meta json] errorType=" + errorType);
		//
		// String errorDetail = meta.optString("errorDetail");
		// DeBug.i(TAG, "[meta json] errorDetail=" + errorDetail);

		VenueCollection collection = new VenueCollection();
		collection.clear();

		if (code == 200) {
			JSONObject response = obj.getJSONObject("response");
			// long suggestedRadius = response.optLong("suggestedRadius");
			// DeBug.i(TAG, "[tag json] suggestedRadius=" +
			// suggestedRadius);
			// String headerLocation = response.optString("headerLocation");
			// DeBug.i(TAG, "[tag json] headerLocation=" + headerLocation);
			// String headerFullLocation =
			// response.optString("headerFullLocation");
			// DeBug.i(TAG, "[tag json] headerFullLocation=" +
			// headerFullLocation);
			// String headerLocationGranularity =
			// response.optString("headerLocationGranularity");
			// DeBug.i(TAG, "[tag json] headerLocationGranularity=" +
			// headerLocationGranularity);
			// long totalResults = response.optLong("totalResults");
			// DeBug.i(TAG, "[tag json] totalResults=" + totalResults);
			// JSONObject suggestedBounds =
			// response.getJSONObject("suggestedBounds");
			// JSONObject ne = suggestedBounds.getJSONObject("ne");
			// double nelat = ne.optDouble("lat");
			// double nelng = ne.optDouble("lng");
			// DeBug.i(TAG, "[tag json] nelat=" + nelat);
			// DeBug.i(TAG, "[tag json] nelng=" + nelng);
			// JSONObject sw = suggestedBounds.getJSONObject("sw");
			// double swlat = sw.optDouble("lat");
			// double swlng = sw.optDouble("lng");
			// DeBug.i(TAG, "[tag json] swlat=" + swlat);
			// DeBug.i(TAG, "[tag json] swlng=" + swlng);
			if (response != null) {
				JSONArray agroups = response.getJSONArray("groups");
				if (agroups != null) {
					for (int k = 0; k < agroups.length(); k++) {
						JSONObject groups = agroups.getJSONObject(k);
						// String type = groups.optString("type");
						// DeBug.i(TAG, "[tag json] type=" + type);
						// String name = groups.optString("name");
						// DeBug.i(TAG, "[tag json] name=" + name);

						JSONArray items = groups.getJSONArray("items");

						if (items != null) {
							for (int i = 0; i < items.length(); i++) {
								JSONObject item = items.getJSONObject(i);
								if (item != null) {
									JSONObject venue = item
											.getJSONObject("venue");
									if (venue != null) {
										String vname = null;
										String phone = null;
										String address = null;
										double lat = 0;
										double lng = 0;
										long distance = 0;
										String url = null;
										ArrayList<String> formattedA = new ArrayList<String>();
										formattedA.clear();

										vname = venue.optString("name");
										DeBug.i(TAG, "[venue json] vname="
												+ vname);

										JSONObject contact = venue
												.getJSONObject("contact");
										if (contact != null) {
											phone = contact.optString("phone");
											DeBug.i(TAG, "[venue json] phone="
													+ phone);
											String formattedPhone = contact
													.optString("formattedPhone");

											DeBug.i(TAG,
													"[venue json] formattedPhone="
															+ formattedPhone);
										}

										JSONObject location = venue
												.getJSONObject("location");
										if (location != null) {
											address = location
													.optString("address");
											DeBug.i(TAG,
													"[venue json] address="
															+ address);

											lat = location.optDouble("lat");
											DeBug.i(TAG, "[venue json] lat="
													+ lat);

											lng = location.optDouble("lng");
											DeBug.i(TAG, "[venue json] lng="
													+ lng);

											distance = location
													.optLong("distance");
											DeBug.i(TAG,
													"[venue json] distance="
															+ distance);

											long postalCode = location
													.optLong("postalCode");
											DeBug.i(TAG,
													"[venue json] postalCode="
															+ postalCode);

											String cc = location
													.optString("cc");
											DeBug.i(TAG, "[venue json] cc="
													+ cc);

											String city = location
													.optString("city");
											DeBug.i(TAG, "[venue json] city="
													+ city);

											String state = location
													.optString("state");
											DeBug.i(TAG, "[venue json] state="
													+ state);

											String country = location
													.optString("country");
											DeBug.i(TAG,
													"[venue json] country="
															+ country);

											JSONArray formattedAddress = location
													.getJSONArray("formattedAddress");
											if (formattedAddress != null) {
												for (int j = 0; j < formattedAddress
														.length(); j++) {
													String a = formattedAddress
															.getString(j);
													DeBug.i(TAG,
															"[venue json] a="
																	+ a);
													formattedA.add(a);
												}
											}
										}

										url = venue.optString("url");
										DeBug.i(TAG, "[venue json] url=" + url);
										DeBug.i(TAG,
												"=========================================================");
										collection.add(new Venue(vname, phone,
												address, lat, lng, url,
												formattedA, distance));
									}
								}
							}
						}
					}
				}
			}
		}
		collection.sort();
		return collection;
	}
}

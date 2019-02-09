package edu.uark.uarkregisterapp.models.api.services;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

import edu.uark.uarkregisterapp.models.api.ApiResponse;
import edu.uark.uarkregisterapp.models.api.enums.ApiObject;
import edu.uark.uarkregisterapp.models.api.interfaces.PathElementInterface;

abstract class BaseRemoteService {
	URL buildPath() {
		return this.buildPath((new PathElementInterface[0]), StringUtils.EMPTY);
	}

	URL buildPath(UUID recordId) {
		return this.buildPath((new PathElementInterface[0]), recordId.toString());
	}

	URL buildPath(PathElementInterface[] pathElements, String parameterValue) {
		StringBuilder completePath = (new StringBuilder(BASE_URL))
				.append(this.apiObject.getPathValue());

		for (PathElementInterface pathElement : pathElements) {
			String pathEntry = pathElement.getPathValue();

			if (!StringUtils.isBlank(pathEntry)) {
				completePath.append(pathEntry).append(URL_JOIN);
			}
		}

		if (!StringUtils.isBlank(parameterValue)) {
			completePath.append(parameterValue);
		}

		URL connectionUrl;
		try {
			connectionUrl = new URL(completePath.toString());
		} catch (MalformedURLException e) {
			e.printStackTrace();
			connectionUrl = null;
		}

		return connectionUrl;
	}

	<T extends Object> ApiResponse<T> performGetRequest(URL connectionUrl) {
		ApiResponse<T> apiResponse = new ApiResponse<>();

		if (connectionUrl == null) {
			return apiResponse
				.setValidResponse(false)
				.setMessage("Invalid network path provided.");
		}

		HttpURLConnection httpURLConnection = null;
		StringBuilder rawResponse = new StringBuilder();

		try {
			httpURLConnection = (HttpURLConnection) connectionUrl.openConnection();
			httpURLConnection.setRequestMethod(GET_REQUEST_METHOD);
			httpURLConnection.addRequestProperty(ACCEPT_REQUEST_PROPERTY, JSON_PAYLOAD_TYPE);

			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));

			char[] buffer = new char[1024];
			int readCharacters = bufferedReader.read(buffer, 0, buffer.length);
			while (readCharacters > 0) {
				rawResponse.append(buffer, 0, readCharacters);
				readCharacters = bufferedReader.read(buffer, 0, buffer.length);
			}

			apiResponse.setValidResponse(
				this.isValidResponse(
					httpURLConnection.getResponseCode()
				)
			);

			bufferedReader.close();
		} catch (IOException e) {
			e.printStackTrace();

			apiResponse
				.setValidResponse(false)
				.setMessage(e.getMessage());
		} finally {
			if (httpURLConnection != null) {
				httpURLConnection.disconnect();
			}
		}

		return apiResponse.setRawResponse(rawResponse.toString());
	}

	<T extends Object> ApiResponse<T> performPutRequest(URL connectionUrl, JSONObject jsonObject) {
		return this.performUploadRequest(PUT_REQUEST_METHOD, connectionUrl, jsonObject);
	}

	<T extends Object> ApiResponse<T> performPostRequest(URL connectionUrl, JSONObject jsonObject) {
		return this.performUploadRequest(POST_REQUEST_METHOD, connectionUrl, jsonObject);
	}

	private <T extends Object> ApiResponse<T> performUploadRequest(String requestType, URL connectionUrl, JSONObject jsonObject) {
		ApiResponse<T> apiResponse = new ApiResponse<>();

		if (connectionUrl == null) {
			return apiResponse
				.setValidResponse(false)
				.setMessage("Invalid network path provided.");
		}

		HttpURLConnection httpURLConnection = null;
		StringBuilder rawResponse = new StringBuilder();

		try {
			byte[] serializedRequestObject = jsonObject.toString().getBytes(UTF8_CHARACTER_ENCODING);

			httpURLConnection = (HttpURLConnection) connectionUrl.openConnection();
			httpURLConnection.setDoInput(true);
			httpURLConnection.setDoOutput(true);
			httpURLConnection.setFixedLengthStreamingMode(serializedRequestObject.length);
			httpURLConnection.setRequestMethod(requestType);
			httpURLConnection.addRequestProperty(ACCEPT_REQUEST_PROPERTY, JSON_PAYLOAD_TYPE);
			httpURLConnection.addRequestProperty(CONTENT_TYPE_REQUEST_PROPERTY, JSON_PAYLOAD_TYPE);

			OutputStream outputStream = httpURLConnection.getOutputStream();
			outputStream.write(serializedRequestObject);
			outputStream.flush();

			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));

			char[] buffer = new char[1024];
			int readCharacters = bufferedReader.read(buffer, 0, buffer.length);
			while (readCharacters > 0) {
				rawResponse.append(buffer, 0, readCharacters);
				readCharacters = bufferedReader.read(buffer, 0, buffer.length);
			}

			apiResponse.setValidResponse(
				this.isValidResponse(
					httpURLConnection.getResponseCode()
				)
			);

			bufferedReader.close();
		} catch (IOException e) {
			e.printStackTrace();

			apiResponse
				.setValidResponse(false)
				.setMessage(e.getMessage());
		} finally {
			if (httpURLConnection != null) {
				httpURLConnection.disconnect();
			}
		}

		return apiResponse.setRawResponse(rawResponse.toString());
	}

	ApiResponse<String> performDeleteRequest(URL connectionUrl) {
		ApiResponse<String> apiResponse = new ApiResponse<>();

		if (connectionUrl == null) {
			return apiResponse
				.setValidResponse(false)
				.setMessage("Invalid network path provided.");
		}

		HttpURLConnection httpURLConnection = null;
		StringBuilder rawResponse = new StringBuilder();

		try {
			httpURLConnection = (HttpURLConnection) connectionUrl.openConnection();
			httpURLConnection.setRequestMethod(DELETE_REQUEST_METHOD);

			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));

			char[] buffer = new char[1024];
			int readCharacters = bufferedReader.read(buffer, 0, buffer.length);
			while (readCharacters > 0) {
				rawResponse.append(buffer, 0, readCharacters);
				readCharacters = bufferedReader.read(buffer, 0, buffer.length);
			}

			apiResponse.setValidResponse(
				this.isValidResponse(
					httpURLConnection.getResponseCode()
				)
			);

			bufferedReader.close();
		} catch (IOException e) {
			e.printStackTrace();

			apiResponse
				.setValidResponse(false)
				.setMessage(e.getMessage());
		} finally {
			if (httpURLConnection != null) {
				httpURLConnection.disconnect();
			}
		}

		return apiResponse.setRawResponse(rawResponse.toString());
	}

	JSONObject rawResponseToJSONObject(String rawResponse) {
		JSONObject jsonObject = null;

		if (!StringUtils.isBlank(rawResponse)) {
			try {
				jsonObject = new JSONObject(rawResponse);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		return jsonObject;
	}

	JSONArray rawResponseToJSONArray(String rawResponse) {
		JSONArray jsonArray = null;

		if (!StringUtils.isBlank(rawResponse)) {
			try {
				jsonArray = new JSONArray(rawResponse);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		return jsonArray;
	}

	private boolean isValidResponse(int responseCode) {
		return (
			(responseCode == HttpURLConnection.HTTP_OK)
			|| (responseCode == HttpURLConnection.HTTP_CREATED)
			|| (responseCode == HttpURLConnection.HTTP_ACCEPTED)
			|| (responseCode == HttpURLConnection.HTTP_NO_CONTENT)
		);
	}

	private ApiObject apiObject;

	BaseRemoteService(ApiObject apiObject) {
		this.apiObject = apiObject;
	}

	private static final String URL_JOIN = "/";
	private static final String GET_REQUEST_METHOD = "GET";
	private static final String PUT_REQUEST_METHOD = "PUT";
	private static final String POST_REQUEST_METHOD = "POST";
	private static final String DELETE_REQUEST_METHOD = "DELETE";
	private static final String UTF8_CHARACTER_ENCODING = "UTF8";
	private static final String ACCEPT_REQUEST_PROPERTY = "Accept";
	private static final String JSON_PAYLOAD_TYPE = "application/json";
	private static final String CONTENT_TYPE_REQUEST_PROPERTY = "Content-Type";
	private static final String BASE_URL = "https://test-app-se.herokuapp.com/api/";
//	private static final String BASE_URL = "https://uarkregservnodejsapi.herokuapp.com/api/";
}

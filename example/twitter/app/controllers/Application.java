package controllers;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;


import models.*;

import play.libs.WS;
import play.libs.WS.HttpResponse;
import play.modules.oauthclient.OAuthClient;
import play.mvc.*;

public class Application extends Controller {
	
	public static void index() throws Exception {
		User user = getUser();
		String url = "http://twitter.com/statuses/mentions.xml";
		String mentions = play.libs.WS.url(getConnector().sign(user.twitterCreds, url)).get().getString();
		render(mentions);
	}

	public static void setStatus(String status) throws Exception {
		User user = getUser();
		String url = "http://twitter.com/statuses/update.json?status=" + URLEncoder.encode(status, "utf-8");
		String response = getConnector().sign(user.twitterCreds, WS.url(url), "POST").post().getString();
		request.current().contentType = "application/json";
		renderText(response);
	}

	// Twitter authentication

	public static void authenticate(String callback) throws Exception {
		// 1: get the request token
		User user = getUser();
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("callback", callback);
		String callbackURL = Router.getFullUrl(request.controller + ".oauthCallback", args);
		getConnector().authenticate(user.twitterCreds, callbackURL);
	}

	public static void oauthCallback(String callback, String oauth_token, String oauth_verifier) throws Exception {
		// 2: get the access token
		User user = getUser();
		getConnector().retrieveAccessToken(user.twitterCreds, oauth_verifier);
		redirect(callback);
	}

	// TODO: Make it real?

	private static User getUser() {
		return User.findOrCreate("guest");
	}

	private static OAuthClient connector = null;
	private static OAuthClient getConnector() {
		if (connector == null) {
			connector = new OAuthClient(
					"http://twitter.com/oauth/request_token",
					"http://twitter.com/oauth/access_token",
					"http://twitter.com/oauth/authorize",
					"eevIR82fiFK3e6VrGpO9rw",
					"OYCQA6fpsLiMVaxqqm1EqDjDWFmdlbkSYYcIbwICrg");
		}
		return connector;
	}

}

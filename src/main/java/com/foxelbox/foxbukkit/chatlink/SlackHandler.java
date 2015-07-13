/**
 * This file is part of FoxBukkitChatLink.
 *
 * FoxBukkitChatLink is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FoxBukkitChatLink is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FoxBukkitChatLink.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.foxelbox.foxbukkit.chatlink;

import com.foxelbox.foxbukkit.chatlink.json.ChatMessageOut;

public class SlackHandler {
	public void sendMessage(ChatMessageOut message) {
	}

	/*private static void publishToSlack(ChatMessageOut message) {
		if(!message.type.equalsIgnoreCase("text")) { // We ignore non-text messages
			return;
		}

		String username;
		if(message.from != null && message.from.name.length() > 0) {
			username = message.from.name;
		} else {
			username = "(no username)";
		}

		String channel;
		switch(message.to.type) {
			case "all":
				channel = "#minecraft";
				break;
			case "permission":
				if(message.to.filter.length == 1 && message.to.filter[0].equals("foxbukkit.opchat")) {
					// op chat
					channel = "#minecraft-ops";
					break;
				}
				return;
			default:
				return;
		}

		// TODO: Do something better than this simple replace.
		final String cleanText = message.contents.replaceAll("<[^>]+>", "").replaceAll("&apos;", "'"); // Remove all of the HTML tags and fix &apos;

		try {
			final URL slackPostURL = new URL("https://slack.com/api/chat.postMessage");

			final Map<String, Object> params = new LinkedHashMap<>();
			params.put("token", Main.configuration.getValue("slack-token", ""));
			params.put("channel", channel);
			params.put("username", username);
			if(message.from != null && message.from.name.length() > 0) {
				// Only set the avatar URL if there's a name attached to this message.
				params.put("icon_url", "https://minotar.net/avatar/" + URLEncoder.encode(message.from.name, "UTF-8") + "/48.png");
			}

			params.put("parse", "none"); // Slack shouldn't do any modification to our text.
			params.put("link_names", "false"); // Slack shouldn't try to link names.
			params.put("unfurl_media", "false"); // Please don't unfurl media from untrusted users.
			params.put("text", cleanText);

			final byte[] encodedParams = encodeURLParams(params);

			final HttpURLConnection connection = (HttpURLConnection) slackPostURL.openConnection();
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			connection.setRequestProperty("Content-Length", Integer.toString(encodedParams.length));

			try(final DataOutputStream wr = new DataOutputStream(connection.getOutputStream())) {
				wr.write(encodedParams);
			}

			SlackResponse slackResponse;
			try(final BufferedReader inputStream = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
				synchronized(gson) {
					slackResponse = gson.fromJson(inputStream, SlackResponse.class);
				}
			}

			if(!slackResponse.ok) {
				throw new Exception("Error occurred while calling Slack API: " + slackResponse.error);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	private static byte[] encodeURLParams(Map<String, Object> params) {
		final StringBuilder encoded = new StringBuilder();

		try {
			for(Map.Entry<String, Object> param : params.entrySet()) {
				if(encoded.length() != 0)
					encoded.append('&');
				encoded.append(URLEncoder.encode(param.getKey(), "UTF-8"));
				encoded.append('=');
				encoded.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
			}

			return encoded.toString().getBytes("UTF-8");
		} catch(UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
	}*/
}

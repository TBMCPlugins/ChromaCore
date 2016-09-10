package buttondevteam.bucket.core;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.json.simple.JSONObject;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import buttondevteam.bucket.MainPlugin;

public final class TBMCCoreAPI {
	/**
	 * Updates or installs the specified plugin. The plugin JAR filename must match the plugin's repository name.
	 * 
	 * @param name
	 *            The plugin's repository/JAR name.
	 * @return Error message or empty string
	 */
	public static String UpdatePlugin(String name) {
		MainPlugin.Instance.getLogger().info("Updating TBMC plugin: " + name);
		String ret = "";
		URL url;
		try {
			url = new URL("https://github.com/TBMCPlugins/" + name + "/raw/master/" + name + ".jar");
			FileUtils.copyURLToFile(url, new File("plugins/" + name + ".jar"));
		} catch (Exception e) {
			MainPlugin.Instance.getLogger().warning("Error!\n" + e);
			ret = e.toString();
		}
		return ret;
	}

	/**
	 * Retrieves all the repository names from the GitHub organization.
	 * 
	 * @return A list of names
	 */
	public static List<String> GetPluginNames() {
		List<String> ret = new ArrayList<>();
		try {
			String resp = DownloadString("https://api.github.com/orgs/TBMCPlugins/repos");
			JsonArray arr = new JsonParser().parse(resp).getAsJsonArray();
			for (JsonElement obj : arr) {
				JsonObject jobj = obj.getAsJsonObject();
				ret.add(jobj.get("name").getAsString());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}

	public static String DownloadString(String urlstr) throws MalformedURLException, IOException {
		URL url = new URL(urlstr);
		URLConnection con = url.openConnection();
		con.setRequestProperty("User-Agent", "TBMCPlugins");
		InputStream in = con.getInputStream();
		String encoding = con.getContentEncoding();
		encoding = encoding == null ? "UTF-8" : encoding;
		String body = IOUtils.toString(in, encoding);
		in.close();
		return body;
	}
}

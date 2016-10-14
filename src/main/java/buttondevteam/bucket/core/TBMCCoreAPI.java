package buttondevteam.bucket.core;

import java.io.File;
import java.io.FileNotFoundException;
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
	 * Updates or installs the specified plugin. The plugin must use Maven.
	 * 
	 * @param name
	 *            The plugin's repository name.
	 * @return Error message or empty string
	 */
	public static String UpdatePlugin(String name) {
		MainPlugin.Instance.getLogger().info("Updating TBMC plugin: " + name);
		String ret = "";
		URL url;
		try {
			url = new URL("https://jitpack.io/com/github/TBMCPlugins/"
					+ (name.equalsIgnoreCase("ButtonCore") ? "ButtonCore/ButtonCore" : name) + "/master-SNAPSHOT/"
					+ name + "-master-SNAPSHOT.jar"); // ButtonCore exception required since it hosts Towny as well
			FileUtils.copyURLToFile(url, new File("plugins/" + name + ".jar"));
		} catch (FileNotFoundException e) {
			ret = "Can't find JAR, the build probably failed. Build log (scroll to bottom):\nhttps://jitpack.io/com/github/TBMCPlugins/"
					+ name + "/master-SNAPSHOT/build.log";
		} catch (IOException e) {
			ret = "IO error - Did you spell the plugin's name correctly?\n" + e.getMessage();
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

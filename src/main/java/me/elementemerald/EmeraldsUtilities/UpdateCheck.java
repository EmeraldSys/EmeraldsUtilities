package me.elementemerald.EmeraldsUtilities;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.bukkit.plugin.java.JavaPlugin;

public class UpdateCheck {
    private final JavaPlugin pl;
    private static class GitHubAPIResponse
    {
        private String tag_name;
    }
    private GitHubAPIResponse jobj;

    public UpdateCheck(JavaPlugin pl)
    {
        this.pl = pl;

        try {
            URL url = new URL("https://api.github.com/repos/EmeraldSys/EmeraldsUtilities/releases/latest");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setInstanceFollowRedirects(false);

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            StringBuffer content = new StringBuffer();
            while ((line = in.readLine()) != null) {
                content.append(line);
            }
            in.close();

            String resp = content.toString();
            GitHubAPIResponse obj = new Gson().fromJson(resp, GitHubAPIResponse.class);
            jobj = obj;
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    public boolean isUpdated()
    {
        String latest = jobj.tag_name;
        return pl.getDescription().getVersion().equalsIgnoreCase(latest);
    }

    public String getLatestVersion()
    {
        return jobj.tag_name;
    }
}

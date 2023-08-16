package nl.juriantech.tnttag.managers;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import nl.juriantech.tnttag.Tnttag;
import nl.juriantech.tnttag.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.logging.Level;

public class DumpManager {

    private final Tnttag plugin;

    public DumpManager(Tnttag plugin) {
        this.plugin = plugin;
    }

    public void dumpLog(CommandSender commandSender) {
        String logContents = getLatestServerLog();
        String expiryDate = getExpiryDate();

        try {
            String apiURL = "https://paste.juriantech.nl/api/create.php";
            String boundary = UUID.randomUUID().toString();
            String lineBreak = "\r\n";

            URL url = new URL(apiURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

            try (OutputStream outputStream = connection.getOutputStream();
                 PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8), true)) {

                // Write the log contents and expiry date to the request body
                writer.append("--").append(boundary).append(lineBreak);
                writer.append("Content-Disposition: form-data; name=\"contents\"").append(lineBreak);
                writer.append(lineBreak).append(logContents).append(lineBreak);

                writer.append("--").append(boundary).append(lineBreak);
                writer.append("Content-Disposition: form-data; name=\"expiry\"").append(lineBreak);
                writer.append(lineBreak).append(expiryDate).append(lineBreak);

                writer.append("--").append(boundary).append("--").append(lineBreak);
                writer.flush();
            }

            // Read the response
            StringBuilder responseData = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    responseData.append(line);
                }
            }

            String response = responseData.toString();
            String link = "https://paste.juriantech.nl/view.php?id=" + response;

            commandSender.sendMessage(ChatUtils.colorize(Tnttag.customizationfile.getString("dump-log.uploaded").replace("{link}", link)));
        } catch (Exception e) {
            commandSender.sendMessage(ChatUtils.colorize(Tnttag.customizationfile.getString("dump-log.failed")));
            e.printStackTrace();
        }
    }

    public void dumpAll(CommandSender commandSender) {
        String tnttagVersion = getTnttagVersion();
        String serverSoftware = Bukkit.getName();
        String serverVersion = Bukkit.getVersion();
        String latestLog = getLatestServerLog();
        String supportStatus = hasLeakMessages(latestLog) ? "NONE" : "FULL";
        String serverPlugins = getServerPlugins();
        String tnttagFiles = getTnttagFiles();

        try {
            String apiURL = "https://dumps.juriantech.nl/api.php";

            URL url = new URL(apiURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=---boundary");

            // Build the request body
            String boundary = "---boundary";
            String lineBreak = "\r\n";
            String postData =
                    "--" + boundary + lineBreak +
                            "Content-Disposition: form-data; name=\"tnttag_version\"" + lineBreak +
                            lineBreak + tnttagVersion + lineBreak +
                            "--" + boundary + lineBreak +
                            "Content-Disposition: form-data; name=\"server_software\"" + lineBreak +
                            lineBreak + serverSoftware + lineBreak +
                            "--" + boundary + lineBreak +
                            "Content-Disposition: form-data; name=\"server_version\"" + lineBreak +
                            lineBreak + serverVersion + lineBreak +
                            "--" + boundary + lineBreak +
                            "Content-Disposition: form-data; name=\"support_status\"" + lineBreak +
                            lineBreak + supportStatus + lineBreak +
                            "--" + boundary + lineBreak +
                            "Content-Disposition: form-data; name=\"server_plugins\"" + lineBreak +
                            lineBreak + serverPlugins + lineBreak +
                            "--" + boundary + lineBreak +
                            "Content-Disposition: form-data; name=\"tnttag_files\"" + lineBreak +
                            lineBreak + tnttagFiles + lineBreak +
                            "--" + boundary + lineBreak +
                            "Content-Disposition: form-data; name=\"latest_log\"" + lineBreak +
                            lineBreak + latestLog + lineBreak +
                            "--" + boundary + "--" + lineBreak;

            byte[] postDataBytes = postData.getBytes(StandardCharsets.UTF_8);

            // Send the request
            try (OutputStream outputStream = connection.getOutputStream()) {
                outputStream.write(postDataBytes);
                outputStream.flush();
            }

            // Read the response
            StringBuilder responseData = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    responseData.append(line);
                }
            }

            JsonObject jsonObject = JsonParser.parseString(responseData.toString()).getAsJsonObject();
            if (jsonObject.has("error") && !jsonObject.get("error").isJsonNull()) {
                plugin.getLogger().severe("We've spotted an error while dumping your logs: " + jsonObject.get("error").getAsString());
                return;
            }

            if (jsonObject.has("identifier") && !jsonObject.get("identifier").isJsonNull()) {
                String identifier = jsonObject.get("identifier").getAsString();
                commandSender.sendMessage(ChatUtils.colorize(Tnttag.customizationfile.getString("dump-all.uploaded").replace("{identifier}", identifier)));
            } else {
                commandSender.sendMessage(ChatUtils.colorize(Tnttag.customizationfile.getString("dump-all.failed")));
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error while sending the dump data to the server.", e);
            commandSender.sendMessage(ChatUtils.colorize(Tnttag.customizationfile.getString("dump-all.failed")));
        }
    }

    private String getTnttagVersion() {
        PluginDescriptionFile description = plugin.getDescription();
        return description.getVersion();
    }

    private boolean hasLeakMessages(String serverLog) {
        List<String> leakSites = Arrays.asList("directleaks", "spigotunlocked", "blackspigot");

        boolean foundLeakMessages = false;

        for (String leakSite : leakSites) {
            if (serverLog.contains(leakSite)) {
                foundLeakMessages = true;
                break;
            }
        }

        return foundLeakMessages;
    }

    private String getServerPlugins() {
        Plugin[] plugins = Bukkit.getPluginManager().getPlugins();
        StringJoiner pluginList = new StringJoiner(", "); //Using an StringJoiner instead of StringBuilder because it handles the delimiter at the end automatically so it results in cleaner code.
        for (Plugin plugin : plugins) {
            pluginList.add(plugin.getName());
        }

        return pluginList.toString();
    }

    private String getTnttagFiles() {
        File pluginDir = plugin.getDataFolder();
        StringBuilder fileContents = new StringBuilder(50000);

        if (pluginDir.exists() && pluginDir.isDirectory()) {
            File[] files = pluginDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && file.getPath().endsWith(".yml")) {
                        String fileName = file.getName();
                        try {
                            BufferedReader reader = new BufferedReader(new FileReader(file));
                            String line;
                            fileContents.append("-----").append(fileName).append("-----\n");
                            while ((line = reader.readLine()) != null) {
                                fileContents.append(line).append("\n");
                            }
                            reader.close();
                            fileContents.append("-----END ").append(fileName).append("-----\n");
                        } catch (IOException e) {
                            plugin.getLogger().log(Level.SEVERE, "Error while reading TNTTag file: " + fileName, e);
                        }
                    }
                }
            }
        }

        return fileContents.toString();
    }

    private String getLatestServerLog() {
        File serverLog = new File("logs/latest.log");
        StringBuilder logContent = new StringBuilder();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(serverLog));
            String line;
            while ((line = reader.readLine()) != null) {
                logContent.append(line).append("\n");
            }
            reader.close();
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Error while reading latest server log.", e);
        }
        return logContent.toString();
    }

    private String getExpiryDate() {
        LocalDate currentDate = LocalDate.now();
        LocalDate expiryDate = currentDate.plusDays(7); // Adding 7 days to the current date
        return expiryDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
    }
}
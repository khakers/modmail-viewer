package com.github.khakers.modmailviewer;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class UpdateChecker {

    private static final Logger logger = LogManager.getLogger();

    private static final String REPO_OWNER = "khakers";
    private static final String REPO_NAME = "modmail-viewer";

    private static final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();

    private final Version appVersion = new Version(ModmailViewer.TAG);

    private final boolean isPrereleaseEligible = ModmailViewer.BRANCH.equalsIgnoreCase("develop") || (appVersion.prerelease != null && !appVersion.prerelease.isBlank());

    public static boolean isDockerContainer() {
        return false;
    }

    public boolean isSemVerUpdateAvailable(Version currentVersion) {
        Request request = new Request.Builder()
                .url(String.format("https://api.github.com/repos/%s/%s/releases", REPO_OWNER, REPO_NAME))
                .header("accept", "application/vnd.github+json")
                .get()
                .build();
        logger.trace("request: {}", request.toString());


        try (Response response = client.newCall(request).execute()) {
            logger.debug("Got github release data with status {}", response.code());

            assert response.body() != null;
            var body = response.body().string();


            var releases = mapper.readTree(body);
            Version latestVersion = null;

            for (var releaseJson : releases) {
                logger.trace("release: {}", releaseJson);
                logger.debug("parsed release: {}", releaseJson.get("tag_name").asText());
                var prerelease = releaseJson.get("prerelease").asBoolean(false);
                var tagName = releaseJson.get("tag_name").asText();
                // If the current version is prerelease eligible, take the latest version regardless of prerelease status
                if (isPrereleaseEligible) {
                    latestVersion = new Version(tagName);
                    break;
                }
                // If the current version is not prerelease eligible, take the first non-prerelease version
                if (!prerelease) {
                    latestVersion = new Version(tagName);
                    break;
                }
            }
            if (latestVersion == null) {
                logger.warn("No releases found from github API");
                return false;
            }

            logger.debug("found version {} from github API. Current version is {}", latestVersion, currentVersion);


            if (currentVersion.compareTo(latestVersion) > 0) {
                var url = releases.get(0).get("html_url");
                logger.warn("An update is available! Version v{} can be downloaded at {}. Out of date versions are not supported.", latestVersion.asVersionString(), url);
                return true;
            }


        } catch (IOException e) {
            logger.error(e);
            return false;
        }
        return false;
    }


    public boolean isUpdateAvailable() {
        if (ModmailViewer.isSemVerRelease()) {
            return isSemVerUpdateAvailable(appVersion);
        } else if (isPrereleaseEligible) {
            logger.info("You're running on a development or pre-release build.");
        }
        return false;
    }


}


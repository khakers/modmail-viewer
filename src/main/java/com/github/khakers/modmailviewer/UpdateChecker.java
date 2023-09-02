package com.github.khakers.modmailviewer;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class UpdateChecker {

    private static final Logger logger = LogManager.getLogger();

    private static final String REPO_OWNER = "khakers";
    private static final String REPO_NAME = "modmail-viewer";

    private static final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();

    private final ScheduledExecutorService executorService;
    private final AtomicBoolean updateAvailable = new AtomicBoolean(false);
    private final AtomicReference<Version> latestVersion = new AtomicReference<>(null);

    private final AtomicReference<Instant> updateFoundTime = new AtomicReference<>(null);

    private final Version CURRENT_VERSION;

    public UpdateChecker() {

        if (ModmailViewer.VERSION.isEmpty()) {
            CURRENT_VERSION = new Version("0.0.0");
            executorService = null;
            return;
        }

        CURRENT_VERSION = new Version(ModmailViewer.VERSION);

        this.executorService = java.util.concurrent.Executors.newSingleThreadScheduledExecutor();

        this.executorService.scheduleAtFixedRate(this::runUpdateLoop, 1, 60, TimeUnit.MINUTES);

    }

    public static boolean isContainerized() {
        return System.getProperty("ENV_TYPE").equalsIgnoreCase("containerized");
    }

    private void runUpdateLoop() {
        var version = fetchLatestVersion();

        version.ifPresent(version1 -> {
            logger.debug("our version is {}", CURRENT_VERSION.asVersionString());
            if (CURRENT_VERSION.compareTo(version1) > 0) {
                logger.warn("An update is available! Version v{}. Out of date versions are not supported.", version1.asVersionString());
                updateAvailable.set(true);
                latestVersion.set(version1);
                updateFoundTime.set(Instant.now());
            }
        });
    }

    public boolean isUpdateAvailable() {
        return updateAvailable.get();
    }

    public Optional<Version> getLatestVersion() {
        return Optional.ofNullable(latestVersion.get());
    }

    public Optional<Instant> getUpdateFoundTime() {
        return Optional.ofNullable(updateFoundTime.get());
    }

    private Optional<Version> fetchLatestVersion() {
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

            var gitTag = releases
                  .get(0)
                  .get("tag_name")
                  .asText();

            logger.debug("found version {} from github API.", gitTag);

            return Optional.of(new Version(gitTag));


        } catch (IOException e) {
            logger.error(e);
        }
        return Optional.empty();
    }

    public boolean isSemVerUpdateAvailable(String version) {


        var foo = this.fetchLatestVersion();

        if (foo.isPresent()) {
            var latestVersion = foo.get();
            var currentVersion = new Version(version);

            if (currentVersion.compareTo(latestVersion) > 0) {
                logger.warn("An update is available! Version v{}. Out of date versions are not supported.", latestVersion.asVersionString());
                return true;
            }
        }

        return false;
    }


}


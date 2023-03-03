package com.github.khakers.modmailviewer.auth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.khakers.modmailviewer.ModMailLogDB;
import com.github.scribejava.apis.DiscordApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.exceptions.OAuthException;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;
import io.javalin.http.Context;
import io.javalin.http.Cookie;
import io.javalin.http.Handler;
import io.javalin.http.SameSite;
import io.javalin.security.RouteRole;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public class AuthHandler {
    //todo logout handler

    private static final Logger logger = LogManager.getLogger();

    private final OAuth20Service service;

    private final JwtAuth jwtAuth;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static ModMailLogDB modMailLogDB;

    private final Map<String, ClientState> ouathState = new HashMap<>();

    private final SecureRandom secureRandom = new SecureRandom();

    public AuthHandler(String callback, String clientId, String clientSecret, String jwtSecret, ModMailLogDB modMailLogDB) {
        this.service = new ServiceBuilder(clientId)
                .apiSecret(clientSecret)
                .defaultScope("identify")
                .callback(callback)
                .userAgent("modmail-viewer")
                .build(DiscordApi.instance());
        this.jwtAuth = new JwtAuth(jwtSecret);
        AuthHandler.modMailLogDB = modMailLogDB;
    }

    public static Role getUserRole(Context ctx) throws Exception {
        var jwtCookie = ctx.cookie("jwt");
        if (jwtCookie != null) {
            try {
                var verifiedJWT = JwtAuth.verifyJWT((String) jwtCookie);
                var user = objectMapper.readValue(verifiedJWT, SiteUser.class);
                return modMailLogDB.getUserRole(user);
            } catch (Exception e) {
                logger.error(e);
                return Role.ANYONE;
            }
        } else {
            logger.debug("could not get user role, no jwt cookie present, users role is ANYONE");
            return Role.ANYONE;
        }
    }

    public static SiteUser getUser(Context ctx) throws JsonProcessingException {
        var jwtCookie = ctx.cookie("jwt");
        if (jwtCookie == null) {
            logger.debug("could not get user, no jwt cookie present");
            return new SiteUser(0L, "anonymous", "0000", "");
        }
        return JwtAuth.decodeJWT(jwtCookie, objectMapper);
    }

    public void HandleAuth(@NotNull Handler handler, @NotNull Context ctx, @NotNull Set<? extends RouteRole> routeRoles) throws Exception {
        logger.debug("{} roles: {}", ctx.endpointHandlerPath(), routeRoles);
        if (routeRoles.contains(Role.ANYONE)) {
            logger.debug("endpoint allows ANYONE");
            handler.handle(ctx);
            return;
        }
        Role userRole = AuthHandler.getUserRole(ctx);
        SiteUser user = AuthHandler.getUser(ctx);
        logger.debug("User id{} @ {} had role {}", user.getId(), ctx.ip(), userRole);
        if (routeRoles.contains(userRole)) {
            handler.handle(ctx);
            logger.debug("User id{} @ {} was authorized and had request handled", user.getId(), ctx.ip());
        } else if (userRole != Role.ANYONE) {
            logger.debug("User id{} @ {} was not authorized was given a 403", user.getId(), ctx.ip());
            ctx.status(403).result();
        } else {
            logger.debug("Redirected {} to auth URL from {}", ctx.ip(), ctx.url());
            ctx.redirect(service.getAuthorizationUrl(generateOuathState(ctx)));
        }
    }

    private String generateOuathState(Context ctx) {
        var key = new BigInteger(130, secureRandom).toString(32);
        var state = new ClientState(ctx.fullUrl());
        ouathState.put(key, state);
        ctx.cookie(new Cookie("state", key, "/", 240, true, 1, true));
        return key;
    }

    private ClientState getAndVerifyOauthState(Context ctx) throws OAuthException {
        var stateKey = ctx.cookie("state");
        var state=  ouathState.get(stateKey);
        if (state == null) {
            throw new OAuthException("Invalid state");
        }
        if (Instant.now().isAfter(state.getTimeGenerated().plus(3, ChronoUnit.MINUTES))) {
            ouathState.remove(stateKey);
            throw new OAuthException("Invalid state: expired");
        }
        ouathState.remove(stateKey);
        ctx.removeCookie("state");
        return state;
    }

    public void handleGenerateJWT(Context ctx, SiteUser user) throws JsonProcessingException {

        // Same site strict cause browser not to send the cookie upon redirect from oauth
        // Which would mean we would need load a page that redirects the user with js
        ctx.cookie(new Cookie("jwt", jwtAuth.generateJWT(user), "/", 10800, true, 1, true, "", "", SameSite.LAX));
    }

    public void handleCallback(Context ctx) throws IOException, ExecutionException, InterruptedException {
        var code = ctx.queryParam("code");
        var state = ctx.queryParam("state");
        if (state == null) {
            ctx.status(400).result("invalid state");
        }
        try {
            var clientState = getAndVerifyOauthState(ctx);
            logger.debug("clients state was: {}", clientState);
            if (code != null) {
                logger.debug("code: {}", code);
                var token = service.getAccessToken(code);
                logger.debug("token {}", token.getRawResponse());
                var request = new OAuthRequest(Verb.GET, "https://discord.com/api/users/@me");
                service.signRequest(token, request);
                try (Response response = service.execute(request)) {
                    logger.debug(response.getCode());
                    logger.debug(response.getBody());

                    var user = objectMapper.readValue( response.getBody(), SiteUser.class);

                    // The user is not authorized for any role and thus does not need to be given a token
                    if (modMailLogDB.getUserRole(user) == Role.ANYONE) {
                        logger.debug("User signed in through discord but was not authorized");
                        ctx.status(403).result();
                        return;
                    }

                    ctx.result(response.getBody());
                    handleGenerateJWT(ctx, user);
                    ctx.redirect(clientState.getRedirectedFrom());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } else {
                ctx.status(400).result("invalid redirect, no code present");
            }
        } catch (OAuthException e) {
            logger.error(e);
            ctx.status(400).result("invalid state");
        }
    }
}

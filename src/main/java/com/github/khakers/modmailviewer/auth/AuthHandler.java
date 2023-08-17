package com.github.khakers.modmailviewer.auth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.khakers.modmailviewer.Config;
import com.github.khakers.modmailviewer.ModMailLogDB;
import com.github.khakers.modmailviewer.ModmailViewer;
import com.github.khakers.modmailviewer.auditlog.OutboundAuditEventLogger;
import com.github.khakers.modmailviewer.auditlog.event.AuditEvent;
import com.github.khakers.modmailviewer.auditlog.event.AuditEventSource;
import com.github.khakers.modmailviewer.auth.discord.GuildMember;
import com.github.khakers.modmailviewer.util.DiscordUtils;
import com.github.scribejava.apis.DiscordApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.exceptions.OAuthException;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.github.scribejava.httpclient.okhttp.OkHttpHttpClient;
import io.javalin.http.*;
import io.javalin.http.util.NaiveRateLimit;
import io.javalin.security.RouteRole;
import okhttp3.OkHttpClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class AuthHandler {
    //todo logout handler

    private static final Logger logger = LogManager.getLogger();
    private static final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
    private static final OkHttpClient client = new OkHttpClient();
    private static ModMailLogDB modMailLogDB;
    private final OAuth20Service service;
    private final JwtAuth jwtAuth;
    private final Map<String, ClientState> ouathState = new Hashtable<>();
    private final SecureRandom secureRandom = new SecureRandom();

    private final OutboundAuditEventLogger auditLogger;


    public AuthHandler(String callback, String clientId, String clientSecret, String jwtSecret, ModMailLogDB modMailLogDB, OutboundAuditEventLogger auditLogger) {
        this.auditLogger = auditLogger;
        this.service = new ServiceBuilder(clientId)
                .apiSecret(clientSecret)
                .defaultScope("identify guilds.members.read")
                .callback(callback)
                .userAgent("modmail-viewer_" + ModmailViewer.COMMIT_ID)
                .httpClient(new OkHttpHttpClient(client))
                .build(DiscordApi.instance());
        this.jwtAuth = new JwtAuth(jwtSecret);
        AuthHandler.modMailLogDB = modMailLogDB;
    }

    public static Role getUserRole(UserToken token) {
        try {
            return modMailLogDB.getUserOrGuildRole(token, token.discordRoles);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Role getUserRole(Context ctx) {
        var jwtCookie = ctx.cookie("jwt");
        if (jwtCookie != null) {
            try {
                var user = JwtAuth.decodeAndVerifyJWT(jwtCookie, objectMapper);
                logger.trace("user = {}", user);
                return modMailLogDB.getUserOrGuildRole(user, user.discordRoles);
            } catch (Exception e) {
                logger.error(e);
                return Role.ANYONE;
            }
        } else {
            logger.debug("could not get user role, no jwt cookie present, users role is ANYONE");
            return Role.ANYONE;
        }
    }

//    public static GuildMember getUserGuildInformation() {
////        /users/@me/guilds/{guild.id}/member
//        var request = new Request.Builder()
//                .url(String.format("https://discord.com/api/v10/users/@me/guilds/%s/member", Config.DISCORD_GUILD_ID))
//                .build();
//        try (var response = client.newCall(request).execute()) {
//            var body = response.body().string();
//            logger.trace("/users/@me/guilds/{guild.id}/member body: {}", body);
//            return objectMapper.readValue(body, GuildMember.class);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }

    public static UserToken getUser(Context ctx) throws JsonProcessingException {
        var jwtCookie = ctx.cookie("jwt");
        if (jwtCookie == null) {
            logger.debug("could not get user, no jwt cookie present");
            return new UserToken(0L, "anonymous", "0000", "", null, false);
        }
        return JwtAuth.decodeAndVerifyJWT(jwtCookie, objectMapper);
    }

    public void HandleAuth(@NotNull Handler handler, @NotNull Context ctx, @NotNull Set<? extends RouteRole> routeRoles) throws Exception {
        logger.trace("{} roles: {}", ctx.endpointHandlerPath(), routeRoles);
        if (routeRoles.contains(Role.ANYONE)) {
            logger.trace("endpoint allows ANYONE");
            handler.handle(ctx);
            return;
        }

        Role userRole = AuthHandler.getUserRole(ctx);
        UserToken user = AuthHandler.getUser(ctx);

        logger.debug("User token was {}", user);
        logger.debug("User id{} @ {} had role {}", user.getId(), ctx.ip(), userRole);
        if (routeRoles.contains(userRole)) {
            handler.handle(ctx);
            logger.debug("User id{} @ {} was authorized and had request handled", user.getId(), ctx.ip());
        } else if (userRole != Role.ANYONE || user.isRealUser()) {
            logger.debug("User id{} @ {} with role {} was not authorized to view {}", user.getId(), ctx.ip(), userRole, ctx.endpointHandlerPath());
            throw new UnauthorizedResponse();
        } else {
            logger.debug("Redirected {} to auth URL from {}", ctx.ip(), ctx.url());
            ctx.header("X-Auth-Redirect", "1");
            // Handling for Unpoly, if we detect a request coming from it, we send a 401 instead of redirecting, which would get hit by CORS
            // our frontend code can handle a 401 and specifically fully load the page to get a redirect to where we wanted to go.

            if (ctx.header("x-up-version") != null) {
                throw new UnauthorizedResponse();
            }
            ctx.redirect(service.getAuthorizationUrl(generateOAuthState(ctx)), HttpStatus.TEMPORARY_REDIRECT);
        }
    }


    private String generateOAuthState(Context ctx) {
        var key = new BigInteger(130, secureRandom).toString(32);
        var state = new ClientState(ctx.fullUrl());
        ouathState.put(key, state);
        ctx.cookie(new Cookie("state", key, "/", -1, Config.isCookiesSecure, 1, true, "", "", SameSite.LAX));
        return key;
    }

    private ClientState getAndVerifyOauthState(Context ctx) throws OAuthException {
        var stateKey = ctx.cookie("state");
        if (stateKey == null) {
            throw new InvalidStateException("No client state was present.");
        }
        var state = ouathState.get(stateKey);
        if (state == null) {
            throw new InvalidStateException("Client state was invalid.");
        }
        if (Instant.now().isAfter(state.getTimeGenerated().plus(3, ChronoUnit.MINUTES))) {
            ouathState.remove(stateKey);
            throw new InvalidStateException("State was expired.");
        }
        ouathState.remove(stateKey);
        ctx.removeCookie("state");
        return state;
    }

    public void handleGenerateJWT(Context ctx, UserToken user, long[] roles) throws JsonProcessingException {

        // Same site strict cause browser not to send the cookie upon redirect from oauth
        // Which would mean we would need load a page that redirects the user with js
        var jwt = jwtAuth.generateJWT(user, roles);
        ctx.cookie(new Cookie("jwt", jwt, "/", 10800, Config.isCookiesSecure, 1, true, "", "", SameSite.LAX));
        logger.trace("new JWT generated with value {}", jwt);
    }

    public void handleCallback(Context ctx) throws IOException, ExecutionException, InterruptedException {
        // TODO This rate limiter naively accepts x-forwarded-for headers, either document security implications or replace it with a better implementation
        NaiveRateLimit.requestPerTimeUnit(ctx, 2, TimeUnit.MINUTES);

        var code = ctx.queryParam("code");
        var state = ctx.queryParam("state");
        if (state == null) {
            ctx.status(400).result("invalid state");
        }
        try {
            var clientState = getAndVerifyOauthState(ctx);
            logger.debug("clients state was: {}", clientState);
            if (code != null) {
                logger.trace("code: {}", code);
                var token = service.getAccessToken(code);
                logger.trace("token {}", token.getRawResponse());
                var userRequest = new OAuthRequest(Verb.GET, "https://discord.com/api/users/@me");
                service.signRequest(token, userRequest);
                try (Response userResponse = service.execute(userRequest)) {
                    logger.trace(userResponse.getCode());
                    logger.trace(userResponse.getBody());

                    var user = objectMapper.readValue(userResponse.getBody(), UserToken.class);

                    // Get and map the guild data
                    var guildRequest = new OAuthRequest(Verb.GET, String.format("https://discord.com/api/v10/users/@me/guilds/%s/member", Config.DISCORD_GUILD_ID));
                    service.signRequest(token, guildRequest);
                    Response guildResponse = service.execute(guildRequest);
                    logger.trace(guildResponse.getBody());
                    var guild = objectMapper.readValue(guildResponse.getBody(), GuildMember.class);

                    // The user is not authorized for any role and thus does not need to be given a token
                    var role = modMailLogDB.getUserOrGuildRole(user, guild.roles());
                    if (role == Role.ANYONE) {
                        logger.debug("User signed in through discord but was not authorized");
                        ctx.status(403).result();
                        return;
                    }

                    this.auditLogger.pushEvent(
                          new AuditEvent.Builder("viewer.login")
                                .fromCtx(ctx)
                                .withDescription("User logged in through discord")
                                .withUserId(user.getId())
                                .withUsername(user.getUsername() + DiscordUtils.getDiscriminatorString(user))
                                .withRole(role)
                                .build());

                    ctx.result(userResponse.getBody());
                    handleGenerateJWT(ctx, user, guild.roles());
                    ctx.redirect(clientState.getRedirectedFrom());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } else {
                ctx.status(400).result("invalid redirect, no code present");
            }
        } catch (InvalidStateException e) {
            logger.error(e);
            ctx.status(400).result("Invalid State: " + e.getMessage());

        } catch (OAuthException e) {
            logger.error(e);
            ctx.status(500);
        }
    }
}

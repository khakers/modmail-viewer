# Modmail Viewer
[![Discord](https://img.shields.io/discord/1040471918639337577?label=Discord&logo=Discord)](https://discord.gg/V5HA9N5Dn8)
[![GitHub latest release (latest SemVer)](https://img.shields.io/github/v/release/khakers/modmail-viewer?logo=Git&logoColor=f5f5f5)](https://github.com/khakers/modmail-viewer/releases/latest)
[![GitHub Help Wanted issues](https://img.shields.io/github/issues/khakers/modmail-viewer/help%20wanted?color=blue)](https://github.com/khakers/modmail-viewer/issues?q=is%3Aissue+is%3Aopen+label%3A%22help+wanted%22)


[![Deploy on Railway](https://railway.app/button.svg)](https://railway.app/template/9oZTzt?referralCode=Bs9kVa)

A drop in alternative frontend for your [modmail-dev/modmail bot](https://github.com/modmail-dev/modmail) instance with built-in Discord authentication, advanced features and better mobile support.

Built from scratch using Javalin, JTE, Bootstrap 5, and Unpoly.


[Demo Site running the latest commit from Master](https://demo.modmail-viewer.khakers.dev/logs/1bef379de4ac)


![modmail-logviewer-log](https://user-images.githubusercontent.com/22665282/211416462-676f67a2-b818-4b8a-9eb6-f855f3bf64b9.png)
![log browsing](https://user-images.githubusercontent.com/22665282/211415993-c1f572f1-f36e-4579-aa84-64a2622ef8ab.png)


_Modmail-Viewer_ is currently a beta. You should always read update notes before applying updates as there may be breaking changes between major versions, and ensure you are running an up to date version to have the latest fixes and support.

Report any bugs you encounter via github issues at this repository (not modmail or logviewer).  
Use GitHub discussions for feature requests or to ask questions. If you'd rather not ask on github, you can join the [Discord server](https://discord.gg/V5HA9N5Dn8) instead and ask there. Please prefer not to directly DM me as it's not discoverable for other people that need help and I will likely not notice it for a while.

## Features

* Discord OAuth2 authentication **based on your modmail roles**. See [the wiki page on permissions.](https://github.com/khakers/modmail-viewer/wiki/Permissions) 
* Browsable paginated logs sorted by most recent message and filtered by status.
* Mobile friendly design.
* Full message Discord Markdown formatting (Including spoilers, custom emojis, and timestamps).
* Customizable Branding
* Dark/Light Themes
* Log text search
* HTTPS support 
* NSFW warnings
* Image spoiler support


## Future Ideas

* Stats dashboard
* Internationalization
  * If you want internationalization and are interested in providing translations, please contact me and/or open an issue/discussion on GitHub.
* Basic API
* Snippet editor (will likely require a bot plugin)
* Persistent Notes editor 
* Configurable log sorting

## Self-hosting

You should place your _modmail-viewer_ instance behind Cloudflare or some other reverse proxy (such as Caddy) to provide
automatic https OR provide the required keys/certs to the application yourself if you plan on using authentication.

### Docker

Running the application with Docker is the fastest and best supported way to run _modmail-viewer_. Additionally, you
can easily run it using any 3rd party service that supports running docker containers.

#### Docker Run

To quickly test _modmail-viewer_, you can use this docker run command. Make sure to substitute the environment variables
for what is applicable to you.

```shell
docker run --name modmail-viewer -p 80:80 \
  --env "MODMAIL_VIEWER_MONGODB_URI=mongodb://mongo:27017" \
  --env "MODMAIL_VIEWER_URL=http://127.0.0.1" \
  --env "MODMAIL_VIEWER_DISCORD_OAUTH_CLIENT_ID=1234" \
  --env "MODMAIL_VIEWER_DISCORD_OAUTH_CLIENT_SECRET=abcd" \
  -d ghcr.io/khakers/modmail-viewer:latest
```

#### Docker Compose

For long term ease of use, it's highly recommended that you use docker compose.
An example docker compose for running only _modmail-logviewer_.
> **Note**
> See [docker-compose.yml](docker-compose.yml) for a compose file that can run modmail, modmail-viewer, and mongodb.

```yaml
version: "3.7"
services:
  viewer:
    image: ghcr.io/khakers/modmail-viewer:latest
    env_file:
      - .env
    ports:
      - "80:80"
      # uncomment if using SSL
      #- "443:443"
    environment:
      - "MODMAIL_VIEWER_MONGODB_URI=mongodb://mongo:27017"
      - "MODMAIL_VIEWER_URL=http://127.0.0.1"
```

##### .env example

Should be located next to your docker-compose.yml, contains secrets.

> **Note**
> You'll need to create a Discord application (or use the one you created for modmail) and retrieve your OAuth2 client ID and Client secret from the OAuth2 section of the [developer dashboard](https://discord.com/developers/applications)
> Additionally, you must add a redirect URI to your Discord applications OAuth2 Redirects. This depends on how you access your site but will look something like "https://"modmail.example.com/callback" 

```properties
MODMAIL_VIEWER_DISCORD_OAUTH_CLIENT_ID=123456789
MODMAIL_VIEWER_DISCORD_OAUTH_CLIENT_SECRET=67234rtg3b2otgfhbn3298t7h
MODMAIL_VIEWER_SECRETKEY=secret
```

### Other

Before getting started, you'll need:

* A JDK/JRE 17 installation

Download the latest release from GitHub and unzip/tar the modmail-viewer archive

Run the webserver via the included scripts `bin/modmail-viewer` on linux, and `bin/modmail-viewer.bat` on windows.

It's recommended to either set up a reverse proxy (such as Cloudflare or Caddy) in front of your _modmail-logviewer_
instance, or enable SSL and provide your own certificate and private key. You can generate one for your domain
automatically with Certbot.

To run the webserver in the background, it's recommended you use a service manager such as Systemd.

## Configuration

### Permissions

See [the wiki page on permissions.](https://github.com/khakers/modmail-viewer/wiki/Permissions)

### Environment Variables

| Environment Variable                       | Description                                                                                                                                                                                                                                                         |
|--------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| MODMAIL_VIEWER_URL                         | The URL your modmail viewer instance is reachable at (i.e http://127.0.0.1)                                                                                                                                                                                         |
| MODMAIL_VIEWER_MONGODB_URI                 | URI for the MongoDB instance                                                                                                                                                                                                                                        |
| MODMAIL_VIEWER_DISCORD_OAUTH_CLIENT_ID     | Your Discord Application ID. Can be skipped if authentication is disabled.                                                                                                                                                                                          |
| MODMAIL_VIEWER_DISCORD_OAUTH_CLIENT_SECRET | Your Discord OAuth2 client secret                                                                                                                                                                                                                                   |
| MODMAIL_VIEWER_DISCORD_GUILD_ID            | The ID of your discord Guild. This should be whatever guild has relevant discord Roles, as it checks the users roles in the provided guild                                                                                                                          |
| MODMAIL_VIEWER_SECRETKEY                   | A randomly generated secret key used for signing auth tokens. <br/>**ANYONE WITH THIS KEY CAN FORGE AUTHENTICATION TOKENS** and impersonate any user. Should be at least 32 characters. If you don't provide one, sessions will not persist across restarts.        |
| MODMAIL_VIEWER_DEV                         | Enables development features. Do not enable in production. Requires a JDK.                                                                                                                                                                                          |
| MODMAIL_VIEWER_AUTH_ENABLED                | Set to `false` to completely disable authentication. May break some features.                                                                                                                                                                                       |
| MODMAIL_VIEWER_SSL                         | Enables SSL and HTTP/2 when connected via https.                                                                                                                                                                                                                    |
| MODMAIL_VIEWER_HTTPS_ONLY                  | Disables the http port and redirects all connections to https. Enabled by default if SSL is enabled. Does not function on localhost.                                                                                                                                |
| MODMAIL_VIEWER_SSL_CERT                    | Path to the SSL certificate pem file. Does not hot reload.                                                                                                                                                                                                          |
| MODMAIL_VIEWER_SSL_KEY                     | Path to the SSL certificate private key pem file. Does not hot reload.                                                                                                                                                                                              |
| MODMAIL_VIEWER_HTTP_PORT                   | Port HTTP traffic will be served at. Defaults to 80                                                                                                                                                                                                                 |
| MODMAIL_VIEWER_HTTPS_PORT                  | Port HTTPS traffic will be served at. Defaults to 443                                                                                                                                                                                                               |
| MODMAIL_VIEWER_SNI                         | Set SNI to be enabled/disabled. Requires SSL to be enabled                                                                                                                                                                                                          |
| MODMAIL_VIEWER_STS                         | Set to true to enable Strict Transport Security. If you cannot use https to connect or may not always be able to this is a dangerous option as it will stop browsers from connecting via http. You should enable this if you always plan on using https to connect. |
| MODMAI_VIEWER_INSECURE                     | Disable setting cookies as secure. **Only do this if your site is *only* accessible via http**                                                                                                                                                                      |
| MODMAIL_VIEWER_BRANDING                    | Text to display in the navbar title. Will display "Modmail-Viewer" by default                                                                                                                                                                                       |
| MODMAIL_VIEWER_LOG_LEVEL                   | Set the application log level (INFO, DEBUG, ERROR, TRACE)                                                                                                                                                                                                           |
| MODMAIL_VIEWER_ANALYTICS                   | Allows you to add the html for your analytics solution. This is inserted at the end of the head (and should thus likely be marked as defer for performance reasons)                                                                                                 |
| MODMAIL_VIEWER_ANALYTICS_BASE64            | Same as the above, but expects it's value to be base64 encoded. This is exists in the off chance that you run into character encoding issues using the standard variable. Will only work if standard option is provited                                             |
| MODMAIL_VIEWER_CSP                         | Overwrites the default Content Security Policy with whatever you provide. The CSP is a security feature do not overwrite it unless you know what you're doing.                                                                                                     |
| MODMAIL_VIEWER_CSP_SCRIPT_SRC_ELEM_EXTRA   | Adds provided value to the CSP script-src-element section. You'll probably need this if you plan on adding an analytics script.                                                                                                                                     |


## Attribution 
This project uses graphics from [Twemoji](https://twemoji.twitter.com/) licensed under CC-BY 4.0: https://creativecommons.org/licenses/by/4.0/

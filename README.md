# Modmail Viewer

A Java based frontend for your [Modmail bot](https://github.com/kyb3r/modmail) instance. Serves as an alternative
to [logviewer](https://github.com/kyb3r/logviewer).

Built using Javalin, JTE, and Bootstrap 5.

## Features

* Discord authentication
* Browsable paginated logs
* (mostly) Mobile friendly design

## Roadmap

* Stats dashboard
* Filtering logs by open/closed
* Basic API
* Dark theme
* Message markdown and mention formatting
* Search

## Self hosting

### Docker

Running the application with docker is

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
An example docker compose for running only modmail-logviewer.
> **Note**
> See [docker-compose.yml](docker-compose.yml) for a compose file that can run modmail, modmail-viewer, and mongodb.

```yaml
version: "3.7"
services:
  viewer:
    image: ghcr.io/khakers/modmail-viewer:latest
    depends_on:
      - mongo
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

### Environment Variables

| Environment Variable                       | Description                                                                                                                                                                                                                                                  |
|--------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| MODMAIL_VIEWER_URL                         | The URL your modmail viewer instance is reachable at (i.e http://127.0.0.1)                                                                                                                                                                                  |
| MODMAIL_VIEWER_MONGODB_URI                 | URI for the MongoDB instance                                                                                                                                                                                                                                 |
| MODMAIL_VIEWER_DISCORD_OAUTH_CLIENT_ID     | Your Discord Application ID. Can be skipped if authentication is disabled.                                                                                                                                                                                   |
| MODMAIL_VIEWER_DISCORD_OAUTH_CLIENT_SECRET | Your Discord OAuth2 client secret                                                                                                                                                                                                                            |
| MODMAIL_VIEWER_SECRETKEY                   | A randomly generated secret key used for signing auth tokens. <br/>**ANYONE WITH THIS KEY CAN FORGE AUTHENTICATION TOKENS** and impersonate any user. Should be at least 32 characters. If you don't provide one, sessions will not persist across restarts. |
| MODMAIL_VIEWER_DEV                         | Enables development features. Do not enable in production.                                                                                                                                                                                                   |
| MODMAIL_VIEWER_AUTH_ENABLED                | Set to `false` to completely disable authentication. May break some features.                                                                                                                                                                                |
| MODMAIL_VIEWER_SSL                         | Enables SSL and HTTP/2 when connected via https.                                                                                                                                                                                                             |
| MODMAIL_VIEWER_HTTPS_ONLY                  | Disables the http port and redirects all connections to https. Enabled by default if SSL is enabled. Does not function on localhost.                                                                                                                         |
| MODMAIL_VIEWER_SSL_CERT                    | Path to the SSL certificate pem file. Does not hot reload.                                                                                                                                                                                                   |
| MODMAIL_VIEWER_SSL_KEY                     | Path to the SSL certificate private key pem file. Does not hot reload.                                                                                                                                                                                       |



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

To quickly test modmail-viewer, you can use this docker run command. Make sure to substitute the environment variables
for what is applicable to you.

```shell
docker run --name modmail-viewer -p 80:7070 \
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
      - "7070:7070"
    environment:
      - "MODMAIL_VIEWER_MONGODB_URI=mongodb://mongo:27017"
      - "MODMAIL_VIEWER_URL=http://127.0.0.1:7070"
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

You should set up a reverse proxy or cloudflare in front of your instance to provide https

To run the webserver in the background, it's recommended you use a service manager such as systemd.

### Environment Variables

| Environment Variable                       | Description                                                                                                                                                                                                                                                  |
|--------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| MODMAIL_VIEWER_URL                         | The URL your modmail viewer instance is reachable at (i.e http://127.0.0.1:7070)                                                                                                                                                                             |
| MODMAIL_VIEWER_MONGODB_URI                 | URI for the MongoDB instance                                                                                                                                                                                                                                 |
| MODMAIL_VIEWER_DISCORD_OAUTH_CLIENT_ID     | Your Discord Application ID                                                                                                                                                                                                                                  |
| MODMAIL_VIEWER_DISCORD_OAUTH_CLIENT_SECRET | Your Discord OAuth2 client secret                                                                                                                                                                                                                            |
| MODMAIL_VIEWER_SECRETKEY                   | A randomly generated secret key used for signing auth tokens. <br/>**ANYONE WITH THIS KEY CAN FORGE AUTHENTICATION TOKENS** and impersonate any user. Should be at least 32 characters. If you don't provide one, sessions will not persist across restarts. |
| MODMAIL_VIEWER_DEV                         | Enabled development features                                                                                                                                                                                                                                 |
| MODMAIL_VIEWER_AUTH_ENABLED                | Set to `false` to completely disable authentication. May break some features                                                                                                                                                                                 |



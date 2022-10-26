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

## Selfhosting

### Docker

Soon™

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
| modmail.viewer.url                         | The URL your modmail viewer instance is reachable at (i.e http://127.0.0.1:7070)                                                                                                                                                                             |
| modmail.viewer.mongodb.uri                 | URI for the MongoDB instance                                                                                                                                                                                                                                 |
| modmail.viewer.discord.oauth.client.id     | Your Discord Application ID                                                                                                                                                                                                                                  |
| modmail.viewer.discord.oauth.client.secret | Your Discord OAuth2 client secret                                                                                                                                                                                                                            |
| modmail.viewer.secretkey                   | A randomly generated secret key used for signing auth tokens. <br/>**ANYONE WITH THIS KEY CAN FORGE AUTHENTICATION TOKENS** and impersonate any user. Should be at least 32 characters. If you don't provide one, sessions will not persist across restarts. |



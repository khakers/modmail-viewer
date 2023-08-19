## [UNRELEASED] - 2023-8-18

All configuration keys have been changed with the migration of the configuration system.
See below for a list of keys and their new values.

| Previous Key                               | New Key                                     |
|--------------------------------------------|---------------------------------------------|
| MODMAIL_VIEWER_URL                         | MODMAIL_VIEWER_APP_URL                      |
| MODMAIL_VIEWER_MONGODB_URI                 | MODMAIL_VIEWER_APP_MONGODB_URI              |
| MODMAIL_VIEWER_DISCORD_OAUTH_CLIENT_ID     | MODMAIL_VIEWER_APP_DISCORD_CLIENT_ID        |
| MODMAIL_VIEWER_DISCORD_OAUTH_CLIENT_SECRET | MODMAIL_VIEWER_APP_DISCORD_CLIENT_SECRET    |
| MODMAIL_VIEWER_DISCORD_GUILD_ID            | MODMAIL_VIEWER_APP_DISCORD_GUILD_ID         |
| MODMAIL_VIEWER_SECRETKEY                   | MODMAIL_VIEWER_APP_AUTH_SECRETKEY           |
| MODMAIL_VIEWER_DEV                         | MODMAIL_VIEWER_APP_DEV                      |
| MODMAIL_VIEWER_AUTH_ENABLED                | MODMAIL_VIEWER_APP_AUTH_ENABLED             |
| MODMAIL_VIEWER_SSL                         | MODMAIL_VIEWER_APP_SSL_ENABLED              |
| MODMAIL_VIEWER_HTTPS_ONLY                  | MODMAIL_VIEWER_APP_SSL_HTTPS_ONLY           |
| MODMAIL_VIEWER_SSL_CERT                    | MODMAIL_VIEWER_APP_SSL_CERT                 |
| MODMAIL_VIEWER_SSL_KEY                     | MODMAIL_VIEWER_APP_SSL_KEY                  |
| MODMAIL_VIEWER_HTTP_PORT                   | MODMAIL_VIEWER_APP_PORT_HTTP                |
| MODMAIL_VIEWER_HTTPS_PORT                  | MODMAIL_VIEWER_APP_PORT_HTTPS               |
| MODMAIL_VIEWER_SNI                         | MODMAIL_VIEWER_APP_SSL_SNI                  |
| MODMAIL_VIEWER_STS                         | MODMAIL_VIEWER_APP_SSL_STS                  |
| MODMAIL_VIEWER_INSECURE                    | MODMAIL_VIEWER_APP_SECURE_COOKIES           |
| MODMAIL_VIEWER_BRANDING                    | **AWAITING MIGRATION**                      |
| MODMAIL_VIEWER_LOG_LEVEL                   | **NO CHANGE**                               |
| MODMAIL_VIEWER_ANALYTICS                   | **AWAITING MIGRATION**                      |
| MODMAIL_VIEWER_ANALYTICS_BASE64            | **DEPRECATED**                              |
| MODMAIL_VIEWER_BOT_ID                      | MODMAIL_VIEWER_APP_BOT_ID                   |
| MODMAIL_VIEWER_CSP                         | MODMAIL_VIEWER_APP_CSP_OVERRIDE             |
| MODMAIL_VIEWER_CSP_SCRIPT_SRC_ELEM_EXTRA   | MODMAIL_VIEWER_APP_CSP_EXTRA_SCRIPT_SOURCES |

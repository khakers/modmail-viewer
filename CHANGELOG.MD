# Changelog
Based on [common changelog spec](https://common-changelog.org/)

## [Unreleased]

_If you are upgrading: please see [`UPGRADING.md`](UPGRADING.md)._


### Changed

- Migrate spoilers to a webcomponent
- Refactor search box for main logs page. 
- Improve detection and handling of invalid Discord avatars.
- Rewrote config caching.
- Fix log searchbox sizing with large ticket quantities.
- Login state no longer has a set max age.
- Hide "license" link from footer.
- Bump `Javalin` from 5.4.2 to 5.6.1
- Bump `flexmark` from 0.64.0 to 0.64.2
- Bump `jackson` from 2.12.4 to 2.13.0
- Bump `log4j2` from 2.19.0 to 2.20.0
- Bump `java-jwt` from 4.2.1 to 4.4.0
- Bump `jte` from 2.2.3 to 3.0.2
- Bump `unpoly` from 3.0.0-rc2 to 3.3.0
- Bump `bootstrap` from 5.3.0-alpha1 to 5.3.0


### Added

- Add Dashboard page. ([#92](https://github.com/khakers/modmail-viewer/pull/92))
- Add naive rate limit for login attempts on /callback.
- Add Audit logging. ([[#69](https://github.com/khakers/modmail-viewer/issues/69))
- Add CSP policy.
- Add security headers
- Add automatic update checks and nag for admin users,
- Add message actions dropdown.
- Support for copying DM message links when enhanced support is present.
- Add selector for amount of tickets to show per page.

### Removed

### Fixed

- Fix MongoDB timestamp parsing issue. ([#89](https://github.com/khakers/modmail-viewer/issues/89))

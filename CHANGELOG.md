# Changelog
Based on [common changelog spec](https://common-changelog.org/)

## [Unreleased]

_If you are upgrading: please see [`UPGRADING.md`](UPGRADING.md)._
 

### Changed

- **Breaking:** Migrate configuration system to Gestalt.
- **Breaking:** Change default http port to 7080.
- **Breaking:** Change default https port to 7443.

### Added

### Removed

- Remove support for automatic secret key generation. You must always provide a valid key when using authentication.
- Deprecate support for `MODMAIL_VIEWER_ANALYTICS_BASE64` configuration key. Use `MODMAIL_VIEWER_ANALYTICS` with ${base64Decode:}.

### Fixed

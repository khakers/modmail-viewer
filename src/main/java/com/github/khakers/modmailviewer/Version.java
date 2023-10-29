package com.github.khakers.modmailviewer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.regex.Pattern;

class Version implements Comparable<Version> {

    private static final Pattern SEMVER_PATTERN = Pattern.compile("^(?<major>0|[1-9]\\d*)\\.(?<minor>0|[1-9]\\d*)\\.(?<patch>0|[1-9]\\d*)(?:-(?<prerelease>(?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*)(?:\\.(?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?(?:\\+(?<buildmetadata>[0-9a-zA-Z-]+(?:\\.[0-9a-zA-Z-]+)*))?$");
    int major;
    int minor;
    int patch;
    @Nullable
    String prerelease;
    @Nullable
    String metaData;

    public Version(@NotNull String prerelease) {
        var matcher = SEMVER_PATTERN.matcher(prerelease);
        matcher.find();
        this.major = Integer.parseInt(matcher.group("major"));
        this.minor = Integer.parseInt(matcher.group("minor"));
        this.patch = Integer.parseInt(matcher.group("patch"));
        this.prerelease = matcher.group("prerelease");
        this.metaData = matcher.group("buildmetadata");

    }

    private static boolean isDigitString(String str) {
        for (char c : str.toCharArray()) {
            if (!Character.isDigit(c)) {
                return false;
            }
        }
        return true;
    }

    public String asVersionString() {
        var string = new StringBuilder();
        string
              .append(this.major)
              .append('.')
              .append(this.minor)
              .append('.')
              .append(this.patch);
        if (this.prerelease != null && !this.prerelease.isBlank()) {
            string.append("-").append(this.prerelease);
        }

        if (this.metaData != null && !this.metaData.isBlank()) {
            string.append("-").append(this.metaData);
        }
        return string.toString();
    }

    /**
     * Compares this version to the given version.
     * Versions that are the same return false.
     *
     * @param version the version to compare to
     * @return true if this version is newer than the given version
     */
    public boolean isNewerThan(Version version) {
        return this.compareTo(version) > 0;
    }

    /**
     * Compares this version to the given version.
     * Versions that are the same return false.
     *
     * @param version the version to compare to
     * @return true if this version is older than the given version
     */
    public boolean isOlderThan(Version version) {
        return this.compareTo(version) < 0;
    }

    public boolean isStable() {
        return this.prerelease == null;
    }

    /**
     *  Compares this version to the given version.
     *  Versions that are exactly the same return true.
     *  Versions that are equal for the purposes of semver may return false.
     * <p>
     *  This method takes into account build Metadata.
     *
     * @param o the object to compare to
     * @return true if the object is a version and is equal to this version
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Version version = (Version) o;
        return major == version.major && minor == version.minor && patch == version.patch && Objects.equals(prerelease, version.prerelease) && Objects.equals(metaData, version.metaData);
    }

    @Override
    public int hashCode() {
        return Objects.hash(prerelease, metaData, major, minor, patch);
    }

    @Override
    public String toString() {
        return "Version{" +
              "major=" + major +
              ", minor=" + minor +
              ", patch=" + patch +
              ", prerelease='" + prerelease + '\'' +
              ", versionString='" + asVersionString() + '\'' +
              ", metaData='" + metaData + '\'' +
              '}';
    }

    /**
     * Calculates the difference between two versions.
     * <p>
     * PRERELEASE versions are compared lexicographically.
     * Per semver, a prerelease version is always less than a stable version of the same major.minor.patch
     *
     * <p>
     * Compares this object with the specified object for order.  Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * <p>The implementor must ensure {@link Integer#signum
     * signum}{@code (x.compareTo(y)) == -signum(y.compareTo(x))} for
     * all {@code x} and {@code y}.  (This implies that {@code
     * x.compareTo(y)} must throw an exception if and only if {@code
     * y.compareTo(x)} throws an exception.)
     *
     * <p>The implementor must also ensure that the relation is transitive:
     * {@code (x.compareTo(y) > 0 && y.compareTo(z) > 0)} implies
     * {@code x.compareTo(z) > 0}.
     *
     * <p>Finally, the implementor must ensure that {@code
     * x.compareTo(y)==0} implies that {@code signum(x.compareTo(z))
     * == signum(y.compareTo(z))}, for all {@code z}.
     *
     * @param version the object to be compared.
     * @return a negative integer, zero, or a positive integer as this object
     * is less than, equal to, or greater than the specified object.
     * @throws NullPointerException if the specified object is null
     * @throws ClassCastException   if the specified object's type prevents it
     *                              from being compared to this object.
     * @apiNote It is strongly recommended, but <i>not</i> strictly required that
     * {@code (x.compareTo(y)==0) == (x.equals(y))}.  Generally speaking, any
     * class that implements the {@code Comparable} interface and violates
     * this condition should clearly indicate this fact.  The recommended
     * language is "Note: this class has a natural ordering that is
     * inconsistent with equals."
     */
    @Override
    public int compareTo(@NotNull Version version) {
        /*
        https://semver.org/#spec-item-11

        Precedence refers to how versions are compared to each other when ordered.

            Precedence MUST be calculated by separating the version into major, minor, patch and pre-release identifiers
            in that order (Build metadata does not figure into precedence).

            Precedence is determined by the first difference when comparing each of these identifiers from left to right
            as follows: Major, minor, and patch versions are always compared numerically.

            Example: 1.0.0 < 2.0.0 < 2.1.0 < 2.1.1.

            When major, minor, and patch are equal, a pre-release version has lower precedence than a normal version:

            Example: 1.0.0-alpha < 1.0.0.

            Precedence for two pre-release versions with the same major, minor, and patch version MUST be determined by
            comparing each dot separated identifier from left to right until a difference is found as follows:

                Identifiers consisting of only digits are compared numerically.

                Identifiers with letters or hyphens are compared lexically in ASCII sort order.

                Numeric identifiers always have lower precedence than non-numeric identifiers.

                A larger set of pre-release fields has a higher precedence than a smaller set, if all of the preceding identifiers are equal.

            Example: 1.0.0-alpha < 1.0.0-alpha.1 < 1.0.0-alpha.beta < 1.0.0-beta < 1.0.0-beta.2 < 1.0.0-beta.11 < 1.0.0-rc.1 < 1.0.0.
         */

        if (this.equals(version))
            return 0;

        if (this.major > version.major)
            return 1;
        else if (this.major < version.major) {
            return -1;
        }

        if (this.minor > version.minor)
            return 1;
        else if (this.minor < version.minor) {
            return -1;
        }

        if (this.patch > version.patch)
            return 1;
        else if (this.patch < version.patch) {
            return -1;
        }

        // A prerelease version is always less than a stable version of the same major.minor.patch
        if (this.isStable() && !version.isStable()) {
            return 1;
        } else if (!this.isStable() && version.isStable()) {
            return -1;
        }

        if (this.prerelease != null && version.prerelease != null) {
            /*
                Precedence for two pre-release versions with the same major, minor, and patch version MUST be determined
                by comparing each dot separated identifier from left to right until a difference is found as follows:

                    Identifiers consisting of only digits are compared numerically.

                    Identifiers with letters or hyphens are compared lexically in ASCII sort order.

                    Numeric identifiers always have lower precedence than non-numeric identifiers.

                    A larger set of pre-release fields has a higher precedence than a smaller set,
                    if all of the preceding identifiers are equal.
             */

            // separate prerelease identifiers by dot from left to right
            var thisPrereleaseIdentifiers = this.prerelease.split("\\.");
            var versionPrereleaseIdentifiers = version.prerelease.split("\\.");
            for (int i = 0; i < thisPrereleaseIdentifiers.length; i++) {
                if (i >= versionPrereleaseIdentifiers.length) {
                    // this prerelease version has more identifiers than the other prerelease version
                    return 1;
                }

                // Identifiers consisting of only digits are compared numerically.
                if (isDigitString(thisPrereleaseIdentifiers[i]) && isDigitString(versionPrereleaseIdentifiers[i])) {
                    var result = Integer.compare(
                          Integer.parseInt(thisPrereleaseIdentifiers[i]),
                          Integer.parseInt(versionPrereleaseIdentifiers[i]));
                    if (result != 0) {
                        return result;
                    }
                }

                // Identifiers with letters or hyphens are compared lexically in ASCII sort order.
                var result = thisPrereleaseIdentifiers[i].compareTo(versionPrereleaseIdentifiers[i]);
                if (result != 0) {
                    return result;
                }
            }


            // return reverse comparison because prerelease versions are compared lexicographically
//            return -version.prerelease.compareTo(this.prerelease);
        }

        return 0;
    }
}

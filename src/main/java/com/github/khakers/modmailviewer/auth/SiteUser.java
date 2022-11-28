package com.github.khakers.modmailviewer.auth;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;
import java.util.Optional;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SiteUser {
    long id;
    String username;
    String discriminator;
    String avatar;

    boolean isRealUser = true;

//    Role role;

    /**
     * Generates a fake SiteUSer with isRealUser set to false.
     */
    public SiteUser() {
        this(0L, "anonymous", "0000", "");
        this.isRealUser = false;
    }

    @JsonCreator
    public SiteUser(@JsonProperty("id") long id, @JsonProperty("username") String username, @JsonProperty("discriminator") String discriminator, @JsonProperty("avatar") String avatar) {
        this.id = id;
        this.username = username;
        this.discriminator = discriminator;
        this.avatar = avatar;
//        this.role = role;
    }

    public long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getDiscriminator() {
        return discriminator;
    }

    public Optional<String> getAvatar() {
        return Optional.ofNullable(avatar);
    }

    public boolean isRealUser() {
        return isRealUser;
    }

    public String getAvatarUrl() {
        if (avatar != null) {
            return String.format("https://cdn.discordapp.com/avatars/%s/%s.png", id, avatar);
        } else {
            return String.format("https://cdn.discordapp.com/embed/avatars/%d.png", Integer.parseInt(this.discriminator) % 5);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SiteUser siteUser = (SiteUser) o;
        return id == siteUser.id && username.equals(siteUser.username) && discriminator.equals(siteUser.discriminator) && avatar.equals(siteUser.avatar);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, username, discriminator, avatar);
    }

    @Override
    public String toString() {
        return "SiteUser{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", discriminator='" + discriminator + '\'' +
                ", avatar=" + avatar +
                '}';
    }
}

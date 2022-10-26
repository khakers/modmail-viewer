package com.github.khakers.modmailviewer.auth;

import java.time.Instant;
import java.util.Objects;

public class ClientState {
    private final Instant timeGenerated;
    private final String redirectedFrom;

    public ClientState(String redirectedFrom) {
        this.timeGenerated = Instant.now();
        this.redirectedFrom = redirectedFrom;
    }

    public Instant getTimeGenerated() {
        return timeGenerated;
    }

    public String getRedirectedFrom() {
        return redirectedFrom;
    }

    @Override
    public String toString() {
        return "ClientState{" +
                "timeGenerated=" + timeGenerated +
                ", redirectedFrom='" + redirectedFrom + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClientState that = (ClientState) o;
        return timeGenerated.equals(that.timeGenerated) && Objects.equals(redirectedFrom, that.redirectedFrom);
    }

    @Override
    public int hashCode() {
        return Objects.hash(timeGenerated, redirectedFrom);
    }
}

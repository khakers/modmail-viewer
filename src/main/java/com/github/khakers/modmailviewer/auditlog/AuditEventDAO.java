package com.github.khakers.modmailviewer.auditlog;

import com.github.khakers.modmailviewer.auditlog.event.AuditEvent;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface AuditEventDAO {
    List<AuditEvent> getAuditEvents();

    Optional<AuditEvent> getAuditEvent(String id);

    List<AuditEvent> searchAuditEvents(Instant rangeStart, Instant rangeEnd, List<Long> userIds, List<String> actions);
}

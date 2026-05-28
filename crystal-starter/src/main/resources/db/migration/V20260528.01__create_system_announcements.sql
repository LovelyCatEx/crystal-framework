CREATE TABLE system_announcements
(
    id            BIGINT       NOT NULL PRIMARY KEY,
    title         VARCHAR(256) NOT NULL,
    content       TEXT         NOT NULL,
    status        INT          NOT NULL DEFAULT 0,
    target        INT          NOT NULL DEFAULT 2,
    priority      INT          NOT NULL DEFAULT 0,
    created_time  BIGINT       NOT NULL,
    modified_time BIGINT       NOT NULL,
    deleted_time  BIGINT                DEFAULT NULL
);

CREATE INDEX idx_announcements_status_target ON system_announcements (status, target, deleted_time);
CREATE INDEX idx_announcements_created_time ON system_announcements (created_time);

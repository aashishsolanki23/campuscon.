CREATE TABLE tickets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    deed_id BIGINT NOT NULL,
    ticket_code VARCHAR(255) UNIQUE NOT NULL,
    qr_code_path TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_deed_id (deed_id),
    INDEX idx_ticket_code (ticket_code),
    UNIQUE KEY unique_user_deed (user_id, deed_id)
);

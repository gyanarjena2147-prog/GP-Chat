-- Clean schema if exists (Warning: Destructive)
DROP TABLE IF EXISTS call_logs CASCADE;
DROP TABLE IF EXISTS status_updates CASCADE;
DROP TABLE IF EXISTS messages CASCADE;
DROP TABLE IF EXISTS chats CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- Users Table
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    phone_number VARCHAR(20) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    status_text VARCHAR(255) DEFAULT 'Hey there! I am using WhatsApp.',
    profile_pic_url VARCHAR(500) DEFAULT '',
    is_online BOOLEAN DEFAULT false,
    last_seen TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Chats Table
CREATE TABLE chats (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    is_group BOOLEAN DEFAULT false,
    last_message_text TEXT DEFAULT '',
    last_message_time BIGINT DEFAULT 0,
    unread_count INT DEFAULT 0,
    is_muted BOOLEAN DEFAULT false,
    group_avatar_seed INT DEFAULT 0
);

-- Messages Table
CREATE TABLE messages (
    id SERIAL PRIMARY KEY,
    chat_id INT REFERENCES chats(id) ON DELETE CASCADE,
    sender_id INT REFERENCES users(id) ON DELETE SET NULL,
    sender_name VARCHAR(100) NOT NULL,
    text TEXT NOT NULL,
    timestamp BIGINT NOT NULL,
    status VARCHAR(20) DEFAULT 'SENT', -- SENT, DELIVERED, READ
    type VARCHAR(20) DEFAULT 'TEXT', -- TEXT, IMAGE, VIDEO, DOCUMENT, VOICE
    media_url VARCHAR(500),
    file_name VARCHAR(255),
    file_size VARCHAR(50),
    is_encrypted BOOLEAN DEFAULT true
);

-- Status Updates (Stories) Table
CREATE TABLE status_updates (
    id SERIAL PRIMARY KEY,
    user_id INT REFERENCES users(id) ON DELETE CASCADE,
    user_name VARCHAR(100) NOT NULL,
    user_profile_pic_url VARCHAR(500) DEFAULT '',
    status_text TEXT,
    status_media_url VARCHAR(500),
    timestamp BIGINT NOT NULL,
    is_viewed BOOLEAN DEFAULT false,
    status_color_hex VARCHAR(10) DEFAULT '#075E54'
);

-- Call Logs Table
CREATE TABLE call_logs (
    id SERIAL PRIMARY KEY,
    user_id INT REFERENCES users(id) ON DELETE CASCADE,
    user_name VARCHAR(100) NOT NULL,
    user_profile_pic_url VARCHAR(500) DEFAULT '',
    is_video BOOLEAN DEFAULT false,
    is_incoming BOOLEAN DEFAULT true,
    is_missed BOOLEAN DEFAULT false,
    timestamp BIGINT NOT NULL,
    duration_seconds INT DEFAULT 0
);

-- Indexing for Chat performance optimization
CREATE INDEX idx_messages_chat_id ON messages(chat_id);
CREATE INDEX idx_messages_timestamp ON messages(timestamp ASC);
CREATE INDEX idx_chats_last_message_time ON chats(last_message_time DESC);
CREATE INDEX idx_status_updates_timestamp ON status_updates(timestamp DESC);

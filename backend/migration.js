const fs = require('fs');
const path = require('path');
const db = require('./db');
require('dotenv').config();

async function runMigration() {
  console.log('[Migration] Starting database migration and seeding...');
  
  try {
    // 1. Read Schema SQL
    const schemaPath = path.join(__dirname, 'schema.sql');
    const schemaSql = fs.readFileSync(schemaPath, 'utf8');

    // 2. Execute SQL commands
    console.log('[Migration] Applying schema.sql to PostgreSQL...');
    await db.query(schemaSql);
    console.log('[Migration] Schema applied successfully!');

    // 3. Seed initial static users
    console.log('[Migration] Seeding initial mock databases users...');
    const seedUsersSql = `
      INSERT INTO users (id, phone_number, name, status_text, profile_pic_url, is_online, last_seen) VALUES
      (1, '+15550199', 'Sarah Jenkins', 'At the gym, text later.', '', true, CURRENT_TIMESTAMP),
      (2, '+15550244', 'John Doe', 'Busy coding 💻🚀', '', false, CURRENT_TIMESTAMP - INTERVAL '5 minutes'),
      (4, '+15550411', 'Alex Rivera', 'Urgent calls only.', '', true, CURRENT_TIMESTAMP),
      (5, '+15550522', 'Elon Mask', 'Occupy Mars!', '', false, CURRENT_TIMESTAMP - INTERVAL '2 hours')
      ON CONFLICT (phone_number) DO NOTHING;
    `;
    await db.query(seedUsersSql);

    // 4. Seed initial chats
    console.log('[Migration] Seeding initial channels...');
    const now = Date.now();
    const seedChatsSql = `
      INSERT INTO chats (id, name, is_group, last_message_text, last_message_time, unread_count, is_muted, group_avatar_seed) VALUES
      (1, 'Sarah Jenkins', false, 'Hey! Are we still meeting up today?', ${now - 120000}, 2, false, 1),
      (2, 'John Doe', false, 'Code is working perfectly! Double check the main.cpp', ${now - 1800000}, 0, false, 2),
      (4, 'Alex Rivera', false, 'ProjectBrief_v2.pdf', ${now - 7200000}, 0, true, 4)
      ON CONFLICT (id) DO NOTHING;
    `;
    await db.query(seedChatsSql);

    // 5. Seed initial messages
    console.log('[Migration] Seeding initial secure chats records...');
    const seedMessagesSql = `
      INSERT INTO messages (chat_id, sender_id, sender_name, text, timestamp, status, type, media_url, file_name, file_size) VALUES
      (1, 1, 'Sarah Jenkins', 'Hey there!', ${now - 900000}, 'READ', 'TEXT', NULL, NULL, NULL),
      (1, 1, 'Sarah Jenkins', 'Hey! Are we still meeting up today?', ${now - 120000}, 'DELIVERED', 'TEXT', NULL, NULL, NULL),
      
      (2, 2, 'John Doe', 'I am pushing the updates now.', ${now - 2400000}, 'READ', 'TEXT', NULL, NULL, NULL),
      (2, 2, 'John Doe', 'Code is working perfectly! Double check the main.cpp', ${now - 1800000}, 'READ', 'TEXT', NULL, NULL, NULL),
      
      (4, 4, 'Alex Rivera', 'Shared the updated brief', ${now - 10800000}, 'READ', 'TEXT', NULL, NULL, NULL),
      (4, 4, 'Alex Rivera', '📄 ProjectBrief_v2.pdf', ${now - 7200000}, 'READ', 'DOCUMENT', 'brief_url', 'ProjectBrief_v2.pdf', '2.4 MB');
    `;
    await db.query(seedMessagesSql);

    // 6. Seed initial statuses
    console.log('[Migration] Seeding initial stories...');
    const seedStatusesSql = `
      INSERT INTO status_updates (user_id, user_name, status_text, timestamp, status_color_hex) VALUES
      (1, 'Sarah Jenkins', 'Beautiful sunrise today! 🌅', ${now - 3600000}, '#2E7D32'),
      (2, 'John Doe', 'Working on the next big feature 🚀☕', ${now - 14400000}, '#1565C0')
    `;
    await db.query(seedStatusesSql);

    console.log('✅ [Migration] Migration completed successfully and database seeded!');
    process.exit(0);
  } catch (error) {
    console.error('❌ [Migration] Database migration failed:', error);
    process.exit(1);
  }
}

// Automatically execute migration if run directly
if (require.main === module) {
  runMigration();
}

module.exports = runMigration;

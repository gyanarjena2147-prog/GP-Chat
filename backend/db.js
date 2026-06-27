const { Pool } = require('pg');
require('dotenv').config();

// Lazily initialize connection pool to prevent early crashes if DB is booting
let poolInstance = null;

function getPool() {
  if (!poolInstance) {
    const connectionString = process.env.DATABASE_URL || 'postgresql://postgres:postgres@localhost:5432/whatsapp_db';
    
    console.log(`[Database] Initializing connection pool with: ${connectionString.split('@')[1] || 'local host'}`);
    
    poolInstance = new Pool({
      connectionString,
      ssl: process.env.DB_SSL === 'true' ? { rejectUnauthorized: false } : false,
      max: 20,
      idleTimeoutMillis: 30000,
      connectionTimeoutMillis: 2000,
    });

    poolInstance.on('error', (err) => {
      console.error('[Database] Unexpected error on idle PostgreSQL client', err);
    });
  }
  return poolInstance;
}

// Helper to query database with automatic log prints
async function query(text, params) {
  const db = getPool();
  const start = Date.now();
  try {
    const res = await db.query(text, params);
    const duration = Date.now() - start;
    console.log(`[Database] Executed Query: ${text.slice(0, 50)}... | Duration: ${duration}ms | Rows: ${res.rowCount}`);
    return res;
  } catch (error) {
    console.error(`[Database] Query Execution Error: ${text}`, error);
    throw error;
  }
}

module.exports = {
  getPool,
  query
};

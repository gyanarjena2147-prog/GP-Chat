const express = require('express');
const http = require('http');
const { Server } = require('socket.io');
const cors = require('cors');
const db = require('./db');
require('dotenv').config();

const app = express();
const server = http.createServer(app);

// Initialize Socket.IO with relaxed CORS for developmental integration
const io = new Server(server, {
  cors: {
    origin: '*',
    methods: ['GET', 'POST']
  }
});

// Middlewares
app.use(cors());
app.use(express.json());

// --- REST API Endpoints ---

// API Index Landing Info
app.get('/', (req, res) => {
  res.json({
    app: "WhatsApp Production Backend Core",
    version: "1.0.0",
    status: "Healthy",
    endpoints: {
      auth: "/api/users/register",
      users: "/api/users",
      chats: "/api/chats",
      messages: "/api/chats/:id/messages",
      status: "/api/status",
      calls: "/api/calls"
    },
    websockets: "Socket.IO listening on HTTP server"
  });
});

// GET: All Users
app.get('/api/users', async (req, res) => {
  try {
    const result = await db.query('SELECT * FROM users ORDER BY name ASC');
    res.json(result.rows);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// POST: Register/Login User
app.post('/api/users/register', async (req, res) => {
  const { phone_number, name, status_text } = req.body;
  if (!phone_number || !name) {
    return res.status(400).json({ error: "Missing phone_number or name" });
  }
  try {
    const result = await db.query(
      `INSERT INTO users (phone_number, name, status_text, is_online, last_seen)
       VALUES ($1, $2, $3, true, CURRENT_TIMESTAMP)
       ON CONFLICT (phone_number) 
       DO UPDATE SET name = $2, status_text = COALESCE($3, users.status_text), is_online = true, last_seen = CURRENT_TIMESTAMP
       RETURNING *`,
      [phone_number, name, status_text || 'Using WhatsApp!']
    );
    res.status(201).json(result.rows[0]);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// GET: All Chats
app.get('/api/chats', async (req, res) => {
  try {
    const result = await db.query('SELECT * FROM chats ORDER BY last_message_time DESC');
    res.json(result.rows);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// GET: Messages for a Chat
app.get('/api/chats/:id/messages', async (req, res) => {
  const { id } = req.params;
  try {
    const result = await db.query(
      'SELECT * FROM messages WHERE chat_id = $1 ORDER BY timestamp ASC',
      [id]
    );
    res.json(result.rows);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// POST: Add Message
app.post('/api/messages', async (req, res) => {
  const { chat_id, sender_id, sender_name, text, type, media_url, file_name, file_size } = req.body;
  if (!chat_id || !sender_id || !text) {
    return res.status(400).json({ error: "Missing chat_id, sender_id or text" });
  }
  const timestamp = Date.now();
  try {
    // 1. Insert message
    const msgResult = await db.query(
      `INSERT INTO messages (chat_id, sender_id, sender_name, text, timestamp, status, type, media_url, file_name, file_size)
       VALUES ($1, $2, $3, $4, $5, 'SENT', $6, $7, $8, $9)
       RETURNING *`,
      [chat_id, sender_id, sender_name, text, timestamp, type || 'TEXT', media_url, file_name, file_size]
    );
    
    // 2. Update Chat preview
    const chatText = type === 'IMAGE' ? '📷 Photo' : type === 'DOCUMENT' ? `📄 ${text}` : text;
    await db.query(
      `UPDATE chats SET last_message_text = $1, last_message_time = $2 WHERE id = $3`,
      [chatText, timestamp, chat_id]
    );

    res.status(201).json(msgResult.rows[0]);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// GET: Status updates
app.get('/api/status', async (req, res) => {
  try {
    const result = await db.query('SELECT * FROM status_updates ORDER BY timestamp DESC');
    res.json(result.rows);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// POST: Add status story
app.post('/api/status', async (req, res) => {
  const { user_id, user_name, status_text, status_media_url, status_color_hex } = req.body;
  if (!user_id || !user_name || (!status_text && !status_media_url)) {
    return res.status(400).json({ error: "Missing user details or story content" });
  }
  const timestamp = Date.now();
  try {
    const result = await db.query(
      `INSERT INTO status_updates (user_id, user_name, status_text, status_media_url, timestamp, status_color_hex)
       VALUES ($1, $2, $3, $4, $5, $6)
       RETURNING *`,
      [user_id, user_name, status_text, status_media_url, timestamp, status_color_hex || '#075E54']
    );
    res.status(201).json(result.rows[0]);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// GET: Call history log
app.get('/api/calls', async (req, res) => {
  try {
    const result = await db.query('SELECT * FROM call_logs ORDER BY timestamp DESC');
    res.json(result.rows);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// POST: Log Call
app.post('/api/calls', async (req, res) => {
  const { user_id, user_name, is_video, is_incoming, is_missed, duration_seconds } = req.body;
  if (!user_id || !user_name) {
    return res.status(400).json({ error: "Missing target caller parameters" });
  }
  try {
    const result = await db.query(
      `INSERT INTO call_logs (user_id, user_name, is_video, is_incoming, is_missed, timestamp, duration_seconds)
       VALUES ($1, $2, $3, $4, $5, $6, $7)
       RETURNING *`,
      [user_id, user_name, is_video || false, is_incoming || false, is_missed || false, Date.now(), duration_seconds || 0]
    );
    res.status(201).json(result.rows[0]);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// --- Socket.IO Real-time Channels & WebRTC Signalling Coordinator ---
io.on('connection', (socket) => {
  console.log(`[Socket] Client connected. Socket ID: ${socket.id}`);

  // User joins a personal or chat room for communication isolation
  socket.on('join:room', (roomId) => {
    socket.join(roomId);
    console.log(`[Socket] Client ${socket.id} joined channel: ${roomId}`);
  });

  // Client broadcasts typing indicators
  socket.on('typing', ({ roomId, userName, isTyping }) => {
    socket.to(roomId).emit('typing', { userName, isTyping });
  });

  // Client transmits a real-time messaging payload
  socket.on('message:send', (messagePayload) => {
    console.log(`[Socket] Message received on server:`, messagePayload);
    // Broadcast back to participants in the room
    io.to(messagePayload.roomId).emit('message:receive', messagePayload);
  });

  // --- WebRTC Peer Signalling Gateway ---
  socket.on('call:dial', ({ targetRoom, callerInfo, isVideo }) => {
    console.log(`[WebRTC] Peer ${socket.id} dialing target room: ${targetRoom}`);
    socket.to(targetRoom).emit('call:incoming', { callerInfo, isVideo, fromSocketId: socket.id });
  });

  socket.on('call:accept', ({ targetSocketId }) => {
    console.log(`[WebRTC] Call accepted by peer. Signaling back to: ${targetSocketId}`);
    io.to(targetSocketId).emit('call:accepted');
  });

  socket.on('call:reject', ({ targetSocketId }) => {
    console.log(`[WebRTC] Call rejected by peer. Signaling back to: ${targetSocketId}`);
    io.to(targetSocketId).emit('call:rejected');
  });

  socket.on('call:end', ({ roomId }) => {
    console.log(`[WebRTC] Call ended in channel: ${roomId}`);
    io.to(roomId).emit('call:ended');
  });

  socket.on('disconnect', () => {
    console.log(`[Socket] Client disconnected: ${socket.id}`);
  });
});

// Start Server on Port 3000
const PORT = process.env.PORT || 3000;
server.listen(PORT, () => {
  console.log(`🚀 [Server] Production Node.js server listening on port ${PORT}`);
});

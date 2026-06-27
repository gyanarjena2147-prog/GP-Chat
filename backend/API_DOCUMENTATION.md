# WhatsApp Backend Core API Documentation

This backend exposes a set of REST endpoints for general storage operations, paired with a Socket.IO WebSocket interface for state propagation (real-time message notifications, typing states) and WebRTC peer signaling.

---

## 1. REST API REFERENCE

All request bodies must be sent with `Content-Type: application/json`.

### Users / Profiles
#### `POST /api/users/register`
Registers a new user, or updates an existing online status if the phone number is already present in the database.
* **Payload**:
```json
{
  "phone_number": "+15550100",
  "name": "Jane Doe",
  "status_text": "Code is poetry. 💻✨"
}
```
* **Success Response (201 Created)**:
```json
{
  "id": 10,
  "phone_number": "+15550100",
  "name": "Jane Doe",
  "status_text": "Code is poetry. 💻✨",
  "is_online": true,
  "last_seen": "2026-06-27T13:00:00.000Z"
}
```

#### `GET /api/users`
Retrieves a list of all active registered users in the PostgreSQL cluster.
* **Response (200 OK)**:
```json
[
  {
    "id": 1,
    "phone_number": "+15550199",
    "name": "Sarah Jenkins",
    "status_text": "At the gym, text later.",
    "is_online": true
  }
]
```

### Chats & Channels
#### `GET /api/chats`
Lists all active direct/group chat metadata sorted by latest message activity times.
* **Response (200 OK)**:
```json
[
  {
    "id": 1,
    "name": "Sarah Jenkins",
    "is_group": false,
    "last_message_text": "Hey! Are we still meeting up today?",
    "last_message_time": 1782565620000,
    "unread_count": 2
  }
]
```

#### `GET /api/chats/:id/messages`
Returns chronological secure message histories for a specific chat.
* **Response (200 OK)**:
```json
[
  {
    "id": 24,
    "chat_id": 1,
    "sender_id": 1,
    "sender_name": "Sarah Jenkins",
    "text": "Hey! Are we still meeting up today?",
    "timestamp": 1782565620000,
    "status": "DELIVERED",
    "type": "TEXT"
  }
]
```

#### `POST /api/messages`
Submits a message and propagates preview status to the chat thread.
* **Payload**:
```json
{
  "chat_id": 1,
  "sender_id": 0,
  "sender_name": "Me",
  "text": "Yes, I am heading over now!",
  "type": "TEXT"
}
```

---

## 2. SOCKET.IO EVENTS & CHANNEL SCHEMAS

The WebSocket server coordinates instantaneous updates across connected nodes.

### Room Subscriptions
#### `join:room`
Clients must emit `join:room` upon clicking or focusing on a chat thread to isolate channels.
* **Parameter**: `roomId` (e.g. `1` or `"room_group_chats"`)

### Messaging
#### `message:send`
Propagates a real-time chat payload to all users in the specific room.
* **Input Payload**:
```json
{
  "roomId": "1",
  "senderId": 0,
  "senderName": "Me",
  "text": "Hello, Sarah!",
  "type": "TEXT"
}
```
* **Broadcasts on**: `message:receive`

#### `typing`
Alerts active participants when an interlocutor is inputting values.
* **Input Payload**:
```json
{
  "roomId": "1",
  "userName": "Me",
  "isTyping": true
}
```

---

## 3. WEBRTC SIGNALING SPECIFICATION

The signaling server coordinates direct peer-to-peer visual communication setups without parsing media data directly.

```
Caller (Socket A)              Server (Express/SIO)            Callee (Socket B)
       |                                |                             |
       | ------ call:dial --------->   |                             |
       |                                | ------ call:incoming ---->  |
       |                                |                             |
       |                                | <----- call:accept -------- |
       | <----- call:accepted --------  |                             |
       |                                |                             |
       | ====== (Direct P2P WebRTC Audio/Video Connection established) =======
       |                                |                             |
       | ------ call:end ----------->   |                             |
       |                                | ------ call:ended --------> |
```

#### `call:dial`
Fires an incoming ringing overlay to a callee node.
* **Payload**:
```json
{
  "targetRoom": "sarah_room",
  "callerInfo": { "id": 0, "name": "Me" },
  "isVideo": true
}
```

#### `call:accept` / `call:reject`
Notifies calling peers whether their dials were approved.
* **Payload**:
```json
{
  "targetSocketId": "caller_socket_session_id"
}
```

#### `call:end`
Terminates signaling frameworks instantly.
* **Payload**:
```json
{
  "roomId": "active_call_room_id"
}
```

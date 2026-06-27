# WhatsApp Complete Production Setup Guide

Welcome to the production setup guide for the complete WhatsApp-like messaging platform. This stack features:
- **Client (Android Native)**: Native Kotlin with Jetpack Compose, Room (Local cache database), Retrofit, dynamic call visualizer, and simulated background activity.
- **Backend Server (Node.js/Express)**: Complete REST API routing, Socket.IO WebSockets engines, and WebRTC peer negotiation gateways.
- **Database Layer (PostgreSQL)**: Durable table structures, Cascaded Foreign Keys, Indexes, and automated database seed managers.
- **Docker Stack**: Production-ready multi-container orchestrations.

---

## 🚀 QUICK START: DOCKER COMPOSE (RECOMMENDED)

The fastest and most stable way to spin up the backend is using Docker Compose. It launches both the **PostgreSQL 15** cluster and the **Node.js Express** WebSocket server, setting up networks and healthchecks automatically.

### Prerequisites
- Docker installed on your host machine
- Docker Compose v2.0+ installed

### Steps
1. **Navigate to the backend directory**:
   ```bash
   cd backend
   ```

2. **Boot up the Docker containers**:
   ```bash
   docker-compose up -d --build
   ```

3. **Verify the services are healthy**:
   ```bash
   docker ps
   ```
   You should see two containers running:
   - `whatsapp_postgres_db` (Healthy, listening on port `5432`)
   - `whatsapp_express_backend` (Healthy, listening on port `3000`)

4. **Verify API status**:
   Open a browser or run:
   ```bash
   curl http://localhost:3000/
   ```
   You should see a JSON health payload returning `status: "Healthy"`.

---

## 🛠️ MANUAL SETUP: BACKEND & DATABASE

If you prefer to run the Node.js server and database natively on your host machine:

### 1. Configure PostgreSQL
1. Ensure PostgreSQL is installed and running on your host machine.
2. Log into PostgreSQL shell:
   ```bash
   psql -U postgres
   ```
3. Create the database:
   ```sql
   CREATE DATABASE whatsapp_db;
   ```

### 2. Configure Environment Variables
Create a file named `.env` inside `/backend/` directory:
```properties
PORT=3000
DATABASE_URL=postgresql://postgres:YOUR_PG_PASSWORD@localhost:5432/whatsapp_db
DB_SSL=false
```

### 3. Install Dependencies
```bash
cd backend
npm install
```

### 4. Run Automatic Database Migrations & Seeds
Execute the automated seeder script to establish tables, cascades, and initial mock channels:
```bash
npm run migrate
```
You should see:
`✅ [Migration] Migration completed successfully and database seeded!`

### 5. Start the Server
- **Production Mode**:
  ```bash
  npm start
  ```
- **Development Mode** (with automatic reload on file changes):
  ```bash
  npm run dev
  ```

---

## 📱 CLIENT INTEGRATION (ANDROID / FLUTTER)

The client codebase is contained in the `/app` and `/src` directories.

### Native Android Compilation
The Android app is fully functional and uses a local **SQLite Room Database** as an offline-first storage solution, meaning it runs instantly without external database requirements.

1. **Gradle Build Check**:
   To assemble a debug APK, run:
   ```bash
   ./gradlew assembleDebug
   ```
2. **Installation**:
   Install the generated APK (`app/build/outputs/apk/debug/app-debug.apk`) directly on your physical test phone or Android emulator.
3. **Connecting Client to Your Backend Node**:
   To wire up your live Android build to the backend Node server, update your networking base URL (in Retrofit client / WebSocket manager files) to point to your hosted Express instance:
   - For **Local Emulator**: `http://10.0.2.2:3000`
   - For **Physical Device**: Connect phone and computer to the same Wi-Fi, then use your computer's local IP (e.g., `http://192.168.1.15:3000`).

---

## 🧪 TESTING WORKFLOW & FEATURES WALKTHROUGH

### 1. Multi-User Messaging & Live Chats
Open the client on your Android emulator. Select a chat (e.g., **Sarah Jenkins**). Send any text message. The local database will immediately record the message, mark it as `SENT`, and transition it to `DELIVERED` and `READ`.
- **Interactive Simulation**: Sarah Jenkins will automatically "type back" and send custom responsive context-aware replies!

### 2. Attachment Deliveries (Photos & PDFs)
Tapping the **Clip Attachment icon** at the bottom reveals options:
- Tap **Gallery** to send a simulated vacation image.
- Tap **Document** to send a PDF document card with custom byte-sizes.
- Tap **Mic** on the input bar to send a simulated voice message with waveform lines.

### 3. Simulating Peer Calling (Voice & Video)
To test WebRTC voice or video ringing states:
1. Tap the **Admin Panel** build-icon in the top header.
2. Select **Incoming Voice Call** or **Incoming Video Call**.
3. Go back; a fullscreen WhatsApp ringing screen overlays your view.
4. Tap **Accept** to engage. You will see a dynamic timer and simulated camera feeds!

### 4. Admin Seeding Control
Use the **Admin Dashboard** to inject customized mock text, image attachments, or document files to any specific user feed to verify layout updates instantly.

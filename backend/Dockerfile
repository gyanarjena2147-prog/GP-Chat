# Use standard lightweight LTS Node.js image
FROM node:20-alpine

# Set working directory inside container
WORKDIR /usr/src/app

# Copy dependency configs
COPY package*.json ./

# Install production-only dependencies
RUN npm ci --only=production

# Copy application source files
COPY . .

# Expose server port (3000 is the WhatsApp backend default)
EXPOSE 3000

# Environment variables defaults
ENV NODE_ENV=production
ENV PORT=3000
ENV DATABASE_URL=postgresql://postgres:postgres@db:5432/whatsapp_db

# Run startup script
CMD ["node", "server.js"]

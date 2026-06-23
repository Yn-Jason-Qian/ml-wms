# ── WMS Web Frontend Dockerfile ──
# Build Vue3 app → Serve via Nginx
FROM node:20-alpine AS builder
WORKDIR /build
COPY package*.json ./
RUN npm ci --legacy-peer-deps
COPY . .
RUN npm run build

FROM nginx:1.27-alpine
COPY --from=builder /build/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80

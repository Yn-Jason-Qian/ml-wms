# ── WMS 前端（Nginx 静态站点 + API/WebSocket 反向代理）──
FROM nginx:1.27-alpine

COPY dist/ /usr/share/nginx/html/
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

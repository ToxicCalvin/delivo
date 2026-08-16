# Multi-stage Dockerfile for building and running a Vue.js application with Nginx
# Stage 1: Node environment build
FROM node:20-alpine AS builder

WORKDIR /app
COPY package*.json ./
RUN npm install

COPY . .
RUN npm run build

# Stage 2: Nginx environment run
FROM nginx:alpine

COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]

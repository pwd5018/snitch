FROM node:22-alpine AS frontend-build
WORKDIR /app
COPY frontend/package.json ./
RUN npm install
COPY frontend/ ./
RUN npm run build

FROM node:22-alpine
WORKDIR /app
COPY backend/package.json ./
RUN npm install --omit=dev
COPY backend/ ./
COPY --from=frontend-build /app/dist ./public

ENV NODE_ENV=production
EXPOSE 8788
EXPOSE 53/udp

CMD ["node", "src/server.js"]

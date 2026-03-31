FROM docker.io/node:22-alpine AS build
WORKDIR /app
COPY package.json ./
RUN npm install
COPY . .
ARG VITE_API_AUTH_TOKEN
ENV VITE_API_AUTH_TOKEN=$VITE_API_AUTH_TOKEN
RUN npm run build

FROM docker.io/nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
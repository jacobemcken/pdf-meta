FROM node:24.18-trixie-slim AS build

# Shadow-cljs requires Java 21+
RUN apt-get update && apt-get -q -y install \
    openjdk-25-jre-headless \ 
    curl

RUN curl -s https://download.clojure.org/install/linux-install-1.11.1.1165.sh | bash \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY . .

# Install dependencies
RUN npm ci

# Build
RUN npm run release

FROM nginx:1-alpine

COPY --from=build /app/public/ /usr/share/nginx/html/

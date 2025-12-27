FROM node:22-alpine

WORKDIR /app

# curl 설치 (health check용)
RUN apk add --no-cache curl

# package.json 복사 및 의존성 설치
COPY package*.json ./
RUN npm install --legacy-peer-deps

# 소스 코드 복사
COPY . .

# 컨트랙트 컴파일
RUN npm run compile

# entrypoint 실행 권한
RUN chmod +x docker-entrypoint.sh

EXPOSE 8545

ENTRYPOINT ["./docker-entrypoint.sh"]

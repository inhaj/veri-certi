#!/bin/sh
set -e

echo "=== Hardhat Node Starting ==="

# 의존성 설치 (처음 실행 시)
if [ ! -d "node_modules" ]; then
    echo "Installing dependencies..."
    npm install --legacy-peer-deps
fi

# Hardhat 노드를 백그라운드에서 시작
echo "Starting Hardhat node..."
npx hardhat node --hostname 0.0.0.0 &
NODE_PID=$!

# 노드가 준비될 때까지 대기
echo "Waiting for Hardhat node to be ready..."
sleep 5

# 노드 상태 확인
for i in 1 2 3 4 5; do
    if curl -s -X POST -H "Content-Type: application/json" \
        --data '{"jsonrpc":"2.0","method":"eth_blockNumber","params":[],"id":1}' \
        http://localhost:8545 > /dev/null 2>&1; then
        echo "Hardhat node is ready!"
        break
    fi
    echo "Waiting for node... ($i/5)"
    sleep 2
done

# 컨트랙트 배포
echo "Deploying contracts..."
npx hardhat run scripts/deploy.js --network localhost

echo "=== Hardhat Node Ready ==="
echo "Contract deployment complete. Node running on port 8545"

# 노드 프로세스 유지
wait $NODE_PID

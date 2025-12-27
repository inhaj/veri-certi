const { ethers } = require("hardhat");
const Redis = require("ioredis");

async function main() {
  console.log("Deploying LedgerRegistry...");

  const [deployer] = await ethers.getSigners();
  console.log("Deploying with account:", deployer.address);

  const balance = await ethers.provider.getBalance(deployer.address);
  console.log("Account balance:", ethers.formatEther(balance), "ETH");

  const LedgerRegistry = await ethers.getContractFactory("LedgerRegistry");
  const ledgerRegistry = await LedgerRegistry.deploy();
  await ledgerRegistry.waitForDeployment();

  const contractAddress = await ledgerRegistry.getAddress();
  console.log("LedgerRegistry deployed to:", contractAddress);

  // Redis에 컨트랙트 주소 저장
  const redisHost = process.env.REDIS_HOST || "redis";
  const redisPort = process.env.REDIS_PORT || 6379;
  
  console.log(`Connecting to Redis at ${redisHost}:${redisPort}...`);
  
  const redis = new Redis({
    host: redisHost,
    port: redisPort,
    retryDelayOnFailover: 100,
    maxRetriesPerRequest: 3
  });

  try {
    const deploymentInfo = {
      contractAddress: contractAddress,
      deployedAt: new Date().toISOString(),
      deployer: deployer.address,
      network: "localhost"
    };

    await redis.set("blockchain:contract:address", contractAddress);
    await redis.set("blockchain:contract:info", JSON.stringify(deploymentInfo));
    
    console.log("Contract address saved to Redis:");
    console.log("  Key: blockchain:contract:address");
    console.log("  Value:", contractAddress);
    
    await redis.quit();
  } catch (error) {
    console.error("Failed to save to Redis:", error.message);
    console.log("Contract deployed but Redis save failed. Manual config needed.");
    console.log("BLOCKCHAIN_CONTRACT_ADDRESS=" + contractAddress);
  }

  console.log("\n=== Deployment Complete ===");
  console.log("Contract Address:", contractAddress);
}

main()
  .then(() => process.exit(0))
  .catch((error) => {
    console.error(error);
    process.exit(1);
  });

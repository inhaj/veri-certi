const { expect } = require("chai");
const { ethers } = require("hardhat");

describe("LedgerRegistry", function () {
  let ledgerRegistry;
  let owner;
  let authorizedUser;
  let unauthorizedUser;

  const organizationId = 1;

  beforeEach(async function () {
    [owner, authorizedUser, unauthorizedUser] = await ethers.getSigners();

    const LedgerRegistry = await ethers.getContractFactory("LedgerRegistry");
    ledgerRegistry = await LedgerRegistry.deploy();
    await ledgerRegistry.waitForDeployment();
  });

  describe("Deployment", function () {
    it("Should set the deployer as owner", async function () {
      expect(await ledgerRegistry.owner()).to.equal(owner.address);
    });

    it("Should authorize the owner as registrant", async function () {
      expect(await ledgerRegistry.authorizedRegistrants(owner.address)).to.be.true;
    });
  });

  describe("Hash Registration", function () {
    it("Should register hash successfully", async function () {
      const sampleHash = ethers.keccak256(ethers.toUtf8Bytes("sample-data-hash"));
      const tx = await ledgerRegistry.registerHash(sampleHash, organizationId);
      const receipt = await tx.wait();
      
      expect(receipt.status).to.equal(1);
    });

    it("Should prevent duplicate hash registration", async function () {
      const sampleHash = ethers.keccak256(ethers.toUtf8Bytes("sample-data-hash"));
      await ledgerRegistry.registerHash(sampleHash, organizationId);
      
      await expect(ledgerRegistry.registerHash(sampleHash, organizationId))
        .to.be.revertedWith("LedgerRegistry: hash already registered");
    });

    it("Should reject unauthorized registrant", async function () {
      const sampleHash = ethers.keccak256(ethers.toUtf8Bytes("sample-data-hash"));
      await expect(
        ledgerRegistry.connect(unauthorizedUser).registerHash(sampleHash, organizationId)
      ).to.be.revertedWith("LedgerRegistry: caller is not authorized");
    });
  });

  describe("Hash Verification", function () {
    it("Should verify registered hash", async function () {
      const sampleHash = ethers.keccak256(ethers.toUtf8Bytes("sample-data-hash"));
      await ledgerRegistry.registerHash(sampleHash, organizationId);
      
      const [exists, timestamp] = await ledgerRegistry.verifyHash(sampleHash);
      
      expect(exists).to.be.true;
      expect(timestamp).to.be.gt(0);
    });

    it("Should return false for unregistered hash", async function () {
      const sampleHash = ethers.keccak256(ethers.toUtf8Bytes("sample-data-hash"));
      const [exists, timestamp] = await ledgerRegistry.verifyHash(sampleHash);
      
      expect(exists).to.be.false;
      expect(timestamp).to.equal(0);
    });
  });

  describe("Authorization", function () {
    it("Should authorize new registrant", async function () {
      await ledgerRegistry.setAuthorizedRegistrant(authorizedUser.address, true);
      expect(await ledgerRegistry.authorizedRegistrants(authorizedUser.address)).to.be.true;
    });

    it("Should prevent non-owner from authorizing", async function () {
      await expect(
        ledgerRegistry.connect(unauthorizedUser).setAuthorizedRegistrant(authorizedUser.address, true)
      ).to.be.revertedWith("LedgerRegistry: caller is not the owner");
    });
  });
});

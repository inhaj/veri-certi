// SPDX-License-Identifier: MIT
pragma solidity ^0.8.24;

/**
 * @title LedgerRegistry
 * @dev 기부금/영수증 데이터의 해시를 블록체인에 기록하여 무결성을 검증하는 컨트랙트
 * 
 * 주요 기능:
 * - 데이터 해시 등록 (registerHash)
 * - 해시 존재 여부 검증 (verifyHash)
 * - 등록 이벤트 발생 (HashRegistered)
 */
contract LedgerRegistry {
    
    // 등록된 해시 저장소: hash => timestamp
    mapping(bytes32 => uint256) private registeredHashes;
    
    // 해시별 등록자 주소
    mapping(bytes32 => address) private hashRegistrants;
    
    // 조직별 해시 목록 관리
    mapping(uint256 => bytes32[]) private organizationHashes;
    
    // 컨트랙트 소유자
    address public owner;
    
    // 허가된 등록자 목록
    mapping(address => bool) public authorizedRegistrants;
    
    // 이벤트
    event HashRegistered(
        bytes32 indexed dataHash,
        uint256 indexed organizationId,
        uint256 timestamp,
        address registrant
    );
    
    event RegistrantAuthorized(address indexed registrant, bool authorized);
    
    // 수정자
    modifier onlyOwner() {
        require(msg.sender == owner, "LedgerRegistry: caller is not the owner");
        _;
    }
    
    modifier onlyAuthorized() {
        require(
            authorizedRegistrants[msg.sender] || msg.sender == owner,
            "LedgerRegistry: caller is not authorized"
        );
        _;
    }
    
    constructor() {
        owner = msg.sender;
        authorizedRegistrants[msg.sender] = true;
    }
    
    /**
     * @dev 데이터 해시를 블록체인에 등록
     * @param dataHash 등록할 데이터 해시 (SHA-256)
     * @param organizationId 조직 ID
     */
    function registerHash(bytes32 dataHash, uint256 organizationId) 
        external 
        onlyAuthorized 
    {
        require(dataHash != bytes32(0), "LedgerRegistry: invalid hash");
        require(registeredHashes[dataHash] == 0, "LedgerRegistry: hash already registered");
        
        uint256 timestamp = block.timestamp;
        
        registeredHashes[dataHash] = timestamp;
        hashRegistrants[dataHash] = msg.sender;
        organizationHashes[organizationId].push(dataHash);
        
        emit HashRegistered(dataHash, organizationId, timestamp, msg.sender);
    }
    
    /**
     * @dev 해시가 등록되어 있는지 검증
     * @param dataHash 검증할 해시
     * @return exists 등록 여부
     * @return timestamp 등록 시간 (없으면 0)
     */
    function verifyHash(bytes32 dataHash) 
        external 
        view 
        returns (bool exists, uint256 timestamp) 
    {
        timestamp = registeredHashes[dataHash];
        exists = timestamp != 0;
    }
    
    /**
     * @dev 해시의 등록자 주소 조회
     * @param dataHash 조회할 해시
     * @return registrant 등록자 주소
     */
    function getRegistrant(bytes32 dataHash) 
        external 
        view 
        returns (address registrant) 
    {
        return hashRegistrants[dataHash];
    }
    
    /**
     * @dev 조직의 등록된 해시 개수 조회
     * @param organizationId 조직 ID
     * @return count 해시 개수
     */
    function getOrganizationHashCount(uint256 organizationId) 
        external 
        view 
        returns (uint256 count) 
    {
        return organizationHashes[organizationId].length;
    }
    
    /**
     * @dev 등록자 권한 설정 (소유자만 가능)
     * @param registrant 등록자 주소
     * @param authorized 권한 부여 여부
     */
    function setAuthorizedRegistrant(address registrant, bool authorized) 
        external 
        onlyOwner 
    {
        require(registrant != address(0), "LedgerRegistry: invalid address");
        authorizedRegistrants[registrant] = authorized;
        emit RegistrantAuthorized(registrant, authorized);
    }
    
    /**
     * @dev 소유권 이전
     * @param newOwner 새 소유자 주소
     */
    function transferOwnership(address newOwner) external onlyOwner {
        require(newOwner != address(0), "LedgerRegistry: invalid new owner");
        owner = newOwner;
    }
}

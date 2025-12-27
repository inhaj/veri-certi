package com.vericerti.infrastructure.blockchain;

import com.vericerti.infrastructure.exception.BlockchainException;
import com.vericerti.infrastructure.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.*;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LedgerRegistryService {

    private final Web3jService web3jService;

    public String registerHash(String dataHash, Long organizationId) {
        if (!web3jService.isInitialized()) {
            throw new BlockchainException(ErrorCode.BLOCKCHAIN_NOT_INITIALIZED);
        }

        String contractAddress = web3jService.getContractAddress();
        if (contractAddress == null || contractAddress.isBlank()) {
            throw new BlockchainException(ErrorCode.BLOCKCHAIN_CONTRACT_NOT_CONFIGURED);
        }

        try {
            byte[] hashBytes = Numeric.hexStringToByteArray(dataHash);
            if (hashBytes.length != 32) {
                throw new BlockchainException(ErrorCode.BLOCKCHAIN_INVALID_HASH, 
                    "Hash must be 32 bytes (64 hex chars)");
            }

            Function function = new Function(
                    "registerHash",
                    Arrays.asList(
                            new Bytes32(hashBytes),
                            new Uint256(BigInteger.valueOf(organizationId))
                    ),
                    Collections.emptyList()
            );

            String encodedFunction = FunctionEncoder.encode(function);

            BigInteger nonce = web3jService.getWeb3j()
                    .ethGetTransactionCount(
                            web3jService.getCredentials().getAddress(),
                            DefaultBlockParameterName.LATEST
                    )
                    .send()
                    .getTransactionCount();

            BigInteger gasPrice = web3jService.getGasProvider().getGasPrice();
            BigInteger gasLimit = web3jService.getGasProvider().getGasLimit();

            RawTransaction rawTransaction = RawTransaction.createTransaction(
                    nonce,
                    gasPrice,
                    gasLimit,
                    contractAddress,
                    encodedFunction
            );

            byte[] signedMessage = TransactionEncoder.signMessage(
                    rawTransaction,
                    web3jService.getCredentials()
            );

            String hexValue = Numeric.toHexString(signedMessage);

            EthSendTransaction response = web3jService.getWeb3j()
                    .ethSendRawTransaction(hexValue)
                    .send();

            if (response.hasError()) {
                throw new BlockchainException(ErrorCode.BLOCKCHAIN_TRANSACTION_FAILED, 
                    "Transaction error: " + response.getError().getMessage());
            }

            String txHash = response.getTransactionHash();
            log.info("Hash registered to blockchain. TxHash: {}", txHash);
            return txHash;

        } catch (BlockchainException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to register hash: {}", e.getMessage());
            throw new BlockchainException(ErrorCode.BLOCKCHAIN_TRANSACTION_FAILED, 
                "Failed to register hash to blockchain", e);
        }
    }

    public VerificationResult verifyHash(String dataHash) {
        if (!web3jService.isInitialized()) {
            throw new BlockchainException(ErrorCode.BLOCKCHAIN_NOT_INITIALIZED);
        }

        String contractAddress = web3jService.getContractAddress();
        if (contractAddress == null || contractAddress.isBlank()) {
            throw new BlockchainException(ErrorCode.BLOCKCHAIN_CONTRACT_NOT_CONFIGURED);
        }

        try {
            byte[] hashBytes = Numeric.hexStringToByteArray(dataHash);
            if (hashBytes.length != 32) {
                throw new BlockchainException(ErrorCode.BLOCKCHAIN_INVALID_HASH, 
                    "Hash must be 32 bytes");
            }

            Function function = new Function(
                    "verifyHash",
                    Collections.singletonList(new Bytes32(hashBytes)),
                    Arrays.asList(
                            new TypeReference<Bool>() {},
                            new TypeReference<Uint256>() {}
                    )
            );

            String encodedFunction = FunctionEncoder.encode(function);

            EthCall response = web3jService.getWeb3j()
                    .ethCall(
                            Transaction.createEthCallTransaction(
                                    web3jService.getCredentials().getAddress(),
                                    contractAddress,
                                    encodedFunction
                            ),
                            DefaultBlockParameterName.LATEST
                    )
                    .send();

            if (response.hasError()) {
                throw new BlockchainException(ErrorCode.BLOCKCHAIN_VERIFICATION_FAILED, 
                    "Call failed: " + response.getError().getMessage());
            }

            List<Type> results = FunctionReturnDecoder.decode(
                    response.getValue(),
                    function.getOutputParameters()
            );

            boolean exists = (Boolean) results.get(0).getValue();
            BigInteger timestamp = (BigInteger) results.get(1).getValue();

            log.debug("Hash verification: exists={}, timestamp={}", exists, timestamp);
            return new VerificationResult(exists, timestamp.longValue());

        } catch (BlockchainException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to verify hash: {}", e.getMessage());
            throw new BlockchainException(ErrorCode.BLOCKCHAIN_VERIFICATION_FAILED, 
                "Failed to verify hash on blockchain", e);
        }
    }

    public record VerificationResult(boolean exists, long timestamp) {}
}

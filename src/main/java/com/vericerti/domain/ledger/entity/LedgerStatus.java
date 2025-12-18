package com.vericerti.domain.ledger.entity;

public enum LedgerStatus {
    PENDING,    // 블록체인 기록 대기
    RECORDED,   // 블록체인 기록 완료
    FAILED      // 기록 실패
}

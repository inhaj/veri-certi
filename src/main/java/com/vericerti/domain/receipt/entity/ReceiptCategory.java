package com.vericerti.domain.receipt.entity;

/**
 * 영수증 지출 분류
 */
public enum ReceiptCategory {
    SALARY,         // 인건비
    OFFICE,         // 사무비
    UTILITIES,      // 공과금
    EQUIPMENT,      // 장비/비품
    SUPPLIES,       // 소모품
    TRAVEL,         // 출장비
    MARKETING,      // 홍보/마케팅
    MAINTENANCE,    // 유지보수
    PROFESSIONAL,   // 전문서비스 (법률/회계)
    OTHER           // 기타
}

package com.vericerti.controller;

import com.vericerti.application.command.CreateAccountCommand;
import com.vericerti.controller.account.request.AccountCreateRequest;
import com.vericerti.controller.account.response.AccountResponse;
import com.vericerti.domain.account.entity.Account;
import com.vericerti.domain.account.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/organizations/{orgId}/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    public ResponseEntity<AccountResponse> create(
            @PathVariable Long orgId,
            @Valid @RequestBody AccountCreateRequest request) {
        Account account = accountService.createAccount(
                new CreateAccountCommand(
                        orgId,
                        request.accountNumber(),
                        request.bankName(),
                        request.accountType(),
                        request.accountHolder(),
                        request.balance(),
                        request.description()
                )
        );
        return ResponseEntity
                .created(URI.create("/api/organizations/" + orgId + "/accounts/" + account.getId()))
                .body(toResponse(account));
    }

    @GetMapping
    public ResponseEntity<List<AccountResponse>> list(@PathVariable Long orgId) {
        List<AccountResponse> accounts = accountService.findByOrganizationId(orgId)
                .stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountResponse> get(@PathVariable Long orgId, @PathVariable Long id) {
        Account account = accountService.findById(id);
        return ResponseEntity.ok(toResponse(account));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long orgId, @PathVariable Long id) {
        accountService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private AccountResponse toResponse(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getOrganizationId(),
                account.getAccountNumber(),
                account.getBankName(),
                account.getAccountType(),
                account.getAccountHolder(),
                account.getBalance(),
                account.getDescription(),
                account.getCreatedAt()
        );
    }
}

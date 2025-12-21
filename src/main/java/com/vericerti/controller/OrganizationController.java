package com.vericerti.controller;

import com.vericerti.application.command.CreateOrganizationCommand;
import com.vericerti.controller.organization.request.OrganizationCreateRequest;
import com.vericerti.controller.organization.response.OrganizationResponse;
import com.vericerti.domain.organization.entity.Organization;
import com.vericerti.domain.organization.service.OrganizationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/organizations")
@RequiredArgsConstructor
public class OrganizationController {

    private final OrganizationService organizationService;

    @PostMapping
    public ResponseEntity<OrganizationResponse> create(@Valid @RequestBody OrganizationCreateRequest request) {
        Organization organization = organizationService.create(
                new CreateOrganizationCommand(
                        request.name(),
                        request.businessNumber(),
                        request.description()
                )
        );
        return ResponseEntity.ok(toResponse(organization));
    }

    @GetMapping
    public ResponseEntity<List<OrganizationResponse>> getAll() {
        List<OrganizationResponse> responses = organizationService.findAll().stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrganizationResponse> getById(@PathVariable Long id) {
        Organization organization = organizationService.findById(id);
        return ResponseEntity.ok(toResponse(organization));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        organizationService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private OrganizationResponse toResponse(Organization org) {
        return new OrganizationResponse(
                org.getId(),
                org.getName(),
                org.getBusinessNumberValue(),
                org.getDescription(),
                org.getCreatedAt()
        );
    }
}


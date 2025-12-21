package com.vericerti.application.command;

public record LoginCommand(
        String email,
        String password
) {}

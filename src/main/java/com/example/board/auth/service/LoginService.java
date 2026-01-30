package com.example.board.auth.service;

import com.example.board.auth.dto.command.LoginCommand;
import com.example.board.auth.dto.response.LoginResult;

public interface LoginService {
    LoginResult login(LoginCommand command);
}

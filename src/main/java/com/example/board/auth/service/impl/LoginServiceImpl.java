package com.example.board.auth.service.impl;

import com.example.board.auth.dto.command.LoginCommand;
import com.example.board.auth.dto.response.LoginResult;
import com.example.board.auth.dto.userdetails.MemberDetails;
import com.example.board.auth.service.AuthSessionService;
import com.example.board.auth.service.LoginService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginServiceImpl implements LoginService {
    private final AuthenticationManager authenticationManager;
    private final AuthSessionService authSessionService;

    @Override
    public LoginResult login(LoginCommand command) {
        try {
            var unauthenticated = UsernamePasswordAuthenticationToken.unauthenticated(command.username(), command.password());
            var authenticate = authenticationManager.authenticate(unauthenticated);
            var principal = (MemberDetails) authenticate.getPrincipal();
            return new LoginResult.Success(authSessionService.createLoginSession(principal.getId(), command.type(), principal.getRole().name()));
        } catch (BadCredentialsException _) {
            return new LoginResult.BadCredentials();
        } catch (DisabledException _) {
            return new LoginResult.AccountPending();
        } catch (LockedException _) {
            return new LoginResult.AccountDormant();
        } catch (AccountExpiredException _) {
            return new LoginResult.AccountWithdrawn();
        }
    }
}

package com.example.board.auth.service.impl;

import com.example.board.auth.dto.userdetails.MemberDetails;
import com.example.board.auth.repository.MemberCredentialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberDetailsService implements UserDetailsService {
    private final MemberCredentialRepository memberCredentialRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var memberCredential = memberCredentialRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 계정입니다."));
        return new MemberDetails(memberCredential);
    }
}

package com.example.board.auth.dto.userdetails;

import com.example.board.auth.entity.MemberCredential;
import com.example.board.auth.entity.MemberRole;
import com.example.board.auth.entity.MemberStatus;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class MemberDetails implements UserDetails {
    private final MemberCredential memberCredential;

    public MemberDetails(MemberCredential memberCredential) {
        this.memberCredential = memberCredential;
    }

    public Long getId() {
        return memberCredential.getId();
    }

    public MemberRole getRole() {
        return memberCredential.getRole();
    }

    @Override
    public @Nullable String getPassword() {
        return memberCredential.getPassword();
    }

    @Override
    public String getUsername() {
        return memberCredential.getUsername();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + memberCredential.getRole().name()));
    }

    @Override
    public boolean isEnabled() {
        return memberCredential.getStatus() != MemberStatus.PENDING;
    }

    @Override
    public boolean isAccountNonLocked() {
        return memberCredential.getStatus() != MemberStatus.DORMANT;
    }

    @Override
    public boolean isAccountNonExpired() {
        return memberCredential.getStatus() != MemberStatus.WITHDRAWN;
    }

}

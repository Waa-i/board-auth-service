package com.example.board.auth.service;

import com.example.board.auth.dto.command.MemberCredentialCreateCommand;
import com.example.board.auth.entity.MemberCredential;
import com.example.board.auth.entity.MemberStatus;
import com.example.board.auth.exception.IllegalCredentialStateException;
import com.example.board.auth.exception.IllegalMemberStatusChangeException;
import com.example.board.auth.repository.MemberCredentialRepository;
import com.example.board.auth.service.impl.MemberCredentialTransactionServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberCredentialTransactionServiceTest {
    @Mock
    private MemberCredentialRepository memberCredentialRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    private MemberCredentialTransactionServiceImpl memberCredentialTransactionService;

    @Test
    @DisplayName("회원 자격 증명 생성")
    void createCredential_success() {
        var id = 1L;
        var username = "testuser";
        var rawPassword = "raw1234";
        var hashedPassword = "hash1234";
        var email = "testuser@gmail.com";
        var command = new MemberCredentialCreateCommand(username, rawPassword, email);
        MemberCredential member = MemberCredential.createMember(username, hashedPassword, email);
        ReflectionTestUtils.setField(member, "id", id);

        when(passwordEncoder.encode(rawPassword)).thenReturn(hashedPassword);
        when(memberCredentialRepository.saveAndFlush(any(MemberCredential.class))).thenReturn(member);

        var actual = memberCredentialTransactionService.createCredential(command);
        Assertions.assertEquals(id, actual);

        verify(passwordEncoder, times(1)).encode(rawPassword);
        verify(memberCredentialRepository, times(1)).saveAndFlush(any(MemberCredential.class));
    }

    @Test
    @DisplayName("회원 자격 증명 삭제")
    void deleteCredential_success() {
        var id = 1L;
        doNothing().when(memberCredentialRepository).deleteById(id);

        assertDoesNotThrow(() -> memberCredentialTransactionService.deleteCredential(id));

        verify(memberCredentialRepository, times(1)).deleteById(id);
    }

    @Test
    @DisplayName("회원 자격 증명 상태 변경 - ACTIVE")
    void activateCredential_success() {
        var id = 1L;
        var username = "testuser";
        var hashedPassword = "hash1234";
        var email = "testuser@gmail.com";
        var credential = MemberCredential.createMember(username, hashedPassword, email);
        when(memberCredentialRepository.findById(id)).thenReturn(Optional.of(credential));

        memberCredentialTransactionService.activateCredential(id);
        assertEquals(MemberStatus.ACTIVE, credential.getStatus());

        verify(memberCredentialRepository, times(1)).findById(id);
    }

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.EXCLUDE, names = {"PENDING"})
    @DisplayName("회원 자격 증명 활성화 실패 - PENDING 상태가 아닌 경우")
    void activateCredential_fail_when_not_pending(MemberStatus status) {
        var id = 1L;
        var username = "testuser";
        var hashedPassword = "hash1234";
        var email = "testuser@gmail.com";
        var credential = MemberCredential.createMember(username, hashedPassword, email);
        ReflectionTestUtils.setField(credential, "status", status);
        when(memberCredentialRepository.findById(id)).thenReturn(Optional.of(credential));

        assertThrows(IllegalMemberStatusChangeException.class, () -> memberCredentialTransactionService.activateCredential(id));

        verify(memberCredentialRepository, times(1)).findById(id);
    }

    @Test
    @DisplayName("회원 자격 증명 활성화 실패 - 존재하지 않는 ID")
    void activateCredential_fail_when_not_found() {
        var id = 123L;
        when(memberCredentialRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(IllegalCredentialStateException.class, () -> memberCredentialTransactionService.activateCredential(id));

        verify(memberCredentialRepository, times(1)).findById(id);
    }
}
package com.mercato.Service;

import com.mercato.Entity.AppRole;
import com.mercato.Entity.EcommUser;
import com.mercato.Payloads.Response.EcommUserResponseDTO;
import com.mercato.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<EcommUserResponseDTO> getAllUsers() {
        List<EcommUser> allUsers = userRepository.findAll();

        return allUsers.stream()
                .map(this::buildEcommUserResponseDTO)
                .toList();
    }

    @Override
    public List<EcommUserResponseDTO> getAllSellers() {
        List<EcommUser> allSellers = userRepository.findUsersByRoleNames(List.of(AppRole.ROLE_SELLER));

        return allSellers.stream()
                .map(this::buildEcommUserResponseDTO)
                .toList();
    }

    private EcommUserResponseDTO buildEcommUserResponseDTO(EcommUser user) {
        List<String> roles = user.getRoles().stream()
                .map(role -> role.getRoleName().name())
                .collect(Collectors.toList());

        return new EcommUserResponseDTO(
                user.getUserId(),
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getProfileImageUrl(),
                user.isEnabled(),
                user.isAccountLocked(),
                user.isEmailVerified(),
                user.getCreatedAt(),
                user.getLastLoginAt(),
                user.isSeller(),
                user.getSellerDisplayName(),
                roles
        );
    }
}

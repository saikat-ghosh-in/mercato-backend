package com.mercato.Service;

import com.mercato.Entity.AppRole;
import com.mercato.Entity.EcommUser;
import com.mercato.Mapper.EcommUserMapper;
import com.mercato.Payloads.Response.EcommUserResponseDTO;
import com.mercato.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<EcommUserResponseDTO> getAllUsers() {
        List<EcommUser> allUsers = userRepository.findAll();

        return allUsers.stream()
                .map(EcommUserMapper::toDto)
                .toList();
    }

    @Override
    public List<EcommUserResponseDTO> getAllSellers() {
        List<EcommUser> allSellers = userRepository.findUsersByRoleNames(List.of(AppRole.ROLE_SELLER));

        return allSellers.stream()
                .map(EcommUserMapper::toDto)
                .toList();
    }
}

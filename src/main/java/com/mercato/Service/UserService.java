package com.mercato.Service;

import com.mercato.Payloads.Request.UpdateProfileRequestDTO;
import com.mercato.Payloads.Response.AccountDeletionStatusResponseDTO;
import com.mercato.Payloads.Response.EcommUserResponseDTO;
import com.mercato.Payloads.Response.SellerResponseDTO;

import java.util.List;
import java.util.Set;

public interface UserService {

    List<EcommUserResponseDTO> getAllUsers();

    void updateUserRoles(String userId, Set<String> roles);

    List<SellerResponseDTO> getAllSellers();

    EcommUserResponseDTO updateProfile(UpdateProfileRequestDTO request);

    void deactivateAccount();

    void deactivateUser(String userId, boolean deleteNow);

    void reactivateAccount(String userId);

    AccountDeletionStatusResponseDTO getAccountDeletionStatus();
}

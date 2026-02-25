package com.ecommerce_backend.Controller;

import com.ecommerce_backend.Payloads.Request.AddressRequestDTO;
import com.ecommerce_backend.Payloads.Response.AddressResponseDTO;
import com.ecommerce_backend.Service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    @PostMapping("/users/addresses/add")
    public ResponseEntity<AddressResponseDTO> createAddress(@RequestBody AddressRequestDTO addressRequestDTO) {
        AddressResponseDTO savedAddressResponseDTO = addressService.createAddress(addressRequestDTO);
        return new ResponseEntity<>(savedAddressResponseDTO, HttpStatus.CREATED);
    }

    @GetMapping("/admin/addresses")
    public ResponseEntity<List<AddressResponseDTO>> getAllAddresses() {
        List<AddressResponseDTO> addressResponseDTOList = addressService.getAllAddresses();
        return new ResponseEntity<>(addressResponseDTOList, HttpStatus.OK);
    }

    @GetMapping("/admin/addresses/{addressId}")
    public ResponseEntity<AddressResponseDTO> getAddress(@PathVariable Long addressId) {
        AddressResponseDTO addressResponseDTO = addressService.getAddress(addressId);
        return new ResponseEntity<>(addressResponseDTO, HttpStatus.OK);
    }

    @GetMapping("/users/addresses")
    public ResponseEntity<List<AddressResponseDTO>> getUserAddresses() {
        List<AddressResponseDTO> addressResponseDTOList = addressService.getUserAddresses();
        return new ResponseEntity<>(addressResponseDTOList, HttpStatus.OK);
    }

    @PutMapping("/users/addresses/{addressId}")
    public ResponseEntity<AddressResponseDTO> updateAddress(@PathVariable Long addressId,
                                                            @RequestBody AddressRequestDTO addressRequestDTO) {
        AddressResponseDTO updatedAddressResponseDTO = addressService.updateAddress(addressId, addressRequestDTO);
        return new ResponseEntity<>(updatedAddressResponseDTO, HttpStatus.OK);
    }

    @DeleteMapping("/users/addresses/{addressId}")
    public ResponseEntity<String> deleteAddress(@PathVariable Long addressId) {
        addressService.deleteAddress(addressId);
        return new ResponseEntity<>("Address deleted successfully", HttpStatus.OK);
    }
}

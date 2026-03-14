package com.mercato.Controller;

import com.mercato.Payloads.Request.AddressRequestDTO;
import com.mercato.Payloads.Response.AddressResponseDTO;
import com.mercato.Service.AddressService;
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

    @PostMapping("/user/addresses/add")
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
    public ResponseEntity<AddressResponseDTO> getAddress(@PathVariable String addressId) {
        AddressResponseDTO addressResponseDTO = addressService.getAddress(addressId);
        return new ResponseEntity<>(addressResponseDTO, HttpStatus.OK);
    }

    @GetMapping("/user/addresses")
    public ResponseEntity<List<AddressResponseDTO>> getUserAddresses() {
        List<AddressResponseDTO> addressResponseDTOList = addressService.getUserAddresses();
        return new ResponseEntity<>(addressResponseDTOList, HttpStatus.OK);
    }

    @PutMapping("/user/addresses/{addressId}")
    public ResponseEntity<AddressResponseDTO> updateAddress(@PathVariable String addressId,
                                                            @RequestBody AddressRequestDTO addressRequestDTO) {
        AddressResponseDTO updatedAddressResponseDTO = addressService.updateAddress(addressId, addressRequestDTO);
        return new ResponseEntity<>(updatedAddressResponseDTO, HttpStatus.OK);
    }

    @DeleteMapping("/user/addresses/{addressId}")
    public ResponseEntity<String> deleteAddress(@PathVariable String addressId) {
        addressService.deleteAddress(addressId);
        return new ResponseEntity<>("Address deleted successfully", HttpStatus.OK);
    }
}

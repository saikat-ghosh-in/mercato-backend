package com.ecommerce_backend.Controller;

import com.ecommerce_backend.Payloads.AddressDto;
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
    public ResponseEntity<AddressDto> createAddress(@RequestBody AddressDto addressDto) {
        AddressDto savedAddressDto = addressService.createAddress(addressDto);
        return new ResponseEntity<>(savedAddressDto, HttpStatus.CREATED);
    }

    @GetMapping("/admin/addresses")
    public ResponseEntity<List<AddressDto>> getAllAddresses() {
        List<AddressDto> addressDtoList = addressService.getAllAddresses();
        return new ResponseEntity<>(addressDtoList, HttpStatus.OK);
    }

    @GetMapping("/admin/addresses/{addressId}")
    public ResponseEntity<AddressDto> getAddress(@PathVariable Long addressId) {
        AddressDto addressDto = addressService.getAddress(addressId);
        return new ResponseEntity<>(addressDto, HttpStatus.OK);
    }


    @GetMapping("/users/addresses")
    public ResponseEntity<List<AddressDto>> getUserAddresses() {
        List<AddressDto> addressDtoList = addressService.getUserAddresses();
        return new ResponseEntity<>(addressDtoList, HttpStatus.OK);
    }

    @PutMapping("/users/addresses/{addressId}")
    public ResponseEntity<AddressDto> updateAddress(@PathVariable Long addressId,
                                                    @RequestBody AddressDto addressDto) {
        AddressDto updatedAddressDto = addressService.updateAddress(addressId, addressDto);
        return new ResponseEntity<>(updatedAddressDto, HttpStatus.OK);
    }

    @DeleteMapping("/users/addresses/{addressId}")
    public ResponseEntity<String> deleteAddress(@PathVariable Long addressId) {
        addressService.deleteAddress(addressId);
        return new ResponseEntity<>("Address deleted successfully", HttpStatus.OK);
    }
}

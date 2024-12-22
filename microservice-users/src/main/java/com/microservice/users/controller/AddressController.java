package com.microservice.users.controller;


import com.microservice.users.dto.AddressRequest;
import com.microservice.users.dto.AddressResponse;
import com.microservice.users.model.Address;
import com.microservice.users.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users/{userId}/addresses")
@RequiredArgsConstructor
public class AddressController {
    private final AddressService addressService;

    @PostMapping
    public ResponseEntity<AddressResponse> addAddress(@PathVariable Long userId, @RequestBody AddressRequest request) {
        Address address = addressService.addAddress(userId, request);
        AddressResponse response = new AddressResponse(
                address.getId(),
                address.getLabel(),
                address.getLine1(),
                address.getLine2(),
                address.getCity(),
                address.getState(),
                address.getCountry(),
                address.getZipCode(),
                address.getPhoneNumber(),
                address.getDefaultAddress()
        );
        return ResponseEntity.ok(response);
    }
}
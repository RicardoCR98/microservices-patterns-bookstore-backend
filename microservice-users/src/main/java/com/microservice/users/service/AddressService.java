package com.microservice.users.service;


import com.microservice.users.dto.AddressRequest;
import com.microservice.users.exceptions.UserNotFoundException;
import com.microservice.users.model.Address;
import com.microservice.users.model.UserProfile;
import com.microservice.users.repositories.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AddressService {
    private final UserProfileRepository repository;

    public Address addAddress(Long userId, AddressRequest request) {
        UserProfile profile = repository.findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Address address = new Address();
        address.setId(UUID.randomUUID().toString());
        address.setLabel(request.getLabel());
        address.setLine1(request.getLine1());
        address.setLine2(request.getLine2());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setCountry(request.getCountry());
        address.setZipCode(request.getZipCode());
        address.setPhoneNumber(request.getPhoneNumber());
        address.setDefaultAddress(request.getDefaultAddress());

        if (profile.getAddresses() == null) {
            profile.setAddresses(List.of(address));
        } else {
            // Si es defaultAddress, poner esta y quitar la default de antes
            if (Boolean.TRUE.equals(address.getDefaultAddress())) {
                profile.getAddresses().forEach(a -> a.setDefaultAddress(false));
            }
            profile.getAddresses().add(address);
        }

        repository.save(profile);
        return address;
    }

    //TODO: Métodos adicionales para actualizar o eliminar direcciones
}
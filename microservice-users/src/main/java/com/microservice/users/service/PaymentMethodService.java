package com.microservice.users.service;


import com.microservice.users.dto.PaymentMethodRequest;
import com.microservice.users.exceptions.UserNotFoundException;
import com.microservice.users.model.PaymentMethod;
import com.microservice.users.model.UserProfile;
import com.microservice.users.repositories.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentMethodService {
    private final UserProfileRepository repository;

    public PaymentMethod addPaymentMethod(Long userId, PaymentMethodRequest request) {
        UserProfile profile = repository.findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        PaymentMethod pm = new PaymentMethod();
        pm.setId(UUID.randomUUID().toString());
        pm.setCardHolderName(request.getCardHolderName());
        pm.setLast4(request.getLast4());
        pm.setExpirationMonth(request.getExpirationMonth());
        pm.setExpirationYear(request.getExpirationYear());
        pm.setCardNumber(request.getCardNumber());

        if (profile.getPaymentMethods() == null) {
            profile.setPaymentMethods(List.of(pm));
        } else {
            profile.getPaymentMethods().add(pm);
        }

        repository.save(profile);
        return pm;
    }
}
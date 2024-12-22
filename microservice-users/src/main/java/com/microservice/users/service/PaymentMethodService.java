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
        pm.setType(request.getType());
        pm.setCardHolderName(request.getCardHolderName());
        pm.setCardBrand(request.getCardBrand());
        pm.setLast4(request.getLast4());
        pm.setExpirationMonth(request.getExpirationMonth());
        pm.setExpirationYear(request.getExpirationYear());
        pm.setDefaultMethod(request.getDefaultMethod());
        pm.setToken(request.getToken());

        if (profile.getPaymentMethods() == null) {
            profile.setPaymentMethods(List.of(pm));
        } else {
            // Si es default, remover default de otro
            if (Boolean.TRUE.equals(pm.getDefaultMethod())) {
                profile.getPaymentMethods().forEach(m -> m.setDefaultMethod(false));
            }
            profile.getPaymentMethods().add(pm);
        }

        repository.save(profile);
        return pm;
    }

    //TODO: Agregar métodos para actualizar o eliminar metodos de pago
}
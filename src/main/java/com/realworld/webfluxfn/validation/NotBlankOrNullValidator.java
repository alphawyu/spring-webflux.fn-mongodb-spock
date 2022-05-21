package com.realworld.webfluxfn.validation;

import lombok.NoArgsConstructor;
import org.hibernate.validator.internal.constraintvalidators.bv.NotBlankValidator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

@NoArgsConstructor
public class NotBlankOrNullValidator implements ConstraintValidator<NotBlankOrNull, String> {

    private final transient NotBlankValidator notBlankValidator = new NotBlankValidator();

    @Override
    public boolean isValid(final String obj, final ConstraintValidatorContext context) {
        if (obj == null) {
            return true;
        }
        return notBlankValidator.isValid(obj, context);
    }
}

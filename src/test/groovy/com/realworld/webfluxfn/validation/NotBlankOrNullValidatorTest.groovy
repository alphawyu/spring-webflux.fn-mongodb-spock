package com.realworld.webfluxfn.validation

import com.realworld.webfluxfn.dto.request.UpdateArticleRequest
import org.hibernate.validator.internal.engine.ConstraintViolationImpl
import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Specification

import javax.validation.ConstraintViolation
import javax.validation.Validation
import javax.validation.Validator
import javax.validation.ValidatorFactory

@SpringBootTest
class NotBlankOrNullValidatorTest extends Specification {
    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    void setup() {
        validatorFactory = Validation.buildDefaultValidatorFactory()
        validator = validatorFactory.getValidator()
    }

    void cleanup() {
        validatorFactory.close()
    }

    def "test NotBlankOrNull, null fields are acceptable"() {
        given: "empty UpdateArticleRequest"
        UpdateArticleRequest testee = UpdateArticleRequest.builder().build()

        when: "validates"
        Set<ConstraintViolation<UpdateArticleRequest>> violations = validator.validate(testee)

        then: "the violation"
        violations.size() == 0
    }

    def "test NotBlankOrNull, a blank field is a violation"() {
        given: "empty UpdateArticleRequest"
        UpdateArticleRequest testee = UpdateArticleRequest.builder().body("").build()

        when: "validates"
        Set<ConstraintViolation<UpdateArticleRequest>> violations = validator.validate(testee)

        then: "the violation"
        violations.size() == 1
        ConstraintViolation[] cAry = violations.toArray()
        cAry[0].getPropertyPath().toString() == 'body'
    }
}
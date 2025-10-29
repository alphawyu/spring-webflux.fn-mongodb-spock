package com.realworld.webfluxfn.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
// @ContextConfiguration(classes = TestConfig.class)
class MongoTest {
    @Autowired
    private MongoTemplate mongoTemplate;

    @DisplayName("Given a document to save, when saving using MongoTemplate, then the document is saved and retrievable")
    @Test
    public void testSaveAndRetrieveDocument() {
        // Given
        TestDocument documentToSave = new TestDocument("testId", "testValue");

        // When
        mongoTemplate.save(documentToSave, "testCollection");

        // Then
        Query query = new Query(Criteria.where("id").is("testId"));
        TestDocument retrievedDocument = mongoTemplate.findOne(query, TestDocument.class, "testCollection");

        assertThat(retrievedDocument).isNotNull();
        assertThat(retrievedDocument.getId()).isEqualTo("testId");
        assertThat(retrievedDocument.getValue()).isEqualTo("testValue");
    }
}
package com.library.model.entity;

import lombok.Data;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "book_relations", indexes = {
        @Index(name = "idx_book_id", columnList = "book_id"),
        @Index(name = "idx_related_book_id", columnList = "related_book_id"),
        @Index(name = "idx_unique_relation", columnList = "book_id, related_book_id", unique = true)
})
public class BookRelation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "book_id", nullable = false)
    private Long bookId;

    @Column(name = "related_book_id", nullable = false)
    private Long relatedBookId;

    @Enumerated(EnumType.STRING)
    @Column(name = "relation_type", nullable = false)
    private RelationType relationType = RelationType.similar;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum RelationType {
        similar, also_bought, same_author
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
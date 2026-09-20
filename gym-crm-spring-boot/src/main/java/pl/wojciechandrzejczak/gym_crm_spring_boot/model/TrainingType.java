package org.example.model;

import jakarta.persistence.*;

@Entity
@Table(name = "training_types")
public class TrainingType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long trainingTypeId;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "training_type_name",
            nullable = false,
            unique = true,
            updatable = false
    )
    private TrainingTypeName trainingTypeName;

    protected TrainingType() {}

    public TrainingType(TrainingTypeName trainingTypeName) {
        this.trainingTypeName = trainingTypeName;
    }

    public Long getTrainingTypeId() {
        return trainingTypeId;
    }

    public TrainingTypeName getTrainingTypeName() {
        return trainingTypeName;
    }
}

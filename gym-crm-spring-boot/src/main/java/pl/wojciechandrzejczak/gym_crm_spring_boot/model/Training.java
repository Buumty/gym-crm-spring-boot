package org.example.model;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "trainings")
public class Training {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long trainingId;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "trainee_id",
            nullable = false
    )
    private Trainee trainee;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "trainer_id",
            nullable = false
    )
    private Trainer trainer;
    @Column(name = "training_name", nullable = false)
    private String trainingName;
    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "training_type_id",
            nullable = false
    )
    private TrainingType trainingType;
    @Column(name = "training_date", nullable = false)
    private LocalDate trainingDate;
    @Column(name = "training_duration", nullable = false)
    private Integer trainingDuration;

    protected Training() {}

    public Training(Trainee trainee, Trainer trainer, String trainingName, TrainingType trainingType, LocalDate trainingDate, Integer trainingDuration) {
        this.trainee = trainee;
        this.trainer = trainer;
        this.trainingName = trainingName;
        this.trainingType = trainingType;
        this.trainingDate = trainingDate;
        this.trainingDuration = trainingDuration;
    }

    public Trainee getTrainee() {
        return trainee;
    }

    public Trainer getTrainer() {
        return trainer;
    }

    public String getTrainingName() {
        return trainingName;
    }

    public TrainingType getTrainingType() {
        return trainingType;
    }

    public LocalDate getTrainingDate() {
        return trainingDate;
    }

    public Integer getTrainingDuration() {
        return trainingDuration;
    }

    public Long getTrainingId() {
        return trainingId;
    }

}

package com.capgemini.wsb.fitnesstracker.training.api;

import com.capgemini.wsb.fitnesstracker.training.internal.ActivityType;
import com.capgemini.wsb.fitnesstracker.training.internal.TrainingDto;

import java.util.Date;
import java.util.List;

public interface TrainingService {
    public List<TrainingDto> findAllTrainings();

    List<TrainingDto> findTrainingsByUserId(Long userId);

    TrainingDto createTraining(TrainingDto trainingDto);

    public List<TrainingDto> findTrainingsEndedAfter(Date endDate);

    TrainingDto updateTraining(Long id, TrainingDto trainingDto);

    List<TrainingDto> findTrainingsByActivityType(ActivityType activityType);

}

package com.capgemini.wsb.fitnesstracker.training.internal;

import com.capgemini.wsb.fitnesstracker.training.api.Training;
import com.capgemini.wsb.fitnesstracker.user.internal.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TrainingMapper {

    @Autowired
    private final UserMapper userMapper;


    public TrainingMapper(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    TrainingDto toDto(Training training) {
        return new TrainingDto(training.getId(),
                               userMapper.toDto(training.getUser()),
                               training.getStartTime(),
                               training.getEndTime(),
                               training.getActivityType(),
                               training.getDistance(),
                               training.getAverageSpeed()
                );
    }

    Training toEntity(TrainingDto trainingDto) {
        return new Training(trainingDto.getId(),
                            userMapper.toEntity(trainingDto.getUser()),
                            trainingDto.getStartTime(),
                            trainingDto.getEndTime(),
                            trainingDto.getActivityType(),
                            trainingDto.getDistance(),
                            trainingDto.getAverageSpeed()
        );
    }
}

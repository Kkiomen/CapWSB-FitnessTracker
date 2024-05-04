package com.capgemini.wsb.fitnesstracker.training.internal;

import com.capgemini.wsb.fitnesstracker.training.api.Training;
import com.capgemini.wsb.fitnesstracker.training.api.TrainingProvider;
import com.capgemini.wsb.fitnesstracker.training.api.TrainingService;
import com.capgemini.wsb.fitnesstracker.user.api.User;
import com.capgemini.wsb.fitnesstracker.user.api.UserService;
import com.capgemini.wsb.fitnesstracker.user.internal.UserDto;
import com.capgemini.wsb.fitnesstracker.user.internal.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrainingServiceImpl implements TrainingProvider, TrainingService {

    private final TrainingRepository trainingRepository;
    private final TrainingMapper trainingMapper;
    private final UserService userService;
    private final UserMapper userMapper;

    /**
     * Get training by id
     * @param trainingId id of the training to be searched
     * @return
     */
    @Override
    public Optional<User> getTraining(final Long trainingId) {
        throw new UnsupportedOperationException("Not finished yet");
    }

    /**
     * Find all trainings
     * @return list of trainings
     */
    @Override
    public List<TrainingDto> findAllTrainings() {
        return trainingRepository.findAll().stream()
                .map(trainingMapper::toDto)
                .toList();
    }

    /**
     * Find all trainings by user id
     * @param userId user id
     * @return list of trainings
     */
    @Override
    public List<TrainingDto> findTrainingsByUserId(Long userId) {
        Optional<User> user = userService.getUser(userId);
        if (user.isEmpty()) {
            throw new IllegalArgumentException("User with id " + userId + " not found");
        }

        return trainingRepository.findByUser(user).stream()
                .map(trainingMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Create a new training
     * @param trainingDto training dto
     * @return created training dto
     */
    @Override
    public TrainingDto createTraining(TrainingDto trainingDto) {

        this.validateTrainingIsNotExists(trainingDto);
        Optional<User> existedUser = getValidatedUser(trainingDto.getUser().getId());

        Training newTraining = trainingMapper.toEntity(trainingDto);
        existedUser.ifPresent(newTraining::setUser);

        Training training = trainingRepository.save(newTraining);

        return  trainingMapper.toDto(training);
    }

    /**
     * Find all trainings that ended after the provided date
     * @param endDate end date
     * @return list of trainings
     */
    @Override
    public List<TrainingDto> findTrainingsEndedAfter(Date endDate) {
        List<Training> trainings = trainingRepository.findAll();

        return trainings.stream()
                .filter(training -> training.getEndTime() != null && training.getEndTime().after(endDate))
                .map(trainingMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Find all trainings by activity type
     * @param activityType activity type
     * @return list of trainings
     */
    @Override
    public List<TrainingDto> findTrainingsByActivityType(ActivityType activityType) {
        List<Training> trainings = trainingRepository.findByActivityType(activityType);

        return trainings.stream()
                .map(trainingMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Update training
     * @param id training id
     * @param trainingDto training dto
     * @return updated training dto
     */
    @Override
    public TrainingDto updateTraining(Long id, TrainingDto trainingDto) {
        TrainingDto existingTraining = this.validateTraining(trainingDto);

        if(trainingDto.getDistance() > 0 && trainingDto.getDistance() != existingTraining.getDistance()){
            existingTraining.setDistance(trainingDto.getDistance());
        }

        if(trainingDto.getEndTime() != null && trainingDto.getEndTime() != existingTraining.getEndTime()){
            existingTraining.setEndTime(trainingDto.getEndTime());
        }

        if(trainingDto.getStartTime() != null && trainingDto.getStartTime() != existingTraining.getStartTime()){
            existingTraining.setStartTime(trainingDto.getStartTime());
        }

        if(trainingDto.getActivityType() != null && trainingDto.getActivityType() != existingTraining.getActivityType()){
            existingTraining.setActivityType(trainingDto.getActivityType());
        }

        Training updatedTraining = trainingMapper.toEntity(existingTraining);

        if(trainingDto.getUser() != null && trainingDto.getUser().getId() != null){
            updatedTraining.setUser(getValidatedUser(trainingDto.getUser().getId()).get());
        }else{
            updatedTraining.setUser(getValidatedUser(existingTraining.getUser().getId()).get());
        }


        return trainingMapper.toDto(
                trainingRepository.save(updatedTraining)
        );
    }

    /**
     * Validate if training with the same id already exists
     * @param trainingDto training dto
     */
    private TrainingDto validateTraining(TrainingDto trainingDto) {
        Optional<Training> trainingWitHSameId = trainingRepository.findById(trainingDto.getId());
        if(trainingWitHSameId.isEmpty()){
            throw new IllegalArgumentException("Training with id " + trainingDto.getId() + " already exists");
        }

        return trainingMapper.toDto(trainingWitHSameId.get());
    }

    /**
     * Validate if training with the same id already exists
     * @param trainingDto training dto
     */
    private void validateTrainingIsNotExists(TrainingDto trainingDto) {
        Optional<Training> trainingWitHSameId = trainingRepository.findById(trainingDto.getId());
        if(trainingWitHSameId.isPresent()){
            throw new IllegalArgumentException("Training with id " + trainingDto.getId() + " already exists");
        }
    }

    /**
     * Get user by id and validate if exists
     * @param userId user id
     * @return UserDto
     */
    private Optional<User> getValidatedUser(Long userId) {
        Optional<User> user = userService.getUser(userId);
        if (user.isEmpty()) {
            throw new IllegalArgumentException("User with id " + userId + " not found");
        }

        return user;

    }
}

import com.capgemini.wsb.FitnessTracker;
import com.capgemini.wsb.fitnesstracker.training.api.Training;
import com.capgemini.wsb.fitnesstracker.training.api.TrainingService;
import com.capgemini.wsb.fitnesstracker.training.internal.ActivityType;
import com.capgemini.wsb.fitnesstracker.training.internal.TrainingDto;
import com.capgemini.wsb.fitnesstracker.training.internal.TrainingMapper;
import com.capgemini.wsb.fitnesstracker.training.internal.TrainingRepository;
import com.capgemini.wsb.fitnesstracker.user.api.User;
import com.capgemini.wsb.fitnesstracker.user.internal.UserDto;
import com.capgemini.wsb.fitnesstracker.user.internal.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(classes = FitnessTracker.class)
@Transactional
public class TrainingServiceTest {

    @Autowired
    private TrainingService trainingService;

    @Autowired
    private TrainingRepository trainingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TrainingMapper trainingMapper;

    @Test
    void findAllTrainings_ReturnsAllTrainings() {
        // given: Set up initial database state
        User user = new User("Jane", "Doe", LocalDate.now(), "jane.doe@example.com");
        userRepository.save(user);

        Training training = new Training(user, new Date(), new Date(), ActivityType.RUNNING, 5.0, 10.0);
        trainingRepository.save(training);

        // when: Perform the actual call
        List<TrainingDto> result = trainingService.findAllTrainings();

        // then: Assert the results
        assertThat(result).isNotEmpty();
        boolean hasEmail = result.stream()
                .anyMatch(t -> t.getUser().getEmail().equals("jane.doe@example.com"));
        assertThat(hasEmail).isTrue();
    }

    @Test
    void findTrainingsByUserId_WhenUserExists_ShouldReturnTrainings() {
        // given
        User user = new User("John", "Doe", LocalDate.now(), "john.doe@example.com");
        user = userRepository.save(user);
        Training training = new Training(user, new Date(), new Date(), ActivityType.RUNNING, 5.0, 10.0);
        trainingRepository.save(training);

        // when
        List<TrainingDto> result = trainingService.findTrainingsByUserId(user.getId());

        // then
        assertThat(result).isNotEmpty();
    }

    @Test
    void findTrainingsByUserId_WhenUserDoesNotExist_ShouldThrowException() {
        // given
        Long userId = 999L; // Assuming this ID does not exist

        // when & then
        assertThrows(IllegalArgumentException.class, () -> trainingService.findTrainingsByUserId(userId));
    }


    @Test
    void createTraining_ShouldSaveAndReturnTrainingDto() {
        // given
        TrainingDto trainingDto = new TrainingDto(995L,
                new UserDto(10L, null,null, null, null),
                new Date(), new Date(), ActivityType.CYCLING, 20.0, 15.0);

        // when
        TrainingDto savedTraining = trainingService.createTraining(trainingDto);

        // then
        assertThat(savedTraining).isNotNull();
        assertThat(savedTraining.getUser().getId()).isEqualTo(trainingDto.getUser().getId());
        assertThat(savedTraining.getActivityType()).isEqualTo(ActivityType.CYCLING);
        assertThat(savedTraining.getDistance()).isEqualTo(trainingDto.getDistance());
    }


    @Test
    void findTrainingsByActivityType_ShouldReturnFilteredTrainings() {
        // given
        User user = new User("Eve", "Brown", LocalDate.now(), "eve.brown@example.com");
        userRepository.save(user);
        Training training = new Training(user, new Date(), new Date(), ActivityType.RUNNING, 10.0, 12.0);
        trainingRepository.save(training);

        // when
        List<TrainingDto> result = trainingService.findTrainingsByActivityType(ActivityType.RUNNING);

        // then
        assertThat(result).isNotEmpty();
        assertThat(result.stream()
                .anyMatch(t -> t.getActivityType() == ActivityType.RUNNING))
                .isTrue();
    }

    @Test
    public void findTrainingsEndedAfter_ShouldFilterCorrectly() throws Exception {
        // Given
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date endDate = sdf.parse("2025-01-01");

        User user = new User("John", "Doe", LocalDate.of(1990, 1, 1), "john.doe@example.com");
        userRepository.save(user);

        Training training1 = new Training(user, sdf.parse("2023-12-30"), sdf.parse("2025-01-02"), ActivityType.RUNNING, 10.0, 8.0);
        Training training2 = new Training(user, sdf.parse("2023-12-30"), sdf.parse("2024-12-31"), ActivityType.CYCLING, 20.0, 10.0);
        trainingRepository.save(training1);
        trainingRepository.save(training2);

        // When
        List<TrainingDto> results = trainingService.findTrainingsEndedAfter(endDate);

        // Then
        assertThat(results).isNotEmpty();
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getEndTime()).isAfter(endDate);
        assertThat(results.get(0).getActivityType()).isEqualTo(ActivityType.RUNNING);
    }

    @Test
    public void updateTraining_UpdatesAndReturnsUpdatedTraining() {
        // Given: Existing user and training
        User user = new User("John", "Doe", LocalDate.now(), "john@example.com");
        userRepository.save(user);

        Training training = new Training(user, new Date(), new Date(), ActivityType.CYCLING, 10.0, 15.0);
        training = trainingRepository.save(training);

        // New data for updating
        UserDto newUserDto = new UserDto(user.getId(), null, null, null,null);
        TrainingDto updateDto = new TrainingDto(training.getId(),
                newUserDto,
                new Date(training.getStartTime().getTime() + 100000), // slight time change
                new Date(training.getEndTime().getTime() + 200000),
                ActivityType.RUNNING,
                12.0,
                20.0);

        // When: Update the training
        TrainingDto updatedTraining = trainingService.updateTraining(training.getId(), updateDto);

        // Then: Verify the updated data
        assertThat(updatedTraining).isNotNull();
        assertThat(updatedTraining.getId()).isEqualTo(training.getId());
        assertThat(updatedTraining.getUser().getId()).isEqualTo(updateDto.getUser().getId());
        assertThat(updatedTraining.getDistance()).isEqualTo(12.0);
        assertThat(updatedTraining.getActivityType()).isEqualTo(ActivityType.RUNNING);

    }


}

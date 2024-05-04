package com.capgemini.wsb.fitnesstracker.training.internal;

import com.capgemini.wsb.fitnesstracker.training.api.TrainingService;
import com.capgemini.wsb.fitnesstracker.training.internal.ActivityType;
import com.capgemini.wsb.fitnesstracker.training.internal.TrainingDto;
import com.capgemini.wsb.fitnesstracker.training.internal.TrainingMapper;
import com.capgemini.wsb.fitnesstracker.user.api.User;
import com.capgemini.wsb.fitnesstracker.user.api.UserExistsException;
import com.capgemini.wsb.fitnesstracker.user.internal.UserDto;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/v1/trainings")
@RequiredArgsConstructor
public class TrainingController {

    private final TrainingService trainingService;
    private final TrainingMapper trainingMapper;

    /**
     * Get all trainings
     * @return list of all trainings
     */
    @GetMapping("/training/list")
    public List<TrainingDto> getAllUsersSummary() {
        return trainingService.findAllTrainings();
    }

    @GetMapping("/training/user/{userId}")
    public List<TrainingDto> getTrainingsByUserId(@PathVariable Long userId) {
        return trainingService.findTrainingsByUserId(userId);
    }

    @PostMapping("/training")
    public ResponseEntity<TrainingDto> addTraining(@RequestBody TrainingDto trainingDto) {
        return ResponseEntity.ok(trainingService.createTraining(trainingDto));
    }

    @GetMapping("/trainings/ended")
    public ResponseEntity<List<TrainingDto>> getAllTrainingsEndedAfter(@RequestParam String endDate) {
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            Date end = formatter.parse(endDate);
            List<TrainingDto> trainings = trainingService.findTrainingsEndedAfter(end);
            return ResponseEntity.ok(trainings);
        } catch (ParseException e) {
            return ResponseEntity.badRequest().body(null); // Or handle the parse exception more gracefully
        }
    }

    @GetMapping("/trainings/by-activity")
    public ResponseEntity<List<TrainingDto>> getAllTrainingsByActivityType(@RequestParam ActivityType activityType) {
        List<TrainingDto> trainings = trainingService.findTrainingsByActivityType(activityType);
        return ResponseEntity.ok(trainings);
    }

    @PatchMapping("/trainings/{id}")
    public ResponseEntity<TrainingDto> updateUser(@PathVariable Long id, @RequestBody TrainingDto trainingDto) {
        try {
            TrainingDto updatedTraining = trainingService.updateTraining(id, trainingDto);
            return ResponseEntity.ok(updatedTraining);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}

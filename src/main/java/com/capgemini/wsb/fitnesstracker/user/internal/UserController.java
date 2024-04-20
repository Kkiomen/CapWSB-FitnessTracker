package com.capgemini.wsb.fitnesstracker.user.internal;

import com.capgemini.wsb.fitnesstracker.user.api.User;
import com.capgemini.wsb.fitnesstracker.user.api.UserExistsException;
import com.capgemini.wsb.fitnesstracker.user.api.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    private final UserMapper userMapper;

    /**
     * Retrieves a summary list of all users.
     * Each summary includes the user's ID and full name (first name and last name concatenated).
     *
     * @return A list of {@link UserSummaryDto} objects, each representing a user's summary details.
     */
    @GetMapping("/user/list")
    public List<UserSummaryDto> getAllUsersSummary() {
        return userService.findAllUsers()
                .stream()
                .map(user -> new UserSummaryDto(user.getId(), user.getFirstName() + " " + user.getLastName()))
                .toList();
    }

    /**
     * Retrieves detailed information about a specific user identified by their ID.
     *
     * @param id The unique identifier of the user.
     * @return A {@link ResponseEntity} containing a {@link UserDto} if the user is found,
     *         or a not found status if the user does not exist.
     */
    @GetMapping("/user/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        return userService.getUser(id)
                .map(user -> ResponseEntity.ok(userMapper.toDto(user)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Creates a new user based on the provided user data transfer object (DTO).
     *
     * @param userDto The {@link UserDto} object containing the user's data.
     * @return A {@link ResponseEntity} with the created {@link User}.
     * @throws UserExistsException if a user with the given details already exists.
     */
    @PostMapping("/user/add")
    public ResponseEntity<User> addUser(@RequestBody UserDto userDto) throws UserExistsException {
        User user = userMapper.toEntity(userDto);
        return ResponseEntity.ok(userService.createUser(user));
    }

    /**
     * Deletes a user identified by their ID.
     *
     * @param id The unique identifier of the user to be deleted.
     * @return A {@link ResponseEntity} indicating the outcome (OK if successful, not found if user does not exist).
     */
    @DeleteMapping("/user/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        return userService.deleteUser(id) ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    /**
     * Searches for users by a fragment of their email address.
     *
     * @param email The email fragment to search for.
     * @return A list of {@link UserSummaryDto} objects containing the IDs and emails of users matching the search.
     */
    @GetMapping("/user/search")
    public List<UserSummaryDto> searchUsersByEmail(@RequestParam String email) {
        return userService.findByEmailIgnoreCaseContaining(email)
                .stream()
                .map(user -> new UserSummaryDto(user.getId(), user.getEmail())) // Zmodyfikowany DTO
                .toList();
    }

    /**
     * Retrieves a list of users who are older than a specified age.
     *
     * @param age The age threshold users must be older than.
     * @return A list of {@link UserDto} objects representing the users who meet the age criteria.
     */
    @GetMapping("/user/age")
    public List<UserDto> getUsersOlderThan(@RequestParam int age) {
        return userService.findUsersOlderThan(age)
                .stream()
                .map(userMapper::toDto)
                .toList();
    }

    /**
     * Updates the details of an existing user identified by their ID.
     *
     * @param id The unique identifier of the user to update.
     * @param userDto The updated user data transfer object.
     * @return A {@link ResponseEntity} containing the updated user's details if successful,
     *         a bad request status if the update data is invalid, or not found if the user does not exist.
     */
    @PutMapping("/user/{id}")
    public ResponseEntity<UserDto> updateUser(@PathVariable Long id, @RequestBody UserDto userDto) {
        try {
            User updatedUser = userService.updateUser(id, userMapper.toEntity(userDto));
            return ResponseEntity.ok(userMapper.toDto(updatedUser));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

}
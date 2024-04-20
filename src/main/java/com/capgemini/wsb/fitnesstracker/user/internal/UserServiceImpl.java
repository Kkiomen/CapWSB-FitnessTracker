package com.capgemini.wsb.fitnesstracker.user.internal;

import com.capgemini.wsb.fitnesstracker.user.api.User;
import com.capgemini.wsb.fitnesstracker.user.api.UserExistsException;
import com.capgemini.wsb.fitnesstracker.user.api.UserProvider;
import com.capgemini.wsb.fitnesstracker.user.api.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
class UserServiceImpl implements UserService, UserProvider {

    private final UserRepository userRepository;

    /**
     * Creates a new user in the database.
     * Throws {@link UserExistsException} if a user with the same email already exists.
     * Throws {@link IllegalArgumentException} if the provided user object already has an assigned database ID.
     *
     * @param user The {@link User} object to be created.
     * @return The newly created {@link User}.
     * @throws UserExistsException if a user with the same email already exists.
     * @throws IllegalArgumentException if the user object already has a database ID.
     */
    @Override
    public User createUser(final User user) throws UserExistsException {
        log.info("Creating User {}", user);
        if (user.getId() != null) {
            throw new IllegalArgumentException("User has already DB ID, update is not permitted!");
        }

        Optional<User> userWithSameEmail = this.getUserByEmail(user.getEmail());
        if(userWithSameEmail.isPresent()){
            throw new UserExistsException(user.getEmail());
        }

        return userRepository.save(user);
    }

    /**
     * Retrieves a user by their unique ID.
     *
     * @param userId The ID of the user to retrieve.
     * @return An {@link Optional} containing the {@link User} if found, or empty if not found.
     */
    @Override
    public Optional<User> getUser(final Long userId) {
        return userRepository.findById(userId);
    }

    /**
     * Retrieves a user by their email address.
     *
     * @param email The email address of the user.
     * @return An {@link Optional} containing the {@link User} if found, or empty if no user has the specified email.
     */
    @Override
    public Optional<User> getUserByEmail(final String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Retrieves all users from the database.
     *
     * @return A list of all {@link User} entities in the database.
     */
    @Override
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Deletes a user by their ID.
     * Returns true if the user was found and deleted, false otherwise.
     *
     * @param userId The ID of the user to be deleted.
     * @return true if the user was deleted, false if the user does not exist.
     */
    public boolean deleteUser(Long userId) {
        if (userRepository.existsById(userId)) {
            userRepository.deleteById(userId);
            return true;
        }
        return false;
    }

    /**
     * Finds users whose email contains the specified fragment, ignoring case.
     *
     * @param emailFragment The fragment of the email to search for.
     * @return A list of {@link User} entities that have email addresses containing the specified fragment.
     */
    public List<User> findByEmailIgnoreCaseContaining(String emailFragment) {
        return userRepository.findByEmailIgnoreCaseContaining(emailFragment);
    }

    /**
     * Finds all users who are older than the specified age.
     *
     * @param age The age threshold.
     * @return A list of {@link User} entities who are older than the specified age.
     */
    public List<User> findUsersOlderThan(int age) {
        LocalDate thresholdDate = LocalDate.now().minusYears(age);
        return userRepository.findAllByBirthdateBefore(thresholdDate);
    }

    /**
     * Updates the details of an existing user identified by their ID.
     * Throws {@link EntityNotFoundException} if no user is found with the given ID.
     *
     * @param id The ID of the user to update.
     * @param newUserDetails The new details to be updated in the user's record.
     * @return The updated {@link User}.
     * @throws EntityNotFoundException if no user is found with the specified ID.
     */
    public User updateUser(Long id, User newUserDetails) {
        return userRepository.findById(id).map(existingUser -> {
            // Aktualizacja imienia, jeśli przekazano w żądaniu
            if (newUserDetails.getFirstName() != null) {
                existingUser.setFirstName(newUserDetails.getFirstName());
            }

            // Aktualizacja nazwiska, jeśli przekazano w żądaniu
            if (newUserDetails.getLastName() != null) {
                existingUser.setLastName(newUserDetails.getLastName());
            }

            // Aktualizacja daty urodzenia, jeśli przekazano w żądaniu
            if (newUserDetails.getBirthdate() != null) {
                existingUser.setBirthdate(newUserDetails.getBirthdate());
            }

            // Aktualizacja emaila, jeśli przekazano w żądaniu
            if (newUserDetails.getEmail() != null) {
                existingUser.setEmail(newUserDetails.getEmail());
            }

            log.info("Updating User {}", existingUser);
            return userRepository.save(existingUser);
        }).orElseThrow(() -> new EntityNotFoundException("User not found with id " + id));
    }

}
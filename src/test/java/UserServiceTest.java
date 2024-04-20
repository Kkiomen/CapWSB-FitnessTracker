import com.capgemini.wsb.FitnessTracker;
import com.capgemini.wsb.fitnesstracker.user.api.User;
import com.capgemini.wsb.fitnesstracker.user.api.UserExistsException;
import com.capgemini.wsb.fitnesstracker.user.api.UserService;
import com.capgemini.wsb.fitnesstracker.user.internal.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = FitnessTracker.class)
@Transactional
public class UserServiceTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Test
    void createUser_ShouldSaveNewUser() throws UserExistsException {
        // GIVEN
        User user = new User("Test", "User", LocalDate.of(2000, 1, 1), "testuser@example.com");

        // WHEN
        User created = userService.createUser(user);

        // THEN
        assertNotNull(created);
        assertNotNull(created.getId());
    }

    @Test
    void createUser_ShouldThrowException_WhenUserExists() {
        // GIVEN
        User user = new User("Existing", "User", LocalDate.of(2000, 1, 1), "existinguser@example.com");
        userRepository.save(user);

        // WHEN + THEN
        assertThrows(UserExistsException.class, () -> {
            userService.createUser(new User("Existing", "User", LocalDate.of(2000, 1, 1), "existinguser@example.com"));
        });
    }

    @Test
    void getUser_ShouldRetrieveUser() {
        // GIVEN
        User user = userRepository.save(new User("Retrieve", "User", LocalDate.of(2000, 1, 1), "retrieveuser@example.com"));

        // WHEN
        Optional<User> found = userService.getUser(user.getId());

        // THEN
        assertTrue(found.isPresent());
        assertEquals(user.getEmail(), found.get().getEmail());
    }

    @Test
    void getUserByEmail_ShouldRetrieveUser() {
        // GIVEN
        User user = userRepository.save(new User("Email", "User", LocalDate.of(2000, 1, 1), "emailuser@example.com"));

        // WHEN
        Optional<User> found = userService.getUserByEmail(user.getEmail());

        // THEN
        assertTrue(found.isPresent());
        assertEquals(user.getEmail(), found.get().getEmail());
    }

    @Test
    void findAllUsers_ShouldReturnAllUsers() {
        // GIVEN
        userRepository.save(new User("All", "Users1", LocalDate.of(2000, 1, 1), "allusers1@example.com"));
        userRepository.save(new User("All", "Users2", LocalDate.of(2000, 1, 1), "allusers2@example.com"));

        // WHEN
        List<User> users = userService.findAllUsers();

        // THEN
        assertFalse(users.isEmpty());
    }

    @Test
    void deleteUser_ShouldRemoveUser() {
        // GIVEN
        User user = userRepository.save(new User("Delete", "User", LocalDate.of(2000, 1, 1), "deleteuser@example.com"));

        // WHEN
        boolean isDeleted = userService.deleteUser(user.getId());

        // THEN
        assertTrue(isDeleted);
        Optional<User> deletedUser = userRepository.findById(user.getId());
        assertFalse(deletedUser.isPresent());
    }

    @Test
    void findByEmailIgnoreCaseContaining_ShouldFindUsers() {
        // GIVEN
        User user = userRepository.save(new User("Find", "ByEmail", LocalDate.of(2000, 1, 1), "findbyemail@example.com"));

        // WHEN
        List<User> users = userService.findByEmailIgnoreCaseContaining("findbyemail");

        // THEN
        assertFalse(users.isEmpty());
        assertTrue(users.stream().anyMatch(u -> u.getEmail().equalsIgnoreCase(user.getEmail())));
    }

    @Test
    void findUsersOlderThan_ShouldFindUsers() {
        // GIVEN
        User olderUser = userRepository.save(new User("Older", "User", LocalDate.now().minusYears(30), "olderuser@example.com"));
        User youngerUser = userRepository.save(new User("Younger", "User", LocalDate.now().minusYears(10), "youngeruser@example.com"));

        // WHEN
        List<User> users = userService.findUsersOlderThan(20);

        // THEN
        assertTrue(users.contains(olderUser));
        assertFalse(users.contains(youngerUser));
    }

    @Test
    void updateUser_ShouldUpdateUserDetails() {
        // GIVEN
        User user = userRepository.save(new User("Update", "User", LocalDate.of(2000, 1, 1), "updateuser@example.com"));
        User updatedDetails = new User("Updated", "UserDetails", user.getBirthdate(), user.getEmail());

        // WHEN
        User updatedUser = userService.updateUser(user.getId(), updatedDetails);

        // THEN
        assertNotNull(updatedUser);
        assertEquals("Updated", updatedUser.getFirstName());
        assertEquals("UserDetails", updatedUser.getLastName());
    }

    @Test
    void updateUser_ShouldThrowException_WhenUserNotFound() {
        // GIVEN
        User updatedDetails = new User("NotFound", "User", LocalDate.of(2000, 1, 1), "notfounduser@example.com");

        // WHEN + THEN
        assertThrows(EntityNotFoundException.class, () -> {
            userService.updateUser(999L, updatedDetails);
        });
    }
}
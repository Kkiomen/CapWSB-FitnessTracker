package com.capgemini.wsb.fitnesstracker.user.internal;

import com.capgemini.wsb.fitnesstracker.user.api.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserDto toDto(User user) {
        return new UserDto(user.getId(),
                           user.getFirstName(),
                           user.getLastName(),
                           user.getBirthdate(),
                           user.getEmail());
    }

    public User toEntity(UserDto userDto) {
        return new User(
                        userDto.getFirstName(),
                        userDto.getLastName(),
                        userDto.getBirthdate(),
                        userDto.getEmail());
    }

}

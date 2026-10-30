package com.jcaa.appcrudusers.adapter.mapper;

import com.jcaa.appcrudusers.adapter.dto.request.CreateUserRequest;
import com.jcaa.appcrudusers.adapter.dto.request.UpdateUserRequest;
import com.jcaa.appcrudusers.adapter.dto.response.UserResponse;
import com.jcaa.appcrudusers.domain.model.User;
import com.jcaa.appcrudusers.domain.model.UserRegistration;
import com.jcaa.appcrudusers.domain.model.UserUpdate;
import com.jcaa.appcrudusers.domain.vo.Email;
import com.jcaa.appcrudusers.domain.vo.Password;
import com.jcaa.appcrudusers.domain.vo.UserId;
import com.jcaa.appcrudusers.domain.vo.UserName;
import com.jcaa.appcrudusers.domain.vo.UserRole;
import com.jcaa.appcrudusers.domain.vo.UserStatus;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;

@UtilityClass
public class UserMapper {
    public static CreateUserRequest toRequest(final UserRegistration user) {
        return new CreateUserRequest(
                user.getId().getValue(),
                user.getName().getValue(),
                user.getEmail().getValue(),
                user.getPassword().getValue(),
                user.getRole().getValue());
    }

    public static UpdateUserRequest toRequest(final UserUpdate user) {
        return new UpdateUserRequest(
                user.getName().getValue(),
                user.getEmail().getValue(),
                user.getPassword().map(Password::getValue).orElse(null),
                user.getRole().getValue(),
                user.getStatus().getValue());
    }

    public static User toDomain(final UserResponse user) {
        return new User(
                new UserId(user.getId()),
                new UserName(user.getName()),
                new Email(user.getEmail()),
                new UserRole(user.getRole()),
                new UserStatus(user.getStatus()));
    }

    public static List<User> toDomain(final List<UserResponse> users) {
        return users.stream()
                .map(UserMapper::toDomain)
                .collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));
    }
}

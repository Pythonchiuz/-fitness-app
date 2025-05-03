package com.backendfitnessapp.dto;

import com.backendfitnessapp.enums.ActivityType;
import com.backendfitnessapp.enums.Target;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.annotations.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignUpRequestBody {
    @NotNull()
    private String email;
    private String firstName;
    private String lastName;
    private String password;
    private ActivityType preferableActivity;
    private Target target;
}

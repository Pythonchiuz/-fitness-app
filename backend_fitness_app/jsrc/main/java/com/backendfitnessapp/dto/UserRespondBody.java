package com.backendfitnessapp.dto;

import com.backendfitnessapp.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRespondBody {
    private String userId;
    private String name;
    private Role role;
    private String email;
}

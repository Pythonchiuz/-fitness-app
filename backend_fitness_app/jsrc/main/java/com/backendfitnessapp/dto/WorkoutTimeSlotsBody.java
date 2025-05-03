package com.backendfitnessapp.dto;

import com.backendfitnessapp.enums.ActivityType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class WorkoutTimeSlotsBody {
    private List<String> timeSlots;
    private String duration;
    private ActivityType type;
}

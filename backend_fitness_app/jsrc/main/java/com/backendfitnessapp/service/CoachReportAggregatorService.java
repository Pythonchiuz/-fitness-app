package com.backendfitnessapp.service;

import com.backendfitnessapp.dto.CoachReport;
import com.backendfitnessapp.dto.GymReport;
import com.backendfitnessapp.entity.Report;

import java.util.List;

public interface CoachReportAggregatorService {
    List<CoachReport> processAggregate(List<Report> currentReprot, List<Report> previousReprot);

}

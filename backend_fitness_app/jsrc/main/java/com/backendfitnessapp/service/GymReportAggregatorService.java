package com.backendfitnessapp.service;

import com.backendfitnessapp.dto.GymReport;
import com.backendfitnessapp.entity.Report;

import java.util.List;

public interface GymReportAggregatorService {
    List<GymReport> processAggregate(List<Report> currentReprot, List<Report> previousReprot);
}

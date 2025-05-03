package com.backendfitnessapp.service;

import com.backendfitnessapp.entity.Report;

import java.util.List;
import java.util.Optional;

public interface ReportService {
    void saveReport(Report newReport);
    Optional<Report> getReportByWorkoutId(String workoutId);
    List<Report> getAllReports();
    void updateReport(Report report);
}

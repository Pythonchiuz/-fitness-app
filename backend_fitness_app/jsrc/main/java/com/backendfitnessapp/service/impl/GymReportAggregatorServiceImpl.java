package com.backendfitnessapp.service.impl;

import com.backendfitnessapp.dto.GymReport;
import com.backendfitnessapp.entity.Report;
import com.backendfitnessapp.enums.ActivityType;
import com.backendfitnessapp.service.GymReportAggregatorService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

public class GymReportAggregatorServiceImpl implements GymReportAggregatorService {
    @Override
    public List<GymReport> processAggregate(List<Report> currentReport, List<Report> previousReport) {
        Map<String, Map<ActivityType, GymReport>> prevAggregated = groupReportsByLocationAndWorkoutType(currentReport);
        Map<String, Map<ActivityType, GymReport>> currAggregated = groupReportsByLocationAndWorkoutType(previousReport);

        List<GymReport> deltaReports = new ArrayList<>();

        currAggregated.forEach((location, currWorkouts) -> {
            Map<ActivityType, GymReport> prevWorkouts = prevAggregated.getOrDefault(location, new HashMap<>());

            currWorkouts.forEach((activityType, currGymReport) -> {
                GymReport prevGymReport = prevWorkouts.getOrDefault(activityType, new GymReport());
                GymReport deltaGymReport = calculateDelta(prevGymReport, currGymReport);
                deltaGymReport.setGymLocation(location);
                deltaGymReport.setWorkoutType(activityType);
                deltaReports.add(deltaGymReport);
            });
        });

        return deltaReports;
    }

    private Map<String, Map<ActivityType, GymReport>> groupReportsByLocationAndWorkoutType(List<Report> reports) {
        return reports.stream()
                .collect(Collectors.groupingBy(Report::getLocation,
                        Collectors.groupingBy(Report::getWorkoutType,
                                Collectors.collectingAndThen(Collectors.toList(), this::aggregate))));
    }

    private GymReport aggregate(List<Report> reports) {
        GymReport result = new GymReport();

        double averageFeedback = reports.stream()
                .flatMap(report -> Optional.ofNullable(report.getFeedbacks()).orElse(Collections.emptyList()).stream())
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0.0);
        result.setAverageFeedback(round(averageFeedback, 2));

        int totalAttendance = reports.stream().mapToInt(report -> Optional.of(report.getAttendanceCount()).orElse(0)).sum();
        int totalCapacity = reports.stream().mapToInt(report -> Optional.of(report.getWorkoutCapacity()).orElse(0)).sum();
        double attendanceRate = (totalCapacity > 0) ? (double) totalAttendance / totalCapacity * 100 : 0.0;

        result.setAttendanceRate(
                this.round( attendanceRate, 2) + "%"
        );
        int minFeedback = reports.stream()
                .flatMap(report -> Optional.ofNullable(report.getFeedbacks()).orElse(Collections.emptyList()).stream())
                .mapToInt(Integer::intValue)
                .min()
                .orElse(0);
        result.setMinimumFeedback(minFeedback);

        result.setWorkoutsLead(reports.stream().mapToInt(report -> Optional.of(report.getWorkoutCount()).orElse(0)).sum());
        return result;
    }

    private GymReport calculateDelta(GymReport previous, GymReport current) {
        double minFeedbackDelta;

        int prevMinFeedback = Optional.of(previous.getMinimumFeedback()).orElse(0);
        int currMinFeedback = Optional.of(current.getMinimumFeedback()).orElse(0);

        if (prevMinFeedback == 0) {
            minFeedbackDelta = currMinFeedback > 0 ? 100.0 : 0.0;
        } else {
            minFeedbackDelta = ((double) (currMinFeedback - prevMinFeedback) / prevMinFeedback) * 100;
        }
        current.setDeltaMinimumFeedback(round(minFeedbackDelta, 2) + "%");

        double prevAttendance = parsePercentage(Optional.ofNullable(previous.getAttendanceRate()).orElse("0%"));
        double currAttendance = parsePercentage(Optional.ofNullable(current.getAttendanceRate()).orElse("0%"));
        double deltaAttendance = currAttendance - prevAttendance;
        current.setAttendanceDelta(round(deltaAttendance, 2) + "%");

        return current;
    }

    private double parsePercentage(String percentage) {
        if (percentage == null || percentage.isEmpty()) {
            return 0;
        }
        return Double.parseDouble(percentage.replace("%", ""));
    }

    private double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException("Decimal places must be non-negative");

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}

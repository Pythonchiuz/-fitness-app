package com.backendfitnessapp.service.impl;

import com.backendfitnessapp.dto.CoachReport;
import com.backendfitnessapp.entity.Report;
import com.backendfitnessapp.service.CoachReportAggregatorService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

public class CoachReportAggregatorServiceImpl implements CoachReportAggregatorService {
    @Override
    public List<CoachReport> processAggregate(List<Report> currentReport, List<Report> previousReport) {
        Map<String, Map<String, CoachReport>> prevAggregated = groupReportsByLocationAndWorkoutType(currentReport);
        Map<String, Map<String, CoachReport>> currAggregated = groupReportsByLocationAndWorkoutType(previousReport);

        List<CoachReport> deltaReports = new ArrayList<>();

        currAggregated.forEach((location, currWorkouts) -> {
            Map<String, CoachReport> prevWorkouts = prevAggregated.getOrDefault(location, new HashMap<>());

            currWorkouts.forEach((email, currCoachReport) -> {
                CoachReport prevCoachReport = prevWorkouts.getOrDefault(email, new CoachReport());
                CoachReport deltaCoachReport = calculateDelta(prevCoachReport, currCoachReport);
                deltaCoachReport.setGymLocation(location);
                deltaCoachReport.setEmail(email);
                deltaReports.add(deltaCoachReport);
            });
        });

        return deltaReports;
    }

    private Map<String, Map<String, CoachReport>> groupReportsByLocationAndWorkoutType(List<Report> reports) {
        return reports.stream()
                .collect(Collectors.groupingBy(Report::getLocation,
                        Collectors.groupingBy(Report::getEmail,
                                Collectors.collectingAndThen(Collectors.toList(), this::aggregate))));
    }

    private CoachReport aggregate(List<Report> reports) {
        CoachReport result = new CoachReport();
        result.setCoach(Optional.ofNullable(reports.get(0).getCoach()).orElse("Unknown Coach"));

        result.setWorkoutsLead(reports.stream().mapToInt(Report::getWorkoutCount).sum());

        double averageFeedback = reports.stream()
                .flatMap(report -> Optional.ofNullable(report.getFeedbacks()).orElse(List.of()).stream())
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0.0);
        result.setAverageFeedback(round(averageFeedback, 2));

        int minFeedback = reports.stream()
                .flatMap(report -> Optional.ofNullable(report.getFeedbacks()).orElse(List.of()).stream())
                .mapToInt(Integer::intValue)
                .min()
                .orElse(0);
        result.setMinimumFeedback(minFeedback);

        result.setWorkoutsLead(reports.stream().mapToInt(Report::getWorkoutCount).sum());
        return result;
    }

    private CoachReport calculateDelta(CoachReport previous, CoachReport current) {
        int prevWorkouts = previous.getWorkoutsLead();
        int currWorkouts = current.getWorkoutsLead();
        int deltaWorkouts = currWorkouts - prevWorkouts;

        double deltaWorkoutsPercentage = (prevWorkouts == 0)
                ? (currWorkouts > 0 ? 100.0 : 0.0)
                : ((double) deltaWorkouts / prevWorkouts) * 100;

        current.setDeltaWorkoutsLead(round(deltaWorkoutsPercentage, 2) + "%");

        int prevMinFeedback = previous.getMinimumFeedback();
        int currMinFeedback = current.getMinimumFeedback();

        double minFeedbackDelta = (prevMinFeedback == 0)
                ? (currMinFeedback > 0 ? 100.0 : 0.0)
                : ((double) (currMinFeedback - prevMinFeedback) / prevMinFeedback) * 100;

        current.setDeltaMinimumFeedback(round(minFeedbackDelta, 2) + "%");

        return current;
    }

    private double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException("Decimal places must be non-negative");

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}

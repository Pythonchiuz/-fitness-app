package com.backendfitnessapp.service.impl;

import com.backendfitnessapp.entity.Report;
import com.backendfitnessapp.service.ReportService;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.util.List;
import java.util.Optional;


public class ReportServiceImpl implements ReportService {
    private final DynamoDbTable<Report> reportTable;

    public ReportServiceImpl() {
        DynamoDbClient dynamoDbClient = DynamoDbClient.builder().region(Region.of(System.getenv("REGION"))).build();
        DynamoDbEnhancedClient enhancedClient = DynamoDbEnhancedClient.builder().dynamoDbClient(dynamoDbClient).build();
        this.reportTable = enhancedClient.table(System.getenv("REPORT_TABLE"), TableSchema.fromBean(Report.class));
    }

    @Override
    public void saveReport(Report newReport) {
        reportTable.putItem(newReport);
    }

    @Override
    public Optional<Report> getReportByWorkoutId(String workoutId) {
        return reportTable.scan().items().stream().filter(report -> report.getWorkoutId().equals(workoutId)).findFirst();
    }

    @Override
    public List<Report> getAllReports() {
        return reportTable.scan().items().stream().toList();
    }

    @Override
    public void updateReport(Report report) {
        reportTable.updateItem(report);
    }
}

package com.backendfitnessapp;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.backendfitnessapp.dto.CoachReport;
import com.backendfitnessapp.dto.GymReport;
import com.backendfitnessapp.entity.Report;
import com.backendfitnessapp.service.AmazonSESService;
import com.backendfitnessapp.service.CoachReportAggregatorService;
import com.backendfitnessapp.service.GymReportAggregatorService;
import com.backendfitnessapp.service.ReportService;
import com.google.gson.Gson;
import com.syndicate.deployment.annotations.environment.EnvironmentVariable;
import com.syndicate.deployment.annotations.environment.EnvironmentVariables;
import com.syndicate.deployment.annotations.events.EventBridgeRuleSource;
import com.syndicate.deployment.annotations.events.EventBridgeRules;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.resources.DependsOn;
import com.syndicate.deployment.model.DeploymentRuntime;
import com.syndicate.deployment.model.ResourceType;
import com.syndicate.deployment.model.RetentionSetting;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import javax.mail.MessagingException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;

@LambdaHandler(
		lambdaName = "reports_sender",
		roleName = "reports_sender-role",
		runtime = DeploymentRuntime.JAVA17,
		isPublishVersion = true,
		aliasName = "${lambdas_alias_name}",
		logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@EnvironmentVariables(value = {
		@EnvironmentVariable(key = "REPORT_TABLE", value = "${report_table}"),
		@EnvironmentVariable(key = "REGION", value = "${region}"),
		@EnvironmentVariable(key = "EVENT_BRIDGE", value = "${event_bridge_rule}")
})
@EventBridgeRules(value = {
		@EventBridgeRuleSource(targetRule = "${event_bridge_rule}"),
})
@DependsOn(name = "${event_bridge_rule}",
		resourceType = ResourceType.EVENTBRIDGE_RULE
)
public class ReportsSender implements RequestHandler<Object, String> {
	private final MyApplication application = DaggerMyApplication.create();
	private final Gson gson = application.getGson();
	private final ReportService reportService = application.getReportService();
	private final GymReportAggregatorService gymAggregator = application.getGymReportAggregatorService();
	private final CoachReportAggregatorService coachAggregator = application.getCoachReportAggregatorService();
	private final AmazonSESService sesService = application.getAmazonSESService();
	private String gymReportFilePath;
	private String coachReportFilePath;

	public String handleRequest(Object request, Context context) {
		try {
			generateReport(context);
		} catch (Exception e) {
			context.getLogger().log(e.getMessage());
			return e.getMessage();
		}
		return "success";
	}

	private void generateReport(Context context) throws IOException, MessagingException {
		LocalDate reportEndDate = LocalDate.now();
		LocalDate reportStartDate = reportEndDate.minusWeeks(1);

		LocalDate previousWeekEnd = reportStartDate.minusDays(1);
		LocalDate previousWeekStart = previousWeekEnd.minusWeeks(1);

		List<Report> reports = reportService.getAllReports().stream().filter(report -> {
			LocalDate reportDate = LocalDate.parse(report.getWorkoutDate());
			return !reportDate.isBefore(reportStartDate) && !reportDate.isAfter(reportEndDate);
		}).toList();

		List<Report> previousWeekReports = reportService.getAllReports().stream()
				.filter(report -> {
					LocalDate reportDate = LocalDate.parse(report.getWorkoutDate());
					return !reportDate.isBefore(previousWeekStart) && !reportDate.isAfter(previousWeekEnd);
				}).toList();

		String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

		previousWeekReports.forEach(report -> {
			context.getLogger().log(report.toString());
		});
		reports.forEach(report -> {
			context.getLogger().log(report.toString());
		});

		salesStatistics(gymAggregator.processAggregate(reports, previousWeekReports), timestamp, reportStartDate, reportEndDate);
		coachesPerformance(coachAggregator.processAggregate(reports, previousWeekReports), timestamp, reportStartDate, reportEndDate);

		File file = new File(gymReportFilePath);
		context.getLogger().log(file.exists() + " " + file.getName());
		sesService.sendMessage(gymReportFilePath, coachReportFilePath, context);
	}

	private void coachesPerformance(List<CoachReport> reports, String timestamp, LocalDate startDate, LocalDate endDate) {
		gymReportFilePath = "/tmp/coach_report_" + timestamp+ ".csv";

		try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(gymReportFilePath), StandardCharsets.UTF_8);
			 CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(
					 "Gym locations","Coach name","Coach e-mail","Report period start","Report period end",
					 "Workouts lead by coach","Delta of Coach workouts lead to previous period in %",
					 "Average Feedback per Coach (1 to 5)","Minimum Feedback for Coach (1 to 5)",
					 "Delta of Minimum Feedback per Coach to previous period in %"))) {
			for (CoachReport report : reports) {
				csvPrinter.printRecord(
						report.getGymLocation(),
						report.getCoach(),
						report.getEmail(),
						startDate.toString(),
						endDate.toString(),
						report.getWorkoutsLead(),
						report.getDeltaWorkoutsLead(),
						report.getAverageFeedback(),
						report.getMinimumFeedback(),
						report.getDeltaMinimumFeedback()
				);
			}

		} catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
	private void salesStatistics(List<GymReport> reports, String timestamp, LocalDate startDate, LocalDate endDate) {
		coachReportFilePath = "/tmp/gym_report_" + timestamp + ".csv";

		try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(coachReportFilePath), StandardCharsets.UTF_8);
			 CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(
					 "Gym locations", "Workout type", "Report period start", "Report period end",
					 "Workouts lead within reporting period", "Clients' attendance rate % (to estimated quantity)",
					 "Delta of clients attendance to previous period in %", "Average Feedback (1 to 5)",
					 "Minimum Feedback (1 to 5)", "Delta of Minimum Feedback to previous period in %"))) {

			for (GymReport report : reports) {
				csvPrinter.printRecord(
						report.getGymLocation(),
						report.getWorkoutType(),
						startDate.toString(),
						endDate.toString(),
						report.getWorkoutsLead(),
						report.getAttendanceRate(),
						report.getAttendanceDelta(),
						report.getAverageFeedback(),
						report.getMinimumFeedback(),
						report.getDeltaMinimumFeedback()
				);
		}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}

package com.backendfitnessapp;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.backendfitnessapp.dto.SqsReportBody;
import com.backendfitnessapp.entity.Report;
import com.backendfitnessapp.service.ReportService;
import com.google.gson.Gson;
import com.syndicate.deployment.annotations.environment.EnvironmentVariable;
import com.syndicate.deployment.annotations.environment.EnvironmentVariables;
import com.syndicate.deployment.annotations.events.SqsTriggerEventSource;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.resources.DependsOn;
import com.syndicate.deployment.model.DeploymentRuntime;
import com.syndicate.deployment.model.ResourceType;
import com.syndicate.deployment.model.RetentionSetting;

import java.time.LocalDate;
import java.util.UUID;

@LambdaHandler(
		lambdaName = "reports_handler",
		roleName = "reports_handler-role",
		runtime = DeploymentRuntime.JAVA17,
		isPublishVersion = true,
		aliasName = "${lambdas_alias_name}",
		logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@SqsTriggerEventSource(
		targetQueue = "fitness_queue",
		batchSize = 10
)
@DependsOn(name = "fitness_queue",
		resourceType = ResourceType.SQS_QUEUE
)
@EnvironmentVariables(value = {
		@EnvironmentVariable(key = "REPORT_TABLE", value = "${report_table}"),
		@EnvironmentVariable(key = "REGION", value = "${region}"),
})
public class ReportsHandler implements RequestHandler<SQSEvent, String> {

	private final MyApplication application = DaggerMyApplication.create();
	private final Gson gson = application.getGson();
	private final ReportService reportService = application.getReportService();
	public String handleRequest(SQSEvent request, Context context) {
		for (SQSEvent.SQSMessage message : request.getRecords()) {
			context.getLogger().log(message.getBody());
			processReport(gson.fromJson(message.getBody(), SqsReportBody.class));
		}
		return "resultMap";
	}

	private void processReport(SqsReportBody sqsReport) {
		Report report = reportService.getReportByWorkoutId(sqsReport.getWorkoutId()).orElse(null);
		if (report == null) {
			report = new Report();
			report.setReportId(UUID.randomUUID().toString());
			report.setReportDate(LocalDate.now().toString());
			report.setCoach(sqsReport.getCoach());
			report.setEmail(sqsReport.getEmail());
			report.setWorkoutId(sqsReport.getWorkoutId());
			report.setWorkoutType(sqsReport.getWorkoutType());
			report.setWorkoutDate(sqsReport.getWorkoutDate());
			report.setWorkoutCount(sqsReport.getWorkoutCount());
			report.setWorkoutCapacity(sqsReport.getWorkoutCapacity());
			report.setAttendanceCount(sqsReport.getAttendanceCount());
			report.setLocation(sqsReport.getLocation());
			if (sqsReport.getFeedbackRating() != null) {
				report.setFeedback(sqsReport.getFeedbackRating());
			}
			reportService.saveReport(report);
		} else {
			report.setReportDate(LocalDate.now().toString());
			if (sqsReport.getFeedbackRating() != null) {
				report.setFeedback(sqsReport.getFeedbackRating());
			}
			reportService.updateReport(report);
		}
	}
}

package com.backendfitnessapp;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.backendfitnessapp.handler.EndpointHandler;
import com.syndicate.deployment.annotations.environment.EnvironmentVariable;
import com.syndicate.deployment.annotations.environment.EnvironmentVariables;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.resources.DependsOn;
import com.syndicate.deployment.model.DeploymentRuntime;
import com.syndicate.deployment.model.ResourceType;
import com.syndicate.deployment.model.RetentionSetting;
import com.syndicate.deployment.model.environment.ValueTransformer;

import java.util.HashMap;
import java.util.Map;

@LambdaHandler(
    lambdaName = "fitness_handler",
	roleName = "fitness_handler-role",
	isPublishVersion = true,
	runtime = DeploymentRuntime.JAVA17,
	aliasName = "${lambdas_alias_name}",
	logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@DependsOn(resourceType = ResourceType.COGNITO_USER_POOL, name = "${cognito_pool}")
@DependsOn(resourceType = ResourceType.DYNAMODB_TABLE, name = "${user_table}")
@DependsOn(resourceType = ResourceType.DYNAMODB_TABLE, name = "${feedback_table}")
@DependsOn(resourceType = ResourceType.DYNAMODB_TABLE, name = "${workout_table}")
@DependsOn(name = "fitness_queue", resourceType = ResourceType.SQS_QUEUE)
@EnvironmentVariables(value = {
		@EnvironmentVariable(key = "REGION", value = "${region}"),
		@EnvironmentVariable(key = "USER_TABLE", value = "${user_table}"),
		@EnvironmentVariable(key = "FEEDBACK_TABLE", value = "${feedback_table}"),
		@EnvironmentVariable(key = "WORKOUT_TABLE", value = "${workout_table}"),
		@EnvironmentVariable(key = "COGNITO_ID", value = "${cognito_pool}", valueTransformer = ValueTransformer.USER_POOL_NAME_TO_USER_POOL_ID),
		@EnvironmentVariable(key = "CLIENT_ID", value = "${cognito_pool}", valueTransformer = ValueTransformer.USER_POOL_NAME_TO_CLIENT_ID),
		@EnvironmentVariable(key = "QUEUE_NAME", value = "${fitness_queue}"),
		@EnvironmentVariable(key = "REPORT_TABLE", value = "${report_table}")
})
public class FitnessHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

	private final MyApplication application = DaggerMyApplication.create();
	private final EndpointHandler generalHandler = application.getGeneralApiHandler();
	private final Map<String, String> corsHeaders = application.getCorsHeaders();

	public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
		return generalHandler.handle(request, context)
				.withHeaders(corsHeaders);
	}
}

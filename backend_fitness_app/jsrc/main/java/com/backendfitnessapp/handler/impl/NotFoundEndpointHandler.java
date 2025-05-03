package com.backendfitnessapp.handler.impl;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.backendfitnessapp.handler.EndpointHandler;


public class NotFoundEndpointHandler implements EndpointHandler {
    @Override
    public APIGatewayProxyResponseEvent handle(APIGatewayProxyRequestEvent requestEvent, Context context) {
        String methodName = requestEvent.getHttpMethod();
        String path = requestEvent.getPath();
        APIGatewayProxyResponseEvent responseEvent = new APIGatewayProxyResponseEvent();
        responseEvent.setStatusCode(404);
        responseEvent.setBody("Handler with method: " + methodName + " and path: " + path + " not Found");
        return responseEvent;
    }
}

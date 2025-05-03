package com.backendfitnessapp.handler.impl;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.backendfitnessapp.handler.EndpointHandler;

import java.util.Map;
import java.util.regex.Pattern;

public class GeneralEndpointHandler implements EndpointHandler {
    private final EndpointHandler notFoundEndpointHandler;
    private final Map<String, EndpointHandler> endpointHandlerMap;

    public GeneralEndpointHandler(Map<String, EndpointHandler> endpointHandlers, EndpointHandler notFoundEndpointHandler) {
        this.endpointHandlerMap = endpointHandlers;
        this.notFoundEndpointHandler = notFoundEndpointHandler;
    }

    public APIGatewayProxyResponseEvent handle(APIGatewayProxyRequestEvent requestEvent, Context context) {
        String var10000 = requestEvent.getHttpMethod();
        String key = var10000 + ":" + requestEvent.getPath();

        for(Map.Entry<String, EndpointHandler> entry : this.endpointHandlerMap.entrySet()) {
            if (Pattern.matches(entry.getKey(), key)) {
                return (entry.getValue()).handle(requestEvent, context);
            }
        }

        return this.notFoundEndpointHandler.handle(requestEvent, context);
    }
}

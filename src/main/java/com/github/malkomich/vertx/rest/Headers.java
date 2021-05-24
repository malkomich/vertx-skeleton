package com.github.malkomich.vertx.rest;

import com.google.common.net.HttpHeaders;

public class Headers {

    private Headers() {}

    public static final String ACCEPT = HttpHeaders.ACCEPT;
    public static final String CONTENT_TYPE = HttpHeaders.CONTENT_TYPE;

    public static final String FORMAT = "application/json";
}

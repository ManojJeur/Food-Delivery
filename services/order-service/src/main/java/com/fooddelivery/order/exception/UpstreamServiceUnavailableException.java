package com.fooddelivery.order.exception;
public class UpstreamServiceUnavailableException extends RuntimeException { public UpstreamServiceUnavailableException(String service) { super(service + " is temporarily unavailable"); } }

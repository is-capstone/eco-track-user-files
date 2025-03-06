package com.enzulode.file.event;

public record FileFailedEvent(String objName, String onBehalfOf, String reason) {}

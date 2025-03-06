package com.enzulode.file.event;

import org.springframework.web.bind.annotation.RequestMethod;

import java.time.LocalDateTime;

public record ApiUsageStatisticsEvent(
    String endpoint,
    RequestMethod method,
    LocalDateTime at,
    String usedBy
) {}

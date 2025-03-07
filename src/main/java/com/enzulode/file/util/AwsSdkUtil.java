package com.enzulode.file.util;

import software.amazon.awssdk.core.SdkResponse;

import java.text.MessageFormat;

public class AwsSdkUtil {
  public static void checkSdkResponse(SdkResponse sdkResponse) {
    if (AwsSdkUtil.isErrorSdkHttpResponse(sdkResponse)) {
      throw new RuntimeException(MessageFormat.format("{0} - {1}", sdkResponse.sdkHttpResponse().statusCode(), sdkResponse.sdkHttpResponse().statusText()));
    }
  }

  public static boolean isErrorSdkHttpResponse(SdkResponse response) {
    return response.sdkHttpResponse() == null || !response.sdkHttpResponse().isSuccessful();
  }
}

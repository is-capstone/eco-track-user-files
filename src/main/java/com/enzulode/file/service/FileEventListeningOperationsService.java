package com.enzulode.file.service;

public interface FileEventListeningOperationsService {

  void fileFailedEvent(String objName, String onBehalfOf);
  void fileSucceedEvent(String objName, String onBehalfOf);
}

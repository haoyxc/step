package com.google.sps.data;

import com.google.auto.value.AutoValue;
import javax.annotation.Nullable;

@AutoValue
public abstract class ServerResponse {
  public static ServerResponse create(@Nullable String email, String url) {
    return new AutoValue_ServerResponse(email, url);
  }
  
  @Nullable abstract String email();
  abstract String url();
}
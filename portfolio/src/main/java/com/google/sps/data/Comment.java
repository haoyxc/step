package com.google.sps.data;

public class Comment {
  public String content;
  public String name;
  public String email;
  public Comment(String content, String name, String email) {
    this.content = content;
    this.name = name;
    this.email = email;
  }
}
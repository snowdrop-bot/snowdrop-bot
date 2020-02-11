package io.snowdrop;

public class Status {

  private String category;
  private double progress;
  private String message;

  public Status() {

  }

  public Status(String category, double progress, String message) {
    this.category = category;
    this.progress = progress;
    this.message = message;
  }

  public String getCategory() {
    return category;
  }

  public double getProgress() {
    return progress;
  }

  public String getMessage() {
    return message;
  }

  public String toJson() {
    return "{category:" + category + ", message:" + message + ", progress:" + progress + "}";
  }

}

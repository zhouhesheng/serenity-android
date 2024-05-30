package us.nineworlds.serenity.common.rest.impl;

import us.nineworlds.serenity.common.Server;

public class SerenityUser implements us.nineworlds.serenity.common.rest.SerenityUser {

  private final String userName;
  private final String userId;
  private final String password;
  private final Server serverInfo;
  private final String accessToken;

  private SerenityUser(Builder builder) {
    this.userName = builder.userName;
    this.userId = builder.userId;
    this.password = builder.password;
    this.serverInfo = builder.serverInfo;
    this.accessToken = builder.accessToken;
  }

  public static Builder builder() {
    return new Builder();
  }

  @Override public String getUserName() {
    return userName;
  }

  @Override public String getUserId() {
    return userId;
  }

  @Override public String getAccessToken() {
    return accessToken;
  }

  @Override public String password() {
    return password;
  }

  @Override public Server getUserServer() {
    return serverInfo;
  }

  public static class Builder {
    private String userName;
    private String userId;
    private String password;
    private Server serverInfo;
    private String accessToken;

    public Builder userName(String userName) {
      this.userName = userName;
      return this;
    }

    public Builder userId(String userId) {
      this.userId = userId;
      return this;
    }

    public Builder password(String password) {
      this.password = password;
      return this;
    }

    public Builder serverInfo(Server serverInfo) {
      this.serverInfo = serverInfo;
      return this;
    }

    public Builder accessToken(String accessToken) {
      this.accessToken = accessToken;
      return this;
    }

    public SerenityUser build() {
      return new SerenityUser(this);
    }
  }
}

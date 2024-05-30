package us.nineworlds.serenity.common.rest;

import us.nineworlds.serenity.common.Server;

public interface SerenityUser {

  String getUserName();

  String getUserId();

  String getAccessToken();

  String password();

  Server getUserServer();

}

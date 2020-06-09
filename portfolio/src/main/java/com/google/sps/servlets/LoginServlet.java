package com.google.sps.servlets;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.Gson;
import com.google.sps.data.ServerResponse;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
  /**
   * returns login status: either null if not logged in or a string with their email
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Gson gson = new Gson();
    UserService userService = UserServiceFactory.getUserService();
    ServerResponse serverResp;
    String redirectUrl = "/";
    String userEmail;
    String url;
    Boolean loggedIn; 
    if (userService.isUserLoggedIn()) {
      userEmail = userService.getCurrentUser().getEmail();
      url = userService.createLogoutURL(redirectUrl);
    } else {
      userEmail = null;
      url = userService.createLoginURL(redirectUrl);
    }
    serverResp = ServerResponse.create(userEmail, url);
    response.setContentType("application/json");
    response.getWriter().println(gson.toJson(serverResp));
  }
}
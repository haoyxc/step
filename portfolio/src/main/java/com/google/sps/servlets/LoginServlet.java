package com.google.sps.servlets;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.Gson;
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
    String jsonObj= "";
    UserService userService = UserServiceFactory.getUserService();
    if (userService.isUserLoggedIn()) {
      String userEmail = userService.getCurrentUser().getEmail();
      String urlToRedirectToAfterUserLogsOut = "/";
      String logoutUrl = userService.createLogoutURL(urlToRedirectToAfterUserLogsOut);
      jsonObj = "{";
      jsonObj += "\"email\": ";
      jsonObj += "\"" + userEmail + "\"";
      jsonObj += ", ";
      jsonObj += "\"url\": ";
      jsonObj += "\"" + logoutUrl + "\"";
      jsonObj += "}";
    } else {
      String urlToRedirectToAfterUserLogsIn = "/";
      String loginUrl = userService.createLoginURL(urlToRedirectToAfterUserLogsIn);
      jsonObj = "{";
      jsonObj += "\"email\": ";
      jsonObj += null;
      jsonObj += ", ";
      jsonObj += "\"url\": ";
      jsonObj += "\"" + loginUrl + "\"";
      jsonObj += "}";
      System.out.println(jsonObj); 
    }
    response.setContentType("application/json");
    response.getWriter().println(jsonObj);
  }
}
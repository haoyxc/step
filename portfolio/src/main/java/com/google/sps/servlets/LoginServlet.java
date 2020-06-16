// Copyright 2020 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

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
  /** returns login status: either null if not logged in or a string with their email */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Gson gson = new Gson();
    UserService userService = UserServiceFactory.getUserService();
    String redirectUrl = "/";
    String userEmail;
    String url;
    if (userService.isUserLoggedIn()) {
      userEmail = userService.getCurrentUser().getEmail();
      url = userService.createLogoutURL(redirectUrl);
    } else {
      userEmail = null;
      url = userService.createLoginURL(redirectUrl);
    }
    ServerResponse serverResp = serverResp = ServerResponse.create(userEmail, url);
    response.setContentType("application/json");
    response.getWriter().println(gson.toJson(serverResp));
  }
}

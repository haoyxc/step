// Copyright 2019 Google LLC
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

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.FetchOptions.Builder;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.gson.Gson;
import com.google.sps.data.Comment;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.FileReader;

/** Servlet that gets comments and posts comments */
@WebServlet("/data")
public class DataServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {  
    // Gets the latest number of comments posted by the name in the queryParam

    String numParam = request.getParameter("num");
    String queryParam = request.getParameter("query");
    
    if (numParam == null || queryParam == null) {
      response.sendError(400);
      return;
    }

    int numParsed = Integer.parseInt(numParam); // the number of comments the user wants

    Query query = new Query("Comment").addSort("timestamp", SortDirection.DESCENDING);
    
    if (!queryParam.isEmpty()) {
      query.addFilter("name", Query.FilterOperator.EQUAL, queryParam); //filter by name of commenter
    }
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    PreparedQuery results = datastore.prepare(query);

    Gson gson = new Gson();
    String json = gson.toJson(results.asList(FetchOptions.Builder.withLimit(numParsed))); 

    response.setContentType("application/json;");
    response.getWriter().println(json);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String body = "";
    try (BufferedReader reader = request.getReader()) {
      if (reader == null) {
            return;
      }
      String line;
      while ((line = reader.readLine()) != null) {
        body = line; 
      }
    } catch (Exception e) {
      System.out.println("oh no");
    }
    System.out.println(body);
    Gson g = new Gson();
    Comment c = g.fromJson(body, Comment.class);

    String text = c.content;
    String name = c.name;
    String email = c.email; 

    if (text == null || name == null) {
      response.sendError(400);
      return;
    }

    long timestamp = System.currentTimeMillis();
    
    Entity commentEntity = new Entity("Comment");

    commentEntity.setProperty("content", text); 
    commentEntity.setProperty("name", name); 
    commentEntity.setProperty("email", email); 
    commentEntity.setProperty("timestamp", timestamp);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(commentEntity);   
    response.sendRedirect("/"); 
  }
}

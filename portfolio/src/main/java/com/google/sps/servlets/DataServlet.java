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
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import java.io.IOException;
import com.google.gson.Gson;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List; 
import java.util.ArrayList; 

/** Servlet that returns some example content. TODO: modify this file to handle comments data */
@WebServlet("/data")
public class DataServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    List<Comment> data = new ArrayList<>(); 
    
    Query query = new Query("Comment").addSort("timestamp", SortDirection.DESCENDING);
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    PreparedQuery results = datastore.prepare(query);
    for (Entity entity : results.asIterable()) {
      long id = entity.getKey().getId();
      String content = (String) entity.getProperty("content");
      String name = (String) entity.getProperty("name");
      long timestamp = (long) entity.getProperty("timestamp");

      Comment comment = new Comment(id, content, name, timestamp);
      data.add(comment);
    }

    Gson gson = new Gson();
    String json = gson.toJson(data); 

    response.setContentType("application/json;");
    response.getWriter().println(json);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String text = request.getParameter("form-comment"); 
    String name = request.getParameter("form-name"); 

    if (text == null || name == null) {
      response.sendError(400);
      return;
    }

    long timestamp = System.currentTimeMillis();
    
    Entity commentEntity = new Entity("Comment");

    commentEntity.setProperty("content", text); 
    commentEntity.setProperty("name", name); 
    commentEntity.setProperty("timestamp", timestamp);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(commentEntity);   

    response.sendRedirect("/"); 
  }

  /** Inner class that stores a comment object
   * id is the Datastore generated id, content is the content of the comment,
   * name is the name of the user who made the comment, 
   * and timestamp is the time the comment was made (as a long)
   */
  class Comment {
    private final long id;
    private final String content;
    private final String name;
    private final long timestamp;
    
    public Comment(long id, String content, String name, long timestamp) {
      this.id = id;
      this.content = content;
      this.name = name;
      this.timestamp = timestamp; 
    }
  }
}

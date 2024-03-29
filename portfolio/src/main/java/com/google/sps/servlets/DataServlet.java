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
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.cloud.language.v1.Document;
import com.google.cloud.language.v1.LanguageServiceClient;
import com.google.cloud.language.v1.Sentiment;
import com.google.gson.Gson;
import com.google.sps.data.Comment;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.BufferedReader;

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
      query.addFilter(
          "name", Query.FilterOperator.EQUAL, queryParam); // filter by name of commenter
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
        body += line;
      }
    } catch (Exception e) {
      response.sendError(400);
    }

    Gson g = new Gson();
    Comment c = g.fromJson(body, Comment.class);

    String text = c.getContent();
    String name = c.getName();
    String email = c.getEmail();
    float score = getSentimentScore(text);

    if (text == null || name == null || email == null) {
      response.sendError(400);
      return;
    }

    long timestamp = System.currentTimeMillis();

    Entity commentEntity = new Entity("Comment");

    commentEntity.setProperty("content", text);
    commentEntity.setProperty("name", name);
    commentEntity.setProperty("email", email);
    commentEntity.setProperty("timestamp", timestamp);
    commentEntity.setProperty("sentimentScore", score);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(commentEntity);
    response.sendRedirect("/");
  }

  /**
   * Gets the sentiment score of a message
   *
   * @param message: the message to get the score of
   * @return the score between -1 and 1 of the message
   */
  private float getSentimentScore(String message) throws IOException {
    Document doc =
        Document.newBuilder().setContent(message).setType(Document.Type.PLAIN_TEXT).build();
    LanguageServiceClient languageService = LanguageServiceClient.create();
    Sentiment sentiment = languageService.analyzeSentiment(doc).getDocumentSentiment();
    float score = sentiment.getScore();
    languageService.close();
    return score;
  }
}

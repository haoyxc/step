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

package com.google.sps.data;

/** 
 * Class that stores the components of a Comment item, reflects the schema of the Datastore.
 * Used in conjunction with gson in DataServlet.
 * content is the text of the commnent, name is the name of the commenter, and email is email of the commenter. 
 */
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
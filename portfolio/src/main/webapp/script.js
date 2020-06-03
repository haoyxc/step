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

document.addEventListener("DOMContentLoaded", loadSkills);

/**
 * Makes the clicked header link have the class "active" and removes the "active" class from the other header links
 */
function setActiveTab(btn) {
  //Gets all tabs with the class name "active" (there should only be 1)
  const activeTabs = document.getElementsByClassName('active');

  //Remove the "active" class from that element
  activeTabs[0].classList.remove('active'); 

  //Add "active" class to the clicked element
  btn.classList.add('active'); 
};

/**
 * Load skills bar in skills container
 */
function loadSkills () {
  $(".skill-per").each(function() {
    let $this = $(this);
    let per = $this.attr("per");
    $this.css("width", per + "%");
    $({ animatedValue: 0 }).animate(
      { animatedValue: per },
      {
        duration: 7500,
        step: function() {
          $this.attr("per", Math.floor(this.animatedValue) + "%");
        },
        complete: function() {
          $this.attr("per", Math.floor(this.animatedValue) + "%");
        }
      }
    );
  });
};

/**
 * Makes a request to /data and gets the response
 */
async function fetchFromData() {
  const selector = $("#num-comments")[0]; //get the select element
  const selectedNum = selector.options[selector.selectedIndex].value; //number of comments user wants

  //make request with the number specified
  const response = await fetch(`/data?num=${selectedNum}`);
  
  // This gives a list of comments
  const comments = await response.json();
  console.log(comments);
  const numComments = comments.length;

  const root = $("#all-messages");

  // Adds all of the messages to a div
  comments.forEach(c => {
    root.appendChild(createCommentElement(c.propertyMap)); 
  }); 

  $("#get-msg-btn").addClass("invisible"); 
  $("#hide-msg-btn").removeClass("invisible"); 
};

/**
 * Creates a comment element with the content, name of commenter, and date
 */
function createCommentElement(comment) {
  const node = $("<div></div>");
  node.addClass("msg");

  const contentElement = $("<span></span>");
  contentElement.addClass("comment-content");
  contentElement.text(comment.content);

  const nameElement = $("<p></p>");
  nameElement.addClass("comment-name");
  const dateReadable = (new Date(comment.timestamp)).toDateString();
  nameElement.text(`${comment.name} at ${dateReadable}`);

  node.append(contentElement);
  node.append(nameElement);
  return node;
}

/**
 * Hides the comments when displayed
 */
function hideComments() {
  // Clear the child elements from comments div
  $("#all-messages").empty(); 
  $("#get-msg-btn").removeClass("invisible"); 
  $("#hide-msg-btn").addClass("invisible");
 };

/**
 * Sets the submit button to disabled if input is empty, enabled otherwise
 */
function configureBtn() {
  if ($("#form-comment-input").val() === "" || $("#form-name-input").val() === "" ) {
    $("#form-submit-btn").prop("disabled", true); 
  } else {
    $("#form-submit-btn").prop("disabled", false); 
  }; 
}; 

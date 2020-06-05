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
 * Function gets called with click of "Show Messages" button
 */
async function onShowBtnClick () {
  await fetchFromData();
  replaceShowWithHideBtn();
}

/**
 * Makes a request to /data and gets the response
 * if stay is true, then the buttons won't update
 */
async function fetchFromData() {
  const selectedNum = $("#num-comments").val(); //the number of comments the user wants
  const query = $("#comment-query-input").val(); //name of user to look for

  //make request with the number specified
  const url = `/data?num=${selectedNum}&query=${query}`;
  const response = await fetch(url);
  
  // This gives a list of comments
  const comments = await response.json();

  const root = $("#all-messages");

  // Tells the user if there's no comments
  if (comments.length === 0) {
    const node = $("<div></div>");
    node.addClass("msg");
    const contentElement = $("<span></span>");
    contentElement.text("Sorry! Nothing to show here");
    node.append(contentElement);
    root.append(node);
  }

  // Adds all of the messages to a div
  comments.forEach(c => {
    root.append(createCommentElement(c.propertyMap, c.key.id)); 
  }); 
};

/** 
 * Replace "Show Messages" button with "Hide Messages" button
 */
function replaceShowWithHideBtn() {
  $("#get-msg-btn").addClass("invisible"); 
  $("#hide-msg-btn").removeClass("invisible"); 
}

/**
 * Creates a comment element with the content, name of commenter, and date
 * as well as a button for user to remove the comment. This is intended for if the 
 * number of comments or name queried changes, and not when the "Show" button is clicked.
 */
function createCommentElement(comment, id) {
  const node = $("<div></div>");
  node.addClass("msg");

  const contentElement = $("<span></span>");
  contentElement.addClass("comment-content");
  contentElement.text(comment.content);

  const nameElement = $("<p></p>");
  nameElement.addClass("comment-name");
  const dateReadable = (new Date(comment.timestamp)).toDateString();
  nameElement.text(`${comment.name} at ${dateReadable}`);

  const deleteBtn = $("<button></button>");
  deleteBtn.text("Delete");
  deleteBtn.on("click", async () => {
    await deleteComment(id);
  })

  node.append(contentElement);
  node.append(nameElement);
  node.append(deleteBtn);
  return node;
}

/**
 * Delete a comment from the Datastore given the id. Alerts the user if something goes wrong and comment cannot be deleted.
 */
async function deleteComment(id) {
  const params = new URLSearchParams();
  params.append('id', id);
  const resp = await fetch('/delete-data', {method: 'POST', body: params});
  if (!resp.ok) {
    alert("AH YIKES! Cannot delete comment");
    return;
  }

  //empty the messages and refetch so the page updates accordingly
  $("#all-messages").empty(); 
  await fetchFromData();
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

/**
 * Toggles image display and the direction of the arrow
 */
function toggleImgs(element, containerName) {
  $(`#${containerName}`).toggleClass("invisible");
  $(element).hasClass("fa-sort-down") ? 
    $(element).addClass("fa-sort-up").removeClass("fa-sort-down") : 
    $(element).addClass("fa-sort-down").removeClass("fa-sort-up");
}

/**
 * Updates the message display based on query parameters if already displayed
 */
function correctDisplay() {
  //only relevant if messages are already showing
  if ($("#get-msg-btn").hasClass("invisible")) {
    $("#all-messages").empty(); 
    fetchFromData();
  }
}

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
document.addEventListener("DOMContentLoaded", checkLogin);
let globalEmail = null;

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

  const email = globalEmail;

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
    root.append(createCommentElement(c.propertyMap, c.key.id, email)); 
  }); 
};

/**
 * Handles the form on submit, send data from form as body to server
 */
async function onSubmitForm() {
  const content = $("#form-comment-input").val(); 
  const name = $("#form-name-input").val();

  // a null email is handled in the server and will send a 404
  const email =  globalEmail;
  
  const resp = await fetch("/data", {
    method: 'POST', 
    headers: {
      'Content-Type': 'application/json'
    }, 
    body: JSON.stringify({content, name, email})
  });
}

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
function createCommentElement(comment, id, emailLoggedIn) {
  const node = $("<div></div>");
  node.addClass("msg");

  const contentElement = $("<span></span>");
  contentElement.addClass("comment-content");
  contentElement.text(comment.content);
  node.append(contentElement);

  const nameElement = $("<p></p>");
  nameElement.addClass("comment-name");
  const dateReadable = (new Date(comment.timestamp)).toDateString();
  nameElement.text(`${comment.name} at ${dateReadable}`);
  node.append(nameElement);

  const emailElement = $("<p></p>");
  emailElement.addClass("comment-email");
  emailElement.attr("href", "mailto:" + comment.email).text(comment.email);
  node.append(emailElement);

  const colorClass = getColorClass(comment.sentimentScore);
  // colorClass is undefined if not within -1 and 1
  if (colorClass) {
    contentElement.addClass(colorClass);
  }

  // delete button only present if person logged in is teh one who posted the comment or me!
  const myEmail = "cindyup@gmail.com";
  if (emailLoggedIn !== null && comment.email === emailLoggedIn || comment.email == myEmail) {
    const deleteBtn = $("<button></button>");
    deleteBtn.text("Delete");
    deleteBtn.on("click", async () => {
      await deleteComment(id);
    })
    node.append(deleteBtn);
  };
  return node;
}

/**
 * Gets the color class for css based on the score between -1 and 1. 
 * Called with first loading the individual comments
 */
function getColorClass(score) {
  switch(score >= -1) {
    case (score < -.75):
      return "neg-four";
    case (score < -.5):
      return "neg-three";
    case (score < -.25):
      return "neg-two";
    case (score < 0):
      return "neg-one";
    case (score < .25):
      return "pos-one";
    case (score < .5):
      return "pos-two";
    case (score < .75):
      return "pos-three";
    case (score <= 1):
      return "pos-four";
  }
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

  // empty the messages and refetch so the page updates accordingly
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
 * Checks the login status of the user. Displays the form only if user is logged in
 */
async function checkLogin() { 
  const resp = await fetch('/login');
  const json = await resp.json();

  // if user is logged in, want the top to say logged out
  if (json.email) {
    $("#form-container").removeClass("invisible");
    updateHeaderToLogoutIfLoggedIn(true, json.url);
    globalEmail = json.email;
    return;
  }
  updateHeaderToLogoutIfLoggedIn(false, json.url);
}

/**
 * Updates the message display based on query parameters if already displayed
 */
function correctDisplay() {
  // only relevant if messages are already showing
  if ($("#get-msg-btn").hasClass("invisible")) {
    $("#all-messages").empty(); 
    fetchFromData();
  }
}

/**
 * Takes in a boolean to reflect whether the user is logged in and if so, changes the text to "Logout".
 * Called whenever page loads.
 */
function updateHeaderToLogoutIfLoggedIn(isLoggedIn, url) {
  $("#login-container").attr("href", url);
  
  // True means user is logged in so the text should be set to logged out
  isLoggedIn ? $("#login-container").text("Logout") : $("#login-container").text("Login");
}

/**
 * Create a map centered around Plano, Texas, where I'm from
 * Each marker displays different text on click
 */
function createMap() {
  const locations = [
    ["Trader Joe's", 33.029256, -96.793147], 
    ["Plano West Senior High School", 33.0437, -96.8140], 
    ["Sprouts Farmers Market", 33.069816, -96.773118]
  ];

  const descriptions = {
    "Trader Joe's": "I fell in love with this place after listening to a podcast called \"Should America be Run by...Trader Joe's?\"", 
    "Plano West Senior High School": "I went to high school here! It was a large public high school (class size was 1400) and it was really not great", 
    "Sprouts Farmers Market": "I go here so often for bread, avocados, cheese, etc. Somtimes multiple times just in one day (it's so bad!)"
  };

  const map = new google.maps.Map(
      document.getElementById('map'),
      {center: {lat: 33.07196, lng: -96.771786}, zoom: 10}
  );

  locations.forEach(loc => {
    const marker = new google.maps.Marker({
      position: {lat: loc[1], lng: loc[2]}, 
      map: map,
      title: loc[0],
    });
    marker.addListener('click', () => showMarkerText(marker, descriptions));
  });
}

/**
 * Show a description associated with a marker when a marker is clicked
 * Clears the space first
 */
function showMarkerText(marker, descriptions) {
  if (descriptions[marker.title]) {
    $("#map-location-title").text("");
    $("#map-location-title").text(marker.title);
    $("#map-text").text("");
    $("#map-text").text(descriptions[marker.title]);
  } 
}

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
(function() {

  'use strict';

  // define variables
  var items = document.querySelectorAll(".timeline li");

  // check if an element is in viewport
  // http://stackoverflow.com/questions/123999/how-to-tell-if-a-dom-element-is-visible-in-the-current-viewport
  function isElementInViewport(el) {
    var rect = el.getBoundingClientRect();
    return (
      rect.top >= 0 &&
      rect.left >= 0 &&
      rect.bottom <= (window.innerHeight || document.documentElement.clientHeight) &&
      rect.right <= (window.innerWidth || document.documentElement.clientWidth)
    );
  }

  function callbackFunc() {
    for (var i = 0; i < items.length; i++) {
      if (isElementInViewport(items[i])) {
        items[i].classList.add("in-view");
      }
    }
  }

  // listen for events
  window.addEventListener("load", callbackFunc);
  window.addEventListener("resize", callbackFunc);
  window.addEventListener("scroll", callbackFunc);

})();

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
}

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
}

/**
 * Makes a request to /data and gets the response
 */
 async function fetchFromData() {
  const response = await fetch('/data');
  const quote = await response.text();
  document.getElementById('secret-message').innerText = quote; 
 }

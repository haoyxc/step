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

package com.google.sps;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

import java.util.Collection;

public final class FindMeetingQuery {
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    
    // If there are no events, can return the whole day as a time range
    if (events.size() == 0) {
      return Arrays.asList(TimeRange.WHOLE_DAY);
    }
    Collection<TimeRange> result; 

    // Required attendees from request
    Collection<String> requestAttendees = request.getAttendees();

    // Optional attendees
    Collection<String> requestAttendeesOptional = request.getOptionalAttendees();

    // The whole day is free if there aren't any attendees
    if (requestAttendees.size() == 0 && requestAttendeesOptional.size() == 0) {
      return Arrays.asList(TimeRange.WHOLE_DAY);
    }

    // List of times that cannot be in the returned interval, eventually sorted by start time 
    List<TimeRange> unavailableTimes =  getUnavailableTimes(events, requestAttendees);
    Collections.sort(unavailableTimes, TimeRange.ORDER_BY_START);
    
    // Merge the times 
    List<TimeRange> unavailableTimesMerged = mergeTimeRanges(unavailableTimes);
    List<TimeRange> availableTimes = getAvailableTimes(unavailableTimes, request.getDuration());

    // TODO: FIX THIS MESS
    return availableTimes;
  }

  private List<TimeRange> mergeTimeRanges(List<TimeRange> rangeList) {
    ArrayList<TimeRange> mergedList = new ArrayList<>();
    for (TimeRange currTimeRange : rangeList) {
      // Can simply add to list if there's no overlap or if mergedList is empty
      if (mergedList.isEmpty() || !currTimeRange.overlaps(mergedList.get(mergedList.size() - 1))) {
        mergedList.add(currTimeRange);
      } else {
        TimeRange lastTimeRange = mergedList.get(mergedList.size() -1);

        // Want to adjust start and end accordingly
        int modifiedStart = Math.min(lastTimeRange.start(), currTimeRange.start());
        int modifiedEnd = Math.max(lastTimeRange.start(), currTimeRange.start());

        // TODO: figure out boolean
        TimeRange mergedRange = TimeRange.fromStartEnd(modifiedStart, modifiedEnd, false);
        mergedList.remove(lastTimeRange);
        mergedList.add(mergedRange);
      }
    }
    return mergedList;
  }
	
  /**
   * Gets all the times where attendees are busy
   *
   * @param events the list of events to process, request
   * @param requestAttendees the attendees that are relevant
   * @return a list of time ranges that are from occupied people
   */
  private List<TimeRange> getUnavailableTimes (Collection<Event> events, Collection<String> requestAttendees) {
    List<TimeRange> timeRangeList = new ArrayList<>();
    for (Event e : events) {
      Set<String> eventAttendees = e.getAttendees();

      for (String attendee : eventAttendees) {
        if (requestAttendees.contains(attendee)) {
          // This event is actually relevant

          timeRangeList.add(e.getWhen());

          break; // Need only one relevant attendee for the event to be valid
        }
      }
    }
    return timeRangeList;   
  }

  private List<TimeRange> getAvailableTimes(List<TimeRange> unavailableTimesMerged, long requestDuration) {
    List<TimeRange> availableTimes = new ArrayList<>();
    int prevEnd = TimeRange.START_OF_DAY;
   	for (TimeRange currTimeRange : unavailableTimesMerged) {
       // TODO: figure out boolean
       TimeRange freeTimeRange = TimeRange.fromStartEnd(prevEnd, currTimeRange.start(), false);
       if (freeTimeRange.duration() >= requestDuration) {
         availableTimes.add(freeTimeRange);
       }
       prevEnd = currTimeRange.end();
     }

     TimeRange lastRange = TimeRange.fromStartEnd(prevEnd, TimeRange.END_OF_DAY, true);
     if (lastRange.duration() >= requestDuration) {
         availableTimes.add(lastRange);
       }
       return availableTimes;
    
  }

}

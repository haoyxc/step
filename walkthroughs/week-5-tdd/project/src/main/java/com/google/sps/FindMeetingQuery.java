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
    // General outline:
    // Consider mandatory and optional employees separately. For each: 
    // 	1. add all the meetings times to a list
    // 	2. merge overlapping times in the list
    // 	3. find available times based on the unavailable times
    
    // Edge cases: duration is longer than a day or events is empty
    if (request.getDuration() >= TimeRange.WHOLE_DAY.duration()) {
      return Arrays.asList();
    } else if (events.isEmpty()) {
      return Arrays.asList(TimeRange.WHOLE_DAY);
    } 

    Collection<String> requestAttendees = request.getAttendees();
    Collection<String> requestAttendeesOptional = request.getOptionalAttendees();

    // The whole day is free if there aren't any attendees
    if (requestAttendees.isEmpty() && requestAttendeesOptional.isEmpty()) {
      return Arrays.asList(TimeRange.WHOLE_DAY);
    }

    // Get available times from the mandatory people
    List<TimeRange> unavailableTimes =  getUnavailableTimes(events, requestAttendees);
    List<TimeRange> unavailableTimesMerged = mergeTimeRanges(unavailableTimes);
    List<TimeRange> availableTimes = getAvailableTimes(unavailableTimesMerged, request.getDuration()); 

    // Process the optional attendees 
    if (!requestAttendeesOptional.isEmpty()) {
    	List<TimeRange> busyTimesOptional = getUnavailableTimes(events, requestAttendeesOptional);
    	List<TimeRange> busyTimesMergedOpt = mergeTimeRanges(busyTimesOptional);
      busyTimesMergedOpt.addAll(unavailableTimesMerged);
      Collections.sort(busyTimesMergedOpt, TimeRange.ORDER_BY_START);
    	List<TimeRange> allBusyTimesMerged = mergeTimeRanges(busyTimesMergedOpt);
    	List<TimeRange> allAvailableTimes = getAvailableTimes(allBusyTimesMerged, request.getDuration());
      if (requestAttendees.isEmpty()) {
        return allAvailableTimes;
      } else if (!allAvailableTimes.isEmpty()) {
        return allAvailableTimes;
      }
    }

    return availableTimes;
  }

	/**
   * Merge overlapping time ranges and returns a new time range that emcompasses the times of both of them
   *
   * @param rangeList the list of unmerged time ranges, sorted by start time
   * @return a list of merged time ranges, still sorted by start time
   */
  private List<TimeRange> mergeTimeRanges(List<TimeRange> rangeList) {
    ArrayList<TimeRange> mergedList = new ArrayList<>();
    for (TimeRange currTimeRange : rangeList) {
      // Can simply add to list if there's no overlap or if mergedList is empty
      if (mergedList.isEmpty() || !currTimeRange.overlaps(mergedList.get(mergedList.size() - 1))) {
        mergedList.add(currTimeRange);
      } else {
        TimeRange lastTimeRange = mergedList.get(mergedList.size() -1);
        int modifiedStart = Math.min(lastTimeRange.start(), currTimeRange.start());
        int modifiedEnd = Math.max(lastTimeRange.end(), currTimeRange.end());

        TimeRange mergedRange = TimeRange.fromStartEnd(modifiedStart, modifiedEnd, false);
        mergedList.remove(lastTimeRange);
        mergedList.add(mergedRange);
      }
    }
    return mergedList;
  }
	
  /**
   * Gets all the times where attendees are busy, sorted by start time
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
          timeRangeList.add(e.getWhen());
          break; // Need only one relevant attendee for the event to be valid
        }
      }
    }
    Collections.sort(timeRangeList, TimeRange.ORDER_BY_START);
    return timeRangeList;   
  }

	/**
   * Returns all the available times during the day that is at least of the specified duration from a list of intervals that are merged
   *
   *@param unavailableTimesMerged the unavailable times that won't be in the returned time ranges
   *@param requestDuration the minimum length of every time range returned
   *@return a list of time ranges, in order of start time
   */
  private List<TimeRange> getAvailableTimes(List<TimeRange> unavailableTimesMerged, long requestDuration) {
    List<TimeRange> availableTimes = new ArrayList<>();
    int prevEnd = TimeRange.START_OF_DAY;
   	for (TimeRange currTimeRange : unavailableTimesMerged) {
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

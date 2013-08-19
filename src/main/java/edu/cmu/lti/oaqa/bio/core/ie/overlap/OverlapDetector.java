package edu.cmu.lti.oaqa.bio.core.ie.overlap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.Range;

/**
 * An interval detector based on 1-dim range of integer comparison.
 * <p>
 * TODO It can be replaced with {@link Range}.
 * 
 * @author Zi Yang <ziy@cs.cmu.edu>
 * 
 */
public class OverlapDetector {

  public static class Interval {

    public int begin;

    public int end;

    public Interval(int begin, int end) {
      super();
      this.begin = begin;
      this.end = end;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + begin;
      result = prime * result + end;
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      Interval other = (Interval) obj;
      if (begin != other.begin)
        return false;
      if (end != other.end)
        return false;
      return true;
    }

    @Override
    public String toString() {
      return "(" + begin + ", " + end + ")";
    }

  }

  private ArrayList<Integer> boundaries = new ArrayList<Integer>();

  public void addNonOverlappingInterval(int begin, int end) {
    int beginIndex = Collections.binarySearch(boundaries, begin);
    int endIndex = Collections.binarySearch(boundaries, end);
    /*
     * beginIndex >= 0 && endIndex >= 0: if both "begin" and "end" correspond to existing boundary
     * positions, then we claim begin must correspond to an "end" of certain existing boundary, and
     * end should be a "begin" of the next span, otherwise it means it is overlapped with other
     * span. Then, the two spans should be concatenated to a longer span, i.e., the "end" of the
     * first span and the "begin" of the second span should be collapsed.
     */
    if (beginIndex >= 0 && endIndex >= 0) {
      assert beginIndex % 2 == 1 && endIndex == beginIndex + 1;
      boundaries.remove(endIndex);
      boundaries.remove(beginIndex);
    }
    /*
     * beginIndex >= 0 && endIndex < 0: if "begin" corresponds to an existing position but "end"
     * does not, then we claim "begin" must be an "end" of an existing span, and the insertion
     * position of "end" should be the "begin" of the next span. Then, the "end" of the original
     * span should be extended to the new "end" position.
     */
    else if (beginIndex >= 0) {
      int endInsPos = -endIndex - 1;
      assert beginIndex % 2 == 1 && endInsPos == beginIndex + 1;
      boundaries.set(beginIndex, end);
    }
    /*
     * beginIndex < 0 && endIndex >= 0: similarly, if "end" corresponds to an existing position but
     * "start" does not, then we claim "end" must be a "start" of an existing span, and the
     * insertion position of "start" should be the same as the "end" position. Then, the "start" of
     * the original span should be moved backward to "start".
     */
    else if (endIndex >= 0) {
      int beginInsPos = -beginIndex - 1;
      assert endIndex % 2 == 0 && beginInsPos == endIndex;
      boundaries.set(endIndex, begin);
    }
    /*
     * otherwise, both positions do not exist in the boundary list. We claim the insertion position
     * of "begin" and "end" should both correspond to a "begin" position of some existing span.
     */
    else {
      int beginInsPos = -beginIndex - 1;
      int endInsPos = -endIndex - 1;
      assert beginInsPos % 2 == 0 && beginInsPos == endInsPos;
      boundaries.add(beginInsPos, end);
      boundaries.add(beginInsPos, begin);
    }
  }

  public boolean isNonOverlapping(int begin, int end) {
    int beginIndex = Collections.binarySearch(boundaries, begin);
    int endIndex = Collections.binarySearch(boundaries, end);
    if (beginIndex >= 0 && endIndex >= 0) {
      return beginIndex % 2 == 1 && endIndex == beginIndex + 1;
    } else if (beginIndex >= 0) {
      int endInsPos = -endIndex - 1;
      return beginIndex % 2 == 1 && endInsPos == beginIndex + 1;
    } else if (endIndex >= 0) {
      int beginInsPos = -beginIndex - 1;
      return endIndex % 2 == 0 && beginInsPos == endIndex;
    } else {
      int beginInsPos = -beginIndex - 1;
      int endInsPos = -endIndex - 1;
      return beginInsPos % 2 == 0 && beginInsPos == endInsPos;
    }
  }

  public void addOverlappingInterval(int begin, int end) {
    addOverlappingInterval(begin, end, 0);
  }

  public void addOverlappingInterval(int begin, int end, int slack) {
    List<Interval> intervals = getIntervalsOverlappingWith(begin, end, slack);
    for (Interval interval : intervals) {
      removeInterval(interval.begin, interval.end);
    }
    intervals.add(new Interval(begin, end));
    Interval boundingBox = getBoundingBox(intervals);
    addNonOverlappingInterval(boundingBox.begin, boundingBox.end);
  }

  public void removeInterval(int begin, int end) {
    int beginIndex = Collections.binarySearch(boundaries, begin);
    int endIndex = Collections.binarySearch(boundaries, end);
    assert beginIndex > 0 && beginIndex % 2 == 0 && endIndex > 0 && endIndex == beginIndex + 1;
    boundaries.remove(endIndex);
    boundaries.remove(beginIndex);
  }

  public List<Interval> getIntervals() {
    List<Interval> intervals = new ArrayList<Interval>();
    for (int i = 0; i < boundaries.size(); i += 2) {
      intervals.add(new Interval(boundaries.get(i), boundaries.get(i + 1)));
    }
    return intervals;
  }

  public List<Interval> getIntervalsOverlappingWith(int begin, int end) {
    return getIntervalsOverlappingWith(begin, end, 0);
  }

  public List<Interval> getIntervalsOverlappingWith(int begin, int end, int slack) {
    int beginOverlapIndex = getOverlappingIntervalBeginIndex(begin - slack);
    int endOverlapIndex = getOverlappingIntervalEndIndex(end + slack);
    List<Interval> overlappingIntervals = new ArrayList<Interval>();
    for (int beginIndex = beginOverlapIndex; beginIndex < endOverlapIndex; beginIndex += 2) {
      overlappingIntervals.add(new Interval(boundaries.get(beginIndex), boundaries
              .get(beginIndex + 1)));
    }
    return overlappingIntervals;
  }

  private int getOverlappingIntervalBeginIndex(int pos) {
    int index = Collections.binarySearch(boundaries, pos);
    if (index >= 0) {
      if (index % 2 == 0) {
        // exactly the begin of an interval, return the index of this position
        return index;
      } else {
        // exactly the end of an interval, return the index of next position
        return index + 1;
      }
    } else {
      int insPos = -index - 1;
      if (insPos % 2 == 0) {
        // outside an interval, return the index of the insertion position, corresponding to the
        // begin of next interval
        return insPos;
      } else {
        // inside an interval, return the index of the previous position
        return insPos - 1;
      }
    }
  }

  private int getOverlappingIntervalEndIndex(int pos) {
    int index = Collections.binarySearch(boundaries, pos);
    if (index >= 0) {
      if (index % 2 == 0) {
        // exactly the begin of an interval, return the index of previous position
        return index - 1;
      } else {
        // exactly the end of an interval, return the index of this position
        return index;
      }
    } else {
      int insPos = -index - 1;
      if (insPos % 2 == 0) {
        // outside an interval, return the index of the insertion position - 1, corresponding to the
        // end of previous interval
        return insPos - 1;
      } else {
        // inside an interval, return the index of the this position
        return insPos;
      }
    }
  }

  public static Interval getBoundingBox(List<Interval> intervals) {
    int min = Integer.MAX_VALUE;
    int max = Integer.MIN_VALUE;
    for (Interval interval : intervals) {
      if (interval.begin < min) {
        min = interval.begin;
      }
      if (interval.end > max) {
        max = interval.end;
      }
    }
    return new Interval(min, max);
  }

  public static void main(String[] args) {
    OverlapDetector detector = new OverlapDetector();
    detector.addNonOverlappingInterval(3, 20);
    detector.addNonOverlappingInterval(24, 43);
    System.out.println(detector.getIntervals());
    detector.addOverlappingInterval(46, 60);
    System.out.println(detector.getIntervals());
    detector.addOverlappingInterval(44, 70, 2);
    System.out.println(detector.getIntervals());
  }
}

package uk.ac.ebi.pride.proteomes.web.client.datamodel;

import uk.ac.ebi.pride.proteomes.web.client.exceptions.IllegalRegionValueException;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 04/11/13
 *         Time: 15:07
 */
public class Region {
    private static class EmptyRegion extends Region {
        EmptyRegion() {}

        EmptyRegion(int start, int end) throws IllegalRegionValueException {
            super(0, 0);
        }

        @Override
        public int getEnd() {
            return 0;
        }

        @Override
        public int getLength() {
            return 0;
        }

        @Override
        public String toString() {
            return "";
        }

        @Override
        public boolean isEmpty() {
            return true;
        }
    }
    private static volatile EmptyRegion emptyRegion = new EmptyRegion();
    private int end;
    private int start;

    private Region(){}

    public Region(int start, int end) throws IllegalRegionValueException {
        if(end - start < 0 || (!(this instanceof EmptyRegion) && end == 0 &&
                start == 0)) {
            throw new IllegalRegionValueException();
        }

        this.start = start;
        this.end = end;
    }

    public static Region tokenize(String regionId) throws
                                                   IllegalRegionValueException,
                                                   NumberFormatException,
                                                   IndexOutOfBoundsException {
        String[] startEnd = regionId.split("-");
        return new Region(Integer.parseInt(startEnd[0]),
                          Integer.parseInt(startEnd[1]));
    }

    public static EmptyRegion emptyRegion() {
        return emptyRegion;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public int getLength() {
        return end - start;
    }

    public String toString() {
        return Integer.toString(start) + "-" + Integer.toString(end);
    }

    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if(o == this) {
            return true;
        }
        if(o == null || o.getClass() != this.getClass()) {
            return false;
        }

        Region other = (Region) o;

        return this.getStart() == other.getStart() && this.getEnd() == other.getEnd();
    }
}

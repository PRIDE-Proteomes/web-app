package uk.ac.ebi.pride.proteomes.web.client.datamodel;

import uk.ac.ebi.pride.proteomes.web.client.exceptions.IllegalRegionValueException;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 04/11/13
 *         Time: 15:07
 */
public class Region {
    private final int end;
    private final int start;

    public Region(int start, int end) throws IllegalRegionValueException {
        if(end - start < 1) {
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

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public int getLength() {
        return end - start;
    }
}

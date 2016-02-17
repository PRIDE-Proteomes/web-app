package uk.ac.ebi.pride.proteomes.web.client.datamodel;

import uk.ac.ebi.pride.proteomes.web.client.datamodel.factory.ModifiedLocation;
import uk.ac.ebi.pride.proteomes.web.client.exceptions.IllegalModificationPositionException;

import java.util.Objects;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 04/11/13
 *         Time: 15:07
 */
public class ModificationWithPosition implements ModifiedLocation, Comparable<ModificationWithPosition> {


    private static class EmptyModificationWithPosition extends ModificationWithPosition {
        EmptyModificationWithPosition() {
        }

        @Override
        public int getPosition() {
            return 0;
        }

        @Override
        public String getModification() {
            return "";
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

    private static volatile EmptyModificationWithPosition emptyModificationWithPosition = new EmptyModificationWithPosition();
    private String modification;
    private Integer position;

    private ModificationWithPosition() {}


    public ModificationWithPosition(String modification, Integer position) throws IllegalModificationPositionException {
        if(position != null){
            if (position < 0 || (!(this instanceof EmptyModificationWithPosition)
                    && position <= 0 && (modification == null || modification.equals("")))) {
                throw new IllegalModificationPositionException();
            }
        }

        this.position = position;
        this.modification = modification;
    }

    public static ModificationWithPosition tokenize(String modificationId) throws
            IllegalModificationPositionException,
            NumberFormatException,
            IndexOutOfBoundsException {
        String[] startEnd = modificationId.split("-");

        return new ModificationWithPosition(startEnd[0], Integer.parseInt(startEnd[1]));
    }

    public static EmptyModificationWithPosition emptyModificationWithPosition() {
        return emptyModificationWithPosition;
    }

    @Override
    public int getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    @Override
    public String getModification() {
        return modification;
    }

    public void setModification(String modification) {
        this.modification = modification;
    }

    public boolean isEmpty() {
        return false;
    }

    @Override
    public String toString() {
        return modification + "-" + Integer.toString(position);
    }

    @Override
    public int compareTo(ModificationWithPosition o) {
        if (this == o) return 0;

        if (this.position < o.position) return -1;
        if (this.position > o.position) return 1;

        int comparison = this.modification.compareTo(o.modification);
        if (comparison != 0) return comparison;

        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ModificationWithPosition)) return false;
        ModificationWithPosition that = (ModificationWithPosition) o;
        return Objects.equals(position, that.position) &&
                Objects.equals(modification, that.modification);
    }

    @Override
    public int hashCode() {
        return Objects.hash(modification, position);
    }
}

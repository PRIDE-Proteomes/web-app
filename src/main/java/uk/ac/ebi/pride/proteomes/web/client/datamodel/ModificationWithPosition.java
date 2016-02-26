package uk.ac.ebi.pride.proteomes.web.client.datamodel;

import uk.ac.ebi.pride.proteomes.web.client.datamodel.factory.ModifiedLocation;

import java.util.Objects;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 04/11/13
 *         Time: 15:07
 */
public class ModificationWithPosition implements ModifiedLocation, Comparable<ModificationWithPosition> {


    private static volatile EmptyModificationWithPosition emptyModificationWithPosition = new EmptyModificationWithPosition();
    private String modification;
    private int position;
    private ModificationWithPosition() {
    }

    public ModificationWithPosition(String modification, int position) {
        this.position = position;
        this.modification = modification;
    }

    public static ModificationWithPosition tokenize(String modificationId) throws
            NumberFormatException,
            IndexOutOfBoundsException {
        ModificationWithPosition mod;
        String[] startEnd = modificationId.split("-");

        if (startEnd.length == 2) {
            mod = new ModificationWithPosition(startEnd[0], Integer.parseInt(startEnd[1]));
        } else {
            //A negative value in the position, implies that the modification doesn't have positon, only type
            mod = new ModificationWithPosition(startEnd[0], -1);
        }

        return mod;
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
        return (position >= 0) ? modification + "-" + Integer.toString(position) : modification;
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

    private static class EmptyModificationWithPosition extends ModificationWithPosition {
        EmptyModificationWithPosition() {
        }

        @Override
        public int getPosition() {
            return -1;
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
}

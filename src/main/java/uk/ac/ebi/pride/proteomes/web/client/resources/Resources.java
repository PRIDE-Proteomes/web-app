package uk.ac.ebi.pride.proteomes.web.client.resources;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

/**
 * @author ntoro
 * @since 17/09/15 16:35
 */
public interface Resources extends ClientBundle {

    public static final Resources INSTANCE =  GWT.create(Resources.class);

    interface PeptideStyle extends CssResource {


        @ClassName("unique-to-gene-color")
        String uniqueToGeneColor();

        @ClassName("unique-to-up-entry-color")
        String uniqueToUpEntryColor();

        @ClassName("unique-to-protein-color")
        String uniqueToProteinColor();

        @ClassName("non-unique-peptide-color")
        String nonUniquePeptideColor();

        @ClassName("unique-to-protein-box")
        String uniqueToProteinBox();

        @ClassName("unique-to-gene-box")
        String uniqueToGeneBox();

        @ClassName("unique-to-up–entry–box")
        String uniqueToUpEntryBox();

        @ClassName("non-unique-peptide-box")
        String nonUniquePeptideBox();

    }

    @Source("peptide-colours.gss")
    PeptideStyle style();
}

package uk.ac.ebi.pride.proteomes.web.client.style;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

/**
 * @author ntoro
 * @since 17/09/15 16:35
 */
public interface Resources extends ClientBundle {

    Resources INSTANCE =  GWT.create(Resources.class);

    interface PeptideStyle extends CssResource {

        @ClassName("unique-to-gene-color")
        String uniqueToGeneColor();

        @ClassName("unique-to-protein-color")
        String uniqueToProteinColor();

        @ClassName("non-unique-peptide-color")
        String nonUniquePeptideColor();

        @ClassName("unique-to-protein-box")
        String uniqueToProteinBox();

        @ClassName("unique-to-gene-box")
        String uniqueToGeneBox();

        @ClassName("non-unique-peptide-box")
        String nonUniquePeptideBox();

    }

    @Source("peptide-style.gss")
    PeptideStyle style();
}

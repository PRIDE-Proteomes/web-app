package uk.ac.ebi.pride.proteomes.web.client.images;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

import static com.google.gwt.resources.client.ImageResource.*;

/**
 * @author ntoro
 * @since 13/01/2016 15:16
 */
public interface FileImages extends ClientBundle {

    FileImages INSTANCE =  GWT.create(FileImages.class);

    @Source("file_TSV.png")
    @ImageOptions(width = 20)
    ImageResource tsvPngFile();

}

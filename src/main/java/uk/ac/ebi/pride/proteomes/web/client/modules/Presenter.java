package uk.ac.ebi.pride.proteomes.web.client.modules;

import com.google.gwt.user.client.ui.AcceptsOneWidget;

/**
 * All the presenters in the application should implement this interface
 * this allows them to put their views in an assigned container. This also
 * allows them to manage all the region inside the container,
 * as well as the container properties, such as size.
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 17/10/13
 *         Time: 10:28
 */
public interface Presenter {
    public void bindToContainer(final AcceptsOneWidget container);
}

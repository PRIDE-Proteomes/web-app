package uk.ac.ebi.pride.proteomes.web.client.modules;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.IsWidget;

/**
 * This interfaces forces views to be connected to uihandler so they are all
 * prepared to send event to anyone that wants to. It makes it standard to
 * hide or show them at will and make then connected to a container to build
 * the DOM tree.
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 17/10/13
 *         Time: 13:59
 */
public interface View extends IsWidget {
    void bindToContainer(AcceptsOneWidget container);
    void setVisible(boolean visible);
}

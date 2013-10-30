package uk.ac.ebi.pride.proteomes.web.client.modules;

import com.google.gwt.user.client.ui.AcceptsOneWidget;

import java.util.Collection;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 17/10/13
 *         Time: 13:59
 */
public interface View<UiHandler> {
    public void bindToContainer(AcceptsOneWidget container);
    public void addUiHandler(UiHandler handler);
    Collection<UiHandler> getUiHandlers();
}

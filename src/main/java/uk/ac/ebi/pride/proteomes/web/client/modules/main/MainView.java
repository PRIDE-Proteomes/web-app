package uk.ac.ebi.pride.proteomes.web.client.modules.main;

import com.google.gwt.user.client.ui.*;

import java.util.Collection;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 16/10/13
 *         Time: 11:26
 */
public class MainView implements MainPresenter.View {

    private final Panel layout;
    private final AcceptsOneWidget northPanel;
    private final AcceptsOneWidget southPanel;

    public MainView() {
        layout = new VerticalPanel();

        layout.setWidth("100%");

        northPanel = new SimplePanel();
        layout.add((Widget) northPanel);

        southPanel = new SimplePanel();
        layout.add((Widget) southPanel);
    }

    @Override
    public void bindToContainer(AcceptsOneWidget container) {
        container.setWidget(layout);
    }

    @Override
    public void addUiHandler(Object o) {
        // TODO
    }

    @Override
    public Collection getUiHandlers() {
        return null;  // TODO
    }

    @Override
    public void hidePopup() {
        // TODO
    }

    @Override
    public void showPopup() {
        // TODO
    }

    @Override
    public void showPopup(String message) {
        // TODO
    }

    @Override
    public AcceptsOneWidget getNorthPlaceHolder() {
        return northPanel;
    }

    @Override
    public AcceptsOneWidget getSouthPlaceHolder() {
        return southPanel;
    }
}

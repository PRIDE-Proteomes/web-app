package uk.ac.ebi.pride.proteomes.web.client.modules.main;

import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import uk.ac.ebi.pride.proteomes.web.client.modules.UiHandler;

import java.util.Collection;
import java.util.List;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 16/10/13
 *         Time: 11:26
 */
public class MainView implements MainPresenter.View {

    private final Panel layout;
    private final List<AcceptsOneWidget> panelList;
    private PopupMask popup;

    public MainView(List<AcceptsOneWidget> panels) {
        panelList = panels;

        layout = new VerticalPanel();
        layout.setWidth("100%");

        for (AcceptsOneWidget panel : panelList) {
            layout.add((Widget) panel);
        }
        popup = new PopupMask();
        //We want to move the popup position when the window is resized
        Window.addResizeHandler(new ResizeHandler() {
            @Override
            public void onResize(ResizeEvent event) {
                if(popup.isShowing())
                    popup.setPosition(popup.getOffsetWidth(),
                                      popup.getOffsetHeight());
            }
        });
    }

    @Override
    public void bindToContainer(AcceptsOneWidget container) {
        container.setWidget(layout);
    }

    @Override
    public void addUiHandler(UiHandler handler) {
        // TODO
    }

    @Override
    public Collection<UiHandler> getUiHandlers() {
        return null;  // TODO
    }

    @Override
    public void setVisible(boolean visible) {
        asWidget().setVisible(visible);
    }

    @Override
    public void hideMessage() {
        popup.hide();
    }

    @Override
    public void showLoadingMessage() {
        popup.displayLoadingMessage();
    }

    @Override
    public void showInfoMessage(String message) {
        popup.displayMessage(message);
    }

    @Override
    public AcceptsOneWidget getPlaceHolder(int i) {
        return panelList.get(i);
    }

    @Override
    public Widget asWidget() {
        return layout;
    }
}

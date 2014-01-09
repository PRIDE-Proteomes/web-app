package uk.ac.ebi.pride.proteomes.web.client.modules.main;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import uk.ac.ebi.pride.widgets.client.disclosure.client.ModuleContainer;

/**
 * @author : Javi Contell <jcontell@ebi.ac.uk>
 * Date: 12/12/12
 * Time: 11:39
 *
 */
public class PopupMask extends    PopupPanel
                       implements PopupPanel.PositionCallback,
                                  ResizeHandler,
                                  Window.ScrollHandler {

    private Label messageLabel;
    private VerticalPanel messageWidget;
    private Widget loadingWidget;

    public PopupMask() {
        super(false);

        messageLabel = new Label("");
        Button backButton = new Button("Back");
        backButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                History.back();
            }
        });

        messageWidget = new VerticalPanel();
        messageWidget.add(messageLabel);
        messageWidget.add(backButton);
        messageWidget.setCellHorizontalAlignment(backButton, HasHorizontalAlignment.ALIGN_CENTER);
        messageWidget.setStyleName("gwt-Popup");

        loadingWidget = ModuleContainer.getLoadingPanel();

        setStyleName("gwt-Popup");

        setGlassEnabled(true);
        setGlassStyleName("gwt-PopupGlass");

        setModal(true);
        setAnimationEnabled(true);

        setWidget(loadingWidget);

        center();
        setPopupPositionAndShow(this);
    }

    @Override
    public void onResize(ResizeEvent event) {
        if(isShowing())
            setPosition(getOffsetWidth(), getOffsetHeight());
    }

    @Override
    public void setPosition(int offsetWidth, int offsetHeight) {
        center();
    }

    @Override
    public void onWindowScroll(Window.ScrollEvent event) {
        if(isShowing())
            setPopupPositionAndShow(this);
    }

    public void displayLoadingMessage(){
        setWidget(loadingWidget);
        setPopupPositionAndShow(this);
    }

    public void displayMessage(String message){
        messageLabel.setText(message);
        setWidget(messageWidget);
        setPopupPositionAndShow(this);
    }
}

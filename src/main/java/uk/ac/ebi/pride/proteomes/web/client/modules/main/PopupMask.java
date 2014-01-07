package uk.ac.ebi.pride.proteomes.web.client.modules.main;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import uk.ac.ebi.pride.proteomes.web.client.utils.Console;
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

    private Label messageWidget;
    private Widget loadingWidget;

    public PopupMask() {
        super(false);

        messageWidget = new Label("");
        loadingWidget = ModuleContainer.getLoadingPanel();

        setGlassEnabled(true);

        setGlassStyleName("gwt-PopupBackground");
        setModal(true);
        setAnimationEnabled(true);

        setWidget(loadingWidget);
        center();

        setPopupPositionAndShow(this);
    }

    @Override
    public void onResize(ResizeEvent event) {
        if(this.isShowing())
            setPosition(getOffsetWidth(), getOffsetHeight());
    }

    @Override
    public void setPosition(int offsetWidth, int offsetHeight) {
        //setPopupPosition(offsetWidth, offsetHeight);
        center();
    }

    @Override
    public void onWindowScroll(Window.ScrollEvent event) {
        if(this.isShowing())
            setPopupPositionAndShow(this);
    }

    @Override
    public void show() {
        super.show();
    }

    public void displayLoadingMessage(){
        setWidget(loadingWidget);
        setPopupPositionAndShow(this);
    }

    public void displayMessage(String message){
        messageWidget.setText(message);
        setWidget(messageWidget);
        setPopupPositionAndShow(this);
    }
}

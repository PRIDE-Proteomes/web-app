package uk.ac.ebi.pride.proteomes.web.client.utils.factories;

import com.google.gwt.user.client.ui.*;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
public abstract class FrontierIcons {

    public static enum Type {
        CLOSE('x', "icon icon-functional", "Close"),
        COLLAPSE('w', "icon icon-functional", "Collapse"),
        EXPAND('u', "icon icon-functional", "Expand"),
        FULL_SCREEN('F', "icon icon-functional", "Full screen"),
        REFRESH('R', "icon icon-functional", "Refresh"),
        DOWNLOAD('=', "icon icon-functional", "Download");

        protected final char c;
        protected final String styleName;
        protected final String tooltip;

        private Type(char c, String styleName, String tooltip) {
            this.c = c;
            this.styleName = styleName;
            this.tooltip = tooltip;
        }
    }

    public static Widget getIcon(Type t){
        Panel icon = new HTMLPanel("");
        setIconToElement(icon, t);
        return icon;
    }

    public static void setIconToElement(Widget w, Type t){
        w.setStyleName(t.styleName);
        w.getElement().setAttribute("aria-hidden", "true");
        w.getElement().setAttribute("data-icon",  String.valueOf(t.c));
        w.getElement().setTitle(t.tooltip);
    }

    public static void setIconAndTextToElement(Widget w, Type t, String text){
        setIconToElement(w, t);
        w.getElement().setInnerHTML("&nbsp;&nbsp;" + text);
    }
}

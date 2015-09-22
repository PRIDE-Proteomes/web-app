package uk.ac.ebi.pride.proteomes.web.client.modules.legend;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import uk.ac.ebi.pride.proteomes.web.client.modules.View;
import uk.ac.ebi.pride.proteomes.web.client.style.Resources;

/**
 * @author ntoro
 * @since 20/05/15 16:45
 */
public class LegendView implements View {

    @UiTemplate("LegendView.ui.xml")
    interface LegendUiBinder extends UiBinder<Widget, LegendView> {}

    private static LegendUiBinder uiBinder = GWT.create(LegendUiBinder.class);

    private Widget root;

    @UiField
    HTMLPanel panel;

    public LegendView() {
        root = uiBinder.createAndBindUi(this);
        Resources.INSTANCE.style().ensureInjected();
    }

    @Override
    public void bindToContainer(AcceptsOneWidget container) {
        container.setWidget(root);
    }

    @Override
    public Widget asWidget() {
        return root;
    }

    @Override
    public void setVisible(boolean visible) {
        asWidget().setVisible(visible);
    }

}

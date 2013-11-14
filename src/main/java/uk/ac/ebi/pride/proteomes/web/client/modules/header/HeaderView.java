package uk.ac.ebi.pride.proteomes.web.client.modules.header;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.*;

import java.util.Collection;

import static uk.ac.ebi.pride.proteomes.web.client.modules.header.HeaderPresenter.View;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 07/11/13
 *         Time: 12:08
 */
public class HeaderView implements View {

    @UiTemplate("HeaderView.ui.xml")
    interface HeaderUiBinder extends UiBinder<HTMLPanel, HeaderView> {
    }

    private static HeaderUiBinder ourUiBinder = GWT.create(HeaderUiBinder.class);

    @UiField
    VerticalPanel vPanel;

    @UiField
    Label title;

    @UiField
    Label description;

    @UiField
    Label attributes;

    HTMLPanel root;

    public HeaderView() {
        root = ourUiBinder.createAndBindUi(this);

        this.title.setText("");
        this.description.setText("");
        this.attributes.setText("");

    }
    @Override
    public void updateTitle(String title) {
       this.title.setText(title);
    }

    @Override
    public void updateDescription(String description) {
        this.description.setText(description);
    }

    @Override
    public void updateProperties(String properties) {
        this.attributes.setText(properties);
    }

    @Override
    public void bindToContainer(AcceptsOneWidget container) {
        container.setWidget(root);
    }

    @Override
    public void addUiHandler(Object o) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Collection getUiHandlers() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Widget asWidget() {
        return vPanel;
    }

    @Override
    public void setVisible(boolean visible) {
        asWidget().setVisible(visible);
    }
}

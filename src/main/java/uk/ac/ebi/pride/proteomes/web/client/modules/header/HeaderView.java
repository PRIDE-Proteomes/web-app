package uk.ac.ebi.pride.proteomes.web.client.modules.header;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.*;
import uk.ac.ebi.pride.proteomes.web.client.modules.UiHandler;

import java.util.Collection;
import java.util.Collections;

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
    public void addUiHandler(UiHandler handler) {
        // We don't emit any Ui events, so we don't have to add any handlers
    }

    @Override
    public Collection<UiHandler> getUiHandlers() {
        return Collections.emptyList();
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

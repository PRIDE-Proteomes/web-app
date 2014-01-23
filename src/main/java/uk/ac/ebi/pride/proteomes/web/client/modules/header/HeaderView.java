package uk.ac.ebi.pride.proteomes.web.client.modules.header;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.*;
import uk.ac.ebi.pride.proteomes.web.client.utils.Pair;
import uk.ac.ebi.pride.proteomes.web.client.utils.factories.HyperlinkFactory;

import java.util.List;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 07/11/13
 *         Time: 12:08
 */
public class HeaderView implements HeaderPresenter.ThisView {

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
    FlowPanel attributes;

    private HTMLPanel root;

    public HeaderView() {
        root = ourUiBinder.createAndBindUi(this);

        title.setText("");
        description.setText("");

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
    public void updateProperties(List<Pair<String, String>> links) {
        clearProperties();
        for(Pair<String, String> link : links) {
            attributes.add(HyperlinkFactory.getInlineHyperLink(link.getA(), link.getB()));
            if(link != links.get(links.size() - 1)) {
                attributes.add(new InlineLabel(", "));
            }
        }
    }

    @Override
    public void clearProperties() {
        attributes.clear();
    }

    @Override
    public void bindToContainer(AcceptsOneWidget container) {
        container.setWidget(root);
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

package uk.ac.ebi.pride.proteomes.web.client.datamodel.adapters;

import uk.ac.ebi.pride.proteomes.web.client.datamodel.factory.Feature;
import uk.ac.ebi.pride.widgets.client.common.handler.FeatureHandler;

public class FeatureAdapter implements FeatureHandler {

    Feature feature;

    public FeatureAdapter(Feature feature) {
        this.feature = feature;
    }

    @Override
    public int getId() {
        return feature.getId();
    }

    @Override
    public String getType() {
        return feature.getType();
    }

    @Override
    public Integer getStart() {
        return feature.getStart();
    }

    @Override
    public Integer getEnd() {
        return feature.getEnd();
    }

}

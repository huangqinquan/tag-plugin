package org.elasticsearch.plugin.analysis.tag;

import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.plugins.SearchPlugin;

import java.util.List;

public class DemoPlugin extends Plugin implements SearchPlugin{

    @Override
    public List<QuerySpec<?>> getQueries() {
        return null;
    }
}

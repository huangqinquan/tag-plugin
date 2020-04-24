package org.elasticsearch.plugin.analysis.tag;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.Term;
import org.elasticsearch.common.component.AbstractComponent;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.script.ScriptContext;
import org.elasticsearch.script.ScriptEngine;
import org.elasticsearch.script.SearchScript;
import org.elasticsearch.search.lookup.SourceLookup;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.Map;

public class TagScriptEngine implements ScriptEngine{

    private static final String SCRIPT_NAME = "tag_weight";

    private static final String SCRIPT_SOURCE = "test";

    protected final Logger logger =  Loggers.getLogger(getClass());



    @Override
    public String getType() {
        return SCRIPT_NAME;
    }

    @Override
    public <FactoryType> FactoryType compile(String name, String code, ScriptContext<FactoryType> context, Map<String, String> params) {
        if (!context.equals(SearchScript.CONTEXT)) {
            throw new IllegalArgumentException(getType()
                    + " scripts cannot be used for context ["
                    + context.name + "]");
        }
        if (!code.equals(SCRIPT_SOURCE)){
            throw new IllegalArgumentException("Unknown script name " + code);
        }
        SearchScript.Factory factory = (p, lookup) -> new SearchScript.LeafFactory() {

            final String tag_id;
            final String[] tag_ids;
            {
                if (p.containsKey("tag_id") == false) {
                    throw new IllegalArgumentException("Missing parameter [tag_id]");
                }

                tag_id = p.get("tag_id").toString();
                tag_ids = tag_id.split(" ");
            }

            @Override
            public SearchScript newInstance(LeafReaderContext context) throws IOException {
                return new SearchScript(p, lookup, context) {
                    @Override
                    public double runAsDouble() {
                        SourceLookup source = lookup.source();
                        Double score = 0d;
                        logger.info("========" + source.get("tag").getClass().toString() + "=========");
                        HashMap tag = (HashMap) source.get("tag");
                        for (String tag_id : tag_ids) {
//                            如果没获取标签直接跳过
                            if (!tag.containsKey(tag_id)) continue;
                            score += (Double)(tag.get(tag_id));
                        }
                        return score;
                    }

                };
            }

            @Override
            public boolean needs_score() {
                return false;
            }
        };

        return context.factoryClazz.cast(factory);
    }

    @Override
    public void close() throws IOException {

    }
}

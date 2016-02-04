package io.github.shadowchild.common.newconfig;


import io.github.shadowchild.common.util.Resource;

/**
 * Created by Zach Piddock on 03/01/2016.
 */
public class JsonConfig extends Config {

    public JsonConfig(Resource location, EnumConfigType type) {

        super(location, type);
    }

    @Override
    public String parseSection(String section) {

        return null;
    }
}
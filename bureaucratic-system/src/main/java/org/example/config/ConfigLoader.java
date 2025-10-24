package org.example.config;

import org.example.model.Config;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.LoaderOptions;

import java.io.InputStream;

public class ConfigLoader {

    public static Config loadConfig(String resourcePath) {
        LoaderOptions loaderOptions = new LoaderOptions();
        Yaml yaml = new Yaml(new Constructor(Config.class, loaderOptions));
        InputStream inputStream = ConfigLoader.class.getClassLoader().getResourceAsStream(resourcePath);

        if (inputStream == null) {
            throw new RuntimeException("Configuration file not found: " + resourcePath);
        }

        return yaml.load(inputStream);
    }
}


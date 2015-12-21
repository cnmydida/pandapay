package org.pandapay.utils.impl;

import org.pandapay.utils.IdGenerator;
import org.pandapay.utils.IdManager;
import org.springframework.boot.bind.RelaxedPropertyResolver;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.support.ApplicationObjectSupport;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by LANLI on 2015/12/20.
 */
public class IdManagerImpl extends ApplicationObjectSupport implements IdManager, EnvironmentAware {

    private Map<String,IdGenerator> idGenerators;
    private final Map<String,Map<String,String>> idGeneratorConfiguration = new HashMap<>();
    private RelaxedPropertyResolver propertyResolver;

    @Override
    public void setEnvironment(Environment environment) {
        propertyResolver = new RelaxedPropertyResolver(environment, "idManager.");
    }

    @PostConstruct
    private void setIdGenerators()
    {
        idGenerators = getApplicationContext().getBeansOfType(IdGenerator.class);
        Map<String, Object> properties = propertyResolver.getSubProperties("");
        for (Map.Entry<String, Object> entry : properties.entrySet())
        {
            String[] strings = StringUtils.split(entry.getKey(), ".");
            if (strings.length == 2)
            {
                String key = strings[0];
                Map<String, String> values = idGeneratorConfiguration.get(key);
                if (values == null)
                {
                    values = new HashMap<>();
                }
                values.put(strings[1], entry.getValue().toString());
                idGeneratorConfiguration.put(key, values);
            }
        }
    }

    @Override
    public IdGenerator getIdGenerator(String idType) {


        Map<String, String> map = idGeneratorConfiguration.get(idType);
        if (map != null)
        {
            String generator = map.get("generator");
            if (generator != null)
            {
                return idGenerators.get(generator);
            }
        }
        return new UuidGenerator();
    }

}

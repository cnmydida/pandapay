package org.pandapay.utils;

import org.pandapay.utils.impl.IdGeneratorConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.bind.RelaxedPropertyResolver;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.support.ApplicationObjectSupport;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by LANLI on 2015/12/27.
 */
@Service
public class IdService implements EnvironmentAware {

    @Autowired
    private IdGeneratorFactory idGeneratorFactory;

    private final Map<String,Map<String,String>> idGeneratorConfiguration = new HashMap<>();

    //private Set<IdGenerator> idGenerators = Collections.newSetFromMap(new ConcurrentHashMap<IdGenerator, Boolean>());

    private ConcurrentMap<String, IdGenerator> idGenerators = new ConcurrentHashMap<>();

    private RelaxedPropertyResolver propertyResolver;

    public <T> T nextId(String idType)
    {
        IdGenerator<T> idGenerator = idGenerators.get(idType);
        if (idGenerator == null)
        {
            Map<String, String> generatorConfiguration = idGeneratorConfiguration.get(idType);
            IdGeneratorType idGeneratorType = IdGeneratorType.valueOf(generatorConfiguration.get("generator"));
            idGenerator = idGeneratorFactory.getIdGenerator(idGeneratorType);
            if (idGenerator instanceof IdGeneratorConfig)
            {
                ((IdGeneratorConfig) idGenerator).setIdType(idType);
                ((IdGeneratorConfig) idGenerator).setParams(generatorConfiguration);
            }
            IdGenerator oldIdGenerator = idGenerators.putIfAbsent(idType, idGenerator);
            if (oldIdGenerator != null)
            {
                idGenerator = oldIdGenerator;
            }
        }
        return idGenerator.nextID();
    }

    @Override
    public void setEnvironment(Environment environment) {
        propertyResolver = new RelaxedPropertyResolver(environment, "idManager.");
        resolveGeneratorConfiguration();
    }

    private void resolveGeneratorConfiguration()
    {
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
}

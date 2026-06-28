package org.febit.common.jooq.converter.support;

import lombok.experimental.UtilityClass;
import org.febit.lang.jackson.JacksonCodec;
import org.febit.lang.jackson.JacksonCodecImpl;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.json.JsonMapper;

@UtilityClass
public class JacksonCodecSupport {

    public static final JacksonCodec CODEC = JacksonCodecImpl.ofStandard(
            JsonMapper.builder()
                    .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
    );

}

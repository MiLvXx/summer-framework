package com.xuxin.summer.utils;

import org.yaml.snakeyaml.resolver.Resolver;

/**
 * description:
 *
 * @author xuxin
 * @since 2024/9/6
 */
public class NoImplicitResolver extends Resolver {
    public NoImplicitResolver() {
        super.yamlImplicitResolvers.clear();
    }
}

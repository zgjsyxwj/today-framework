/**
 * Original Author -> 杨海健 (taketoday@foxmail.com) https://taketoday.cn
 * Copyright © TODAY & 2017 - 2019 All Rights Reserved.
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *   
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see [http://www.gnu.org/licenses/]
 */
package cn.taketoday.web.mapping;

import java.util.ArrayList;
import java.util.List;

import cn.taketoday.context.annotation.MissingBean;
import cn.taketoday.context.utils.OrderUtils;
import cn.taketoday.web.Constant;
import cn.taketoday.web.config.ActionConfiguration;
import cn.taketoday.web.interceptor.HandlerInterceptor;
import cn.taketoday.web.utils.WebUtils;

/**
 * @author TODAY <br>
 *         2019-05-15 21:34
 * @since 2.3.7
 */
@MissingBean
public class ResourceMappingRegistry {

    private final List<ResourceMapping> resourceMappings = new ArrayList<>();

    public ResourceMapping addResourceMapping(String... pathPatterns) {
        ResourceMapping resourceMapping = new ResourceMapping(null, pathPatterns);

        getResourceMappings().add(resourceMapping);
        return resourceMapping;
    }

    @SafeVarargs
    public final <T extends HandlerInterceptor> ResourceMapping addResourceMapping(Class<T>... handlerInterceptors) {

        final ActionConfiguration actionConfiguration = //
                WebUtils.getWebApplicationContext().getBean(Constant.ACTION_CONFIG, ActionConfiguration.class);

        ResourceMapping resourceHandlerMapping = //
                new ResourceMapping(actionConfiguration.addInterceptors(handlerInterceptors));

        this.getResourceMappings().add(resourceHandlerMapping);
        return resourceHandlerMapping;
    }

    public boolean isEmpty() {
        return resourceMappings.isEmpty();
    }

    public List<ResourceMapping> getResourceMappings() {
        return resourceMappings;
    }

    public ResourceMappingRegistry sortResourceMappings() {
        OrderUtils.reversedSort(resourceMappings);
        return this;
    }

}

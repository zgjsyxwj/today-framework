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
package cn.taketoday.web.resolver.result;

import cn.taketoday.context.annotation.Autowired;
import cn.taketoday.context.annotation.Env;
import cn.taketoday.context.annotation.Singleton;
import cn.taketoday.web.Constant;
import cn.taketoday.web.RequestContext;
import cn.taketoday.web.mapping.HandlerMethod;
import cn.taketoday.web.ui.ModelAndView;
import cn.taketoday.web.view.ViewResolver;

/**
 * @author TODAY <br>
 *         2019-07-14 00:53
 */
@Singleton
public class VoidResultResolver extends ModelAndViewResultResolver {

    @Override
    public boolean supports(HandlerMethod handlerMethod) {
        return handlerMethod.is(void.class);
    }

    @Autowired
    public VoidResultResolver(ViewResolver viewResolver, //
            @Env(value = Constant.DOWNLOAD_BUFF_SIZE, defaultValue = "10240") int downloadFileBuf) //
    {
        super(viewResolver, downloadFileBuf);
    }

    @Override
    public void resolveResult(RequestContext requestContext, Object result) throws Throwable {

        final ModelAndView modelAndView = requestContext.modelAndView();
        if (modelAndView != null) {
            resolveModelAndView(requestContext, modelAndView);
        }
    }

}

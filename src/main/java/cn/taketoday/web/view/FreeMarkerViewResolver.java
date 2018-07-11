/**
 * Original Author -> 杨海健 (taketoday@foxmail.com) https://taketoday.cn
 * Copyright © Today & 2017 - 2018 All Rights Reserved.
 * 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package cn.taketoday.web.view;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import cn.taketoday.web.config.ConfigurationFactory;
import freemarker.ext.jsp.TaglibFactory;
import freemarker.ext.servlet.HttpRequestHashModel;
import freemarker.ext.servlet.HttpRequestParametersHashModel;
import freemarker.ext.servlet.HttpSessionHashModel;
import freemarker.ext.servlet.ServletContextHashModel;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.ObjectWrapper;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Today
 * @date 2018年6月26日 下午7:16:46
 */
@Slf4j
public final class FreeMarkerViewResolver extends AbstractViewResolver {

	private ObjectWrapper		wrapper;
	private Configuration		configuration;
	private TaglibFactory		taglibFactory;

	public static final String	KEY_REQUEST				= "Request";
	public static final String	KEY_REQUEST_PRIVATE		= "__FreeMarkerServlet.Request__";
	public static final String	KEY_REQUEST_PARAMETERS	= "RequestParameters";
	public static final String	KEY_SESSION				= "Session";
	public static final String	KEY_APPLICATION			= "Application";
	public static final String	KEY_APPLICATION_PRIVATE	= "__FreeMarkerServlet.Application__";
	public static final String	KEY_JSP_TAGLIBS			= "JspTaglibs";

	@Override
	public void initViewResolver(ConfigurationFactory configurationFactory) {
		
		log.info("configuration freeMarker view resolver");
		init(configurationFactory);
		
		configuration = new Configuration();
		this.wrapper = new DefaultObjectWrapper();
		this.taglibFactory = new TaglibFactory(servletContext);
		
		configuration.setLocale(locale);
		configuration.setObjectWrapper(wrapper);
		configuration.setDefaultEncoding(encoding);
		configuration.setServletContextForTemplateLoading(servletContext, prefix); // path -> /WEB-INF/..
	}

	public FreeMarkerViewResolver() {

	}

	/**
	 * create Model Attributes
	 * @param request
	 * @param response
	 * @return
	 * @throws TemplateModelException
	 */
	private final TemplateModel createModel(HttpServletRequest request, HttpServletResponse response)
			throws TemplateModelException {

		AllScopesModel model = new AllScopesModel(wrapper, servletContext, request);

		// Create hash model wrapper for servlet context (the application)
		@SuppressWarnings("deprecation")
		ServletContextHashModel servletContextModel = new ServletContextHashModel(servletContext, wrapper);

		model.putUnlistedModel(KEY_APPLICATION, servletContextModel);
		model.putUnlistedModel(KEY_JSP_TAGLIBS, this.taglibFactory);

		// Create hash model wrapper for session
		HttpSession session = request.getSession();

		model.putUnlistedModel(KEY_SESSION, new HttpSessionHashModel(session, wrapper));
		// Create hash model wrapper for request
		model.putUnlistedModel(KEY_REQUEST, new HttpRequestHashModel(request, response, wrapper));
		model.putUnlistedModel(KEY_REQUEST_PARAMETERS, new HttpRequestParametersHashModel(request));

		return model;
	}


	/**
	 * FreeMarker View
	 */
	@Override
	public void resolveView(String templateName, HttpServletRequest request, HttpServletResponse response) throws Exception {

		configuration.getTemplate(templateName + suffix).process(
					createModel(request, response), response.getWriter());
	
	}

}

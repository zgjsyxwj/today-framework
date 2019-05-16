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
package cn.taketoday.context.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Objects;

import cn.taketoday.context.utils.ClassUtils;
import cn.taketoday.context.utils.ResourceUtils;

/**
 * @author TODAY <br>
 *         2019-05-14 21:47
 */
public class ClassPathResource implements Resource, WritableResource {

	private final Resource resource;

	public ClassPathResource(URL location) throws IOException {
		this.resource = ResourceUtils.getResource(location);
	}

	public ClassPathResource(String location) throws IOException {
		this(Objects.requireNonNull(ClassUtils.getClassLoader().getResource(location), location));
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return resource.getInputStream();
	}

	@Override
	public long contentLength() throws IOException {
		return resource.contentLength();
	}

	@Override
	public String getName() {
		return resource.getName();
	}

	@Override
	public long lastModified() throws IOException {
		return resource.lastModified();
	}

	@Override
	public URL getLocation() throws IOException {
		return resource.getLocation();
	}

	@Override
	public File getFile() throws IOException {
		return resource.getFile();
	}

	@Override
	public boolean exists() {
		return resource.exists();
	}

	@Override
	public boolean isDirectory() throws IOException {
		return resource.isDirectory();
	}

	@Override
	public String[] list() throws IOException {
		return resource.list();
	}

	@Override
	public Resource createRelative(String relativePath) throws IOException {
		return resource.createRelative(relativePath);
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		if (resource instanceof WritableResource) {
			return ((WritableResource) resource).getOutputStream();
		}
		throw new IOException("Writable operation is not supported");
	}

}

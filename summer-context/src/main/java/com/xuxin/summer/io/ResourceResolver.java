package com.xuxin.summer.io;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple classpath scan
 * 
 * @author 迷旅
 *
 */
public class ResourceResolver {

	Logger logger = LoggerFactory.getLogger(getClass());

	String basePackage;

	public ResourceResolver(String basePackage) {
		this.basePackage = basePackage;
	}

	public <R> List<R> scan(Function<Resource, R> mapper) {
		String basePackagePath = this.basePackage.replace(".", "/");
        try {
			List<R> collector = new ArrayList<>();
			scan0(basePackagePath, basePackagePath, collector, mapper);
			return collector;
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}

	}

	<R> void scan0(String basePackagePath, String path, List<R> collector, Function<Resource, R> mapper)
			throws IOException, URISyntaxException {
		
		logger.atDebug().log("scan path: {}", path);
		// 通过 ClassLoader 获取 URL列表
		Enumeration<URL> en = getContextClassLoader().getResources(path);
		while (en.hasMoreElements()) {
			URL url = en.nextElement();
			URI uri = url.toURI();
			String uriStr = removeTrailingSlash(uriToString(uri));
			String uriBaseStr = uriStr.substring(0, uriStr.length() - basePackagePath.length());
			if (uriBaseStr.startsWith("file:")) {
				uriBaseStr = uriBaseStr.substring(5);
			}
			if (uriStr.startsWith("jar:")) {
				scanFile(true, uriBaseStr, jarUriToPath(basePackagePath, uri), collector, mapper);
			} else {
				scanFile(false, uriBaseStr, Paths.get(uri), collector, mapper);
			}
		}
	}

	<R> void scanFile(boolean isJar, String base, Path root, List<R> collector, Function<Resource, R> mapper)
			throws IOException {
		String baseDir = removeTrailingSlash(base);
		Files.walk(root).filter(Files::isRegularFile).forEach(file -> {
			Resource res;
			if (isJar) {
				res = new Resource(baseDir, removeLeadingSlash(file.toString()));
			} else {
				String path = file.toString();
				String name = removeLeadingSlash(path.substring(baseDir.length()));
				res = new Resource("file:" + path, name);
			}
			logger.atDebug().log("found resource: {}", res);
			R r = mapper.apply(res);
			if (r != null) {
				collector.add(r);
			}
		});
	}
	
	/*
	 * 移除前导的斜线 
	 */
	String removeLeadingSlash(String s) {
		if (s.startsWith("/") || s.startsWith("\\")) {
			s = s.substring(1);
		}
		return s;
	}
	
	/*
	 * 移除末尾的斜线
	 */
	String removeTrailingSlash(String s) {
		if (s.endsWith("/") || s.endsWith("\\")) {
			s = s.substring(0, s.length() - 1);
		}
		return s;
	}

	Path jarUriToPath(String basePackagePath, URI jarUri) throws IOException {
		return FileSystems.newFileSystem(jarUri, Map.of()).getPath(basePackagePath);
	}

	String uriToString(URI uri) {
		return URLDecoder.decode(uri.toString(), StandardCharsets.UTF_8);
	}

	/*
	 * 考虑到Web应用ClassLoader是基于Servlet容器提供， 所以首先从`Thread.getContextClassLoader()`获取
	 */
	ClassLoader getContextClassLoader() {

		ClassLoader c1;
		c1 = Thread.currentThread().getContextClassLoader();
		if (c1 == null) {
			c1 = getClass().getClassLoader();
		}
		return c1;
	}
}

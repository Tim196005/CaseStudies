package io.casestudy.flight;

import io.vertx.core.json.pointer.JsonPointer;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ApplicationConstants {

	public static final String BASE_DIR = "BASE_DIR";
	public static final String JAVA_CONFIG_DIR = "JAVA_CONFIG_DIR";
	public static final String PATTERN = "pattern";
	public static final String CACHE = "cache";
	public static final String FILETYPE_PROPERTIES = "*.properties";
	public static final String PROPERTIES = "properties";
	public static final String DIRECTORY = "directory";
	public static final String FILESETS = "filesets";
	public static final String FORMAT = "format";
	
	public static final JsonPointer APPLICATION_NAME_POINTER = JsonPointer.from("/application/name");
	
	public static final JsonPointer SERVER_OPTIONS_POINTER = JsonPointer.from("/serverOptions");
	public static final JsonPointer HTTPCLIENT_OPTIONS_POINTER = JsonPointer.from("/httpClientOptions");
	
	public static final JsonPointer VERTX_OPTIONS_POINTER = JsonPointer.from("/vertxOptions");
	public static final JsonPointer CONFIGRETRIEVER_OPTIONS_POINTER = JsonPointer.from("/configRetrieverOptions");
	public static final JsonPointer EVENTBUS_OPTIONS_POINTER = JsonPointer.from("/eventBusOptions");
	
	public static final String API_CONFIG_DIR = "API_CONFIG_DIR";
	
}

package io.swagger.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.models.Swagger;
import io.swagger.models.auth.AuthorizationValue;
import io.swagger.parser.util.ClasspathHelper;
import io.swagger.parser.util.DeserializationUtils;
import io.swagger.parser.util.RemoteUrl;
import io.swagger.parser.util.SwaggerDeserializationResult;
import io.swagger.util.Json;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLHandshakeException;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class SwaggerDomainParser implements SwaggerParserExtension {

    private static final Logger LOGGER = LoggerFactory.getLogger(SwaggerDomainParser.class);
    private static final String HTTP = "http";
    private static final String FILE_SCHEME = "file:";

    @Override
    public SwaggerDeserializationResult readWithInfo(JsonNode node) {
        return new SwaggerDomainDeserializer()
                .deserialize(node);
    }

    @Override
    public SwaggerDeserializationResult readWithInfo(String location, List<AuthorizationValue> auths) {
        if(StringUtils.isEmpty(location)) {
            return new SwaggerDeserializationResult()
                  .message("location must be specified.");
        }
        try {
            final String data = getData(location, auths);
            final JsonNode rootNode = getRootNode(data);
            return readWithInfo(rootNode);
        } catch (SSLHandshakeException e) {
            SwaggerDeserializationResult output = new SwaggerDeserializationResult();
            output.message("unable to read location `" + location + "` due to a SSL configuration error.  " +
                    "It is possible that the server SSL certificate is invalid, self-signed, or has an untrusted " +
                    "Certificate Authority.");
            return output;
        } catch (Exception e) {
            SwaggerDeserializationResult output = new SwaggerDeserializationResult();
            output.message("unable to read location `" + location + "`");
            return output;
        }
    }

    @Override
    public Swagger read(String location, List<AuthorizationValue> auths) throws IOException {
        LOGGER.info("reading from " + location);
        try {
            String data;
            location = location.replaceAll("\\\\","/");
            if (location.toLowerCase().startsWith("http")) {
                data = RemoteUrl.urlToString(location, auths);
                return convertToSwagger(data);
            }
            final String fileScheme = "file:";
            final Path path;
            if (location.toLowerCase().startsWith(fileScheme)) {
                path = Paths.get(URI.create(location));
            } else {
                path = Paths.get(location);
            }
            if(Files.exists(path)) {
                data = FileUtils.readFileToString(path.toFile(), "UTF-8");
            } else {
                data = ClasspathHelper.loadFileFromClasspath(location);
            }
            return convertToSwagger(data);
        } catch (Exception e) {
            if (System.getProperty("debugParser") != null) {
                e.printStackTrace();
            }
            return null;
        }
    }

    @Override
    public Swagger read(JsonNode node) throws IOException {
        if (node == null) {
            return null;
        }
        return Json.mapper().convertValue(node, Swagger.class);
    }

    public Swagger parse(String data) throws Exception {
        Validate.notEmpty(data, "data must not be null!");
        return convertToSwagger(data);
    }

    protected String getData(String location, List<AuthorizationValue> auths) throws Exception {
        location = location.replaceAll("\\\\","/");

        if (location.toLowerCase().startsWith(HTTP)) {
            return RemoteUrl.urlToString(location, auths);
        }

        final Path path;

        if (location.toLowerCase().startsWith(FILE_SCHEME)) {
            path = Paths.get(URI.create(location));
        } else {
            path = Paths.get(location);
        }

        if (Files.exists(path)) {
            return FileUtils.readFileToString(path.toFile(), StandardCharsets.UTF_8);
        } else {
            return ClasspathHelper.loadFileFromClasspath(location);
        }
    }

    protected JsonNode getRootNode(String data) throws Exception {
        if (data.trim().startsWith("{")) {
            ObjectMapper mapper = Json.mapper();
            return mapper.readTree(data);
        }
        return DeserializationUtils.readYamlTree(data);
    }

    private Swagger convertToSwagger(String data) throws Exception {
        if (StringUtils.isBlank(data)) {
            return null;
        }
        final JsonNode rootNode = getRootNode(data);

        if (System.getProperty("debugParser") != null) {
            final String swaggerTree = ReflectionToStringBuilder.toString(rootNode, ToStringStyle.MULTI_LINE_STYLE);
            LOGGER.info(String.format("\n\nSwagger Tree: \n%s\n\n", swaggerTree));
        }
        if(rootNode == null) {
            return null;
        }
        // must have swagger node set
        JsonNode swaggerNode = rootNode.get("definitions");
        if (swaggerNode == null) {
            System.out.println("ta aki.....");
            return null;
        }
        SwaggerDeserializationResult result = readWithInfo(rootNode);
        Swagger convertValue = result.getSwagger();
        if (System.getProperty("debugParser") != null) {
            final String swaggerTree = ReflectionToStringBuilder.toString(rootNode, ToStringStyle.MULTI_LINE_STYLE);
            LOGGER.info(String.format("\n\nSwagger Tree convertValue: \n%s\n\n", swaggerTree));
        }
        return convertValue;
    }
}

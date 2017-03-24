package io.swagger.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.models.Model;
import io.swagger.models.Swagger;
import io.swagger.parser.util.SwaggerDeserializationResult;
import io.swagger.parser.util.SwaggerDeserializer;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

public class SwaggerDomainDeserializer extends SwaggerDeserializer {

    static String ROOT_KEY = "definitions";
    static String OBJECT_TYPE = "object";

    @Override
    public SwaggerDeserializationResult deserialize(JsonNode rootNode) {
        final SwaggerDeserializationResult result = new SwaggerDeserializationResult();
        final ParseResult parseResult = new ParseResult();
        final Swagger swagger = parseDefinition(rootNode, parseResult);
        result.setSwagger(swagger);
        result.setMessages(parseResult.getMessages());

        return result;
    }

    public Swagger parseDefinition(JsonNode jsonNode, ParseResult result) {
        if (!jsonNode.getNodeType().equals(JsonNodeType.OBJECT)) {
            result.invalidType(StringUtils.EMPTY, StringUtils.EMPTY, OBJECT_TYPE, jsonNode);
            result.invalid();
            return null;
        }
        String location = StringUtils.EMPTY;
        final Swagger swagger = new Swagger();
        final ObjectNode objectNode = (ObjectNode) jsonNode;
        final ObjectNode definitionsNode = getObject("definitions", objectNode, true, location, result);
        final Map<String, Model> definitions = definitions(definitionsNode, "definitions", result);
        swagger.setDefinitions(definitions);

        return swagger;
    }
}

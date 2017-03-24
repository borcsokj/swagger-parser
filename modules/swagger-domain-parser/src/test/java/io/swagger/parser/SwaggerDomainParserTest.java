package io.swagger.parser;

import io.swagger.models.Model;
import io.swagger.models.Swagger;
import io.swagger.models.properties.Property;
import org.apache.commons.io.FileUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.util.Map;

public class SwaggerDomainParserTest {

    @Test
    public void testDefinitions() throws Exception {
        String yaml = FileUtils.readFileToString(new File("src/test/resources/petstore-definition.yaml"));
        Swagger swagger = new SwaggerDomainParser().parse(yaml);

        Assert.assertNotNull(swagger);
        Assert.assertNotNull(swagger.getDefinitions());

        Map<String, Property> propertyMap;

        final Model order = swagger.getDefinitions().get("Order");
        Assert.assertNotNull(order);
        propertyMap = order.getProperties();
        Assert.assertNotNull(propertyMap);
        Assert.assertEquals(propertyMap.size(), 6);

        final Model category = swagger.getDefinitions().get("Category");
        Assert.assertNotNull(category);
        propertyMap = category.getProperties();
        Assert.assertNotNull(propertyMap);
        Assert.assertEquals(propertyMap.size(), 2);

        final Model user = swagger.getDefinitions().get("User");
        Assert.assertNotNull(user);
        propertyMap = user.getProperties();
        Assert.assertNotNull(propertyMap);
        Assert.assertEquals(propertyMap.size(), 8);

        final Model tag = swagger.getDefinitions().get("Tag");
        Assert.assertNotNull(tag);
        propertyMap = tag.getProperties();
        Assert.assertNotNull(propertyMap);
        Assert.assertEquals(propertyMap.size(), 2);

        final Model pet = swagger.getDefinitions().get("Pet");
        Assert.assertNotNull(pet);
        propertyMap = pet.getProperties();
        Assert.assertNotNull(propertyMap);
        Assert.assertEquals(propertyMap.size(), 6);

        final Model apiResponse = swagger.getDefinitions().get("ApiResponse");
        Assert.assertNotNull(apiResponse);
        propertyMap = apiResponse.getProperties();
        Assert.assertNotNull(propertyMap);
        Assert.assertEquals(propertyMap.size(), 3);

    }
}

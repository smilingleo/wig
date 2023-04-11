package me.smilingleo;

import me.smilingleo.commands.Compose;
import me.smilingleo.exceptions.ValidationException;

import org.junit.Test;

public class RenderContextTest {

    @Test(expected = ValidationException.class)
    public void testDuplicateNewObject() {
        RenderContext context = new RenderContext();
        // define two objects with same name.
        Compose.parse("Cmd_ListToDict(default__messageses|FilterByValue(locale__c,EQ,zh_CN),key__c,value__c,Message)");
        Compose.parse("Cmd_ListToDict(default__messageses|FilterByValue(locale__c,EQ,en_US),key__c,value__c,Message)");
    }

    @Test(expected = ValidationException.class)
    public void testCanNotUseStandardObjectName() {
        RenderContext context = new RenderContext();
        // define two objects with same name.
        Compose.parse("Cmd_ListToDict(default__messageses|FilterByValue(locale__c,EQ,zh_CN),key__c,value__c,Invoice)");
    }
}

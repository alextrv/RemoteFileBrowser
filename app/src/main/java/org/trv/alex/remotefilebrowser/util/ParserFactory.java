package org.trv.alex.remotefilebrowser.util;

import org.trv.alex.remotefilebrowser.util.parser.Parser;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class ParserFactory {

    private static Map<String, Parser> mParsers = new HashMap<>();

    private ParserFactory() {
    }

    public static Parser getInstance(String name) {
        if (mParsers.containsKey(name)) {
            return mParsers.get(name);
        } else {
            try {
                Class<?> clazz = Class.forName(
                        ParserFactory.class.getPackage().getName()
                                + ".parser."
                                + name);
                Parser parser = (Parser) clazz.getConstructor().newInstance();
                mParsers.put(name, parser);
                return parser;
            } catch (ClassNotFoundException
                    | NoSuchMethodException
                    | InstantiationException
                    | IllegalAccessException
                    | InvocationTargetException e) {
                e.printStackTrace();
                return null;
            }
        }
    }

}

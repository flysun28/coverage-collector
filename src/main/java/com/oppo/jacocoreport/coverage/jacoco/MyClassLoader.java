package com.oppo.jacocoreport.coverage.jacoco;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MyClassLoader extends ClassLoader {
    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        byte[] classBytes = null;
        Path path = null;
        try{
            path = Paths.get(new URI(name));
            classBytes = Files.readAllBytes(path);
        }catch (IOException | URISyntaxException e){
            e.printStackTrace();
        }
        Class clazz = defineClass(name,classBytes,0,classBytes.length);
        return clazz;
    }
}

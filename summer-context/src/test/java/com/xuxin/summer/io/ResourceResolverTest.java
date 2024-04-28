package com.xuxin.summer.io;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.sql.DataSourceDefinition;
import jakarta.annotation.sub.AnnoScan;

public class ResourceResolverTest {

    @Test
    public void scanClass() {
        var pkg = "com.xuxin.scan";
        var rr = new ResourceResolver(pkg);
        List<String> classes = rr.scan(res -> {
            String name = res.name();
            if (name.endsWith(".class")) {
                return name.substring(0, name.length() - 6).replace("/", ".").replace("\\", ".");
            }
            return null;
        });
        Collections.sort(classes);
        System.out.println(classes);
        String[] listClasses = new String[] {
                // list of some scan classes:
                "com.xuxin.scan.convert.ValueConverterBean", //
                "com.xuxin.scan.destroy.AnnotationDestroyBean", //
                "com.xuxin.scan.init.SpecifyInitConfiguration", //
                "com.xuxin.scan.proxy.OriginBean", //
                "com.xuxin.scan.proxy.FirstProxyBeanPostProcessor", //
                "com.xuxin.scan.proxy.SecondProxyBeanPostProcessor", //
                "com.xuxin.scan.nested.OuterBean", //
                "com.xuxin.scan.nested.OuterBean$NestedBean", //
                "com.xuxin.scan.sub1.Sub1Bean", //
                "com.xuxin.scan.sub1.sub2.Sub2Bean", //
                "com.xuxin.scan.sub1.sub2.sub3.Sub3Bean", //
        };
        for (String clazz : listClasses) {
            assertTrue(classes.contains(clazz));
        }
    }

    @Test
    public void scanJar() {
        var pkg = PostConstruct.class.getPackageName();
        var rr = new ResourceResolver(pkg);
        List<String> classes = rr.scan(res -> {
            String name = res.name();
            if (name.endsWith(".class")) {
                return name.substring(0, name.length() - 6).replace("/", ".").replace("\\", ".");
            }
            return null;
        });
        // classes in jar:
        assertTrue(classes.contains(PostConstruct.class.getName()));
        assertTrue(classes.contains(PreDestroy.class.getName()));
        assertTrue(classes.contains(PermitAll.class.getName()));
        assertTrue(classes.contains(DataSourceDefinition.class.getName()));
        // jakarta.annotation.sub.AnnoScan is defined in classes:
        assertTrue(classes.contains(AnnoScan.class.getName()));
    }

    @Test
    public void scanTxt() {
        var pkg = "com.xuxin.scan";
        var rr = new ResourceResolver(pkg);
        List<String> classes = rr.scan(res -> {
            String name = res.name();
            if (name.endsWith(".txt")) {
                return name.replace("\\", "/");
            }
            return null;
        });
        Collections.sort(classes);
        assertArrayEquals(new String[] {
                // txt files:
                "com/xuxin/scan/sub1/sub1.txt", //
                "com/xuxin/scan/sub1/sub2/sub2.txt", //
                "com/xuxin/scan/sub1/sub2/sub3/sub3.txt", //
        }, classes.toArray(String[]::new));
    }
}

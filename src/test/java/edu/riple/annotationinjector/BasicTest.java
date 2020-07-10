package edu.riple.annotationinjector;

import edu.riple.annotationinjector.tools.InjectorTestHelper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;


@RunWith(JUnit4.class)
public class BasicTest {

    Injector injector;
    String fixPath = "/Users/nima/Developer/AnnotationInjector/testfiles/fixes.json";

    @Before
    public void setup() {
        injector = Injector.builder(Injector.MODE.OVERWRITE).setFixesJsonFilePath(fixPath).build();

    }

    @Test
    public void basicTest() {
        injector.start();
        System.out.println("Finished");
    }

    @Test
    public void return_nullable_simple() {
        String rootName = "return_nullable_simple";
        new InjectorTestHelper()
                .setRootPath(System.getProperty("user.dir") + "/tests/" + rootName)
                .addInput(
                    "Super.java",
                    "package com.uber;",
                    "public class Super {",
                    "   Object test(boolean flag) {",
                    "       return new Object();",
                    "   }",
                    "}"
                )
                .expectOutput(
                    "Super.java",
                    "package com.uber;",
                    "import javax.Nullable;",
                    "public class Super {",
                    "   @Nullable",
                    "   Object test(boolean flag) {",
                    "       return new Object();",
                    "   }",
                    "}"
                ).addInput(
                    "com/Superb.java",
                    "package com.uber;",
                    "public class Superb {",
                    "   Object test(boolean flag) {",
                    "       return new Object();",
                    "   }",
                    "}"
                )
                .expectOutput(
                    "com/Superb.java",
                    "package com.uber;",
                    "import javax.Nullable;",
                    "public class Superb {",
                    "   @Nullable",
                    "   Object test(boolean flag) {",
                    "       return new Object();",
                    "   }",
                    "}"
                )
                .addFixes(new Fix(
                    "javax.annotation.Nullable",
                    "test(boolean)",
                    "",
                    "METHOD_RETURN",
                    "",
                    "com.uber.Super",
                    "com.uber",
                    "Super.java",
                    "true"
                        ),
                        new Fix(
                    "javax.annotation.Nullable",
                    "test(boolean)",
                    "",
                    "METHOD_RETURN",
                    "",
                    "com.uber.Superb",
                    "com.uber",
                    "com/Superb.java",
                    "true"
                        )
                ).start();
    }
}

package edu.riple.annotationinjector;

import edu.riple.annotationinjector.tools.InjectorTestHelper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;


@RunWith(JUnit4.class)
public class BasicTest {

    @Before
    public void setup() {

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
                        "import javax.annotation.Nullable;",
                        "public class Super {",
                        "   @Nullable",
                        "   Object test(boolean flag) {",
                        "       return new Object();",
                        "   }",
                        "}"
                ).addInput(
                "com/Superb.java",
                "package com.uber;",
                "public class Superb extends Super {",
                "   Object test(boolean flag) {",
                "       return new Object();",
                "   }",
                "}"
        )
                .expectOutput(
                        "com/Superb.java",
                        "package com.uber;",
                        "import javax.annotation.Nullable;",
                        "public class Superb extends Super{",
                        "   @Nullable",
                        "   Object test(boolean flag) {",
                        "       return new Object();",
                        "   }",
                        "}"
                )
                .addFixes(
                        new Fix(
                                "javax.annotation.Nullable",
                                "test(boolean)",
                                "",
                                "METHOD_RETURN",
                                "",
                                "com.uber.Super",
                                "com.uber",
                                "Super.java",
                                "true")
                        ,
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

    @Test
    public void add_nullable_param_simple() {
        String rootName = "add_nullable_param_simple";

        new InjectorTestHelper()
                .setRootPath(System.getProperty("user.dir") + "/tests/" + rootName)
                .addInput(
                        "Super.java",
                        "package com.uber;",
                        "import javax.annotation.Nullable;",
                        "public class Super {",
                        "   @Nullable Object test(Object flag) {",
                        "       if(flag == null) {",
                        "           return new Object();",
                        "       }",
                        "       else return new Object();",
                        "   }",
                        "}"
                )
                .expectOutput(
                        "Super.java",
                        "package com.uber;",
                        "import javax.annotation.Nullable;",
                        "public class Super {",
                        "   @Nullable Object test(@Nullable Object flag) {",
                        "       if(flag == null) {",
                        "           return new Object();",
                        "       }",
                        "       else return new Object();",
                        "   }",
                        "}"
                )
                .addFixes(new Fix(
                        "javax.annotation.Nullable",
                        "test(java.lang.Object)",
                        "flag",
                        "METHOD_PARAM",
                        "",
                        "com.uber.Super",
                        "com.uber",
                        "Super.java",
                        "true")
                ).start();
    }

    @Test
    public void add_nullable_param_simple_2() {
        String rootName = "add_nullable_param_simple_2";

        new InjectorTestHelper()
                .setRootPath(System.getProperty("user.dir") + "/tests/" + rootName)
                .addInput(
                        "Super.java",
                        "package com.uber;",
                        "import javax.annotation.Nullable;",
                        "public class Super {",
                        "   @Nullable Object test(Object flag) {",
                        "       if(flag == null) {",
                        "           return new Object();",
                        "       }",
                        "       else return new Object();",
                        "   }",
                        "}"
                )
                .expectOutput(
                        "Super.java",
                        "package com.uber;",
                        "import javax.annotation.Nullable;",
                        "public class Super {",
                        "   @Nullable Object test(@Nullable Object flag) {",
                        "       if(flag == null) {",
                        "           return new Object();",
                        "       }",
                        "       else return new Object();",
                        "   }",
                        "}"
                )
                .addFixes(new Fix(
                        "javax.annotation.Nullable",
                        "test(java.lang.Object)",
                        "flag",
                        "METHOD_PARAM",
                        "@Nullable",
                        "com.uber.Super",
                        "com.uber",
                        "Super.java",
                        "true")
                ).start();
    }
}

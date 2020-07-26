package edu.riple.annotationinjector;

import edu.riple.annotationinjector.tools.InjectorTestHelper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class BasicTest {
  // When applying a series of fixes to the same file, for each fix's uri, give the output address
  // of the previous fix to keep the changes of previous processing
  // In this case, for the second and the rest, "../out/" should be added at the beginning of every
  // uri. (See return_nullable_signature_duplicate_type test)

  @Before
  public void setup() {}

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
            "}")
        .expectOutput(
            "Super.java",
            "package com.uber;",
            "import javax.annotation.Nullable;",
            "public class Super {",
            "   @Nullable",
            "   Object test(boolean flag) {",
            "       return new Object();",
            "   }",
            "}")
        .addInput(
            "com/Superb.java",
            "package com.uber;",
            "public class Superb extends Super {",
            "   Object test(boolean flag) {",
            "       return new Object();",
            "   }",
            "}")
        .expectOutput(
            "com/Superb.java",
            "package com.uber;",
            "import javax.annotation.Nullable;",
            "public class Superb extends Super{",
            "   @Nullable",
            "   Object test(boolean flag) {",
            "       return new Object();",
            "   }",
            "}")
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
                "true"),
            new Fix(
                "javax.annotation.Nullable",
                "test(boolean)",
                "",
                "METHOD_RETURN",
                "",
                "com.uber.Superb",
                "com.uber",
                "com/Superb.java",
                "true"))
        .start();
  }

  @Test
  public void return_nullable_inner_class() {
    String rootName = "return_nullable_inner_class";

    new InjectorTestHelper()
        .setRootPath(System.getProperty("user.dir") + "/tests/" + rootName)
        .addInput(
            "Super.java",
            "package com.uber;",
            "import javax.annotation.Nullable;",
            "public class Super {",
            "   @Nullable",
            "   Object test(boolean flag) {",
            "       return new Object();",
            "   }",
            "   class SuperInner {",
            "   Object bar(@Nullable Object foo) {",
            "       return foo;",
            "   }",
            "}")
        .expectOutput(
            "Super.java",
            "package com.uber;",
            "import javax.annotation.Nullable;",
            "public class Super {",
            "   @Nullable",
            "   Object test(boolean flag) {",
            "       return new Object();",
            "   }",
            "   class SuperInner {",
            "       @Nullable",
            "       Object bar(@Nullable Object foo) {",
            "           return foo;",
            "       }",
            "   }",
            "}")
        .addFixes(
            new Fix(
                "javax.annotation.Nullable",
                "bar(java.lang.Object)",
                "",
                "METHOD_RETURN",
                "",
                "com.uber.Super.SuperInner",
                "com.uber",
                "Super.java",
                "true"))
        .start();
  }

  @Test
  public void return_nullable_signature_duplicate_type() {
    String rootName = "return_nullable_signature_duplicate_type";

    new InjectorTestHelper()
            .setRootPath(System.getProperty("user.dir") + "/tests/" + rootName)
            .addInput(
                    "Super.java",
                    "package com.uber;",
                    "public class Super {",
                    "   Object test(Object flag, String name, String lastname) {",
                    "       if(flag == null) {",
                    "           return new Object();",
                    "       }",
                    "       else return new Object();",
                    "   }",
                    "   Object test(Object flag, Object name, String lastname) {",
                    "       if(flag == null) {",
                    "           return new Object();",
                    "       }",
                    "       else return new Object();",
                    "   }",
                    "}")
            .expectOutput(
                    "Super.java",
                    "package com.uber;",
                    "import javax.annotation.Nullable;",
                    "public class Super {",
                    "   @Nullable Object test(Object flag, String name, String lastname) {",
                    "       if(flag == null) {",
                    "           return new Object();",
                    "       }",
                    "       else return new Object();",
                    "   }",
                    "   Object test(Object flag, @Nullable Object name, String lastname) {",
                    "       if(flag == null) {",
                    "           return new Object();",
                    "       }",
                    "       else return new Object();",
                    "   }",
                    "}")
            .addFixes(
                    new Fix(
                            "javax.annotation.Nullable",
                            "test(Object, String, String)",
                            "",
                            "METHOD_RETURN",
                            "",
                            "com.uber.Super",
                            "com.uber",
                            "Super.java",
                            "true"),
                    new Fix(
                            "javax.annotation.Nullable",
                            "test(Object, Object, String)",
                            "name",
                            "METHOD_PARAM",
                            "",
                            "com.uber.Super",
                            "com.uber",
                            "../out/Super.java",
                            "true"))
            .start();
  }

  @Test
  public void param_nullable_simple() {
    String rootName = "param_nullable_simple";

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
            "}")
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
            "}")
        .addFixes(
            new Fix(
                "javax.annotation.Nullable",
                "test(java.lang.Object)",
                "flag",
                "METHOD_PARAM",
                "",
                "com.uber.Super",
                "com.uber",
                "Super.java",
                "true"))
        .start();
  }

  @Test
  public void param_nullable_signature_incomplete() {
    String rootName = "param_nullable_signature_incomplete";

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
            "}")
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
            "}")
        .addFixes(
            new Fix(
                "javax.annotation.Nullable",
                "test(Object)",
                "flag",
                "METHOD_PARAM",
                "@Nullable",
                "com.uber.Super",
                "com.uber",
                "Super.java",
                "true"))
        .start();
  }

  @Test
  public void param_nullable_interface() {
    String rootName = "param_nullable_interface";

    new InjectorTestHelper()
        .setRootPath(System.getProperty("user.dir") + "/tests/" + rootName)
        .addInput(
            "SSAInstructionFactory.java",
            "package com.uber;",
            "public interface SSAInstructionFactory {",
            "SSAAbstractInvokeInstruction InvokeInstruction(",
            "   int index,",
            "   int result,",
            "   int[] params,",
            "   int exception,",
            "   CallSiteReference site,",
            "   BootstrapMethod bootstrap);",
            "",
            "SSAAbstractInvokeInstruction InvokeInstruction(",
            "   int index, int[] params, int exception, CallSiteReference site, BootstrapMethod bootstrap);",
            "}")
        .expectOutput(
                "SSAInstructionFactory.java",
                "package com.uber;",
                "import javax.annotation.Nullable;",
                "public interface SSAInstructionFactory {",
                "SSAAbstractInvokeInstruction InvokeInstruction(",
                "   int index,",
                "   int result,",
                "   int[] params,",
                "   int exception,",
                "   CallSiteReference site,",
                "   @Nullable BootstrapMethod bootstrap);",
                "",
                "SSAAbstractInvokeInstruction InvokeInstruction(",
                "   int index, int[] params, int exception, CallSiteReference site, BootstrapMethod bootstrap);",
                "}")
        .addFixes(
            new Fix(
                "javax.annotation.Nullable",
                "InvokeInstruction(int,int,int[],int,com.ibm.wala.classLoader.CallSiteReference,com.ibm.wala.shrikeCT.BootstrapMethodsReader.BootstrapMethod)",
                "bootstrap",
                "METHOD_PARAM",
                "",
                "com.uber.SSAInstructionFactory",
                "com.uber",
                "SSAInstructionFactory.java",
                "true"))
        .start();
  }

  @Test
  public void param_nullable_generics_simple() {
    String rootName = "param_nullable_generics_simple";

    new InjectorTestHelper()
        .setRootPath(System.getProperty("user.dir") + "/tests/" + rootName)
        .addInput(
            "ModRef.java",
            "package com.uber;",
            "public class ModRef<T extends InstanceKey> {",
            "   public Map<CGNode, OrdinalSet<PointerKey>> computeMod(",
            "     CallGraph cg, PointerAnalysis<T> pa, HeapExclusions heapExclude) {",
            "     if (cg == null) {",
            "       throw new IllegalArgumentException(\"cg is null\");",
            "     }",
            "     Map<CGNode, Collection<PointerKey>> scan = scanForMod(cg, pa, heapExclude);",
            "     return CallGraphTransitiveClosure.transitiveClosure(cg, scan);",
            "   }",
            "}")
        .expectOutput(
            "ModRef.java",
            "package com.uber;",
            "import javax.annotation.Nullable;",
            "public class ModRef<T extends InstanceKey> {",
            "   public Map<CGNode, OrdinalSet<PointerKey>> computeMod(",
            "     CallGraph cg, PointerAnalysis<T> pa, @Nullable HeapExclusions heapExclude) {",
            "     if (cg == null) {",
            "       throw new IllegalArgumentException(\"cg is null\");",
            "     }",
            "     Map<CGNode, Collection<PointerKey>> scan = scanForMod(cg, pa, heapExclude);",
            "     return CallGraphTransitiveClosure.transitiveClosure(cg, scan);",
            "   }",
            "}")
        .addFixes(
            new Fix(
                "javax.annotation.Nullable",
                "computeMod(com.ibm.wala.ipa.callgraph.CallGraph,com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis<T>,com.ibm.wala.ipa.slicer.HeapExclusions)",
                "heapExclude",
                "METHOD_PARAM",
                "",
                "com.uber.ModRef",
                "com.uber",
                "ModRef.java",
                "true"))
        .start();
  }

  @Test
  public void field_nullable_simple() {
    String rootName = "field_nullable_simple";

    new InjectorTestHelper()
        .setRootPath(System.getProperty("user.dir") + "/tests/" + rootName)
        .addInput(
            "Super.java",
            "package com.uber;",
            "import javax.annotation.Nullable;",
            "public class Super {",
            "   Object h = new Object();",
            "   public void test(@Nullable Object f) {",
            "      h = f;",
            "   }",
            "}")
        .expectOutput(
            "Super.java",
            "package com.uber;",
            "import javax.annotation.Nullable;",
            "public class Super {",
            "   @Nullable Object h = new Object();",
            "   public void test(@Nullable Object f) {",
            "      h = f;",
            "   }",
            "}")
        .addFixes(
            new Fix(
                "javax.annotation.Nullable",
                "",
                "h",
                "CLASS_FIELD",
                "",
                "com.uber.Super",
                "com.uber",
                "Super.java",
                "true"))
        .start();
  }

  @Test
  public void empty_method_param_pick() {
    String rootName = "empty_method_param_pick";

    new InjectorTestHelper()
        .setRootPath(System.getProperty("user.dir") + "/tests/" + rootName)
        .addInput(
            "Super.java",
            "package com.uber;",
            "import javax.annotation.Nullable;",
            "public class Super {",
            "   Object test() {",
            "       return new Object();",
            "   }",
            "   class SuperInner {",
            "       Object bar(@Nullable Object foo) {",
            "           return foo;",
            "       }",
            "   }",
            "}")
        .expectOutput(
            "Super.java",
            "package com.uber;",
            "import javax.annotation.Nullable;",
            "public class Su  per {",
            "   @Nullable",
            "   Object test() {",
            "       return new Object();",
            "   }",
            "   class SuperInner {",
            "       Object bar(@Nullable Object foo) {",
            "           return foo;",
            "       }",
            "   }",
            "}")
        .addFixes(
            new Fix(
                "javax.annotation.Nullable",
                "test()",
                "",
                "METHOD_RETURN",
                "",
                "com.uber.Super",
                "com.uber",
                "Super.java",
                "true"))
        .start();
  }

  @Test
  public void skip_duplicate_annotation() {
    String rootName = "skip_duplicate_annotation";

    Fix fix =
        new Fix(
            "javax.annotation.Nullable",
            "test()",
            "",
            "METHOD_RETURN",
            "",
            "com.uber.Super",
            "com.uber",
            "Super.java",
            "true");

    new InjectorTestHelper()
        .setRootPath(System.getProperty("user.dir") + "/tests/" + rootName)
        .addInput(
            "Super.java",
            "package com.uber;",
            "import javax.annotation.Nullable;",
            "public class Super {",
            "   @Nullable Object test() {",
            "       return new Object();",
            "   }",
            "}")
        .expectOutput(
            "Super.java",
            "package com.uber;",
            "import javax.annotation.Nullable;",
            "public class Super {",
            "   @Nullable Object test() {",
            "       return new Object();",
            "   }",
            "}")
        .addFixes(fix, fix.duplicate(), fix.duplicate())
        .start();
  }
}

// todo: test these later:

//  For method pick (multiple Generics)
//  public <A extends AbstractValue<A>, S extends Store<S>, T extends TransferFunction<A, S>>
//  S test(TransferFunction<A, S> s, Context context, T transfer) {
//    return null;
//  }

// For method pick (For Generics as argument)
// ArrayList<String> vs ArrayList<Object>

//  For method pick (For Class<T> as argument
//  public static <T extends Shape> void drawWithShadow(T shape, Class<T> shapeClass) {
//    // The shadow must be the same shape as what's passed in
//    T shadow = shapeClass.newInstance();
//    // Set the shadow's properties to from the shape...
//    shadow.draw(); // First, draw the shadow
//    shape.draw();  // Now draw the shape on top of it
//  }

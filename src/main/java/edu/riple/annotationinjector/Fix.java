package edu.riple.annotationinjector;

import org.json.simple.JSONObject;

import java.util.Objects;

public class Fix {
    public final String annotation;
    public final String method;
    public final String param;
    public final String location;
    public final String modifiers;
    public final String className;
    public final String pkg;
    public final String inject;
    String uri;


    enum KEYS {
        PARAM("param"),
        METHOD("method"),
        LOCATION("location"),
        MODIFIERS("modifiers"),
        CLASS("class"),
        PKG("pkg"),
        URI("uri"),
        INJECT("inject"),
        ANNOTATION("annotation");
        public final String label;

        KEYS(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return "KEYS{" + "label='" + label + '\'' + '}';
        }
    }

    public Fix(
            String annotation,
            String method,
            String param,
            String location,
            String modifiers,
            String className,
            String pkg,
            String uri,
            String inject) {
        this.annotation = annotation;
        this.method = method;
        this.param = param;
        this.location = location;
        this.modifiers = modifiers;
        this.className = className;
        this.pkg = pkg;
        this.uri = uri;
        this.inject = inject;
    }

    static Fix createFromJson(JSONObject fix) {
        return new Fix(
                fix.get(KEYS.ANNOTATION.label).toString(),
                fix.get(KEYS.METHOD.label).toString(),
                fix.get(KEYS.PARAM.label).toString(),
                fix.get(KEYS.LOCATION.label).toString(),
                fix.get(KEYS.MODIFIERS.label).toString(),
                fix.get(KEYS.CLASS.label).toString(),
                fix.get(KEYS.PKG.label).toString(),
                fix.get(KEYS.URI.label).toString(),
                fix.get(KEYS.INJECT.label).toString());
    }

    @Override
    public String toString() {
        return "Fix{"
                + "annotation='"
                + annotation
                + '\''
                + ", method='"
                + method
                + '\''
                + ", param='"
                + param
                + '\''
                + ", location='"
                + location
                + '\''
                + ", modifiers='"
                + modifiers
                + '\''
                + ", className='"
                + className
                + '\''
                + ", pkg='"
                + pkg
                + '\''
                + ", inject='"
                + inject
                + '\''
                + ", uri='"
                + uri
                + '\''
                + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Fix)) return false;
        Fix fix = (Fix) o;
        return Objects.equals(annotation, fix.annotation)
                && Objects.equals(method, fix.method)
                && Objects.equals(param, fix.param)
                && Objects.equals(location, fix.location)
                && Objects.equals(modifiers, fix.modifiers)
                && Objects.equals(className, fix.className)
                && Objects.equals(pkg, fix.pkg)
                && Objects.equals(inject, fix.inject)
                && Objects.equals(uri, fix.uri);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                annotation, method, param, location, modifiers, className, pkg, inject, uri);
    }
}

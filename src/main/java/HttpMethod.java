public enum HttpMethod {
    GET, POST, PUT, PATCH, OPTIONS, DELETE;

    public static HttpMethod byName(String str) {
        for (HttpMethod method: HttpMethod.values()) {
            if (method.name().equalsIgnoreCase(str)) {
                return method;
            }
        }
        throw new IllegalArgumentException("Wrong method type passed!");
    }
}
package com.recruitment.context;

public final class UserContextHolder {

    private static final ThreadLocal<UserContext> HOLDER = new ThreadLocal<>();

    private UserContextHolder() {
    }

    public static void set(UserContext context) {
        HOLDER.set(context);
    }

    public static UserContext get() {
        return HOLDER.get();
    }

    public static void clear() {
        HOLDER.remove();
    }

    public static UserContext require() {
        UserContext context = HOLDER.get();
        if (context == null) {
            throw new com.recruitment.exception.BusinessException(401, "请先登录");
        }
        return context;
    }
}

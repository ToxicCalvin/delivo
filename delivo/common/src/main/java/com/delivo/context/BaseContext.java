package com.delivo.context;

public class BaseContext {

    public static ThreadLocal<Long> threadLocal = new ThreadLocal<>();
    public static ThreadLocal<Integer> roleThreadLocal = new ThreadLocal<>();
    public static ThreadLocal<String> langThreadLocal = new ThreadLocal<>();

    public static void setCurrentId(Long id) {
        threadLocal.set(id);
    }

    public static Long getCurrentId() {
        return threadLocal.get();
    }

    public static void removeCurrentId() {
        threadLocal.remove();
    }

    public static void setCurrentRole(Integer role) {
        roleThreadLocal.set(role);
    }

    public static Integer getCurrentRole() {
        return roleThreadLocal.get();
    }

    public static void removeCurrentRole() {
        roleThreadLocal.remove();
    }

    public static void setCurrentLang(String lang) {
        langThreadLocal.set(lang);
    }

    public static String getCurrentLang() {
        String lang = langThreadLocal.get();
        return lang != null ? lang : "en";
    }

    public static void removeCurrentLang() {
        langThreadLocal.remove();
    }

    public static boolean isZh() {
        return "zh".equalsIgnoreCase(getCurrentLang());
    }

}

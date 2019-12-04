package com.alexii.j2v8debuggerJ;

public final class DebuggerExtensions {
   private static final String scriptsDomain = "http://app/";

   private static String getScriptsUrlBase() {
      return scriptsDomain + StethoHelper.INSTANCE.getScriptsPathPrefix();
   }

   private static String scriptIdToUrl(String scriptId) {
      return getScriptsUrlBase() + scriptId;
   }

   private static String urlToScriptId(String url) {
      return url != null ? removePrefix(url, getScriptsUrlBase()) : null;
   }

   public static String getScriptIdToUrlResult(String scriptId) {
      return scriptIdToUrl(scriptId);
   }

   public static String getUrlToScriptIdResult(String url) {
      return urlToScriptId(url);
   }

   public static String removePrefix(String url, String prefix)  {
      if (url.startsWith(prefix)) {
         return url.substring(prefix.length());
      }
      return url;
   }
}
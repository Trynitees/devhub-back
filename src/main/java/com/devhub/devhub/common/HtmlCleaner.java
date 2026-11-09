package com.devhub.devhub.common;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Component;

@Component
public class HtmlCleaner {

    private static final Safelist SAFELIST = Safelist.none()
            .addTags("p", "br", "strong", "ul", "ol","em", "li", "a", "img", "h1", "h2", "h3", "blockquote", "code", "pre")
            .addAttributes("a", "href", "title")
            .addAttributes("img", "src", "alt")
            .addProtocols("a", "href", "http", "https", "mailto")
            .addProtocols("img", "src", "http", "https");

    public String clean(String html){
        if (html == null){
            return "";
        }
        return Jsoup.clean(html, "", SAFELIST, new Document.OutputSettings().prettyPrint(false));
    }
}

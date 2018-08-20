package com.viewol.crawl.common.utils;

import com.youguu.core.logging.Log;
import com.youguu.core.logging.LogFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;

public class SampleHTMLUtils {
    private static final Log log = LogFactory.getLog("viewol_crawl");

    private static String html = null;
    private static Document doc = null;
    private static final String PATH = "sample.html";

    static {
        try (InputStream inputStream = new ClassPathResource(PATH).getInputStream();){
            html = FileUtils.loadInput(inputStream);
            doc = Jsoup.parse(html);
        } catch (Exception e) {
            log.error("Failed to init， exception={}", e);
        }
    }

    public static String getSampleHTML() {
        return html;
    }

    public static Document getSampleDocument() {
        return doc.clone();
    }

}

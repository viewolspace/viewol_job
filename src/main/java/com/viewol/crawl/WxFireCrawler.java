package com.viewol.crawl;

import cn.edu.hfut.dmic.webcollector.model.CrawlDatum;
import cn.edu.hfut.dmic.webcollector.model.CrawlDatums;
import cn.edu.hfut.dmic.webcollector.model.Page;
import cn.edu.hfut.dmic.webcollector.net.HttpRequest;
import cn.edu.hfut.dmic.webcollector.plugin.berkeley.BreadthCrawler;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.viewol.crawl.common.WxCrawlerConstant;
import com.viewol.crawl.common.utils.DateTimeUtils;
import com.viewol.crawl.common.utils.ResourceTransferUtils;
import com.viewol.crawl.common.utils.SampleHTMLUtils;
import com.viewol.crawl.pojo.ArticleSummary;
import com.viewol.crawl.pojo.CommMsgInfo;
import com.viewol.crawl.pojo.MsgExtInfo;
import com.viewol.crawl.proxy.Proxy;
import com.viewol.pojo.Info;
import com.youguu.core.logging.Log;
import com.youguu.core.logging.LogFactory;
import com.youguu.core.pojo.Response;
import com.youguu.core.util.HttpUtil;
import com.youguu.core.util.MD5;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;


public class WxFireCrawler extends BreadthCrawler {
    private static final Log log = LogFactory.getLog("viewol_crawl");
    private static final String URL = "http://mp.weixin.qq.com";
    private Long sleepTime;
    private List<Info> infoList;
    private String outputPath;

    private static java.net.Proxy proxy = null;

    public WxFireCrawler(String wxCrawlPath, String outputPath, Long sleepTime, List<Info> infoList) throws Exception {
        super(wxCrawlPath, false);
        this.outputPath = outputPath;
        this.sleepTime = sleepTime;
        this.infoList = infoList;
    }

    @Override
    public void visit(Page page, CrawlDatums next) {
        try {
            Thread.sleep(sleepTime != null ? sleepTime.longValue() : 5000L);
        } catch (InterruptedException e) {
            log.info("Failed to sleep, e={}", e);
        }
        if (page.matchType(WxCrawlerConstant.CrawlDatumType.ACCOUNT_SEARCH)) {
            parseSogouSearchResult(page, next);
        } else if (page.matchType(WxCrawlerConstant.CrawlDatumType.ARTICLE_LIST)) {
            parseWxArticleList(page, next);
        } else if (page.matchType(WxCrawlerConstant.CrawlDatumType.ARTICLE_DETAIL)) {
            parseWxArticleDetail(page);
        } else {
            log.info("未知CrawlDatumType");
        }
    }

    /**
     * 解析搜狗的微信公众号搜索结果页
     *
     * @param page
     * @param next
     */
    protected void parseSogouSearchResult(Page page, CrawlDatums next) {
        String accountName = page.meta(WxCrawlerConstant.CrawlMetaKey.ACCOUNT_NAME);
        log.info("Parsing sogou search result page，accountName: {}", accountName);
        Element accountLinkEle = page.select("p.tit>a").first();
        if (accountLinkEle == null) {
            log.info("accountName\"{}\" not exist", accountName);
            return;
        }
        //防止公众号名错误
        String detectedAccount = accountLinkEle.text().trim();
        if (!accountName.equals(detectedAccount)) {
            log.info("accountName \"{}\" not matched \"{}\"", accountName, detectedAccount);
            return;
        }
        //解析出公众号搜索结果页面中的URL
        String accountUrl = accountLinkEle.attr("abs:href");
        Element wxAccountEl = page.select("p.info>label[name='em_weixinhao']").first();
        if (wxAccountEl == null || StringUtils.isEmpty(wxAccountEl.text())) {
            log.info("accountId \"{}\" not exist", accountName);
            return;
        }
        next.add(new CrawlDatum(accountUrl, WxCrawlerConstant.CrawlDatumType.ARTICLE_LIST)
                .meta(WxCrawlerConstant.CrawlMetaKey.ACCOUNT_NAME, accountName)
                .meta(WxCrawlerConstant.CrawlMetaKey.ACCOUNT_ID, wxAccountEl.text()));
    }

    protected void setCrawlInfo(String key) {
    }

    /**
     * 是否爬取过。只有在打开断点续爬时才做检查
     *
     * @param key
     * @return
     */
    protected boolean hasCrawled(String key) {
        return false;
    }

    private List<ArticleSummary> parseArticleListByPage(Page page) throws Exception {
        int startIndex = page.html().indexOf(WxCrawlerConstant.ArticleList.ARTICLE_LIST_KEY) + WxCrawlerConstant.ArticleList.ARTICLE_LIST_KEY.length();
        int endIndex = page.html().indexOf(WxCrawlerConstant.ArticleList.ARTICLE_LIST_SUFFIX);
        String jsonStr = page.html().substring(startIndex, endIndex).trim();
        jsonStr = jsonStr.substring(0, jsonStr.length() - 1);
        JSONObject json = JSONObject.parseObject(jsonStr);
        return JSONArray.parseArray(json.getString("list"), ArticleSummary.class);
    }

    /**
     * 解析微信公众号主页文章列表
     *
     * @param page
     * @param next
     */
    protected void parseWxArticleList(Page page, CrawlDatums next) {

        String accountName = page.meta(WxCrawlerConstant.CrawlMetaKey.ACCOUNT_NAME);
        log.info("Parsing weixin article list page，accountName:{}", accountName);
        String accountId = page.meta(WxCrawlerConstant.CrawlMetaKey.ACCOUNT_ID);
        List<ArticleSummary> articles = null;
        try {
            articles = parseArticleListByPage(page);
        } catch (Exception e1) {
            log.info("Need to enter identifying code, {}", page.key());
        }
        if (CollectionUtils.isNotEmpty(articles)) {
            try {
                List<Info> infoList = new ArrayList<>();
                for (ArticleSummary articleSummary : articles) {
                    log.info("新闻标题:{}", JSON.toJSON(articleSummary));

                    CrawlDatum crawlDatum = parseArticleSummary(accountId, accountName, articleSummary.getApp_msg_ext_info(), articleSummary.getComm_msg_info());
                    if (crawlDatum != null) {
                        next.add(crawlDatum);
                    }

                    // 处理多条图文信息
                    if (WxCrawlerConstant.YES.equals(articleSummary.getApp_msg_ext_info().getIs_multi())
                            && StringUtils.isNotEmpty(articleSummary.getApp_msg_ext_info().getMulti_app_msg_item_list())) {
                        List<MsgExtInfo> subArticles = JSONArray.parseArray(articleSummary.getApp_msg_ext_info().getMulti_app_msg_item_list(), MsgExtInfo.class);
                        for (MsgExtInfo subArticle : subArticles) {
                            CrawlDatum subCrawlDatum = parseArticleSummary(accountId, accountName, subArticle, articleSummary.getComm_msg_info());
                            if (subCrawlDatum != null) {
                                next.add(subCrawlDatum);
                            }
                        }
                    }
                }

            } catch (Exception e) {
                log.info("Failed to parseWxArticleList，exception={}", e);
            }
        }
    }

    private CrawlDatum parseArticleSummary(String accountId, String accountName, MsgExtInfo appMsgExtInfoObj, CommMsgInfo commMsgInfoObj) {
        if (appMsgExtInfoObj == null) {
            return null;
        }
        if (StringUtils.isEmpty(appMsgExtInfoObj.getTitle()) || StringUtils.isEmpty(appMsgExtInfoObj.getContent_url())) {
            log.info("Article not exist, skip");
            return null;
        }
        String publishDate = DateTimeUtils.parseDate(commMsgInfoObj.getDatetime() + "000");
        String key = accountId + "###" + appMsgExtInfoObj.getTitle();
        if (hasCrawled(key)) {
            log.info("Article has crawled, skip, accountName：{}，article：{}", accountName, appMsgExtInfoObj.getTitle());
            return null;
        }
        String cover = appMsgExtInfoObj.getCover();
        String author = appMsgExtInfoObj.getAuthor();
        String newURL = ResourceTransferUtils.getCoverImageURL(cover);
        if (StringUtils.isNotEmpty(newURL)) {
            cover = newURL;
        } else {
            log.info("Failed to CoverImage resourceTranslation, article: {}, cover: {}", appMsgExtInfoObj.getTitle(), cover);
        }
        String articleUrl = appMsgExtInfoObj.getContent_url().replaceAll("&amp;", "&");
        if (!appMsgExtInfoObj.getContent_url().startsWith("http")) {
            articleUrl = WxCrawlerConstant.ARTICLE_URL_PREFIX + (articleUrl.startsWith("/") ? "" : "/") + articleUrl;
        }
        return new CrawlDatum(articleUrl, WxCrawlerConstant.CrawlDatumType.ARTICLE_DETAIL).key(key)
                .meta(WxCrawlerConstant.CrawlMetaKey.ACCOUNT_NAME, accountName)
                .meta(WxCrawlerConstant.CrawlMetaKey.ACCOUNT_ID, accountId)
                .meta(WxCrawlerConstant.CrawlMetaKey.ARTICLE_TITLE, appMsgExtInfoObj.getTitle())
                .meta(WxCrawlerConstant.CrawlMetaKey.ARTICLE_COVER, cover)
                .meta(WxCrawlerConstant.CrawlMetaKey.ARTICLE_DIGEST, appMsgExtInfoObj.getDigest())
                .meta(WxCrawlerConstant.CrawlMetaKey.ARTICLE_PUBLISH_DATE, publishDate)
                .meta(WxCrawlerConstant.CrawlMetaKey.ARTICLE_AUTHOR, author);
    }

    /**
     * 解析微信公众号文章详情页
     *
     * @param page
     */
    protected void parseWxArticleDetail(Page page) {
        try {
            String accountName = page.meta(WxCrawlerConstant.CrawlMetaKey.ACCOUNT_NAME);
            String accountId = page.meta(WxCrawlerConstant.CrawlMetaKey.ACCOUNT_ID);
            String cover = page.meta(WxCrawlerConstant.CrawlMetaKey.ARTICLE_COVER);
            String title = page.meta(WxCrawlerConstant.CrawlMetaKey.ARTICLE_TITLE);
            String digest = page.meta(WxCrawlerConstant.CrawlMetaKey.ARTICLE_DIGEST);
            String publishDate = page.meta(WxCrawlerConstant.CrawlMetaKey.ARTICLE_PUBLISH_DATE);
            String author = page.meta(WxCrawlerConstant.CrawlMetaKey.ARTICLE_AUTHOR);

            if (hasCrawled(page.key())) {
                log.info("This article has crawled, skip, accountName：{}，article：{}", accountName, title);
                return;
            }

            log.info("Parsing weixin article detail page，accountName：{}, article：{}", accountName, title);

            Document sourceDoc = Jsoup.parse(page.html());
            Document targetDoc = SampleHTMLUtils.getSampleDocument();
            targetDoc.title(title);

            try {
                targetDoc.select(WxCrawlerConstant.HTMLElementSelector.RICH_MEDIA_CONTENT).first()
                        .appendChild(sourceDoc.select(WxCrawlerConstant.HTMLElementSelector.RICH_MEDIA_CONTENT).first().clone());

            } catch (Exception ee) {
                log.error("targetDoc 异常");
            }

            // 处理图片节点
            Elements imgElements = targetDoc.select("img");
            if (CollectionUtils.isNotEmpty(imgElements)) {
                for (Element imgElement : imgElements) {
                    parseImageElement(imgElement);
                }
            }

            // 处理音频节点
            Elements mpvoiceElements = targetDoc.select("mpvoice");
            if (CollectionUtils.isNotEmpty(mpvoiceElements)) {
                for (Element voiceElement : mpvoiceElements) {
                    parseVoiceElement(voiceElement);
                }
            }

            // 处理视频节点
            Elements videoElements = targetDoc.select("iframe.video_iframe");
            if (CollectionUtils.isNotEmpty(videoElements)) {
                for (Element videoElement : videoElements) {
                    parseVideoElement(videoElement);
                }
            }

            // 处理背景图属性
            Elements backgroundElements = targetDoc.getElementsByAttributeValueMatching("style",
                    "background-image: url");
            if (CollectionUtils.isNotEmpty(backgroundElements)) {
                for (Element styleElement : backgroundElements) {
                    String value = parseBackgroundImageURL(styleElement.attr("style"));
                    if (StringUtils.isNotEmpty(value)) {
                        styleElement.attr("style", value);
                    }
                }
            }
            //解决“此图片来自微信公众平台未经允许不可引用”的方法
            Element element = new Element("meta");
            element.attr("name", "referrer");
            element.attr("content", "never");
            targetDoc.head().insertChildren(1, element);
            String content = targetDoc.outerHtml();

            log.info("accountName: {}, accountId: {}, cover: {}, title: {}, author: {}, publishDate: {}, digest: {}",
                    accountName, accountId, cover, title, author, publishDate, digest);

            String contentUrl = new MD5().getMD5ofStr(title) + ".html";
            if (StringUtils.isNotEmpty(outputPath)) {
                FileUtils.writeStringToFile(new File(getOutputAccountPath("SecurityChina"),
                        com.viewol.crawl.common.utils.FileUtils.normalizeFileName(contentUrl)), content, "UTF-8");
            }

            Info info = new Info();
            info.setTitle(title);
            info.setSummary(digest);
            SimpleDateFormat dft = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            info.setPubTime(dft.parse(publishDate));
            info.setPicUrl(cover);
            info.setContentUrl(contentUrl);
            info.setCreateTime(new Date());
            info.setClassify(2);//消防展
            infoList.add(info);
            log.info("已抓取文章数量：" + infoList.size());
//            setCrawlInfo(page.key());

        } catch (Exception ex) {
            log.info("Failed to parseWxArticleDetail, exception={}", ex);
        } finally {
            setCrawlInfo(page.key());
        }
    }

    private File getOutputAccountPath(String accountId) {
        File folder = new File(outputPath, accountId);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        return folder;
    }

    /**
     * 替换style属性中background-img的外部资源引用
     *
     * @param style
     * @return
     */
    private String parseBackgroundImageURL(String style) {
        if (StringUtils.isEmpty(style) || style.indexOf("background-image: url(") == -1) {
            return style;
        }
        style = style.replaceAll("&quot;", "\"");
        String regex = "background-image: url\\(\"(.*?)\"\\)";
        Matcher m = java.util.regex.Pattern.compile(regex).matcher(style);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String url = m.group(1);
            if (url.startsWith("//res.wx.qq.com")) {
                url = "http:" + url;
            }
            String newURL = ResourceTransferUtils.getStyleResourceURL(url);
            if (StringUtils.isNotEmpty(newURL)) {
                String newValue = String.format("background-image: url(\"%s\")", newURL);
                m.appendReplacement(sb, newValue);
            } else {
                log.info("Failed to background url resourceTranslation, url={}", url);
            }
        }
        m.appendTail(sb);
        return sb.toString();
    }

    /**
     * 处理mpvoice音频节点
     *
     * @param voiceElement
     */
    private void parseVoiceElement(Element voiceElement) {
        String voiceURL = WxCrawlerConstant.VOICE_URL + voiceElement.attr("voice_encode_fileid");
        String newURL = ResourceTransferUtils.getVoiceURL(voiceURL);
        if (StringUtils.isNotEmpty(newURL)) {
            voiceElement.append("<audio src=\"" + newURL + "\">您的浏览器不支持audio标签</audio>");
        } else {
            log.info("Failed to voice resourceTranslation, voiceURL={}", voiceURL);
        }
    }

    /**
     * 处理腾讯视频节点
     *
     * @param videoElement
     */
    private void parseVideoElement(Element videoElement) {
        videoElement.attr("width", "640").attr("height", "498");
        videoElement.attr("frameborder", "0");

        String url = videoElement.attr("data-src");
        if (StringUtils.isEmpty(url)) {
            url = videoElement.attr("src");
        }
        url.replaceFirst("https://v.qq.com/iframe/preview.html", "https://v.qq.com/iframe/player.html");
        videoElement.attr("data-src", url).attr("src", url);
    }


    /**
     * 替换图片中的链接
     *
     * @param imgElement
     */
    private void parseImageElement(Element imgElement) {

        // Step1: 处理 data-src 属性
        String imgURL = imgElement.attr("data-src");
        if (StringUtils.isNotEmpty(imgURL)) {
            String newURL = ResourceTransferUtils.getNewImageUrl(imgURL);
            if (StringUtils.isNotEmpty(newURL)) {
                imgElement.attr("data-src", newURL);
            } else {
                log.info("Failed to Image data-src resourceTranslation, imgURL={}", imgURL);
            }
        }

        // Step2: 处理 src 属性
        String imgURL2 = imgElement.attr("src");
        if (StringUtils.isNotEmpty(imgURL2)) {
            if (imgURL2.equals(imgURL)) {
                imgElement.attr("src", imgElement.attr("data-src"));
            } else {
                String newURL = ResourceTransferUtils.getNewImageUrl(imgURL2);
                if (StringUtils.isNotEmpty(newURL)) {
                    imgElement.attr("src", newURL);
                } else {
                    log.info("Failed to Image src resourceTranslation, imgURL2={}", imgURL2);
                }
            }
        } else {
            imgElement.attr("src", imgElement.attr("data-src"));
        }
    }

    @Override
    public void start(int depth) throws Exception {
        super.start(depth);
    }

    /**
     * 根据公众号名称设置种子URL
     *
     * @param account
     * @throws UnsupportedEncodingException
     */
    public void addAccount(String account) throws UnsupportedEncodingException {
        String seedUrl = WxCrawlerConstant.SEARCH_URL + URLEncoder.encode(account, "utf-8");
        CrawlDatum seed = new CrawlDatum(seedUrl, WxCrawlerConstant.CrawlDatumType.ACCOUNT_SEARCH).meta(WxCrawlerConstant.CrawlMetaKey.ACCOUNT_NAME, account);
        addSeed(seed);
    }

    @Override
    public Page getResponse(CrawlDatum crawlDatum) throws Exception {
        while (proxy==null){
            proxy = this.getProxy();
            Thread.sleep(1000);
            log.info("获取代理proxy:{} ",proxy);
        }
        HttpRequest request = null;
        Page page = null;
        while (true){
            try{
                request = new HttpRequest(crawlDatum,proxy);
                page = request.responsePage();
                String html = page.html();
                if(isRand(html)){
                    log.info("需要输入验证码 ， 重新获取代理");
                    proxy = this.getProxy();
                    log.info("重新获取代理proxy:{}",proxy);
                    Thread.sleep(1000);
                    continue;
                }
                break;
            }catch (Exception e){
                e.printStackTrace();
                log.info("代理proxy:{} 不可用 ",proxy);
                proxy = this.getProxy();
                log.info("重新获取代理proxy:{}",proxy);
            }finally {

            }
        }

        return request.responsePage();
    }


    private boolean isRand(String html) {
        if (html.indexOf("为了保护你的网络安全，请输入验证码") > 0 && html.indexOf("验证码有误") > 0) {
            return true;
        }
        return false;
    }

    private java.net.Proxy getProxy() {
        List<Proxy> list = xiongmaodaili();
        if (list != null && list.size() > 0) {
            java.net.Proxy proxy = new java.net.Proxy(java.net.Proxy.Type.HTTP, new InetSocketAddress(list.get(0).getIp(), Integer.parseInt(list.get(0).getPort())));
            return proxy;
        }
        return null;
    }


    private List<Proxy> xiongmaodaili() {
        List<Proxy> ipList = new ArrayList<>();
        String url = "http://www.xiongmaodaili.com/xiongmao-web/api/glip?secret=678849a490966df97c3915ea15159d33&orderNo=GL20180911140922dNP3VKgg&count=1&isTxt=0&proxyType=1";

        try {
            //熊猫代理要求1秒钟只能请求一次接口，此处简单处理，每次请求前睡眠1秒钟
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Response<String> response = HttpUtil.sendGet(url, null, "UTF-8");
        if (response != null && "0000".equals(response.getCode())) {
            String json = response.getT();
            JSONObject jsonObject = JSONObject.parseObject(json);
            if ("0".equals(jsonObject.getString("code"))) {
                JSONArray ipArray = jsonObject.getJSONArray("obj");

                for (int i = 0; i < ipArray.size(); i++) {
                    JSONObject ipObj = (JSONObject) ipArray.get(i);
                    Proxy proxy = new Proxy();
                    proxy.setIp(ipObj.getString("ip"));
                    proxy.setPort(ipObj.getString("port"));
//                    boolean ret = testProxy(proxy);
//                    if(ret){
//                        ipList.add(proxy);
//                    }
                    log.info(proxy.toString());
                    ipList.add(proxy);
                }
            } else {
                log.info("获取代理IP失败: status:" + jsonObject.getString("status") + ", msg:" + jsonObject.getString("msg"));
            }
        } else {
            log.info("获取代理IP失败: msg: " + response.getMsg());
        }

        return ipList;
    }

}
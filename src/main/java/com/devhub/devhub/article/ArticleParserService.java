package com.devhub.devhub.article;

import com.devhub.devhub.common.HtmlCleaner;
import com.devhub.devhub.post.Tag;
import com.devhub.devhub.post.TagRepository;
import com.devhub.devhub.user.User;
import com.devhub.devhub.user.UserService;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

@Service
public class ArticleParserService {

    private static final Logger log = LoggerFactory.getLogger(ArticleParserService.class);
    private static final int LIMIT_PER_SOURCE = 10;

    private final ParserProperties properties;
    private final ArticleRepository articleRepository;
    private final TagRepository tagRepository;
    private final UserService userService;
    private final HtmlCleaner htmlCleaner;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    public ArticleParserService(ParserProperties properties,
                                ArticleRepository articleRepository,
                                TagRepository tagRepository,
                                UserService userService,
                                HtmlCleaner htmlCleaner) {
        this.properties = properties;
        this.articleRepository = articleRepository;
        this.tagRepository = tagRepository;
        this.userService = userService;
        this.htmlCleaner = htmlCleaner;
    }

    public int parseAll(String username) {
        User author = userService.getByUsername(username);
        int added = 0;
        for (ParserProperties.Source source : properties.sources()) {
            try {
                added += parseSource(source, author);
            } catch (Exception e) {
                log.warn("Не удалось прочитать источник {}: {}", source.name(), e.getMessage());
            }
        }
        return added;
    }

    private int parseSource(ParserProperties.Source source, User author) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create(source.url()))
                .timeout(Duration.ofSeconds(15))
                .header("User-Agent", "Mozilla/5.0 (compatible; DevHubBot/1.0)")
                .GET()
                .build();
        HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
        if (response.statusCode() != 200) {
            throw new IllegalStateException("ответ сервера " + response.statusCode());
        }

        SyndFeed feed = new SyndFeedInput().build(new XmlReader(new ByteArrayInputStream(response.body())));

        Tag tag = tagRepository.findByName(source.name())
                .orElseGet(() -> tagRepository.save(new Tag(source.name())));

        int added = 0;
        List<SyndEntry> entries = feed.getEntries().stream().limit(LIMIT_PER_SOURCE).toList();
        for (SyndEntry entry : entries) {
            String link = entry.getLink();
            if (link == null || entry.getTitle() == null || articleRepository.existsBySourceUrl(link)) {
                continue;
            }
            String text = toEditableHtml(rawContent(entry));
            if (text.isEmpty()) {
                continue;
            }

            Article article = new Article();
            article.setTitle(shorten(entry.getTitle().trim(), 200));
            article.setText(text);
            article.setSourceUrl(link);
            article.setAuthor(author);
            article.getTags().add(tag);
            articleRepository.save(article);
            added++;
        }
        log.info("Источник {}: добавлено статей {}", source.name(), added);
        return added;
    }

    private String rawContent(SyndEntry entry) {
        if (!entry.getContents().isEmpty() && entry.getContents().get(0).getValue() != null) {
            return entry.getContents().get(0).getValue();
        }
        return entry.getDescription() != null ? entry.getDescription().getValue() : "";
    }

    private String toEditableHtml(String html) {
        if (html == null || html.isBlank()) {
            return "";
        }
        Document doc = Jsoup.parseBodyFragment(html);
        doc.select("script, style").remove();

        StringBuilder result = new StringBuilder();
        for (Element el : doc.body().select("h1, h2, h3, h4, p, li, pre")) {
            String text = el.tagName().equals("pre") ? el.wholeText().strip() : el.text().strip();
            if (text.isEmpty()) {
                continue;
            }
            String tagName = switch (el.tagName()) {
                case "h1", "h2", "h3", "h4" -> "h3";
                case "pre" -> "pre";
                default -> "p";
            };
            result.append(new Element(tagName).text(text).outerHtml()).append("\n");
        }

        if (result.isEmpty()) {
            String plain = doc.body().text().strip();
            if (!plain.isEmpty()) {
                result.append(new Element("p").text(plain).outerHtml());
            }
        }
        return htmlCleaner.clean(result.toString()).trim();
    }

    private String shorten(String value, int max) {
        return value.length() <= max ? value : value.substring(0, max - 1) + "…";
    }
}

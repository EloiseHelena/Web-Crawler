// HtmlParser.groovy
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements

class HtmlParser {
    static Document parse(String htmlContent) {
        return Jsoup.parse(htmlContent)
    }

    static void printLinks(Document document) {
        Elements links = document.select("a[href]")
        for (link in links) {
            println "Link: ${link.attr('href')} - Texto: ${link.text()}"
        }
    }

}

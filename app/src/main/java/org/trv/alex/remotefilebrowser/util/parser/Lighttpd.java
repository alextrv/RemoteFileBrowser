package org.trv.alex.remotefilebrowser.util.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.trv.alex.remotefilebrowser.util.FileProperties;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class Lighttpd implements Parser {

    public List<FileProperties> getFileList(String url) {
        List<FileProperties> fileList = new ArrayList<>();
        try {
            URLConnection connection = new URL(url).openConnection();
            Scanner scanner = new Scanner(connection.getInputStream());
            StringBuilder html = new StringBuilder();
            while (scanner.hasNextLine()) {
                html.append(scanner.nextLine());
            }
            // Document doc = Jsoup.connect(url).get();
            Document doc = Jsoup.parse(html.toString());
            Elements elements = doc.select("tbody > tr");
            for (Element element : elements) {
                String name = element.select(".n").text();
                if (name.equals(PARENT_DIR_DOTS)) {
                    continue;
                }
                String type = element.select(".t").text();
                String href = element.select(".n > a").attr("href");
                String absoluteURL = new URL(new URL(url), href).toString();
                String modified = element.select(".m").text();
                String size = element.select(".s").text();
                FileProperties fp = new FileProperties(absoluteURL, name, modified, size, type);
                fileList.add(fp);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
        return fileList;
    }

}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package HtmlParser;

import Keyboards.jaxb.Keyboard;
import Keyboards.jaxb.KeyboardKList;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.util.XMLEventAllocator;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

/**
 *
 * @author Tel Pai
 */
public class StaxParser {

    public StaxParser() {
    }
    static KeyboardKList keyboardList = new KeyboardKList();
    static XMLEventAllocator allocator = null;

    //Test parser
//    public static void main(String[] args) {
//        StaxParser s = new StaxParser();
//        s.htmlToFile("asd.html", "https://www.phongcachxanh.vn/shop/category/ban-phim-co-2");
//        s.parseObject("asd.html");
//        s.saveToHtml("web/WEB-INF", keyboardList);
//    }

    public void htmlToFile(String filePath, String uri) {

        Writer writer = null;
        try {
            URL url = new URL(uri);
            URLConnection uc = url.openConnection();
            uc.addRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 3.0");
            InputStream is = uc.getInputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String inputLine;
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath), "UTF-8"));
            while ((inputLine = in.readLine()) != null) {
                sb.append(inputLine + "\n");
            }
            removeJSScriptFromHtml(sb);
            String htmlString = getBodyTagFromHtml(sb);
            htmlString = StandardlizeTagHtml(htmlString);
            htmlString = htmlString.replaceAll("&", "&amp;");
            writer.write(htmlString);
            in.close();
            writer.close();
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void parseObject(String filePath) {
        XMLInputFactory fact = XMLInputFactory.newInstance();
        fact.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, false);
        fact.setProperty(XMLInputFactory.IS_VALIDATING, false);
        XMLEventReader reader = null;

        int idGen = 0;
        String img = "";
        String name = "";
        String price = "";
        try {
            reader = fact.createXMLEventReader(new InputStreamReader(new FileInputStream(filePath), "UTF-8"));
            boolean isProductTag = false;
            while (reader.hasNext()) {
                XMLEvent eve = reader.nextEvent();
                if (eve.isEndElement()) {
                    EndElement ee = (EndElement) eve;
                    if (ee.getName().toString().equals("td")) {
                        isProductTag = false;
                        Keyboard keyboard = new Keyboard();
                        keyboard.setId((idGen++) + "");
                        keyboard.setModel(name);
                        keyboard.setPrice(price);
                        keyboard.setImgUrl(img);
                        keyboardList.getKeyboardItem().add(keyboard);
                    }
                }
                if (eve.isStartElement()) {
                    StartElement se = (StartElement) eve;
                    if (se.getName().toString().equals("td")) {
                        Attribute attr = se.getAttributeByName(new QName("class"));
                        if (attr != null) {
                            if (attr.getValue().contains("oe_product")) {
                                isProductTag = true;
                            }
                        }
                    }
                    if (se.getName().toString().equals("img") && isProductTag) {
                        Attribute imgSrc = se.getAttributeByName(new QName("src"));
                        img = "https://www.phongcachxanh.vn" + imgSrc.getValue();
                        System.out.println(img);
                    }
                    if (se.getName().toString().equals("a") && isProductTag) {
                        Attribute attr = se.getAttributeByName(new QName("itemprop"));
                        if (attr != null) {
                            if (attr.getValue().contains("name")) {
                                XMLEvent nextEvent = reader.nextEvent();
                                name = nextEvent.asCharacters().toString();
                                System.out.println(name);
                            }
                        }
                    }
                    if (se.getName().toString().equals("span") && isProductTag) {
                        Attribute priceAttr = se.getAttributeByName(new QName("itemprop"));
                        if (priceAttr != null) {
                            if (priceAttr.getValue().equals("price")) {
                                XMLEvent nextEvent = reader.nextEvent();
                                price = nextEvent.asCharacters().toString();
                                System.out.println(price);
                            }
                        }
                    }

                }
            }
        } catch (XMLStreamException ex) {
            Logger.getLogger(StaxParser.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(StaxParser.class.getName()).log(Level.SEVERE, null, ex);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(StaxParser.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void removeJSScriptFromHtml(StringBuilder sb) {
        while (sb.indexOf("<script") != -1) {
            int startTagIndex = sb.indexOf("<script");
            int endTagIndex = sb.indexOf("</script>", startTagIndex) + 9;
            sb.delete(startTagIndex, endTagIndex);
        }
    }

    public String getBodyTagFromHtml(StringBuilder sb) {
        int startTagIndex = sb.indexOf("<body");
        int endTagIndex = sb.indexOf("</body>", startTagIndex) + 7;
        String result = sb.substring(startTagIndex, endTagIndex);
        return result;
    }

    public String StandardlizeTagHtml(String html) {
        String htmlString = "" + html;
        int count = (htmlString.length() - htmlString.replace("<input", "").length()) / 6;
        int startInputTagIndex = 0;
        int endInputTagIndex = 0;
        String fixedhtmlString = "";
        for (int i = 0; i < count; i++) {
            startInputTagIndex = htmlString.indexOf("<input");
            endInputTagIndex = htmlString.indexOf("/>", startInputTagIndex) + 2;
            String assumedTag = htmlString.substring(startInputTagIndex, endInputTagIndex);
            String fixTag = assumedTag.replaceAll("/>", "></input>");
            if (startInputTagIndex != -1) {
                int a = htmlString.length();
                fixedhtmlString = htmlString.replaceAll(assumedTag, fixTag);
                htmlString = fixedhtmlString;
            }
        }
        return htmlString;
    }

    public void saveToXml(String xmlDataFilePath, KeyboardKList itemList) {
        try {
            JAXBContext context = JAXBContext.newInstance(itemList.getClass());
            Marshaller marshall = context.createMarshaller();
            marshall.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
            marshall.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshall.marshal(itemList, new File(xmlDataFilePath));
        } catch (PropertyException ex) {
            Logger.getLogger(StaxParser.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JAXBException ex) {
            Logger.getLogger(StaxParser.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

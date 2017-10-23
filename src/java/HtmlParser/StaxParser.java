/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package HtmlParser;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.util.XMLEventAllocator;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

/**
 *
 * @author Tel Pai
 */
public class StaxParser {

    public StaxParser() {
    }

    static XMLEventAllocator allocator = null;
    
    public static void main (String[] args) {
        StaxParser s = new StaxParser();
        s.PimmykeyboardParser("/asd");
    }
    public void PimmykeyboardParser(String filePath) {

        Writer writer = null;
        try {
            URL url = new URL("https://pimpmykeyboard.com/all-products/");
            URLConnection uc = url.openConnection();
            uc.addRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 3.0");
            InputStream is = uc.getInputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            String inputLine;
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath), "UTF-8"));
            while ((inputLine = in.readLine()) != null) {
                writer.write(inputLine + "\n");
            }
            in.close();
            writer.close();
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            XMLInputFactory fact = XMLInputFactory.newInstance();
            fact.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, false);
            fact.setProperty(XMLInputFactory.IS_VALIDATING, false);
            XMLEventReader reader = null;
            try {
                reader = fact.createXMLEventReader(new InputStreamReader(new FileInputStream(filePath), "UTF-8"));
                boolean isProductTag = false;
                while (reader.hasNext()) {
                    XMLEvent eve = reader.nextEvent();
                    if (eve.isStartElement()) {
                        StartElement se = (StartElement) eve;
                        if (se.getName().toString().equals("ul")) {
                            Attribute attr = se.getAttributeByName(new QName("class"));
                            if (attr != null) {
                                if (attr.getValue().equals("ProductList ")) {
                                    isProductTag = true;
                                    System.out.println(isProductTag);
                                }
                            }
                        }
                    }
                }
            } catch (FileNotFoundException ex) {
                Logger.getLogger(StaxParser.class.getName()).log(Level.SEVERE, null, ex);
            } catch (XMLStreamException ex) {
                Logger.getLogger(StaxParser.class.getName()).log(Level.SEVERE, null, ex);
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(StaxParser.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }
}

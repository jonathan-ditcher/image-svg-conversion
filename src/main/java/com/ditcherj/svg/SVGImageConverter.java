package com.ditcherj.svg;

import org.apache.batik.svggen.SVGGeneratorContext;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xhtmlrenderer.simple.Graphics2DRenderer;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;

/**
 * Created by jon on 27/03/2017.
 *
 * Converts images to svg, not very efficent..
 *
 */
public class SVGImageConverter {

    private static final Logger logger = LoggerFactory.getLogger(SVGImageConverter.class);
    private static final String TEMPLATE_FILE = "template.html";

    private int svgPrecision = 12; // max 0 - 12

    public SVGImageConverter() {
    }

    public void parse(InputStream inputStream, OutputStream outputStream) throws ParserConfigurationException, IOException, SAXException {
        this.parse(inputStream, new OutputStreamWriter(outputStream));
    }

    public void parse(InputStream inputStream, Writer writer) throws ParserConfigurationException, IOException, SAXException {
        BufferedImage inputImage = ImageIO.read(inputStream);
        this.parse(inputImage, writer);
    }

    public void parse(BufferedImage inputImage, Writer writer) throws ParserConfigurationException, IOException, SAXException {
        logger.trace("");

        long start = System.currentTimeMillis();

        String content = this.getContent(inputImage);
        logger.trace("content[{}]", content);

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = dbFactory.newDocumentBuilder();

        InputSource source = new InputSource(new StringReader(content));
        Document xhtmlContent = documentBuilder.parse(source);

        Graphics2DRenderer renderer = new Graphics2DRenderer();
        renderer.setDocument(xhtmlContent, "");

        Document svgDocument = documentBuilder.newDocument();

        this.render(svgDocument, renderer, writer);

        logger.trace("finished in [" + (System.currentTimeMillis() - start) + "]ms");
    }

    private String getContent(BufferedImage inputImage) throws IOException {
        logger.trace("");

        String imageSource = this.encodeImageToBase64(inputImage);

        ClassLoader classLoader = getClass().getClassLoader();
        File templateFile = new File(classLoader.getResource(TEMPLATE_FILE).getFile());

        String template = FileUtils.readFileToString(templateFile);
        String content = template.replace("${src}", imageSource);

        return content;
    }

    private String encodeImageToBase64(BufferedImage inputImage) throws IOException {
        logger.trace("");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(inputImage, "PNG", out);
        byte[] bytes = out.toByteArray();

        String base64String = new String(Base64.encodeBase64(bytes), "UTF-8");
        String src = "data:image/png;base64," + base64String;

        return src;
    }

    private void render(Document svgDocument, Graphics2DRenderer renderer, Writer writer) throws IOException {
        logger.trace("svgPrecision[{}]", this.svgPrecision);

        int width = 10; // doesnt seem to do much
        int height = 10; // doesnt seem to do much

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = (Graphics2D) image.getGraphics();

        SVGGeneratorContext generatorContext = SVGGeneratorContext.createDefault(svgDocument);
        generatorContext.setEmbeddedFontsOn(true);
        generatorContext.setPrecision(this.svgPrecision);

        SVGGraphics2D svgGenerator = new SVGGraphics2D(generatorContext, false);
        renderer.layout(g2d, new Dimension(width, height));
        renderer.render(svgGenerator);

        svgGenerator.stream(writer, true);
        writer.flush();
        writer.close();
    }

    public void setSvgPrecision(int svgPrecision) {
        this.svgPrecision = svgPrecision;
    }
}

/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package br.gafs.calvinista.util;

import lombok.AllArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;
import org.ghost4j.renderer.SimpleRenderer;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;


/**
 *
 * @author Gabriel
 */
public class PDFToImageConverterUtil {
    private static final double LIMIT_HEIGHT = 1000;
    private static final double LIMIT_WIDTH = 1000;

    private static final long TIMEOUT = 10000;

    private final static Logger LOGGER = Logger.getLogger(PDFToImageConverterUtil.class.getSimpleName());

    public static PDFConverter convert(File pdf, int index, int limit){
        return new PDFConverter(pdf, index, limit);
    }
    @AllArgsConstructor
    public static class PDFConverter {
        private File pdf;
        private int index;
        private int limit;


        public int forEachPage(PageHandler handler) throws IOException {
            LOGGER.info("Abrindo documento " + pdf.getName());

            PDDocument document = PDDocument.load(pdf);

            PDFRenderer pdfRenderer = new PDFRenderer(document);

            // create renderer
            SimpleRenderer renderer = new SimpleRenderer();

            int pageCount;
            try {
                pageCount = document.getNumberOfPages();

                int total = limit > 0 ? Math.min(pageCount, index + limit) : pageCount;

                for (int i=index;i<total;i++) {
                    long inicio = System.currentTimeMillis();

                    PDPage page = document.getPage(i);

                    LOGGER.info("Iniciando processamento de " + pdf.getName() + " - " + (i+1));

                    BufferedImage image = pdfRenderer.renderImageWithDPI(i, 300);

                    LOGGER.info("Processamento de " + pdf.getName() + " - " + (i+1) + " levou " + (System.currentTimeMillis() - inicio) + " millis.");

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();

                    ImageIOUtil.writeImage(image, "jpeg", baos, 300);

                    handler.handle(i, baos.toByteArray());
                }
            } catch (Exception e) {
                throw new IOException(e);
            }

            return pageCount;
        }
    }

    public interface PageHandler {
        void handle(int page, byte[] data) throws IOException;
    }

}
